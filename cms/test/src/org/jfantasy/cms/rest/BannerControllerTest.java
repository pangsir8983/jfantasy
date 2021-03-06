package org.jfantasy.cms.rest;

import org.jfantasy.cms.bean.Banner;
import org.jfantasy.cms.service.BannerService;
import org.jfantasy.framework.error.IgnoreException;
import org.jfantasy.framework.util.common.JdbcUtil;
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
import org.springframework.transaction.TransactionDefinition;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration(value = "src/main/webapp")
@ContextConfiguration(locations = {"classpath:spring/applicationContext.xml"})
public class BannerControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private BannerService bannerService;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        this.testSave();
    }

    @After
    public void tearDown() throws Exception {
        Banner banner = this.bannerService.get("bannertest");
        if(banner!=null){
            this.bannerService.delete(banner.getKey());
        }
    }

    @Test
    public void testFindPager() throws Exception {
        MvcResult result = JdbcUtil.transaction(new JdbcUtil.Callback<MvcResult>() {
            @Override
            public MvcResult run()  {
                try {
                    return  mockMvc.perform(MockMvcRequestBuilders.get("/cms/banners?pageSize=15&EQS_key=bannertest")).andDo(MockMvcResultHandlers.print()).andReturn();
                } catch (Exception e) {
                   throw new IgnoreException(e.getMessage(),e);
                }
            }
        }, TransactionDefinition.PROPAGATION_REQUIRED);
        Assert.assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    public void testView() throws Exception{
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/cms/banners/bannertest")).andDo(MockMvcResultHandlers.print()).andReturn();
        Assert.assertEquals(200, result.getResponse().getStatus());
    }

    public void testSave() throws Exception{
        Banner banner = new Banner();
        banner.setKey("bannertest");
        banner.setName("接口测试");
        banner.setSize("500x600");
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/cms/banners").content(JSON.serialize(banner)).contentType(MediaType.APPLICATION_JSON)).andDo(MockMvcResultHandlers.print()).andReturn();
        Assert.assertEquals(HttpStatus.CREATED.value(), result.getResponse().getStatus());
    }


    @Test
    public void testUpdate() throws Exception{
        Banner banner = this.bannerService.get("bannertest");
        if(banner!=null){
            banner.setName("接口测试更新");
            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/cms/banners/"+banner.getKey()).content(JSON.serialize(banner)).contentType(MediaType.APPLICATION_JSON)).andDo(MockMvcResultHandlers.print()).andReturn();
            Assert.assertEquals(200, result.getResponse().getStatus());
        }
    }

    @Test
    public void testDelete() throws Exception{
        Banner banner = this.bannerService.get("bannertest");
        if(banner!=null){
            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.delete("/cms/banners/"+banner.getKey())).andDo(MockMvcResultHandlers.print()).andReturn();
            Assert.assertEquals(204, result.getResponse().getStatus());
        }
    }
}
