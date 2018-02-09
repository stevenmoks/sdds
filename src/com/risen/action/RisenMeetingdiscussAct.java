package com.risen.action;

import static com.jeecms.common.page.SimplePage.cpn;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.risen.entity.RisenMeetingdiscuss;
import com.jeecms.core.entity.CmsSite;
import com.risen.service.RisenMeetingdiscussMng;
import com.jeecms.core.web.util.CmsUtils;
import com.jeecms.core.web.WebErrors;
import com.jeecms.common.page.Pagination;
import com.jeecms.common.web.CookieUtils;

@Controller
public class RisenMeetingdiscussAct {
	private static final Logger log = LoggerFactory.getLogger(RisenMeetingdiscussAct.class);

	@RequiresPermissions("risenMeetingdiscuss:v_list")
	@RequestMapping("/risenMeetingdiscuss/v_list.do")
	public String list(Integer pageNo, HttpServletRequest request, ModelMap model) {
		Pagination pagination = manager.getPage(cpn(pageNo), CookieUtils
				.getPageSize(request));
		model.addAttribute("pagination",pagination);
		model.addAttribute("pageNo",pagination.getPageNo());
		return "risenMeetingdiscuss/list";
	}

	@RequiresPermissions("risenMeetingdiscuss:v_add")
	@RequestMapping("/risenMeetingdiscuss/v_add.do")
	public String add(ModelMap model) {
		return "risenMeetingdiscuss/add";
	}

	@RequiresPermissions("risenMeetingdiscuss:v_edit")
	@RequestMapping("/risenMeetingdiscuss/v_edit.do")
	public String edit(Integer id, Integer pageNo, HttpServletRequest request, ModelMap model) {
		WebErrors errors = validateEdit(id, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		model.addAttribute("risenMeetingdiscuss", manager.findById(id));
		model.addAttribute("pageNo",pageNo);
		return "risenMeetingdiscuss/edit";
	}

	@RequiresPermissions("risenMeetingdiscuss:o_save")
	@RequestMapping("/risenMeetingdiscuss/o_save.do")
	public String save(RisenMeetingdiscuss bean, HttpServletRequest request, ModelMap model) {
		WebErrors errors = validateSave(bean, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		bean = manager.save(bean);
		log.info("save RisenMeetingdiscuss id={}", bean.getId());
		return "redirect:v_list.do";
	}

	@RequiresPermissions("risenMeetingdiscuss:o_update")
	@RequestMapping("/risenMeetingdiscuss/o_update.do")
	public String update(RisenMeetingdiscuss bean, Integer pageNo, HttpServletRequest request,
			ModelMap model) {
		WebErrors errors = validateUpdate(bean.getId(), request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		bean = manager.update(bean);
		log.info("update RisenMeetingdiscuss id={}.", bean.getId());
		return list(pageNo, request, model);
	}

	@RequiresPermissions("risenMeetingdiscuss:o_delete")
	@RequestMapping("/risenMeetingdiscuss/o_delete.do")
	public String delete(Integer[] ids, Integer pageNo, HttpServletRequest request,
			ModelMap model) {
		WebErrors errors = validateDelete(ids, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		RisenMeetingdiscuss[] beans = manager.deleteByIds(ids);
		for (RisenMeetingdiscuss bean : beans) {
			log.info("delete RisenMeetingdiscuss id={}", bean.getId());
		}
		return list(pageNo, request, model);
	}

	private WebErrors validateSave(RisenMeetingdiscuss bean, HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		CmsSite site = CmsUtils.getSite(request);
//		bean.setSite(site);
		return errors;
	}
	
	private WebErrors validateEdit(Integer id, HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		CmsSite site = CmsUtils.getSite(request);
		if (vldExist(id, site.getId(), errors)) {
			return errors;
		}
		return errors;
	}

	private WebErrors validateUpdate(Integer id, HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		CmsSite site = CmsUtils.getSite(request);
		if (vldExist(id, site.getId(), errors)) {
			return errors;
		}
		return errors;
	}

	private WebErrors validateDelete(Integer[] ids, HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		CmsSite site = CmsUtils.getSite(request);
		if (errors.ifEmpty(ids, "ids")) {
			return errors;
		}
		for (Integer id : ids) {
			vldExist(id, site.getId(), errors);
		}
		return errors;
	}

	private boolean vldExist(Integer id, Integer siteId, WebErrors errors) {
		if (errors.ifNull(id, "id")) {
			return true;
		}
		RisenMeetingdiscuss entity = manager.findById(id);
		if(errors.ifNotExist(entity, RisenMeetingdiscuss.class, id)) {
			return true;
		}
//		if (!entity.getSite().getId().equals(siteId)) {
//			errors.notInSite(RisenMeetingdiscuss.class, id);
//			return true;
//		}
		return false;
	}
	
	@Autowired
	private RisenMeetingdiscussMng manager;
}