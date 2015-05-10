package com.gweb.interceptor;

import com.gweb.pipe.GPipe;
import com.opensymphony.xwork2.ActionInvocation;

import java.util.Iterator;

/**
 * @author float.lu
 */
public class DefaultPipeInvocation implements GPipeInvocation {

    protected Iterator<GPipeInterceptor> interceptors;
    protected GPipe gPipe;
    private ActionInvocation actionInvocation;

    public String invoke() throws Exception {
        if(interceptors == null){
            gPipe.execute();
        }
        /**
         * if interceptors is not null, exxcute interceptor one by one
         */
        while (interceptors.hasNext()){
            GPipeInterceptor interceptor = interceptors.next();
            interceptor.intercept(this);
        }
        return gPipe.execute();
    }

    public Iterator<GPipeInterceptor> getInterceptors() {
        return interceptors;
    }

    public void setInterceptors(Iterator<GPipeInterceptor> interceptors) {
        this.interceptors = interceptors;
    }

    public GPipe getgPipe() {
        return gPipe;
    }

    public void setgPipe(GPipe gPipe) {
        this.gPipe = gPipe;
    }

    public GPipe getGPipe() {
        return this.gPipe;
    }

    public ActionInvocation getActionInvocation() {
        return actionInvocation;
    }

    public void setActionInvocation(ActionInvocation actionInvocation) {
        this.actionInvocation = actionInvocation;
    }
}
