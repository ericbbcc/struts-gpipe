package com.gweb.concurrent;

import com.gweb.interceptor.GPipeInvocation;
import com.gweb.pipe.Constants;
import com.opensymphony.xwork2.inject.Container;
import com.opensymphony.xwork2.inject.Inject;
import com.opensymphony.xwork2.util.ValueStack;
import com.opensymphony.xwork2.util.ValueStackFactory;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.views.freemarker.FreemarkerManager;
import org.apache.struts2.views.freemarker.ScopesHashModel;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author float.lu
 */
public class PlaySyncFTLTask implements Callable<Name2HTML>{

    private Configuration configuration;
    private GPipeInvocation invocation;
    private Map.Entry entry;
    private Locale locale;
    private Container container;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private ServletContext servletContext;
    private ValueStack valueStack;
    private FreemarkerManager freemarkerManager;

    public PlaySyncFTLTask(Configuration configuration, GPipeInvocation invocation, Map.Entry entry, Locale locale, ValueStack valueStack) {
        this.configuration = configuration;
        this.invocation = invocation;
        this.entry = entry;
        this.locale = locale;
        this.valueStack = valueStack;
        this.container = (Container)invocation.getActionInvocation().getInvocationContext().get("com.opensymphony.xwork2.ActionContext.container");
        this.request = (HttpServletRequest)invocation.getActionInvocation().getInvocationContext().get(ServletActionContext.HTTP_REQUEST);
        this.response = (HttpServletResponse)invocation.getActionInvocation().getInvocationContext().get(ServletActionContext.HTTP_RESPONSE);
        this.servletContext = (ServletContext)invocation.getActionInvocation().getInvocationContext().get(ServletActionContext.SERVLET_CONTEXT);
        this.freemarkerManager = container.getInstance(FreemarkerManager.class);
    }

    public Name2HTML call() throws Exception {
        ((GPipeInvocation)entry.getValue()).invoke();
        String ftlName = invocation.getGPipe().execute();
        valueStack.push(invocation.getGPipe());
        ScopesHashModel model = freemarkerManager.buildTemplateModel(valueStack,invocation.getGPipe(),servletContext,request,response,configuration.getObjectWrapper());
        Template template = configuration.getTemplate(ftlName, locale);
        StringWriter writer = new StringWriter();
        template.process(model,writer);
        return new Name2HTML(Constants.GPIPE_PREFIX + entry.getKey().toString(),writer.toString());
    }
}
