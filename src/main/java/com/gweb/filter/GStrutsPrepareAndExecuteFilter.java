package com.gweb.filter;

import com.gweb.util.GWebContext;
import com.opensymphony.xwork2.inject.Container;
import org.apache.struts2.dispatcher.Dispatcher;
import org.apache.struts2.dispatcher.ng.filter.StrutsPrepareAndExecuteFilter;

import javax.servlet.FilterConfig;

/**
 * @author float.lu
 */
public class GStrutsPrepareAndExecuteFilter extends StrutsPrepareAndExecuteFilter {
    @Override
    protected void postInit(Dispatcher dispatcher, FilterConfig filterConfig) {
        initGWebContext(dispatcher);
        //TODO init gweb
    }

    private void initGWebContext(Dispatcher dispatcher){
        Container container = dispatcher.getContainer();
        GWebContext context = container.getInstance(GWebContext.class);
        context.init();
    }
}
