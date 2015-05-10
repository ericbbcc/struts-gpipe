package com.gweb.util;

import java.io.File;
import java.io.FilenameFilter;

/**
 * @author float.lu
 */
public class FileUtils {

    /**
     * get all files
     * @return
     */
    public static File[] getRecursionFiles(File file)throws Exception{
        if(!file.isDirectory()){
            return new File[]{file};
        }else if(file.isDirectory()){
             return file.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".groovy");
                }
            });

        }
        return null;
    }
}
