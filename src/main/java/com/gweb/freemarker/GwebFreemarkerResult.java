package com.gweb.freemarker;

import com.gweb.pipe.GPipe;
import com.gweb.annotations.Asyn;
import com.gweb.concurrent.Name2HTML;
import com.gweb.concurrent.PlayAsynFTLTask;
import com.gweb.concurrent.PlaySyncFTLTask;
import com.gweb.groovy.GPipeFactory;
import com.gweb.interceptor.DefaultPipeInvocation;
import com.gweb.interceptor.GPipeInterceptor;
import com.gweb.interceptor.GPipeInvocation;
import com.gweb.pipe.Constants;
import com.gweb.util.GPipeUtils;
import com.gweb.util.GWebContext;
import com.gweb.util.HTMLUtils;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.inject.Container;
import com.opensymphony.xwork2.inject.Inject;
import com.opensymphony.xwork2.util.ValueStackFactory;
import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;
import freemarker.core.Environment;
import freemarker.template.*;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.views.freemarker.FreemarkerResult;
import org.apache.struts2.views.freemarker.ScopesHashModel;
import org.apache.struts2.views.util.ResourceUtil;

import javax.servlet.http.HttpServletRequest;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author float.lu
 */
public class GwebFreemarkerResult extends FreemarkerResult {

    private static final Logger LOG = LoggerFactory.getLogger(GwebFreemarkerResult.class);
    private static final String PARENT_TEMPLATE_WRITER = GwebFreemarkerResult.class.getName() +  ".parentWriter";
    protected GPipeFactory GPipeFactory;
    protected GWebContext gWebContext;
    private Container container;
    protected ValueStackFactory valueStackFactory;
    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    private static CompletionService<String> asynCompletionService     = new ExecutorCompletionService<String>(
            executorService);
    private static CompletionService<Name2HTML> synCompletionService     = new ExecutorCompletionService<Name2HTML>(
            executorService);

    @Inject
    public void setValueStackFactory(ValueStackFactory valueStackFactory) {
        this.valueStackFactory = valueStackFactory;
    }

    @Inject
    public void setGPipeFactory(@Inject Container container) {
        this.GPipeFactory = container.getInstance(GPipeFactory.class,Constants.GROOVY_BEAN_FACTORY);
        this.gWebContext = container.getInstance(GWebContext.class);
        this.container = container;
    }

    @Override
    public void doExecute(String finalLocation, ActionInvocation invocation)throws IOException, TemplateException{
        super.location = finalLocation;
        super.configuration = getConfiguration();
        super.wrapper = getObjectWrapper();
        super.invocation = invocation;

        ActionContext ctx = invocation.getInvocationContext();
        HttpServletRequest req = (HttpServletRequest) ctx.get(ServletActionContext.HTTP_REQUEST);

        if (!finalLocation.startsWith("/")) {
            String base = ResourceUtil.getResourceBase(req);
            finalLocation = base + "/" + finalLocation;
        }
        Template template = configuration.getTemplate(finalLocation, deduceLocale());
        TemplateModel model = createModel();
        List<String> gPipesFromFTL = GPipeUtils.getGPipesFromFTL(template.toString());
        Map<String,Map<String, GPipeInvocation>> distriGPipes = getGPipes(gPipesFromFTL, invocation);
        if(distriGPipes.get(Constants.SYNC) != null){
            processSyncModel(configuration, model, distriGPipes.get(Constants.SYNC));
        }
        if(distriGPipes.get(Constants.ASYN) != null){
            preProcessAsynModel(model, distriGPipes.get(Constants.ASYN));
        }
        // Give subclasses a chance to hook into preprocessing
        if (preTemplateProcess(template, model)) {
            try {
                // Process the template
                Writer writer = getWriter();
                if (isWriteIfCompleted() || configuration.getTemplateExceptionHandler() == TemplateExceptionHandler.RETHROW_HANDLER) {
                    CharArrayWriter parentCharArrayWriter = (CharArrayWriter) req.getAttribute(PARENT_TEMPLATE_WRITER);
                    boolean isTopTemplate = false;
                    if (isTopTemplate = (parentCharArrayWriter == null)) {
                        //this is the top template
                        parentCharArrayWriter = new CharArrayWriter();
                        //set it in the request because when the "action" tag is used a new VS and ActionContext is created
                        req.setAttribute(PARENT_TEMPLATE_WRITER, parentCharArrayWriter);
                    }

                    try {
                        template.process(model, parentCharArrayWriter);

                        if (isTopTemplate) {
                            parentCharArrayWriter.flush();
                            parentCharArrayWriter.writeTo(writer);
                        }
                    } catch (TemplateException e) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error("Error processing Freemarker result!", e);
                        }
                        throw e;
                    } catch (IOException e) {
                        if (LOG.isErrorEnabled()){
                            LOG.error("Error processing Freemarker result!", e);
                        }
                        throw e;
                    } finally {
                        if (isTopTemplate && parentCharArrayWriter != null) {
                            req.removeAttribute(PARENT_TEMPLATE_WRITER);
                            parentCharArrayWriter.close();
                        }
                    }
                } else {
                    template.process(model, writer);
                }
                postProcessAsynModel(distriGPipes.get(Constants.ASYN));
            } finally {
                // Give subclasses a chance to hook into postprocessing
                postTemplateProcess(template, model);
            }
        }
    }

    private GPipeInvocation buildGPipeInvocation(GPipe gPipe, ActionInvocation invocation){
        List<GPipeInterceptor> interceptors = new ArrayList<GPipeInterceptor>(gWebContext.getInterceptors());
        DefaultPipeInvocation ginvocation = new DefaultPipeInvocation();
        ginvocation.setgPipe(gPipe);
        ginvocation.setInterceptors(interceptors.iterator());
        ginvocation.setActionInvocation(invocation);
        return ginvocation;
    }


    /**
     * @param gPipesFromFTL
     * @return
     */
    private Map<String,Map<String, GPipeInvocation>> getGPipes(List<String> gPipesFromFTL, ActionInvocation invocation)throws TemplateException{
        Map<String, Map<String, GPipeInvocation>> allMapGPipe = new HashMap<String, Map<String, GPipeInvocation>>();
        Map<String, GPipeInvocation> syncMapGpipe = new HashMap<String, GPipeInvocation>();
        Map<String, GPipeInvocation> asynMapGpipe = new HashMap<String, GPipeInvocation>();
        for(String p : gPipesFromFTL){
            Object o = GPipeFactory.getGPipe(p);
            if(o != null && o instanceof GPipe){
                Asyn asyn = ((GPipe)o).getClass().getAnnotation(Asyn.class);
                if(asyn != null){
                    asynMapGpipe.put(p, buildGPipeInvocation((GPipe)o, invocation));
                }else{
                    syncMapGpipe.put(p, buildGPipeInvocation((GPipe)o, invocation));
                }
            }else{
                throw new TemplateException("No GPipe mapping to " + Constants.GPIPE_PREFIX + p.toString(),
                        Environment.getCurrentEnvironment());
            }
        }
        allMapGPipe.put(Constants.ASYN, asynMapGpipe);
        allMapGPipe.put(Constants.SYNC, syncMapGpipe);
        return allMapGPipe;
    }

    private void processSyncModel(Configuration configuration,
                                  TemplateModel model,
                                  Map<String,GPipeInvocation> gPipes)throws IOException,TemplateException{
        try{
            for(Map.Entry e : gPipes.entrySet()){
                    synCompletionService.submit(new PlaySyncFTLTask(configuration,(GPipeInvocation)e.getValue(),e,deduceLocale(), valueStackFactory.createValueStack()));
            }
            for(Map.Entry e : gPipes.entrySet()){
                Future<Name2HTML> future = synCompletionService.take();
                Name2HTML name2HTML = future.get();
                ((ScopesHashModel)model).put(name2HTML.getName(), name2HTML.getHtml());
            }
        }catch (Exception ec){
            throw new TemplateException(ec,Environment.getCurrentEnvironment());
        }
    }

    private void preProcessAsynModel(TemplateModel model,
                                     Map<String,GPipeInvocation> gPipes){
        for(Map.Entry e : gPipes.entrySet()){
            try{
                ((ScopesHashModel) model).put(Constants.GPIPE_PREFIX + e.getKey(),
                        HTMLUtils.getPerGPipeHTML(e.getKey().toString()));
            }catch (Exception te){

            }
        }
    }

    private void postProcessAsynModel(Map<String, GPipeInvocation> gPipeMap)throws IOException,TemplateException{
        try {
            if(gPipeMap == null){
                return;
            }
            Writer writer = getWriter();
            for(Map.Entry e : gPipeMap.entrySet()){
                if(e.getValue() != null && e.getValue() instanceof GPipeInvocation)
                    asynCompletionService.submit(new PlayAsynFTLTask(e.getKey().toString(),
                        (GPipeInvocation)e.getValue(),configuration, deduceLocale(), valueStackFactory.createValueStack()));
            }
            for (Map.Entry e : gPipeMap.entrySet()){
                Future<String> future = asynCompletionService.take();
                String ftlString = future.get();
                writer.write(ftlString);
                writer.flush();
            }
        }catch (IOException ie){
            throw ie;
        }catch (InterruptedException iie){
            throw new TemplateException("InterruptedException",Environment.getCurrentEnvironment());
        }catch (ExecutionException eee){
            throw new TemplateException("ExecutionException",Environment.getCurrentEnvironment());
        }
    }
}
