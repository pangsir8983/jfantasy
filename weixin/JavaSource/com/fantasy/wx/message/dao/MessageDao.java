package com.fantasy.wx.message.dao;

import com.fantasy.framework.dao.hibernate.HibernateDao;
import com.fantasy.wx.message.bean.Message;
import org.springframework.stereotype.Repository;

/**
 * Created by zzzhong on 2014/8/28.
 */
@Repository
public class MessageDao extends HibernateDao<Message, Long> {

}