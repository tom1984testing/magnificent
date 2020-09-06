package sourcelabs.magnificent;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.util.Arrays;

import javax.net.ssl.SSLException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PooledHttpClient {

    private static final Logger logger = LoggerFactory.getLogger(PooledHttpClient.class);

    private PoolingHttpClientConnectionManager cm;

    private CloseableHttpClient hc;

    public static class InternalRetryHandler extends DefaultHttpRequestRetryHandler {

        public InternalRetryHandler() {
            super(3, true, Arrays.asList(InterruptedIOException.class, UnknownHostException.class, SSLException.class));
        }
    }

    public PooledHttpClient(int maxConnection) {
        cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(maxConnection);
        cm.setDefaultMaxPerRoute(maxConnection);
        cm.setDefaultSocketConfig(SocketConfig.custom().setSoReuseAddress(true).build());
        hc = HttpClients.custom().setConnectionManager(cm).setRetryHandler(new InternalRetryHandler()).build();
    }

    public void shutdown() {
        if (hc != null) {
            try {
                hc.close();
            } catch (IOException e) {
                logger.error("fail to close shared httpclient due to: ", e);
            }
        }
        if (cm != null) {
            cm.shutdown();
        }
    }

    public CloseableHttpResponse execute(HttpUriRequest req) throws IOException, ClientProtocolException {
        return hc.execute(req);
    }
}
