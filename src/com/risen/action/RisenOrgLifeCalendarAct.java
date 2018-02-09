package com.risen.action;

import static com.jeecms.common.page.SimplePage.cpn;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jeecms.common.page.Pagination;
import com.jeecms.common.web.CookieUtils;
import com.jeecms.core.entity.CmsSite;
import com.jeecms.core.entity.CmsUser;
import com.jeecms.core.manager.CmsDepartmentMng;
import com.jeecms.core.web.WebErrors;
import com.jeecms.core.web.util.CmsUtils;
import com.risen.entity.RisenOrgLifeCalendar;
import com.risen.service.IRisenOrgLifeCalendarService;

@Controller
public class RisenOrgLifeCalendarAct {
	private static final Logger log = LoggerFactory.getLogger(RisenOrgLifeCalendarAct.class);

	@RequiresPermissions("risenOrgLifeCalendar:v_list")
	@RequestMapping("/risenOrgLifeCalendar/v_list.do")
	public String list(String type,Integer pageNo, HttpServletRequest request, ModelMap model ) {
		CmsUser user = CmsUtils.getUser(request);
		RisenOrgLifeCalendar bean = new RisenOrgLifeCalendar();
		bean.setRisenlcHolderid(user.getId());//举办人编号
		bean.setRisenlcHoldername(user.getUsername());//举办人姓名
		bean.setRisenlcOrgid(user.getDepartment().getId());//所属组织编号
		bean.setRisenlcOrgname(user.getDepartment().getName());//所属组织名称
		bean.setRisenlcMeetingtype(type);
		String risenlcOdate = request.getParameter("risenlcOdate");
		SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if(!StringUtils.isEmpty(risenlcOdate)){//如果活动时间不为空则为条件查询 设置起始日期和结束日期
			try {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				bean.setRisenlcOdate(sdf.parse(risenlcOdate));
				bean.setStartDate(format.parse(risenlcOdate+" 00:00:00"));
				bean.setEndDate(format.parse(risenlcOdate+" 23:59:59"));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		/*if(!StringUtils.isEmpty(bean.getRisenlcReminddate())){//如果提醒时间不为空则为条件查询 设置起始日期和结束日期
			try {
				bean.setStartDate1(format.parse(bean.getRisenlcReminddate()+" 00:00:00"));
				bean.setEndDate1(format.parse(bean.getRisenlcReminddate()+" 23:59:59"));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}*/
		Pagination pagination = manager.getPage(cpn(pageNo), CookieUtils
				.getPageSize(request),bean);
		model.addAttribute("pagination",pagination);
		model.addAttribute("pageNo",pagination.getPageNo());
		model.addAttribute("type",type);
		return "orglifecalendar/list";
	}

	@RequiresPermissions("risenOrgLifeCalendar:v_add")
	@RequestMapping("/risenOrgLifeCalendar/v_add.do")
	public String add(String type,ModelMap model,HttpServletRequest request) {
		model.addAttribute("type",type);
		return "orglifecalendar/add1";
	}

	@RequiresPermissions("risenOrgLifeCalendar:v_edit")
	@RequestMapping("/risenOrgLifeCalendar/v_edit.do")
	public String edit(String type,Integer id, Integer pageNo, HttpServletRequest request, ModelMap model) {
		WebErrors errors = validateEdit(id, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		model.addAttribute("risenOrgLifeCalendar", manager.findById(id));
		model.addAttribute("type",type);
		model.addAttribute("pageNo",pageNo);
		return "orglifecalendar/edit";
	}

	@RequiresPermissions("risenOrgLifeCalendar:o_save")
	@RequestMapping("/risenOrgLifeCalendar/o_save.do")
	public String save(RisenOrgLifeCalendar bean,Integer pageNo, HttpServletRequest request, ModelMap model) {
		WebErrors errors = validateSave(bean, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		CmsUser user = CmsUtils.getUser(request);
		bean.setRisenlcHolderid(user.getId());//举办人编号
		bean.setRisenlcOrgid(user.getDepartment().getId());//所属组织编号
		bean.setRisenlcOrgname(user.getDepartment().getName());//所属组织名称
		bean = manager.save(bean);
		log.info("save risenOrgLifeCalendar id={}", bean.getRisenlcUuid());
		return list(bean.getRisenlcMeetingtype(), pageNo, request, model);
	}

	@RequiresPermissions("risenOrgLifeCalendar:o_update")
	@RequestMapping("/risenOrgLifeCalendar/o_update.do")
	public String update(String risenlcMeetingtype,RisenOrgLifeCalendar bean, Integer pageNo, HttpServletRequest request,
			ModelMap model) {
		WebErrors errors = validateUpdate(bean.getRisenlcUuid(), request);
		String type = request.getParameter("risenlcMeetingtype");
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		bean = manager.update(bean);
		log.info("update risenOrgLifeCalendar id={}.", bean.getRisenlcUuid());
		return list(type,pageNo, request, model);
	}

	@RequiresPermissions("risenOrgLifeCalendar:o_delete")
	@RequestMapping("/risenOrgLifeCalendar/o_delete.do")
	public String delete(String type,Integer id, Integer pageNo, HttpServletRequest request,
			ModelMap model,RisenOrgLifeCalendar bean) {
			 manager.delete(id);
			log.info("delete risenOrgLifeCalendar id={}",id);
			try {
				list(type, pageNo, request, model);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		return list(type, pageNo, request, model);
	}
	
	

	private WebErrors validateSave(RisenOrgLifeCalendar bean, HttpServletRequest request) {
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

	

	private boolean vldExist(Integer id, Integer siteId, WebErrors errors) {
		if (errors.ifNull(id, "id")) {
			return true;
		}
		RisenOrgLifeCalendar entity = manager.findById(id);
		if(errors.ifNotExist(entity, RisenOrgLifeCalendar.class, id)) {
			return true;
		}
//		if (!entity.getSite().getId().equals(siteId)) {
//			errors.notInSite(risenOrgLifeCalendar.class, id);
//			return true;
//		}
		return false;
	}
	
	
	@Autowired
	private IRisenOrgLifeCalendarService manager;
	@Autowired
	private CmsDepartmentMng departManager;
}