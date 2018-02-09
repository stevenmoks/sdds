package com.risen.dao.impl;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.transform.AliasToBeanResultTransformer;
import org.hibernate.transform.ResultTransformer;
import org.hibernate.transform.Transformers;

import com.jeecms.common.hibernate4.Finder;
import com.jeecms.common.hibernate4.HibernateBaseDao;
import com.jeecms.common.page.Pagination;
import com.risen.dao.IRisenTargetDetailDao;
import com.risen.entity.RisenTarget;
import com.risen.entity.RisenTargetDetail;

public class RisenTargetDetailDao  extends HibernateBaseDao<RisenTargetDetail, Integer> implements IRisenTargetDetailDao{

	@Override
	protected Class<RisenTargetDetail> getEntityClass() {
		// TODO Auto-generated method stub
		return RisenTargetDetail.class;
	}

	@Override
	public void batchUpdBaseScore(Integer[] orgId, Integer score) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int confirmShare(Integer orgId, Integer score) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void delete(Integer id) {
		// TODO Auto-generated method stub
		RisenTargetDetail entity=super.get(id);
		if(entity!=null){
			getSession().delete(entity);
		}
	}

	@Override
	public RisenTargetDetail findById(Integer id) {
		// TODO Auto-generated method stub
		RisenTargetDetail entity=get(id);
		return entity;
	}

	@Override
	public Pagination findByOrgId(int pageNo, int pageSize,Integer id) {
		// TODO Auto-generated method stub
		Finder finder = Finder.create(" from RisenTargetDetail where 1=1");
		if(!StringUtils.isEmpty(String.valueOf(id))){
			finder.append(" and risentgd_orgid = "+id);
		}
		return find(finder,pageNo,pageSize);
	}

	@Override
	public Pagination getPage(int pageNo, int pageSize) {
		// TODO Auto-generated method stub
		Finder finder = Finder.create(" from RisenTargetDetail where 1=1");
		return find(finder,pageNo,pageSize);
	}

	@Override
	public List<RisenTargetDetail> getReportData(Integer orgId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RisenTargetDetail save(RisenTargetDetail voteQues) {
		// TODO Auto-generated method stub
		getSession().save(voteQues);
		return voteQues;
	}

	@Override
	public RisenTargetDetail update(RisenTargetDetail bean) {
		// TODO Auto-generated method stub
		
		//String sql = "update Risen_targetDetail set ";
		getSession().update(bean);
		return bean;
	}

	@Override
	public void updateBaseScore(Integer orgId, Integer score) {
		// TODO Auto-generated method stub
		//String sql = "update risen_targetdetail set ";
		//getSession().createSQLQuery(sql);
	}

	@Override
	public Pagination findByParentId(int pageNo, int pageSize, Integer id) {
		// TODO Auto-generated method stub
		Finder f = Finder.create(" from RisenTargetDetail where 1=1");
		if(!StringUtils.isEmpty(String.valueOf(id))){
			f.append(" and pid = "+id);
		}
		return find(f,pageNo,pageSize);
	}

	@Override
	public RisenTargetDetail findByParentId(Integer parentId,Integer id) {
		// TODO Auto-generated method stub
		String sql = "select * from Risen_TargetDetail where risentgd_pid = "+parentId +" and risentgd_orgid = "+id;
		SQLQuery query = getSession().createSQLQuery(sql);
		//query.setResultTransformer(Transformers.aliasToBean(RisenTargetDetail.class));
		query.addEntity(RisenTargetDetail.class);
		List<RisenTargetDetail> list = query.list();
		if(list.size()<=0){
			return null;
		}else{
			RisenTargetDetail model = list.get(0);
			return model;
		}
	}

	@Override
	public Pagination showAllSub(int pageNo, int pageSize, String parentId) {
		// TODO Auto-generated method stub
		Finder f = Finder.create(" from RisenTargetDetail where 1=1");
		if(!StringUtils.isEmpty(parentId)){
			f.append(" and risentgd_pid="+parentId+" order by risentgd_status asc,id desc");
		}
		return find(f,pageNo,pageSize);
	}

	@Override
	public Pagination getMyTarget(int pageNo, int pageSize, String deptId) {
		// TODO Auto-generated method stub
		Finder f = Finder.create(" from RisenTargetDetail where 1=1");
		if(!StringUtils.isEmpty(deptId)){
			f.append(" and risentgd_orgid="+deptId);
		}
		return find(f,pageNo,pageSize);
	}

	@Override
	public void deleteByPid(Integer id) {
		// TODO Auto-generated method stub
		String sql = "delete from RisenTargetDetail where risentgd_pid = "+id;
		Query query = getSession().createQuery(sql);
		query.executeUpdate();
	}

	@Override
	public List<RisenTargetDetail> getAllStart(Integer pid, String status) {
		// TODO Auto-generated method stub
		String sql = "from RisenTargetDetail where risentgd_pid = "+pid +" and risentgd_status != '"+status+"'";
		Query query = getSession().createQuery(sql);
		List<RisenTargetDetail> list = (List<RisenTargetDetail>)query.list();
		return list;
	}

	@Override
	public List<RisenTargetDetail> getAllFinishedList(Integer pid, String status) {
		// TODO Auto-generated method stub
		String sql = "from RisenTargetDetail where risentgd_pid = "+pid +" and risentgd_status = '"+status+"'";
		Query query = getSession().createQuery(sql);
		List<RisenTargetDetail> list = (List<RisenTargetDetail>)query.list();
		return list;
	}

	@Override
	public List<RisenTargetDetail> getAllUnfinishedList(Integer departId,
			Integer id) {
		// TODO Auto-generated method stub
		String sql = "from RisenTargetDetail where risentgdPid = "+id +" and risentgdStatus != '9' and risentgdOrgid = '"+departId+"'";
		Query query = getSession().createQuery(sql);
		List<RisenTargetDetail> list = (List<RisenTargetDetail>)query.list();
		return list;
	}
}