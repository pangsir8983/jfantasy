package com.fantasy.cms.service;


import com.fantasy.attr.bean.Attribute;
import com.fantasy.attr.bean.AttributeType;
import com.fantasy.attr.bean.AttributeVersion;
import com.fantasy.attr.bean.Converter;
import com.fantasy.attr.service.AttributeService;
import com.fantasy.attr.service.AttributeTypeService;
import com.fantasy.attr.service.AttributeVersionService;
import com.fantasy.attr.service.ConverterService;
import com.fantasy.attr.typeConverter.FileDetailTypeConverter;
import com.fantasy.attr.util.VersionUtil;
import com.fantasy.cms.bean.Article;
import com.fantasy.cms.bean.ArticleCategory;
import com.fantasy.cms.bean.Content;
import com.fantasy.file.bean.FileDetail;
import com.fantasy.file.service.FileUploadService;
import com.fantasy.framework.dao.Pager;
import com.fantasy.framework.dao.hibernate.PropertyFilter;
import com.fantasy.framework.util.common.ClassUtil;
import com.fantasy.framework.util.common.DateUtil;
import com.fantasy.framework.util.common.file.FileUtil;
import com.fantasy.framework.util.htmlcleaner.HtmlCleanerUtil;
import com.fantasy.framework.util.ognl.OgnlUtil;
import com.fantasy.framework.util.reflect.MethodProxy;
import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.htmlcleaner.TagNode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/applicationContext.xml"})
public class CmsServiceTest {

    private static Log logger = LogFactory.getLog(CmsServiceTest.class);

    @Resource
    private CmsService cmsService;

    @Resource
    private AttributeVersionService attributeVersionService;
    @Resource
    private ConverterService converterService;
    @Resource
    private AttributeTypeService attributeTypeService;
    @Resource
    private AttributeService attributeService;
    @Resource
    private FileUploadService fileUploadService;

    @Before
    public void setUp() throws Exception{
        ArticleCategory category = new ArticleCategory();
        category.setCode("TEST");
        category.setName("测试分类");
        category.setLayer(1);
        category.setDescription("测试文章分类");
        this.cmsService.save(category);

        Article article = new Article();
        article.setTitle("test-测试文章");
        article.setSummary("测试文章摘要");

        TagNode root = HtmlCleanerUtil.htmlCleaner(new URL("http://view.163.com/14/1117/10/AB8E2C1O00012Q9L.html"),"gbk");
        TagNode text = HtmlCleanerUtil.findFristTagNode(root,"//div[@class='feed-text']");
        article.setContent(new Content(HtmlCleanerUtil.getAsString(text)));

        article.setCategory(category);
        this.cmsService.save(article);
    }

    private void addArticleVersion() {
        Converter converter = new Converter();
        converter.setName("图片转换器");
        converter.setTypeConverter(FileDetailTypeConverter.class.getName());
        converter.setDescription("");
        converterService.save(converter);

        converter = converterService.findUnique(Restrictions.eq("name", "图片转换器"), Restrictions.eq("typeConverter", FileDetailTypeConverter.class.getName()));
        logger.debug(converter);
        Assert.assertNotNull(converter);

        AttributeType attributeType = new AttributeType();
        attributeType.setName("图片数据类型");
        attributeType.setDataType(FileDetail[].class.getName());
        attributeType.setConverter(converter);
        attributeType.setDescription("");
        attributeTypeService.save(attributeType);

        attributeType = attributeTypeService.findUnique(Restrictions.eq("name", "图片数据类型"));
        logger.debug(attributeType);
        Assert.assertNotNull(attributeType);

        Attribute attribute = new Attribute();
        attribute.setCode("images");
        attribute.setName("多张图片");
        attribute.setDescription("");
        attribute.setAttributeType(attributeType);
        attribute.setNonNull(true);
        attribute.setNotTemporary(false);
        attributeService.save(attribute);

        AttributeVersion version = new AttributeVersion();
        version.setNumber("1.0");
        version.setClassName(Article.class.getName());
        version.setAttributes(new ArrayList<Attribute>());
        version.getAttributes().add(attribute);
        attributeVersionService.save(version);
    }

    @After
    public void tearDown() throws Exception {
        this.testDelete();
    }

    @Test
    public void testSaveImages(){
        for(Converter converter : converterService.find(Restrictions.eq("name", "图片转换器"), Restrictions.eq("typeConverter", FileDetailTypeConverter.class.getName()))){
            this.converterService.delete(converter.getId());
        }
        //添加动态bean定义
        this.addArticleVersion();

        Article article = VersionUtil.createDynaBean(Article.class, "1.0");

        MethodProxy proxy = ClassUtil.getMethodProxy(article.getClass(), "setImages");

        article.setTitle("测试动态图片");
        article.setSummary("测试动态图片");
        article.setContent(new Content("测试动态图片"));
        article.setCategory(this.cmsService.get("TEST"));

        try {
            File file = new File(BannerServiceTest.class.getResource("banner_1.jpg").getFile());
            String mimeType = FileUtil.getMimeType(file);
            FileDetail fileDetail = fileUploadService.upload(file, mimeType, file.getName(), "test");
            VersionUtil.getOgnlUtil(article.getVersion().getAttributes().get(0).getAttributeType()).setValue("images",article,fileDetail.getFileManagerId() + ":" + fileDetail.getAbsolutePath());
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        logger.debug(article);
        this.cmsService.save(article);

        article = this.cmsService.get(article.getId());

        Object images = VersionUtil.getOgnlUtil(article.getVersion().getAttributes().get(0).getAttributeType()).getValue("images",article);

        Assert.assertEquals(1, Array.getLength(images));

        this.cmsService.delete(article.getId());

        AttributeVersion version = attributeVersionService.getVersion(Article.class, "1.0");
        if (version == null) {
            for(Converter converter : converterService.find(Restrictions.eq("name", "图片转换器"), Restrictions.eq("typeConverter", FileDetailTypeConverter.class.getName()))){
                this.converterService.delete(converter.getId());
            }
        }else {
            for (Attribute attribute : version.getAttributes()) {
                this.converterService.delete(attribute.getAttributeType().getConverter().getId());
            }
            this.attributeVersionService.delete(version.getId());
        }
    }


    public void testDelete(){
        List<PropertyFilter> filters = new ArrayList<PropertyFilter>();
        filters.add(new PropertyFilter("EQS_category.code","TEST"));
        List<Article> articles = this.cmsService.find(filters,"id","asc",10);
        for(Article article : articles){
            this.cmsService.delete(article.getId());
        }
        ArticleCategory category = this.cmsService.get("TEST");
        this.cmsService.remove(category.getCode());
    }

    @Test
    public void findPager() {
        Pager<Article> pager = new Pager<Article>();
        List<PropertyFilter> filters = new ArrayList<PropertyFilter>();
        filters.add(new PropertyFilter("EQS_category.code", "TEST"));
        pager = cmsService.findPager(pager, filters);

        Assert.assertTrue(pager.getTotalCount() > 0);

        for(Article article : pager.getPageItems()) {
            article = this.cmsService.get(article.getId());
            logger.debug(article);
            logger.debug(article.getContent().toString());
        }

    }

    @Test
    public void save() {
        List<Article> articles = this.cmsService.getArticles(new Criterion[]{Restrictions.isNotNull("version")}, 1);
        if (!articles.isEmpty()) {
            Article article = this.cmsService.get(articles.get(0).getId());
            if (!article.getAttributeValues().isEmpty()) {
                Attribute attribute = article.getAttributeValues().get(0).getAttribute();
                OgnlUtil.getInstance().setValue(attribute.getCode(), article, "123456");
                article.setTitle("JUnit测试修改标题-" + DateUtil.format("yyyy-MM-dd"));
                cmsService.save(article);
            }
        }
        articles = this.cmsService.getArticles(new Criterion[]{Restrictions.isNull("version")}, 1);
        if (!articles.isEmpty()) {
            Article article = this.cmsService.get(articles.get(0).getId());
            article.setTitle("JUnit测试修改标题-" + DateUtil.format("yyyy-MM-dd"));
            cmsService.save(article);
        }
    }

    @Test
    public void get() {
        List<Article> articles = this.cmsService.getArticles(new Criterion[]{Restrictions.isNotNull("version")}, 1);
        if (!articles.isEmpty()) {
            Article article = this.cmsService.get(articles.get(0).getId());
            //test not null
            Assert.assertNotNull(article);
            //test attrubuteValues not null
            Assert.assertNotNull(article.getAttributeValues());
        }
        articles = this.cmsService.getArticles(new Criterion[]{Restrictions.isNull("version")}, 1);
        if (!articles.isEmpty()) {
            Article article = this.cmsService.get(articles.get(0).getId());
            //test not null
            Assert.assertNotNull(article);
        }

    }

}