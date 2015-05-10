package com.gweb.interceptor;

import com.gweb.pipe.GPipe;

import com.opensymphony.xwork2.ActionInvocation;

/**
 * @author float.lu
 */
public interface GPipeInvocation {
    String invoke() throws Exception;
    GPipe getGPipe();
    ActionInvocation getActionInvocation();
}
