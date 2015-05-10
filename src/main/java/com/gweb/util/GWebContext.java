package com.gweb.util;

import com.gweb.groovy.GPipeFactory;
import com.gweb.interceptor.GPipeInterceptor;
import com.gweb.pipe.Constants;
import com.opensymphony.xwork2.inject.Container;
import com.opensymphony.xwork2.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author float.lu
 */
public class GWebContext {
    private String groovyBaseDir;
    private List<GPipeInterceptor> interceptors;
    private Container container;
    private GPipeFactory gPipeFactory;

    @Inject
    public GWebContext(@Inject Container container) {
        this.container = container;
        this.groovyBaseDir = container.getInstance(String.class, Constants.GWEB_GROOVY_DIR);
    }

    public String getGroovyBaseDir() {
        return groovyBaseDir;
    }

    public void setGroovyBaseDir(String groovyBaseDir) {
        this.groovyBaseDir = groovyBaseDir;
    }

    public void init(){
        buildIntercepters();
        initGroovyFactory();
    }

    private void buildIntercepters(){
        //TODO 读取配置文件，获取自定义拦截器
        String interceptorsStr = container.getInstance(String.class, Constants.GWEB_INTERCEPTORS);
        if(interceptorsStr == null ||
                interceptorsStr.trim() == ""){
            return;
        }
        StringTokenizer tokenizer = new StringTokenizer(interceptorsStr, ",");
        interceptors = new ArrayList<GPipeInterceptor>();
        while (tokenizer.hasMoreTokens()){
            GPipeInterceptor interceptor = container.getInstance(GPipeInterceptor.class ,tokenizer.nextToken());
            if(interceptor != null){
                interceptors.add(interceptor);
            }
        }
    }

    private void initGroovyFactory(){
        GPipeFactory factory = container.getInstance(GPipeFactory.class,Constants.GROOVY_BEAN_FACTORY);
        factory.init();
    }

    public List<GPipeInterceptor> getInterceptors() {
        return interceptors;
    }
}
