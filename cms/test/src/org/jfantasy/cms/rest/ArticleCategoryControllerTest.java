package org.jfantasy.cms.rest;

import org.jfantasy.cms.bean.ArticleCategory;
import org.jfantasy.cms.service.CmsService;
import org.jfantasy.framework.util.jackson.JSON;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;


@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration(value = "src/main/webapp")
@ContextConfiguration(locations = {"classpath:spring/applicationContext.xml"})
public class ArticleCategoryControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private CmsService cmsService;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        this.testSave();
    }

    @After
    public void tearDown() throws Exception {
        ArticleCategory articleCategory = this.cmsService.get("admintest");
        if(articleCategory!=null){
            this.cmsService.delete(articleCategory.getCode());
        }
    }

    @Test
    @Transactional
    public void testFindPager() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/cms/categorys?pageSize=30&EQS_code=admintest")).andDo(MockMvcResultHandlers.print()).andReturn();
        Assert.assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @Transactional
    public void testView() throws Exception{
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/cms/categorys/admintest")).andDo(MockMvcResultHandlers.print()).andReturn();
        Assert.assertEquals(200, result.getResponse().getStatus());
    }

    public void testSave() throws Exception{
        ArticleCategory category = new ArticleCategory();
        category.setCode("admintest");
        category.setName("接口测试");
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/cms/categorys").contentType(MediaType.APPLICATION_JSON).content(JSON.serialize(category))).andDo(MockMvcResultHandlers.print()).andReturn();
        Assert.assertEquals(HttpStatus.CREATED.value(), result.getResponse().getStatus());
    }


    @Test
    public void testUpdate() throws Exception{
        ArticleCategory category = this.cmsService.get("admintest");
        if(category!=null){
            category.setName("接口测试更新");
            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/cms/categorys/" + category.getCode()).contentType(MediaType.APPLICATION_JSON).content(JSON.serialize(category))).andDo(MockMvcResultHandlers.print()).andReturn();
            Assert.assertEquals(200, result.getResponse().getStatus());
        }
    }

    @Test
    public void testDelete() throws Exception{
        ArticleCategory articleCategory = this.cmsService.get("admintest");
        if(articleCategory!=null){
            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.delete("/cms/categorys/"+articleCategory.getCode())).andDo(MockMvcResultHandlers.print()).andReturn();
            Assert.assertEquals(200, result.getResponse().getStatus());
        }
    }


}
