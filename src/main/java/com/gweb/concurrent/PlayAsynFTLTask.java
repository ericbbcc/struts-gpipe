package com.gweb.concurrent;

import com.gweb.pipe.GPipe;
import com.gweb.interceptor.GPipeInvocation;
import com.gweb.util.HTMLUtils;
import com.opensymphony.xwork2.inject.Container;
import com.opensymphony.xwork2.util.ValueStack;
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
import java.util.concurrent.Callable;

/**
 * @author float.lu
 */
public class PlayAsynFTLTask implements Callable<String> {

    private String name;
    private GPipeInvocation invocation;
    private Configuration configuration;
    private Locale locale;
    private Container container;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private ServletContext servletContext;
    private ValueStack valueStack;
    private FreemarkerManager freemarkerManager;

    public PlayAsynFTLTask(String name, GPipeInvocation invocation, Configuration configuration, Locale locale, ValueStack valueStack) {
        this.name = name;
        this.invocation = invocation;
        this.configuration = configuration;
        this.locale = locale;
        this.valueStack = valueStack;
        this.container = (Container)invocation.getActionInvocation().getInvocationContext().get("com.opensymphony.xwork2.ActionContext.container");
        this.request = (HttpServletRequest)invocation.getActionInvocation().getInvocationContext().get(ServletActionContext.HTTP_REQUEST);
        this.response = (HttpServletResponse)invocation.getActionInvocation().getInvocationContext().get(ServletActionContext.HTTP_RESPONSE);
        this.servletContext = (ServletContext)invocation.getActionInvocation().getInvocationContext().get(ServletActionContext.SERVLET_CONTEXT);
        this.freemarkerManager = container.getInstance(FreemarkerManager.class);
    }
    public String call() throws Exception {
        invocation.invoke();
        GPipe gPipe = invocation.getGPipe();
        String ftlName = gPipe.execute();
        valueStack.push(invocation.getGPipe());
        ScopesHashModel model = freemarkerManager.buildTemplateModel(valueStack,invocation.getGPipe(),servletContext,request,response,configuration.getObjectWrapper());
        Template template = configuration.getTemplate(ftlName, locale);
        StringWriter strWrite = new StringWriter();
        template.process(model, strWrite);
        return HTMLUtils.getPositionScript(name, strWrite.toString());
    }
}
