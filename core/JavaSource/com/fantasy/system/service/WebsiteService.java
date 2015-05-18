package com.fantasy.system.service;

import com.fantasy.file.service.FileManagerService;
import com.fantasy.framework.dao.Pager;
import com.fantasy.framework.dao.hibernate.PropertyFilter;
import com.fantasy.framework.spring.SpringContextUtil;
import com.fantasy.system.bean.Setting;
import com.fantasy.system.bean.Website;
import com.fantasy.system.dao.SettingDao;
import com.fantasy.system.dao.WebsiteDao;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.criterion.Criterion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class WebsiteService {

    private static final Log logger = LogFactory.getLog(WebsiteService.class);

    @Autowired
    private WebsiteDao websiteDao;
    @Autowired
    private SettingDao settingDao;
    @Autowired
    private FileManagerService fileManagerService;

    @Transactional(readOnly = true, propagation = Propagation.NOT_SUPPORTED)
    public Website findUniqueByKey(String key) {
        Website website = this.websiteDao.findUniqueBy("key", key);
//        Class lazyClass = website.getDefaultFileManager().getClass();
//        Field field = ClassUtil.getDeclaredField(lazyClass,"handler");
//        try {
//            website.getDefaultFileManager().getName();
//            JavassistLazyInitializer lazyInitializer = (JavassistLazyInitializer) field.get(website.getDefaultFileManager());
//            System.out.println(lazyInitializer.getIdentifier());
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        }
        for (Setting setting : website.getSettings()) {
            Hibernate.initialize(setting);
        }
        Hibernate.initialize(website.getRootMenu());
        Hibernate.initialize(website.getDefaultFileManager());
        return website;
    }

    public Website get(Long id) {
        Website website = websiteDao.get(id);
        Hibernate.initialize(website);
        return website;
    }

    public Website get(String key) {
        Website website = websiteDao.findUniqueBy("key", key);
        Hibernate.initialize(website);
        return website;
    }

    public void delete(Long... ids) {
        for (Long id : ids) {
            this.websiteDao.delete(id);
        }
    }

    public Pager<Website> findPager(Pager<Website> pager, List<PropertyFilter> filters) {
        return websiteDao.findPager(pager, filters);
    }

    public Website save(Website website) {
        websiteDao.save(website);
        return website;
    }

    public boolean websiteCodeUnique(String key, Long id) {
        Website website = this.websiteDao.findUniqueBy("key", key);
        return (website == null) || website.getId().equals(id);
    }

    public List<Website> getAll() {
        return this.websiteDao.getAll();
    }

    /**
     * 获取列表
     *
     * @return
     */
    public List<Website> listWebsite() {
        return this.websiteDao.find(new Criterion[0], "id", "asc");
    }

    /**
     * 静态获取列表
     *
     * @return
     */
    public static List<Website> websiteList() {
        WebsiteService websiteService = SpringContextUtil.getBeanByType(WebsiteService.class);
        return websiteService.listWebsite();
    }
}
