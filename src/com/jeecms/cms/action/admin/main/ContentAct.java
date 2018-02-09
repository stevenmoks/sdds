package com.jeecms.cms.action.admin.main;

import static com.jeecms.common.page.SimplePage.cpn;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.crypto.JcaCipherService;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;

import com.jeecms.cms.entity.main.Channel;
import com.jeecms.cms.entity.main.CmsModel;
import com.jeecms.cms.entity.main.CmsModelItem;
import com.jeecms.cms.entity.main.CmsTopic;
import com.jeecms.cms.entity.main.Content;
import com.jeecms.cms.entity.main.ContentAttachment;
import com.jeecms.cms.entity.main.ContentCheck;
import com.jeecms.cms.entity.main.ContentDoc;
import com.jeecms.cms.entity.main.ContentExt;
import com.jeecms.cms.entity.main.ContentShareCheck;
import com.jeecms.cms.entity.main.ContentTxt;
import com.jeecms.cms.entity.main.ContentType;
import com.jeecms.cms.entity.main.Content.ContentStatus;
import com.jeecms.cms.entity.main.ContentRecord.ContentOperateType;
import com.jeecms.cms.manager.assist.CmsFileMng;
import com.jeecms.cms.manager.main.ChannelMng;
import com.jeecms.cms.manager.main.CmsModelItemMng;
import com.jeecms.cms.manager.main.CmsModelMng;
import com.jeecms.cms.manager.main.CmsTopicMng;
import com.jeecms.cms.manager.main.ContentMng;
import com.jeecms.cms.manager.main.ContentShareCheckMng;
import com.jeecms.cms.manager.main.ContentTypeMng;
import com.jeecms.cms.service.ImageSvc;
import com.jeecms.cms.staticpage.exception.ContentNotCheckedException;
import com.jeecms.cms.staticpage.exception.GeneratedZeroStaticPageException;
import com.jeecms.cms.staticpage.exception.StaticPageNotOpenException;
import com.jeecms.cms.staticpage.exception.TemplateNotFoundException;
import com.jeecms.cms.staticpage.exception.TemplateParseException;
import com.jeecms.common.image.ImageUtils;
import com.jeecms.common.office.FileUtils;
import com.jeecms.common.office.OpenOfficeConverter;
import com.jeecms.common.page.Pagination;
import com.jeecms.common.upload.FileRepository;
import com.jeecms.common.upload.UploadUtils;
import com.jeecms.common.util.StrUtils;
import com.jeecms.common.web.CookieUtils;
import com.jeecms.common.web.RequestUtils;
import com.jeecms.common.web.ResponseUtils;
import com.jeecms.common.web.springmvc.MessageResolver;
import com.jeecms.common.web.springmvc.RealPathResolver;
import com.jeecms.core.entity.CmsDepartment;
import com.jeecms.core.entity.CmsGroup;
import com.jeecms.core.entity.CmsSite;
import com.jeecms.core.entity.CmsUser;
import com.jeecms.core.entity.CmsWorkflowRecord;
import com.jeecms.core.entity.Ftp;
import com.jeecms.core.manager.CmsGroupMng;
import com.jeecms.core.manager.CmsLogMng;
import com.jeecms.core.manager.CmsSiteMng;
import com.jeecms.core.manager.CmsUserMng;
import com.jeecms.core.manager.CmsWorkflowRecordMng;
import com.jeecms.core.manager.DbFileMng;
import com.jeecms.core.tpl.TplManager;
import com.jeecms.core.web.WebErrors;
import com.jeecms.core.web.util.CmsUtils;
import com.jeecms.core.web.util.CoreUtils;
import com.risen.entity.RisenIntegral;
import com.risen.entity.RisenIntegralRecord;
import com.risen.service.IRisenIntegralRecordService;
import com.risen.service.IRisenIntegralService;

@Controller
public class ContentAct{
	private static final Logger log = LoggerFactory.getLogger(ContentAct.class);

	@RequiresPermissions("content:v_left")
	@RequestMapping("/content/v_left.do")
	public String left(String source, ModelMap model) {
		model.addAttribute("source", source);
		return "content/left";
	}

	/**
	 * 栏目导航
	 * 
	 * @param root
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 */
	@RequiresPermissions("content:v_tree")
	@RequestMapping(value = "/content/v_tree.do")
	public String tree(String root, HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		log.debug("tree path={}", root);
		boolean isRoot;
		// jquery treeview的根请求为root=source
		if (StringUtils.isBlank(root) || "source".equals(root)) {
			isRoot = true;
		} else {
			isRoot = false;
		}
		model.addAttribute("isRoot", isRoot);
		WebErrors errors = validateTree(root, request);
		if (errors.hasErrors()) {
			log.error(errors.getErrors().get(0));
			ResponseUtils.renderJson(response, "[]");
			return null;
		}
		List<Channel> list;
		Integer siteId = CmsUtils.getSiteId(request);
		Integer userId = CmsUtils.getUserId(request);
		CmsUser user=CmsUtils.getUser(request);
		if(user.getUserSite(siteId).getAllChannel()){
			if (isRoot) {
				list = channelMng.getTopList( siteId, true);
			} else {
				list = channelMng.getChildList(Integer.parseInt(root), true);
			}
		}else{
			Integer departId=null;
			CmsDepartment userDepartment=CmsUtils.getUser(request).getDepartment();
			if(userDepartment!=null){
				departId=userDepartment.getId();
			}
			if (isRoot) {
				list = channelMng.getTopListForDepartId(departId,userId,siteId,true);
			} else {
				list = channelMng.getChildListByDepartId(departId,siteId, Integer
						.parseInt(root), true);
			}
		}
		
		model.addAttribute("list", list);
		response.setHeader("Cache-Control", "no-cache");
		response.setContentType("text/json;charset=UTF-8");
		return "content/tree";
	}

	/**
	 * 副栏目树
	 * 
	 * @param root
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 */
	@RequiresPermissions("content:v_tree_channels")
	@RequestMapping(value = "/content/v_tree_channels.do")
	public String treeChannels(String root, HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		tree(root, request, response, model);
		return "content/tree_channels";
	}

	@RequiresPermissions("content:v_list")
	@RequestMapping("/content/v_list.do")
	public String list(String queryStatus, Integer queryTypeId,
			Boolean queryTopLevel, Boolean queryRecommend,
			Integer queryOrderBy, Integer cid, Integer pageNo,
			HttpServletRequest request, ModelMap model) {
		long time = System.currentTimeMillis();
		String queryTitle = RequestUtils.getQueryParam(request, "queryTitle");
		queryTitle = StringUtils.trim(queryTitle);
		String queryInputUsername = RequestUtils.getQueryParam(request,
				"queryInputUsername");
		queryInputUsername = StringUtils.trim(queryInputUsername);
		if (queryTopLevel == null) {
			queryTopLevel = false;
		}
		if (queryRecommend == null) {
			queryRecommend = false;
		}
		if (queryOrderBy == null) {
			queryOrderBy = 4;
		}
		ContentStatus status;
		if (!StringUtils.isBlank(queryStatus)) {
			status = ContentStatus.valueOf(queryStatus);
		} else {
			status = ContentStatus.all;
		}
		Integer queryInputUserId = null;
		if (!StringUtils.isBlank(queryInputUsername)) {
			CmsUser u = cmsUserMng.findByUsername(queryInputUsername);
			if (u != null) {
				queryInputUserId = u.getId();
			} else {
				// 用户名不存在，清空。
				//queryInputUsername = null;
				queryInputUserId=null;
			}
		}else{
			queryInputUserId=0;
		}
		CmsSite site = CmsUtils.getSite(request);
		Integer siteId = site.getId();
		CmsUser user = CmsUtils.getUser(request);
		Integer userId = user.getId();
		byte currStep = user.getCheckStep(siteId);
		Channel channel = channelMng.getChannelByDepts(user.getDepartment().getId());
		Integer channelId = channel.getId();
		boolean isSendToOut = false;
		if(!user.getUsername().equals("省局机关党委")&&!(user.getUsername().equals("admin"))){
			if(cid!=null){
				if(channelMng.findById(cid).getDepartId()!=null){
					cid = channelId;
				}
			}else{
				cid = channelId;
			}
		}
		if(cid!=null){
			String nowChannelId = String.valueOf(cid);
			String wantsToSendOutIds = ",290,10354,10355,10356,10357,34243,292,293,575,589,35283,591,600,603,604,606,";
			wantsToSendOutIds += "609,10334,10335,621,622,35003,629,3513,632,3535,";
			if(wantsToSendOutIds.indexOf(","+nowChannelId+",")>-1){
				isSendToOut = true;
			}
		}
		Pagination p = manager.getPageByRight(queryTitle, queryTypeId,user.getId(),
				queryInputUserId, queryTopLevel, queryRecommend, status, user
						.getCheckStep(siteId), siteId, cid, userId,
				queryOrderBy, cpn(pageNo), CookieUtils.getPageSize(request));
		List<ContentType> typeList = contentTypeMng.getList(true);
		List<CmsModel>models=cmsModelMng.getList(false, true,siteId);
		if(cid!=null){
			Channel c=channelMng.findById(cid);
			models=c.getModels(models);
			model.addAttribute("c", c);
		}
		CmsDepartment dept = CmsUtils.getUser(request).getDepartment();
		int deptid = dept.getId();
		List<Content> modellist = (List<Content>)p.getList();
		for (Content content : modellist) {
			Set<CmsDepartment> set = content.getChannel().getDepartments();
			Iterator<CmsDepartment> ito = set==null?null:set.iterator();
			Boolean isDepartment = false;
			while (ito==null?false:ito.hasNext()) {
				int deptold = ito.next().getId();
				if(deptid == deptold){
					isDepartment = true;
				}
			}
			content.setIsDeptment(isDepartment.toString());
		}
		p.setList(modellist);
		Integer [] cidarr = {609,10334,10335,3854,3945,4099,4241,4377,4505,4571,4770,4878,4946,13648,17062,27702,23659,14263,32093};
		String istzgg = "false";
		if(cid!=null && cid != 0){
			for(int i=0; i<cidarr.length; i++){
				if(cid.equals(cidarr[i])){
					istzgg = "true";
				}
			}
		}
		model.addAttribute("istzgg",istzgg);
		model.addAttribute("deptid",deptid);
		model.addAttribute("pagination", p);
		model.addAttribute("cid", cid);
		model.addAttribute("isSendToOut", isSendToOut);
		model.addAttribute("typeList", typeList);
		model.addAttribute("currStep", currStep);
		model.addAttribute("site", site);
		model.addAttribute("models", models);
		addAttibuteForQuery(model, queryTitle, queryInputUsername, queryStatus,
				queryTypeId, queryTopLevel, queryRecommend, queryOrderBy,
				pageNo);
		time = System.currentTimeMillis() - time;
		return "content/list";
	}
	@RequiresPermissions("content:shareContent")
	@RequestMapping("/content/shareContent.do")
	@ResponseBody
	public Object shareContent(Integer[] contentId,HttpServletRequest request){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
		for (Integer i : contentId) {
			Content cot=manager.findById(i);
			List<RisenIntegralRecord> c=irService.findByContentId(cot.getId());
			/*if(!org.springframework.util.StringUtils.isEmpty(c)){
				return cot.getId();
			}*/
			if(c.size() == 0){
				return cot.getId();
			}
			CmsUser user=CmsUtils.getUser(request);
			RisenIntegral it=integralService.findByOrgId(user.getDepartment().getId());
			if(org.springframework.util.StringUtils.isEmpty(it)){//如果积分表没有记录就新增一条
					it=new RisenIntegral();
					it.setRisenitBase(1);
					it.setRisenitOrgid(user.getDepartment().getId());
					it.setRisenitOrgname(user.getDepartment().getName());
					it.setRisenitYear(new Integer(sdf.format(new Date())));
					it.setRisenitDesc("");
					it.setRisenitScore(new Double(0));
					integralService.save(it);
			}
			
			RisenIntegralRecord record=new RisenIntegralRecord();
			record.setRisenirChannel(cot.getChannel().getName());
			record.setRisenirContentid(cot.getId());
			record.setRisenirContent(cot.getContentExt().getTitle());
			record.setRisenirContenturl(cot.getUrl());
			record.setRisenirOrgid(user.getDepartment().getId());
			record.setRisenirOrgname(user.getDepartment().getName());
			record.setRisenirTargetorgid(user.getDepartment().getParent().getId());
			record.setRisenirResult(0);
			record.setRisenirScore(new Double(0));
			record.setRisenirHandledate(new Date());
			record.setRisenirContenttype(cot.getType().getName());
			try {
				irService.save(record);
			} catch (Exception e) {
				e.printStackTrace();
				return 0;
			}
		}
		
		return 1;
	}
	
	@RequiresPermissions("content:v_add")
	@RequestMapping("/content/v_add.do")
	public String add(Integer cid,Integer modelId, HttpServletRequest request,ModelMap model) {
		WebErrors errors = validateAdd(cid,modelId, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		CmsSite site = CmsUtils.getSite(request);
		Integer siteId = site.getId();
		CmsUser user = CmsUtils.getUser(request);
		Integer userId = user.getId();
		// 栏目
		Channel c;
		if (cid != null) {
			c = channelMng.findById(cid);
		} else {
			c = null;
		}
		// 模型
		CmsModel m;
		if(modelId==null){
			if (c != null) {
				m = c.getModel();
			} else {
				m = cmsModelMng.getDefModel();
				// TODO m==null给出错误提示
				if (m == null) {
					throw new RuntimeException("default model not found!");
				}
			}
		}else{
			m=cmsModelMng.findById(modelId);
		}
		// 模型项列表
		List<CmsModelItem> itemList = cmsModelItemMng.getList(m.getId(), false,
				false);
		// 栏目列表
		List<Channel> channelList;
		Set<Channel> rights;
		if (user.getUserSite(siteId).getAllChannel()) {
			// 拥有所有栏目权限
			rights = null;
		} else {
//			rights = user.getChannels(siteId);
			//更改成部门获取栏目权限
			rights = user.getChannelsByDepartment(siteId);
		}
		if (c != null) {
			channelList = c.getListForSelect(rights, true);
		} else {
//			List<Channel> topList = channelMng.getTopListByRigth(userId,siteId, true);
			//更改成部门获取栏目权限
			Integer departId=null;
			if(user.getDepartment()!=null){
				departId=user.getDepartment().getId();
			}
			List<Channel> topList = channelMng.getTopListForDepartId(departId,userId,siteId,true);
			channelList = Channel.getListForSelect(topList, rights, true);
		}

		// 专题列表
		List<CmsTopic> topicList;
		if (c != null) {
			topicList = cmsTopicMng.getListByChannel(c.getId());
		} else {
			topicList = new ArrayList<CmsTopic>();
		}
		// 内容模板列表
		List<String> tplList = getTplContent(site, m, null);
		// 内容手机模板列表
		List<String> tplMobileList = getTplMobileContent(site, m, null);
		// 会员组列表
		List<CmsGroup> groupList = cmsGroupMng.getList();
		// 内容类型
		List<ContentType> typeList = contentTypeMng.getList(false);
		model.addAttribute("site",CmsUtils.getSite(request));
		model.addAttribute("model", m);
		model.addAttribute("itemList", itemList);
		model.addAttribute("channelList", channelList);
		model.addAttribute("topicList", topicList);
		model.addAttribute("tplList", tplList);
		model.addAttribute("tplMobileList", tplMobileList);
		model.addAttribute("groupList", groupList);
		model.addAttribute("typeList", typeList);
		if (cid != null) {
			model.addAttribute("cid", cid);
		}
		if (c != null) {
			model.addAttribute("channel", c);
		}
		CmsUser loginUser = CmsUtils.getUser(request);
		if(loginUser.getUsername().equals("admin")){
			model.addAttribute("user","");
		}else{
			model.addAttribute("user",loginUser.getUsername());
		}
		model.addAttribute("sessionId",request.getSession().getId());
		return "content/add";
	}

	@RequiresPermissions("content:v_view")
	@RequestMapping("/content/v_view.do")
	public String view(String queryStatus, Integer queryTypeId,
			Boolean queryTopLevel, Boolean queryRecommend,
			Integer queryOrderBy, Integer pageNo, Integer cid, Integer id,
			HttpServletRequest request, ModelMap model) {
		CmsSite site = CmsUtils.getSite(request);
		CmsUser user = CmsUtils.getUser(request);
		byte currStep = user.getCheckStep(site.getId());
		Content content = manager.findById(id);

		model.addAttribute("content", content);
		model.addAttribute("currStep", currStep);
		model.addAttribute("site", site);
		if (cid != null) {
			model.addAttribute("cid", cid);
		}
		String queryTitle = RequestUtils.getQueryParam(request, "queryTitle");
		String queryInputUsername = RequestUtils.getQueryParam(request,
				"queryInputUsername");
		addAttibuteForQuery(model, queryTitle, queryInputUsername, queryStatus,
				queryTypeId, queryTopLevel, queryRecommend, queryOrderBy,
				pageNo);
		return "content/view";
	}

	@RequiresPermissions("content:v_edit")
	@RequestMapping("/content/v_edit.do")
	public String edit(String queryStatus, Integer queryTypeId, String addr,
			Boolean queryTopLevel, Boolean queryRecommend,
			Integer queryOrderBy, Integer pageNo, Integer cid, Integer id, String listtype,
			HttpServletRequest request, ModelMap model) {
		WebErrors errors = validateEdit(id, request);
		errors.getErrors();
		for (String error : errors.getErrors()) {
			if(!error.contains("非本站数据")){
				if (errors.hasErrors()) {
					return errors.showErrorPage(model);
				}
			}
		}
		
		CmsSite site = CmsUtils.getSite(request);
		Integer siteId = site.getId();
		CmsUser user = CmsUtils.getUser(request);
		// 内容
		Content content = manager.findById(id);
		// 栏目
		Channel channel = content.getChannel();
		// 模型
		CmsModel m=content.getModel();
		// 模型项列表
		List<CmsModelItem> itemList = cmsModelItemMng.getList(m.getId(), false,
				false);
		// 栏目列表
		Set<Channel> rights;
		if (user.getUserSite(siteId).getAllChannel()) {
			// 拥有所有栏目权限
			rights = null;
		} else {
//			rights = user.getChannels(siteId);
			//更改成部门获取栏目权限
			rights = user.getChannelsByDepartment(siteId);
		}

		List<Channel> topList = channelMng.getTopList(site.getId(), true);
		List<Channel> channelList = Channel.getListForSelect(topList, rights,
				true);

		// 专题列表
		List<CmsTopic> topicList = cmsTopicMng
				.getListByChannel(channel.getId());
		Set<CmsTopic> topics = content.getTopics();
		for (CmsTopic t : topics) {
			if (!topicList.contains(t)) {
				topicList.add(t);
			}
		}
		Integer[] topicIds = CmsTopic.fetchIds(content.getTopics());
		// 内容模板列表
		List<String> tplList = getTplContent(site, m, content.getTplContent());
		// 内容手机模板列表
		List<String> tplMobileList = getTplMobileContent(site, m, null);
		// 会员组列表
		List<CmsGroup> groupList = cmsGroupMng.getList();
		Integer[] groupIds = CmsGroup.fetchIds(content.getViewGroups());
		// 内容类型
		List<ContentType> typeList = contentTypeMng.getList(false);
		// 当前模板，去除基本路径
		int tplPathLength = site.getTplPath().length();
		String tplContent = content.getTplContent();
		if (!StringUtils.isBlank(tplContent)) {
			tplContent = tplContent.substring(tplPathLength);
		}
		String tplMobileContent = content.getMobileTplContent();
		if (!StringUtils.isBlank(tplMobileContent)) {
			tplMobileContent = tplMobileContent.substring(tplPathLength);
		}
		model.addAttribute("addr", addr);
		model.addAttribute("listtype",listtype);
		model.addAttribute("site",CmsUtils.getSite(request));
		model.addAttribute("content", content);
		model.addAttribute("channel", channel);
		model.addAttribute("model", m);
		model.addAttribute("itemList", itemList);
		model.addAttribute("channelList", channelList);
		model.addAttribute("topicList", topicList);
		model.addAttribute("topicIds", topicIds);
		model.addAttribute("tplList", tplList);
		model.addAttribute("tplMobileList", tplMobileList);
		model.addAttribute("groupList", groupList);
		model.addAttribute("groupIds", groupIds);
		model.addAttribute("typeList", typeList);
		model.addAttribute("tplContent", tplContent);
		model.addAttribute("tplMobileContent", tplMobileContent);
		model.addAttribute("cid", channel.getId());
		model.addAttribute("cname",channel.getChannelExt().getName());
		String queryTitle = RequestUtils.getQueryParam(request, "queryTitle");
		String queryInputUsername = RequestUtils.getQueryParam(request,
				"queryInputUsername");
		addAttibuteForQuery(model, queryTitle, queryInputUsername, queryStatus,
				queryTypeId, queryTopLevel, queryRecommend, queryOrderBy,
				pageNo);
		model.addAttribute("sessionId",request.getSession().getId());
		return "content/edit";
	}


	@RequiresPermissions("content:o_save")
	@RequestMapping("/content/o_save.do")
	public String save(Content bean, ContentExt ext, ContentTxt txt,ContentDoc doc,
			Boolean copyimg,Integer[] channelIds, Integer[] topicIds, Integer[] viewGroupIds,
			String[] attachmentPaths, String[] attachmentNames,
			String[] attachmentFilenames, String[] picPaths, String[] picDescs,
			Integer channelId, Integer typeId, String tagStr, Boolean draft,
			Integer cid, Integer modelId,Short charge,Double chargeAmount,
			HttpServletRequest request,HttpServletResponse response, ModelMap model) {
		WebErrors errors = validateSave(bean, channelId, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		// 加上模板前缀
		CmsSite site = CmsUtils.getSite(request);
		CmsUser user = CmsUtils.getUser(request);
		String tplPath = site.getTplPath();
		if (!StringUtils.isBlank(ext.getTplContent())) {
			ext.setTplContent(tplPath + ext.getTplContent());
		}
		if (!StringUtils.isBlank(ext.getTplMobileContent())) {
			ext.setTplMobileContent(tplPath + ext.getTplMobileContent());
		}
		bean.setAttr(RequestUtils.getRequestMap(request, "attr_"));
		String[] tagArr = StrUtils.splitAndTrim(tagStr, ",", MessageResolver
				.getMessage(request, "content.tagStr.split"));
		if(txt!=null&&copyimg!=null&&copyimg){
			txt=copyContentTxtImg(txt, site);
		}
		try{
			bean = manager.save(bean, ext, txt, doc,channelIds, topicIds, viewGroupIds,
					tagArr, attachmentPaths, attachmentNames, attachmentFilenames,
					picPaths, picDescs, channelId, typeId, draft,false,
					charge,chargeAmount, user, false);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		//积分处理
		//record(channelId,user);
		//处理附件
		fileMng.updateFileByPaths(attachmentPaths,picPaths,ext.getMediaPath(),ext.getTitleImg(),ext.getTypeImg(),ext.getContentImg(),true,bean);
		log.info("save Content id={}", bean.getId());
		cmsLogMng.operating(request, "content.log.save", "id=" + bean.getId()
				+ ";title=" + bean.getTitle());
		/*
		if (cid != null) {
			model.addAttribute("cid", cid);
		}
		model.addAttribute("message", "global.success");
		return add(cid,modelId, request, model);
		*/
		if(cid==null){
			cid = bean.getChannel().getId(); 
		}
		return "redirect:../content/v_list.do?cid="+cid;
	}
	
	//老网站数据导入
		@RequiresPermissions("content:o_saveOld")
		@RequestMapping("/content/o_saveOld.do")
		public String saveOld(String old_id,String new_id,Integer row,String site_id,
				HttpServletRequest request,HttpServletResponse response, ModelMap model){
			/*
			 * 建立老网站连接池 begin
			 */
			String driver = "oracle.jdbc.driver.OracleDriver";
			String url = "jdbc:oracle:thin:@140.12.96.103:1521:orcl";
			ResultSet rs = null;
			ResultSet rs1 = null;
			Statement stm = null;
			Connection conn = null;
			int cc = 0;
			try {
				Class.forName(driver);
				conn = DriverManager.getConnection(url, "sddj","sddj");
				stm = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
				//ResultSet rs = stm.executeQuery("select t.vc_content,t.vc_attach,t.c_deploytime,t.vc_pic from jcms_info t where t.i_webid = 60 and t.i_cataid =" + old_id + " and t.c_deploytime >= '2016-1-1' order by t.c_deploytime asc");
				rs = stm.executeQuery("select t.vc_content,t.vc_attach,t.c_deploytime,t.vc_pic from jcms_info t where t.i_webid = 60 and t.i_cataid =" + old_id + " order by t.c_deploytime asc");
				
				Statement stm1 = conn.createStatement();
				//ResultSet rs1 = stm1.executeQuery("select count(*) as rowCount from jcms_info t where t.i_webid = 60 and t.i_cataid =" + old_id +" and t.c_deploytime >= '2016-1-1'");
				rs1 = stm1.executeQuery("select count(*) as rowCount from jcms_info t where t.i_webid = 60 and t.i_cataid =" + old_id);
				rs1.next();
            cc = rs1.getInt("rowCount");
            System.out.println("=====共有" + cc + "条数据=====");
            System.out.println("=====现在开始执行导入=====");
			} catch (Exception e) {
				e.printStackTrace();
			}
			/*
			 * End
			 */
			
			/*
			 * 循环遍历取出的list集合
			 */
			int i = 0;
			if(row != null){
				try {
					rs.absolute(row);
				} catch (SQLException e) {
					e.printStackTrace();
				}
				i = row;
			}
			try {
			while(rs.next()){
				/*
				 * 定义参数  并将取出来的值set到参数中
				 * 
				 */
				Content bean = new Content();
				ContentTxt txt = new ContentTxt();
				ContentExt ext = new ContentExt();
				
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				File file=new File("/weblogic/info/" + rs.getString(1));
				if(!file.exists()){
					i++;
					System.out.println("第" + i + "-----导入失败,没找到文件/weblogic/info/" + rs.getString(1));
					continue;
				}
				Document document = db.parse(file);
				
				//内容
				String text = document.getElementsByTagName("documentroot").item(0).getChildNodes().item(19).getTextContent().replace("<content>", "").replace("</content>", "");
				txt.setTxt(text);//set内容
				
				//set发布时间
				SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				if(rs.getString(3) != null && !rs.getString(3).equals("")){
					bean.setSortDate(formater.parse(rs.getString(3)));
					ext.setReleaseDate(formater.parse(rs.getString(3)));
				}
				
				String title = document.getElementsByTagName("documentroot").item(0).getChildNodes().item(1).getTextContent().replace("<title>", "").replace("</title>", "");
				ext.setTitle(title);//set标题
				//作者
				String author = document.getElementsByTagName("documentroot").item(0).getChildNodes().item(13).getTextContent().replace("<editor>", "").replace("</editor>", "");
				ext.setAuthor(author);
				
				//摘要
				String des = document.getElementsByTagName("documentroot").item(0).getChildNodes().item(17).getTextContent().replace("<describe>", "").replace("</describe>", "");
				ext.setDescription(des);
				
					
				String[] attachmentPaths = {""};
				String[] attachmentNames = {""};
				String[] attachmentFilenames = {""};
				if(rs.getString(2) != null && !rs.getString(2).equals("")){
					attachmentPaths[0] = rs.getString(2);
				}
				Integer typeId = 1;
				if(rs.getString(4) != null && !rs.getString(4).equals("")){
					typeId = 2;
					ext.setTypeImg(rs.getString(4));
				}
				CmsUser user = new CmsUser();
				user = cmsUserMng.findByUsername("admin");
				bean.setAttr(RequestUtils.getRequestMap(request, "attr_"));
				CmsSite site = siteMng.findById(Integer.valueOf(site_id));
				bean.setSite(site);
				bean.setModel(cmsModelMng.findById(1));
				bean.setRecommend(false);
				/*
				 * set参数End
				 */
				
				/*
				 * 调用CMS自带的content数据保存的接口
				 */
				
				bean = manager.save(bean, ext, txt, null,null, null, null,
						null, attachmentPaths, attachmentNames, attachmentFilenames,
						null, null, Integer.valueOf(new_id), typeId, null ,false,
						null ,null, user, false);
				/*
				 * 数据保存完毕
				 */
				++i;
				System.out.println("第" + i + "-----导入成功,标题：" + title);
			}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				stm.close();
				rs.close();
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			model.addAttribute("cc", cc);
			model.addAttribute("i",i);
			return "content/over";
	}

	@RequiresPermissions("content:o_update")
	@RequestMapping("/content/o_update.do")
	public String update(String queryStatus, Integer queryTypeId,
			Boolean queryTopLevel, Boolean queryRecommend, Integer oldChannelId,
			Integer queryOrderBy, Content bean, ContentExt ext, ContentTxt txt,ContentDoc doc,
			Boolean copyimg,Integer[] channelIds, Integer[] topicIds, Integer[] viewGroupIds,
			String[] attachmentPaths, String[] attachmentNames,
			String[] attachmentFilenames, String[] picPaths,String[] picDescs,
			Integer channelId, Integer typeId, String tagStr, Boolean draft,
			Integer cid,String[]oldattachmentPaths,String[] oldpicPaths,
			String oldTitleImg,String oldContentImg,String oldTypeImg,
			Short charge,Double chargeAmount, String listtype,
			Integer pageNo, HttpServletRequest request,
			ModelMap model) {
		WebErrors errors = validateUpdate(bean.getId(), request);
		errors.getErrors();
		for (String error : errors.getErrors()) {
			if(!error.contains("非本站数据")){
				if (errors.hasErrors()) {
					return errors.showErrorPage(model);
				}
			}
		}
		if (oldChannelId!=null) {
			channelId = oldChannelId;
		}
		// 加上模板前缀
		CmsSite site = CmsUtils.getSite(request);
		CmsUser user = CmsUtils.getUser(request);
		String tplPath = site.getTplPath();
		if (!StringUtils.isBlank(ext.getTplContent())) {
			ext.setTplContent(tplPath + ext.getTplContent());
		}
		if (!StringUtils.isBlank(ext.getTplMobileContent())) {
			ext.setTplMobileContent(tplPath + ext.getTplMobileContent());
		}
		String[] tagArr = StrUtils.splitAndTrim(tagStr, ",", MessageResolver
				.getMessage(request, "content.tagStr.split"));
		Map<String, String> attr = RequestUtils.getRequestMap(request, "attr_");
		if(txt!=null&&copyimg!=null&&copyimg){
			txt=copyContentTxtImg(txt, site);
		}
		bean = manager.update(bean, ext, txt,doc, tagArr, channelIds, topicIds,
				viewGroupIds, attachmentPaths, attachmentNames,
				attachmentFilenames, picPaths, picDescs, attr, channelId,
				typeId, draft, charge,chargeAmount,user, false);
		//处理之前的附件有效性
		fileMng.updateFileByPaths(oldattachmentPaths,oldpicPaths,null,oldTitleImg,oldTypeImg,oldContentImg,false,bean);
		//处理更新后的附件有效性
		fileMng.updateFileByPaths(attachmentPaths,picPaths,ext.getMediaPath(),ext.getTitleImg(),ext.getTypeImg(),ext.getContentImg(),true,bean);
		log.info("update Content id={}.", bean.getId());
		cmsLogMng.operating(request, "content.log.update", "id=" + bean.getId()
				+ ";title=" + bean.getTitle());
		if("share".equals(listtype)){
			RisenIntegralRecord bean2 = new RisenIntegralRecord();
			bean2.setRisenirTargetorgid(CmsUtils.getUser(request).getDepartment().getId());
			bean2.setRisenirResult(0);
			Pagination pagination = recordService.getPage(cpn(pageNo), CookieUtils
					.getPageSize(request),bean2);
			model.addAttribute("pagination",pagination);
			model.addAttribute("pageNo",pagination.getPageNo());
			
			return "shareContent/list";
		}
		return list(queryStatus, queryTypeId, queryTopLevel, queryRecommend,
				queryOrderBy, cid, pageNo, request, model);
	}
	
	@RequiresPermissions("content:v_share_edit")
	@RequestMapping("/content/v_share_edit.do")
	public String updateShareContent(String queryStatus, Integer queryTypeId, String addr,
			Boolean queryTopLevel, Boolean queryRecommend,
			Integer queryOrderBy, Integer pageNo, Integer cid, String listtype,
			HttpServletRequest request, ModelMap model){
		Integer id = Integer.parseInt(request.getParameter("contentId"));
		
		CmsSite site = CmsUtils.getSite(request);
		Integer siteId = site.getId();
		CmsUser user = CmsUtils.getUser(request);
		// 内容
		Content content = manager.findById(id);
		// 栏目
		Channel channel = content.getChannel();
		String channelName = channel.getName();
		// 模型
		CmsModel m=content.getModel();
		// 模型项列表
		List<CmsModelItem> itemList = cmsModelItemMng.getList(m.getId(), false,
				false);
		// 栏目列表
		Set<Channel> rights;
		if (user.getUserSite(siteId).getAllChannel()) {
			// 拥有所有栏目权限
			rights = null;
		} else {
//			rights = user.getChannels(siteId);
			//更改成部门获取栏目权限
			rights = user.getChannelsByDepartment(siteId);
		}

		List<Channel> topList = channelMng.getTopList(site.getId(), true);
		List<Channel> channelList = Channel.getListForSelect(topList, rights,
				true);

		// 专题列表
		List<CmsTopic> topicList = cmsTopicMng
				.getListByChannel(channel.getId());
		Set<CmsTopic> topics = content.getTopics();
		for (CmsTopic t : topics) {
			if (!topicList.contains(t)) {
				topicList.add(t);
			}
		}
		Integer[] topicIds = CmsTopic.fetchIds(content.getTopics());
		// 内容模板列表
		List<String> tplList = getTplContent(site, m, content.getTplContent());
		// 内容手机模板列表
		List<String> tplMobileList = getTplMobileContent(site, m, null);
		// 会员组列表
		List<CmsGroup> groupList = cmsGroupMng.getList();
		Integer[] groupIds = CmsGroup.fetchIds(content.getViewGroups());
		// 内容类型
		List<ContentType> typeList = contentTypeMng.getList(false);
		// 当前模板，去除基本路径
		int tplPathLength = site.getTplPath().length();
		String tplContent = content.getTplContent();
		if (!org.apache.commons.lang.StringUtils.isBlank(tplContent)) {
			tplContent = tplContent.substring(tplPathLength);
		}
		String tplMobileContent = content.getMobileTplContent();
		if (!org.apache.commons.lang.StringUtils.isBlank(tplMobileContent)) {
			tplMobileContent = tplMobileContent.substring(tplPathLength);
		}
		model.addAttribute("addr", addr);
		model.addAttribute("listtype",listtype);
		model.addAttribute("site",CmsUtils.getSite(request));
		model.addAttribute("content", content);
		model.addAttribute("contentId", content.getId());
		model.addAttribute("channel", channel);
		model.addAttribute("channelId", channel.getId());
		model.addAttribute("channelName", channelName);
		model.addAttribute("model", m);
		model.addAttribute("itemList", itemList);
		model.addAttribute("channelList", channelList);
		model.addAttribute("topicList", topicList);
		model.addAttribute("topicIds", topicIds);
		model.addAttribute("tplList", tplList);
		model.addAttribute("tplMobileList", tplMobileList);
		model.addAttribute("groupList", groupList);
		model.addAttribute("groupIds", groupIds);
		model.addAttribute("typeList", typeList);
		model.addAttribute("tplContent", tplContent);
		model.addAttribute("tplMobileContent", tplMobileContent);
		if (cid != null) {
			model.addAttribute("cid", cid);
		}

		String queryTitle = RequestUtils.getQueryParam(request, "queryTitle");
		String queryInputUsername = RequestUtils.getQueryParam(request,
				"queryInputUsername");
		addAttibuteForQuery(model, queryTitle, queryInputUsername, queryStatus,
				queryTypeId, queryTopLevel, queryRecommend, queryOrderBy,
				pageNo);
		model.addAttribute("sessionId",request.getSession().getId());
		return "content/share_con_edit";
	}
	
	@RequiresPermissions("content:affirm_update")
	@RequestMapping(value = "/content/affirm_update.do")
	public String shareUpdate(String queryStatus, Integer queryTypeId,
			Boolean queryTopLevel, Boolean queryRecommend, Integer oldChannelId,
			Integer queryOrderBy, Content bean, ContentExt ext, ContentTxt txt,ContentDoc doc,
			Boolean copyimg,Integer[] channelIds, Integer[] topicIds, Integer[] viewGroupIds,
			String[] attachmentPaths, String[] attachmentNames,
			String[] attachmentFilenames, String[] picPaths,String[] picDescs,
			Integer channelId, Integer typeId, String tagStr, Boolean draft,
			Integer cid,String[]oldattachmentPaths,String[] oldpicPaths,
			String oldTitleImg,String oldContentImg,String oldTypeImg,
			Short charge,Double chargeAmount, String listtype,
			Integer pageNo, HttpServletRequest request,
			ModelMap model){
		/**
		 * title:标题
		 * description:摘要
		 * author:作者
		 * topLevel:固定级别
		 * topLevelDate:固定到期日期
		 * sortDate:排序时间
		 * releaseDate:发布时间
		 * pigeonholeDate:归档日期
		 * typeId:内容类型id
		 * recommend:是否选择推荐
		 * 
		 * draft:是否选择草稿
		 * attachmentPaths:附件地址数组
		 * attachmentNames:附件名称数组
		 * mediaPath:多媒体地址
		 * mediaType:多媒体类型(文件格式)
		 * txt:内容
		 * copyimg:内容附加属性(是否需要附加属性)
		 * picPaths:图片地址数组
		 * picDescs:图片描述数组
		 */
		String[] tagArr = StrUtils.splitAndTrim(tagStr, ",", MessageResolver
				.getMessage(request, "content.tagStr.split"));
		Map<String, String> attr = RequestUtils.getRequestMap(request, "attr_");
		CmsUser user = CmsUtils.getUser(request);
		
		manager.update(bean, ext, txt,doc, tagArr, channelIds, topicIds,
				viewGroupIds, attachmentPaths, attachmentNames,
				attachmentFilenames, picPaths, picDescs, attr, channelId,
				typeId, draft, charge,chargeAmount,user, false);
		
		
		//改变RISEN_INTEGRALRECORD表中的部分数据
		StringBuffer contentTitle = new StringBuffer(ext.getTitle());
		List<RisenIntegralRecord> risenIntegralRecordList = recordService.findByContentId(bean.getId());
		for(RisenIntegralRecord risenIntegralRecord : risenIntegralRecordList){
			risenIntegralRecord.setRisenirContent(contentTitle+"");
			recordService.update(risenIntegralRecord);
		}
		
		if("share".equals(listtype)){
			return "redirect:../risenReject/v_list.do";
		}else{
			return list(queryStatus, queryTypeId, queryTopLevel, queryRecommend,
									queryOrderBy, cid, pageNo, request, model);
		}
	}


	@RequiresPermissions("content:o_delete")
	@RequestMapping("/content/o_delete.do")
	public String delete(String queryStatus, Integer queryTypeId,
			Boolean queryTopLevel, Boolean queryRecommend,
			String originIds, String shareIds,
			Integer queryOrderBy, Integer cid, Integer pageNo,
			HttpServletRequest request, ModelMap model) {
		CmsSite site = CmsUtils.getSite(request);
		
		/*WebErrors errors = validateDelete(ids, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}*/
		
		if (originIds != null && !originIds.equals("")) {
			List<Integer> originIdList = new LinkedList<Integer>(); //源数据id集合
			if (originIds.contains(",")) {
				String[] originStr = originIds.split(",");
				for (int i = 0; i < originStr.length; i++) {
					originIdList.add(Integer.valueOf(originStr[i]));
				}
			}else{
				originIdList.add(Integer.parseInt(originIds));
			}
			Integer[] originIdIntegers = originIdList.toArray(new Integer[0]);
			
			deleteOrigin(site, originIdIntegers, request);
		}
		
		if (shareIds != null && !shareIds.equals("")) {
			List<Integer> shareIdList = new LinkedList<Integer>();	//共享数据id集合
			if (shareIds.contains(",")) {
				String[] shareIdStr = shareIds.split(",");
				for (int i = 0; i < shareIdStr.length; i++) {
					shareIdList.add(Integer.valueOf(shareIdStr[i]));
				}
			}else {
				shareIdList.add(Integer.parseInt(shareIds));
			}
			Integer[] shareIdIntegers = shareIdList.toArray(new Integer[0]);
			
			/**
			 * 第一种情况cid不为空(从指定的栏目进入了列表)，直接根据cid和contentId删除sharecheck
			 * 第二种情况cid为空
			 */
			
			deleteShare(site, shareIdIntegers, cid, request);
		}
		
		return list(queryStatus, queryTypeId, queryTopLevel, queryRecommend,
				queryOrderBy, cid, pageNo, request, model);
	}
	
	public void deleteOrigin(CmsSite site, Integer[] originIdIntegers, HttpServletRequest request){
		Content[] beans;
		// 是否开启回收站
		if (site.getResycleOn()) {
			manager.deleteShares(originIdIntegers);
			beans = manager.cycle(CmsUtils.getUser(request),originIdIntegers);
			for (Content bean : beans) {
				log.info("delete to cycle, Content id={}", bean.getId());
			}
		} else {
			List<Integer> noShareIds = new ArrayList<Integer>();
			for(Integer id:originIdIntegers){
				Content c=manager.findById(id);
				if(!c.getShared()){
					noShareIds.add(id);
				}
				//处理附件
				manager.updateFileByContent(c, false);
			}
			manager.deleteShares(originIdIntegers);
			if((noShareIds != null) && (noShareIds.size() >0)){
				Integer[] noShareDelIds = new Integer[noShareIds.size()];
				for (int i = 0; i < noShareIds.size(); i++) {
					noShareDelIds[i] = noShareIds.get(i);
				}
				beans = manager.deleteByIdsWithShare(noShareDelIds, site.getId());
				for (Content bean : beans) {
					log.info("delete Content id={}", bean.getId());
					cmsLogMng.operating(request, "content.log.delete", "id="
							+ bean.getId() + ";title=" + bean.getTitle());
				}
			}
		}	
	}
	public void deleteShare(CmsSite site, Integer[] shareIdIntegers, Integer cid, HttpServletRequest request){
		if (cid != null) {
			Integer countParent = channelMng.getCountById(cid);	//countParent等于0表示是末级栏目
			if (countParent == 0) {
				for (Integer contentId : shareIdIntegers) {
					ContentShareCheck contentShareCheck = checkMng.findByChannelIdAndContentId(cid, contentId);
					Integer[] ids = new Integer[1];
					ids[0] = contentShareCheck.getId();
					//checkMng.deleteByIds(ids);
					/*contentShareCheck.setShareValid(false);
					checkMng.update(contentShareCheck);*/
				}
				deleteShareSon(shareIdIntegers, request);
			}else{
				deleteShareSon(shareIdIntegers, request);
			}
		}else {
			deleteShareSon(shareIdIntegers, request);
		}
		
	}
	
	public void deleteShareSon(Integer[] shareIdIntegers, HttpServletRequest request){
		CmsUser user = CmsUtils.getUser(request);
		Integer deptId = user.getDepartment().getId();
		//List<Integer> oChannelIdList = new LinkedList<Integer>();	//有权限的那些channelId
		//谁删除谁则删除他的下级共享上来的数据
		List<ContentShareCheck> contentShareCheckList = new ArrayList<ContentShareCheck>();
		//
		List<RisenIntegralRecord> records = new ArrayList<RisenIntegralRecord>();
		for (Integer contentId : shareIdIntegers) {
			records = recordService.findByContentIdAndDeptId(deptId, contentId);
			if(records!=null && records.size()>0){
				for (RisenIntegralRecord risenIntegralRecord : records) {
					contentShareCheckList.add(checkMng.findById(risenIntegralRecord.getRisenirSharecheck()));
				}
			}
			//List<ContentShareCheck> contentShareCheckList = checkMng.findByContentId(contentId);
			//在此处下级存在的栏目上级肯定不存在   此处多余
			/*
			for (ContentShareCheck contentShareCheck : contentShareCheckList) {
				Integer channelId = contentShareCheck.getChannel().getId();
				Integer count = channelMng.findByIdAndDepartId(channelId, deptId);
				if (count>0) {		//说明有权限
					oChannelIdList.add(channelId);
				}
			}
			*/
		}
		if(contentShareCheckList.size()>0){
			for (ContentShareCheck contentShareCheck : contentShareCheckList) {
				checkMng.deleteById(contentShareCheck.getId());
			}
		}
		if(records.size()>0){
			for (RisenIntegralRecord record : records) {
				recordService.delete(record.getRisenirUuid());
			}
		}
		/*
		for (Integer contentId : shareIdIntegers) {
			for (Integer channelId : oChannelIdList) {
				ContentShareCheck contentShareCheck = checkMng.findByChannelIdAndContentId(channelId, contentId);
				Integer[] ids = new Integer[1];
				ids[0] = contentShareCheck.getId();
				checkMng.deleteByIds(ids);
				/*contentShareCheck.setShareValid(false);
				checkMng.update(contentShareCheck);
			}
		}
		*/
	}
	
	@RequiresPermissions("content:o_check")
	@RequestMapping("/content/o_check.do")
	public String check(String queryStatus, Integer queryTypeId,
			Boolean queryTopLevel, Boolean queryRecommend,
			Integer queryOrderBy, Integer[] ids, Integer cid, Integer pageNo,
			HttpServletRequest request, ModelMap model) {
		WebErrors errors = validateCheck(ids, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		CmsUser user = CmsUtils.getUser(request);
		Content[] beans = manager.check(ids, user);
		for (Content bean : beans) {
			log.info("check Content id={}", bean.getId());
		}
		return list(queryStatus, queryTypeId, queryTopLevel, queryRecommend,
				queryOrderBy, cid, pageNo, request, model);
	}
	
	@RequiresPermissions("content:o_check")
	@RequestMapping("/content/o_ajax_check.do")
	public void ajaxCheck(Integer[] ids, HttpServletRequest request, HttpServletResponse response,
			ModelMap model) throws JSONException {
		WebErrors errors = validateCheck(ids, request);
		JSONObject json=new JSONObject();
		if (errors.hasErrors()) {
			json.put("error", errors.getErrors().get(0));
			json.put("success", false);
		}
		CmsUser user = CmsUtils.getUser(request);
		manager.check(ids, user);
		json.put("success", true);
		ResponseUtils.renderJson(response, json.toString());
	}

	@RequiresPermissions("content:o_static")
	@RequestMapping("/content/o_static.do")
	public String contentStatic(String queryStatus, Integer queryTypeId,
			Boolean queryTopLevel, Boolean queryRecommend,
			Integer queryOrderBy, Integer[] ids, Integer cid, Integer pageNo,
			HttpServletRequest request, ModelMap model) {
		WebErrors errors = validateStatic(ids, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		try {
			Content[] beans = manager.contentStatic(CmsUtils.getUser(request),ids);
			for (Content bean : beans) {
				log.info("static Content id={}", bean.getId());
			}
			model.addAttribute("message", errors.getMessage(
					"content.staticGenerated", beans.length));
		} catch (TemplateNotFoundException e) {
			model.addAttribute("message", errors.getMessage(e.getMessage(),
					new Object[] { e.getErrorTitle(), e.getGenerated() }));
		} catch (TemplateParseException e) {
			model.addAttribute("message", errors.getMessage(e.getMessage(),
					new Object[] { e.getErrorTitle(), e.getGenerated() }));
		} catch (GeneratedZeroStaticPageException e) {
			model.addAttribute("message", errors.getMessage(e.getMessage(), e
					.getGenerated()));
		} catch (StaticPageNotOpenException e) {
			model.addAttribute("message", errors.getMessage(e.getMessage(),
					new Object[] { e.getErrorTitle(), e.getGenerated() }));
		} catch (ContentNotCheckedException e) {
			model.addAttribute("message", errors.getMessage(e.getMessage(),
					new Object[] { e.getErrorTitle(), e.getGenerated() }));
		}
		return list(queryStatus, queryTypeId, queryTopLevel, queryRecommend,
				queryOrderBy, cid, pageNo, request, model);
	}

	@RequiresPermissions("content:o_reject")
	@RequestMapping("/content/o_reject.do")
	public String reject(String queryStatus, Integer queryTypeId,
			Boolean queryTopLevel, Boolean queryRecommend,
			Integer queryOrderBy, Integer[] ids, Integer cid, 
			String rejectOpinion, Integer pageNo, HttpServletRequest request,
			ModelMap model) {
		WebErrors errors = validateReject(ids, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		CmsUser user = CmsUtils.getUser(request);
		Content[] beans = manager.reject(ids, user, rejectOpinion);
		for (Content bean : beans) {
			log.info("reject Content id={}", bean.getId());
		}
		return list(queryStatus, queryTypeId, queryTopLevel, queryRecommend,
				queryOrderBy, cid, pageNo, request, model);
	}
	
	@RequiresPermissions("content:o_reject")
	@RequestMapping("/content/o_ajax_reject.do")
	public void ajaxReject(Integer[] ids, String rejectOpinion, HttpServletRequest request, HttpServletResponse response,
			ModelMap model) throws JSONException {
		WebErrors errors = validateReject(ids, request);
		JSONObject json=new JSONObject();
		if (errors.hasErrors()) {
			json.put("error", errors.getErrors().get(0));
			json.put("success", false);
		}
		CmsUser user = CmsUtils.getUser(request);
		manager.reject(ids, user, rejectOpinion);
		json.put("success", true);
		ResponseUtils.renderJson(response, json.toString());
	}
	
	@RequiresPermissions("content:o_submit")
	@RequestMapping("/content/o_submit.do")
	public String submit(String queryStatus, Integer queryTypeId,
			Boolean queryTopLevel, Boolean queryRecommend,
			Integer queryOrderBy, Integer[] ids, Integer cid, Integer pageNo,
			HttpServletRequest request, ModelMap model) {
		WebErrors errors = validateCheck(ids, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		CmsUser user = CmsUtils.getUser(request);
		Content[] beans = manager.submit(ids, user);
		for (Content bean : beans) {
			log.info("submit Content id={}", bean.getId());
		}
		return list(queryStatus, queryTypeId, queryTopLevel, queryRecommend,
				queryOrderBy, cid, pageNo, request, model);
	}
	
	@RequiresPermissions("content:v_tree_radio")
	@RequestMapping(value = "/content/v_tree_radio.do")
	public String move_tree(String root, HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		tree(root, request, response, model);
		return "content/tree_move";
	}
	
	@RequiresPermissions("content:o_move")
	@RequestMapping("/content/o_move.do")
		public void move(Integer contentIds[], Integer channelId,
				HttpServletRequest request,HttpServletResponse response) throws JSONException {
		JSONObject json = new JSONObject();
		Boolean pass = true;
		CmsUser user = CmsUtils.getUser(request);
		Integer departId = user.getDepartment().getId();
		if(user.getUsername().equals("admin")){
			json.put("erro", "你权限太大了不允许");
			json.put("pass", false);
			ResponseUtils.renderJson(response, json.toString());
			return;
		}
		if (contentIds != null && channelId != null) {
			Channel channel=channelMng.findById(channelId);
			for(Integer contentId:contentIds){
				Content bean=manager.findById(contentId);
				if(bean.getShared()){
					RisenIntegralRecord record = recordService.findByContentIdAndDeptId(departId, contentId).get(0);
					//List<ContentShareCheck> sharechecks = new ArrayList<ContentShareCheck>(bean.getContentShareCheckSet()) ;
					ContentShareCheck shareCheck = checkMng.findById(record.getRisenirSharecheck());
					shareCheck.setChannel(channel);
					checkMng.update(shareCheck);
				}else{
					if(bean!=null&&channel!=null){
						bean.removeSelfAddToChannels(channelMng.findById(channelId));
						bean.setChannel(channel);
						manager.update(CmsUtils.getUser(request), bean, ContentOperateType.move);
					}
				}
			}
		}
		json.put("pass", pass);
		ResponseUtils.renderJson(response, json.toString());
	}
	
	@RequiresPermissions("content:o_copy")
	@RequestMapping("/content/o_copy.do")
		public void copy(Integer contentIds[],Integer channelId,
				Integer siteId,HttpServletRequest request,
				HttpServletResponse response)throws JSONException {
		JSONObject json = new JSONObject();
		CmsUser user=CmsUtils.getUser(request);
		siteId=user.getSiteIds()[0];
		Boolean pass = true;
		if (contentIds != null) {
			for(Integer contentId:contentIds){
		        Content bean = manager.findById(contentId);
				Content beanCopy = new Content();
				ContentExt extCopy=new ContentExt();
				ContentTxt txtCopy=new ContentTxt();
				ContentDoc docCopy=null;
				beanCopy=bean.cloneWithoutSet();
				beanCopy.setChannel(channelMng.findById(channelId));
				//复制到别站
				if(siteId!=null){
					beanCopy.setSite(siteMng.findById(siteId));
				}
				boolean draft=false;
				if(bean.getStatus().equals(ContentCheck.DRAFT)){
					draft=true;
				}
				BeanUtils.copyProperties(bean.getContentExt(), extCopy);
				if(bean.getContentTxt()!=null){
					BeanUtils.copyProperties(bean.getContentTxt(), txtCopy);
				}
				if(bean.getContentDoc()!=null){
					docCopy=new ContentDoc();
					BeanUtils.copyProperties(bean.getContentDoc(), docCopy);
				}
				manager.save(beanCopy, extCopy, txtCopy, docCopy, null,
						bean.getTopicIds(), bean.getViewGroupIds(), 
						bean.getTagArray(), bean.getAttachmentPaths(),
						bean.getAttachmentNames(),bean.getAttachmentFileNames(),
						bean.getPicPaths(), bean.getPicDescs(),
						channelId, bean.getType().getId(), draft,false,
						bean.getChargeModel(),bean.getChargeAmount(),user, false);
		    }
		}
		json.put("pass", pass);
		ResponseUtils.renderJson(response, json.toString());
	}
	
	@RequiresPermissions("content_reuse:o_copy")
	@RequestMapping("/content_reuse/o_copy.do")
	public void contentCopy(Integer contentIds[],Integer channelId,Integer siteId,HttpServletRequest request,HttpServletResponse response) throws JSONException {
		copy(contentIds, channelId, siteId, request, response);
	}
	/**
	 * 引用
	 * @param contentIds
	 * @param channelId
	 */
	@RequiresPermissions("content:o_refer")
	@RequestMapping("/content/o_refer.do")
		public void refer(Integer contentIds[],Integer channelId,HttpServletRequest request,HttpServletResponse response) throws JSONException {
		JSONObject json = new JSONObject();
		CmsUser user=CmsUtils.getUser(request);
		Boolean pass = true;
		if(user==null){
			ResponseUtils.renderJson(response, "false");
		}
		if (contentIds != null) {
			for(Integer contentId:contentIds){
				manager.updateByChannelIds(contentId, new Integer[]{channelId});
			}
		}else{
			ResponseUtils.renderJson(response, "false");
		}
		json.put("pass", pass);
		ResponseUtils.renderJson(response, json.toString());
	}
	
	@RequiresPermissions("content:o_priority")
	@RequestMapping("/content/o_priority.do")
	public String priority(Integer[] wids, Byte[] topLevel,
			String queryStatus, Integer queryTypeId,
			Boolean queryTopLevel, Boolean queryRecommend,
			Integer queryOrderBy, Integer cid, Integer pageNo,
			HttpServletRequest request, ModelMap model) {
		for(int i=0;i<wids.length;i++){
			Content c=manager.findById(wids[i]);
			c.setTopLevel(topLevel[i]);
			manager.update(c);
		}
		log.info("update content priority.");
		return list(queryStatus, queryTypeId, queryTopLevel, queryRecommend, queryOrderBy, cid, pageNo, request, model);
	}
	
	@RequiresPermissions("content:o_pigeonhole")
	@RequestMapping("/content/o_pigeonhole.do")
	public String pigeonhole(Integer[] ids, 
			String queryStatus, Integer queryTypeId,
			Boolean queryTopLevel, Boolean queryRecommend,
			Integer queryOrderBy, Integer cid, Integer pageNo,
			HttpServletRequest request, ModelMap model) {
		for(int i=0;i<ids.length;i++){
			Content c=manager.findById(ids[i]);
			c.setStatus(ContentCheck.PIGEONHOLE);
			manager.update(CmsUtils.getUser(request), c, ContentOperateType.pigeonhole);
		}
		log.info("update CmsFriendlink priority.");
		return list(queryStatus, queryTypeId, queryTopLevel, queryRecommend, queryOrderBy, cid, pageNo, request, model);
	}
	
	@RequiresPermissions("content:o_unpigeonhole")
	@RequestMapping("/content/o_unpigeonhole.do")
	public String unpigeonhole(Integer[] ids, 
			String queryStatus, Integer queryTypeId,
			Boolean queryTopLevel, Boolean queryRecommend,
			Integer queryOrderBy, Integer cid, Integer pageNo,
			HttpServletRequest request, ModelMap model) {
		for(int i=0;i<ids.length;i++){
			Content c=manager.findById(ids[i]);
			c.setStatus(ContentCheck.CHECKED);
			manager.update(CmsUtils.getUser(request), c, ContentOperateType.reuse);
		}
		log.info("update CmsFriendlink priority.");
		return list(queryStatus, queryTypeId, queryTopLevel, queryRecommend, queryOrderBy, cid, pageNo, request, model);
	}
	
	/**
	 * 推送至专题
	 * @param contentIds
	 * @param topicIds
	 */
	@RequiresPermissions("content:o_send_to_topic")
	@RequestMapping("/content/o_send_to_topic.do")
		public void sendToTopic(Integer contentIds[],Integer[] topicIds,HttpServletRequest request,HttpServletResponse response) throws JSONException {
		JSONObject json = new JSONObject();
		CmsUser user=CmsUtils.getUser(request);
		Boolean pass = true;
		if(user==null){
			ResponseUtils.renderJson(response, "false");
		}
		if (contentIds != null) {
			for(Integer contentId:contentIds){
				manager.addContentToTopics(contentId,topicIds);
			}
		}else{
			ResponseUtils.renderJson(response, "false");
		}
		json.put("pass", pass);
		ResponseUtils.renderJson(response, json.toString());
	}

	@RequiresPermissions("content:o_upload_attachment")
	@RequestMapping("/content/o_upload_attachment.do")
	public String uploadAttachment(
			@RequestParam(value = "attachmentFile", required = false) MultipartFile file,
			String attachmentNum, HttpServletRequest request, ModelMap model) {
		CmsSite site = CmsUtils.getSite(request);
		CmsUser user= CmsUtils.getUser(request);
		String origName = file.getOriginalFilename();
		String ext = FilenameUtils.getExtension(origName).toLowerCase(
				Locale.ENGLISH);
		WebErrors errors = validateUpload(file,request);
		if (errors.hasErrors()) {
			model.addAttribute("error", errors.getErrors().get(0));
			return "content/attachment_iframe";
		}
		// TODO 检查允许上传的后缀
		try {
			String fileUrl;
			if (site.getConfig().getUploadToDb()) {
				String dbFilePath = site.getConfig().getDbFileUri();
				fileUrl = dbFileMng.storeByExt(site.getUploadPath(), ext, file
						.getInputStream());
				// 加上访问地址
				fileUrl = request.getContextPath() + dbFilePath + fileUrl;
			} else if (site.getUploadFtp() != null) {
				Ftp ftp = site.getUploadFtp();
				String ftpUrl = ftp.getUrl();
				fileUrl = ftp.storeByExt(site.getUploadPath(), ext, file
						.getInputStream());
				// 加上url前缀
				fileUrl = ftpUrl + fileUrl;
			} else {
				String ctx = request.getContextPath();
				fileUrl = fileRepository.storeByExt(site.getUploadPath(), ext,
						file);
				// 加上部署路径
				fileUrl = ctx + fileUrl;
			}
			cmsUserMng.updateUploadSize(user.getId(), Integer.parseInt(String.valueOf(file.getSize()/1024)));
			fileMng.saveFileByPath(fileUrl, origName, false);
			model.addAttribute("attachmentPath", fileUrl);
			model.addAttribute("attachmentName", origName);
			model.addAttribute("attachmentNum", attachmentNum);
		} catch (IllegalStateException e) {
			model.addAttribute("error", e.getMessage());
			log.error("upload file error!", e);
		} catch (IOException e) {
			model.addAttribute("error", e.getMessage());
			log.error("upload file error!", e);
		}
		return "content/attachment_iframe";
	}

	@RequiresPermissions("content:o_upload_media")
	@RequestMapping("/content/o_upload_media.do")
	public void uploadMedia(
			@RequestParam(value = "mediaFile", required = false) MultipartFile file,
			String filename, HttpServletRequest request,HttpServletResponse response,
			ModelMap model) {
		CmsSite site = CmsUtils.getSite(request);
		CmsUser user = CmsUtils.getUser(request);
		String origName = file.getOriginalFilename();
		String ext = FilenameUtils.getExtension(origName).toLowerCase(
				Locale.ENGLISH);
		WebErrors errors = validateUpload(file, request);
		JSONObject jsonArray = new JSONObject();
		JSONObject jsonObt = new JSONObject();
		if (errors.hasErrors()) {
			model.addAttribute("error", errors.getErrors().get(0));
			try {
				jsonObt.put("error", "file error");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		// TODO 检查允许上传的后缀
		String fileUrl="";
		try {
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

			try {
				jsonObt.put("url", fileUrl);
			} catch (JSONException e) {
				//e.printStackTrace();
			}
			cmsUserMng.updateUploadSize(user.getId(), Integer.parseInt(String.valueOf(file.getSize()/1024)));
			if(fileMng.findByPath(fileUrl)!=null){
				fileMng.saveFileByPath(fileUrl, origName, false);
			}
			model.addAttribute("mediaPath", fileUrl);
			model.addAttribute("mediaExt", ext);
		} catch (IllegalStateException e) {
			model.addAttribute("error", e.getMessage());
			log.error("upload file error!", e);
		} catch (IOException e) {
			model.addAttribute("error", e.getMessage());
			log.error("upload file error!", e);
		}
		try {
			jsonObt.put("name", origName);
			jsonObt.put("size", file.getSize());
			jsonArray.put("files", jsonObt);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ResponseUtils.renderText(response, jsonArray.toString());
	}
	
	@RequiresPermissions("content:o_upload_doc")
	@RequestMapping("/content/o_upload_doc.do")
	public String uploadDoc(
			@RequestParam(value = "docFile", required = false) MultipartFile file,
			String filename, HttpServletRequest request, ModelMap model) {
		CmsSite site = CmsUtils.getSite(request);
		CmsUser user = CmsUtils.getUser(request);
		String origName = file.getOriginalFilename();
		String ext = FilenameUtils.getExtension(origName).toLowerCase(
				Locale.ENGLISH);
		WebErrors errors = validateUpload(file, request);
		if (errors.hasErrors()) {
			model.addAttribute("error", errors.getErrors().get(0));
			return "content/wenku_iframe";
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
					fileUrl = dbFileMng.storeByExt(site.getLibraryPath(), ext,
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
					fileUrl = ftp.storeByExt(site.getLibraryPath(), ext, file
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
					fileUrl = fileRepository.storeByExt(site.getLibraryPath(),
							ext, file);
				}
				// 加上部署路径
				fileUrl = ctx + fileUrl;
			}
			cmsUserMng.updateUploadSize(user.getId(), Integer.parseInt(String.valueOf(file.getSize()/1024)));
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
		return "content/wenku_iframe";
	}
	
	@RequiresPermissions("content_cycle:v_list")
	@RequestMapping("/content_cycle/v_list.do")
	public String cycleList(Integer queryTypeId, Boolean queryTopLevel,
			Boolean queryRecommend, Integer queryOrderBy, Integer cid,
			Integer pageNo, HttpServletRequest request, ModelMap model) {
		list(ContentStatus.recycle.toString(), queryTypeId, queryTopLevel,
				queryRecommend, queryOrderBy, cid, pageNo, request, model);
		return "content/cycle_list";
	}

	@RequiresPermissions("content_cycle:o_recycle")
	@RequestMapping("/content_cycle/o_recycle.do")
	public String cycleRecycle(String queryStatus, Integer queryTypeId,
			Boolean queryTopLevel, Boolean queryRecommend,
			Integer queryOrderBy, Integer[] ids, Integer cid, Integer pageNo,
			HttpServletRequest request, ModelMap model) {
		WebErrors errors = validateDelete(ids, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		Content[] beans = manager.recycle(ids);
		for (Content bean : beans) {
			log.info("delete Content id={}", bean.getId());
		}
		return cycleList(queryTypeId, queryTopLevel, queryRecommend,
				queryOrderBy, cid, pageNo, request, model);
	}

	@RequiresPermissions("content_cycle:o_delete")
	@RequestMapping("/content_cycle/o_delete.do")
	public String cycleDelete(String queryStatus, Integer queryTypeId,
			Boolean queryTopLevel, Boolean queryRecommend,
			Integer queryOrderBy, Integer[] ids, Integer cid, Integer pageNo,
			HttpServletRequest request, ModelMap model) {
		CmsSite site=CmsUtils.getSite(request);
		WebErrors errors = validateDelete(ids, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		for(Integer id:ids){
			Content c=manager.findById(id);
			//处理附件
			manager.updateFileByContent(c, false);
		}
		Content[] beans = manager.deleteByIdsWithShare(ids, site.getId());
		for (Content bean : beans) {
			log.info("delete Content id={}", bean.getId());
		}
		return cycleList(queryTypeId, queryTopLevel, queryRecommend,
				queryOrderBy, cid, pageNo, request, model);
	}

	@RequiresPermissions("content:o_generateTags")
	@RequestMapping("/content/o_generateTags.do")
	public void generateTags(String title,HttpServletResponse response) throws JSONException {
		JSONObject json = new JSONObject();
		String tags="";
		if(StringUtils.isNotBlank(title)){
			tags=StrUtils.getKeywords(title, true);
		}
		json.put("tags", tags);
		ResponseUtils.renderJson(response, json.toString());
	}
	
	@RequiresPermissions("content:v_check_records")
	@RequestMapping(value = "/content/v_check_records.do")
	public String checkRecords(Integer id, HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		Content content=manager.findById(id);
		List<CmsWorkflowRecord>records=new ArrayList<CmsWorkflowRecord>();
		if(content.getContentEvent()!=null){
			records=workflowRecordMng.getList(content.getContentEvent().getId(), null);
		}
		model.addAttribute("records", records);
		return "content/checkrecords";
	}
	
	@RequiresPermissions("content:rank_list")
	@RequestMapping(value = "/content/rank_list.do")
	public String contentRankList(Integer orderBy,Integer pageNo, HttpServletRequest request,
			HttpServletResponse response, ModelMap model) {
		model.addAttribute("orderBy", orderBy);
		model.addAttribute("pageNo", cpn(pageNo));
		model.addAttribute("pageSize", CookieUtils.getPageSize(request));
		model.addAttribute("site", CmsUtils.getSite(request));
		return "content/ranklist";
	}
	
	@RequiresPermissions("content:o_upload_docs")
	@RequestMapping("/content/o_upload_docs.do")
	public String uploadDocs(
			@RequestParam(value = "docFile", required = false) MultipartFile file,
			String docNum, HttpServletRequest request, ModelMap model) {
		CmsSite site = CmsUtils.getSite(request);
		CmsUser user = CmsUtils.getUser(request);
		String origName = file.getOriginalFilename();
		String ext = FilenameUtils.getExtension(origName).toLowerCase(
				Locale.ENGLISH);
		WebErrors errors = validateUpload(file, request);
		if (errors.hasErrors()) {
			model.addAttribute("error", errors.getErrors().get(0));
			return "content/doc_iframe";
		}
		// TODO 检查允许上传的后缀
		try {
			String fileUrl;
			if (site.getConfig().getUploadToDb()) {
				String dbFilePath = site.getConfig().getDbFileUri();
				fileUrl = dbFileMng.storeByExt(site.getUploadPath(), ext, file
						.getInputStream());
				// 加上访问地址
				fileUrl = request.getContextPath() + dbFilePath + fileUrl;
			} else if (site.getUploadFtp() != null) {
				Ftp ftp = site.getUploadFtp();
				String ftpUrl = ftp.getUrl();
				fileUrl = ftp.storeByExt(site.getUploadPath(), ext, file
						.getInputStream());
				// 加上url前缀
				fileUrl = ftpUrl + fileUrl;
			} else {
				//String ctx = request.getContextPath();
				fileUrl = fileRepository.storeByExt(site.getUploadPath(), ext,
						file);
				// 加上部署路径
				//fileUrl = ctx + fileUrl;
			}
			cmsUserMng.updateUploadSize(user.getId(), Integer.parseInt(String.valueOf(file.getSize()/1024)));
			model.addAttribute("docPath", fileUrl);
			model.addAttribute("docName", origName);
			model.addAttribute("docNum", docNum);
		} catch (IllegalStateException e) {
			model.addAttribute("error", e.getMessage());
			log.error("upload file error!", e);
		} catch (IOException e) {
			model.addAttribute("error", e.getMessage());
			log.error("upload file error!", e);
		}
		return "content/doc_iframe";
	}
	
	@RequiresPermissions("content:showContentInfo")
	@RequestMapping(value = "/content/showContentInfo.do", method = RequestMethod.GET)
	public String showContentInfo(Integer cId,HttpServletRequest request,ModelMap model){
		Content content = manager.findById(cId);
		List<ContentAttachment> attachments = content.getAttachments();
		model.addAttribute("content", content);
		model.addAttribute("txt", content.getContentTxt().getTxt());
		model.addAttribute("attachments", attachments);
		return "content/showInfo";
	}
	

	@RequiresPermissions("content:import_docs")
	@RequestMapping(value = "/content/import_docs.do", method = RequestMethod.GET)
	public String importDocs(HttpServletRequest request,ModelMap model) {
		CmsSite site=CmsUtils.getSite(request);
		CmsUser user=CmsUtils.getUser(request);
		Integer siteId = site.getId();
		// 栏目列表
		List<Channel> channelList;
		Set<Channel> rights;
		if (user.getUserSite(siteId).getAllChannel()) {
			// 拥有所有栏目权限
			rights = null;
		} else {
			rights = user.getChannels(siteId);
		}
		List<Channel> topList = channelMng.getTopListByRigth(user.getId(), siteId, true);
		channelList = Channel.getListForSelect(topList, rights, true);
		CmsModel m;
		m = cmsModelMng.getDefModel();
		// TODO m==null给出错误提示
		if (m == null) {
			throw new RuntimeException("default model not found!");
		}// 内容类型
		List<ContentType> typeList = contentTypeMng.getList(false);
		model.addAttribute("typeList",typeList);
		model.addAttribute("channelList",channelList);
		model.addAttribute("model",m);
		return "content/import";
	}
	
	@RequiresPermissions("content:import_docs")
	@RequestMapping(value = "/content/import_docs.do", method = RequestMethod.POST)
	public String submitDocs(String[] docPaths, String[] docNames,String[] docFilenames,
			Integer channelId,Integer typeId,Integer modelId,Boolean draft,HttpServletRequest request,ModelMap model) {
		if(docPaths!=null&&docPaths.length>0){
			 openOfficeConverter.setFilePath(realPathResolver.get(CmsUtils.getSite(request).getUploadPath()));
			 String docImgPath=request.getContextPath()+CmsUtils.getSite(request).getUploadPath()+"/"+UploadUtils.generateMonthname();
			 try{
				 for(Integer d=0;d<docPaths.length;d++){
					 String path=realPathResolver.get(docPaths[d]);
					 path=path.replace("\\", "/");
					 File outFile=openOfficeConverter.convert(path,OpenOfficeConverter.HTML);
					 String html=FileUtils.toHtmlString(outFile, docImgPath); 
					 //有些文档获取不到title 取消title获取文章标题方式
					// String title=FileUtils.subString(html, "<TITLE>", "</TITLE>");
					 String title=FileUtils.getFilePrefix(docNames[d]);
					 String txt=FileUtils.clearFormat(FileUtils.subString(html, "<HTML>", "</HTML>"), docImgPath);
				     saveContent(channelId, typeId,modelId, draft, title, txt, request);
				 }
			 }catch (Exception e) {
				 e.printStackTrace();
				 WebErrors errors=WebErrors.create(request);
				 errors.addErrorCode("openoffice.error");
				 return errors.showErrorPage(model);
			 }
		}
		return list(null, null, null, null, null, channelId, 1, request, model);
	}
	
	
	
	private void saveContent(Integer channelId,Integer typeId,Integer modelId,Boolean draft,
			String title,String txt,HttpServletRequest request){
		Content content=new Content();
		ContentExt ext=new ContentExt();
		ext.setTitle(title);
		ContentTxt t=new ContentTxt();
		t.setTxt(txt);
		if(typeId==null){
			typeId=1;
		}
		if(draft==null){
			draft=false;
		}
		content.setModel(cmsModelMng.findById(modelId));
		content.setSite(CmsUtils.getSite(request));
		manager.save(content, ext, t,null, channelId, typeId, draft,CmsUtils.getUser(request), false);
	}

	private void addAttibuteForQuery(ModelMap model, String queryTitle,
			String queryInputUsername, String queryStatus, Integer queryTypeId,
			Boolean queryTopLevel, Boolean queryRecommend,
			Integer queryOrderBy, Integer pageNo) {
		if (!StringUtils.isBlank(queryTitle)) {
			model.addAttribute("queryTitle", queryTitle);
		}
		if (!StringUtils.isBlank(queryInputUsername)) {
			model.addAttribute("queryInputUsername", queryInputUsername);
		}
		if (queryTypeId != null) {
			model.addAttribute("queryTypeId", queryTypeId);
		}
		if (queryStatus != null) {
			model.addAttribute("queryStatus", queryStatus);
		}
		if (queryTopLevel != null) {
			model.addAttribute("queryTopLevel", queryTopLevel);
		}
		if (queryRecommend != null) {
			model.addAttribute("queryRecommend", queryRecommend);
		}
		if (queryOrderBy != null) {
			model.addAttribute("queryOrderBy", queryOrderBy);
		}
		if (pageNo != null) {
			model.addAttribute("pageNo", pageNo);
		}
	}

	private List<String> getTplContent(CmsSite site, CmsModel model, String tpl) {
		String sol = site.getSolutionPath();
		String tplPath = site.getTplPath();
		List<String> tplList = tplManager.getNameListByPrefix(model
				.getTplContent(sol, false));
		tplList = CoreUtils.tplTrim(tplList, tplPath, tpl);
		return tplList;
	}
	
	private List<String> getTplMobileContent(CmsSite site, CmsModel model, String tpl) {
		String sol = site.getMobileSolutionPath();
		String tplPath = site.getTplPath();
		List<String> tplList = tplManager.getNameListByPrefix(model
				.getTplContent(sol, false));
		tplList = CoreUtils.tplTrim(tplList, tplPath, tpl);
		return tplList;
	}
	
	private WebErrors validateTree(String path, HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		// if (errors.ifBlank(path, "path", 255)) {
		// return errors;
		// }
		return errors;
	}

	private WebErrors validateAdd(Integer cid,Integer modelId, HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		if (cid == null) {
			return errors;
		}
		Channel c = channelMng.findById(cid);
		if (errors.ifNotExist(c, Channel.class, cid)) {
			return errors;
		}
		//所选发布内容模型不在栏目模型范围内
		if(modelId!=null){
			CmsModel m=cmsModelMng.findById(modelId);
			if(errors.ifNotExist(m, CmsModel.class, modelId)){
				return errors;
			}
			//默认没有配置的情况下modelIds为空 则允许添加
			if(c.getModelIds().size()>0&&!c.getModelIds().contains(modelId.toString())){
				errors.addErrorCode("channel.modelError", c.getName(),m.getName());
			}
		}
		Integer siteId = CmsUtils.getSiteId(request);
		if (!c.getSite().getId().equals(siteId)) {
			errors.notInSite(Channel.class, cid);
			return errors;
		}
		return errors;
	}

	private WebErrors validateSave(Content bean, Integer channelId,
			HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		CmsSite site = CmsUtils.getSite(request);
		bean.setSite(site);
		if (errors.ifNull(channelId, "channelId")) {
			return errors;
		}
		Channel channel = channelMng.findById(channelId);
		if (errors.ifNotExist(channel, Channel.class, channelId)) {
			return errors;
		}
		if (channel.getChild().size() > 0) {
			errors.addErrorCode("content.error.notLeafChannel");
		}
		//所选发布内容模型不在栏目模型范围内
		if(bean.getModel().getId()!=null){
			CmsModel m=bean.getModel();
			if(errors.ifNotExist(m, CmsModel.class, bean.getModel().getId())){
				return errors;
			}
			//默认没有配置的情况下modelIds为空 则允许添加
			if(channel.getModelIds().size()>0&&!channel.getModelIds().contains(bean.getModel().getId().toString())){
				errors.addErrorCode("channel.modelError", channel.getName(),m.getName());
			}
		}
		return errors;
	}

	private WebErrors validateEdit(Integer id, HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		CmsSite site = CmsUtils.getSite(request);
		if (vldExist(id, site.getId(), errors)) {
			return errors;
		}
		// Content content = manager.findById(id);
		// TODO 是否有编辑的数据权限。
		return errors;
	}

	private WebErrors validateUpdate(Integer id, HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		CmsSite site = CmsUtils.getSite(request);
		if (vldExist(id, site.getId(), errors)) {
			return errors;
		}
		Content content = manager.findById(id);
		// TODO 是否有编辑的数据权限。
		// 是否有审核后更新权限。
		if (!content.isHasUpdateRight()) {
			errors.addErrorCode("content.error.afterCheckUpdate");
			return errors;
		}
		return errors;
	}

	private WebErrors validateDelete(Integer[] ids, HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
//		CmsSite site = CmsUtils.getSite(request);
		errors.ifEmpty(ids, "ids");
		if(ids!=null&&ids.length>0){
			for (Integer id : ids) {
				/*
				if (vldExist(id, site.getId(), errors)) {
					return errors;
				}
				*/
				Content content = manager.findById(id);
				// TODO 是否有编辑的数据权限。
				// 是否有审核后删除权限。
				if (!content.isHasDeleteRight()) {
					errors.addErrorCode("content.error.afterCheckDelete");
					return errors;
				}

			}
		}
		return errors;
	}

	private WebErrors validateCheck(Integer[] ids, HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		CmsSite site = CmsUtils.getSite(request);
		errors.ifEmpty(ids, "ids");
		for (Integer id : ids) {
			vldExist(id, site.getId(), errors);
		}
		return errors;
	}

	private WebErrors validateStatic(Integer[] ids, HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		CmsSite site = CmsUtils.getSite(request);
		errors.ifEmpty(ids, "ids");
		for (Integer id : ids) {
			vldExist(id, site.getId(), errors);
		}
		return errors;
	}

	private WebErrors validateReject(Integer[] ids, HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		CmsSite site = CmsUtils.getSite(request);
		errors.ifEmpty(ids, "ids");
		for (Integer id : ids) {
			vldExist(id, site.getId(), errors);
		}
		return errors;
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

	private boolean vldExist(Integer id, Integer siteId, WebErrors errors) {
		if (errors.ifNull(id, "id")) {
			return true;
		}
		Content entity = manager.findById(id);
		if (errors.ifNotExist(entity, Content.class, id)) {
			return true;
		}
		if (!entity.getSite().getId().equals(siteId)) {
			errors.notInSite(Content.class, id);
			return true;
		}
		return false;
	}
	
	private ContentTxt copyContentTxtImg(ContentTxt txt,CmsSite site){
		if(StringUtils.isNotBlank(txt.getTxt())){
			txt.setTxt(copyTxtHmtlImg(txt.getTxt(), site));
		}
		if(StringUtils.isNotBlank(txt.getTxt1())){
			txt.setTxt1(copyTxtHmtlImg(txt.getTxt1(), site));
		}	
		if(StringUtils.isNotBlank(txt.getTxt2())){
			txt.setTxt2(copyTxtHmtlImg(txt.getTxt2(), site));
		}
		if(StringUtils.isNotBlank(txt.getTxt3())){
			txt.setTxt3(copyTxtHmtlImg(txt.getTxt3(), site));
		}
		return txt;
	}
	
	private String copyTxtHmtlImg(String txtHtml,CmsSite site){
		List<String>imgUrls=ImageUtils.getImageSrc(txtHtml);
		for(String img:imgUrls){
			txtHtml=txtHtml.replace(img, imageSvc.crawlImg(img,site));
		}
		return txtHtml;
	}
	
	@Autowired
	private IRisenIntegralRecordService recordService;
	@Autowired
	private IRisenIntegralService integralService;
	@Autowired
	private ChannelMng channelMng;
	@Autowired
	private CmsUserMng cmsUserMng;
	@Autowired
	private CmsModelMng cmsModelMng;
	@Autowired
	private CmsModelItemMng cmsModelItemMng;
	@Autowired
	private CmsTopicMng cmsTopicMng;
	@Autowired
	private CmsGroupMng cmsGroupMng;
	@Autowired
	private ContentTypeMng contentTypeMng;
	@Autowired
	private TplManager tplManager;
	@Autowired
	private FileRepository fileRepository;
	@Autowired
	private DbFileMng dbFileMng;
	@Autowired
	private CmsLogMng cmsLogMng;
	@Autowired
	private ContentMng manager;
	@Autowired
	private CmsFileMng fileMng;
	@Autowired
	private RealPathResolver realPathResolver;
	@Autowired
	private OpenOfficeConverter openOfficeConverter;
	@Autowired
	private CmsWorkflowRecordMng workflowRecordMng;
	@Autowired
	private CmsSiteMng siteMng;
	@Autowired
	private ImageSvc imageSvc;
	@Autowired
	private IRisenIntegralRecordService irService;
	@Autowired
	private ContentShareCheckMng checkMng;
	@Autowired
	private ContentTypeMng ctMng;
	
}