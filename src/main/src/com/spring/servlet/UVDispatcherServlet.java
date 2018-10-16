package com.spring.servlet;
/*
 * @author uv
 * @date 2018/9/28 19:51
 * 调度中心，分发请求，IOC
 */

import com.alibaba.fastjson.JSON;
import com.spring.annotation.UVAutowried;
import com.spring.annotation.UVController;
import com.spring.annotation.UVRequestMapping;
import com.spring.annotation.UVResponseBody;
import com.spring.annotation.UVService;
import com.spring.core.MethodHandler;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.MethodParameterNamesScanner;

public class UVDispatcherServlet extends HttpServlet {

    //spring配置文件
    private Properties properties = new Properties();
    //存放所有带注解的类
    private List<String> classNameList = new ArrayList<>();
    //IOC容器,通过类型注入
    private Map<String, Object> IOCByType = new HashMap<>();
    //当通过类型找不到对应实例时，通过名称注入(名称相同时会覆盖之前的值，这里就不处理了)
    private Map<String, Object> IOCByName = new HashMap<>();
    //url 到controller方法的映射
    private Map<String, MethodHandler> urlHandler = new HashMap<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //6、处理请求，执行相应的方法
        doHandler(req, resp);
    }

    @Override
    public void init() throws ServletException {

        System.out.println("servlet开始初始化");
        //1、加载配置文件 spring-config.properties,获取扫描路径
        doLoadConfig();
        //2、扫描配置的路径下的带有注解的类
        doScanner(properties.getProperty("basepackage"));
        //3、初始化所有的类，被放入到IOC容器中
        doPutIoc();
        //4、实现@UVAutowried自动注入
        doAutowried();
        //5、初始化HandlerMapping，根据url映射不同的controller方法
        doMapping();
        System.out.println("servlet初始化完成");
    }

    //1、加载配置文件 spring-config.properties,获取扫描路径
    private void doLoadConfig() {
        //ServletConfig:代表当前Servlet在web.xml中的配置信息
        ServletConfig config = this.getServletConfig();
        String configLocation = config.getInitParameter("contextConfigLocation");
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(configLocation);
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //2、扫描配置的路径下的带有注解的类
    private void doScanner(String path) {
        //java文件
        if (path.endsWith(".class")) {
            //获取到带有包路径的类名
            String className = path.substring(0, path.lastIndexOf(".class"));
            //扫描的类
            classNameList.add(className);
            return;
        }
        URL url = this.getClass().getClassLoader().getResource("/" + path.replaceAll("\\.", "/"));
        //是包路径，继续迭代
        File file = new File(url.getFile());
        File[] files = file.listFiles();
        for (File f : files) {
            doScanner(path + "." + f.getName());
        }
    }

    //3、初始化所有的类，被放入到IOC容器中
    private void doPutIoc() {
        if (classNameList.isEmpty()) {
            return;
        }
        try {
            for (String className : classNameList) {
                //反射获取实例对象
                Class<?> clazz = Class.forName(className);
                //IOC容器key命名规则：1.默认类名首字母小写  2.使用用户自定义名，如 @UVService("abc") 3.如果service实现了接口，可以使用接口作为key

                //controller,service注解类
                if (clazz.isAnnotationPresent(UVController.class)) {
                    UVController uvController = clazz.getAnnotation(UVController.class);
                    String beanName = uvController.value().trim();
                    //如果用户没有定义名称，使用名首字母小写
                    if (StringUtils.isBlank(beanName)) {
                        beanName = lowerFirstCase(clazz.getSimpleName());
                    }
                    //byName
                    Object instance = clazz.newInstance();
                    IOCByName.put(beanName, instance);
                    //byType
                    IOCByType.put(clazz.getName(), instance);
                } else if (clazz.isAnnotationPresent(UVService.class)) {
                    UVService uvService = clazz.getAnnotation(UVService.class);
                    String beanName = uvService.value().trim();
                    //如果用户没有定义名称，使用名首字母小写
                    if (StringUtils.isBlank(beanName)) {
                        beanName = lowerFirstCase(clazz.getSimpleName());
                    }
                    //byName
                    Object instance = clazz.newInstance();
                    IOCByName.put(beanName, instance);
                    //byType
                    IOCByType.put(clazz.getName(), instance);
                    //如果service实现了接口，可以使用接口作为key
                    Class<?>[] interfaces = clazz.getInterfaces();
                    for (Class<?> interf : interfaces) {
                        IOCByName.put(lowerFirstCase(interf.getSimpleName()), instance);
                        IOCByType.put(interf.getName(), instance);
                    }
                } else {
                    continue;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //4、实现@UVAutowried自动注入
    private void doAutowried() {
        if (IOCByName.isEmpty() && IOCByType.isEmpty()) {
            return;
        }
        for (Entry<String, Object> entry : IOCByType.entrySet()) {
            //获取变量
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : fields) {
                //private、protected修饰的变量可访问
                field.setAccessible(true);

                if (!field.isAnnotationPresent(UVAutowried.class)) {
                    continue;
                }
                Object instance = null;
                String beanName = field.getType().getName();
                String simpleName = lowerFirstCase(field.getType().getSimpleName());
                //首先根据Type注入，没有实例时根据Name，否则抛出异常
                if (IOCByType.containsKey(beanName)) {
                    instance = IOCByType.get(beanName);
                } else if (IOCByName.containsKey(simpleName)) {
                    instance = IOCByName.get(simpleName);
                } else {
                    throw new RuntimeException("not find class to autowire");
                }
                try {
                    //向obj对象的这个Field设置新值value,依赖注入
                    field.set(entry.getValue(), instance);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //5、初始化HandlerMapping，根据url映射不同的controller方法
    private void doMapping() {
        if (IOCByType.isEmpty() && IOCByName.isEmpty()) {
            return;
        }
        for (Entry<String, Object> entry : IOCByType.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();
            //判断是否是controller
            if (!clazz.isAnnotationPresent(UVController.class)) {
                continue;
            }
            String startUrl = "/";
            //判断controller类上是否有UVRequestMapping注解，如果有则拼接url
            if (clazz.isAnnotationPresent(UVRequestMapping.class)) {
                UVRequestMapping requestMapping = clazz.getAnnotation(UVRequestMapping.class);
                String value = requestMapping.value();
                if (!StringUtils.isBlank(value)) {
                    startUrl += value;
                }
            }
            //遍历controller类中UVRequestMapping注解修饰的方法，添加到urlHandler中,完成url到方法的映射
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                if (!method.isAnnotationPresent(UVRequestMapping.class)) {
                    continue;
                }
                UVRequestMapping annotation = method.getAnnotation(UVRequestMapping.class);
                String url = startUrl + "/" + annotation.value().trim();
                //解决多个/重叠的问题
                url = url.replaceAll("/+", "/");

                MethodHandler methodHandler = new MethodHandler();
                //放入方法
                methodHandler.setMethod(method);
                try {
                    //放入方法所在的controller
                    methodHandler.setObject(entry.getValue());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //放入方法的参数列表
                List<String> params = doParamHandler(method);
                methodHandler.setParams(params);
                urlHandler.put(url, methodHandler);
            }
        }
    }

    //6、处理请求，执行相应的方法
    private void doHandler(HttpServletRequest request, HttpServletResponse response) throws IOException {
        boolean jsonResult = false;
        String uri = request.getRequestURI();
        PrintWriter writer = response.getWriter();
        //没有映射的url，返回404
        if (!urlHandler.containsKey(uri)) {
            writer.write("404 Not Found");
            return;
        }
        //获取url对应的method包装类
        MethodHandler methodHandler = urlHandler.get(uri);
        //处理url的method
        Method method = methodHandler.getMethod();
        //method所在的controller
        Object object = methodHandler.getObject();
        //method的参数列表
        List<String> params = methodHandler.getParams();

        //如果controller或这个方法有UVResponseBody修饰，返回json
        if (object.getClass().isAnnotationPresent(UVResponseBody.class) || method.isAnnotationPresent(UVResponseBody.class)) {
            jsonResult = true;
        }
        List<Object> args = new ArrayList<>();
        for (String param : params) {
            //从request中获取参数，然后放入参数列表
            String parameter = request.getParameter(param);
            args.add(parameter);
        }

        try {
            //执行方法，处理，返回结果
            Object result = method.invoke(object, args.toArray());
            //返回json(使用阿里的fastJson)
            if (jsonResult) {
                writer.write(JSON.toJSONString(object));
            } else { //返回视图
                doResolveView((String) result, request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            //方法执行异常，返回500
            writer.write("500 Internal Server Error");
            return;
        }

    }

    //8、视图解析，返回视图
    private void doResolveView(String indexView, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        //视图前缀
        String prefix = properties.getProperty("view.prefix");
        //视图后缀
        String suffix = properties.getProperty("view.suffix");
        String view = (prefix + indexView + suffix).trim().replaceAll("/+", "/");
        request.getRequestDispatcher(view).forward(request, response);
    }

    //处理字符串首字母小写
    private String lowerFirstCase(String str) {
        char[] chars = str.toCharArray();
        //ascii码计算
        chars[0] += 32;
        return String.valueOf(chars);
    }


    /**
     * 在Java 8之前的版本，代码编译为class文件后，方法参数的类型是固定的，但参数名称却丢失了， 这和动态语言严重依赖参数名称形成了鲜明对比。 现在，Java 8开始在class文件中保留参数名，给反射带来了极大的便利。 使用reflections包，jdk7和jdk8都可用
     **/
    //处理method的参数
    private List<String> doParamHandler(Method method) {
        //使用reflections进行参数名的获取
        Reflections reflections = new Reflections(new MethodParameterNamesScanner());
        //参数名与顺序对应
        List<String> paramNames = reflections.getMethodParamNames(method);
        return paramNames;
    }

}
