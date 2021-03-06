package io.github.exp1orer.util;

import io.github.exp1orer.server.LDAPServer;

import java.io.*;
import java.util.Properties;

public class Config {
    private Config() {}

    static {
        // 必须配置项
        String[] keys = new String[]{ "Api", "Token", "Domain", "EnableLDAPLog", "EnableHttpLog"};
        String pwd = System.getProperty("user.dir");
        Properties properties = new Properties();
        try {
            BufferedReader br = new BufferedReader(new FileReader(pwd + File.separator + "config.properties"));
            properties.load(br);

            for (String key : keys) {
                if (properties.getProperty(key) == null) {
                    System.out.println(String.format("%s不能为空，请检查配置文件."));
                    System.exit(1);
                }

                Dnslog.platform = properties.getProperty("Platform");
                Dnslog.api = properties.getProperty("Api");
                Dnslog.token = properties.getProperty("Token");
                Dnslog.rootDomain = properties.getProperty("Domain");
                Dnslog.sleep = properties.getProperty("Sleep") == null ? 5 : Long.parseLong(properties.getProperty("Sleep"));
                LDAPServer.enableLDAPLog = "True".equalsIgnoreCase(properties.getProperty("EnableLDAPLog"));
                HttpUtil.enableHttpLog = "True".equalsIgnoreCase(properties.getProperty("EnableHttpLog"));
            }
        } catch (FileNotFoundException notFoundException) {
            System.out.println("config.properties文件不能为空，必须配置DNSLOG！");
            System.exit(1);
        } catch (IOException io) {
            io.printStackTrace();
            System.exit(1);
        }
    }
}
