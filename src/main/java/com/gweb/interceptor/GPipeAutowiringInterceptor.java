package com.gweb.interceptor;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.spring.SpringObjectFactory;
import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.WebApplicationContext;

/**
 * @author float.lu
 */
public class GPipeAutowiringInterceptor implements GPipeInterceptor, ApplicationContextAware{
    private static final Logger LOG = LoggerFactory.getLogger(GPipeAutowiringInterceptor.class);

    private ApplicationContext context;
    private boolean initialized = false;
    private SpringObjectFactory factory;
    private Integer autowireStrategy;
    public static final String APPLICATION_CONTEXT = "com.opensymphony.xwork2.spring.interceptor.GPipeAutowiringInterceptor.applicationContext";


    public String intercept(GPipeInvocation invocation) throws Exception {
        if (!initialized) {
            ActionInvocation actionInvocation = invocation.getActionInvocation();
            ApplicationContext applicationContext = (ApplicationContext)actionInvocation.getInvocationContext().getApplication().get(
                    WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);

            if (applicationContext == null) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("ApplicationContext could not be found.  Action classes will not be autowired.");
                }
            } else {
                setApplicationContext(applicationContext);
                factory = new SpringObjectFactory();
                factory.setApplicationContext(getApplicationContext());
                if (autowireStrategy != null) {
                    factory.setAutowireStrategy(autowireStrategy.intValue());
                }
            }
            initialized = true;
        }

        if (factory != null) {
            Object bean = invocation.getGPipe();
            factory.autoWireBean(bean);
        }
        return invocation.invoke();
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    protected ApplicationContext getApplicationContext() {
        return context;
    }

    public Integer getAutowireStrategy() {
        return autowireStrategy;
    }

    public void setAutowireStrategy(Integer autowireStrategy) {
        this.autowireStrategy = autowireStrategy;
    }
}
