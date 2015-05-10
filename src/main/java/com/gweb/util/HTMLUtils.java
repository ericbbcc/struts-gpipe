package com.gweb.util;

/**
 * @author float.lu
 */
public class HTMLUtils {

    public static String getPerGPipeHTML(String id){
        return "<pipe id=\"" + id + "\"> </pipe>";
    }

    public static String getPositionScript(String id, String content){
        return "<script>var div = document.getElementById(\"" + id + "\");" +
                "div.innerHTML = \"" + content + "\";" +
                "</script>";
    }
}
