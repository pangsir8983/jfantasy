package com.fantasy.swp.service;

import com.fantasy.file.FileManager;
import com.fantasy.swp.*;
import com.fantasy.swp.runtime.ExecutionEntity;

/**
 * 默认的 page servie
 */
public class DefaultPageService implements PageService {

    private FileManager fileManager;

    public void setFileManager(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    @Override
    public PageInstance createPageInstance(OutPutUrl outPutUrl, Template template, TemplateData... datas) {
        return new ExecutionEntity(fileManager, outPutUrl, template, datas);
    }

}
