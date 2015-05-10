package com.gweb.groovy;

import com.gweb.pipe.GPipe;
import com.gweb.annotations.GPipeMapping;
import com.gweb.util.FileUtils;
import com.gweb.util.GWebContext;
import com.opensymphony.xwork2.inject.Container;
import com.opensymphony.xwork2.inject.Inject;
import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;
import freemarker.core.Environment;
import freemarker.template.TemplateException;
import groovy.lang.GroovyClassLoader;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author float.lu
 */
public class DefaultGPipeFactory implements GPipeFactory {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultGPipeFactory.class);
    //cache GPipeMapping to GPipe.class
    private Map<String,Class<GPipe>> gPipes = new ConcurrentHashMap<String, Class<GPipe>>();
    private GWebContext context;

    @Inject
    public DefaultGPipeFactory(@Inject Container container) {
        this.context = container.getInstance(GWebContext.class);
    }

    public void init() {
        ClassLoader parent = Thread.currentThread().getContextClassLoader();
        try {
            URL url = parent.getResource(context.getGroovyBaseDir());
            File[] files = FileUtils.getRecursionFiles(new File(url.toURI()));
            buildGPipes(files);
        }catch (Exception e){

        }
    }

    private void buildGPipes(File[] files)throws Exception{
        GroovyClassLoader gLoader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader());
        for(File file : files){
            try {
                Class<GPipe> pipeClazz = gLoader.parseClass(file);
                GPipeMapping gPipeMapping = pipeClazz.getAnnotation(GPipeMapping.class);
                if(gPipeMapping != null){
                    gPipes.put(gPipeMapping.value(),pipeClazz);
                }else{

                }
            }catch (Exception e){
                throw e;
            }
        }
    }

    public Object getGPipe(String beanName)throws TemplateException{
        Class<GPipe> gPipeClass = gPipes.get(beanName);
        if(gPipeClass == null){
            throw new TemplateException("gPipe name:" + beanName + "not found.", Environment.getCurrentEnvironment());
        }
        GPipe gPipe = null;
        try{
            gPipe = gPipeClass.newInstance();
        }catch (IllegalAccessException ie){
            LOG.info("beanName:" + beanName + "instance failed!" + ie.getMessage());
        }catch (InstantiationException ise){
            LOG.info("beanName:" + beanName + "instance failed!" + ise.getMessage());
        }
        return gPipe;
    }
}
