package com.risen.dao;

import java.util.List;

import com.jeecms.common.page.Pagination;
import com.risen.entity.RisenDevelopment;

public interface IRisenDevelopmentDao {
	/**
	 * 保存
	 * @param baseModel
	 * @return
	 */
	public RisenDevelopment save(RisenDevelopment baseModel);
	
	/**
	 * 流程展示列表
	 */
	public List<RisenDevelopment> risenDevelopmentList(RisenDevelopment baseModel);
	
	/**
	 * 删除
	 */
	public RisenDevelopment deleteData(String uuid);
	
	/**
	 * 查询单个对象
	 */
	public RisenDevelopment getModel(String uuid);
	
	
}
