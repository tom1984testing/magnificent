package sourcelabs.magnificent;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceStatusRecorder implements Runnable {
    
    private static Logger logger = LoggerFactory.getLogger(ServiceStatusRecorder.class);
    
    // in milliseconds
    protected long checkInterval = 15 * 1000L;
    
    private AtomicLong totalRequests = new AtomicLong();
    
    private AtomicLong lastTotalRequests = new AtomicLong();
    
    private AtomicLong totalSuccessRequests = new AtomicLong();
    
    private AtomicLong totalFailureRequests = new AtomicLong();
    
    private ConcurrentHashMap<Integer, Integer> failureRequestsByCode = new ConcurrentHashMap<Integer, Integer>();
    
    protected ExecutorService worker;
    
    private Date startTime = new Date();
    
    public void init() {
        worker = Executors.newSingleThreadScheduledExecutor();
        ((ScheduledExecutorService) worker).scheduleWithFixedDelay(this, 1000L, checkInterval,
                TimeUnit.MILLISECONDS);
    }
    
    public void run() {
        int timeUsed = (int) (System.currentTimeMillis() - startTime.getTime()) / 1000;
        logger.info("total requests, num: {}, tps: {}", totalRequests.get(), totalRequests.get()/timeUsed);
        logger.info("total success requests, num: {}", totalSuccessRequests.get());
        logger.info("total failure requests, num: {}", totalFailureRequests.get());
        if (totalRequests.get() > 0) {
            logger.info("success ratio, num: {}", totalSuccessRequests.get()*100.0 / totalRequests.get());
        }
        for (int code : failureRequestsByCode.keySet()) {
            logger.info("total failure requests by code, code: {}, num: {}", code, failureRequestsByCode.get(code));
        }
        
        // check whether service is responding
        if (lastTotalRequests.get() == totalRequests.get()) {
            logger.warn("no requests since last {} ms, maybe the serice is not responsing", checkInterval);
        }
        lastTotalRequests.set(totalRequests.get());
    }

    public void incrRequests() {
        totalRequests.incrementAndGet();
    }

    public void incrFailureRequest() {
        totalFailureRequests.incrementAndGet();
    }

    public void incrSuccessRequest() {
        totalSuccessRequests.getAndIncrement();
    }

    public void incrFailureRequestByCode(int code) {
        if (!failureRequestsByCode.containsKey(code)) {
            failureRequestsByCode.put(code, 1);
        } else {
            failureRequestsByCode.put(code, failureRequestsByCode.get(code)+1);
        }
    }

    public long getCheckInterval() {
        return checkInterval;
    }

    public void setCheckInterval(long checkInterval) {
        this.checkInterval = checkInterval;
    }
    

}
