package com.fantasy.wx.message.service;

import com.fantasy.framework.dao.Pager;
import com.fantasy.framework.dao.hibernate.PropertyFilter;
import com.fantasy.framework.util.jackson.JSON;
import com.fantasy.wx.message.bean.Message;
import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/applicationContext.xml"})
public class MessageServiceTest {
    private static final Log logger = LogFactory.getLog(Message.class);
    @Resource
    private IMessageService iMessageService;

    @Before
    public void setUp() throws Exception {
        testSave();
    }

    @After
    public void tearDown() throws Exception {
        Pager<Message> pager = testFindPager();
        Message m = pager.getPageItems().get(0);
        m = iMessageService.getMessage(m.getId());
        logger.debug(JSON.serialize(m));
        iMessageService.delete(m.getId());
    }

    @Test
    public void test() {

    }

    public Pager<Message> testFindPager() throws Exception {
        Pager<Message> pager = new Pager<Message>();
        List<PropertyFilter> messageList = new ArrayList<PropertyFilter>();
        messageList.add(new PropertyFilter("EQL_createTime", "1414221271382"));
        Pager p = iMessageService.findPager(pager, messageList);
        Assert.assertNotNull(p.getPageItems());
        logger.debug(JSON.serialize(p));
        return p;
    }

    public void testSave() throws Exception {
        Message m = new Message();
        m.setContent("hello world");
        m.setCreateTime(1414221271382L);
        m.setMsgId(6074034108453352314L);
        m.setMsgType("text");
        m.setToUserName("gh_889dabbab6cb");
        m.setFromUserName("o8W9zt_0puksLdwJlqTGXdH9ViRU");
        iMessageService.save(m);
    }
}