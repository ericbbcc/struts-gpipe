package com.gweb.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author float.lu
 */
public class GPipeUtils {
    public static List<String> getGPipesFromFTL(String ftl){
        Pattern pattern = Pattern.compile("\\$\\{(GPipe_.*)\\}");
        Matcher matcher = pattern.matcher(ftl);
        List<String> models = new ArrayList<String>();
        while(matcher.find()){
            String modelName = matcher.group();
            models.add(modelName.substring("${GPipe_".length(), modelName.length() - 1));
        }
        return models;
    }
}
