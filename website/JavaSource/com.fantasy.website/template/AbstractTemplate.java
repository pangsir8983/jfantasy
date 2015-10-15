package com.fantasy.website.template;

import com.fantasy.website.Template;
import com.fantasy.website.TemplateData;
import com.fantasy.website.data.SimpleData;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractTemplate<T> implements Template {

    private Map<String, TemplateData> data = new HashMap<String, TemplateData>();

    public AbstractTemplate() {
    }

    public AbstractTemplate(T content) {
        this.content = content;
    }

    private T content;

    @Override
    public T getContent() {
        return this.content;
    }

    public void setContent(T content) {
        this.content = content;
    }

    public Map<String, TemplateData> getData() {
        return this.data;
    }

    public void add(String key, TemplateData data) {
        this.data.put(key, data);
    }

    public void add(String key, String value) {
        this.add(key, new SimpleData(value));
    }
}