package com.risen.service.impl;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeecms.cms.cache.DepartmentCache;
import com.jeecms.cms.entity.main.Content;
import com.jeecms.common.page.Pagination;
import com.jeecms.core.dao.CmsDepartmentDao;
import com.jeecms.core.entity.CmsDepartment;
import com.jeecms.core.entity.CmsUser;
import com.jeecms.core.web.util.CmsUtils;
import com.risen.dao.IRisenIntegralDao;
import com.risen.dao.IRisenIntegralRecordDao;
import com.risen.entity.RisenIntegral;
import com.risen.entity.RisenIntegralRecord;
import com.risen.service.IRisenIntegralRecordService;
import com.sun.star.awt.Size;

@Service
@Transactional
public class RisenIntegralRecordServiceImpl implements IRisenIntegralRecordService {
	@Transactional(readOnly = true)
	public Pagination getPage(int pageNo, int pageSize) {
		Pagination page = dao.getPage(pageNo, pageSize);
		return page;
	}
	@Transactional(readOnly = true)
	public RisenIntegralRecord findById(Integer id) {
		RisenIntegralRecord entity = dao.findById(id);
		return entity;
	}

	public RisenIntegralRecord save(RisenIntegralRecord bean) {
		dao.save(bean);
		return bean;
	}
	
	public void deleteByIds(Integer[] ids) {
		
		for (int i = 0,len = ids.length; i < len; i++) {
			 delete(ids[i]);
		}
		
	}
	@Autowired
	private IRisenIntegralRecordDao dao;
	@Autowired
	private CmsDepartmentDao deptDao;
	@Autowired
	private IRisenIntegralDao itdao;
	
	public RisenIntegralRecord update(RisenIntegralRecord bean) {
		bean=dao.update(bean);
		return bean;
	}

	public void delete(Integer id) {
		dao.delete(id);
		
	}

	@Override
	public Pagination getPage(int pageNo, int pageSize, RisenIntegralRecord bean) {
		Pagination page = dao.getPage(pageNo, pageSize,bean);
		return page;
	}
	//已共享内容<查看是否上级已经采纳>
	@Override
	public Pagination getPagehs(int pageNo,HttpServletRequest request, int pageSize, Integer deptId,RisenIntegralRecord bean) {
		Pagination page = dao.getPagehs(pageNo,request, pageSize,deptId, bean);
		return page;
	}

	//得分记录<查看详细已共享内容的分数记录>
	@Override
	public Pagination getPagehsMonth(int pageNo,HttpServletRequest request, int pageSize, Integer deptId,RisenIntegralRecord bean) {
		Pagination page = dao.getPagehsMonth(pageNo,request, pageSize,deptId, bean);
		return page;
	}
	//查看所有分享产生的总数
	@Override
	public List<RisenIntegralRecord> getPageNum(int pageNo,HttpServletRequest request, int pageSize, Integer deptId,RisenIntegralRecord bean) {
		List<RisenIntegralRecord> list = dao.getPageNum(pageNo, request, pageSize, deptId, bean);		
		return list;
	}
	@Override
	public int confirmShare(Integer[] ids,Integer type,Double score) {
		RisenIntegralRecord ir=dao.getByid(ids[0]);
		RisenIntegral it=itdao.findByOrgId(ir.getRisenirOrgid());
		if(type==1){//type=1时是采用默认加分规则
			if(ids[0]!=null && new Double(it.getRisenitBase())!=null){
				ir.setRisenirResult(1);
				ir.setRisenirScore(new Double(it.getRisenitBase()));
				dao.update(ir);
			}
			if(ir.getRisenirOrgid()!=null &&  new Double(it.getRisenitBase())!=null){
				it.setRisenitScore(it.getRisenitScore()==null?0:it.getRisenitScore()+score);
				itdao.update(it);
			}
		}else if(type==2){//type=2的时候是采纳时自定义加分
			if(ids[0]!=null && score!=null){
				ir.setRisenirResult(1);
				ir.setRisenirScore(score);
				dao.update(ir);
			}
			if(ir.getRisenirOrgid()!=null &&  score!=null){
				it.setRisenitScore(it.getRisenitScore()==null?0:it.getRisenitScore()+score);
				itdao.update(it);
			}
		}
		return 1;
	}

	@Override
	public List<RisenIntegralRecord> findByContentId(Integer id) {
		// TODO Auto-generated method stub
		return dao.findByContentId(id);
	}
	@Override
	public Pagination getScoresByDeptIdAndDate(int pageNo,
			HttpServletRequest request, int pageSize, Integer deptId,
			String startDate, String endDate,Integer status) {
		// TODO Auto-generated method stub
		return dao.getScoresByDeptIdAndDate(pageNo,request,pageSize,deptId,startDate,endDate,status);
	}
	
	@Override
	public Pagination getScoresFrontByDeptIdAndDate(int pageNo,
			HttpServletRequest request, int pageSize, Integer deptId,
			String startDate, String endDate,Integer status) {
		// TODO Auto-generated method stub
		return dao.getScoresFrontByDeptIdAndDate(pageNo,request,pageSize,deptId,startDate,endDate,status);
	}
	
	@Override
	public List<RisenIntegralRecord> findByContentIdAndDeptId(Integer departId,
			Integer contentId) {
		// TODO Auto-generated method stub
		return dao.findByContentIdAndDeptId(departId,contentId);
	}
}