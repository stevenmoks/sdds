package com.jeecms.cms.action.admin.main;

import static com.jeecms.common.page.SimplePage.cpn;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Colour;
import jxl.format.PageOrientation;
import jxl.format.PaperSize;
import jxl.format.UnderlineStyle;
import jxl.format.VerticalAlignment;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.jeecms.cms.entity.assist.CmsWebservice;
import com.jeecms.cms.manager.assist.CmsFileMng;
import com.jeecms.cms.manager.assist.CmsWebserviceMng;
import com.jeecms.common.page.Pagination;
import com.jeecms.common.upload.FileRepository;
import com.jeecms.common.web.CookieUtils;
import com.jeecms.common.web.RequestUtils;
import com.jeecms.common.web.ResponseUtils;
import com.jeecms.core.dao.CmsUserDao;
import com.jeecms.core.entity.CmsConfigItem;
import com.jeecms.core.entity.CmsDepartment;
import com.jeecms.core.entity.CmsGroup;
import com.jeecms.core.entity.CmsSite;
import com.jeecms.core.entity.CmsUser;
import com.jeecms.core.entity.CmsUserExt;
import com.jeecms.core.entity.Ftp;
import com.jeecms.core.entity.NewCmsUser;
import com.jeecms.core.manager.CmsConfigItemMng;
import com.jeecms.core.manager.CmsDepartmentMng;
import com.jeecms.core.manager.CmsGroupMng;
import com.jeecms.core.manager.CmsLogMng;
import com.jeecms.core.manager.CmsUserMng;
import com.jeecms.core.manager.DbFileMng;
import com.jeecms.core.web.WebErrors;
import com.jeecms.core.web.util.CmsUtils;
import com.risen.entity.CoreDictionary;
import com.risen.service.CmsFloatingMng;
import com.risen.service.ICoreDictionaryService;
import com.utility.excel.ExcelHelper;
import com.utility.excel.JxlExcelHelper;

@Controller
public class CmsMemberAct {
	private static final Logger log = LoggerFactory
			.getLogger(CmsMemberAct.class);

	@RequiresPermissions("member:v_list")
	@RequestMapping("/member/v_list.do")
	public String list(String queryUsername, String sddscpIdnumber,
			Integer queryGroupId,Boolean queryDisabled, Integer pageNo,String sddscpUsertype,
			Integer departmentId,
			HttpServletRequest request, ModelMap model) {
		Pagination pagination = manager.getPage(queryUsername, sddscpIdnumber,
				null, queryGroupId, queryDisabled, false, null, null,
				null,null,null,null,cpn(pageNo),
				CookieUtils.getPageSize(request),sddscpUsertype,"0"); //sddscpUsertype 后面的  "0" 这个值（isadminlist）是判断是否是管理员list，0代表否,即 从党员管理模块进入
		model.addAttribute("pagination", pagination);

		model.addAttribute("queryUsername", queryUsername);
		model.addAttribute("sddscpIdnumber", sddscpIdnumber);
		model.addAttribute("queryGroupId", queryGroupId);
		model.addAttribute("queryDisabled", queryDisabled);
		model.addAttribute("groupList", cmsGroupMng.getList());
		model.addAttribute("sddscpUsertype", sddscpUsertype);
		model.addAttribute("departmentId", departmentId);
		return "member/list";
	}

	@RequiresPermissions("member:v_add")
	@RequestMapping("/member/v_add.do")
	public String add(ModelMap model,HttpServletRequest request) {
		CmsUser user = CmsUtils.getUser(request);
		CmsSite site=CmsUtils.getSite(request);
		List<CmsGroup> groupList = cmsGroupMng.getList();
		List<CmsConfigItem>registerItems=cmsConfigItemMng.getList(site.getConfig().getId(), CmsConfigItem.CATEGORY_REGISTER);
		//获取字典 begin
		List<CmsUser> partypositionList = manager.getCorecdKeyList("党内职务");
		List<CmsUser> outpartyList = manager.getCorecdKeyList("出党");
		List<CmsUser>  jobstatusList = manager.getCorecdKeyList("工作身份");
		List<CoreDictionary>jiguan=coreDictionaryService.getJobDictByOrgType(1);
		List<CoreDictionary>dangzongzhi=coreDictionaryService.getJobDictByOrgType(2);
		List<CoreDictionary>zhibu=coreDictionaryService.getJobDictByOrgType(3);
		//获取字典 end
		//获取机关党委
		try {
		CmsUser loginUser=CmsUtils.getUser(request);	
		
		List<CmsDepartment>jiguanOrg=cmsDeptMng.getDeptByTypeAndLoginId("机关党委",loginUser.getDepartment().getId());
		//获取党总支
		List<CmsDepartment>zongOrg=cmsDeptMng.getDeptByTypeAndLoginId("党总支",loginUser.getDepartment().getId());
		//获取支部
		List<CmsDepartment>zhibuOrg=cmsDeptMng.getDeptByTypeAndLoginId("支部",loginUser.getDepartment().getId());
		String sddscpIsinjob = request.getParameter("sddscpIsinjob");
		String sddscpAll = request.getParameter("sddscpAll");
		model.addAttribute("dept", user.getDepartment());
		model.addAttribute("sddscpIsinjob", sddscpIsinjob);
		model.addAttribute("sddscpAll", sddscpAll);
		model.addAttribute("registerItems", registerItems);
		model.addAttribute("groupList", groupList);
		model.addAttribute("partypositionList", partypositionList);
		model.addAttribute("outpartyList", outpartyList);
		model.addAttribute("jobstatusList", jobstatusList);
		model.addAttribute("jiguan", jiguan);
		model.addAttribute("dangzongzhi", dangzongzhi);
		model.addAttribute("zhibu", zhibu);
		model.addAttribute("jiguanOrg", jiguanOrg);
		model.addAttribute("zongOrg", zongOrg);
		model.addAttribute("zhibuOrg", zhibuOrg);
		model.addAttribute("userName",user.getUsername());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "member/add";
	}

	@RequiresPermissions("member:v_edit")
	@RequestMapping("/member/v_edit.do")
	public String edit(Integer id, Integer queryGroupId, Boolean queryDisabled,
			HttpServletRequest request, ModelMap model,String ischeck,Integer zjstatus) {
		String queryUsername = RequestUtils.getQueryParam(request,
				"queryUsername");
		String queryEmail = RequestUtils.getQueryParam(request, "queryEmail");
		CmsSite site=CmsUtils.getSite(request);
		CmsUser userdept = CmsUtils.getUser(request);
		WebErrors errors = validateEdit(id, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		} 
		CmsUser user=manager.findById(id);
		CmsDepartment  talgetdepart=null;

		if(zjstatus!=null){
			talgetdepart=cmsDeptMng.findById(Integer.parseInt(user.getSddscpNewaddyd()));
			model.addAttribute("zjstatus", zjstatus);
			model.addAttribute("targetDeptName", talgetdepart.getName());
			model.addAttribute("outPartyCause",user.getSddscpOutpartycause());
		}
		List<CmsGroup> groupList = cmsGroupMng.getList();
		List<CmsConfigItem>registerItems=cmsConfigItemMng.getList(site.getConfig().getId(), CmsConfigItem.CATEGORY_REGISTER);
		List<String>userAttrValues=new ArrayList<String>();
		for(CmsConfigItem item:registerItems){
			userAttrValues.add(user.getAttr().get(item.getField()));
		}
		//获取字典 begin
		List<CmsUser> partypositionList = manager.getCorecdKeyList("党内职务");
		List<CmsUser> outpartyList = manager.getCorecdKeyList("出党");
		List<CmsUser>  jobstatusList = manager.getCorecdKeyList("工作身份");
		List<CoreDictionary>jiguan=coreDictionaryService.getJobDictByOrgType(1);
		List<CoreDictionary>dangzongzhi=coreDictionaryService.getJobDictByOrgType(2);
		List<CoreDictionary>zhibu=coreDictionaryService.getJobDictByOrgType(3);
		//获取当前登录USER
		CmsUser loginUser=CmsUtils.getUser(request);
		List<CmsDepartment>jiguanOrg=cmsDeptMng.getDeptByTypeAndLoginId("机关党委",loginUser.getDepartment().getId());
		//获取党总支
		List<CmsDepartment>zongOrg=cmsDeptMng.getDeptByTypeAndLoginId("党总支",loginUser.getDepartment().getId());
		//获取支部
		List<CmsDepartment>zhibuOrg=cmsDeptMng.getDeptByTypeAndLoginId("支部",loginUser.getDepartment().getId());
		String sddscpIsinjob = request.getParameter("sddscpIsinjob");
		String sddscpAssess = request.getParameter("sddscpAssess");
		String sddscpChanges = request.getParameter("sddscpChanges");
		String sddscpAll = request.getParameter("sddscpAll");
		//获取字典 end
		model.addAttribute("dept", userdept.getDepartment());
		model.addAttribute("queryUsername", queryUsername);
		model.addAttribute("queryEmail", queryEmail);
		model.addAttribute("queryGroupId", queryGroupId);
		model.addAttribute("queryDisabled", queryDisabled);
		model.addAttribute("groupList", groupList);
		model.addAttribute("cmsMember", user);
		model.addAttribute("groupList", groupList);

		model.addAttribute("sddscpIsinjob", sddscpIsinjob);
		model.addAttribute("sddscpAssess", sddscpAssess);
		model.addAttribute("sddscpChanges", sddscpChanges);
		model.addAttribute("sddscpAll", sddscpAll);
		model.addAttribute("registerItems", registerItems);
		model.addAttribute("userAttrValues", userAttrValues);
		model.addAttribute("partypositionList", partypositionList);
		model.addAttribute("outpartyList", outpartyList);
		model.addAttribute("jobstatusList", jobstatusList);
		model.addAttribute("jiguan", jiguan);
		model.addAttribute("dangzongzhi", dangzongzhi);
		model.addAttribute("zhibu", zhibu);
		model.addAttribute("jiguanOrg", jiguanOrg);
		model.addAttribute("zongOrg", zongOrg);
		model.addAttribute("zhibuOrg", zhibuOrg);
		model.addAttribute("userName",userdept.getUsername());
		model.addAttribute("userType",user.getSddscpChangestype());
		model.addAttribute("ischeck", ischeck);
		return "member/edit";
	}

	@RequiresPermissions("member:o_save")
	@RequestMapping("/member/o_save.do")
	public String save(CmsUser bean, CmsUserExt ext, String username,
			String email, String password, Integer groupId,Integer grain,
			String sddscpIdnumber,String sddscpNational,String sddscpAddress,String sddscpPoliticaltype,
			String sddscpPartyposition,String sddscpBasescore,Integer sddscpXfscore,String sddscpKfscore,
			String sddscpSumscore,String sddscpOrgname,Date sddscpJoinpartydate,Date sddscpEbranchdate,
			Date sddscpJoinworktime,String sddscpEducation,String sddscpGraduate,String sddscpDegree,
			String sddscpMatrimonial,String sddscpWorkplace,String sddscpJobposition,String sddscpNative,
			String sddscpResidence,String sddscpOtherphone,String sddscpUsertype,Integer departmentId,String departName,
			String sddscpExcellentcause,String sddscpUnqualifiedcause,String sddscpIsinjob,String sddscpOutpartytype,String sddscpOutpartycause,
			String sddscpJobstatus,
			Integer sddscpJgdw,Integer sddscpDzz,Integer sddscpZb,String sddscpJgdwjob,String sddscpDzzjob,String sddscpZbjob,String sddscpAssess,
			String sddscpChanges,String sddscpChangestype,String sddscpJgdwname,String sddscpDzzname,String sddscpZbname,
			HttpServletRequest request, ModelMap model) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		WebErrors errors = validateSave(bean, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		String ip = RequestUtils.getIpAddr(request);
		Map<String,String>attrs=RequestUtils.getRequestMap(request, "attr_");
		CmsUser loginUser = CmsUtils.getUser(request);
		CmsDepartment depart = loginUser.getDepartment();
		String jobType = "1";
		if(sddscpJobstatus.indexOf("离")>-1){
			jobType = "0";
		}
		String deptId = request.getParameter("deptsIds");
		Integer deptIds  = null;
		if(!StringUtils.isBlank(deptId)){
			deptIds = new Integer(deptId);
		}
		if (departmentId != null) {
			deptIds = departmentId;
		}
		
		bean = manager.registerMember(username, email, password, ip, groupId,
				grain, sddscpIdnumber, sddscpNational, sddscpAddress,
				sddscpPoliticaltype, sddscpPartyposition, sddscpBasescore,
				sddscpXfscore, sddscpKfscore, sddscpSumscore, sddscpOrgname,
				sddscpJoinpartydate, sddscpEbranchdate, sddscpJoinworktime,
				sddscpEducation, sddscpGraduate, sddscpDegree,
				sddscpMatrimonial, sddscpWorkplace, sddscpJobposition,
				sddscpNative, sddscpResidence, sddscpOtherphone,
				sddscpUsertype, deptIds, departName, sddscpExcellentcause,
				sddscpUnqualifiedcause, jobType, sddscpOutpartytype,
				sddscpOutpartycause, sddscpJobstatus, "1", null, null,
				sddscpJgdw, sddscpDzz, sddscpZb, sddscpJgdwjob, sddscpDzzjob,
				sddscpZbjob, sddscpAssess, sddscpChanges, sddscpChangestype,
				sddscpJgdwname, sddscpDzzname, sddscpZbname, false, ext, attrs,
				true,null);
		if(!StringUtils.isBlank(sddscpChanges)){
			CmsDepartment olddepart = null;
			if(("2".indexOf(sddscpChanges)>-1)){//changes=3的不加真加数据记录
				if(bean.getSddscpZb()!=null){//这里就暂时不做市局机关支部有职位 ，区县级机关党委也有职位的判断了
					CmsDepartment newdepart = cmsDeptMng.findById(bean.getSddscpZb());
					floatMng.outAndaddWithUser(bean,false,olddepart,newdepart,sddscpChanges,null);
				}
				if(bean.getSddscpZb()==null&&bean.getSddscpDzz()!=null){
					CmsDepartment newdepart = cmsDeptMng.findById(bean.getSddscpDzz());
					floatMng.outAndaddWithUser(bean,false,olddepart,newdepart,sddscpChanges,null);
				}
				if(bean.getSddscpZb()==null&&bean.getSddscpDzz()==null&&bean.getSddscpJgdw()!=null){
					CmsDepartment newdepart = cmsDeptMng.findById(bean.getSddscpJgdw());
					floatMng.outAndaddWithUser(bean,false,olddepart,newdepart,sddscpChanges,null);
				}
			}
		}
		/*
		 * bean = manager.registerMember(username, email, password, ip,
		 * groupId,grain
		 * ,sddscpIdnumber,sddscpNational,sddscpAddress,sddscpPoliticaltype
		 * ,sddscpPartyposition,sddscpBasescore,
		 * sddscpXfscore,sddscpKfscore,sddscpSumscore
		 * ,sddscpOrgname,sddscpJoinpartydate
		 * ,sddscpEbranchdate,sddscpJoinworktime
		 * ,sddscpEducation,sddscpGraduate,sddscpDegree,sddscpMatrimonial,
		 * sddscpWorkplace
		 * ,sddscpJobposition,sddscpNative,sddscpResidence,sddscpOtherphone
		 * ,sddscpUsertype
		 * ,departmentId,departName,sddscpExcellentcause,sddscpUnqualifiedcause,
		 * sddscpIsinjob,sddscpOutpartytype,sddscpOutpartycause,sddscpJobstatus,
		 * "1", null, null, sddscpJgdw, sddscpDzz, sddscpZb, sddscpJgdwjob,
		 * sddscpDzzjob, sddscpZbjob, sddscpAssess,false,ext,attrs);
		 */
		cmsWebserviceMng.callWebService("false", username, password, email,
				ext, CmsWebservice.SERVICE_TYPE_ADD_USER);
		log.info("save CmsMember id={}", bean.getId());
		cmsLogMng.operating(request, "cmsMember.log.save", "id=" + bean.getId()
				+ ";username=" + bean.getUsername());
		String isAll = request.getParameter("sddscpAll");
		String sddscpIsinjob1 = request.getParameter("sddscpIsinjob1");
		if (!StringUtils.isBlank(isAll)) {
			return "redirect:new_v_list.do?sddscpAll=1";
		} else if (!StringUtils.isBlank(sddscpIsinjob1)) {
			return "redirect:new_v_list.do?sddscpIsinjob="+sddscpIsinjob1;
		}else{
			return "redirect:new_v_list.do?sddscpAll=1";
		}
	}
	
	/*
	 *  点击党员列表中的转出按钮时，
	 */
	@RequiresPermissions("member:o_zhuanchu")
	@RequestMapping("/member/o_zhuanchu.do")
	public String zhuanchu(HttpServletRequest request,
			ModelMap mode,Integer userid,Integer zjstatus,Integer targetDeptId,String SDDSCPTHSM,String outReason,Integer targetLifeOrg){
		if(userid==null){
			userid= (Integer) request.getAttribute("userid");
		}
		if(targetDeptId==null){
			targetDeptId=(Integer) request.getAttribute("targetDeptId");
		}
		if(zjstatus==null){
			zjstatus=(Integer) request.getAttribute("zjstatus");
		}
		//Integer targetLifeOrg=(Integer) request.getAttribute("targetLifeOrg");
		String outReson2 = request.getParameter("outReason");
		
		CmsUser bean = userDao.findById(userid);
		String sddscpChanges= null;
		CmsDepartment newdepart=null;
		CmsDepartment olddepart=null;
		StringBuffer buffer = new StringBuffer();
		
		// 当这个转接状态不等于4或者5的时候，比说点击接收按钮时该值是3
		if(zjstatus!=4 && zjstatus!=5 ){
			// 判断该名党员是否是第一次转出
			if(bean.getSddscpNewaddyd()!=null){
				newdepart = cmsDeptMng.findById(Integer.parseInt(bean.getSddscpNewaddyd()));
			}
			if( bean.getSddscpZb()!=null ){// 在转出之前该名党员所在支部的 id
				olddepart = cmsDeptMng.findById(bean.getSddscpZb());
			}
			buffer.append(","+olddepart.getParent().getId());// 获取该支部的上一级组织的ID
			//党员转出
			if(bean.getSddscpNewaddyd()!=null){
				if(newdepart!=null){
					if(olddepart!=null){
						if(newdepart.getSddspoOrgtype().equals("支部")){
							if(!(newdepart.getParent().getId().intValue()==olddepart.getParent().getId().intValue())){
								floatMng.outAndaddWithUser(bean, false,olddepart,newdepart,sddscpChanges,null);
								if(newdepart.getSddspoRelations().equals(olddepart.getSddspoRelations())){//跨区县
									bean.setSddscpHistorydy(bean.getSddscpHistorydy()+","+olddepart.getParent().getId().toString());
								}else{
									if(olddepart.getSddspoOrglevel().intValue()==2){//基层支部转出本市
										bean.setSddscpHistorydy(bean.getSddscpHistorydy()+","+olddepart.getParent().getParent().getId().toString()+","+olddepart.getParent().getId().toString());
									}else if(olddepart.getSddspoOrglevel().intValue()==1){//区县级支部转出本市
										bean.setSddscpHistorydy(bean.getSddscpHistorydy()+","+olddepart.getParent().getParent().getId().toString()+","+olddepart.getParent().getId().toString());
									}else{//市级支部转出本市
										bean.setSddscpHistorydy(bean.getSddscpHistorydy()+","+olddepart.getParent().getId().toString());
									}
								}
							}
							bean.setSddscpZb(newdepart.getId());
							bean.setSddscpZbname(newdepart.getName());
							bean.setDepartment(newdepart);		
							bean.setSddscpHistorydy(bean.getSddscpHistorydy()+","+olddepart.getId().toString());
							bean.setSddscpOutpartycause("");
							bean.setSddscpNewaddyd(null);
							bean.setSddscpOutpartycause(outReson2);
							//bean.setSddscpIsinjob("2");
							// 修改历史党员组织的数据库中的字段值
							//bean.setSddscpHistorydy(","+olddepart.getParent().getId());
						}else{
							System.out.println("新组织必须为支部");
						}
					}else{
						System.out.println("新旧组织不能为空");
					}
				}
			}else{
				if(targetDeptId!=null){// 转到的目标党组织的ID
					newdepart = cmsDeptMng.findById(targetDeptId);
					if(newdepart.getSddspoOrgtype().equals("支部")){//目标组织的党组织类型是支部
						bean.setSddscpNewaddyd(newdepart.getId().toString());// 目标组织的党组织ID
						bean.setSddscpOutpartycause(outReson2);

						//bean.setSddscpIsinjob("2");
					}else{
						System.out.println("目标组织必须为支部");
					}
				}else{//脱离山东地税系统
					bean.setSddscpHistorydy(bean.getSddscpHistorydy()+","+bean.getSddscpZb());
					bean.setSddscpDzz(null);
					bean.setSddscpDzzname("");
					bean.setSddscpDzzjob("无");
					bean.setSddscpJgdw(null);
					bean.setSddscpJgdwname("");
					bean.setSddscpJgdwjob("无");
					//bean.setSddscpZbname("");
					bean.setSddscpZbjob("无");
					bean.setSddscpHistorydy(bean.getSddscpZb().toString());
					//bean.setSddscpZb(null);
					bean.setSddscpNewaddyd(null);
					bean.setSddscpOutpartycause(outReson2);
					bean.setSddscpIsinjob("2");
					bean.setSddscpIsvalid("0");
					if(targetLifeOrg!=null){
						bean.setSddscpTxTargetLifeOrg(targetLifeOrg);
					}
					floatMng.outAndaddWithUser(bean, false,olddepart,newdepart,sddscpChanges,null);
				}
			}
		}else{//退回
			if(zjstatus==5){//确认收到退回说明
				bean.setSddscpZjStatus(null);
			}else{
				bean.setSddscpNewaddyd(null);
				bean.setSddscpThsm(SDDSCPTHSM);
				bean.setSddscpZjStatus(zjstatus);
			}
		}
		manager.updateUser(bean);
		//return memberList(bean, 1, request, model);
		return "redirect:new_v_list.do?sddscpAll=1";
	}
	@RequiresPermissions("member:o_update")
	@RequestMapping("/member/o_update.do")
	public String update(Integer id, String email, String password,
			Boolean disabled, CmsUserExt ext, Integer groupId,Integer grain,
			String queryUsername, String queryEmail, Integer queryGroupId,String sddscpIdnumber,String sddscpNational,String sddscpAddress,String sddscpPoliticaltype,
			String sddscpPartyposition,String sddscpBasescore,Integer sddscpXfscore,String sddscpKfscore,
			String sddscpSumscore,String sddscpOrgname,Date sddscpJoinpartydate,Date sddscpEbranchdate,
			Date sddscpJoinworktime,String sddscpEducation,String sddscpGraduate,String sddscpDegree,
			String sddscpMatrimonial,String sddscpWorkplace,String sddscpJobposition,String sddscpNative,
			String sddscpResidence,String sddscpOtherphone,String sddscpUsertype,Integer departmentId,String departName,
			String sddscpExcellentcause,String sddscpUnqualifiedcause,String sddscpIsinjob,String sddscpOutpartytype,String sddscpOutpartycause,
			Integer sddscpJgdw,Integer sddscpDzz,Integer sddscpZb,String sddscpJgdwjob,String sddscpDzzjob,String sddscpZbjob,String sddscpAssess,
			String sddscpChanges,String sddscpChangestype,String sddscpJgdwname,String sddscpDzzname,String sddscpZbname,
			String sddscpJobstatus,Boolean queryDisabled, Integer pageNo, HttpServletRequest request,ModelMap model) {
			WebErrors errors = validateUpdate(id, request);
			if (errors.hasErrors()) {
				return errors.showErrorPage(model);
			}
			String isAll = request.getParameter("sddscpAll");
			String sddscpIsinjob1 = request.getParameter("sddscpIsinjob1");
			String sddscpChanges1 = request.getParameter("sddscpChanges1");
			String sddscpAssess1 = request.getParameter("sddscpAssess1");
			String targetDeptId = request.getParameter("targetDeptId");
			String zjstatus = request.getParameter("zjstatus");
			Map<String,String>attrs=RequestUtils.getRequestMap(request, "attr_");
			String jobType = "1";
			if(sddscpJobstatus.indexOf("离")>-1){
				jobType = "0";
			}
			String deptId = request.getParameter("deptsIds");
			Integer deptIds  = null;
			if(!StringUtils.isBlank(deptId)){
				deptIds = new Integer(deptId);
			}
			if (departmentId != null) {
				deptIds = departmentId;
			}
			CmsUser bean = manager.updateMember(id, email, password, disabled, ext,
					groupId, grain, sddscpIdnumber, sddscpNational, sddscpAddress,
					sddscpPoliticaltype, sddscpPartyposition, sddscpBasescore,
					sddscpXfscore, sddscpKfscore, sddscpSumscore, sddscpOrgname,
					sddscpJoinpartydate, sddscpEbranchdate, sddscpJoinworktime,
					sddscpEducation, sddscpGraduate, sddscpDegree,
					sddscpMatrimonial, sddscpWorkplace, sddscpJobposition,
					sddscpNative, sddscpResidence, sddscpOtherphone,
					sddscpUsertype, deptIds, departName, sddscpExcellentcause,
					sddscpUnqualifiedcause, jobType, sddscpOutpartytype,
					sddscpOutpartycause, sddscpJobstatus, sddscpJgdw, sddscpDzz,
					sddscpZb, sddscpJgdwjob, sddscpDzzjob, sddscpZbjob,
					sddscpAssess, sddscpChanges, sddscpChangestype, sddscpJgdwname,
					sddscpDzzname, sddscpZbname, attrs, request,targetDeptId);
			if(sddscpChangestype!=null){
				if (sddscpChangestype.equals("2")&& !StringUtils.isBlank(sddscpChanges)) {
					bean.setRegisterTime(new Date());
						if("5".indexOf(sddscpChanges)>-1){//开出党籍
							bean.setSddscpOutpartycause("开出党籍");
						}else if("7".indexOf(sddscpChanges)>-1){//退党
							bean.setSddscpOutpartycause("退党");
						}else if("9".indexOf(sddscpChanges)>-1){//死亡
							bean.setSddscpOutpartycause(sddscpOutpartycause);
						}
						//targetDeptId不为空的时候 表示此次执行转出操作
						if(targetDeptId=="" || targetDeptId==null){
							//由于更换了减少类型故而targetDeptId肯定为空
							     String type = cmsDeptMng.getSsxByDepartId(bean.getDepartment().getId());
							     floatMng.saveOutWithUser(bean, type, "");
							     bean.setSddscpHistorydy(bean.getSddscpHistorydy()+","+bean.getSddscpZb().toString());
								 bean.setSddscpJgdwname(null);
								 bean.setSddscpJgdw(null);
								 bean.setSddscpDzz(null);
								 bean.setSddscpDzzname(null);
								 bean.setSddscpJgdwjob("无");
								 bean.setSddscpDzzjob("无");
								 bean.setSddscpZbjob("无");
								 bean.setSddscpZcdate(new Date().toString());
								 bean.setSddscpIsvalid("0");
								 bean.setSddscpIsinjob("2");// 将其设置为历史党员
								 if( bean.getSddscpZb()!=null ){// 在转出之前该名党员所在支部的 id
										CmsDepartment olddepart = cmsDeptMng.findById(bean.getSddscpZb());
										floatMng.outAndaddWithUser(bean, false,olddepart,null,sddscpChanges,null);
								 }else{
										System.out.println("党员必须在支部下面");
								 }
								 userDao.updateUserInfo(bean);
						}else{
							System.out.println("转出地税系统不需要目标组织");
						}
						//转出转入组织替换操作end
				}
			}
			
			cmsWebserviceMng.callWebService("false",bean.getUsername(), password, email, ext,CmsWebservice.SERVICE_TYPE_UPDATE_USER);
			log.info("update CmsMember id={}.", bean.getId());
			cmsLogMng.operating(request, "cmsMember.log.update", "id="
					+ bean.getId() + ";username=" + bean.getUsername());
			//return memberList(bean, pageNo, request, model);
			if(zjstatus!=null && zjstatus.equals("3")){
				this.zhuanchu(request,null,id,3,null,null,sddscpOutpartycause,null);
			}
			if(isAll!=null && isAll!=""){
				return "redirect:new_v_list.do?sddscpAll=1";
			}else if (sddscpIsinjob1!=null && sddscpIsinjob1!=""){
				return "redirect:new_v_list.do?sddscpIsinjob="+sddscpIsinjob1;
			}else if(sddscpChanges1!=null && sddscpChanges1!=""){
				return "redirect:new_v_list.do?sddscpChanges="+sddscpChanges1;
			}else if(sddscpAssess1!=null && sddscpAssess1!=""){
				return "redirect:new_v_list.do?sddscpAssess="+sddscpAssess1;
			}else{
				return "redirect:new_v_list.do?sddscpAll=1";
			}
	}
	

	@RequiresPermissions("member:o_delete")
	@RequestMapping("/member/o_delete.do")
	public String delete(Integer[] ids, Integer queryGroupId,CmsUser user,
			Boolean queryDisabled, Integer pageNo,String sddscpUsertype,Integer departmentId, HttpServletRequest request,
			ModelMap model) {
		String queryUsername = RequestUtils.getQueryParam(request,
				"queryUsername");
		String queryEmail = RequestUtils.getQueryParam(request, "queryEmail");
		WebErrors errors = validateDelete(ids, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		CmsUser[] beans = null;
		try{
			beans = manager.deleteByIds(ids);
		}catch(Exception e){
			e.printStackTrace();
		}
		for (CmsUser bean : beans) {
			Map<String,String>paramsValues=new HashMap<String, String>();
			paramsValues.put("username", bean.getUsername());
			paramsValues.put("admin", "false");
			cmsWebserviceMng.callWebService(CmsWebservice.SERVICE_TYPE_DELETE_USER, paramsValues);
			log.info("delete CmsMember id={}", bean.getId());
			cmsLogMng.operating(request, "cmsMember.log.delete", "id="
					+ bean.getId() + ";username=" + bean.getUsername());
		}
		String isAll = request.getParameter("sddscpAll");
		String sddscpIsinjob1 = request.getParameter("sddscpIsinjob1");
		String sddscpChanges1 = request.getParameter("sddscpChanges1");
		String sddscpAssess1 = request.getParameter("sddscpAssess1");
		//return memberList(user, pageNo, request, model);
		if(isAll!=null && isAll!=""){
			return "redirect:new_v_list.do?sddscpAll=1";
		}else if (sddscpIsinjob1!=null && sddscpIsinjob1!=""){
			return "redirect:new_v_list.do?sddscpIsinjob="+sddscpIsinjob1;
		}else if(sddscpChanges1!=null && sddscpChanges1!=""){
			return "redirect:new_v_list.do?sddscpChanges="+sddscpChanges1;
		}else if(sddscpAssess1!=null && sddscpAssess1!=""){
			return "redirect:new_v_list.do?sddscpAssess="+sddscpAssess1;
		}else{
			return "redirect:new_v_list.do?sddscpAll=1";
		}
	}
	
	@RequiresPermissions("member:o_check")
	@RequestMapping("/member/o_check.do")
	public String check(Integer[] ids, Integer queryGroupId,
			Boolean queryDisabled, Integer pageNo, String sddscpUsertype,Integer departmentId,HttpServletRequest request,
			ModelMap model) {
		String queryUsername = RequestUtils.getQueryParam(request,
				"queryUsername");
		String queryEmail = RequestUtils.getQueryParam(request, "queryEmail");
		WebErrors errors = validateDelete(ids, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		for(Integer id:ids){
			CmsUser user=manager.findById(id);
			user.setDisabled(false);
			manager.updateUser(user);
			log.info("check CmsMember id={}", user.getId());
			cmsLogMng.operating(request, "cmsMember.log.delete", "id="
					+ user.getId() + ";username=" + user.getUsername());
		}
		return list(queryUsername, queryEmail, queryGroupId, queryDisabled,
				pageNo, sddscpUsertype,departmentId,request, model);
	}

	@RequiresPermissions("member:v_check_username")
	@RequestMapping(value = "/member/v_check_username.do")
	public void checkUsername(HttpServletRequest request, HttpServletResponse response) {
		String username=RequestUtils.getQueryParam(request,"username");
		String pass;
		if (StringUtils.isBlank(username)) {
			pass = "false";
		} else {
			pass = manager.usernameNotExist(username) ? "true" : "false";
		}
		ResponseUtils.renderJson(response, pass);
	}
	
	@RequiresPermissions("member:v_check_idNumber")
	@RequestMapping(value = "/member/v_check_idNumber.do")
	public void checkIdnumber(HttpServletRequest request, HttpServletResponse response) {
		String idNumber=RequestUtils.getQueryParam(request,"sddscpIdnumber");
		String pass;
		if (StringUtils.isBlank(idNumber)) {
			pass = "false";
		} else {
			pass = manager.existUserByIdnumber(idNumber) ? "true" : "false";
		}
		ResponseUtils.renderJson(response, pass);
	}

	private WebErrors validateSave(CmsUser bean, HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		return errors;
	}

	private WebErrors validateEdit(Integer id, HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		if (vldExist(id, errors)) {
			return errors;
		}
		return errors;
	}

	private WebErrors validateUpdate(Integer id, HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		if (vldExist(id, errors)) {
			return errors;
		}
		// TODO 验证是否为管理员，管理员不允许修改。
		return errors;
	}

	private WebErrors validateDelete(Integer[] ids, HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		if(!errors.ifEmpty(ids, "ids")){
			for (Integer id : ids) {
				vldExist(id, errors);
			}
		}
		return errors;
	}

	private boolean vldExist(Integer id, WebErrors errors) {
		if (errors.ifNull(id, "id")) {
			return true;
		}
		CmsUser entity = manager.findById(id);
		if (errors.ifNotExist(entity, CmsUser.class, id)) {
			return true;
		}
		return false;
	}
	
	/**
	 * 进页面方法
	 * @param model
	 * @param sddscpUsertype
	 * @return
	 */
	@RequestMapping("/member/topage.do")
	public String toPage(HttpServletRequest request,Model model,String sddscpAssess,String sddscpIsinjob,String sddscpChanges){
		String sddscpIsAll = request.getParameter("sddscpAll");
		model.addAttribute("sddscpIsinjob", sddscpIsinjob);
		model.addAttribute("sddscpAssess",sddscpAssess);
		model.addAttribute("sddscpChanges",sddscpChanges);
		model.addAttribute("sddscpAll",sddscpIsAll);
		if("1".equals(sddscpIsinjob)){
			return "member/list_injob";
		}else if("0".equals(sddscpIsinjob)){// 离退休党员
			return "member/list_history";
		}else if("2".equals(sddscpIsinjob)){// 历史党员
			return "member/list_dead";
		}else{
			if("1".equals(sddscpAssess)){//优秀
				return "member/list_excellent";
			}else if("2".equals(sddscpAssess)){//不合格
				return "member/list_unqualified";
			}else {
				if("1".equals(sddscpChanges)){
					return "member/list_develop";
				} else if("0".equals(sddscpChanges)){
					return "member/list_develop";
				}else{
					return "member/list_all";
				}
			}
		}
	}
	
	/**
	 * 根据组织获取党员列表
	 * @param queryUsername
	 * @param queryEmail
	 * @param queryGroupId
	 * @param queryDisabled
	 * @param pageNo
	 * @param request
	 * @param model
	 * @param departId
	 * @return
	 */
	@RequiresPermissions("member:getListByDepartId")
	@RequestMapping("/member/getListByDepartId.do")
	public String getListByDepartId(String queryUsername,String sddscpAssess,String sddscpIsinjob,String sddscpChanges, String queryEmail, Boolean queryDisabled, Integer pageNo,
			HttpServletRequest request, ModelMap model,Integer departId) {
		CmsUser userdept = CmsUtils.getUser(request);
		String loginUsername = userdept.getUsername();
		if (departId==null) {
			departId = userdept.getDepartment().getId();
		}
		CmsDepartment dept = cmsDeptMng.findById(departId);
		if("机关党委,党总支".indexOf(dept.getSddspoOrgtype())>-1){
			if((departId != 1) && (departId != 68)){
				departId = dept.getParent().getId();
			}
		}
		Pagination pagination = manager.getMemberByQXandDepartId(departId,sddscpAssess,sddscpIsinjob,sddscpChanges,cpn(pageNo),CookieUtils.getPageSize(request));
		CmsUser user = new CmsUser();
		boolean haveadd = false;
		user.setSddscpAssess(sddscpAssess);
		user.setSddscpChanges(sddscpChanges);
		user.setSddscpIsinjob(sddscpIsinjob);
		user.setDepartment(cmsDeptMng.findById(departId));
		CmsUser self = CmsUtils.getUser(request);
		if(self != null){
			if(cmsDeptMng.findlistByPid(self.getDepartment().getId())){
				haveadd = true;
			}
			if("admin".equals(self.getUsername())){
				haveadd = true;
			}
		}
		String sddscpIsAll = request.getParameter("sddscpAll");
		model.addAttribute("sddscpAll",sddscpIsAll);
		model.addAttribute("pagination", pagination);
		model.addAttribute("departId",departId);
		model.addAttribute("sddscpAssess",sddscpAssess);
		model.addAttribute("sddscpChanges",sddscpChanges);
		model.addAttribute("sddscpIsinjob",sddscpIsinjob);
		model.addAttribute("queryUsername", queryUsername);
		model.addAttribute("user", user);
		model.addAttribute("loginUsername", loginUsername);
		model.addAttribute("queryEmail", queryEmail);
		model.addAttribute("queryDisabled", queryDisabled);
		model.addAttribute("groupList", cmsGroupMng.getList());
		model.addAttribute("haveadd", haveadd);
		model.addAttribute("logindepartId",userdept.getDepartment().getId().toString());
		return "member/list";
	}
	
	/**
	 * 替换数据工具
	 * @return
	 */
	@RequestMapping("/member/changeuserdata.do")
	public String changeuserdata(boolean all){
		manager.finduserdatetool(all);
		return "";
	}
	/**
	  * 评分列表
	  */
	@RequestMapping("/scoreuserxf/scoreUserList.do")
	 public String scoreUserList(HttpServletRequest request, ModelMap model){
		 CmsUser user = CmsUtils.getUser(request);
		 Map<String, String> m = new HashMap<String, String>();
		 if (StringUtils.isNotBlank(request.getParameter("queryUsername"))) {
			 m.put("queryUsername", request.getParameter("queryUsername"));
			 model.addAttribute("queryUsername", request.getParameter("queryUsername"));
		 }
		 if (StringUtils.isNotBlank(request.getParameter("queryIdcard"))) {
			 m.put("queryIdcard", request.getParameter("queryIdcard"));
			 model.addAttribute("queryIdcard", request.getParameter("queryIdcard"));
		 }
		 List<CmsUser> list = manager.scoreUserList(user.getDepartment().getId(), m);
		 model.addAttribute("list", list);
		 return "scoreuserxf/scoreUserXF";
	 }
	/**
	 * 获取评分
	 * dialog数据
	 */
	@RequestMapping(value = "/scoreuserxf/getUserPf.do", method = RequestMethod.POST)
	@ResponseBody
	public Object getUserPf(Integer userid){
		CmsUser user = manager.findById(userid);
		String str = "";
		str += user.getId()+","
		+user.getUsername()+","
		+user.getSddscpXfscore()+","
		+user.getSddscpFjf()+","
		+user.getSddscpJcf()+","
		+user.getSddscpPfsmfj()+","
		+user.getSddscpPfsmjc();
		return  str;
	}

	 /**
	  * 更新分数
	  */
	@RequestMapping("/scoreuserxf/updateScore.do")
	 public String updateScore(CmsUser model){
		manager.updateScore(model);
		return "redirect:scoreUserList.do";
	 }
	
	/*
	 * 党员管理
	 */
	@RequestMapping("/member/new_v_list.do")
	public String memberList(CmsUser user, Integer pageNo,HttpServletRequest request, ModelMap model){
		CmsUser self = CmsUtils.getUser(request);
		String loginUsername = self.getUsername();
		boolean isadmin = false;
		Pagination pagination =null;
		boolean haveadd = false;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
		String risendsYear = request.getParameter("risendsYear");
		//String sddscpIsinjob = request.getParameter("sddscpIsinjob");
		//String sddscpHistoryValue = request.getParameter("sddscpHistorydy");//sddscpHistorydy
		String sddscpHistoryValue = self.getSddscpHistorydy();
		if(StringUtils.isBlank(risendsYear)){
			risendsYear = sdf.format(new Date());
		}
		//System.out.println("党组织id:"+self.getDepartment().getId()); ;
		String departId = request.getParameter("departId");
		//System.out.println("departId"+departId);
		if(departId==null||(departId=="")){
			if(self.getUsername().equals("省局机关党委")||(self.getUsername().equals("admin"))){
				departId = "1";
			}else{
				if(!self.getDepartment().getSddspoOrgtype().equals("支部")){
					departId = String.valueOf(self.getDepartment().getParent().getId());
				}else{
					departId = String.valueOf(self.getDepartment().getId());
				}
			}
		}
		Integer deptId = new Integer(departId);
		Integer newDeptId = 0;
		if(StringUtils.isNotBlank(self.getUsername())){
			if("admin".equals(self.getUsername())){isadmin = true;haveadd = true;}
			if(StringUtils.isNotBlank(user.getUsername()) || StringUtils.isNotBlank(user.getSddscpIdnumber())){
				user.setDepartment(cmsDeptMng.findById(deptId));
				pagination = manager.memberListAndYear(user, cpn(pageNo), CookieUtils.getPageSize(request),isadmin,risendsYear);
			} else {
				// 20170301 added by raoxq 修复BUG,处理支部登录后也看到上级党组织下的所有党员这个BUG
				if ("支部".equals(self.getDepartment().getSddspoOrgtype())) {
					if (deptId != self.getDepartment().getId()) {
						deptId = self.getDepartment().getId();
					}
					user.setDepartment(cmsDeptMng.findById(deptId));
					pagination = manager.getMemberByQXandDepartIdAndYear(
							deptId,null, user.getSddscpAssess(),
							user.getSddscpIsinjob(), user.getSddscpChanges(),
							cpn(pageNo), CookieUtils.getPageSize(request),
							risendsYear,sddscpHistoryValue);
				} else {
					CmsDepartment dept = cmsDeptMng.findById(deptId);
					if (!dept.getSddspoOrgtype().equals("支部")) {
						if (dept.getParent() != null) {
							if (dept.getParent().getParent() == null) {
								if (dept.getPriority() == 1) {
									deptId = dept.getParent().getId();
									newDeptId = self.getDepartment().getId();
								} else {
									deptId = dept.getId();
								}
							} else {
								deptId = dept.getParent().getId();
							}
						} else {
							deptId = dept.getId();
						}
					}
					user.setDepartment(cmsDeptMng.findById(deptId));
					pagination = manager.getMemberByQXandDepartIdAndYear(
							deptId,newDeptId, user.getSddscpAssess(),
							user.getSddscpIsinjob(), user.getSddscpChanges(),
							cpn(pageNo), CookieUtils.getPageSize(request),
							risendsYear,user.getSddscpHistorydy());
				}
			}
		}
		if(cmsDeptMng.findlistByPid(deptId)){
			haveadd = true;
		}                                   
		String isAll = request.getParameter("sddscpAll");
		model.addAttribute("pagination", pagination);
		model.addAttribute("user", user);
		model.addAttribute("sddscpAssess", request.getParameter("sddscpAssess"));
		model.addAttribute("sddscpChanges", request.getParameter("sddscpChanges"));
		model.addAttribute("sddscpAll", isAll);
		model.addAttribute("sddscpIsinjob", request.getParameter("sddscpIsinjob"));
		model.addAttribute("groupList", cmsGroupMng.getList());
		model.addAttribute("risendsYear",risendsYear);
		model.addAttribute("loginUsername", loginUsername);
		model.addAttribute("pageNo", pagination.getPageNo());
		model.addAttribute("pageSize", pagination.getPageSize());
		if (StringUtils.isNotBlank(request.getParameter("username"))) {
			model.addAttribute("username", request.getParameter("username").toString());
		}
		if (StringUtils.isNotBlank(request.getParameter("sddscpIdnumber"))) {
			model.addAttribute("sddscpIdnumber", request.getParameter("sddscpIdnumber").toString());
		}
		model.addAttribute("haveadd", haveadd);
		model.addAttribute("departId", departId);
		CmsUser userdept = CmsUtils.getUser(request);
		model.addAttribute("logindepartId", userdept.getDepartment().getId().toString());

		//model.addAttribute("logindepartId2", self.getDepartment().getId().toString());
		return "member/list";
	}
	
	//上传
	@RequiresPermissions("member:o_upload_doc")
	@RequestMapping("/member/o_upload_doc.do")
	public String uploadDoc(
			@RequestParam(value = "docFile", required = false) MultipartFile file,
			String filename, HttpServletRequest request, ModelMap model) {
		String msg = "";
		int count = 0;
		CmsSite site = CmsUtils.getSite(request);
		CmsUser user = CmsUtils.getUser(request);
		String origName = file.getOriginalFilename();
		if(origName.indexOf("xls")==-1){
			msg = "请上传微软2003版的excel文件";
			return "changeremind/wenku_iframe";
		}
		String ext = FilenameUtils.getExtension(origName).toLowerCase(
				Locale.ENGLISH);
		WebErrors errors = validateUpload(file, request);
		if (errors.hasErrors()) {
			model.addAttribute("error", errors.getErrors().get(0));
			return "changeremind/wenku_iframe";
		}
		// TODO 检查允许上传的后缀
		try {
			String fileUrl;
			if (site.getConfig().getUploadToDb()) {
				String dbFilePath = site.getConfig().getDbFileUri();
				if (!StringUtils.isBlank(filename)
						&& FilenameUtils.getExtension(filename).equals(ext)) {
					filename = filename.substring(dbFilePath.length());
					fileUrl = dbFileMng.storeByFilename(filename, file
							.getInputStream());
				} else {
					fileUrl = dbFileMng.storeByExt(site.getUploadPath(), ext,
							file.getInputStream());
					// 加上访问地址
					fileUrl = request.getContextPath() + dbFilePath + fileUrl;
				}
			} else if (site.getUploadFtp() != null) {
				Ftp ftp = site.getUploadFtp();
				String ftpUrl = ftp.getUrl();
				if (!StringUtils.isBlank(filename)
						&& FilenameUtils.getExtension(filename).equals(ext)) {
					filename = filename.substring(ftpUrl.length());
					fileUrl = ftp.storeByFilename(filename, file
							.getInputStream());
				} else {
					fileUrl = ftp.storeByExt(site.getUploadPath(), ext, file
							.getInputStream());
					// 加上url前缀
					fileUrl = ftpUrl + fileUrl;
				}
			} else {
				String ctx = request.getContextPath();
				if (!StringUtils.isBlank(filename)
						&& FilenameUtils.getExtension(filename).equals(ext)) {
					filename = filename.substring(ctx.length());
					fileUrl = fileRepository.storeByFilename(filename, file);
				} else {
					fileUrl = fileRepository.storeByExt(site.getUploadPath(),
							ext, file);
				}
				// 加上部署路径
				fileUrl = ctx + fileUrl;
			}
			manager.updateUploadSize(user.getId(), Integer.parseInt(String.valueOf(file.getSize()/1024)));
			if(fileMng.findByPath(fileUrl)!=null){
				fileMng.saveFileByPath(fileUrl, origName, false);
			}
			model.addAttribute("docPath", fileUrl);
			model.addAttribute("docExt", ext);
		} catch (IllegalStateException e) {
			model.addAttribute("error", e.getMessage());
			log.error("upload file error!", e);
		} catch (IOException e) {
			model.addAttribute("error", e.getMessage());
			log.error("upload file error!", e);
		}
		
		return "changeremind/wenku_iframe";
	}
	
	private WebErrors validateUpload(MultipartFile file,
			HttpServletRequest request) {
		String origName = file.getOriginalFilename();
		CmsUser user= CmsUtils.getUser(request);
		String ext = FilenameUtils.getExtension(origName).toLowerCase(Locale.ENGLISH);
		int fileSize = (int) (file.getSize() / 1024);
		WebErrors errors = WebErrors.create(request);
		if (errors.ifNull(file, "file")) {
			return errors;
		}
		if(origName!=null&&(origName.contains("/")||origName.contains("\\")||origName.indexOf("\0")!=-1)){
			errors.addErrorCode("upload.error.filename", origName);
		}
		//非允许的后缀
		if(!user.isAllowSuffix(ext)){
			errors.addErrorCode("upload.error.invalidsuffix", ext);
			return errors;
		}
		//超过附件大小限制
		if(!user.isAllowMaxFile((int)(file.getSize()/1024))){
			errors.addErrorCode("upload.error.toolarge",origName,user.getGroup().getAllowMaxFile());
			return errors;
		}
		//超过每日上传限制
		if (!user.isAllowPerDay(fileSize)) {
			long laveSize=user.getGroup().getAllowPerDay()-user.getUploadSize();
			if(laveSize<0){
				laveSize=0;
			}
			errors.addErrorCode("upload.error.dailylimit", laveSize);
		}
		return errors;
	}
	
	
	
	@RequestMapping("/member/importuser.do")
	@ResponseBody
	public String importFiles(String filepath,HttpServletRequest request) {
		String msg = "";
		int count = 0;
		int toRow = 0;
		String fpaht = request.getSession().getServletContext().getRealPath("/"); 
		File file = new File(fpaht+filepath);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		//获取Excel对象并初始化
		if(file.exists()){
			ExcelHelper eh = null;
			try {
				eh = JxlExcelHelper.getInstance(file);
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			List<NewCmsUser> list = new ArrayList<NewCmsUser>();
			List<CmsUser> list2 = new ArrayList<CmsUser>();
			List<CmsUserExt> list3 = new ArrayList<CmsUserExt>();
			//指定Excel对应的实体列(机关党委、党总支、支部还要根据名称来存ID)
			String[] filedName = new String[]{"username","sddscpSex","sddscpNational","sddscpBirthday","sddscpIdnumber","sddscpEducation","sddscpDegree","sddscpGraduate",
					"sddscpJobstatus","sddscpJoinworktime","sddscpJoinpartydate","sddscpEbranchdate","sddscpMatrimonial","sddscpWorkplace","sddscpJobposition",
					"sddscpJgdwname","sddscpJgdwjob","sddscpDzzname","sddscpDzzjob","sddscpZbname","sddscpZbjob","sddscpNative","sddscpResidence","sddscpAddress",
					"sddscpPhone","sddscpMobile","email"};
			
			//将指定的Excel文件中的数据转换成数据列表
			try {
				list = eh.readExcel(NewCmsUser.class, filedName, true);
				CmsDepartment departmentvalue = new CmsDepartment();
				String departtype = "";
				for (int i = 0; i < list.size(); i++) {
					toRow++;
					//toRow += i;
					CmsUser saveUser = new CmsUser();//用户model
					CmsUserExt userExt = new CmsUserExt();//用户拓展属性model
					if (!StringUtils.isNotBlank(list.get(i).getUsername()) && !StringUtils.isNotBlank(list.get(i).getSddscpIdnumber())) {
						continue;
					}
					//-----------username
					if(StringUtils.isNotBlank(list.get(i).getUsername())){
						saveUser.setUsername(list.get(i).getUsername());
					}else {
						return msg = "第"+(toRow+1)+"行,请填写 ‘姓名’ 字段";
					}
					
					//-----------sddscpSex
					if(StringUtils.isNotBlank(list.get(i).getSddscpSex())){
						if("男".equals(list.get(i).getSddscpSex())){
							userExt.setGender(true);
							saveUser.setSddscpSex("1");
						}else if ("女".equals(list.get(i).getSddscpSex())) {
							userExt.setGender(false);
							saveUser.setSddscpSex("2");
						}else {
							return msg = "第"+(toRow+1)+"行请正确填写 '性别' 字段";
						}
					}else {
						return msg = "第"+(toRow+1)+"行,请填写 '性别' 字段";
					}
					//-----------email
					if(StringUtils.isNotBlank(list.get(i).getEmail())){
						saveUser.setEmail(list.get(i).getEmail());
					}else {
						saveUser.setEmail("");
					}
					//-----------sddscpNational
					if(StringUtils.isNotBlank(list.get(i).getSddscpNational())){
						saveUser.setSddscpNational(list.get(i).getSddscpNational());
					}else {
						saveUser.setSddscpNational("");
					}
					//-----------sddscpIdnumber
					if(StringUtils.isNotBlank(list.get(i).getSddscpIdnumber())){
						boolean exist = manager.existUserByIdnumber(list.get(i).getSddscpIdnumber().trim());
						if(exist){
							saveUser.setSddscpIdnumber(list.get(i).getSddscpIdnumber().trim());
						}else{
							return msg = "第"+(toRow+1)+"行,身份证已存在";
						}
					}else {
						return msg = "第"+(toRow+1)+"行,请填写 '身份证号' 字段";
					}
					//-----------sddscpAddress
					if(StringUtils.isNotBlank(list.get(i).getSddscpAddress())){
						saveUser.setSddscpAddress(list.get(i).getSddscpAddress());
					}else {
						saveUser.setSddscpAddress("");
					}
					//-----------sddscpBirthday
					if(list.get(i).getSddscpBirthday()!=null){
						userExt.setBirthday(list.get(i).getSddscpBirthday());
					}else {
						return msg = "第"+(toRow+1)+"行,请填写 '出生日期' 字段";
					}
					//-----------sddscpJobstatus
					if(StringUtils.isNotBlank(list.get(i).getSddscpJobstatus())){
						if(!("公务员".equals(list.get(i).getSddscpJobstatus())) && !("其他").equals(list.get(i).getSddscpJobstatus())){
							if(list.get(i).getSddscpJobstatus().indexOf("事业")>=0){
								saveUser.setSddscpJobstatus("事业编制人员");
							}else if(list.get(i).getSddscpJobstatus().indexOf("工人")>=0 || list.get(i).getSddscpJobstatus().indexOf("工勤")>=0){
								saveUser.setSddscpJobstatus("工勤");
							}else if(list.get(i).getSddscpJobstatus().indexOf("离退")>=0){
								saveUser.setSddscpJobstatus("离退休党员");
							}else {
								return msg = "第"+(toRow+1)+"行,请正确填写 ‘工作身份’ 字段";
							}
						}else {
							saveUser.setSddscpJobstatus(list.get(i).getSddscpJobstatus());
						}
					}else {
						saveUser.setSddscpJobstatus("");
					}
					//-----------sddscpJoinpartydate
					if(list.get(i).getSddscpJoinpartydate()!=null){
						Date joinPartyDate = list.get(i).getSddscpJoinpartydate();
						Date birthday = list.get(i).getSddscpBirthday();
						if(joinPartyDate.compareTo(birthday)>0){
							saveUser.setSddscpJoinpartydate(list.get(i).getSddscpJoinpartydate());
						}else{
							return msg = "第"+(toRow+1)+"行,入党时间字段的值必须小于出生日期的值";
						}
					}else {
						return msg = "第"+(toRow+1)+"行,请填写 '入党时间' 字段";
					}
					//-----------sddscpEbranchdate
					if(list.get(i).getSddscpEbranchdate()!=null){
						saveUser.setSddscpEbranchdate(list.get(i).getSddscpEbranchdate());
					}
					else {
						saveUser.setSddscpEbranchdate(sdf.parse("2000-01-01"));
					}
					//-----------sddscpJoinworktime
					if(list.get(i).getSddscpJoinworktime()!=null){
						saveUser.setSddscpJoinworktime(list.get(i).getSddscpJoinworktime());
					}
					else {
						return msg = "第"+(toRow+1)+"行,请填写 '参加工作日期' 字段";
					}
					//-----------sddscpEducation
					String saveSddscpEducation = "";
					if(StringUtils.isNotBlank(list.get(i).getSddscpEducation())){
						if("研究生".equals(list.get(i).getSddscpEducation())){
							saveSddscpEducation = "1";
						}
						else if(list.get(i).getSddscpEducation().indexOf("本科")>=0){
							saveSddscpEducation = "2";
						}
						else if("大学专科".equals(list.get(i).getSddscpEducation())){
							saveSddscpEducation = "3";
						}
						else if("专科以下".equals(list.get(i).getSddscpEducation())){
							saveSddscpEducation = "4";
						}
						else {
							return msg = "第"+(toRow+1)+"行,‘学历’ 填写有误";
						}
						saveUser.setSddscpEducation(saveSddscpEducation);
					}
					else {
						return msg = "第"+(toRow+1)+"行,请填写 '学历' 字段";
					}
					//-----------sddscpGraduate
					if(StringUtils.isNotBlank(list.get(i).getSddscpGraduate())){
						saveUser.setSddscpGraduate(list.get(i).getSddscpGraduate());
					}else {
						saveUser.setSddscpGraduate("");
					}
					//-----------sddscpDegree
					String saveSddscpDegree = "";
					if(StringUtils.isNotBlank(list.get(i).getSddscpDegree())){
						if("无".equals(list.get(i).getSddscpDegree())){
							saveSddscpDegree = "1";
						}
						else if("学士".equals(list.get(i).getSddscpDegree())){
							saveSddscpDegree = "2";
						}
						else if("硕士".equals(list.get(i).getSddscpDegree())){
							saveSddscpDegree = "3";		
						}
						else if("博士".equals(list.get(i).getSddscpDegree())){
							saveSddscpDegree = "4";
						}
						else{
							return msg = "第"+(toRow+1)+"行,请填写正确 '学位' 的数据,如无学位,填写 无";
						}
						saveUser.setSddscpDegree(saveSddscpDegree);
					}
					else {
						saveUser.setSddscpDegree("1");
					}
					//-----------sddscpMatrimonial
					String saveSddscpMatrimonial = "";
					if(StringUtils.isNotBlank(list.get(i).getSddscpMatrimonial())){
						if("未婚".equals(list.get(i).getSddscpMatrimonial())){
							saveSddscpMatrimonial = "1";
						}
						else if("已婚".equals(list.get(i).getSddscpMatrimonial())){
							saveSddscpMatrimonial = "2";
						}
						else {
							return msg = "第"+(toRow+1)+"行,请填写 ‘已婚’ 或 ‘未婚’";
						}
						saveUser.setSddscpMatrimonial(saveSddscpMatrimonial);
					}
					else {
						saveUser.setSddscpMatrimonial("1");
					}
					//-----------sddscpWorkplace
					if(StringUtils.isNotBlank(list.get(i).getSddscpWorkplace())){
						saveUser.setSddscpWorkplace(list.get(i).getSddscpWorkplace());
					}else {
						saveUser.setSddscpWorkplace("");
					}
					//-----------sddscpJobposition
					if(StringUtils.isNotBlank(list.get(i).getSddscpJobposition())){
						saveUser.setSddscpJobposition(list.get(i).getSddscpJobposition());
					}else {
						saveUser.setSddscpJobposition("");
					}
					//-----------sddscpNative
					if(StringUtils.isNotBlank(list.get(i).getSddscpNative())){
						saveUser.setSddscpNative(list.get(i).getSddscpNative());
					}else {
						saveUser.setSddscpNative("");
					}
					//-----------sddscpResidence
					if(StringUtils.isNotBlank(list.get(i).getSddscpResidence())){
						saveUser.setSddscpResidence(list.get(i).getSddscpResidence());
					}else {
						saveUser.setSddscpResidence("");
					}
					//-----------sddscpPhone
					if(StringUtils.isNotBlank(list.get(i).getSddscpPhone())){
						userExt.setPhone(list.get(i).getSddscpPhone());
						saveUser.setSddscpPhone(list.get(i).getSddscpPhone());
					}else {
						saveUser.setSddscpPhone("");
					}
					//-----------sddscpMobile
					if(StringUtils.isNotBlank(list.get(i).getSddscpMobile())){
						userExt.setMobile(list.get(i).getSddscpMobile());
						saveUser.setSddscpMobile(list.get(i).getSddscpMobile());
					}else {
						saveUser.setSddscpMobile("");
					}
					//-----------sddscpOtherphone
					if(StringUtils.isNotBlank(list.get(i).getSddscpOtherphone())){
						saveUser.setSddscpOtherphone(list.get(i).getSddscpOtherphone());
					}else {
						saveUser.setSddscpOtherphone("");
					}
					
					//-----------sddscpJgdwname/sddscpDzzname/sddscpZbname begin------------------
					boolean orgIsNotNull = false;
					try {
						if(StringUtils.isNotBlank(list.get(i).getSddscpJgdwname())){
							departmentvalue = cmsDeptMng.findByName(list.get(i).getSddscpJgdwname());
							if(departmentvalue!=null){
								if(StringUtils.isNotBlank(list.get(i).getSddscpJgdwjob())){
									saveUser.setSddscpJgdwname(departmentvalue.getName());
									saveUser.setSddscpJgdw(departmentvalue.getId());
									saveUser.setSddscpJgdwjob(list.get(i).getSddscpJgdwjob());
									orgIsNotNull = true;
									departtype = "机关党委";
								}else {
									return msg = "第"+(toRow+1)+"行,请填写 ‘机关党委职务’";
								}
							}else {
								return "第"+(toRow+1)+"行,机关党委与系统名称不一致，请重新填写";
							}
						}
						
						if(StringUtils.isNotBlank(list.get(i).getSddscpDzzname())){
							departmentvalue = cmsDeptMng.findByName(list.get(i).getSddscpDzzname());
							if(departmentvalue!=null){
								if(StringUtils.isNotBlank(list.get(i).getSddscpDzzjob())){
									saveUser.setSddscpDzzname(departmentvalue.getName());
									saveUser.setSddscpDzz(departmentvalue.getId());
									saveUser.setSddscpDzzjob(list.get(i).getSddscpDzzjob());
									orgIsNotNull = true;
									departtype = "党总支";
								}else {
									return msg = "第"+(toRow+1)+"行,请填写 ‘党总支职务’";
								}
							}else {
								return "第"+(toRow+1)+"行,党总支与系统名称不一致，请重新填写";
							}
						}
						if(StringUtils.isNotBlank(list.get(i).getSddscpZbname())){
							departmentvalue = cmsDeptMng.findByName(list.get(i).getSddscpZbname());
							if(departmentvalue!=null){
								if(StringUtils.isNotBlank(list.get(i).getSddscpZbjob())){
									saveUser.setSddscpZbname(departmentvalue.getName());
									saveUser.setSddscpZb(departmentvalue.getId());
									saveUser.setSddscpZbjob(list.get(i).getSddscpZbjob());
									orgIsNotNull = true;
									departtype = "支部";
								}else{
									return "第"+(toRow+1)+"行,支部职务必须填写书记,专职副书记,副书记,委员,无  中任意一项,不能是空白。";
								}
							}else {
								return msg = "第"+(toRow+1)+"行,您所填写的 '所属"+departtype+"'名称与系统不符,请参照系统后填写正确的支部名称.";
							}
						}else{
							return msg = "第"+(toRow+1)+"行,‘所属支部’为必填项";
						}
						String jJob = list.get(i).getSddscpJgdwjob();
						String dJob = list.get(i).getSddscpDzzjob();
						String zJob = list.get(i).getSddscpZbjob();
						if(!StringUtils.isBlank(jJob)){
							if(",书记,专职副书记,副书记,委员,无,".indexOf(","+jJob.trim()+",")<0){
								return msg = "第"+(toRow+1)+"行,机关党委职务只能是 书记,专职副书记,副书记,委员,无  中任意一项.";
							}
						}
						if(!StringUtils.isBlank(dJob)){
							if(",书记,专职副书记,副书记,委员,无,".indexOf(","+dJob.trim()+",")<0){
								return msg = "第"+(toRow+1)+"行,党总支职务只能是 书记,专职副书记,副书记,委员,无  中任意一项.";
							}
						}
						if(!StringUtils.isBlank(zJob)){
							//if("书记,专职副书记,副书记,委员,无".indexOf(zJob.trim())<0){
						    if(",书记,专职副书记,副书记,支部委员,无,".indexOf(","+zJob.trim()+",")<0){
						    	return msg = "第 "+(toRow+1)+" 行,支部职务只能是 书记,副书记,支部委员,无  四类中任意一项.\r\t注意:如没有职务，请填无，不能空白";
							}
						}
						if (StringUtils.isNotBlank(departmentvalue.getName())) {
							//操作组织名称and组织ID
							saveUser.setDepartment(CmsUtils.getUser(request).getDepartment());
							saveUser.setSddscpOrgname(CmsUtils.getUser(request).getDepartment().getName());
						}
					} catch (Exception e) {
						msg = "第"+(toRow+1)+"行,您填写的所属 '"+departtype+"'信息存在重复，请记录党员信息并联系工作(技术支持)人员.";
						// TODO: handle exception
					}
					//-----------sddscpJgdwname/sddscpDzzname/sddscpZbname end------------------
					
					list2.add(saveUser);
					list3.add(userExt);
				}
				//--------其他必填/非必填字段 begin
				String password = "123";
				String sddscpPoliticaltype = "1";	//1.中共党员 2.预备党员
				String sddscpPartyposition = "";
				String sddscpExcellentcause = "";
				String sddscpUnqualifiedcause = "";
				String sddscpOutpartytype = "";
				String sddscpOutpartycause = "";
				String sddscpAssess = "";
				String sddscpChanges = "";
				String sddscpChangestype = "1";
                
				//--------其他必填字段 end
				if(list2.size()>0){
					for (int j = 0; j < list2.size(); j++) {
						//-----------------导入程序启动 begin
						try {
							String sddscpJobType = "";
							if(list.get(j).getSddscpJobstatus()==null || (list.get(j).getSddscpJobstatus().length()==0)){
								sddscpJobType = "";
							}else{
								if(list.get(j).getSddscpJobstatus().indexOf("去世")>-1){
									sddscpJobType = "2";
								}else if(list.get(j).getSddscpJobstatus().indexOf("退党")>-1){
									sddscpJobType = "2";
								}else if(list.get(j).getSddscpJobstatus().indexOf("离")>-1){
									sddscpJobType = "0";
								}else{
									sddscpJobType = "1";
								}
							}
							manager.registerMember(
									(StringUtils.isBlank(list2.get(j).getUsername())?"":list2.get(j).getUsername().trim()), 
									(StringUtils.isBlank(list2.get(j).getEmail())? "":list2.get(j).getEmail().trim()), 
									password, "127.0.0.1", 1, 0, 
									(StringUtils.isBlank(list2.get(j).getSddscpIdnumber())?"":list2.get(j).getSddscpIdnumber().trim()),
									(StringUtils.isBlank(list2.get(j).getSddscpNational())?"":list2.get(j).getSddscpNational().trim()), 
									(StringUtils.isBlank(list2.get(j).getSddscpAddress())? "":list2.get(j).getSddscpAddress().trim()), 
									(StringUtils.isBlank(sddscpPoliticaltype)?"":sddscpPoliticaltype.trim()), 
									(StringUtils.isBlank(sddscpPartyposition)?"": sddscpPartyposition.trim()),
									"0",0, "0", "0", 
									(StringUtils.isBlank(list2.get(j).getSddscpOrgname())?"":list2.get(j).getSddscpOrgname().trim()), list2.get(j).getSddscpJoinpartydate(),list2.get(j).getSddscpEbranchdate(), list2.get(j).getSddscpJoinworktime(),
									(StringUtils.isBlank(list2.get(j).getSddscpEducation())?"":list2.get(j).getSddscpEducation().trim()), 
									(StringUtils.isBlank(list2.get(j).getSddscpGraduate())?"":list2.get(j).getSddscpGraduate().trim()), list2.get(j).getSddscpDegree(),list2.get(j).getSddscpMatrimonial(), 
									(StringUtils.isBlank(list2.get(j).getSddscpWorkplace())?"":list2.get(j).getSddscpWorkplace().trim()), 
									(StringUtils.isBlank(list2.get(j).getSddscpJobposition())?"":list2.get(j).getSddscpJobposition().trim()), 
									(StringUtils.isBlank(list2.get(j).getSddscpNative())?"":list2.get(j).getSddscpNative().trim()),
									(StringUtils.isBlank(list2.get(j).getSddscpResidence())?"":list2.get(j).getSddscpResidence().trim()),
									list2.get(j).getSddscpOtherphone(), "1", list2.get(j).getDepartment().getId(), list2.get(j).getDepartment().getName(), 
									(StringUtils.isBlank(sddscpExcellentcause)?"":sddscpExcellentcause.trim()), 
									(StringUtils.isBlank(sddscpUnqualifiedcause)?"": sddscpUnqualifiedcause.trim()), sddscpJobType, sddscpOutpartytype, 
									(StringUtils.isBlank(sddscpOutpartycause)?"":sddscpOutpartycause.trim()), 
									(StringUtils.isBlank(list2.get(j).getSddscpJobstatus())?"":list2.get(j).getSddscpJobstatus().trim()),"1", "", "", list2.get(j).getSddscpJgdw(), list2.get(j).getSddscpDzz(), list2.get(j).getSddscpZb(), 
									(StringUtils.isBlank(list2.get(j).getSddscpJgdwjob())?"":list2.get(j).getSddscpJgdwjob().trim()),
									(StringUtils.isBlank(list2.get(j).getSddscpDzzjob())?"":list2.get(j).getSddscpDzzjob().trim()), 
									(StringUtils.isBlank(list2.get(j).getSddscpZbjob())?"":list2.get(j).getSddscpZbjob().trim()), sddscpAssess, sddscpChanges,sddscpChangestype,
									(StringUtils.isBlank(list2.get(j).getSddscpJgdwname())?"":list2.get(j).getSddscpJgdwname().trim()),
									(StringUtils.isBlank(list2.get(j).getSddscpDzzname())?"":list2.get(j).getSddscpDzzname().trim()),
									(StringUtils.isBlank(list2.get(j).getSddscpZbname())?"":list2.get(j).getSddscpZbname().trim()), false, list3.get(j), null, false,null);
							
							count++;
						} catch (Exception e) {
							// TODO: handle exception
							e.printStackTrace();
						}
						//-----------------导入程序启动 end
					}
					msg = "数据导入成功，一共导入"+count+"条数据！";
				}
			} catch (Exception e) {
				msg = "数据解析异常，导入失败！";
				e.printStackTrace();
			}
		}else {
			msg = "请上传文件";
		}
		return msg;
	}
	@RequestMapping("/member/getexcel.do")
	public void getexcel(CmsUser user,HttpServletResponse response,HttpServletRequest request){
		String pageNo = request.getParameter("pageNo");
		String ids = request.getParameter("ids");
		String pageSize = request.getParameter("pageSize"); 
		if("undefined".equalsIgnoreCase(ids)){
			ids=null;
		}
		CmsUser selfUsers = CmsUtils.getUser(request);
		String sddscpAssess = request.getParameter("sddscpAssess");
		String sddscpChanges = request.getParameter("sddscpChanges");
		String sddscpIsinjob = request.getParameter("sddscpIsinjob");
		String username = request.getParameter("username");
		String departId = request.getParameter("departId");
		if(departId==null||(departId=="")){
			departId = String.valueOf(selfUsers.getDepartment().getId());
		}
		Integer deptId = new Integer(departId);
		List<CmsUser> list =null;
		user.setDepartment(cmsDeptMng.findById(deptId));
		if("支部,工作指导组".indexOf(user.getDepartment().getSddspoOrgtype())>-1){
			list = manager.getMemberByQXandDepartIdAndYids(deptId,sddscpAssess,sddscpIsinjob,sddscpChanges,Integer.parseInt(pageNo),CookieUtils.getPageSize(request),ids,username);
		} else {
			list = manager.getMemberByQXandDepartIdAndYids(user.getDepartment().getParent().getId(),sddscpAssess,sddscpIsinjob,sddscpChanges,Integer.parseInt(pageNo),CookieUtils.getPageSize(request),ids,username);
		}
//		List<CmsUser> list=manager.memberListexcel(user,pageNo,ids,pageSize);
		//获取Excel对象并初始化
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String filename = format.format(new Date().getTime())+".xls";
		response.setContentType("application/ms-excel;charset=UTF-8");
		OutputStream out=null;
		try {
			response.setHeader("Content-Disposition", "attachment;filename="
			.concat(String.valueOf(URLEncoder.encode(filename, "UTF-8"))));
			 out= response.getOutputStream();
			 // 创建Excel工作薄
			 WritableWorkbook  wwb=Workbook.createWorkbook(out);
			 // 取得到文件的输出流
			 response.reset(); // 清空输出流
			 response.setContentType("application/vnd.ms-excel"); // 定义输出类型
			 // 设置字体
			 WritableFont wfont = new WritableFont(WritableFont.ARIAL,18,WritableFont.BOLD,false,UnderlineStyle.NO_UNDERLINE,Colour.BLACK); 
			 WritableCellFormat font = new WritableCellFormat(wfont);
			 font.setAlignment(Alignment.CENTRE);
			 WritableSheet sheet = wwb.createSheet("党员信息表",0);
			//第一行
				Label label = null;
				sheet.mergeCells(0,0,26,0);//合并单元格
				label = new Label(0, 0,"党员信息库", font);
				sheet.addCell(label);
				wfont = new WritableFont(WritableFont.createFont("宋体"),14,WritableFont.BOLD);
				font = new WritableCellFormat(wfont);
				font.setWrap(true);//自动换行
				font.setAlignment(Alignment.CENTRE);//居中
				font.setVerticalAlignment(VerticalAlignment.CENTRE);
//				label = new Label(0, 1, "所在党组织", font);
//				sheet.setColumnView(0,35);
//				sheet.addCell(label);
				label = new Label(0, 1, "姓名", font);
				sheet.setColumnView(0,15);
				sheet.addCell(label);
				label = new Label(1, 1, "性别", font);
				sheet.setColumnView(1,5);
				sheet.addCell(label);
				label = new Label(2, 1, "民族", font);
				sheet.setColumnView(2,8);
				sheet.addCell(label);
				label = new Label(3, 1, "出生日期", font);
				sheet.setColumnView(3,18);
				sheet.addCell(label);
				label = new Label(4, 1, "身份证号", font);
				sheet.setColumnView(4,35);
				sheet.addCell(label);
				label = new Label(5, 1, "学历", font);
				sheet.setColumnView(5,12);
				sheet.addCell(label);
				label = new Label(6, 1, "学位", font);
				sheet.setColumnView(6,12);
				sheet.addCell(label);
				label = new Label(7, 1, "毕业院校", font);
				sheet.setColumnView(7,18);
				sheet.addCell(label);
				label = new Label(8, 1, "工作身份", font);
				sheet.setColumnView(8,12);
				sheet.addCell(label);
				//参加工作时间
				label = new Label(9, 1, "参加工作时间", font);
				sheet.setColumnView(9,12);
				sheet.addCell(label);
//				label = new Label(5, 1, "出生年月", font);
//				sheet.setColumnView(5,18);
//				sheet.addCell(label);
				label = new Label(10, 1, "入党日期", font);
				sheet.setColumnView(10,18);
				sheet.addCell(label);
				label = new Label(11, 1, "进入支部日期", font);
				sheet.setColumnView(11,18);
				sheet.addCell(label);
				label = new Label(12, 1, "婚姻状况", font);
				sheet.setColumnView(12,12);
				sheet.addCell(label);
				label = new Label(13, 1, "工作单位", font);
				sheet.setColumnView(13,35);
				sheet.addCell(label);
				label = new Label(14, 1, "职务", font);
				sheet.setColumnView(14,12);
				sheet.addCell(label);
				label = new Label(15, 1, "所属机关党委", font);
				sheet.setColumnView(15,18);
				sheet.addCell(label);
				label = new Label(16, 1, "机关党委职务", font);
				sheet.setColumnView(16,18);
				sheet.addCell(label);
				label = new Label(17, 1, "所属党总支", font);
				sheet.setColumnView(17,18);
				sheet.addCell(label);
				label = new Label(18, 1, "党总支职务", font);
				sheet.setColumnView(18,18);
				sheet.addCell(label);
				label = new Label(19, 1, "所属支部", font);
				sheet.setColumnView(19,35);
				sheet.addCell(label);
				label = new Label(20, 1, "支部职务", font);
				sheet.setColumnView(20,18);
				sheet.addCell(label);
				label = new Label(21, 1, "籍贯", font);
				sheet.setColumnView(21,16);
				sheet.addCell(label);
				label = new Label(22, 1, "户籍所在地", font);
				sheet.setColumnView(22,18);
				sheet.addCell(label);
				label = new Label(23, 1, "家庭住址", font);
				sheet.setColumnView(23,35);
				sheet.addCell(label);
				label = new Label(24, 1, "固定电话", font);
				sheet.setColumnView(24,18);
				sheet.addCell(label);
				label = new Label(25, 1, "手机", font);
				sheet.setColumnView(25,18);
				sheet.addCell(label);
				label = new Label(26, 1, "电子邮箱", font);
				sheet.setColumnView(26,18);
				sheet.addCell(label);
				//数据
				wfont = new WritableFont(WritableFont.createFont("宋体"),12);
				font = new WritableCellFormat(wfont); 
				font.setWrap(true);//自动换行
				font.setAlignment(Alignment.CENTRE);//居中
				font.setVerticalAlignment(VerticalAlignment.CENTRE);
				SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM");
				for(int i=0; i < list.size(); i++){
					CmsUser cp=list.get(i);
//					label = new Label(0,(i+2),cp.getSddscpJgdwname(),font);
//					sheet.addCell(label);
					label = new Label(0,(i+2),cp.getUsername(),font);
					sheet.addCell(label);
					if(null!=cp.getSddscpSex()){
						if("1".equals(cp.getSddscpSex())){
							label = new Label(1,(i+2),"男",font);
						}else{
							label = new Label(1,(i+2),"女",font);
						}	
					}else{
						label = new Label(1,(i+2),"",font);
					}
					sheet.addCell(label);
					label = new Label(2,(i+2),cp.getSddscpNational(),font);
					sheet.addCell(label);
					//出生日期
					String SddscpBirthday = "";
					if(null!=cp.getSddscpBirthday()){
						SddscpBirthday=new SimpleDateFormat("yyyy-MM-dd").format(cp.getSddscpBirthday());
					}
					label = new Label(3,(i+2),	SddscpBirthday ,font);
					sheet.addCell(label);
					//居民身份证
					label = new Label(4,(i+2),cp.getSddscpIdnumber(),font);
					sheet.addCell(label);
					//党员学历
					if(null!=cp.getSddscpEducation()){
						if("1".equals(cp.getSddscpEducation())){
							label = new Label(5,(i+2),"研究生",font);
						}else if("2".equals(cp.getSddscpEducation())){
							label = new Label(5,(i+2),"大学本科",font);
						}else if("3".equals(cp.getSddscpEducation())){
							label = new Label(5,(i+2),"大学专科",font);
						}else if("4".equals(cp.getSddscpEducation())){
							label = new Label(5,(i+2),"专科以下",font);
						}else{
							label = new Label(5,(i+2),"无",font);
						}		
					}else{
						label = new Label(5,(i+2),"",font);
					}
					sheet.addCell(label);
					//学位
					if(null!=cp.getSddscpDegree()){
						if("1".equals(cp.getSddscpDegree())){
							label = new Label(6,(i+2),"无",font);
						}else if("2".equals(cp.getSddscpDegree())){
							label = new Label(6,(i+2),"学士",font);
						}else if("3".equals(cp.getSddscpDegree())){
							label = new Label(6,(i+2),"硕士",font);
						}else{
							label = new Label(6,(i+2),"博士",font);
						}
					}else{
						label = new Label(6,(i+2),"",font);
					}
					sheet.addCell(label);
					//毕业院校
					label = new Label(7,(i+2),cp.getSddscpGraduate(),font);
					sheet.addCell(label);
					//工作身份
					label = new Label(8,(i+2),cp.getSddscpJobstatus(),font);
					sheet.addCell(label);
					//参加工作时间
					if(cp.getSddscpJoinworktime()!=null){
						label=new Label(9,(i+2),dateFormat.format(cp.getSddscpJoinworktime()),font);
					}else{
						label=new Label(9,(i+2),"",font); 
					}
					sheet.addCell(label);
//					if(cp.getSddscpBirthday()!=null){
//						label = new Label(5,(i+2),dateFormat.format(cp.getSddscpBirthday()),font);
//					}else{
//						label = new Label(5,(i+2),"",font);
//					}
//					sheet.addCell(label);
					if(cp.getSddscpJoinpartydate()!=null){
						label = new Label(10,(i+2),dateFormat.format(cp.getSddscpJoinpartydate()),font);
					}else{
						label = new Label(10,(i+2),"",font);
					}
					sheet.addCell(label);
					//进入支部日期
					if(cp.getSddscpEbranchdate()!=null){
						label = new Label(11,(i+2),dateFormat.format(cp.getSddscpEbranchdate()),font);
					}else{
						label = new Label(11,(i+2),"",font);
					}
					sheet.addCell(label);
					//婚姻状况
					if(null!=cp.getSddscpMatrimonial()){
						if("1".equals(cp.getSddscpMatrimonial())){
							label = new Label(12,(i+2),"未婚",font);
						}else if("2".equals(cp.getSddscpMatrimonial())){
							label = new Label(12,(i+2),"已婚",font);
						}else if("3".equals(cp.getSddscpMatrimonial())){
							label = new Label(12,(i+2),"离婚",font);
						}else if("4".equals(cp.getSddscpMatrimonial())){
							label = new Label(12,(i+2),"再婚",font);
						}
					}else{
						label = new Label(12,(i+2),"",font);
					}
					sheet.addCell(label);
					//工作单位
					label = new Label(13,(i+2),cp.getSddscpWorkplace(),font);
					sheet.addCell(label);
					label = new Label(14,(i+2),cp.getSddscpJobposition(),font);
					sheet.addCell(label);
					String SddscpJgdw=cp.getSddscpJgdw()+"";
					if(cp.getSddscpJgdw()!=null){
						label = new Label(15,(i+2),SddscpJgdw,font);
					}else{
						label = new Label(15,(i+2),"",font);	
					}
					sheet.addCell(label);
					//机关党委职务
					if(null!=cp.getSddscpJgdwjob()){
						label = new Label(16,(i+2),cp.getSddscpJgdwjob(),font);
					}else{
						label = new Label(16,(i+2),"",font);
					}
					sheet.addCell(label);
					//所属党总支
					String SddscpDzz=cp.getSddscpDzz()+"";
					if(null!=cp.getSddscpDzz()){
						label = new Label(17,(i+2),SddscpDzz,font);
					}else{
						label = new Label(17,(i+2),"",font);
					}
					//党总支职位SDDSCP_DZZJOB
					if(cp.getSddscpDzzjob()==null||cp.getSddscpDzzjob().length()<=0){
						label = new Label(18,(i+2),"",font);
					}else{
						label = new Label(18,(i+2),cp.getSddscpDzzjob(),font);
					}
					sheet.addCell(label);
					//SDDSCP_ZB
					if(cp.getSddscpZbname()!=null){
						String SddscpZbname=cp.getSddscpZbname().toString();
						label = new Label(19,(i+2),SddscpZbname,font);
					}else{
						label = new Label(19,(i+2),"",font);
					}
					sheet.addCell(label);
					if(cp.getSddscpZbjob()==null||cp.getSddscpZbjob().length()<=0){
						label = new Label(20,(i+2),"",font);
					}else{
						label = new Label(20,(i+2),cp.getSddscpZbjob(),font);
					}
					sheet.addCell(label);
					//籍贯
					if(cp.getSddscpNative()==null||cp.getSddscpNative().length()<=0){
						label = new Label(21,(i+2),"",font);
					}else{
						label = new Label(21,(i+2),cp.getSddscpNative(),font);
					}
					sheet.addCell(label);
					//户籍所在地
					if(cp.getSddscpResidence()==null||cp.getSddscpResidence().length()<=0){
						label = new Label(22,(i+2),"",font);
					}else{
						label = new Label(22,(i+2),cp.getSddscpResidence(),font);
					}
					sheet.addCell(label);
					//家庭住址
					if(cp.getSddscpAddress()==null||cp.getSddscpAddress().length()<=0){
						label = new Label(23,(i+2),"",font);
					}else{
						label = new Label(23,(i+2),cp.getSddscpAddress(),font);
					}
					sheet.addCell(label);
					//固定电话
					if(cp.getSddscpPhone()==null||cp.getSddscpPhone().length()<=0){
						label = new Label(24,(i+2),"",font);
					}else{
						label = new Label(24,(i+2),cp.getSddscpPhone(),font);
					}
					sheet.addCell(label);
					//手机
					if(cp.getSddscpMobile()==null||cp.getSddscpMobile().length()<=0){
						label = new Label(25,(i+2),"",font);
					}else{
						label = new Label(25,(i+2),cp.getSddscpMobile(),font);
					}
					sheet.addCell(label);
					//邮箱
					if(cp.getEmail()==null||cp.getEmail().length()<=0){
						label = new Label(26,(i+2),"",font);
					}else{
						label = new Label(26,(i+2),cp.getEmail(),font);
					}
					sheet.addCell(label);
				}
				//设置打印区域
				sheet.getSettings().setOrientation(PageOrientation.PORTRAIT) ;// 设置为竖向打印
				sheet.getSettings().setPaperSize(PaperSize.A4) ; // 设置纸张
				sheet.getSettings().setFitHeight(297) ; // 打印区高度
				//设置边距
				sheet.getSettings().setTopMargin(0.5) ;
				sheet.getSettings().setBottomMargin(0.3) ;
				sheet.getSettings().setLeftMargin(0.8);
				sheet.getSettings().setRightMargin(0.1) ;
				//设置页脚
				sheet.getSettings().setFooterMargin(0.07) ;   // 设置页脚边距（下）
				sheet.getSettings().setScaleFactor(85);       //打印缩放比例
				// 导出表格至Response流
				// 导出表格至Response流
				filename = java.net.URLEncoder.encode(filename, "UTF-8");
				response.setHeader("Content-Disposition", "attachment;filename="+ filename + ".xls"); // 设定输出文件
				wwb.write();
				//关闭文件
				wwb.close();
				out.flush();
				out.close();  
				response.flushBuffer();
		 }catch(Exception e){
			 e.printStackTrace();
		 }
	}
	
	
	@RequestMapping("/member/assess.do")
	public String assess(CmsUser user, Integer pageNo,HttpServletRequest request, ModelMap model){
		CmsUser self = CmsUtils.getUser(request);
		CmsDepartment department = new CmsDepartment();
		String orgtype = "";
		Pagination pagination = new Pagination();
		String departId = request.getParameter("departId");
		if(StringUtils.isNotBlank(self.getUsername())){
			if(!("admin".equals(self.getUsername())) || !("省局机关党委".equals(self.getUsername()))){
				if(departId==null || (departId=="")){
					departId = String.valueOf(self.getDepartment().getId());
				}
				department = cmsDeptMng.findById(new Integer(departId));
				if(StringUtils.isNotBlank(department.getSddspoOrgtype())){
					if("工作指导组".equals(department.getSddspoOrgtype())){
						orgtype = "gzzdz";
					}else if("机关党委".equals(department.getSddspoOrgtype())){
						orgtype = "jgdw";
					}else if("党总支".equals(department.getSddspoOrgtype())){
						orgtype = "dzz";
					}else if("支部".equals(department.getSddspoOrgtype())){
						orgtype = "zb";
					}else{}
				}
				pagination = manager.assess(orgtype, new Integer(departId), user,  cpn(pageNo), CookieUtils.getPageSize(request));
			}
			else {
				pagination = manager.assess(orgtype, null, user,  cpn(pageNo), CookieUtils.getPageSize(request));
			}
		}
		model.addAttribute("pagination", pagination);
		model.addAttribute("departId", departId);
		model.addAttribute("user", user);
		return "orglifecalendar/assesslist";
	}
	
	
	@RequestMapping("/member/getAssessData.do")
	@ResponseBody
	public String assessDialogData(Integer userid){
		CmsUser user = manager.findById(userid);
		String username = "";
		String uid = "";
		String sddscpAssess = "";
		String sddscpExcellentcause = "";
		String sddscpUnqualifiedcause = "";
		String sddscpAssessyear = "";
		if(user != null){
			username = user.getUsername();
			uid = user.getId().toString();
			sddscpAssess = user.getSddscpAssess();
			sddscpExcellentcause = user.getSddscpExcellentcause();
			sddscpUnqualifiedcause = user.getSddscpUnqualifiedcause();
			sddscpAssessyear = user.getSddscpAssessyear();
		}
		return username+","+uid+","+sddscpAssess+","+sddscpExcellentcause+","+sddscpUnqualifiedcause+","+sddscpAssessyear;
	}
	
	@RequestMapping("/member/updateAssess.do")
	@ResponseBody
	public String updateAssess(CmsUser user,HttpServletRequest request, ModelMap model){
		String msg = "";
		try {
			manager.updateAssess(user);
			msg = "评议成功";
		} catch (Exception e) {
			msg = "评议失败，请联系管理员";
			// TODO: handle exception
		}
		return msg;
	}
	
	@RequestMapping("/member/uploadMb.do")
	@ResponseBody
	public String uploadMb(String filePath,HttpServletRequest request){
		return request.getSession().getServletContext().getRealPath("/")+filePath;
	}
	
	@RequestMapping("/member/recovery.do")
	@ResponseBody
	public String recovery(Integer id){
		String msg = "";
		try {
			manager.recovery(id);
			msg = "操作成功.";
		} catch (Exception e) {
			// TODO: handle exception
			msg = "操作失败.";
		}
		return msg;
	}
	@RequiresPermissions("member:getInParty")
	@RequestMapping("/member/getInParty.do")
	public String getInParty(Integer pageNo,HttpServletRequest request,ModelMap model,String inType){
		CmsUser user = CmsUtils.getUser(request);
		CmsDepartment depart = user.getDepartment();
		String allDepts = "";
		boolean isShiju = false;
		Integer parentDeptId = null;
		if(depart.getSddspoOrgtype().equals("支部")){
			allDepts = "('"+depart.getId()+"')";
			parentDeptId = depart.getId();
		}else{
			if(depart.getId()!=1){
				parentDeptId = depart.getParent().getId();
				if(depart.getParent().getParent()==null){
					isShiju = true;
				}
				List<CmsDepartment> depts = cmsDeptMng.getAllDeptById(parentDeptId, isShiju);
				if(depts!=null && depts.size()>0){
					allDepts = allDepts + "(";
					for (CmsDepartment cmsDepartment : depts) {
						allDepts = allDepts + "'"+cmsDepartment.getId()+"',";
					}
					allDepts = allDepts.substring(0,allDepts.length()-1)+")";
				}else{
					allDepts = "('"+depart.getId()+"')";
				}
			}
		}
		Pagination pagination = manager.getInPartyUser(cpn(pageNo),
				CookieUtils.getPageSize(request),inType,allDepts);
		model.addAttribute("pagination", pagination);
		model.addAttribute("pageNo",pagination.getPageNo());
		model.addAttribute("inType", inType);
		return "member/list_inParty";
	}
	
	@RequiresPermissions("member:getOutParty")
	@RequestMapping("/member/getOutParty.do")
	public String getOutParty(Integer pageNo,HttpServletRequest request,ModelMap model,String outType){
		CmsUser user = CmsUtils.getUser(request);
		CmsDepartment depart = user.getDepartment();
		String allDepts = "";
		boolean isShiju = false;
		Integer parentDeptId = null;
		if(depart.getSddspoOrgtype().equals("支部")){
			allDepts = "('"+depart.getId()+"')";
			parentDeptId = depart.getId();
		}else{
			if(depart.getId()!=1){
				parentDeptId = depart.getParent().getId();
				if(depart.getParent().getParent()==null){
					isShiju = true;
				}
				List<CmsDepartment> depts = cmsDeptMng.getAllDeptById(parentDeptId, isShiju);
				if(depts!=null && depts.size()>0){
					allDepts = allDepts + "(";
					for (CmsDepartment cmsDepartment : depts) {
						allDepts = allDepts + "'"+cmsDepartment.getId()+"',";
					}
					allDepts = allDepts.substring(0,allDepts.length()-1)+")";
				}else{
					allDepts = "('"+depart.getId()+"')";
				}
			}
		}
		Pagination pagination = manager.getOutPartyUser(cpn(pageNo),
				CookieUtils.getPageSize(request),outType,allDepts);
		model.addAttribute("pagination", pagination);
		model.addAttribute("pageNo",pagination.getPageNo());
		model.addAttribute("outType", outType);
		return "member/list_outParty";
	}
	
	@Autowired
	private CmsDepartmentMng cmsDeptMng;
	@Autowired
	private CmsGroupMng cmsGroupMng;
	@Autowired
	private CmsLogMng cmsLogMng;
	@Autowired
	private CmsUserMng manager;
	@Autowired
	private CmsUserDao userDao;
	@Autowired
	private CmsConfigItemMng cmsConfigItemMng;
	@Autowired
	private CmsWebserviceMng cmsWebserviceMng;
	@Autowired
	private ICoreDictionaryService coreDictionaryService;
	@Autowired
	private DbFileMng dbFileMng;
	@Autowired
	private FileRepository fileRepository;
	@Autowired
	private CmsFileMng fileMng;
	@Autowired
	private CmsFloatingMng floatMng;
}