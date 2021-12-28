package io.github.exp1orer.util;

import io.github.exp1orer.StartUp;

import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpUtil {
    public static InputStream respInputStream;
    public static boolean enableHttpLog;

    static {
        try {
            SSLContext sslcontext = SSLContext.getInstance("SSL");
            sslcontext.init(null, new TrustManager[] { new miTM() }, null);
            HostnameVerifier ignoreHostnameVerifier = new HostnameVerifier() {
                @Override
                public boolean verify(String s, SSLSession sslsession) {
                    return true;
                }
            };
            HttpsURLConnection.setDefaultHostnameVerifier(ignoreHostnameVerifier);
            HttpsURLConnection.setDefaultSSLSocketFactory(sslcontext.getSocketFactory());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }

    private static class miTM implements TrustManager, X509TrustManager {
        public boolean isServerTrusted(X509Certificate[] certs) {
            return true;
        }

        public boolean isClientTrusted(X509Certificate[] certs) {
            return true;
        }

        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {}

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {}

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }

    public static boolean connection(String url, String method) {
        return connection(url, method, null, null, null);
    }

    public static boolean connection(String url, String method, Map<String, String> headers) {
        return connection(url, method, headers, null, null);
    }

    public static boolean connection(String url, String method, Map<String, String> headers, String body, Proxy proxy) {
        HttpURLConnection conn;

        try {
            conn = (HttpURLConnection) (proxy != null ? new URL(url).openConnection(proxy) : new URL(url).openConnection());
            conn.setInstanceFollowRedirects(false);
            // 连接超时时间为15秒
            conn.setConnectTimeout(15000);
            // 读取超时为30秒
            conn.setReadTimeout(30000);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:94.0) Gecko/20100101 Firefox/94.0");
            if (headers != null) {
                for (String key : headers.keySet()) {
                    conn.setRequestProperty(key, headers.get(key));
                }
            }
            if ("GET".equalsIgnoreCase(method)) {
                conn.setRequestMethod(method);
                conn.connect();
            } else if ("POST".equalsIgnoreCase(method)) {
                conn.setDoOutput(true);
                if (body != null) {
                    OutputStream os = conn.getOutputStream();
                    os.write(body.getBytes());
                }
            }

            Map<String, List<String>> fields = conn.getHeaderFields();
            if (!fields.isEmpty()) {
                // 不打印Dnslog平台请求
                if (!url.contains("ceye.io")) {
                    if (enableHttpLog) {
                        System.out.println("[*] Send Http Request: " + url);
                    }
                }
//                System.out.println(conn.getResponseCode());
                respInputStream = conn.getInputStream();
            }

        } catch (IOException ioException) {
//            ioException.printStackTrace();
            return false;
        }

        return true;
    }

    public static String getResponseBody() throws IOException {
        String line;
        StringBuffer sb = new StringBuffer();
        if (respInputStream != null) {
            BufferedReader br = new BufferedReader(new InputStreamReader(respInputStream));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        }

        return sb.toString();
    }

    public static boolean formatHttp(String file) throws IOException {
        String line, host, key, value, uri = "", url = "", method = "GET";
        StringBuffer body = new StringBuffer();
        String protocol = "http";
        boolean httpRequestFlag = false, httpBody = false;
        Map<String, String> headers = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(file));

        while ((line = br.readLine()) != null) {
            if (!httpRequestFlag) {
                method = line.split(" ")[0];
                if (!"GET".equalsIgnoreCase(method) && !"POST".equalsIgnoreCase(method)) {
                    System.out.println("请检查文件内容是否为HTTP请求!");
                    System.exit(1);
                }
                httpRequestFlag = true;
                uri = line.split(" ")[1].trim();
                continue;
            }

            if (line.startsWith("Host")) {
                host = line.split(":")[1].trim();
                if (host.startsWith("https://")) {
                    protocol = "https";
                }
                url = String.format("%s://%s%s", protocol, host, uri);
                continue;
            }

            // 处理请求头
            if (line.split(":").length >= 2) {
                key = line.substring(0, line.indexOf(":") - 1).trim();
                value = line.substring(line.indexOf(":") + 1).trim();
                headers.put(key, value);
                continue;
            }

            if ("".equals(line)) {
                httpBody = true;
            }

            if (httpBody && !"".equals(line)) {
                body.append(line);
            }
        }

        StartUp.method = method;
        StartUp.url = url;
        StartUp.headers = headers;
        StartUp.body = body.toString();

        return true;
    }

    public static String normalzedUrl(String url) throws Exception {
        Map<String, String> parameter = new HashMap<String, String>();
        URI uri = new URI(url);
        String scheme = uri.getScheme();
        String host = uri.getHost();
        int port = uri.getPort();
        String path = uri.getPath();
        String[] querys = uri.getQuery().split("&");
        String key, value;
        StringBuffer sb = new StringBuffer();

        for (String query : querys) {
            String[] split = query.split("=");
            key = split[0];
            value = URLEncoder.encode(split[1]);
            parameter.put(key, value);
        }

        sb.append(scheme + "://" + host + (port == -1 ? "" : ":" +String.valueOf(port)) + path + "?");
        for (String tmp_key : parameter.keySet()) {
            sb.append(tmp_key + "=" + parameter.get(tmp_key) + "&");
        }
        sb.setLength(sb.length() - 1);

        return sb.toString();
    }
}
