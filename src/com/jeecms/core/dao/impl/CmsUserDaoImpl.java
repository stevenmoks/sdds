package com.jeecms.core.dao.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.hibernate.type.StandardBasicTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.jeecms.common.hibernate4.Finder;
import com.jeecms.common.hibernate4.HibernateBaseDao;
import com.jeecms.common.page.Pagination;
import com.jeecms.core.dao.CmsDepartmentDao;
import com.jeecms.core.dao.CmsUserDao;
import com.jeecms.core.entity.CmsDepartment;
import com.jeecms.core.entity.CmsUser;
import com.risen.dao.CmsFloatingDao;

@Repository
public class CmsUserDaoImpl extends HibernateBaseDao<CmsUser, Integer>
		implements CmsUserDao {
	public Pagination getPage(String username, String sddscpIdnumber, Integer siteId,
			Integer groupId, Boolean disabled, Boolean admin, Integer rank,
			String realName,Integer departId,Integer roleId,
			Boolean allChannel,Boolean allControlChannel,
			int pageNo, int pageSize,String sddscpUsertype,String isadminlist) {
		String hql = "";
		if("1".equals(isadminlist)){
			hql = "select bean from CmsUser bean ";
		}else if("0".equals(isadminlist)){
			hql = "select bean from CmsUser bean join bean.userExtSet ext ";
		}else {
			
		}
		Finder f = Finder.create(hql);
		if (siteId != null||allChannel!=null||allControlChannel!=null) {
			f.append(" join bean.userSites userSite");
		}
		if(roleId!=null){
			f.append(" join bean.roles role ");
		}
		f.append(" where 1=1");
		if(siteId!=null){
			f.append(" and  userSite.site.id=:siteId");
			f.setParam("siteId", siteId);
		}
		if (!StringUtils.isBlank(username)) {
			f.append(" and bean.username like :username");
			f.setParam("username", "%" + username + "%");
		}
		if (!StringUtils.isBlank(sddscpIdnumber)) {
			f.append(" and bean.sddscpIdnumber like :sddscpIdnumber");
			f.setParam("sddscpIdnumber", "%" + sddscpIdnumber + "%");
		}
		if (groupId != null) {
			f.append(" and bean.group.id=:groupId");
			f.setParam("groupId", groupId);
		}
		if (disabled != null) {
			f.append(" and bean.disabled=:disabled");
			f.setParam("disabled", disabled);
		}
		if (admin != null) {
			f.append(" and bean.admin=:admin");
			f.setParam("admin", admin);
		}
		if (rank != null) {
			f.append(" and bean.rank<=:rank");
			f.setParam("rank", rank);
		}
		if("0".equals(isadminlist)){
			if (!StringUtils.isBlank(realName)) {
				f.append(" and ext.realname like :realname");
				f.setParam("realname", "%" + realName + "%");
			}
		}
		if(departId!=null){
			f.append(" and (bean.department.id=:departId");
			f.append(" or bean.sddscpJgdw=:departId");
			f.append(" or bean.sddscpDzz=:departId");
			f.append(" or bean.sddscpZb=:departId)");
			f.setParam("departId", departId);
		}
		if(roleId!=null){
			f.append(" and role.id=:roleId");
			f.setParam("roleId", roleId);
		}
		if (allChannel != null) {
			f.append(" and userSite.allChannel=:allChannel");
			f.setParam("allChannel", allChannel);
		}
		if (allControlChannel != null) {
			f.append(" and userSite.allChannelControl=:allControlChannel");
			f.setParam("allControlChannel", allControlChannel);
		}
		//用户有多个站的管理权限需要去重复
		/*
		if(allChannel!=null||allControlChannel!=null){
			f.append(" group by bean having count(bean)=1");
		}
		*/
		if(!StringUtils.isBlank(sddscpUsertype)){
			f.append(" and bean.sddscpUsertype=:sddscpUsertype ");
			f.setParam("sddscpUsertype", sddscpUsertype);
		}
		if("0".equals(isadminlist)){
			f.append(" and bean.sddscpIsperororg=:sddscpIsperororg ");
			f.setParam("sddscpIsperororg", "1");
		}
		f.append(" order by bean.id desc");
		return find(f, pageNo, pageSize);
	}
	
	@SuppressWarnings("unchecked")
	public List<CmsUser> getList(String username, String email, Integer siteId,
			Integer groupId, Boolean disabled, Boolean admin, Integer rank) {
		Finder f = Finder.create("select bean from CmsUser bean");
		if (siteId != null) {
			f.append(" join bean.userSites userSite");
			f.append(" where userSite.site.id=:siteId");
			f.setParam("siteId", siteId);
		} else {
			f.append(" where 1=1");
		}
		if (!StringUtils.isBlank(username)) {
			f.append(" and bean.username like :username");
			f.setParam("username", "%" + username + "%");
		}
		if (!StringUtils.isBlank(email)) {
			f.append(" and bean.email like :email");
			f.setParam("email", "%" + email + "%");
		}
		if (groupId != null) {
			f.append(" and bean.group.id=:groupId");
			f.setParam("groupId", groupId);
		}
		if (disabled != null) {
			f.append(" and bean.disabled=:disabled");
			f.setParam("disabled", disabled);
		}
		if (admin != null) {
			f.append(" and bean.admin=:admin");
			f.setParam("admin", admin);
		}
		if (rank != null) {
			f.append(" and bean.rank<=:rank");
			f.setParam("rank", rank);
		}
		f.append(" order by bean.id desc");
		return find(f);
	}

	@SuppressWarnings("unchecked")
	public List<CmsUser> getAdminList(Integer siteId, Boolean allChannel,
			Boolean disabled, Integer rank) {
		Finder f = Finder.create("select bean from CmsUser");
		f.append(" bean join bean.userSites us");
		f.append(" where us.site.id=:siteId");
		f.setParam("siteId", siteId);
		if (allChannel != null) {
			f.append(" and us.allChannel=:allChannel");
			f.setParam("allChannel", allChannel);
		}
		if (disabled != null) {
			f.append(" and bean.disabled=:disabled");
			f.setParam("disabled", disabled);
		}
		if (rank != null) {
			f.append(" and bean.rank<=:rank");
			f.setParam("rank", rank);
		}
		f.append(" and bean.admin=true");
		f.append(" order by bean.id asc");
		return find(f);
	}
	
	public Pagination getAdminsByDepartId(Integer id, int pageNo,int pageSize){
		Finder f = Finder.create("select bean from CmsUser bean ");
		f.append(" where bean.sddscpJgdw=:departId");
		f.append(" or bean.sddscpDzz=:departId");
		f.append(" or bean.sddscpZb=:departId)");
		f.setParam("departId", id);
		f.append(" and bean.admin=true");
		f.append(" order by bean.id asc");
		return find(f,pageNo,pageSize);
	}
	
	public Pagination getAdminsByRoleId(Integer roleId, int pageNo, int pageSize){
		Finder f = Finder.create("select bean from CmsUser");
		f.append(" bean join bean.roles role");
		f.append(" where role.id=:roleId");
		f.setParam("roleId", roleId);
		f.append(" and bean.admin=true");
		f.append(" order by bean.id asc");
		return find(f,pageNo,pageSize);
	}

	public CmsUser findById(Integer id) {
		CmsUser entity = get(id);
		return entity;
	}

	public CmsUser findByUsername(String username) {
		return findUniqueByProperty("username", username);
	}

	public int countByUsername(String username) {
		String hql = "select count(*) from CmsUser bean where bean.username=:username";
		Query query = getSession().createQuery(hql);
		query.setParameter("username", username);
		return ((Number) query.iterate().next()).intValue();
	}
	public int countMemberByUsername(String username) {
		String hql = "select count(*) from CmsUser bean where bean.username=:username and bean.admin=false";
		Query query = getSession().createQuery(hql);
		query.setParameter("username", username);
		return ((Number) query.iterate().next()).intValue();
	}

	public int countByEmail(String email) {
		String hql = "select count(*) from CmsUser bean where bean.email=:email";
		Query query = getSession().createQuery(hql);
		query.setParameter("email", email);
		return ((Number) query.iterate().next()).intValue();
	}

	public CmsUser save(CmsUser bean) {
		getSession().save(bean);
		return bean;
	}

	public CmsUser deleteById(Integer id) {
		CmsUser entity = super.get(id);
		if (entity != null) {
			getSession().delete(entity);
		}
		return entity;
	}
	
	public boolean loginCodeExist(long codeId){
		String hql="select bean from CmsUser bean where bean.sddscpOrglogincode ="+codeId;
		Query query = getSession().createQuery(hql);
		List list=query.list();
		if(list.size()>0){
			return false;
		}else{
			return true;
		}
		
	};
	/**
	 * 根据身份证号来查询人员信息
	 * date:2016/10/11
	 * author:dongliang
	 */
	
	public CmsUser findByUserCardId(String sddscpIdnumber){
		return findUniqueByProperty("sddscpIdnumber", sddscpIdnumber);
	}
	/**
     * 根据组织ID删除数据
     * 删除组织时，级联删除user表数据(仅限删除组织时使用)
     */
    public Integer deleteUserBydepartid(Integer departId){
    	String sql = " delete from JC_USER WHERE depart_id = " + departId;
    	Query query = getSession().createSQLQuery(sql);
    	return query.executeUpdate();
    }
    /**
	 * 根据组织ID查人员
	 * @param departId
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	public Pagination getMemberByDepartId(Integer departId,String sddscpAssess,String sddscpIsinjob,String sddscpChanges,int pageNo,int pageSize){
		Finder finder = Finder.create("select bean from CmsUser bean ");
		finder.append(" where (bean.department.id=:departId or bean.sddscpJgdw=:departId or bean.sddscpDzz=:departId or bean.sddscpZb=:departId) and bean.sddscpIsperororg = 1 ");
		finder.setParam("departId", departId);
		if(!StringUtils.isBlank(sddscpIsinjob)){
			finder.append(" and bean.sddscpIsinjob :sddscpIsinjob");
			finder.setParam("sddscpIsinjob", sddscpIsinjob);
		}
		if(!StringUtils.isBlank(sddscpChanges)){
			finder.append(" and bean.sddscpChanges :sddscpChanges");
			finder.setParam("sddscpChanges", sddscpChanges);
		}
		if(!StringUtils.isBlank(sddscpAssess)){
			finder.append(" and bean.sddscpAssess :sddscpAssess");
			finder.setParam("sddscpAssess", sddscpAssess);
		}
		finder.append(" and bean.admin=false");
		finder.append(" order by bean.id asc");
		return find(finder,pageNo,pageSize);
	}
	
    /**
	 * 根据组织ID权限查人员
	 * @param departId
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	public Pagination getMemberQXByDepartId(Integer departId,String sddscpAssess,String sddscpIsinjob,String sddscpChanges,int pageNo,int pageSize){
		StringBuffer sb = new StringBuffer();
		StringBuffer sa = new StringBuffer();
		StringBuffer sc = new StringBuffer();
		sb.append("select * from jc_user bean ");
		sc.append("select count(*) from jc_user bean ");
		if(departId!=1){
			if(departId==68){
				sa.append(" where (bean.depart_id in (select bean2.depart_id from jc_department bean2 where bean2.parent_id = 68) ");
				sa.append(" or bean.SDDSCP_JGDW in ( select bean2.depart_id from jc_department bean2 where bean2.parent_id = 68) ");
				sa.append(" or bean.SDDSCP_DZZ in ( select bean2.depart_id from jc_department bean2 where bean2.parent_id = 68) ");
				sa.append(" or bean.SDDSCP_ZB in ( select bean2.depart_id from jc_department bean2 where bean2.parent_id = 68)) ");
				sa.append(" and bean.SDDSCP_ISPERORORG = 1 ");
			}else{
				sa.append(" where (bean.depart_id in (select bean2.depart_id from jc_department bean2 start with  bean2.depart_id=");
				sa.append(departId);
				sa.append(" connect by prior bean2.depart_id=bean2.parent_id) or bean.SDDSCP_JGDW in ( select bean2.depart_id from jc_department bean2 start with  bean2.depart_id= ");
				sa.append(departId);
				sa.append(" connect by prior bean2.depart_id=bean2.parent_id) or bean.SDDSCP_DZZ in ( select bean2.depart_id from jc_department bean2 start with  bean2.depart_id= ");
				sa.append(departId);
				sa.append(" connect by prior bean2.depart_id=bean2.parent_id) or bean.SDDSCP_ZB in ( select bean2.depart_id from jc_department bean2 start with  bean2.depart_id= ");
				sa.append(departId);
				sa.append(" connect by prior bean2.depart_id=bean2.parent_id)) and bean.SDDSCP_ISPERORORG = 1 ");
				}
		}else{
			sa.append(" where (bean.depart_id in (select bean2.depart_id from jc_department bean2 start with bean2.parent_id is null or bean2.parent_id=''");
			sa.append(" connect by prior bean2.depart_id=bean2.parent_id) or bean.SDDSCP_JGDW in ( select bean2.depart_id from jc_department bean2 start with bean2.parent_id is null or bean2.parent_id='' ");
			sa.append(" connect by prior bean2.depart_id=bean2.parent_id) or bean.SDDSCP_DZZ in ( select bean2.depart_id from jc_department bean2 start with bean2.parent_id is null or bean2.parent_id='' ");
			sa.append(" connect by prior bean2.depart_id=bean2.parent_id) or bean.SDDSCP_ZB in ( select bean2.depart_id from jc_department bean2 start with bean2.parent_id is null or bean2.parent_id='' ");
			sa.append(" connect by prior bean2.depart_id=bean2.parent_id)) and bean.SDDSCP_ISPERORORG = 1 ");
		}
		
		
		if(!StringUtils.isBlank(sddscpIsinjob)){
			sa.append(" and bean.SDDSCP_ISINJOB = ");
			sa.append("'" + sddscpIsinjob + "'");
		}
		if(!StringUtils.isBlank(sddscpAssess)){
			sa.append(" and bean.SDDSCP_ASSESS like ");
			sa.append("'" + sddscpAssess + "'");
		}
		if(!StringUtils.isBlank(sddscpChanges)){
			sa.append(" and bean.SDDSCP_CHANGES = '"+sddscpChanges+"'");
		}
		sa.append(" and bean.sddscp_Isperororg='1' ");
		sc.append(sa.toString());
		sa.append(" and rownum>=").append((pageNo - 1) * pageSize).append(" and rownum<=").append(pageNo * pageSize);
		sa.append(" order by bean.USER_ID asc");
		sb.append(sa.toString());
		List<CmsUser> list = this.getSession().createSQLQuery(sb.toString()).addEntity(CmsUser.class).list();
		Object count = this.getSession().createSQLQuery(sc.toString()).uniqueResult();
		Pagination p = new Pagination();
		p.setPageNo(pageNo);
		p.setPageSize(pageSize);
		p.setList(list);
		p.setTotalCount(count==null?0:Integer.parseInt(count.toString()));
		return p;
	}
	public Pagination getMemberQXByDepartIdAndYear(Integer departId,Integer newDeptId,String sddscpAssess,String sddscpIsinjob,String sddscpChanges,int pageNo,int pageSize,String risendsYear,String sddscpHistorydy){
		StringBuffer sb = new StringBuffer();
		StringBuffer sa = new StringBuffer();
		StringBuffer sc = new StringBuffer();
		StringBuffer sd = new StringBuffer();
		sd.append(" select bean.* from (");
		sb.append("select rownum rn,bean.* from jc_user bean where 1=1");
		sc.append("select count(*) from jc_user bean where 1=1");
		//sc.append("select count(*) from (");
		if(departId!=1){
			sa.append(" and (bean.SDDSCP_ZB in (select bean2.depart_id from jc_department bean2 start with  bean2.depart_id=");
			sa.append(departId + " connect by prior bean2.depart_id=bean2.parent_id)");
			sa.append(" or bean.sddscp_newadddy in (select bean2.depart_id from jc_department bean2 start with  bean2.depart_id=");
			sa.append(departId + " connect by prior bean2.depart_id=bean2.parent_id)) and bean.SDDSCP_ISPERORORG = 1 ");
		}else{
			sa.append(" and (bean.SDDSCP_ZB in (select bean2.depart_id from jc_department bean2 start with bean2.parent_id is null or bean2.parent_id='' ");
			sa.append(" connect by prior bean2.depart_id=bean2.parent_id)");
			sa.append(" or bean.sddscp_newadddy in (select bean2.depart_id from jc_department bean2 start with bean2.parent_id is null or bean2.parent_id='' ");
			sa.append(" connect by prior bean2.depart_id=bean2.parent_id)) and bean.SDDSCP_ISPERORORG = 1 ");
			//sa.append(" connect by prior bean2.depart_id=bean2.parent_id))");

		}
		//String IsinjobValue = request.getParameter(sddscpIsinjob);
		if(!StringUtils.isBlank(sddscpIsinjob)){
			
			// 1代表在职党员
			if("1".equals(sddscpIsinjob)){
			    sa.append(" and bean.SDDSCP_ISINJOB = ");
				sa.append("'" + sddscpIsinjob + "'");
			}
			// 2 代表历史党员
			if("2".equals(sddscpIsinjob)){
				sa.append(" and bean.SDDSCP_ISINJOB = 1 ");
				//sa.append("'" + sddscpIsinjob + "'");
			}
			// 0 为离退休状态的党员
			if("0".equals(sddscpIsinjob)){
				sa.append(" and bean.SDDSCP_ISINJOB = 0 ");

			}
		}else{
			sa.append(" and bean.SDDSCP_ISINJOB <>2 ");
		}
		
		
			
		
		if(!StringUtils.isBlank(sddscpChanges)){
			sa.append(" and bean.SDDSCP_POLITICALTYPE = '2'");
		}
		sa.append(" and bean.sddscp_isperororg='1' ");
		if(!StringUtils.isBlank(sddscpAssess)){
			sc.append(sa.toString());
			if("1".equalsIgnoreCase(sddscpAssess)){
				sb.append(" and bean.user_id in (select risends_userid from risen_discussion where risends_score=1");
				sc.append(" and bean.user_id in (select risends_userid from risen_discussion where risends_score=1");
			}
			if("2".equalsIgnoreCase(sddscpAssess)){
				sb.append(" and bean.user_id in (select risends_userid from risen_discussion where risends_score=4");
				sc.append(" and bean.user_id in (select risends_userid from risen_discussion where risends_score=4");
			}
			if(!StringUtils.isBlank(risendsYear)){
				sb.append(" and risends_year = "+risendsYear+")");
				sc.append(" and risends_year = "+risendsYear+")");
			}
		}else{
			sc.append(sa.toString());
		}
		sb.append(sa.toString());
		sd.append(sb.toString()).append(" ) bean where 1=1");
		sd.append(" and rn>").append((pageNo - 1) * pageSize).append(" and rn<=").append(pageNo * pageSize);
		sd.append(" order by bean.rn asc");
		// 用户点击历史党员的时候
		if("2".equals(sddscpIsinjob)){
			CmsDepartment depart = departDao.findById(departId);
			if(depart!=null){
				sc.delete(0,sd.length());
				sd.delete(0,sd.length());
				sc.append("select count(*) from jc_user where sddscp_isperororg=1");
				sd.append("select * from (select rownum rn,bean.* from jc_user bean where bean.sddscp_isperororg=1");
				if(departId.intValue()==1){
					sd.append(" and bean.sddscp_isinjob=2");
					sc.append(" and sddscp_isinjob=2");
				}else{
					sd.append(" and bean.sddscp_zb in (select bean2.depart_id from jc_department bean2 start with  bean2.depart_id="+ departId+" connect by prior bean2.depart_id=bean2.parent_id) and bean.sddscp_isinjob=2");
					sd.append(" or instr(bean.sddscp_historydy,"+departId+")>0");
					sc.append(" and sddscp_zb in (select bean2.depart_id from jc_department bean2 start with  bean2.depart_id="+ departId+" connect by prior bean2.depart_id=bean2.parent_id) and sddscp_isinjob=2");
					sc.append(" or instr(sddscp_historydy,"+departId+")>0");
				}
			
				sd.append(") where rn>").append((pageNo - 1) * pageSize).append(" and rn<=").append(pageNo * pageSize);
				sd.append(" order by rn asc");
			}
			
		}
	List<CmsUser> list = this.getSession().createSQLQuery(sd.toString()).addEntity(CmsUser.class).list();
	Object count = this.getSession().createSQLQuery(sc.toString()).uniqueResult();
	Pagination p = new Pagination();
	p.setPageNo(pageNo);
	p.setPageSize(pageSize);
	p.setList(list);
	p.setTotalCount(count==null?0:Integer.parseInt(count.toString()));
	return p;
	}					 
	public List<CmsUser> getMemberByQXandDepartIdAndYids(Integer departId,String sddscpAssess,String sddscpIsinjob,String sddscpChanges,int pageNo,int pageSize,String ids,String username){
		StringBuffer sa = new StringBuffer();
		StringBuffer sc = new StringBuffer();
		sc.append("select * from jc_user bean where 1=1");
		if(departId!=1){
			sa.append(" and (bean.SDDSCP_JGDW in (select bean2.depart_id from jc_department bean2 start with  bean2.depart_id=");
			sa.append(departId + " connect by prior bean2.depart_id=bean2.parent_id)");
			sa.append(" or bean.SDDSCP_DZZ in (select bean2.depart_id from jc_department bean2 start with  bean2.depart_id=");
			sa.append(departId + " connect by prior bean2.depart_id=bean2.parent_id)");
			sa.append(" or bean.SDDSCP_ZB in (select bean2.depart_id from jc_department bean2 start with  bean2.depart_id=");
			sa.append(departId + " connect by prior bean2.depart_id=bean2.parent_id)");
			sa.append(" or bean.depart_id in (select bean2.depart_id from jc_department bean2 start with  bean2.depart_id=");
			sa.append(departId);
			sa.append(" connect by prior bean2.depart_id=bean2.parent_id)) and bean.SDDSCP_ISPERORORG = 1 ");
		}else{
			sa.append(" and (bean.SDDSCP_JGDW in (select bean2.depart_id from jc_department bean2 start with  bean2.parent_id is null or bean2.parent_id='' ");
			sa.append(" connect by prior bean2.depart_id=bean2.parent_id)");
			sa.append(" or bean.SDDSCP_DZZ in (select bean2.depart_id from jc_department bean2 start with  bean2.parent_id is null or bean2.parent_id='' ");
			sa.append(" connect by prior bean2.depart_id=bean2.parent_id)");
			sa.append(" or bean.SDDSCP_ZB in (select bean2.depart_id from jc_department bean2 start with  bean2.parent_id is null or bean2.parent_id='' ");
			sa.append(" connect by prior bean2.depart_id=bean2.parent_id)");
			sa.append(" or bean.depart_id in (select bean2.depart_id from jc_department bean2 start with  bean2.parent_id is null or bean2.parent_id=''");
			sa.append(" connect by prior bean2.depart_id=bean2.parent_id)) and bean.SDDSCP_ISPERORORG = 1 ");
		}
		if(!StringUtils.isBlank(sddscpIsinjob)){
			sa.append(" and bean.SDDSCP_ISINJOB = ");
			sa.append("'" + sddscpIsinjob + "'");
		}
		if(!StringUtils.isBlank(sddscpChanges)){
			sa.append(" and bean.SDDSCP_CHANGES = '"+sddscpChanges+"'");
		}
		if(!StringUtils.isBlank(username)){
			sa.append(" and bean.username like '%"+username+"%'");
		}
		sa.append(" and bean.sddscp_Isperororg='1'");
		if(!StringUtils.isBlank(ids)){
			sa.append(" and bean.user_id in ("+ids+")");
			sc.append(sa.toString());
		}else{
			sc.append(sa.toString());
			//sd.append(" and rn>").append((pageNo - 1) * pageSize).append(" and rn<=").append(pageNo * pageSize);
		}			
		List<CmsUser> list = this.getSession().createSQLQuery(sc.toString()).addEntity(CmsUser.class).list();
		return list;
	}
	//
	public List<CmsUser> getMemberQXByDepartIdExport(Integer departId,String sddscpAssess,String sddscpIsinjob,String sddscpChanges,int pageNo,int pageSize,String ids){
		StringBuffer sb = new StringBuffer();
		StringBuffer sa = new StringBuffer();
		sb.append("select * from jc_user bean ");
		if(departId!=1){
			sa.append("where 1=1 and (bean.SDDSCP_JGDW in (select bean2.depart_id from jc_department bean2 start with  bean2.depart_id=");
			sa.append(departId + " connect by prior bean2.depart_id=bean2.parent_id)");
			sa.append(" or bean.SDDSCP_DZZ in (select bean2.depart_id from jc_department bean2 start with  bean2.depart_id=");
			sa.append(departId + " connect by prior bean2.depart_id=bean2.parent_id)");
			sa.append(" or bean.SDDSCP_ZB in (select bean2.depart_id from jc_department bean2 start with  bean2.depart_id=");
			sa.append(departId + " connect by prior bean2.depart_id=bean2.parent_id)");
			sa.append(" or bean.depart_id in (select bean2.depart_id from jc_department bean2 start with  bean2.depart_id=");
			sa.append(departId);
			sa.append(" connect by prior bean2.depart_id=bean2.parent_id)) and bean.SDDSCP_ISPERORORG = 1 ");	
		}else{
			sa.append("where 1=1 and (bean.SDDSCP_JGDW in (select bean2.depart_id from jc_department bean2 start with  bean2.parent_id is null or bean2.parent_id='' ");
			sa.append(" connect by prior bean2.depart_id=bean2.parent_id)");
			sa.append(" or bean.SDDSCP_DZZ in (select bean2.depart_id from jc_department bean2 start with  bean2.parent_id is null or bean2.parent_id='' ");
			sa.append(" connect by prior bean2.depart_id=bean2.parent_id)");
			sa.append(" or bean.SDDSCP_ZB in (select bean2.depart_id from jc_department bean2 start with  bean2.parent_id is null or bean2.parent_id='' ");
			sa.append(" connect by prior bean2.depart_id=bean2.parent_id)");
			sa.append(" or bean.depart_id in (select bean2.depart_id from jc_department bean2 start with  bean2.parent_id is null or bean2.parent_id='' ");
			sa.append(" connect by prior bean2.depart_id=bean2.parent_id)) and bean.SDDSCP_ISPERORORG = 1 ");
		}
		
		if(!StringUtils.isBlank(sddscpIsinjob)){
			sa.append(" and bean.SDDSCP_ISINJOB = ");
			sa.append("'" + sddscpIsinjob + "'");
		}
		if(!StringUtils.isBlank(sddscpAssess)){
			sa.append(" and bean.SDDSCP_ASSESS like ");
			sa.append("'" + sddscpAssess + "'");
		}
		if(!StringUtils.isBlank(sddscpChanges)){
			sa.append(" and bean.SDDSCP_CHANGES = '1'");
		}
		if(!StringUtils.isBlank(ids)){
			sa.append(" and bean.user_id in ("+ids+") ");
		}
		sa.append(" and bean.sddscp_Isperororg='1'");
		sa.append(" and rownum>=").append((pageNo - 1) * pageSize).append(" and rownum<=").append(pageNo * pageSize);
		sa.append(" order by bean.USER_ID asc");
		sb.append(sa.toString());
		List<CmsUser> list = this.getSession().createSQLQuery(sb.toString()).addEntity(CmsUser.class).list();
		return list;
	}
	/**
	 * 重写登录方法
	 */
	public CmsUser findByLogincode(String logincode){
		return findUniqueByProperty("sddscpOrglogincode", logincode);
	}
	@Override
	protected Class<CmsUser> getEntityClass() {
		return CmsUser.class;
	}

	@Override
	public List<CmsUser> queryMeberByName(String name) {
		String sql ="select * from jc_user t where username like '"+name+"%'";
		List<CmsUser> list=getSession().createSQLQuery(sql).addEntity(CmsUser.class).list();
		return list;
	}
	 /**
	  * 支部换届
	  * 撤职
	  * orgtype : 工作指导组（不匹配） 机关党委 党总支 支部  
	  */
	 public CmsUser changeSecretaryczByUid(CmsUser user,String orgtype){
		 String param = "";
		 if("机关党委".equals(orgtype)){
			 param = "sddscpJgdwjob";
		 }else if("党组织".equals(orgtype)){
			 param = "sddscpDzzjob";
		 }else if("支部".equals(orgtype)){
			 param = "sddscpZbjob";
		 }else {
			 param = "sddscpJgdwjob";	//如果为空即为机关党委
		 }
		 String hql = "update CmsUser bean set bean."+param+" =:job where bean.id =:oid";	//撤职hql
		 Query query = getSession().createQuery(hql);
		 query.setParameter("job", "");
		 query.setParameter("oid", user.getId());
		 query.executeUpdate();
		 return user;
	 }
	 /**
	  * 支部换届
	  * 任职
	  * orgtype 同上
	  */
	 public CmsUser changeSecretaryrzByUid(CmsUser user,String orgtype){
		 String param = "";
		 if("机关党委".equals(orgtype)){
			 param = "sddscpJgdwjob";
		 }else if("党组织".equals(orgtype)){
			 param = "sddscpDzzjob";
		 }else if("支部".equals(orgtype)){
			 param = "sddscpZbjob";
		 }else {
			 param = "sddscpJgdwjob";	//如果为空即为机关党委
		 }
		 String hql = "update CmsUser bean set bean."+param+" =:job where bean.id =:nid";
		 Query query = getSession().createQuery(hql);
		 query.setParameter("job", "书记");	//先默认写死为书记，后做更改再动态传参匹配
		 query.setParameter("nid", user.getId());
		 query.executeUpdate();
		 return user;
	 }
	 /**
		 * 修改user数据工具
		 */
    public void changeuserdatatool(Integer departid,Integer userid){
    	String hql = "update CmsUser bean set bean.department.id =:departid where bean.id =:userid";
    	Query query = getSession().createQuery(hql);
    	query.setParameter("departid", departid);
    	query.setParameter("userid", userid);
    	query.executeUpdate();
    }
	/**
	 * 修改user数据工具
	 */
    @SuppressWarnings("unchecked")
	 public List<CmsUser> findalllistuser(boolean all){
    	String hql = " from CmsUser bean where bean.sddscpIsperororg ='1'";
		if(!all){
			hql += " and bean.department.id = 1";
		}
		Finder finder = Finder.create(hql);
		return find(finder);
	 }
    /**
	  * 评分列表
	  */
    @SuppressWarnings("unchecked")
	 public List<CmsUser> scoreUserList(Integer departid,String orgtype, Map<String, String> m){
   	 	String orgtypeparam = "sddscpJgdw";
    	if("dzz".equals(orgtype)){
    		orgtypeparam = "sddscpDzz";
    	}else if("zb".equals(orgtype)){
    		orgtypeparam = "sddscpZb";
    	}
    	else{}
    	
    	String apSql = "";
    	if (m.get("queryUsername") != null) {
    		apSql+= (" and bean.username like '%"+m.get("queryUsername").toString()+"%'");
    	}
    	if (m.get("queryIdcard") != null) {
    		apSql+= (" and bean.sddscpIdnumber = '"+m.get("queryIdcard").toString()+"'");
    	}
    	String hql = " from CmsUser bean where bean.sddscpIsperororg ='1' and bean." + orgtypeparam + " =:departid " + apSql +" order by bean.sddscpXfscore desc";
		Finder finder = Finder.create(hql).setParam("departid", departid);
		return find(finder);
	 }

	 /**
	  * 更新分数
	  */
	 public CmsUser updateScore(CmsUser model){
		 Query query = getSession().createQuery(" update CmsUser bean set bean.sddscpXfscore =:sddscpXfscore,bean.sddscpJcf=:sddscpJcf,bean.sddscpFjf=:sddscpFjf,bean.sddscpPfsmfj=:sddscpPfsmfj,bean.sddscpPfsmjc=:sddscpPfsmjc where bean.id =:userid ");
		 query.setParameter("sddscpXfscore", (model.getSddscpJcf()+model.getSddscpFjf()));
		 query.setParameter("sddscpJcf", model.getSddscpJcf());
		 query.setParameter("sddscpFjf", model.getSddscpFjf());
		 query.setParameter("sddscpPfsmfj", model.getSddscpPfsmfj());
		 query.setParameter("sddscpPfsmjc", model.getSddscpPfsmjc());
		 query.setParameter("userid", model.getId());
		 query.executeUpdate();
		 return model;
	 }
	 
	 public Pagination memberList(CmsUser user,int pageNo, int pageSize,boolean isadmin){
		 Finder finder = Finder.create(" select bean from CmsUser bean join bean.userExtSet ext where 1=1 and bean.sddscpIsperororg = 1 ");
		 if(!StringUtils.isBlank(user.getSddscpIsinjob())){
				finder.append(" and bean.sddscpIsinjob =:sddscpIsinjob ");
				finder.setParam("sddscpIsinjob",user.getSddscpIsinjob());
		 }
		 if(!StringUtils.isBlank(user.getSddscpChanges())){
				finder.append(" and bean.sddscpChanges =:sddscpChanges ");
				finder.setParam("sddscpChanges",user.getSddscpChanges());
		 }
		 if(!StringUtils.isBlank(user.getSddscpAssess())){
				finder.append(" and bean.sddscpAssess =:sddscpAssess ");
				finder.setParam("sddscpAssess", user.getSddscpUsertype());
		 }
		 if (!StringUtils.isBlank(user.getUsername())) {
			 finder.append(" and bean.username like :username");
			 finder.setParam("username", "%" + user.getUsername() + "%");
		 }
		 if (!StringUtils.isBlank(user.getSddscpIdnumber())) {
			 finder.append(" and bean.sddscpIdnumber like :sddscpIdnumber");
			 finder.setParam("sddscpIdnumber", "%" + user.getSddscpIdnumber() + "%");
		 }
		 if(!isadmin){
			 if(user.getDepartment()!=null){
				 finder.append(" and (bean.department.id =:departid");
				 finder.append(" or bean.sddscpJgdw =:departid");
				 finder.append(" or bean.sddscpDzz =:departid");
				 finder.append(" or bean.sddscpZb =:departid)");
				 finder.setParam("departid", user.getDepartment().getId());
			 }
		 }
		 finder.append(" and bean.username != 'admin' ");
		 return find(finder, pageNo, pageSize);
	 }
	 
	 public Pagination memberListAndYear(CmsUser user,int pageNo, int pageSize,boolean isadmin,String risendsYear){
		 StringBuffer sb = new StringBuffer();
		 StringBuffer sc = new StringBuffer();
		 StringBuffer sa = new StringBuffer();
		 StringBuffer sd = new StringBuffer();
		 sd.append(" select bean.* from (");
		 sc.append("select count(*) from jc_user bean where 1=1 and bean.sddscp_Isperororg = 1");
		 sb.append(" select rownum rn,bean.* from jc_user bean where bean.sddscp_Isperororg = 1 ");
		 if(!StringUtils.isBlank(user.getSddscpIsinjob())){
			 sa.append(" and bean.sddscp_Isinjob = "+user.getSddscpIsinjob());
		 }
		 if(!StringUtils.isBlank(user.getSddscpChanges())){
			 sa.append(" and bean.sddscp_Changes ="+user.getSddscpChanges());
		 }
		 
		 if (!StringUtils.isBlank(user.getUsername())) {
			 sa.append(" and bean.username like '%"+user.getUsername()+"%'");
		 }
		 if (!StringUtils.isBlank(user.getSddscpIdnumber())) {
			 sa.append(" and bean.sddscp_Idnumber like '%"+user.getSddscpIdnumber()+"%'");
		 }
		 if(!isadmin){
			 if(user.getDepartment()!=null){
				 Integer deptId = user.getDepartment().getId();
				 if(deptId!=1){
					 sa.append(" and (bean.SDDSCP_JGDW in ( select depart_id from jc_department t start with  t.depart_id=" + deptId + " connect by prior t.depart_id=t.parent_id )");
					 sa.append(" or bean.depart_id in  ( select depart_id from jc_department t start with  t.depart_id=" + deptId + " connect by prior t.depart_id=t.parent_id )" );
					 sa.append(" or bean.SDDSCP_DZZ in  ( select depart_id from jc_department t start with  t.depart_id=" + deptId + " connect by prior t.depart_id=t.parent_id )");
					 sa.append(" or bean.SDDSCP_ZB in ( select depart_id from jc_department t start with  t.depart_id=" + deptId + " connect by prior t.depart_id=t.parent_id )");
				 }else{
					 sa.append(" and (bean.SDDSCP_JGDW in ( select depart_id from jc_department t start with t.parent_id is null or t.parent_id=''  connect by prior t.depart_id=t.parent_id )");
					 sa.append(" or bean.depart_id in  ( select depart_id from jc_department t start with  t.parent_id is null or t.parent_id='' connect by prior t.depart_id=t.parent_id )" );
					 sa.append(" or bean.SDDSCP_DZZ in  ( select depart_id from jc_department t start with  t.parent_id is null or t.parent_id='' connect by prior t.depart_id=t.parent_id )");
					 sa.append(" or bean.SDDSCP_ZB in ( select depart_id from jc_department t start with t.parent_id is null or t.parent_id='' connect by prior t.depart_id=t.parent_id )");
				 }
				 sa.append(")");
			 }
		 }
		 sa.append(" and bean.username != 'admin' ");
		 if(!StringUtils.isBlank(user.getSddscpAssess())){
			 
			 if("1".equalsIgnoreCase(user.getSddscpAssess())){
				 sa.append(" and bean.user_Id in (select risends_userid from risen_discussion where risends_score =1");
			 }
			 if("2".equalsIgnoreCase(user.getSddscpAssess())){
				 sa.append(" and bean.user_Id in (select risends_userid from risen_discussion where risends_score =4");
			 }
			 if(!StringUtils.isBlank(risendsYear)){
				 sa.append(" and risends_Year="+risendsYear+")");
			 }
		 }
		 sc.append(sa.toString());
		 sb.append(sa.toString());
		 sd.append(sb.toString()).append(") bean").append(" where 1=1 and rn>").append((pageNo - 1) * pageSize).append(" and rn<=").append(pageNo * pageSize);
		 sd.append(" order by rn asc");
		 List<CmsUser> list = this.getSession().createSQLQuery(sd.toString()).addEntity(CmsUser.class).list();
		 Pagination p = new Pagination();
		 p.setPageNo(pageNo);
		 p.setPageSize(pageSize);
		 p.setList(list);
		 Object count = this.getSession().createSQLQuery(sc.toString()).uniqueResult();
		 p.setTotalCount(count==null?0:Integer.parseInt(count.toString()));
		 return p;
	 }
	 public List<CmsUser> memberListAndids(CmsUser user,int pageNo, int pageSize,boolean isadmin,String ids){
		 StringBuffer sb = new StringBuffer();
		 StringBuffer sc = new StringBuffer();
		 StringBuffer sa = new StringBuffer();
		 StringBuffer sd = new StringBuffer();
		 sd.append(" select bean.* from (");
		 sc.append("select count(*) from jc_user bean where 1=1 and bean.sddscp_Isperororg = 1 ");
		 sb.append(" select rownum rn,bean.* from jc_user bean where bean.sddscp_Isperororg = 1 ");
		 if(!StringUtils.isBlank(user.getSddscpIsinjob())){
			 sa.append(" and bean.sddscp_Isinjob = "+user.getSddscpIsinjob());
		 }
		 if(!StringUtils.isBlank(user.getSddscpChanges())){
			 sa.append(" and bean.sddscp_Changes ="+user.getSddscpChanges());
		 }
		 
		 if (!StringUtils.isBlank(user.getUsername())) {
			 sa.append(" and bean.username like '%"+user.getUsername()+"%'");
		 }
		 if (!StringUtils.isBlank(user.getSddscpIdnumber())) {
			 sa.append(" and bean.sddscp_Idnumber like '%"+user.getSddscpIdnumber()+"%'");
		 }
		 if(!isadmin){
			 if(user.getDepartment()!=null){
				 Integer departId =  user.getDepartment().getId();
				 if(departId!=1){
					 sa.append(" and (bean.SDDSCP_JGDW in (select bean2.depart_id from jc_department bean2 start with  bean2.depart_id=");
						sa.append(departId + " connect by prior bean2.depart_id=bean2.parent_id)");
						sa.append(" or bean.SDDSCP_DZZ in (select bean2.depart_id from jc_department bean2 start with  bean2.depart_id=");
						sa.append(departId + " connect by prior bean2.depart_id=bean2.parent_id)");
						sa.append(" or bean.SDDSCP_ZB in (select bean2.depart_id from jc_department bean2 start with  bean2.depart_id=");
						sa.append(departId + " connect by prior bean2.depart_id=bean2.parent_id)");
						sa.append(" or bean.depart_id in (select bean2.depart_id from jc_department bean2 start with  bean2.depart_id=");
						sa.append(departId);
						sa.append(" connect by prior bean2.depart_id=bean2.parent_id)) and bean.SDDSCP_ISPERORORG = 1 ");
				 }else{
					 sa.append(" and (bean.SDDSCP_JGDW in (select bean2.depart_id from jc_department bean2 start with  bean2.parent_id is null or bean2.parent_id='' ");
						sa.append(" connect by prior bean2.depart_id=bean2.parent_id)");
						sa.append(" or bean.SDDSCP_DZZ in (select bean2.depart_id from jc_department bean2 start with  bean2.parent_id is null or bean2.parent_id='' ");
						sa.append(" connect by prior bean2.depart_id=bean2.parent_id)");
						sa.append(" or bean.SDDSCP_ZB in (select bean2.depart_id from jc_department bean2 start with  bean2.parent_id is null or bean2.parent_id='' ");
						sa.append(" connect by prior bean2.depart_id=bean2.parent_id)");
						sa.append(" or bean.depart_id in (select bean2.depart_id from jc_department bean2 start with  bean2.parent_id is null or bean2.parent_id='' ");
						sa.append(" connect by prior bean2.depart_id=bean2.parent_id)) and bean.SDDSCP_ISPERORORG = 1 ");
				 }
			 }
		 }
		 sa.append(" and bean.username != 'admin' ");
		 if (!StringUtils.isBlank(ids)) {
			 sa.append(" and bean.user_id in ("+ids+")");
			 sc.append(sa.toString());
			 sb.append(sa.toString());
			 sd.append(sb.toString()).append(") bean");
		 }else{
			 sc.append(sa.toString());
			 sb.append(sa.toString());
			 sd.append(sb.toString()).append(") bean").append(" where 1=1 ");
			 sd.append(" order by rn asc");
		 }
		 List<CmsUser> list = this.getSession().createSQLQuery(sd.toString()).addEntity(CmsUser.class).list();
		 return list;
	 }

	 public List<CmsUser> exportMemberList(CmsUser user,int pageNo, int pageSize,boolean isadmin,String ids){
		 Finder finder = Finder.create(" select bean from CmsUser bean join bean.userExtSet ext where 1=1 and bean.sddscpIsperororg = 1 ");
		 if(!StringUtils.isBlank(user.getSddscpIsinjob())){
				finder.append(" and bean.sddscpIsinjob =:sddscpIsinjob ");
				finder.setParam("sddscpIsinjob",user.getSddscpIsinjob());
		 }
		 if(!StringUtils.isBlank(user.getSddscpChanges())){
				finder.append(" and bean.sddscpChanges =:sddscpChanges ");
				finder.setParam("sddscpChanges",user.getSddscpChanges());
		 }
		 if(!StringUtils.isBlank(user.getSddscpAssess())){
				finder.append(" and bean.sddscpAssess =:sddscpAssess ");
				finder.setParam("sddscpAssess", user.getSddscpUsertype());
		 }
		 if (!StringUtils.isBlank(user.getUsername())) {
			 finder.append(" and bean.username like :username");
			 finder.setParam("username", "%" + user.getUsername() + "%");
		 }
		 if (!StringUtils.isBlank(user.getSddscpIdnumber())) {
			 finder.append(" and bean.sddscpIdnumber like :sddscpIdnumber");
			 finder.setParam("sddscpIdnumber", "%" + user.getSddscpIdnumber() + "%");
		 }
		 if (!StringUtils.isBlank(ids)) {
			 finder.append(" and bean.id in ("+ids+")");
		 }
		 if(!isadmin){
			 if(user.getDepartment()!=null){
				 finder.append(" and (bean.department.id =:departid");
				 finder.append(" or bean.sddscpJgdw =:departid");
				 finder.append(" or bean.sddscpDzz =:departid");
				 finder.append(" or bean.sddscpZb =:departid)");
				 finder.setParam("departid", user.getDepartment().getId());
			 }
		 }
		 finder.append(" and bean.username != 'admin' ");
		 Pagination pagination = find(finder, pageNo, pageSize);
		 return (List<CmsUser>)pagination.getList();
	 }

	 public Pagination assess(String orgtype,Integer departid,CmsUser user,int pageNo, int pageSize){
		 String param = "sddscpJgdw";
		 if("dzz".equals(orgtype)){
			 param = "sddscpDzz";
		 }else if("zb".equals(orgtype)){
			 param = "sddscpZb";
		 }else {}
		 Finder finder = Finder.create(" select bean from CmsUser bean where 1=1 and bean.sddscpIsperororg = 1 and bean.sddscpIsinjob = 1 and bean.username != 'admin' ");
		 if(departid != null){
			 finder.append(" and (bean.sddscpJgdw=:param or bean.sddscpDzz=:param1 or bean.sddscpZb=:param2)");
			 finder.setParam("param", departid);
			 finder.setParam("param1", departid);
			 finder.setParam("param2", departid);
		 }
		 if (!StringUtils.isBlank(user.getUsername())) {
			 finder.append(" and bean.username like :username");
			 finder.setParam("username", "%" + user.getUsername() + "%");
		 }
		 if (!StringUtils.isBlank(user.getSddscpIdnumber())) {
			 finder.append(" and bean.sddscpIdnumber like :sddscpIdnumber");
			 finder.setParam("sddscpIdnumber", "%" + user.getSddscpIdnumber() + "%");
		 }
		 if(!StringUtils.isBlank(user.getSddscpAssessyear())){
			 finder.append(" and bean.sddscpAssessyear = :sddscpAssessyear");
			 finder.setParam("sddscpAssessyear", user.getSddscpAssessyear());
		 }
		 return find(finder, pageNo, pageSize);
	 }

	@Override
	public List<CmsUser> memberListexcel(CmsUser user ,String pageNo,String ids,String pageSize) { 
		 Finder finder = Finder.create(" select bean from CmsUser bean join bean.userExtSet ext where 1=1 and bean.sddscpIsperororg = 1 and bean.username!='admin'");
		 if(!StringUtils.isBlank(user.getSddscpUsertype())){
				finder.append(" and bean.sddscpUsertype like :sddscpUsertype ");
				finder.setParam("sddscpUsertype", "%" + user.getSddscpUsertype() + "%");
		 }
		 if(!StringUtils.isBlank(user.getSddscpIsinjob())){
				finder.append(" and bean.sddscpIsinjob =:sddscpIsinjob ");
				finder.setParam("sddscpIsinjob",user.getSddscpIsinjob());
		 }
		 if(!StringUtils.isBlank(user.getSddscpChanges())){
				finder.append(" and bean.sddscpChanges =:sddscpChanges ");
				finder.setParam("sddscpChanges",user.getSddscpChanges());
		 }
		 if(!StringUtils.isBlank(user.getSddscpAssess())){
				finder.append(" and bean.sddscpAssess =:sddscpAssess ");
				finder.setParam("sddscpAssess", user.getSddscpUsertype());
		 }
		 if (!StringUtils.isBlank(user.getUsername())) {
			 finder.append(" and bean.username like :username");
			 finder.setParam("username", "%" + user.getUsername() + "%");
		 }
		 if (!StringUtils.isBlank(user.getSddscpJgdwname())) {
			 finder.append(" and bean.username like :sddscpJgdwname");
			 finder.setParam("sddscpJgdwname", "%" + user.getSddscpJgdwname() + "%");
		 }
		 if(user.getDepartment()!=null){
			 finder.append(" and (bean.department = "+user.getDepartment().getId());
			 finder.append(" or bean.sddscpJgdw = "+user.getDepartment().getId());
			 finder.append(" or bean.sddscpDzz = "+user.getDepartment().getId());
			 finder.append(" or bean.sddscpZb = "+user.getDepartment().getId()+")");
		 }
		 /*
		 if(user.getUsername().equals("admin")){
			 if(user.getDepartment()!=null){
				 finder.append(" and bean.department.id =:departid");
				 finder.setParam("departid", user.getDepartment().getId());
			 }
		 }
		 */
		 if (!StringUtils.isBlank(user.getSddscpIdnumber())) {
			 finder.append(" and bean.sddscpIdnumber like :sddscpIdnumber");
			 finder.setParam("sddscpIdnumber", "%" + user.getSddscpIdnumber() + "%");
		 }
		 if(!StringUtils.isBlank(ids)){
			 finder.append(" and bean.id in ("+ids+")");
			 return find(finder);
		 }else if(!StringUtils.isBlank(pageNo)){
			 Pagination pagination = null;
			 try{
				 pagination= find(finder,Integer.parseInt(pageNo),Integer.parseInt(pageSize));
			 }catch(Exception e){
				 e.printStackTrace();
			 }
			 List<CmsUser> users =  (List<CmsUser>) pagination.getList();
			 return users;
		 }else{
			 return find(finder);
		 }

	}
	 public CmsUser updateAssess(CmsUser user){
		 String param1 = "";
		 String param2 = "";
		 String param3 = "";
		 if(StringUtils.isNotBlank(user.getSddscpExcellentcause()) && !"无".equals(user.getSddscpExcellentcause())){
			 param1 = ",bean.sddscpExcellentcause =:sddscpExcellentcause";
		 }
		 if(StringUtils.isNotBlank(user.getSddscpUnqualifiedcause()) && !"无".equals(user.getSddscpUnqualifiedcause())){
			 param2 = ",bean.sddscpUnqualifiedcause=:sddscpUnqualifiedcause";
		 }
		 if(StringUtils.isNotBlank(user.getSddscpAssessyear())){
			 param2 = ",bean.sddscpAssessyear=:sddscpAssessyear";
		 }
		 Query query = getSession().createQuery(" update CmsUser bean set bean.sddscpAssess =:sddscpAssess"+param1+param2+param3+" where bean.id =:id ");
		 if(StringUtils.isNotBlank(user.getSddscpExcellentcause()) && !"无".equals(user.getSddscpExcellentcause())){
			 query.setParameter("sddscpExcellentcause",user.getSddscpExcellentcause());
		 }
		 if(StringUtils.isNotBlank(user.getSddscpUnqualifiedcause()) && !"无".equals(user.getSddscpUnqualifiedcause())){
			 query.setParameter("sddscpExcellentcause",user.getSddscpUnqualifiedcause());
		 }
		 if(StringUtils.isNotBlank(user.getSddscpAssessyear())){
			 query.setParameter("sddscpAssessyear",user.getSddscpAssessyear());
		 }
		 query.setParameter("sddscpAssess", user.getSddscpAssess());
		 query.setParameter("id", user.getId());
		 query.executeUpdate();
		 return user;
	 }

	 public CmsUser recovery(CmsUser model){
		 String sql = "update jc_user set sddscp_isinjob = 1 where user_id = " + model.getId();
		 Query query = getSession().createSQLQuery(sql);
		 query.executeUpdate();
		 return model;
	 }

	@Override
	public List<CmsUser> getUserByIdNumber(String idNumber) {
		// TODO Auto-generated method stub
		String hql = "from CmsUser bean where bean.sddscpIdnumber=:sddscpIdnumber";
		List<CmsUser> users = getSession().createQuery(hql).setParameter("sddscpIdnumber", idNumber).list();
		return users;
	}

	@Override
	public List<CmsUser> getUserByIdNumberAndUsername(String idNumber,
			String username) {
		// TODO Auto-generated method stub
		if(!StringUtils.isBlank(username) && !StringUtils.isBlank(idNumber)){
			String hql = "from CmsUser bean where bean.sddscpIdnumber=:sddscpIdnumber and bean.username=:username";
			Query query = getSession().createQuery(hql).setParameter("sddscpIdnumber", idNumber);
			query.setParameter("username", username);
			List<CmsUser> users = query.list();
			return users;
		}else{
			return null;
		}
	}

	@Override
	public CmsUser updateUserInfo(CmsUser user) {
		// TODO Auto-generated method stub
		getSession().update(user);
		return user;
	}
	@Override
	public Pagination getInPartyUser(int pageNo, int pageSize, String inType,
			String deptIds) {
		// TODO Auto-generated method stub
		Finder f = Finder.create("select bean from CmsUser bean,CmsFloating floating where 1=1 and bean.id = floating.sddsfiUserid ");
		if(!StringUtils.isBlank(deptIds)){
			f.append(" and bean.department.id in "+deptIds);
		}
		if(!StringUtils.isBlank(inType)){
			if("2".equals(inType)){
				f.append(" and floating.sddsfiInprovince = '1' and bean.id = 31920 ");
			}else if("3".equals(inType)){
				f.append(" and floating.sddsfiIncity = '1' ");
			}else if("4".equals(inType)){
				f.append(" and floating.sddsfiIncounty = '1' ");
			}
		}
		return find(f,pageNo,pageSize);
	}

	@Override
	public Pagination getOutPartyUser(int pageNo, int pageSize, String outType,
			String deptIds) {
		// TODO Auto-generated method stub
		/*
		//先得到转出的用户id
		List<CmsFloating> floats = floatDao.getUserByType(outType);
		String userIds = "";
		for (CmsFloating cmsFloating : floats) {
			userIds = userIds + "'" + cmsFloating.getSddsfiUserid()+"',";
		}
		userIds = StringUtils.stripEnd(userIds, ",");
		*/
		String hql = "select bean from CmsUser bean";
		Finder f = Finder.create(hql);
		if(!StringUtils.isBlank(outType)){
			if("6,7,8".indexOf(outType)>-1){
				f.append(",CmsFloating floating and bean.id = floating.sddsfiUserid");
			}
			if("5".equals(outType)){
				f.append(" where bean.sddscpChanges = '5' ");
			}
			if("9".equals(outType)){
				f.append(" where bean.sddscpChanges = '9' ");
			}
			if("6".equals(outType)){
				f.append(" and floating.sddsfiOutprovince = '1' ");
			}else if("7".equals(outType)){
				f.append(" and floating.sddsfiOutcity = '1' ");
			}else if("8".equals(outType)){
				f.append(" and floating.sddsfiOutcounty = '1' ");
			}
		}
		if(!StringUtils.isBlank(deptIds)){
			f.append(" and bean.department.id in "+deptIds);
		}
		return find(f,pageNo,pageSize);
	}
	@Autowired
	private CmsDepartmentDao departDao;
	@Override
	public CmsUser findByDpId(Integer depid) {
		// TODO Auto-generated method stub
		String sql = "select * from jc_user bean where bean.depart_id= "+ depid +" and bean.sddscp_Isperororg=2";
		return (CmsUser) getSession().createSQLQuery(sql).addEntity(CmsUser.class).uniqueResult();
	}
}