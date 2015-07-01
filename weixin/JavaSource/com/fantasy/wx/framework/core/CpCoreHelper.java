package com.fantasy.wx.framework.core;

import com.fantasy.file.FileItem;
import com.fantasy.file.manager.LocalFileManager;
import com.fantasy.framework.util.common.StringUtil;
import com.fantasy.framework.util.jackson.JSON;
import com.fantasy.framework.util.web.WebUtil;
import com.fantasy.security.bean.enums.Sex;
import com.fantasy.wx.framework.exception.WeiXinException;
import com.fantasy.wx.framework.message.*;
import com.fantasy.wx.framework.message.content.*;
import com.fantasy.wx.framework.message.user.Group;
import com.fantasy.wx.framework.message.user.OpenIdList;
import com.fantasy.wx.framework.message.user.User;
import com.fantasy.wx.framework.oauth2.Scope;
import com.fantasy.wx.framework.session.AccountDetails;
import com.fantasy.wx.framework.session.WeiXinSession;
import me.chanjar.weixin.common.bean.WxMenu;
import me.chanjar.weixin.common.bean.result.WxMediaUploadResult;
import me.chanjar.weixin.common.exception.WxErrorException;
import me.chanjar.weixin.common.util.http.URIUtil;
import me.chanjar.weixin.cp.api.WxCpConfigStorage;
import me.chanjar.weixin.cp.api.WxCpInMemoryConfigStorage;
import me.chanjar.weixin.cp.api.WxCpService;
import me.chanjar.weixin.cp.api.WxCpServiceImpl;
import me.chanjar.weixin.cp.bean.*;
import me.chanjar.weixin.cp.bean.messagebuilder.VideoBuilder;
import me.chanjar.weixin.cp.bean.outxmlbuilder.NewsBuilder;
import me.chanjar.weixin.cp.util.xml.XStreamTransformer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * 企业号
 */
public class CpCoreHelper implements WeiXinCoreHelper {


    private final static Log LOG = LogFactory.getLog(CpCoreHelper.class);

    private Map<String, WeiXinDetails> weiXinDetailsMap = new HashMap<String, WeiXinDetails>();

    @Override
    public void register(AccountDetails accountDetails) {
        if (weiXinDetailsMap.containsKey(accountDetails.getAppId())) {
            weiXinDetailsMap.remove(accountDetails.getAppId());
        }
        weiXinDetailsMap.put(accountDetails.getAppId(), new WeiXinDetails(accountDetails));
    }

    @Override
    public WeiXinMessage parseInMessage(WeiXinSession session, HttpServletRequest request) throws WeiXinException {
        String signature = request.getParameter("signature");
        String nonce = request.getParameter("nonce");
        String timestamp = request.getParameter("timestamp");
        String encrypt_type = request.getParameter("encrypt_type");
        String msg_signature = request.getParameter("msg_signature");
        InputStream input;
        try {
            input = request.getInputStream();
        } catch (IOException e) {
            throw new WeiXinException(e.getMessage(), e);
        }

        WeiXinDetails weiXinDetails = getWeiXinDetails(session.getId());

        if (!weiXinDetails.getWxCpService().checkSignature(msg_signature, timestamp, nonce, signature)) {
            // 消息签名不正确，说明不是公众平台发过来的消息
            throw new WeiXinException("非法请求");
        }

        String encryptType = StringUtils.isBlank(encrypt_type) ? "raw" : encrypt_type;

        WxCpXmlMessage inMessage;
        if ("raw".equals(encryptType)) {
            // 明文传输的消息
            inMessage = XStreamTransformer.fromXml(WxCpXmlMessage.class, input);
        } else if ("aes".equals(encryptType)) {
            // 是aes加密的消息
            inMessage = WxCpXmlMessage.fromEncryptedXml(input, weiXinDetails.getWxCpConfigStorage(), timestamp, nonce, msg_signature);
        } else {
            throw new WeiXinException("不可识别的加密类型");
        }
        LOG.debug("inMessage=>" + JSON.serialize(inMessage));
        if ("text".equals(inMessage.getMsgType())) {
            return MessageFactory.createTextMessage(inMessage.getMsgId(), inMessage.getFromUserName(), new Date(inMessage.getCreateTime()), inMessage.getContent());
        } else if ("image".equalsIgnoreCase(inMessage.getMsgType())) {
            return MessageFactory.createImageMessage(this, inMessage.getMsgId(), inMessage.getFromUserName(), new Date(inMessage.getCreateTime()), inMessage.getMediaId(), inMessage.getUrl());
        } else if ("voice".equalsIgnoreCase(inMessage.getMsgType())) {
            return MessageFactory.createVoiceMessage(inMessage.getMsgId(), inMessage.getFromUserName(), new Date(inMessage.getCreateTime()), inMessage.getMediaId(), inMessage.getFormat(), inMessage.getRecognition());
        } else if ("video".equalsIgnoreCase(inMessage.getMsgType())) {
            return MessageFactory.createVideoMessage(inMessage.getMsgId(), inMessage.getFromUserName(), new Date(inMessage.getCreateTime()), inMessage.getMediaId(), inMessage.getThumbMediaId());
        } else if ("location".equalsIgnoreCase(inMessage.getMsgType())) {
            return MessageFactory.createLocationMessage(inMessage.getMsgId(), inMessage.getFromUserName(), new Date(inMessage.getCreateTime()), inMessage.getLocationX(), inMessage.getLocationY(), inMessage.getScale(), inMessage.getLabel());
        } else if ("link".equalsIgnoreCase(inMessage.getMsgType())) {
            return MessageFactory.createLinkMessage(inMessage.getMsgId(), inMessage.getFromUserName(), new Date(inMessage.getCreateTime()), inMessage.getTitle(), inMessage.getDescription(), inMessage.getUrl());
        } else if ("event".equalsIgnoreCase(inMessage.getMsgType())) {
            return MessageFactory.createEventMessage(inMessage.getMsgId(), inMessage.getFromUserName(), new Date(inMessage.getCreateTime()), inMessage.getEvent(), inMessage.getEventKey(), inMessage.getTicket(), inMessage.getLatitude(), inMessage.getLongitude(), inMessage.getPrecision());
        } else {
            LOG.debug(inMessage);
            throw new WeiXinException("无法处理的消息类型" + inMessage.getMsgType());
        }
    }

    @Override
    public String buildOutMessage(WeiXinSession session, String encryptType, WeiXinMessage message) throws WeiXinException {
        encryptType = StringUtils.isBlank(encryptType) ? "raw" : encryptType;
        WeiXinDetails weiXinDetails = getWeiXinDetails(session.getId());
        WxCpXmlOutMessage outMessage;
        if (message instanceof TextMessage) {
            outMessage = WxCpXmlOutMessage.TEXT()
                    .content(((TextMessage) message).getContent())
                    .fromUser(message.getFromUserName())
                    .toUser(message.getToUserName())
                    .build();
        } else if (message instanceof ImageMessage) {
            Media media = ((ImageMessage) message).getContent().getMedia();
            media.setId(this.mediaUpload(session, media.getType(), media.getFileItem()));
            outMessage = WxCpXmlOutMessage.IMAGE().mediaId(media.getId()).fromUser(message.getFromUserName())
                    .toUser(message.getToUserName())
                    .build();
        } else if (message instanceof VoiceMessage) {
            Media media = ((VoiceMessage) message).getContent().getMedia();
            media.setId(this.mediaUpload(session, media.getType(), media.getFileItem()));
            outMessage = WxCpXmlOutMessage.VOICE().mediaId(media.getId()).fromUser(message.getFromUserName())
                    .toUser(message.getToUserName())
                    .build();
        } else if (message instanceof VideoMessage) {
            Media media = ((VideoMessage) message).getContent().getMedia();
            media.setId(this.mediaUpload(session, media.getType(), media.getFileItem()));
            outMessage = WxCpXmlOutMessage.VIDEO().mediaId(media.getId()).fromUser(message.getFromUserName())
                    .toUser(message.getToUserName())
                    .build();
        } /*else if (message instanceof MusicMessage) {
            Music music = ((MusicMessage) message).getContent();
            Media thumb = music.getThumb();
            thumb.setId(this.mediaUpload(session, thumb.getType(), thumb.getFileItem()));
            outMessage = WxCpXmlOutMessage.MUSIC().musicUrl(music.getUrl()).hqMusicUrl(music.getHqUrl()).title(music.getTitle()).description(music.getDescription()).thumbMediaId(thumb.getId()).fromUser(message.getFromUserName())
                    .toUser(message.getToUserName())
                    .build();
        }*/ else if (message instanceof NewsMessage) {
            List<News> newses = ((NewsMessage) message).getContent();
            NewsBuilder newsBuilder = WxCpXmlOutMessage.NEWS();
            for (News news : newses) {
                WxCpXmlOutNewsMessage.Item item = new WxCpXmlOutNewsMessage.Item();
                item.setTitle(news.getLink().getTitle());
                item.setDescription(news.getLink().getDescription());
                item.setPicUrl(news.getPicUrl());
                item.setUrl(news.getLink().getUrl());
                newsBuilder.addArticle(item);
            }
            outMessage = newsBuilder.fromUser(message.getFromUserName())
                    .toUser(message.getToUserName())
                    .build();
        } else {
            throw new WeiXinException("不支持的消息类型");
        }
        if ("raw".equals(encryptType)) {
            return XStreamTransformer.toXml((Class) this.getClass(), this);
        } else if ("aes".equals(encryptType)) {
            return outMessage.toEncryptedXml(weiXinDetails.getWxCpConfigStorage());
        } else {
            throw new WeiXinException("不可识别的加密类型");
        }
    }

    @Override
    public void sendImageMessage(WeiXinSession session, Image content, String... toUsers) throws WeiXinException {
        try {
            if (toUsers.length == 0) {
                this.sendImageMessage(session, content, -1);
                return;
            }
            //上传图片文件
            Media media = content.getMedia();
            media.setId(this.mediaUpload(session, media.getType(), media.getFileItem()));
            if (toUsers.length == 1) {
                getWeiXinDetails(session.getId()).getWxCpService().messageSend(WxCpMessage.IMAGE().toUser(toUsers[0]).mediaId(media.getId()).build());
            } else {
                for (String toUser : toUsers) {
                    this.sendImageMessage(session, content, toUser);
                }
            }
        } catch (WxErrorException e) {
            throw new WeiXinException(e.getMessage(), e);
        }
    }

    @Override
    public void sendImageMessage(WeiXinSession session, Image content, long toGroup) throws WeiXinException {
        throw new WeiXinException("企业号不支持该接口");
    }

    @Override
    public void sendVoiceMessage(WeiXinSession session, Voice content, String... toUsers) throws WeiXinException {
        try {
            if (toUsers.length == 0) {
                this.sendVoiceMessage(session, content, -1);
                return;
            }
            //上传语言文件
            Media media = content.getMedia();
            media.setId(this.mediaUpload(session, media.getType(), media.getFileItem()));
            if (toUsers.length == 1) {
                getWeiXinDetails(session.getId()).getWxCpService().messageSend(WxCpMessage.VOICE().toUser(toUsers[0]).mediaId(media.getId()).build());
            } else {
                for (String toUser : toUsers) {
                    this.sendVoiceMessage(session, content, toUser);
                }
            }
        } catch (WxErrorException e) {
            throw new WeiXinException(e.getMessage(), e);
        } catch (WeiXinException e) {
            throw new WeiXinException(e.getMessage(), e);
        }
    }

    @Override
    public void sendVoiceMessage(WeiXinSession session, Voice content, long toGroup) throws WeiXinException {
        throw new WeiXinException("企业号不支持该接口");
    }

    @Override
    public void sendVideoMessage(WeiXinSession session, Video content, String... toUsers) throws WeiXinException {
        try {
            if (toUsers.length == 0) {
                this.sendVideoMessage(session, content, -1);
                return;
            }
            //上传视频
            Media media = content.getMedia();
            media.setId(this.mediaUpload(session, media.getType(), media.getFileItem()));
            //发送消息
            if (toUsers.length == 1) {
                //上传缩略图
                Media thumb = content.getThumb();
                thumb.setId(this.mediaUpload(session, thumb.getType(), thumb.getFileItem()));
                VideoBuilder videoBuilder = WxCpMessage.VIDEO().toUser(toUsers[0]).mediaId(media.getId()).thumbMediaId(thumb.getId());
                if (StringUtil.isNotBlank(content.getTitle())) {
                    videoBuilder.title(content.getTitle());
                }
                if (StringUtil.isNotBlank(content.getDescription())) {
                    videoBuilder.description(content.getDescription());
                }
                getWeiXinDetails(session.getId()).getWxCpService().messageSend(videoBuilder.build());
            } else {
                for (String toUser : toUsers) {
                    this.sendVideoMessage(session, content, toUser);
                }
            }
        } catch (WxErrorException e) {
            throw new WeiXinException(e.getMessage(), e);
        } catch (WeiXinException e) {
            throw new WeiXinException(e.getMessage(), e);
        }
    }

    @Override
    public void sendVideoMessage(WeiXinSession session, Video content, long toGroup) throws WeiXinException {
        throw new WeiXinException("企业号不支持该接口");
    }

    @Override
    public void sendMusicMessage(WeiXinSession session, Music content, String toUser) throws WeiXinException {
        throw new WeiXinException("企业号不支持该接口");
    }

    @Override
    public void sendNewsMessage(WeiXinSession session, List<News> content, String toUser) throws WeiXinException {
        try {
            me.chanjar.weixin.cp.bean.messagebuilder.NewsBuilder newsBuilder = WxCpMessage.NEWS().toUser(toUser);
            for (News news : content) {
                WxCpMessage.WxArticle article = new WxCpMessage.WxArticle();
                article.setPicUrl(news.getPicUrl());
                article.setTitle(news.getLink().getTitle());
                article.setDescription(news.getLink().getDescription());
                article.setUrl(news.getLink().getUrl());
                newsBuilder.addArticle(article);
            }
            getWeiXinDetails(session.getId()).getWxCpService().messageSend(newsBuilder.build());
        } catch (WxErrorException e) {
            throw new WeiXinException(e.getMessage(), e);
        } catch (WeiXinException e) {
            throw new WeiXinException(e.getMessage(), e);
        }
    }

    public void sendNewsMessage(WeiXinSession session, List<Article> articles, String... toUsers) throws WeiXinException {
        throw new WeiXinException("企业号不支持该接口");
    }

    @Override
    public void sendNewsMessage(WeiXinSession session, List<Article> articles, long toGroup) throws WeiXinException {
        throw new WeiXinException("企业号不支持该接口");
    }

    @Override
    public String oauth2buildAuthorizationUrl(WeiXinSession session, String redirectUri, Scope scope, String state) throws WeiXinException {
        WxCpConfigStorage wxCpConfigStorage = getWeiXinDetails(session.getId()).getWxCpConfigStorage();
        String url = "https://open.weixin.qq.com/connect/oauth2/authorize?";
        url += "appid=" + wxCpConfigStorage.getCorpId();
        url += "&redirect_uri=" + URIUtil.encodeURIComponent(redirectUri);
        url += "&response_type=code";
        url += "&scope=" + scope.getValue();
        if (StringUtil.isNotBlank(state)) {
            url += "&state=" + state;
        }
        url += "#wechat_redirect";
        return url;
    }

    public User getOauth2User(WeiXinSession session, String code) throws WeiXinException {
        try {
            String[] user = getWeiXinDetails(session.getId()).getWxCpService().oauth2getUserInfo(code);
            return null;
        } catch (WxErrorException e) {
            throw new WeiXinException(e.getMessage(), e);
        }
    }

    @Override
    public void sendTextMessage(WeiXinSession session, String content, String... toUsers) throws WeiXinException {
        try {
            if (toUsers.length == 0) {
                sendTextMessage(session, content, -1);
            } else if (toUsers.length == 1) {
                getWeiXinDetails(session.getId()).getWxCpService().messageSend(WxCpMessage.TEXT().toUser(toUsers[0]).content(content).build());
            } else {
                for (String toUser : toUsers) {
                    this.sendTextMessage(session, content, toUser);
                }
            }
        } catch (WxErrorException e) {
            throw new WeiXinException(e.getMessage(), e);
        } catch (WeiXinException e) {
            throw new WeiXinException(e.getMessage(), e);
        }
    }

    @Override
    public void sendTextMessage(WeiXinSession session, String content, long toGroup) throws WeiXinException {
        throw new WeiXinException("企业号不支持该接口");
    }

    @Override
    public List<Group> getGroups(WeiXinSession session) throws WeiXinException {
        throw new WeiXinException("企业号不支持该接口");
    }

    @Override
    public Group groupCreate(WeiXinSession session, String groupName) throws WeiXinException {
        throw new WeiXinException("企业号不支持该接口");
    }

    @Override
    public void groupUpdate(WeiXinSession session, long groupId, String groupName) throws WeiXinException {
        throw new WeiXinException("企业号不支持该接口");
    }

    @Override
    public void userUpdateGroup(WeiXinSession session, String userId, long groupId) throws WeiXinException {
        throw new WeiXinException("企业号不支持该接口");
    }

    @Override
    public List<User> getUsers(WeiXinSession session) throws WeiXinException {
        return null;
    }

    @Override
    public OpenIdList getOpenIds(WeiXinSession session) {
        return null;
    }

    @Override
    public OpenIdList getOpenIds(WeiXinSession session, String nextOpenId) {
        return null;
    }

//    @Override
//    public List<String> getUsers(WeiXinSession session,String nextOpenId) throws WeiXinException {
//        try {
//            List<String> openIds = new ArrayList<String>();
//            WxCpUserList userList = getWeiXinDetails(session.getId()).getWxCpService().userList(null);
//            openIds.addAll(userList.getOpenIds());
//            while (userList.getTotal() > userList.getCount()) {
//                userList = getWeiXinDetails(session.getId()).getWxCpService().userList(userList.getNextOpenId());
//                openIds.addAll(userList.getOpenIds());
//            }
//        } catch (WxErrorException e) {
//            throw new WeiXinException(e.getMessage(),e);
//        } catch (WeiXinException e) {
//            throw new WeiXinException(e.getMessage(),e);
//        }
//        return null;
//    }

    @Override
    public Long getGroupIdByUserId(WeiXinSession session, String openId) throws WeiXinException {
        throw new WeiXinException("企业号不支持该接口");
    }

    @Override
    public User getUser(WeiXinSession session, String userId) throws WeiXinException {
        try {
            return toUser(getWeiXinDetails(session.getId()).getWxCpService().userGet(userId));
        } catch (WxErrorException e) {
            throw new WeiXinException(e.getMessage(), e);
        } catch (WeiXinException e) {
            throw new WeiXinException(e.getMessage(), e);
        }
    }

    private User toUser(WxCpUser wxCpUser) {
        if (wxCpUser == null) {
            return null;
        }
        /*
         User user = new User();
        user.setOpenId(wxCpUser.getUserId());
        user.setAvatar(wxCpUser.getHeadImgUrl());
        user.setCity(wxCpUser.getCity());
        user.setCountry(wxCpUser.getCountry());
        user.setProvince(wxCpUser.getProvince());
        user.setLanguage(wxCpUser.getLanguage());
        user.setNickname(wxCpUser.getNickname());
        user.setSex(toSex(wxCpUser.getSex()));
        user.setSubscribe(wxCpUser.isSubscribe());
        if (wxCpUser.getSubscribeTime() != null) {
            user.setSubscribeTime(new Date(wxCpUser.getSubscribeTime()));
        }
        user.setUnionid(wxCpUser.getUnionId());
        return user;
        */
        return null;
    }

    private Sex toSex(String sex) {
        if ("男".equals(sex)) {
            return Sex.male;
        }
        if ("女".equals(sex)) {
            return Sex.female;
        }
        return Sex.unknown;
    }

    @Override
    public String mediaUpload(WeiXinSession session, Media.Type mediaType, FileItem fileItem) throws WeiXinException {
        try {
            WxMediaUploadResult uploadMediaRes = getWeiXinDetails(session.getId()).getWxCpService().mediaUpload(mediaType.name(), WebUtil.getExtension(fileItem.getName()), fileItem.getInputStream());
            return mediaType == Media.Type.thumb ? uploadMediaRes.getThumbMediaId() : uploadMediaRes.getMediaId();
        } catch (WxErrorException e) {
            throw new WeiXinException(e.getMessage(), e);
        } catch (WeiXinException e) {
            throw new WeiXinException(e.getMessage(), e);
        } catch (IOException e) {
            throw new WeiXinException(e.getMessage(), e);
        }
    }

    private LocalFileManager fileManager = new LocalFileManager(System.getProperty("java.io.tmpdir"));

    public FileItem mediaDownload(WeiXinSession session, String mediaId) throws WeiXinException {
        try {
            File file = getWeiXinDetails(session.getId()).getWxCpService().mediaDownload(mediaId);
            if (file == null) {
                return null;
            }
            return fileManager.retrieveFileItem(file);
        } catch (WxErrorException e) {
            throw new WeiXinException(e.getMessage(), e);
        }
    }

    @Override
    public void refreshMenu(WeiXinSession session, Menu... menus) throws WeiXinException {
        WxMenu wxMenu = new WxMenu();
        for (Menu menu : menus) {
            WxMenu.WxMenuButton wxMenuButton = new WxMenu.WxMenuButton();
            wxMenuButton.setName(menu.getName());
            wxMenuButton.setType(menu.getType().getValue());
            wxMenuButton.setUrl(menu.getUrl());
            wxMenuButton.setKey(menu.getKey());

            for (Menu subMenu : menu.getChildren()) {
                WxMenu.WxMenuButton subWxMenuButton = new WxMenu.WxMenuButton();
                subWxMenuButton.setName(subMenu.getName());
                subWxMenuButton.setType(subMenu.getType().getValue());
                subWxMenuButton.setUrl(subMenu.getUrl());
                subWxMenuButton.setKey(subMenu.getKey());
                wxMenuButton.getSubButtons().add(subWxMenuButton);
            }

            wxMenu.getButtons().add(wxMenuButton);
        }
        try {
            getWeiXinDetails(session.getId()).getWxCpService().menuCreate(wxMenu);
        } catch (WxErrorException e) {
            throw new WeiXinException(e.getMessage(), e);
        }
    }

    public Jsapi getJsapi(WeiXinSession session) throws WeiXinException {
        return getWeiXinDetails(session.getId()).getJsapi();
    }

    @Override
    public List<Menu> getMenus(WeiXinSession session) throws WeiXinException {
        try {
            WxMenu wxMenu = getWeiXinDetails(session.getId()).getWxCpService().menuGet();
            List<Menu> menus = new ArrayList<Menu>(wxMenu.getButtons().size());
            for (WxMenu.WxMenuButton button : wxMenu.getButtons()) {
                Menu.MenuType type = StringUtil.isBlank(button.getType()) ? Menu.MenuType.UNKNOWN : Menu.MenuType.valueOf(button.getType().toUpperCase());
                if (button.getSubButtons().isEmpty()) {
                    menus.add(new Menu(type, button.getName(), StringUtil.defaultValue(button.getKey(), button.getUrl())));
                } else {
                    List<Menu> subMenus = new ArrayList<Menu>();
                    for (WxMenu.WxMenuButton wxMenuButton : button.getSubButtons()) {
                        subMenus.add(new Menu(Menu.MenuType.valueOf(wxMenuButton.getType().toUpperCase()), wxMenuButton.getName(), StringUtil.defaultValue(wxMenuButton.getKey(), wxMenuButton.getUrl())));
                    }
                    menus.add(new Menu(type, button.getName(), StringUtil.defaultValue(button.getKey(), button.getUrl()), subMenus.toArray(new Menu[subMenus.size()])));
                }
            }
            return menus;
        } catch (WxErrorException e) {
            throw new WeiXinException(e.getMessage(), e);
        }
    }

    @Override
    public void clearMenu(WeiXinSession session) throws WeiXinException {
        try {
            getWeiXinDetails(session.getId()).getWxCpService().menuDelete();
        } catch (WxErrorException e) {
            throw new WeiXinException(e.getMessage(), e);
        }
    }

    private WeiXinDetails getWeiXinDetails(String appid) throws WeiXinException {
        if (!weiXinDetailsMap.containsKey(appid)) {
            throw new WeiXinException("[appid=" + appid + "]未注册！");
        }
        return weiXinDetailsMap.get(appid);
    }

    private static class WeiXinDetails {
        private WxCpService wxCpService;
        private WxCpConfigStorage wxCpConfigStorage;
        private Jsapi jsapi;

        public WeiXinDetails(AccountDetails accountDetails) {
            this.wxCpService = new WxCpServiceImpl();
            this.jsapi = new DefaultJsapi(new WeiXinCpService(this.wxCpService));

            WxCpInMemoryConfigStorage wxCpConfigStorage = new WxCpInMemoryConfigStorage();
            wxCpConfigStorage.setCorpId(accountDetails.getAppId());
            wxCpConfigStorage.setCorpSecret(accountDetails.getSecret());
            wxCpConfigStorage.setToken(accountDetails.getToken());
            wxCpConfigStorage.setAesKey(accountDetails.getAesKey());

            this.wxCpService.setWxCpConfigStorage(this.wxCpConfigStorage = wxCpConfigStorage);
        }


        public Jsapi getJsapi() {
            return jsapi;
        }

        public WxCpService getWxCpService() {
            return wxCpService;
        }

        public WxCpConfigStorage getWxCpConfigStorage() {
            return wxCpConfigStorage;
        }
    }


}