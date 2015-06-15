package com.fantasy.wx.framework.web.filter;

import com.fantasy.framework.util.common.StringUtil;
import com.fantasy.security.SpringSecurityUtils;
import com.fantasy.security.userdetails.SimpleUser;
import com.fantasy.wx.framework.exception.WeiXinException;
import com.fantasy.wx.framework.factory.WeiXinSessionFactory;
import com.fantasy.wx.framework.factory.WeiXinSessionUtils;
import com.fantasy.wx.listener.WeiXinSessionListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

@Component
public class WeixinSessionOpenFilter extends GenericFilterBean {

    private final static Log LOG = LogFactory.getLog(WeixinSessionOpenFilter.class);

    @Autowired
    private WeiXinSessionFactory weiXinSessionFactory;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        SimpleUser simpleUser = SpringSecurityUtils.getCurrentUser(SimpleUser.class);
        if (simpleUser == null) {
            chain.doFilter(request, response);
        } else {
            String appid = (String) simpleUser.data(WeiXinSessionListener.WEIXIN_APPID);
            if (StringUtil.isBlank(appid)) {
                LOG.error(" appid 获取失败 . 请检查 applicationContext-security.xml 是否配置 WeiXinSessionLoginSuccessHandler ");
                chain.doFilter(request,response);
                return;
            }
            try {
                WeiXinSessionUtils.saveSession(weiXinSessionFactory.openSession(appid));
            } catch (WeiXinException e) {
                LOG.error(e.getMessage(), e);
            } finally {
                chain.doFilter(request, response);
                WeiXinSessionUtils.closeSession();
            }
        }
    }

}
