package com.jeecms.cms.statistic;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.jeecms.cms.statistic.CmsStatistic.TimeRange;
import com.jeecms.core.entity.CmsDepartment;

public interface CmsStatisticDao {
	public long memberStatistic(TimeRange timeRange);
	
	public long memberStatistic(TimeRange timeRange, String statisticType,Map<String, Object> restrictions,CmsDepartment dept);
	
	public List<Object[]> statisticMemberByTarget(Integer target, Date timeBegin,Date timeEnd);
	
	public List<Object[]> statisticMemberByTarget(Integer target, String statisticType, Date timeBegin,Date timeEnd,Map<String, Object> restrictions);

	public long contentStatistic(TimeRange timeRange,
			Map<String, Object> restrictions);
	
	public List<Object[]> statisticContentByTarget(Integer target,
			Date timeBegin,Date timeEnd,Map<String, Object> restrictions);
	
	public List<Object[]> statisticCommentByTarget(
			Integer target,Integer siteId,Boolean isReplyed,Date timeBegin,Date timeEnd);


	public List<Object[]> statisticGuestbookByTarget(Integer target,Integer siteId,
			 Boolean isReplyed,Date timeBegin,Date timeEnd);

	public long commentStatistic(TimeRange timeRange,
			Map<String, Object> restrictions);

	public long guestbookStatistic(TimeRange timeRange,
			Map<String, Object> restrictions);
	
	/**
	 * 党员基本情况分析_报表分析
	 * @param statisticType
	 * @param departId
	 * @param sddscpUsertype
	 * @return
	 */
	public List<Object[]> basicInfoList(String statisticType,Integer departId,String sddscpUsertype);
	
	/**
	 * 党员基本情况分析_各类型总数
	 * @param statisticType
	 * @param departId
	 * @param sddscpUsertype
	 * @return
	 */
	public List<Object[]> basicInfoListCount(String statisticType,Integer departId,String sddscpUsertype);
	
	/**
	 * 党员入党时间情况分析_报表分析
	 * @param statisticType
	 * @param departId
	 * @param sddscpUsertype
	 * @return
	 */
	public List<Object[]> partyTimeList(String statisticType,Integer departId,String sddscpUsertype);
	
	/**
	 * 党员入党时间情况分析_各类型总数
	 * @param statisticType
	 * @param departId
	 * @param sddscpUsertype
	 * @return
	 */
	public List<Object[]> partyTimeListCount(String statisticType,Integer departId,String sddscpUsertype);
	
	/**
	 * 党员学历情况分析_报表分析
	 * @param statisticType
	 * @param departId
	 * @param sddscpUsertype
	 * @return
	 */
	public List<Object[]> educationAnalysisList(String statisticType,Integer departId,String sddscpUsertype);
	
	/**
	 * 党员学历情况分析_各类型总数
	 * @param statisticType
	 * @param departId
	 * @param sddscpUsertype
	 * @return
	 */
	public List<Object[]> educationAnalysisListCount(String statisticType,Integer departId,String sddscpUsertype);
	
	/**
	 * 党员职务分析_报表分析
	 * @param statisticType
	 * @param departId
	 * @param sddscpUsertype
	 * @return
	 */
	public List<Object[]> dutiesList(String statisticType,Integer departId);
	/**
	 * 党员变化情况分析 -- 本年内党员减少情况  -- 出党/停止党籍
	 * 
	 */
	public Integer getOutOrStopPartyUserNum(Integer departId,String decreaseType);
	/*
	 * 党员年度变化情况分析    统计近五年的党员总人数  
	 */
	public List<Object[]> getAllMembersNotLeave();
	
}
