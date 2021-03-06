package io.github.exp1orer.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;

import java.io.*;
import java.util.Map;

public class Dnslog {
    public static String platform;
    public static String api;
    public static String token;
    public static String rootDomain;
    public static long sleep;

    public static String getRandomDomain(int length) {
        String randomStr = Parser.getRandomStr(length);
        return randomStr + "." + rootDomain;

    }

    public static boolean getRecord(String domain) {
        String url = api.replace("{token}", token).replace("{filter}", domain);
        boolean resp = HttpUtil.connection(url, "GET");
        try {
            String responseBody = HttpUtil.getResponseBody();
            Map result = JSON.parseObject(responseBody, Map.class);
            if (result == null || ((JSONArray) result.get("data")).size() == 0) {
                return false;
            }
        } catch (IOException io) {
            io.printStackTrace();
            return false;
        } catch (JSONException jsonException) {
            jsonException.printStackTrace();
            return false;
        }

        return true;
    }

}
