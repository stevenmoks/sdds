package com.risen.service;

import java.util.List;

import com.jeecms.common.page.Pagination;
import com.risen.entity.RisenTargetDetail;

public interface IRisenTargetDetailService {
	public Pagination getPage(int pageNo, int pageSize);
	public Pagination getAllSub(int pageNo, int pageSize,String parentId);
	public RisenTargetDetail save(RisenTargetDetail voteQues);
	public RisenTargetDetail findById(Integer id);
	public RisenTargetDetail showInfo(Integer parentid, Integer deptId);
	public void delete(Integer id);
	public RisenTargetDetail update(RisenTargetDetail bean);
	public Pagination findByOrgId(int pageNo, int pageSize,Integer id);
	public Pagination findByParendId(int pageNo, int pageSize,Integer id);
	public RisenTargetDetail updateBase(RisenTargetDetail bean);
	public Pagination getMyTarget(int pageNo, int pageSize,String deptId);
	public List<RisenTargetDetail> getAllFinishedList(Integer id);
	/*
	 * 批量删除某目标下的所有完成情况
	 */
	public void deleteByTargetId(Integer targetId);
	/*
	 * 获取未完成的目标情况
	 */
	public List<RisenTargetDetail> getAllUnfinishedList(Integer depart,Integer id);
	/**
	 * 获取报表信息
	 * @author slc 2016-11-28 下午11:23:16
	 * @return
	 */
	public List<RisenTargetDetail>getReportData(Integer orgId);
	/**
	 * 修改加分基数
	 * @author slc 2016-12-3 下午9:02:32
	 * @return
	 */
	public void updateBaseScore(Integer orgId,Integer score);
	/**
	 * 批量修改加分基数
	 * @author slc 2016-12-3 下午9:04:48
	 * @param orgId
	 * @param score
	 * @return
	 */
	public void batchUpdBaseScore(Integer[] orgId,Integer score);
}
