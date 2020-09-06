package sourcelabs.magnificent;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HealthChecker implements Runnable {
    
    private static Logger logger = LoggerFactory.getLogger(HealthChecker.class);
    
    protected PooledHttpClient phc;
    
    // in milliseconds
    protected long checkInterval = 2000L;
    
    protected String serviceURL = "https://api.us-west-1.saucelabs.com/v1/magnificent/";
    
    protected ExecutorService worker;
    
    protected ServiceStatusRecorder recorder; 

    public void init() {
        worker = Executors.newSingleThreadScheduledExecutor();
        ((ScheduledExecutorService) worker).scheduleWithFixedDelay(this, 1000L, checkInterval,
                TimeUnit.MILLISECONDS);
    }
    
    public void run() {
        logger.info("send request to {}", serviceURL);
        CloseableHttpResponse resp = null;
        try {
            recorder.incrRequests();
            HttpGet get = new HttpGet(serviceURL);
            resp = phc.execute(get);
            int retCode = resp.getStatusLine().getStatusCode();
            if (200 != retCode) {
                logger.error("magnificent returns error, code: {}, msg: {}", retCode, EntityUtils.toString(resp.getEntity(), "utf-8"));
                recorder.incrFailureRequest();
                recorder.incrFailureRequestByCode(retCode);
                return;
            }
            
            recorder.incrSuccessRequest();
            logger.info("send request to {} return successfully", serviceURL);
        } catch (Exception e) {
            logger.error("magnificent returns exception: {}", e.toString());
            recorder.incrFailureRequest();
        } finally {
            if (resp != null) {
                try {
                    resp.close();
                } catch (IOException e) {
                }
            }
        }
        
    }
    
    public void setPhc(PooledHttpClient phc) {
        this.phc = phc;
    }


    public void setCheckInterval(long checkInterval) {
        this.checkInterval = checkInterval;
    }

    public void setServiceURL(String serviceURL) {
        this.serviceURL = serviceURL;
    }

    public ServiceStatusRecorder getRecorder() {
        return recorder;
    }

    public void setRecorder(ServiceStatusRecorder recorder) {
        this.recorder = recorder;
    }
    
}
