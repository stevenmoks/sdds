package com.jeecms.cms.action.front;

import static com.jeecms.common.page.SimplePage.cpn;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.jeecms.cms.manager.assist.CmsBallotMng;
import com.jeecms.common.page.Pagination;
import com.jeecms.common.web.CookieUtils;
import com.jeecms.core.entity.CmsDepartment;
import com.jeecms.core.entity.CmsSite;
import com.jeecms.core.manager.CmsDepartmentMng;
import com.jeecms.core.manager.CmsSiteMng;
import com.jeecms.core.manager.CmsUserMng;
import com.jeecms.core.web.util.CmsUtils;
import com.jeecms.core.web.util.FrontUtils;

@Controller
public class BallotAct {
	private static final Logger log = LoggerFactory.getLogger(BallotAct.class);
	public static final String DIR = "ballot";
	public static final String BALLOT_LIST = "tpl.ballotList";
	@Autowired
	private CmsDepartmentMng dpmanager;
	@Autowired
	private CmsUserMng umanager;
	@Autowired
	private CmsSiteMng siteMng;
	
	@RequestMapping(value = "/ballot/list.jspx", method = RequestMethod.GET)
	public String list(Integer pageNo, Integer ctgId,
			HttpServletRequest request, HttpServletResponse response,
			ModelMap model){
		//CmsSite site = CmsUtils.getSite(request);
		CmsSite site= null;
		String departId = request.getParameter("departId");
		if("1".equals(departId)){
			site = siteMng.findById(1);
		}else{
			CmsDepartment  depar = dpmanager.findByPid(new Integer(departId));
			List<CmsSite> list = new ArrayList<CmsSite>(umanager.findByDpId(depar.getId()).getSites());
			site = list.get(0);
		}
		FrontUtils.frontData(request, model, site);
		Pagination pagination = manager.getPages(new Integer(departId),cpn(pageNo), CookieUtils.getPageSize(request));
		model.addAttribute("pagination", pagination);
		model.addAttribute("userId", departId);
		return FrontUtils.getTplPath(request, site.getSolutionPath(),DIR, BALLOT_LIST);
	}
	@Autowired
	private CmsBallotMng manager;
}
