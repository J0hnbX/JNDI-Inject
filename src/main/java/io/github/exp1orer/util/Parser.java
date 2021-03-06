package io.github.exp1orer.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Parser {
    public Map<String,String> parse(String[] args) {
        Map<String, String> argsMap = new HashMap<String, String>();
        for (String arg : args) {
            String[] strings = arg.split("=", 2);
            argsMap.put(strings[0], strings[1]);
        }

        return argsMap;
    }

    public static Map<String, String> parseHeaders(String headers) {
        if (headers == null) {
            return null;
        }
        Map<String, String> map = new HashMap<String, String>();
        String[] header = headers.split(";");
        String key;
        String value;
        for (String head : header) {
            key = head.substring(0, head.indexOf(":")).trim();
            value = head.substring(head.indexOf(":") - 1).trim();
            map.put(key, value);
        }

        return map;
    }

    public static String getRandomStr(int length) {
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(3);
            long result = 0;
            switch (number) {
                case 0:
                    result = Math.round(Math.random() * 25 + 65);
                    sb.append(String.valueOf((char) result));
                    break;
                case 1:
                    result = Math.round(Math.random() * 25 + 97);
                    sb.append(String.valueOf((char) result));
                    break;
                case 2:
                    sb.append(String.valueOf(new Random().nextInt(10)));
                    break;
            }
        }

        return sb.toString();
    }
}
