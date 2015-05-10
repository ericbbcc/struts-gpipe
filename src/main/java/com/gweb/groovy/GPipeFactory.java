package com.gweb.groovy;


import freemarker.template.TemplateException;

/**
 * @author float.lu
 */
public interface GPipeFactory {
    Object getGPipe(String beanName)throws TemplateException;
    void init();
}
