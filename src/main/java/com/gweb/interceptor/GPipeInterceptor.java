package com.gweb.interceptor;

import java.io.Serializable;

/**
 * @author float.lu
 */
public interface GPipeInterceptor extends Serializable{
    String intercept(GPipeInvocation invocation) throws Exception;
}
