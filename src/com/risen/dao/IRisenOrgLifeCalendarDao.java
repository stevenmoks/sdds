package com.risen.dao;

import java.util.List;

import com.jeecms.common.page.Pagination;
import com.risen.entity.RisenChangeremindrecord;
import com.risen.entity.RisenOrgLifeCalendar;

public interface IRisenOrgLifeCalendarDao {
	public Pagination getPage(int pageNo, int pageSize);
	public RisenOrgLifeCalendar save(RisenOrgLifeCalendar voteQues);
	public RisenOrgLifeCalendar findById(Integer id);
	public void delete(Integer id);
	public RisenOrgLifeCalendar update(RisenOrgLifeCalendar bean);
	public Pagination getPage(int pageNo, int pageSize,
			RisenOrgLifeCalendar bean);
	/**
	 * 本日/本月活动 查询所有活动
	 * @author slc 2016-11-19 下午2:36:30
	 * @param bean
	 * @return
	 */
	public List<RisenOrgLifeCalendar> ActivitiesToday(RisenOrgLifeCalendar bean);
	
	
	public   List<RisenOrgLifeCalendar> ChanToday(RisenOrgLifeCalendar bean);
	/**
	 * 本日提醒 查询明日最近一次的活动详情
	 * @author slc 2016-11-19 下午2:39:11
	 * @param bean
	 * @return
	 */
	public RisenOrgLifeCalendar TodayRemind(RisenOrgLifeCalendar bean);
	
	/**
	 * 本日提醒 查询明日最近一次的换届详情
	 * @author slc 2016-11-19 下午2:39:11
	 * @param bean
	 * @return
	 */
	public  RisenOrgLifeCalendar TodayRemindChan(RisenOrgLifeCalendar bean);
	
	/**
	 * 获取有活动的日期
	 * @return
	 */
	public List<String> getEvent(Integer id);
}
