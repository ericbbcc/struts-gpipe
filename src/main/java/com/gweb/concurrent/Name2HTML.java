package com.gweb.concurrent;

/**
 * @author float.lu
 */
public class Name2HTML {
    private String name;
    private String html;

    public Name2HTML(String name, String html) {
        this.name = name;
        this.html = html;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }
}
