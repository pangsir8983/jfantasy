package com.fantasy.security.dao;

import com.fantasy.framework.dao.hibernate.HibernateDao;
import com.fantasy.security.bean.OrgRelation;
import com.fantasy.security.bean.Organization;
import org.springframework.stereotype.Repository;

/**
 * Created by yhx on 2015/1/21.
 * 组织维度
 */
@Repository
public class OrgRelationDao extends HibernateDao<OrgRelation,Long> {
}