package com.risen.dao.impl;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Repository;

import com.jeecms.common.hibernate4.Finder;
import com.jeecms.common.hibernate4.HibernateBaseDao;
import com.jeecms.common.page.Pagination;
import com.risen.dao.ICoreDictionaryDao;
import com.risen.entity.CoreDictionary;


@Repository
public class CoreDictionaryDaoImpl extends HibernateBaseDao<CoreDictionary, Integer> implements ICoreDictionaryDao {

	@Override
	protected Class<CoreDictionary> getEntityClass() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 新增字典数据
	 */
	@Override
	public CoreDictionary save(CoreDictionary baseModel) {
		// TODO Auto-generated method stub
		getSession().save(baseModel);
		return baseModel;
	}

	/**
	 * 列表
	 */
	@Override
	public Pagination getPage(int pageNo, int pageSize , String corecdType) {
		// TODO Auto-generated method stub
		Finder f = Finder.create(" from CoreDictionary bean ");
		f.append(" where 1=1 ");
		if(!StringUtils.isBlank(corecdType)){
			f.append(" and bean.corecdType=:corecdType ");
			f.setParam("corecdType", corecdType);
		}
		f.append(" order by bean.id ");
		return find(f, pageNo, pageSize);
	}

	/**
	 * 字典类型列表
	 */
	@Override
	public List<CoreDictionary> getCorecdTypeList(){
		Finder f = Finder.create(" select bean.corecdType from CoreDictionary bean group by bean.corecdType ");
		return find(f);
	}
	
	/**
	 * 字典键值列表
	 */
	public List<CoreDictionary> getCorecdKeyList(String corecdType){
		Finder f = Finder.create(" from CoreDictionary bean ");
		f.append(" where 1=1 ");
		if(!StringUtils.isBlank(corecdType)){
			f.append(" and bean.corecdType like :corecdType ");
			f.setParam("corecdType", "%" + corecdType + "%");
		}
		return find(f);
	}

	/**
	 * 根据组织类型获取职位名称
	 */
	public List<CoreDictionary> getJobDictByOrgType(Integer type) {
		Finder f = Finder.create(" from CoreDictionary bean ");
		f.append(" where 1=1 ");
		if(type==1){//机关党委
			f.append(" and bean.id in (4,6,8,122,123,7)");
		}else if(type==2){//党总支
			f.append(" and bean.id in (4,6,8,14,123,11)");
		}else if(type==3){//支部
			f.append(" and bean.id in (4,8,11,14,123)");
		}
		f.append(" order by id");
		/*
		if(!StringUtils.isBlank(corecdType)){
			f.append(" and bean.corecdType like :corecdType ");
			f.setParam("corecdType", "%" + corecdType + "%");
		}*/
		return find(f);
	}
}
