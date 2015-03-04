package com.fantasy.swp.service;

import com.fantasy.framework.dao.hibernate.PropertyFilter;
import com.fantasy.swp.IPage;
import com.fantasy.swp.IPageItem;
import com.fantasy.swp.SwpContext;
import com.fantasy.swp.bean.*;
import com.fantasy.swp.runtime.GenerateImpl;
import com.fantasy.system.bean.Website;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;


@Component
public class PageBeanService {
    @Resource
    private _PageService pageService;
    @Resource
    private TemplateService templateService;
    @Resource
    private GenerateImpl generate;
    @Resource
    private PageItemService pageItemService;
    @Resource
    private DataService dataService;

    public List<IPage> listPage(){
        List<Page> pages = pageService.find(new ArrayList<PropertyFilter>());
        List<IPage> ipages = new ArrayList<IPage>();
        for(Page page : pages){
            PageBean pageBean = new PageBean();
            pageBean.setPage(page);
            ipages.add(pageBean);
        }
        return ipages;
    }

    public IPage savePage(Website website,String url, String templatePath, String name){
        return this.savePage(website, url, templatePath, name,null,0);
    }

    public IPage savePage(Website website,String url, String templatePath, String name,List<Data> datas){
        return this.savePage(website, url, templatePath, name,datas,0);
    }

    public IPage savePage(Website website,String url, String templatePath, String name,List<Data> datas,int pageSize){
        if(datas!=null && datas.size()>0){
            for(Data data : datas){
                this.dataService.save(data);
            }
        }
        PageBean pageBean = new PageBean();
        Page page = new Page();
        page.setWebSite(website);
        page.setPath(url);
        page.setName(name);
        page.setTemplate(this.templateService.findUniqueByPath(templatePath,website.getId()));
        page.setDatas(datas);
        page.setPageSize(pageSize);
        // 保存数据库
        this.pageService.save(page);

        pageBean.setPage(page);

        return pageBean;
    }

    public IPage get(String url, Long websiteId) {
        PageBean pageBean = new PageBean();
        Page page = this.pageService.findUniqueByPath(url,websiteId);
        pageBean.setPage(page);
        return pageBean;
    }

    public void remove(String url, Long websiteId) {
        this.pageService.deleteByPath(url,websiteId);
    }

    public List<IPageItem> execute(Page page) {
        Page newpage = this.generate.create(page);
        List<IPageItem> pageItems = new ArrayList<IPageItem>();
        for(PageItem pageItem : newpage.getPageItems()){
            PageItemBean pageItemBean = new PageItemBean();
            pageItemBean.setPageItem(pageItem);
            pageItems.add(pageItemBean);
        }
        return pageItems;
    }

    public IPageItem getPageItem(String path, Long pageId) {
        PageItem pageItem = pageItemService.findUniqueByPath(path,pageId);
        PageItemBean iPageItem = new PageItemBean();
        iPageItem.setPageItem(pageItem);
        return iPageItem;
    }
}
