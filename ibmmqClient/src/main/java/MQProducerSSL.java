import com.ibm.mq.*;
import com.ibm.mq.constants.MQConstants;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.Console;
import java.io.FileInputStream;
import java.security.KeyStore;

public class MQProducerSSL {
    private static final String qManager = "SSLQM";
    private static final String qName = "localq";
    private static final String kpassword = "123456";
    public static boolean sslEnabled = true;

    public static void main(String args[]) {
        try {
            MQEnvironment.hostname = "localhost";
            MQEnvironment.channel = "myChannel";
            MQEnvironment.port = 1414;
            MQEnvironment.userID = "Taneesha";
            MQEnvironment.password = "123456";
            if(sslEnabled) {
                char[] KSPW = kpassword.toCharArray();
                // instantiate a KeyStore with type JKS
                KeyStore ks = KeyStore.getInstance("JKS");
                // load the contents of the KeyStore
                ks.load(new FileInputStream("C:\\Program Files (x86)\\IBM\\WebSphere MQ\\Qmgrs\\SSLQM\\ssl\\keyStore.jks"), KSPW);
                // Create a keystore object for the truststore
                KeyStore trustStore = KeyStore.getInstance("JKS");
                // Open our file and read the truststore (no password)
                trustStore.load(new FileInputStream("C:\\Program Files (x86)\\IBM\\WebSphere MQ\\Qmgrs\\SSLQM\\ssl\\trustStore.jks"), null);

                // Create a default trust and key manager
                TrustManagerFactory trustManagerFactory =
                        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                KeyManagerFactory keyManagerFactory =
                        KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());

                // Initialise the managers
                trustManagerFactory.init(trustStore);
                keyManagerFactory.init(ks,KSPW);

                // Get an SSL context.
                // Note: not all providers support all CipherSuites. But the
                // "SSL_RSA_WITH_3DES_EDE_CBC_SHA" CipherSuite is supported on both SunJSSE
                // and IBMJSSE2 providers

                // Accessing available algorithm/protocol in the SunJSSE provider
                // see http://java.sun.com/javase/6/docs/technotes/guides/security/SunProviders.html
                SSLContext sslContext = SSLContext.getInstance("SSLv3");

                // Acessing available algorithm/protocol in the IBMJSSE2 provider
                // see http://www.ibm.com/developerworks/java/jdk/security/142/secguides/jsse2docs/JSSE2RefGuide.html
                // SSLContext sslContext = SSLContext.getInstance("SSL_TLS");
                System.out.println("SSLContext provider: " +
                        sslContext.getProvider().toString());

                // Initialise our SSL context from the key/trust managers
                sslContext.init(keyManagerFactory.getKeyManagers(),
                        trustManagerFactory.getTrustManagers(), null);

                // Get an SSLSocketFactory to pass to WMQ
                MQEnvironment.sslSocketFactory = sslContext.getSocketFactory();

                MQEnvironment.sslFipsRequired = false;
                MQEnvironment.sslCipherSuite = "SSL_RSA_EXPORT_WITH_RC2_CBC_40_MD5";
            }

            MQQueueManager qMgr = new MQQueueManager(qManager);
            int openOptions = MQConstants.MQOO_OUTPUT;
            MQQueue queue = qMgr.accessQueue(qName, openOptions);

            MQMessage msg = new MQMessage();
            String message = "Hello world";
            msg.writeUTF(message);
            MQPutMessageOptions pmo = new MQPutMessageOptions();
            queue.put(msg, pmo);

            queue.close();
            qMgr.disconnect();
        } catch (java.io.IOException ex) {
            System.out.println("An IOException occurred whilst writing to the message buffer: " + ex);
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
