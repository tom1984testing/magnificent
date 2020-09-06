package sourcelabs.magnificent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.GenericXmlApplicationContext;

public class Bootstrap {
    
    private static Logger logger = LoggerFactory.getLogger(Bootstrap.class);

    public static void main(String[] args) {
        logger.info("magnificent check starts ...");
        GenericXmlApplicationContext ctx = new GenericXmlApplicationContext();
        ctx.load("classpath:spring/applicationContext.xml");
        ctx.refresh();
        ctx.registerShutdownHook();
    }
    
}
