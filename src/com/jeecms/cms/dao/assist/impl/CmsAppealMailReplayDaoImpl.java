package com.jeecms.cms.dao.assist.impl;

import java.util.Date;
import java.util.List;

import org.hibernate.SQLQuery;
import org.springframework.stereotype.Repository;

import com.jeecms.cms.dao.assist.CmsAppealMailReplayDao;
import com.jeecms.cms.entity.assist.CmsAppealMail;
import com.jeecms.common.hibernate4.HibernateBaseDao;
import com.risen.entity.CmsAppealMailReplay;

@Repository
public class CmsAppealMailReplayDaoImpl extends HibernateBaseDao<CmsAppealMailReplay, Integer> implements CmsAppealMailReplayDao {

	@Override
	public CmsAppealMailReplay findById(Integer id) {
		// TODO Auto-generated method stub
		String sql = "select * from jc_appealmail_replay where appeal_id = "+id;
		SQLQuery query = getSession().createSQLQuery(sql);
		query.addEntity(CmsAppealMailReplay.class);
		List<CmsAppealMailReplay> replays = query.list();
		CmsAppealMailReplay replay = null;
		if(replays.size()>0){
			replay = replays.get(0);
		}
		return replay;
	}
	@Override
	public CmsAppealMailReplay save(CmsAppealMailReplay bean) {
		// TODO Auto-generated method stub
		getSession().save(bean);
		return bean;
	}

	@Override
	protected Class<CmsAppealMailReplay> getEntityClass() {
		// TODO Auto-generated method stub
		return CmsAppealMailReplay.class;
	}

	

}
