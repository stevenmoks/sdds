package com.jeecms.cms.action.admin.main;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jeecms.cms.entity.main.Channel;
import com.jeecms.cms.entity.main.ChannelExt;
import com.jeecms.cms.entity.main.ChannelTxt;
import com.jeecms.cms.entity.main.CmsModel;
import com.jeecms.cms.entity.main.CmsModelItem;
import com.jeecms.cms.manager.main.ChannelMng;
import com.jeecms.cms.manager.main.CmsModelItemMng;
import com.jeecms.cms.manager.main.CmsModelMng;
import com.jeecms.common.util.ChineseCharToEn;
import com.jeecms.common.web.RequestUtils;
import com.jeecms.common.web.ResponseUtils;
import com.jeecms.core.entity.CmsDepartment;
import com.jeecms.core.entity.CmsGroup;
import com.jeecms.core.entity.CmsSite;
import com.jeecms.core.entity.CmsUser;
import com.jeecms.core.entity.CmsWorkflow;
import com.jeecms.core.manager.CmsDepartmentMng;
import com.jeecms.core.manager.CmsGroupMng;
import com.jeecms.core.manager.CmsLogMng;
import com.jeecms.core.manager.CmsUserMng;
import com.jeecms.core.manager.CmsWorkflowMng;
import com.jeecms.core.tpl.TplManager;
import com.jeecms.core.web.WebErrors;
import com.jeecms.core.web.util.CmsUtils;
import com.jeecms.core.web.util.CoreUtils;


@Controller
public class ChannelAct {
	private static final Logger log = LoggerFactory.getLogger(ChannelAct.class);
	
	@RequiresPermissions("channel:channel_main")
	@RequestMapping("/channel/channel_main.do")
	public String channelMain(ModelMap model) {
		return "channel/channel_main";
	}
	
	@RequiresPermissions("channel:v_left")
	@RequestMapping("/channel/v_left.do")
	public String left() {
		return "channel/left";
	}

	@RequiresPermissions("channel:v_tree")
	@RequestMapping(value = "/channel/v_tree.do")
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
		CmsUser user=CmsUtils.getUser(request);
		Integer siteId = CmsUtils.getSiteId(request);
		Integer userId = CmsUtils.getUserId(request);
		//当前站所有权限
		if(user.getUserSite(siteId).getAllChannelControl()){
			if (isRoot) {
				CmsSite site = CmsUtils.getSite(request);
				list = manager.getTopList(site.getId(), false);
			} else {
				Integer rootId = Integer.valueOf(root);
				list = manager.getChildList(rootId, false);
			}
		}else{
			Integer departId=null;
			CmsDepartment userDepartment=CmsUtils.getUser(request).getDepartment();
			if(userDepartment!=null){
				departId=userDepartment.getId();
			}
			if (isRoot) {
				list = manager.getControlTopListForDepartId(departId,userId,siteId,false);
			} else {
				list = manager.getControlChildListByDepartId(departId,siteId, Integer
						.parseInt(root), false);
			}
		}
		model.addAttribute("list", list);
		response.setHeader("Cache-Control", "no-cache");
		response.setContentType("text/json;charset=UTF-8");
		return "channel/tree";
	}

	@RequiresPermissions("channel:v_list")
	@RequestMapping("/channel/v_list.do")
	public String list(Integer root, HttpServletRequest request, ModelMap model) {
		List<Channel> list;
		CmsUser user=CmsUtils.getUser(request);
		Integer siteId = CmsUtils.getSiteId(request);
		Integer userId = CmsUtils.getUserId(request);
		if(user.getUserSite(CmsUtils.getSiteId(request)).getAllChannelControl()){
			if (root == null) {
				list = manager.getTopList(siteId, false);
			} else {
				list = manager.getChildList(root, false);
			}
		}else{
			Integer departId=null;
			CmsDepartment userDepartment=user.getDepartment();
			if(userDepartment!=null){
				departId=userDepartment.getId();
			}
			if (root==null) {
				list = manager.getControlTopListForDepartId(departId,userId,siteId,false);
			} else {
				list = manager.getControlChildListByDepartId(departId,siteId, root, false);
			}
		}
		
		model.addAttribute("modelList", cmsModelMng.getList(false,null,siteId));
		model.addAttribute("root", root);
		model.addAttribute("list", list);
		return "channel/list";
	}

	@RequiresPermissions("channel:v_add")
	@RequestMapping("/channel/v_add.do")
	public String add(Integer root, Integer modelId,
			HttpServletRequest request, ModelMap model) {
		CmsSite site = CmsUtils.getSite(request);
		Channel parent = null;
		if (root != null) {
			parent = manager.findById(root);
			model.addAttribute("parent", parent);
			model.addAttribute("root", root);
		}
		// 模型
		CmsModel m = cmsModelMng.findById(modelId);
		// 栏目模板列表
		List<String> channelTplList = getTplChannel(site, m, null);
		// 内容模板列表
		List<String> contentTplList = getTplContent(site, m, null);
		List<CmsModel> models=cmsModelMng.getList(false,true,site.getId());
		Map<String,List<String>>modelTplMap=new HashMap<String, List<String>>();
		for(CmsModel tempModel:models){
			List<String> modelTplList = getTplContent(site, tempModel, null);
			modelTplMap.put(tempModel.getId().toString(), modelTplList);
		}
		// 栏目移动版模板列表
	    List<String> channelMobileTplList = getMobileTplChannel(site, m, null);
		List<String> contentMobileTplList = getMobileTplContent(site, m, null);
		Map<String,List<String>>modelMobileTplMap=new HashMap<String, List<String>>();
		for(CmsModel tempModel:models){
			List<String> modelMobileTplList = getMobileTplContent(site, tempModel, null);
			modelMobileTplMap.put(tempModel.getId().toString(), modelMobileTplList);
		}
		// 模型项列表
		List<CmsModelItem> itemList = cmsModelItemMng.getList(modelId, true,
				false);
		List<CmsGroup> groupList = cmsGroupMng.getList();
		// 浏览会员组列表
		List<CmsGroup> viewGroups = groupList;
		// 投稿会员组列表
		Collection<CmsGroup> contriGroups;
		if (parent != null) {
			contriGroups = parent.getContriGroups();
		} else {
			contriGroups = groupList;
		}
		// 管理员列表
		Collection<CmsUser> users;
		if (parent != null) {
			users = parent.getUsers();
		} else {
			users = cmsUserMng.getAdminList(site.getId(), false, false, null);
		}
		// 工作流列表
		List<CmsWorkflow>workflows=workflowMng.getList(site.getId(), false);
		model.addAttribute("site",CmsUtils.getSite(request));
		model.addAttribute("channelTplList", channelTplList);
		model.addAttribute("contentTplList", contentTplList);
		model.addAttribute("itemList", itemList);
		model.addAttribute("viewGroups", viewGroups);
		model.addAttribute("contriGroups", contriGroups);
		model.addAttribute("contriGroupIds", CmsGroup.fetchIds(contriGroups));
		model.addAttribute("users", users);
		model.addAttribute("userIds", CmsUser.fetchIds(users));
		model.addAttribute("model", m);
		model.addAttribute("models", models);
		model.addAttribute("modelTplMap", modelTplMap);
		model.addAttribute("workflows", workflows);
		model.addAttribute("sessionId",request.getSession().getId());
		model.addAttribute("channelMobileTplList", channelMobileTplList);
		model.addAttribute("contentMobileTplList", contentMobileTplList);
		model.addAttribute("modelMobileTplMap", modelMobileTplMap);
		return "channel/add";
	}

	@RequiresPermissions("channel:v_edit")
	@RequestMapping("/channel/v_edit.do")
	public String edit(Integer id, Integer root, HttpServletRequest request,
			ModelMap model) {
		CmsSite site = CmsUtils.getSite(request);
		WebErrors errors = validateEdit(id, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		if (root != null) {
			model.addAttribute("root", root);
		}
		// 栏目
		Channel channel = manager.findById(id);
		// 当前模板，去除基本路径
		int tplPathLength = site.getTplPath().length();
		String tplChannel = channel.getTplChannel();
		if (!StringUtils.isBlank(tplChannel)) {
			tplChannel = tplChannel.substring(tplPathLength);
		}
		String tplMobileChannel = channel.getMobileTplChannel();
		if (!StringUtils.isBlank(tplMobileChannel)) {
			tplMobileChannel = tplMobileChannel.substring(tplPathLength);
		}
		String tplContent = channel.getTplContent();
		if (!StringUtils.isBlank(tplContent)) {
			tplContent = tplContent.substring(tplPathLength);
		}
		// 父栏目
		Channel parent = channel.getParent();
		// 模型
		CmsModel m = channel.getModel();
		// 栏目列表
		List<Channel> topList = manager.getTopList(site.getId(), false);
		List<Channel> channelList = Channel.getListForSelect(topList, null,
				channel, false);

		// 栏目模板列表
		List<String> channelTplList = getTplChannel(site, m, channel.getTplChannel());
		// 内容模板列表
		List<String> contentTplList = getTplContent(site, m, channel.getTplContent());
		//模型列表和各个模型模板
		List<CmsModel> models=cmsModelMng.getList(false,true,site.getId());
		Map<String,List<String>>modelTplMap=new HashMap<String, List<String>>();
		for(CmsModel tempModel:models){
			List<String> modelTplList = getTplContent(site, tempModel, null);
			modelTplMap.put(tempModel.getId().toString(), modelTplList);
		}
		// 栏目移动版模板列表
	    List<String> channelMobileTplList = getMobileTplChannel(site, m, null);
		List<String> contentMobileTplList = getMobileTplContent(site, m, null);
		Map<String,List<String>>modelMobileTplMap=new HashMap<String, List<String>>();
		for(CmsModel tempModel:models){
			List<String> modelMobileTplList = getMobileTplContent(site, tempModel, null);
			modelMobileTplMap.put(tempModel.getId().toString(), modelMobileTplList);
		}
		List<CmsGroup> groupList = cmsGroupMng.getList();
		// 模型项列表
		List<CmsModelItem> itemList = cmsModelItemMng.getList(m.getId(), true,
				false);
		// 浏览会员组列表、浏览会员组IDS
		List<CmsGroup> viewGroups = groupList;
		Integer[] viewGroupIds = CmsGroup.fetchIds(channel.getViewGroups());
		// 投稿会员组列表
		Collection<CmsGroup> contriGroups;
		if (parent != null) {
			contriGroups = parent.getContriGroups();
		} else {
			contriGroups = groupList;
		}
		// 投稿会员组IDS
		Integer[] contriGroupIds = CmsGroup.fetchIds(channel.getContriGroups());
		// 管理员列表
		Collection<CmsUser> users;
		if (parent != null) {
			users = parent.getUsers();
		} else {
			users = cmsUserMng.getAdminList(site.getId(), false, false, null);
		}
		// 管理员IDS
		Integer[] userIds = channel.getUserIds();
		// 工作流列表
		List<CmsWorkflow>workflows=workflowMng.getList(site.getId(), false);
		model.addAttribute("site",CmsUtils.getSite(request));
		model.addAttribute("channelList", channelList);
		model.addAttribute("modelList", cmsModelMng.getList(false,null,site.getId()));
		model.addAttribute("tplChannel", tplChannel);
		model.addAttribute("tplContent", tplContent);
		model.addAttribute("channelTplList", channelTplList);
		model.addAttribute("contentTplList", contentTplList);
		model.addAttribute("itemList", itemList);
		model.addAttribute("viewGroups", viewGroups);
		model.addAttribute("viewGroupIds", viewGroupIds);
		model.addAttribute("contriGroups", contriGroups);
		model.addAttribute("contriGroupIds", contriGroupIds);
		model.addAttribute("users", users);
		model.addAttribute("userIds", userIds);
		model.addAttribute("channel", channel);
		model.addAttribute("model", m);
		model.addAttribute("models", models);
		model.addAttribute("modelTplMap", modelTplMap);
		model.addAttribute("workflows", workflows);
		model.addAttribute("sessionId",request.getSession().getId());
		model.addAttribute("channelMobileTplList", channelMobileTplList);
		model.addAttribute("contentMobileTplList", contentMobileTplList);
		model.addAttribute("modelMobileTplMap", modelMobileTplMap);
		model.addAttribute("tplMobileChannel", tplMobileChannel);
		return "channel/edit";
	}

	@RequiresPermissions("channel:o_save")
	@RequestMapping("/channel/o_save.do")
	public String save(Integer root,Integer did ,Channel bean, ChannelExt ext,
			ChannelTxt txt, Integer[] viewGroupIds, Integer[] contriGroupIds,
			Integer[] userIds, Integer modelId,Integer workflowId,
			Integer[] modelIds,String[] tpls, String[] mtpls, HttpServletRequest request,ModelMap model) {
		WebErrors errors = validateSave(bean, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		bean.setDepartId(did);
		CmsSite site = CmsUtils.getSite(request);
		// 加上模板前缀
		String tplPath = site.getTplPath();
		if (!StringUtils.isBlank(ext.getTplChannel())) {
			ext.setTplChannel(tplPath + ext.getTplChannel());
		}
		if (!StringUtils.isBlank(ext.getTplContent())) {
			ext.setTplContent(tplPath + ext.getTplContent());
		}
		if (!StringUtils.isBlank(ext.getTplMobileChannel())) {
			ext.setTplMobileChannel(tplPath + ext.getTplMobileChannel());
		}
		if(tpls!=null&&tpls.length>0){
			for(int t=0;t<tpls.length;t++){
				if (!StringUtils.isBlank(tpls[t])) {
					tpls[t]=tplPath+tpls[t];
				}
			}
		}
		if(mtpls!=null&&mtpls.length>0){
			for(int t=0;t<mtpls.length;t++){
				if (!StringUtils.isBlank(mtpls[t])) {
					mtpls[t]=tplPath+mtpls[t];
				}
			}
		}
//		String siteId = request.getParameter("siteId");
		Integer siteId=CmsUtils.getSite(request).getId();
			bean.setAttr(RequestUtils.getRequestMap(request, "attr_"));
			bean = manager.save(bean, ext, txt, viewGroupIds, contriGroupIds,
					userIds, new Integer(siteId), root, modelId,workflowId,modelIds,tpls,mtpls);
		log.info("save Channel id={}, name={}", bean.getId(), bean.getName());
		cmsLogMng.operating(request, "channel.log.save", "id=" + bean.getId()
				+ ";title=" + bean.getTitle());
		model.addAttribute("root", root);
		return "redirect:v_list.do";
	}

	@RequiresPermissions("channel:o_update")
	@RequestMapping("/channel/o_update.do")
	public String update(Integer root,Integer did, Channel bean, ChannelExt ext,
			ChannelTxt txt, Integer[] viewGroupIds, Integer[] contriGroupIds,
			Integer[] userIds, Integer parentId,Integer workflowId,
			Integer[] modelIds,String[] tpls, String[] mtpls,
			Integer modelId,HttpServletRequest request,ModelMap model) {
		WebErrors errors = validateUpdate(bean.getId(), request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		bean.setDepartId(did);
		CmsSite site = CmsUtils.getSite(request);
		// 加上模板前缀
		String tplPath = site.getTplPath();
		if (!StringUtils.isBlank(ext.getTplChannel())) {
			ext.setTplChannel(tplPath + ext.getTplChannel());
		}
		if (!StringUtils.isBlank(ext.getTplContent())) {
			ext.setTplContent(tplPath + ext.getTplContent());
		}
		if (!StringUtils.isBlank(ext.getTplMobileChannel())) {
			ext.setTplMobileChannel(tplPath + ext.getTplMobileChannel());
		}
		if(tpls!=null&&tpls.length>0){
			for(int t=0;t<tpls.length;t++){
				if (!StringUtils.isBlank(tpls[t])&&!tpls[t].startsWith(tplPath)) {
					tpls[t]=tplPath+tpls[t];
				}
			}
		}
		if(mtpls!=null&&mtpls.length>0){
			for(int t=0;t<mtpls.length;t++){
				if (!StringUtils.isBlank(mtpls[t])&&!mtpls[t].startsWith(tplPath)) {
					mtpls[t]=tplPath+mtpls[t];
				}
			}
		}
		Map<String, String> attr = RequestUtils.getRequestMap(request, "attr_");
		bean = manager.update(bean, ext, txt, viewGroupIds, contriGroupIds,
				userIds, parentId, attr,modelId,workflowId,modelIds,tpls,mtpls);
		log.info("update Channel id={}.", bean.getId());
		cmsLogMng.operating(request, "channel.log.update", "id=" + bean.getId()
				+ ";name=" + bean.getName());
		return list(root, request, model);
	}

	@RequiresPermissions("channel:o_delete")
	@RequestMapping("/channel/o_delete.do")
	public String delete(Integer root, Integer[] ids,
			HttpServletRequest request, ModelMap model) {
		WebErrors errors = validateDelete(ids, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		Channel[] beans = manager.deleteByIds(ids);
		for (Channel bean : beans) {
			log.info("delete Channel id={}", bean.getId());
			cmsLogMng.operating(request, "channel.log.delete", "id="
					+ bean.getId() + ";title=" + bean.getTitle());
		}
		return list(root, request, model);
	}

	@RequiresPermissions("channel:o_priority")
	@RequestMapping("/channel/o_priority.do")
	public String priority(Integer root, Integer[] wids, Integer[] priority,
			HttpServletRequest request, ModelMap model) {
		WebErrors errors = validatePriority(wids, priority, request);
		if (errors.hasErrors()) {
			return errors.showErrorPage(model);
		}
		manager.updatePriority(wids, priority);
		model.addAttribute("message", "global.success");
		return list(root, request, model);
	}
	
	@RequiresPermissions("channel:v_create_path")
	@RequestMapping(value = "/channel/v_create_path.do")
	public void createPath(String name,HttpServletRequest request, HttpServletResponse response) {
		String path;
		if (StringUtils.isBlank(name)) {
			path = "";
		} else {
			path=ChineseCharToEn.getAllFirstLetter(name);
		}
		ResponseUtils.renderJson(response, path);
	}
	
	@RequiresPermissions("channel:v_check_path")
	@RequestMapping(value = "/channel/v_check_path.do")
	public void checkPath(Integer cid,String path,HttpServletRequest request, HttpServletResponse response) {
		String pass;
		if (StringUtils.isBlank(path)) {
			pass = "false";
		} else {
			Channel c = manager.findByPath(path, CmsUtils.getSiteId(request));
			if(c==null){
				pass="true" ;
			}else{
				if(c.getId().equals(cid)){
					pass= "true";
				}else{
					pass="false";
				}
			}
		}
		ResponseUtils.renderJson(response, pass);
	}

	private List<String> getTplChannel(CmsSite site, CmsModel model, String tpl) {
		String sol = site.getSolutionPath();
		List<String> tplList = tplManager.getNameListByPrefix(model.getTplChannel(sol, false));
		return CoreUtils.tplTrim(tplList, site.getTplPath(), tpl);
	}
	
	private List<String> getMobileTplChannel(CmsSite site, CmsModel model, String tpl) {
		String sol = site.getMobileSolutionPath();
		List<String> tplList = tplManager.getNameListByPrefix(model.getTplChannel(sol, false));
		return CoreUtils.tplTrim(tplList, site.getTplPath(), tpl);
	}

	private List<String> getTplContent(CmsSite site, CmsModel model, String tpl) {
		String sol = site.getSolutionPath();
		List<String> tplList = tplManager.getNameListByPrefix(model
				.getTplContent(sol, false));
		return CoreUtils.tplTrim(tplList, site.getTplPath(), tpl);
	}
	
	private List<String> getMobileTplContent(CmsSite site, CmsModel model, String tpl) {
		String sol = site.getMobileSolutionPath();
		List<String> tplList = tplManager.getNameListByPrefix(model
				.getTplContent(sol, false));
		return CoreUtils.tplTrim(tplList, site.getTplPath(), tpl);
	}

	private WebErrors validateTree(String path, HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		// if (errors.ifBlank(path, "path", 255)) {
		// return errors;
		// }
		return errors;
	}

	private WebErrors validateSave(Channel bean, HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		CmsSite site = CmsUtils.getSite(request);
		bean.setSite(site);
		return errors;
	}

	private WebErrors validateEdit(Integer id, HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		CmsSite site = CmsUtils.getSite(request);
		if (vldExist(id, site.getId(), errors)) {
			return errors;
		}
		errors=validateRight(id, errors, request);
		return errors;
	}

	private WebErrors validateUpdate(Integer id, HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		CmsSite site = CmsUtils.getSite(request);
		if (vldExist(id, site.getId(), errors)) {
			return errors;
		}
		errors=validateRight(id, errors, request);
		return errors;
	}

	private WebErrors validateDelete(Integer[] ids, HttpServletRequest request) {
		WebErrors errors = WebErrors.create(request);
		CmsSite site = CmsUtils.getSite(request);
		errors.ifEmpty(ids, "ids");
		for (Integer id : ids) {
			if (vldExist(id, site.getId(), errors)) {
				return errors;
			}
			// 检查是否可以删除
			String code = manager.checkDelete(id);
			if (code != null) {
				errors.addErrorCode(code);
				return errors;
			}
			errors=validateRight(id, errors, request);
		}
		return errors;
	}
	
	private WebErrors validateRight(Integer id, WebErrors errors,HttpServletRequest request) {
		CmsSite site = CmsUtils.getSite(request);
		CmsUser user = CmsUtils.getUser(request);
		if(!user.getUserSite(site.getId()).getAllChannelControl()){
			CmsDepartment userDepartment=user.getDepartment();
			if(userDepartment!=null){
				Set<Integer> cids=userDepartment.getControlChannelIds(site.getId());
				if(!cids.contains(id)){
					errors.addErrorCode("cmsChannel.noRight");
				}
			}else{
				errors.addErrorCode("cmsChannel.noRight");
			}
		}
		return errors;
	}

	private boolean vldExist(Integer id, Integer siteId, WebErrors errors) {
		if (errors.ifNull(id, "id")) {
			return true;
		}
		Channel entity = manager.findById(id);
		if (errors.ifNotExist(entity, Channel.class, id)) {
			return true;
		}
		if (!entity.getSite().getId().equals(siteId)) {
			errors.notInSite(Channel.class, id);
			return true;
		}
		return false;
	}

	private WebErrors validatePriority(Integer[] wids, Integer[] priority,
			HttpServletRequest request) {
		CmsSite site = CmsUtils.getSite(request);
		WebErrors errors = WebErrors.create(request);
		if (errors.ifEmpty(wids, "wids")) {
			return errors;
		}
		if (errors.ifEmpty(priority, "priority")) {
			return errors;
		}
		if (wids.length != priority.length) {
			errors.addErrorString("wids length not equals priority length");
			return errors;
		}
		for (int i = 0, len = wids.length; i < len; i++) {
			if (vldExist(wids[i], site.getId(), errors)) {
				return errors;
			}
			if (priority[i] == null) {
				priority[i] = 0;
			}
		}
		return errors;
	}
	
	/*
	 * 给支部批量添加栏目的时候,用此方法
	 * date:2017.1.14
	 */
	@RequiresPermissions("channel:v_branchSave.do")
	@RequestMapping("/channel/v_branchSave.do")
	public String branchSave(){
		return "channel/branchsave";
	}

	/*
	 * 支部添加栏目时跳转到的方法
	 */
	@RequiresPermissions("channel:o_branchSave.do")
	@RequestMapping("/channel/o_branchSave.do")
	public String batchSave(String cid,String siteId, 
			ChannelTxt txt, Integer[] viewGroupIds,
			Integer[] userIds,Integer workflowId,
			Integer[] modelIds,String[] tpls, String[] mtpls, HttpServletRequest request,ModelMap model) {
		model.addAttribute("beginTime", new SimpleDateFormat("HH:mm:ss").format(new Date()));
		String [] names={"图片新闻","支部动态","规范化党支部","党费缴纳","党务公开","特色工作","制度建设"};
		String [] path={"tpxw","zbdt","dwgf","dfjn","dwgk","tsgz","zdjs"};
		Integer [] contriGroupIds={1,3};
		int modelId=1;
		String[] ids=cid.trim().split(",");
		//String[] siteIds = siteId;
		try {
			
			CmsSite site = CmsUtils.getSite(request);
			Integer siteValue = Integer.parseInt(siteId);
		for (int j=0;j<ids.length;j++) {
			int root=Integer.valueOf(ids[j]);
			Channel c=manager.findById(root);
			
			for(int i=0;i<names.length;i++){
				Channel cnel=new Channel();
				ChannelExt ext=new ChannelExt();
				cnel.setDisplay(true);
				cnel.setPath(c.getPath()+path[i]);
				cnel.setPriority(10);
				ext.setAllowUpdown(true);
				ext.setContentImgHeight(310);
				ext.setContentImgWidth(310);
				ext.setName(names[i]);
				ext.setPageSize(10);
				ext.setTitleImgHeight(139);
				ext.setTitleImgWidth(139);
				
				
				// 加上模板前缀
				String tplPath = site.getTplPath();
				if (!StringUtils.isBlank(ext.getTplChannel())) {
					ext.setTplChannel(tplPath + ext.getTplChannel());
				}
				if (!StringUtils.isBlank(ext.getTplContent())) {
					ext.setTplContent(tplPath + ext.getTplContent());
				}
				if (!StringUtils.isBlank(ext.getTplMobileChannel())) {
					ext.setTplMobileChannel(tplPath + ext.getTplMobileChannel());
				}
				if(tpls!=null&&tpls.length>0){
					for(int t=0;t<tpls.length;t++){
						if (!StringUtils.isBlank(tpls[t])) {
							tpls[t]=tplPath+tpls[t];
						}
					}
				}
				if(mtpls!=null&&mtpls.length>0){
					for(int t=0;t<mtpls.length;t++){
						if (!StringUtils.isBlank(mtpls[t])) {
							mtpls[t]=tplPath+mtpls[t];
						}
					}
				}
				cnel.setAttr(RequestUtils.getRequestMap(request, "attr_"));
				
				try {
					manager.save(cnel, ext, new ChannelTxt(), null, contriGroupIds, null, siteValue, root, modelId, workflowId, modelIds, tpls, mtpls);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				log.info("save Channel id={}, name={}", cnel.getId(), cnel.getName());
				cmsLogMng.operating(request, "channel.log.save", "id=" + cnel.getId()
						+ ";title=" + cnel.getTitle());			
			}		
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
		model.addAttribute("endTime", new SimpleDateFormat("HH:mm:ss").format(new Date()));
		model.addAttribute("message","添加栏目完成");
		
		return "channel/branchsave";
		
	}
	
	//验证是否存在子集
	@RequiresPermissions("channel:checkChild.do")
	@RequestMapping("/channel/checkChild.do")
	public void checkChild(HttpServletRequest request,HttpServletResponse response){
		Integer parentChild = new Integer(request.getParameter("channelId"));
		boolean exists = manager.existChildChannel(parentChild);
		if(exists){
			try {
				response.getWriter().write("noexists");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	/*
	 * 给省局,市局批量添加栏目的时候,用到的方法
	 * date:2017.1.15
	 */
	@RequiresPermissions("channel:v_batchSave.do")
	@RequestMapping("/channel/v_batchSave.do")
	public String batchSave(){
		return "channel/batchSave";
	}
	
	
	@RequiresPermissions("channel:v_branchLanMuSave.do")
	@RequestMapping("/channel/v_branchLanMuSave.do")
	public String branchLanMuSave(){
		return "channel/branchLanMu";
	}
	
	@RequiresPermissions("channel:v_batchSaveArea.do")
	@RequestMapping("/channel/v_batchSaveArea.do")
	public String batchSaveArea(){
		return "channel/batchSaveArea";
	}
	@RequiresPermissions("channel:getSiteId.do")
	@RequestMapping("/channel/getSiteId.do")
	//得到站点的ID
	public void getSiteId(HttpServletRequest request,HttpServletResponse response,ModelMap model,String departId){
		try{
			String siteId = manager.getSiteIdByDids(departId);
			if(siteId==null){
				response.getWriter().write("NoBind");
			}else{
				response.getWriter().write(siteId);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/*
	 * 省、市批量添加栏目
	 */
	@RequiresPermissions("channel:o_batchSave.do")
	@RequestMapping("/channel/o_batchSave.do")
	public String batchSave1(String cid, 
			ChannelTxt txt, Integer[] viewGroupIds,
			Integer[] userIds,Integer workflowId,
			Integer[] modelIds,String[] tpls, String[] mtpls, HttpServletRequest request,ModelMap model,String siteId) {
		model.addAttribute("beginTime", new SimpleDateFormat("HH:mm:ss").format(new Date()));
		String[]maps={"djzc","cyzd","dwgl","djfc","wsdx","whsh"};
		String[]mn={"党建之窗","从严治党","党务管理","党建风采","网上党校","文化生活"};

		String[]m1={"tpxw","djdt","ldjh","jyjl","mtsd","lyyt","dzqk"};
		String[]mn1={"图片新闻","党建动态","领导讲话","经验交流","媒体视点","理论研讨","电子期刊"};
		
		String[]m2={"qwsy","djzr","gfhdzbjs","jcdzzjs","sxdzzjs"};
		String[]mn2={"权威声音","党建责任","规范化党支部建设","基层党组织建设","三型党组织建设"};

		
		String[]m3={"tzgg","djwj","djhy","dwpx","dsdjt","dsj","dwgk"};
		String[]mn3={"通知公告","党建文件","党建会议","党务培训","地税大讲堂","大事记","党务公开"};
		
		String[]m4={"xjdx","shsydqh","ghzj","dsxf","qxzy","wsrys","yxdy"};
		String[]mn4={"先进典型","税徽闪耀党旗红","光辉足迹","地税先锋","创先争优","网上荣誉室","优秀党员"};
		
		String[]m5={"xjpxljh","dsbl","dwzn","dgdz","ddzs","dkhc","dbyl","gxjd","tszs","hstj"};
		String[]mn5={"习近平系列讲话","党史博览","党务指南","党规党章","党的知识","党课荟萃","党报要论","国学经典","他山之石","好书推荐"};
		String[]m6={"shzp","wxyd","hsly","jkys","syzp","gmsd"};
		String[]mn6={"书画作品","文学园地","红色旅游","健康养生","摄影作品","革命圣地"};
	
		Integer[] contriGroupIds={1,3};
		int modelId=1;
		String[] ids = cid.trim().split(",");
		try {	
			CmsSite site = CmsUtils.getSite(request);
			Integer siteValue = Integer.parseInt(siteId);
		    for (int j=0;j<ids.length;j++){
				int root=Integer.valueOf(ids[j]);
				Channel c=manager.findById(root);
			
			    for (int q=0;q<maps.length;q++){
				
				Channel cnel=new Channel();
				ChannelExt ext=new ChannelExt();
				cnel.setDisplay(true);
				cnel.setPath(c.getPath()+maps[q]);
				cnel.setPriority(10);
				ext.setAllowUpdown(true);
				ext.setContentImgHeight(310);
				ext.setContentImgWidth(310);
				ext.setName(mn[q]);
				ext.setPageSize(10);
				ext.setTitleImgHeight(139);
				ext.setTitleImgWidth(139);
				
				
				// 加上模板前缀
				String tplPath = site.getTplPath();
				if (!StringUtils.isBlank(ext.getTplChannel())) {
					ext.setTplChannel(tplPath + ext.getTplChannel());
				}
				if (!StringUtils.isBlank(ext.getTplContent())) {
					ext.setTplContent(tplPath + ext.getTplContent());
				}
				if (!StringUtils.isBlank(ext.getTplMobileChannel())) {
					ext.setTplMobileChannel(tplPath + ext.getTplMobileChannel());
				}
				if(tpls!=null&&tpls.length>0){
					for(int t=0;t<tpls.length;t++){
						if (!StringUtils.isBlank(tpls[t])) {
							tpls[t]=tplPath+tpls[t];
						}
					}
				}
				if(mtpls!=null&&mtpls.length>0){
					for(int t=0;t<mtpls.length;t++){
						if (!StringUtils.isBlank(mtpls[t])) {
							mtpls[t]=tplPath+mtpls[t];
						}
					}
				}
				cnel.setAttr(RequestUtils.getRequestMap(request, "attr_"));
				
				try {
					cnel=manager.save(cnel, ext, new ChannelTxt(), null, contriGroupIds, null, siteValue, root, modelId, workflowId, modelIds, tpls, mtpls);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				log.info("save Channel id={}, name={}", cnel.getId(), cnel.getName());
				cmsLogMng.operating(request, "channel.log.save", "id=" + cnel.getId()
						+ ";title=" + cnel.getTitle());
				if(q==0){//党建之窗
					for (int index=0;index<m1.length;index++) {
						Channel cnel1=new Channel();
						ChannelExt ext1=new ChannelExt();
						cnel1.setDisplay(true);
						cnel1.setPath(c.getPath()+m1[index]);
						cnel1.setPriority(10);
						ext1.setAllowUpdown(true);
						ext1.setContentImgHeight(310);
						ext1.setContentImgWidth(310);
						ext1.setName(mn1[index]);
						ext1.setPageSize(10);
						ext1.setTitleImgHeight(139);
						ext1.setTitleImgWidth(139);
						
						
						// 加上模板前缀
						tplPath = site.getTplPath();
						if (!StringUtils.isBlank(ext1.getTplChannel())) {
							ext1.setTplChannel(tplPath + ext1.getTplChannel());
						}
						if (!StringUtils.isBlank(ext1.getTplContent())) {
							ext1.setTplContent(tplPath + ext1.getTplContent());
						}
						if (!StringUtils.isBlank(ext1.getTplMobileChannel())) {
							ext1.setTplMobileChannel(tplPath + ext1.getTplMobileChannel());
						}
						if(tpls!=null&&tpls.length>0){
							for(int t=0;t<tpls.length;t++){
								if (!StringUtils.isBlank(tpls[t])) {
									tpls[t]=tplPath+tpls[t];
								}
							}
						}
						if(mtpls!=null&&mtpls.length>0){
							for(int t=0;t<mtpls.length;t++){
								if (!StringUtils.isBlank(mtpls[t])) {
									mtpls[t]=tplPath+mtpls[t];
								}
							}
						}
						cnel1.setAttr(RequestUtils.getRequestMap(request, "attr_"));
						
						try {
							cnel1=manager.save(cnel1, ext1, new ChannelTxt(), null, contriGroupIds, null, siteValue, cnel.getId(), modelId, workflowId, modelIds, tpls, mtpls);
						} catch (Exception e) {
							e.printStackTrace();
						}
						
						log.info("save Channel id={}, name={}", cnel1.getId(), cnel1.getName());
						cmsLogMng.operating(request, "channel.log.save", "id=" + cnel1.getId()
								+ ";title=" + cnel1.getTitle());
						if(index==5){//党建动态
							String[] is={"xj","jcs"};
							String[] in={"县局","基层所"};
							for(int b=0;b<is.length;b++){
								Channel cnel2=new Channel();
								ChannelExt ext2=new ChannelExt();
								cnel2.setDisplay(true);
								cnel2.setPath(c.getPath()+is[b]);
								cnel2.setPriority(10);
								ext2.setAllowUpdown(true);
								ext2.setContentImgHeight(310);
								ext2.setContentImgWidth(310);
								ext2.setName(in[b]);
								
								ext2.setPageSize(10);
								ext2.setTitleImgHeight(139);
								ext2.setTitleImgWidth(139);
								
								
								// 加上模板前缀
								tplPath = site.getTplPath();
								if (!StringUtils.isBlank(ext2.getTplChannel())) {
									ext2.setTplChannel(tplPath + ext2.getTplChannel());
								}
								if (!StringUtils.isBlank(ext2.getTplContent())) {
									ext2.setTplContent(tplPath + ext2.getTplContent());
								}
								if (!StringUtils.isBlank(ext2.getTplMobileChannel())) {
									ext2.setTplMobileChannel(tplPath + ext2.getTplMobileChannel());
								}
								cnel2.setAttr(RequestUtils.getRequestMap(request, "attr_"));
								
								try {
									cnel2=manager.save(cnel2, ext2, new ChannelTxt(), null, contriGroupIds, null, siteValue, cnel1.getId(), modelId, workflowId, modelIds, tpls, mtpls);
								} catch (Exception e) {
									e.printStackTrace();
								}
								
								log.info("save Channel id={}, name={}", cnel2.getId(), cnel2.getName());
								cmsLogMng.operating(request, "channel.log.save", "id=" + cnel2.getId()
										+ ";title=" + cnel2.getTitle());
								
							}
						}
					}
				}else if(q==1){//从严治党
					for (int index=0;index<m2.length;index++) {
							Channel cnel1=new Channel();
							ChannelExt ext1=new ChannelExt();
							cnel1.setDisplay(true);
							cnel1.setPath(c.getPath()+m2[index]);
							cnel1.setPriority(10);
							ext1.setAllowUpdown(true);
							ext1.setContentImgHeight(310);
							ext1.setContentImgWidth(310);
							ext1.setName(mn2[index]);
							ext1.setPageSize(10);
							ext1.setTitleImgHeight(139);
							ext1.setTitleImgWidth(139);
							
							
							// 加上模板前缀
							tplPath = site.getTplPath();
							if (!StringUtils.isBlank(ext1.getTplChannel())) {
								ext1.setTplChannel(tplPath + ext1.getTplChannel());
							}
							if (!StringUtils.isBlank(ext1.getTplContent())) {
								ext1.setTplContent(tplPath + ext1.getTplContent());
							}
							if (!StringUtils.isBlank(ext1.getTplMobileChannel())) {
								ext1.setTplMobileChannel(tplPath + ext1.getTplMobileChannel());
							}
							if(tpls!=null&&tpls.length>0){
								for(int t=0;t<tpls.length;t++){
									if (!StringUtils.isBlank(tpls[t])) {
										tpls[t]=tplPath+tpls[t];
									}
								}
							}
							if(mtpls!=null&&mtpls.length>0){
								for(int t=0;t<mtpls.length;t++){
									if (!StringUtils.isBlank(mtpls[t])) {
										mtpls[t]=tplPath+mtpls[t];
									}
								}
							}
							cnel1.setAttr(RequestUtils.getRequestMap(request, "attr_"));
							
							try {
								cnel1=manager.save(cnel1, ext1, new ChannelTxt(), null, contriGroupIds, null, siteValue, cnel.getId(), modelId, workflowId, modelIds, tpls, mtpls);
							} catch (Exception e) {
								e.printStackTrace();
							}
							
							log.info("save Channel id={}, name={}", cnel1.getId(), cnel1.getName());
							cmsLogMng.operating(request, "channel.log.save", "id=" + cnel1.getId()
									+ ";title=" + cnel1.getTitle());
							if(index==3){//基层组织建设
								String[] is={"sx","zz","zf","ffcl","zdjs"};
								String[] in={"思想","组织","作风","反腐倡廉","制度建设"};
								for(int b=0;b<is.length;b++){
									Channel cnel2=new Channel();
									ChannelExt ext2=new ChannelExt();
									cnel2.setDisplay(true);
									cnel2.setPath(c.getPath()+is[b]);
									cnel2.setPriority(10);
									ext2.setAllowUpdown(true);
									ext2.setContentImgHeight(310);
									ext2.setContentImgWidth(310);
									ext2.setName(in[b]);
									
									ext2.setPageSize(10);
									ext2.setTitleImgHeight(139);
									ext2.setTitleImgWidth(139);
									
									
									// 加上模板前缀
									tplPath = site.getTplPath();
									if (!StringUtils.isBlank(ext2.getTplChannel())) {
										ext2.setTplChannel(tplPath + ext2.getTplChannel());
									}
									if (!StringUtils.isBlank(ext2.getTplContent())) {
										ext2.setTplContent(tplPath + ext2.getTplContent());
									}
									if (!StringUtils.isBlank(ext2.getTplMobileChannel())) {
										ext2.setTplMobileChannel(tplPath + ext2.getTplMobileChannel());
									}
									if(tpls!=null&&tpls.length>0){
										for(int t=0;t<tpls.length;t++){
											if (!StringUtils.isBlank(tpls[t])) {
												tpls[t]=tplPath+tpls[t];
											}
										}
									}
									if(mtpls!=null&&mtpls.length>0){
										for(int t=0;t<mtpls.length;t++){
											if (!StringUtils.isBlank(mtpls[t])) {
												mtpls[t]=tplPath+mtpls[t];
											}
										}
									}
									cnel2.setAttr(RequestUtils.getRequestMap(request, "attr_"));
									
									try {
										cnel2=manager.save(cnel2, ext2, new ChannelTxt(), null, contriGroupIds, null, siteValue, cnel1.getId(), modelId, workflowId, modelIds, tpls, mtpls);
									} catch (Exception e) {
										e.printStackTrace();
									}
									
									log.info("save Channel id={}, name={}", cnel2.getId(), cnel2.getName());
									cmsLogMng.operating(request, "channel.log.save", "id=" + cnel2.getId()
											+ ";title=" + cnel2.getTitle());
									
								}
							}	
							if(index==4){//三型党组织建设
								String[] is={"xxx","fwx","cxx"};
								String[] in={"学习型","服务型","创新型"};
								for(int b=0;b<is.length;b++){
									Channel cnel2=new Channel();
									ChannelExt ext2=new ChannelExt();
									cnel2.setDisplay(true);
									cnel2.setPath(c.getPath()+is[b]);
									cnel2.setPriority(10);
									ext2.setAllowUpdown(true);
									ext2.setContentImgHeight(310);
									ext2.setContentImgWidth(310);
									ext2.setName(in[b]);
									
									ext2.setPageSize(10);
									ext2.setTitleImgHeight(139);
									ext2.setTitleImgWidth(139);
									
									
									// 加上模板前缀
									tplPath = site.getTplPath();
									if (!StringUtils.isBlank(ext2.getTplChannel())) {
										ext2.setTplChannel(tplPath + ext2.getTplChannel());
									}
									if (!StringUtils.isBlank(ext2.getTplContent())) {
										ext2.setTplContent(tplPath + ext2.getTplContent());
									}
									if (!StringUtils.isBlank(ext2.getTplMobileChannel())) {
										ext2.setTplMobileChannel(tplPath + ext2.getTplMobileChannel());
									}
									if(tpls!=null&&tpls.length>0){
										for(int t=0;t<tpls.length;t++){
											if (!StringUtils.isBlank(tpls[t])) {
												tpls[t]=tplPath+tpls[t];
											}
										}
									}
									if(mtpls!=null&&mtpls.length>0){
										for(int t=0;t<mtpls.length;t++){
											if (!StringUtils.isBlank(mtpls[t])) {
												mtpls[t]=tplPath+mtpls[t];
											}
										}
									}
									cnel2.setAttr(RequestUtils.getRequestMap(request, "attr_"));
									
									try {
										cnel2=manager.save(cnel2, ext2, new ChannelTxt(), null, contriGroupIds, null, siteValue, cnel1.getId(), modelId, workflowId, modelIds, tpls, mtpls);
									} catch (Exception e) {
										e.printStackTrace();
									}
									
									log.info("save Channel id={}, name={}", cnel2.getId(), cnel2.getName());
									cmsLogMng.operating(request, "channel.log.save", "id=" + cnel2.getId()
											+ ";title=" + cnel2.getTitle());
									
								}
							}
					}
				}else if(q==2){//党务管理
					
						for (int index=0;index<m3.length;index++) {
							Channel cnel1=new Channel();
							ChannelExt ext1=new ChannelExt();
							cnel1.setDisplay(true);
							cnel1.setPath(c.getPath()+m3[index]);
							cnel1.setPriority(10);
							ext1.setAllowUpdown(true);
							ext1.setContentImgHeight(310);
							ext1.setContentImgWidth(310);
							ext1.setName(mn3[index]);
							
							ext1.setPageSize(10);
							ext1.setTitleImgHeight(139);
							ext1.setTitleImgWidth(139);
							
							
							// 加上模板前缀
							tplPath = site.getTplPath();
							if (!StringUtils.isBlank(ext1.getTplChannel())) {
								ext1.setTplChannel(tplPath + ext1.getTplChannel());
							}
							if (!StringUtils.isBlank(ext1.getTplContent())) {
								ext1.setTplContent(tplPath + ext1.getTplContent());
							}
							if (!StringUtils.isBlank(ext1.getTplMobileChannel())) {
								ext1.setTplMobileChannel(tplPath + ext1.getTplMobileChannel());
							}
							if(tpls!=null&&tpls.length>0){
								for(int t=0;t<tpls.length;t++){
									if (!StringUtils.isBlank(tpls[t])) {
										tpls[t]=tplPath+tpls[t];
									}
								}
							}
							if(mtpls!=null&&mtpls.length>0){
								for(int t=0;t<mtpls.length;t++){
									if (!StringUtils.isBlank(mtpls[t])) {
										mtpls[t]=tplPath+mtpls[t];
									}
								}
							}
							cnel1.setAttr(RequestUtils.getRequestMap(request, "attr_"));
							
							try {
								cnel1=manager.save(cnel1, ext1, new ChannelTxt(), null, contriGroupIds, null, siteValue, cnel.getId(), modelId, workflowId, modelIds, tpls, mtpls);
							} catch (Exception e) {
								e.printStackTrace();
							}
							
							log.info("save Channel id={}, name={}", cnel1.getId(), cnel1.getName());
							cmsLogMng.operating(request, "channel.log.save", "id=" + cnel1.getId()
									+ ";title=" + cnel1.getTitle());
							if(index==1){//党建文件
								String[] is={"djwjxt","djwjjg"};
								String[] in={"系统","机关"};
								for(int b=0;b<is.length;b++){
									Channel cnel2=new Channel();
									ChannelExt ext2=new ChannelExt();
									cnel2.setDisplay(true);
									cnel2.setPath(c.getPath()+is[b]);
									cnel2.setPriority(10);
									ext2.setAllowUpdown(true);
									ext2.setContentImgHeight(310);
									ext2.setContentImgWidth(310);
									ext2.setName(in[b]);
									
									ext2.setPageSize(10);
									ext2.setTitleImgHeight(139);
									ext2.setTitleImgWidth(139);
									
									
									// 加上模板前缀
									tplPath = site.getTplPath();
									if (!StringUtils.isBlank(ext2.getTplChannel())) {
										ext2.setTplChannel(tplPath + ext2.getTplChannel());
									}
									if (!StringUtils.isBlank(ext2.getTplContent())) {
										ext2.setTplContent(tplPath + ext2.getTplContent());
									}
									if (!StringUtils.isBlank(ext2.getTplMobileChannel())) {
										ext2.setTplMobileChannel(tplPath + ext2.getTplMobileChannel());
									}
									if(tpls!=null&&tpls.length>0){
										for(int t=0;t<tpls.length;t++){
											if (!StringUtils.isBlank(tpls[t])) {
												tpls[t]=tplPath+tpls[t];
											}
										}
									}
									if(mtpls!=null&&mtpls.length>0){
										for(int t=0;t<mtpls.length;t++){
											if (!StringUtils.isBlank(mtpls[t])) {
												mtpls[t]=tplPath+mtpls[t];
											}
										}
									}
									cnel2.setAttr(RequestUtils.getRequestMap(request, "attr_"));
									
									try {
										cnel2=manager.save(cnel2, ext2, new ChannelTxt(), null, contriGroupIds, null, siteValue, cnel1.getId(), modelId, workflowId, modelIds, tpls, mtpls);
									} catch (Exception e) {
										e.printStackTrace();
									}
									
									log.info("save Channel id={}, name={}", cnel2.getId(), cnel2.getName());
									cmsLogMng.operating(request, "channel.log.save", "id=" + cnel2.getId()
											+ ";title=" + cnel2.getTitle());
									
								}
							}
                            if(index==2){//党建会议
								String[] is={"djhyxt","djhyjg"};
								String[] in={"系统","机关"};
								for(int b=0;b<is.length;b++){
									Channel cnel2=new Channel();
									ChannelExt ext2=new ChannelExt();
									cnel2.setDisplay(true);
									cnel2.setPath(c.getPath()+is[b]);
									cnel2.setPriority(10);
									ext2.setAllowUpdown(true);
									ext2.setContentImgHeight(310);
									ext2.setContentImgWidth(310);
									ext2.setName(in[b]);
									
									ext2.setPageSize(10);
									ext2.setTitleImgHeight(139);
									ext2.setTitleImgWidth(139);
									
									
									// 加上模板前缀
									tplPath = site.getTplPath();
									if (!StringUtils.isBlank(ext2.getTplChannel())) {
										ext2.setTplChannel(tplPath + ext2.getTplChannel());
									}
									if (!StringUtils.isBlank(ext2.getTplContent())) {
										ext2.setTplContent(tplPath + ext2.getTplContent());
									}
									if (!StringUtils.isBlank(ext2.getTplMobileChannel())) {
										ext2.setTplMobileChannel(tplPath + ext2.getTplMobileChannel());
									}
									if(tpls!=null&&tpls.length>0){
										for(int t=0;t<tpls.length;t++){
											if (!StringUtils.isBlank(tpls[t])) {
												tpls[t]=tplPath+tpls[t];
											}
										}
									}
									if(mtpls!=null&&mtpls.length>0){
										for(int t=0;t<mtpls.length;t++){
											if (!StringUtils.isBlank(mtpls[t])) {
												mtpls[t]=tplPath+mtpls[t];
											}
										}
									}
									cnel2.setAttr(RequestUtils.getRequestMap(request, "attr_"));
									
									try {
										cnel2=manager.save(cnel2, ext2, new ChannelTxt(), null, contriGroupIds, null, siteValue, cnel1.getId(), modelId, workflowId, modelIds, tpls, mtpls);
									} catch (Exception e) {
										e.printStackTrace();
									}
									
									log.info("save Channel id={}, name={}", cnel2.getId(), cnel2.getName());
									cmsLogMng.operating(request, "channel.log.save", "id=" + cnel2.getId()
											+ ";title=" + cnel2.getTitle());
									
								}
							}			
							if(index==6){//党务公开
								String[] is={"dwgkxt","dwgkjg"};
								String[] in={"系统","机关"};
								for(int b=0;b<is.length;b++){
									Channel cnel2=new Channel();
									ChannelExt ext2=new ChannelExt();
									cnel2.setDisplay(true);
									cnel2.setPath(c.getPath()+is[b]);
									cnel2.setPriority(10);
									ext2.setAllowUpdown(true);
									ext2.setContentImgHeight(310);
									ext2.setContentImgWidth(310);
									ext2.setName(in[b]);
									
									ext2.setPageSize(10);
									ext2.setTitleImgHeight(139);
									ext2.setTitleImgWidth(139);
									
									
									// 加上模板前缀
									tplPath = site.getTplPath();
									if (!StringUtils.isBlank(ext2.getTplChannel())) {
										ext2.setTplChannel(tplPath + ext2.getTplChannel());
									}
									if (!StringUtils.isBlank(ext2.getTplContent())) {
										ext2.setTplContent(tplPath + ext2.getTplContent());
									}
									if (!StringUtils.isBlank(ext2.getTplMobileChannel())) {
										ext2.setTplMobileChannel(tplPath + ext2.getTplMobileChannel());
									}
									if(tpls!=null&&tpls.length>0){
										for(int t=0;t<tpls.length;t++){
											if (!StringUtils.isBlank(tpls[t])) {
												tpls[t]=tplPath+tpls[t];
											}
										}
									}
									if(mtpls!=null&&mtpls.length>0){
										for(int t=0;t<mtpls.length;t++){
											if (!StringUtils.isBlank(mtpls[t])) {
												mtpls[t]=tplPath+mtpls[t];
											}
										}
									}
									cnel2.setAttr(RequestUtils.getRequestMap(request, "attr_"));
									
									try {
										cnel2=manager.save(cnel2, ext2, new ChannelTxt(), null, contriGroupIds, null, siteValue, cnel1.getId(), modelId, workflowId, modelIds, tpls, mtpls);
									} catch (Exception e) {
										e.printStackTrace();
									}
									
									log.info("save Channel id={}, name={}", cnel2.getId(), cnel2.getName());
									cmsLogMng.operating(request, "channel.log.save", "id=" + cnel2.getId()
											+ ";title=" + cnel2.getTitle());
								}
							}
					}
				} else if (q==3) {//党建风采
						for (int index=0;index<m4.length;index++) {
							Channel cnel1=new Channel();
							ChannelExt ext1=new ChannelExt();
							cnel1.setDisplay(true);
							cnel1.setPath(c.getPath()+m4[index]);
							cnel1.setPriority(10);
							ext1.setAllowUpdown(true);
							ext1.setContentImgHeight(310);
							ext1.setContentImgWidth(310);
							ext1.setName(mn4[index]);
							ext1.setPageSize(10);
							ext1.setTitleImgHeight(139);
							ext1.setTitleImgWidth(139);
							
							
							// 加上模板前缀
							tplPath = site.getTplPath();
							if (!StringUtils.isBlank(ext1.getTplChannel())) {
								ext1.setTplChannel(tplPath + ext1.getTplChannel());
							}
							if (!StringUtils.isBlank(ext1.getTplContent())) {
								ext1.setTplContent(tplPath + ext1.getTplContent());
							}
							if (!StringUtils.isBlank(ext1.getTplMobileChannel())) {
								ext1.setTplMobileChannel(tplPath + ext1.getTplMobileChannel());
							}
							if(tpls!=null&&tpls.length>0){
								for(int t=0;t<tpls.length;t++){
									if (!StringUtils.isBlank(tpls[t])) {
										tpls[t]=tplPath+tpls[t];
									}
								}
							}
							if(mtpls!=null&&mtpls.length>0){
								for(int t=0;t<mtpls.length;t++){
									if (!StringUtils.isBlank(mtpls[t])) {
										mtpls[t]=tplPath+mtpls[t];
									}
								}
							}
							cnel1.setAttr(RequestUtils.getRequestMap(request, "attr_"));
							
							try {
								cnel1=manager.save(cnel1, ext1, new ChannelTxt(), null, contriGroupIds, null, siteValue, cnel.getId(), modelId, workflowId, modelIds, tpls, mtpls);
							} catch (Exception e) {
								e.printStackTrace();
							}
							
							log.info("save Channel id={}, name={}", cnel1.getId(), cnel1.getName());
							cmsLogMng.operating(request, "channel.log.save", "id=" + cnel1.getId()
									+ ";title=" + cnel1.getTitle());
							
							if(index==4){//创先争优
								String[] is={"sjdzbgzf","sjdjjtgr","sjdjsfckxfg"};
								String[] in={"十佳党支部工作法","十佳党建集体/个人","十佳党建师范窗口/先锋岗"};
								for(int b=0;b<is.length;b++){
									Channel cnel2=new Channel();
									ChannelExt ext2=new ChannelExt();
									cnel2.setDisplay(true);
									cnel2.setPath(c.getPath()+is[b]);
									cnel2.setPriority(10);
									ext2.setAllowUpdown(true);
									ext2.setContentImgHeight(310);
									ext2.setContentImgWidth(310);
									ext2.setName(in[b]);
									
									ext2.setPageSize(10);
									ext2.setTitleImgHeight(139);
									ext2.setTitleImgWidth(139);
									
									
									// 加上模板前缀
									tplPath = site.getTplPath();
									if (!StringUtils.isBlank(ext2.getTplChannel())) {
										ext2.setTplChannel(tplPath + ext2.getTplChannel());
									}
									if (!StringUtils.isBlank(ext2.getTplContent())) {
										ext2.setTplContent(tplPath + ext2.getTplContent());
									}
									if (!StringUtils.isBlank(ext2.getTplMobileChannel())) {
										ext2.setTplMobileChannel(tplPath + ext2.getTplMobileChannel());
									}
									if(tpls!=null&&tpls.length>0){
										for(int t=0;t<tpls.length;t++){
											if (!StringUtils.isBlank(tpls[t])) {
												tpls[t]=tplPath+tpls[t];
											}
										}
									}
									if(mtpls!=null&&mtpls.length>0){
										for(int t=0;t<mtpls.length;t++){
											if (!StringUtils.isBlank(mtpls[t])) {
												mtpls[t]=tplPath+mtpls[t];
											}
										}
									}
									cnel2.setAttr(RequestUtils.getRequestMap(request, "attr_"));
									
									try {
										cnel2=manager.save(cnel2, ext2, new ChannelTxt(), null, contriGroupIds, null, siteValue, cnel1.getId(), modelId, workflowId, modelIds, tpls, mtpls);
									} catch (Exception e) {
										e.printStackTrace();
									}
									
									log.info("save Channel id={}, name={}", cnel2.getId(), cnel2.getName());
									cmsLogMng.operating(request, "channel.log.save", "id=" + cnel2.getId()
											+ ";title=" + cnel2.getTitle());
								}
							}
					}
				}  else if (q==4) {//网上党校
					for (int index=0;index<m5.length;index++) {
						Channel cnel1=new Channel();
						ChannelExt ext1=new ChannelExt();
						cnel1.setDisplay(true);
						cnel1.setPath(c.getPath()+m5[index]);
						cnel1.setPriority(10);
						ext1.setAllowUpdown(true);
						ext1.setContentImgHeight(310);
						ext1.setContentImgWidth(310);
						ext1.setName(mn5[index]);
						ext1.setPageSize(10);
						ext1.setTitleImgHeight(139);
						ext1.setTitleImgWidth(139);
						
						
						// 加上模板前缀
						tplPath = site.getTplPath();
						if (!StringUtils.isBlank(ext1.getTplChannel())) {
							ext1.setTplChannel(tplPath + ext1.getTplChannel());
						}
						if (!StringUtils.isBlank(ext1.getTplContent())) {
							ext1.setTplContent(tplPath + ext1.getTplContent());
						}
						if (!StringUtils.isBlank(ext1.getTplMobileChannel())) {
							ext1.setTplMobileChannel(tplPath + ext1.getTplMobileChannel());
						}
						if(tpls!=null&&tpls.length>0){
							for(int t=0;t<tpls.length;t++){
								if (!StringUtils.isBlank(tpls[t])) {
									tpls[t]=tplPath+tpls[t];
								}
							}
						}
						if(mtpls!=null&&mtpls.length>0){
							for(int t=0;t<mtpls.length;t++){
								if (!StringUtils.isBlank(mtpls[t])) {
									mtpls[t]=tplPath+mtpls[t];
								}
							}
						}
						cnel1.setAttr(RequestUtils.getRequestMap(request, "attr_"));
						
						try {
							cnel1=manager.save(cnel1, ext1, new ChannelTxt(), null, contriGroupIds, null, siteValue, cnel.getId(), modelId, workflowId, modelIds, tpls, mtpls);
						} catch (Exception e) {
							e.printStackTrace();
						}
						
						log.info("save Channel id={}, name={}", cnel1.getId(), cnel1.getName());
						cmsLogMng.operating(request, "channel.log.save", "id=" + cnel1.getId()
								+ ";title=" + cnel1.getTitle());
					}
				}else if (q==5){//文化生活
					for (int index=0;index<m6.length;index++) {
						Channel cnel1=new Channel();
						ChannelExt ext1=new ChannelExt();
						cnel1.setDisplay(true);
						cnel1.setPath(c.getPath()+m6[index]);
						cnel1.setPriority(10);
						ext1.setAllowUpdown(true);
						ext1.setContentImgHeight(310);
						ext1.setContentImgWidth(310);
						ext1.setName(mn6[index]);
						ext1.setPageSize(10);
						ext1.setTitleImgHeight(139);
						ext1.setTitleImgWidth(139);
						
						
						// 加上模板前缀
						tplPath = site.getTplPath();
						if (!StringUtils.isBlank(ext1.getTplChannel())) {
							ext1.setTplChannel(tplPath + ext1.getTplChannel());
						}
						if (!StringUtils.isBlank(ext1.getTplContent())) {
							ext1.setTplContent(tplPath + ext1.getTplContent());
						}
						if (!StringUtils.isBlank(ext1.getTplMobileChannel())) {
							ext1.setTplMobileChannel(tplPath + ext1.getTplMobileChannel());
						}
						if(tpls!=null&&tpls.length>0){
							for(int t=0;t<tpls.length;t++){
								if (!StringUtils.isBlank(tpls[t])) {
									tpls[t]=tplPath+tpls[t];
								}
							}
						}
						if(mtpls!=null&&mtpls.length>0){
							for(int t=0;t<mtpls.length;t++){
								if (!StringUtils.isBlank(mtpls[t])) {
									mtpls[t]=tplPath+mtpls[t];
								}
							}
						}
						cnel1.setAttr(RequestUtils.getRequestMap(request, "attr_"));
						
						try {
							cnel1=manager.save(cnel1, ext1, new ChannelTxt(), null, contriGroupIds, null, siteValue, cnel.getId(), modelId, workflowId, modelIds, tpls, mtpls);
						} catch (Exception e) {
							e.printStackTrace();
						}
						
						log.info("save Channel id={}, name={}", cnel1.getId(), cnel1.getName());
						cmsLogMng.operating(request, "channel.log.save", "id=" + cnel1.getId()
								+ ";title=" + cnel1.getTitle());
					    }
				    }      
			    }	
		    }
		} catch (Exception e) {
			e.printStackTrace();
		}
		model.addAttribute("endTime", new SimpleDateFormat("HH:mm:ss").format(new Date()));
		model.addAttribute("message","添加栏目完成");
		
		return "channel/batchSave";
		
	}
	
	
	/*
	 * 区 或县局批量添加栏目
	 */
	@RequiresPermissions("channel:o_batchSaveArea.do")
	@RequestMapping("/channel/o_batchSaveArea.do")
	public String batchSaveArea(String cid, 
			ChannelTxt txt, Integer[] viewGroupIds,
			Integer[] userIds,Integer workflowId,
			Integer[] modelIds,String[] tpls, String[] mtpls, HttpServletRequest request,ModelMap model,String siteId) {
		model.addAttribute("beginTime", new SimpleDateFormat("HH:mm:ss").format(new Date()));
		String[]maps={"djzc","cyzd","dwgl","djfc","wsdx","whsh"};
		String[]mn={"党建之窗","从严治党","党务管理","党建风采","网上党校","文化生活"};

		String[]m1={"tpxw","djdt","ldjh","jyjl","mtsd","lyyt","dzqk"};
		String[]mn1={"图片新闻","党建动态","领导讲话","经验交流","媒体视点","理论研讨","电子期刊"};
		
		String[]m2={"qwsy","djzr","gfhdzbjs","jcdzzjs","sxdzzjs"};
		String[]mn2={"权威声音","党建责任","规范化党支部建设","基层党组织建设","三型党组织建设"};

		
		String[]m3={"tzgg","djwj","djhy","dwpx","dsdjt","dsj","dwgk"};
		String[]mn3={"通知公告","党建文件","党建会议","党务培训","地税大讲堂","大事记","党务公开"};
		
		String[]m4={"xjdx","shsydqh","ghzj","dsxf","qxzy","wsrys","yxdy"};
		String[]mn4={"先进典型","税徽闪耀党旗红","光辉足迹","地税先锋","创先争优","网上荣誉室","优秀党员"};
		
		String[]m5={"xjpxljh","dsbl","dwzn","dgdz","ddzs","dkhc","dbyl","gxjd","tszs","hstj"};
		String[]mn5={"习近平系列讲话","党史博览","党务指南","党规党章","党的知识","党课荟萃","党报要论","国学经典","他山之石","好书推荐"};
		String[]m6={"shzp","wxyd","hsly","jkys","syzp","gmsd"};
		String[]mn6={"书画作品","文学园地","红色旅游","健康养生","摄影作品","革命圣地"};
	
		Integer[] contriGroupIds={1,3};
		int modelId=1;
		String[] ids = cid.trim().split(",");
		try {	
			CmsSite site = CmsUtils.getSite(request);
			Integer siteValue = Integer.parseInt(siteId);
		    for (int j=0;j<ids.length;j++){
				int root=Integer.valueOf(ids[j]);
				Channel c=manager.findById(root);
			
			    for (int q=0;q<maps.length;q++){
				
				Channel cnel=new Channel();
				ChannelExt ext=new ChannelExt();
				cnel.setDisplay(true);
				cnel.setPath(c.getPath()+maps[q]);
				cnel.setPriority(10);
				ext.setAllowUpdown(true);
				ext.setContentImgHeight(310);
				ext.setContentImgWidth(310);
				ext.setName(mn[q]);
				ext.setPageSize(10);
				ext.setTitleImgHeight(139);
				ext.setTitleImgWidth(139);
				
				
				// 加上模板前缀
				String tplPath = site.getTplPath();
				if (!StringUtils.isBlank(ext.getTplChannel())) {
					ext.setTplChannel(tplPath + ext.getTplChannel());
				}
				if (!StringUtils.isBlank(ext.getTplContent())) {
					ext.setTplContent(tplPath + ext.getTplContent());
				}
				if (!StringUtils.isBlank(ext.getTplMobileChannel())) {
					ext.setTplMobileChannel(tplPath + ext.getTplMobileChannel());
				}
				if(tpls!=null&&tpls.length>0){
					for(int t=0;t<tpls.length;t++){
						if (!StringUtils.isBlank(tpls[t])) {
							tpls[t]=tplPath+tpls[t];
						}
					}
				}
				if(mtpls!=null&&mtpls.length>0){
					for(int t=0;t<mtpls.length;t++){
						if (!StringUtils.isBlank(mtpls[t])) {
							mtpls[t]=tplPath+mtpls[t];
						}
					}
				}
				cnel.setAttr(RequestUtils.getRequestMap(request, "attr_"));
				
				try {
					cnel=manager.save(cnel, ext, new ChannelTxt(), null, contriGroupIds, null, siteValue, root, modelId, workflowId, modelIds, tpls, mtpls);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				log.info("save Channel id={}, name={}", cnel.getId(), cnel.getName());
				cmsLogMng.operating(request, "channel.log.save", "id=" + cnel.getId()
						+ ";title=" + cnel.getTitle());
				if(q==0){//党建之窗
					for (int index=0;index<m1.length;index++) {
						Channel cnel1=new Channel();
						ChannelExt ext1=new ChannelExt();
						cnel1.setDisplay(true);
						cnel1.setPath(c.getPath()+m1[index]);
						cnel1.setPriority(10);
						ext1.setAllowUpdown(true);
						ext1.setContentImgHeight(310);
						ext1.setContentImgWidth(310);
						ext1.setName(mn1[index]);
						ext1.setPageSize(10);
						ext1.setTitleImgHeight(139);
						ext1.setTitleImgWidth(139);
						
						
						// 加上模板前缀
						tplPath = site.getTplPath();
						if (!StringUtils.isBlank(ext1.getTplChannel())) {
							ext1.setTplChannel(tplPath + ext1.getTplChannel());
						}
						if (!StringUtils.isBlank(ext1.getTplContent())) {
							ext1.setTplContent(tplPath + ext1.getTplContent());
						}
						if (!StringUtils.isBlank(ext1.getTplMobileChannel())) {
							ext1.setTplMobileChannel(tplPath + ext1.getTplMobileChannel());
						}
						if(tpls!=null&&tpls.length>0){
							for(int t=0;t<tpls.length;t++){
								if (!StringUtils.isBlank(tpls[t])) {
									tpls[t]=tplPath+tpls[t];
								}
							}
						}
						if(mtpls!=null&&mtpls.length>0){
							for(int t=0;t<mtpls.length;t++){
								if (!StringUtils.isBlank(mtpls[t])) {
									mtpls[t]=tplPath+mtpls[t];
								}
							}
						}
						cnel1.setAttr(RequestUtils.getRequestMap(request, "attr_"));
						
						try {
							cnel1=manager.save(cnel1, ext1, new ChannelTxt(), null, contriGroupIds, null, siteValue, cnel.getId(), modelId, workflowId, modelIds, tpls, mtpls);
						} catch (Exception e) {
							e.printStackTrace();
						}
						
						log.info("save Channel id={}, name={}", cnel1.getId(), cnel1.getName());
						cmsLogMng.operating(request, "channel.log.save", "id=" + cnel1.getId()
								+ ";title=" + cnel1.getTitle());
						if(index==5){//党建动态
							String[] is={"jcs"};
							String[] in={"基层所"};
							for(int b=0;b<is.length;b++){
								Channel cnel2=new Channel();
								ChannelExt ext2=new ChannelExt();
								cnel2.setDisplay(true);
								cnel2.setPath(c.getPath()+is[b]);
								cnel2.setPriority(10);
								ext2.setAllowUpdown(true);
								ext2.setContentImgHeight(310);
								ext2.setContentImgWidth(310);
								ext2.setName(in[b]);
								
								ext2.setPageSize(10);
								ext2.setTitleImgHeight(139);
								ext2.setTitleImgWidth(139);
								
								
								// 加上模板前缀
								tplPath = site.getTplPath();
								if (!StringUtils.isBlank(ext2.getTplChannel())) {
									ext2.setTplChannel(tplPath + ext2.getTplChannel());
								}
								if (!StringUtils.isBlank(ext2.getTplContent())) {
									ext2.setTplContent(tplPath + ext2.getTplContent());
								}
								if (!StringUtils.isBlank(ext2.getTplMobileChannel())) {
									ext2.setTplMobileChannel(tplPath + ext2.getTplMobileChannel());
								}
								cnel2.setAttr(RequestUtils.getRequestMap(request, "attr_"));
								
								try {
									cnel2=manager.save(cnel2, ext2, new ChannelTxt(), null, contriGroupIds, null, siteValue, cnel1.getId(), modelId, workflowId, modelIds, tpls, mtpls);
								} catch (Exception e) {
									e.printStackTrace();
								}
								
								log.info("save Channel id={}, name={}", cnel2.getId(), cnel2.getName());
								cmsLogMng.operating(request, "channel.log.save", "id=" + cnel2.getId()
										+ ";title=" + cnel2.getTitle());
								
							}
						}
					}
				}else if(q==1){//从严治党
					for (int index=0;index<m2.length;index++) {
							Channel cnel1=new Channel();
							ChannelExt ext1=new ChannelExt();
							cnel1.setDisplay(true);
							cnel1.setPath(c.getPath()+m2[index]);
							cnel1.setPriority(10);
							ext1.setAllowUpdown(true);
							ext1.setContentImgHeight(310);
							ext1.setContentImgWidth(310);
							ext1.setName(mn2[index]);
							ext1.setPageSize(10);
							ext1.setTitleImgHeight(139);
							ext1.setTitleImgWidth(139);
							
							
							// 加上模板前缀
							tplPath = site.getTplPath();
							if (!StringUtils.isBlank(ext1.getTplChannel())) {
								ext1.setTplChannel(tplPath + ext1.getTplChannel());
							}
							if (!StringUtils.isBlank(ext1.getTplContent())) {
								ext1.setTplContent(tplPath + ext1.getTplContent());
							}
							if (!StringUtils.isBlank(ext1.getTplMobileChannel())) {
								ext1.setTplMobileChannel(tplPath + ext1.getTplMobileChannel());
							}
							if(tpls!=null&&tpls.length>0){
								for(int t=0;t<tpls.length;t++){
									if (!StringUtils.isBlank(tpls[t])) {
										tpls[t]=tplPath+tpls[t];
									}
								}
							}
							if(mtpls!=null&&mtpls.length>0){
								for(int t=0;t<mtpls.length;t++){
									if (!StringUtils.isBlank(mtpls[t])) {
										mtpls[t]=tplPath+mtpls[t];
									}
								}
							}
							cnel1.setAttr(RequestUtils.getRequestMap(request, "attr_"));
							
							try {
								cnel1=manager.save(cnel1, ext1, new ChannelTxt(), null, contriGroupIds, null, siteValue, cnel.getId(), modelId, workflowId, modelIds, tpls, mtpls);
							} catch (Exception e) {
								e.printStackTrace();
							}
							
							log.info("save Channel id={}, name={}", cnel1.getId(), cnel1.getName());
							cmsLogMng.operating(request, "channel.log.save", "id=" + cnel1.getId()
									+ ";title=" + cnel1.getTitle());
							if(index==3){//基层组织建设
								String[] is={"sx","zz","zf","ffcl","zdjs"};
								String[] in={"思想","组织","作风","反腐倡廉","制度建设"};
								for(int b=0;b<is.length;b++){
									Channel cnel2=new Channel();
									ChannelExt ext2=new ChannelExt();
									cnel2.setDisplay(true);
									cnel2.setPath(c.getPath()+is[b]);
									cnel2.setPriority(10);
									ext2.setAllowUpdown(true);
									ext2.setContentImgHeight(310);
									ext2.setContentImgWidth(310);
									ext2.setName(in[b]);
									
									ext2.setPageSize(10);
									ext2.setTitleImgHeight(139);
									ext2.setTitleImgWidth(139);
									
									
									// 加上模板前缀
									tplPath = site.getTplPath();
									if (!StringUtils.isBlank(ext2.getTplChannel())) {
										ext2.setTplChannel(tplPath + ext2.getTplChannel());
									}
									if (!StringUtils.isBlank(ext2.getTplContent())) {
										ext2.setTplContent(tplPath + ext2.getTplContent());
									}
									if (!StringUtils.isBlank(ext2.getTplMobileChannel())) {
										ext2.setTplMobileChannel(tplPath + ext2.getTplMobileChannel());
									}
									if(tpls!=null&&tpls.length>0){
										for(int t=0;t<tpls.length;t++){
											if (!StringUtils.isBlank(tpls[t])) {
												tpls[t]=tplPath+tpls[t];
											}
										}
									}
									if(mtpls!=null&&mtpls.length>0){
										for(int t=0;t<mtpls.length;t++){
											if (!StringUtils.isBlank(mtpls[t])) {
												mtpls[t]=tplPath+mtpls[t];
											}
										}
									}
									cnel2.setAttr(RequestUtils.getRequestMap(request, "attr_"));
									
									try {
										cnel2=manager.save(cnel2, ext2, new ChannelTxt(), null, contriGroupIds, null, siteValue, cnel1.getId(), modelId, workflowId, modelIds, tpls, mtpls);
									} catch (Exception e) {
										e.printStackTrace();
									}
									
									log.info("save Channel id={}, name={}", cnel2.getId(), cnel2.getName());
									cmsLogMng.operating(request, "channel.log.save", "id=" + cnel2.getId()
											+ ";title=" + cnel2.getTitle());
									
								}
							}	
							if(index==4){//三型党组织建设
								String[] is={"xxx","fwx","cxx"};
								String[] in={"学习型","服务型","创新型"};
								for(int b=0;b<is.length;b++){
									Channel cnel2=new Channel();
									ChannelExt ext2=new ChannelExt();
									cnel2.setDisplay(true);
									cnel2.setPath(c.getPath()+is[b]);
									cnel2.setPriority(10);
									ext2.setAllowUpdown(true);
									ext2.setContentImgHeight(310);
									ext2.setContentImgWidth(310);
									ext2.setName(in[b]);
									
									ext2.setPageSize(10);
									ext2.setTitleImgHeight(139);
									ext2.setTitleImgWidth(139);
									
									
									// 加上模板前缀
									tplPath = site.getTplPath();
									if (!StringUtils.isBlank(ext2.getTplChannel())) {
										ext2.setTplChannel(tplPath + ext2.getTplChannel());
									}
									if (!StringUtils.isBlank(ext2.getTplContent())) {
										ext2.setTplContent(tplPath + ext2.getTplContent());
									}
									if (!StringUtils.isBlank(ext2.getTplMobileChannel())) {
										ext2.setTplMobileChannel(tplPath + ext2.getTplMobileChannel());
									}
									if(tpls!=null&&tpls.length>0){
										for(int t=0;t<tpls.length;t++){
											if (!StringUtils.isBlank(tpls[t])) {
												tpls[t]=tplPath+tpls[t];
											}
										}
									}
									if(mtpls!=null&&mtpls.length>0){
										for(int t=0;t<mtpls.length;t++){
											if (!StringUtils.isBlank(mtpls[t])) {
												mtpls[t]=tplPath+mtpls[t];
											}
										}
									}
									cnel2.setAttr(RequestUtils.getRequestMap(request, "attr_"));
									
									try {
										cnel2=manager.save(cnel2, ext2, new ChannelTxt(), null, contriGroupIds, null, siteValue, cnel1.getId(), modelId, workflowId, modelIds, tpls, mtpls);
									} catch (Exception e) {
										e.printStackTrace();
									}
									
									log.info("save Channel id={}, name={}", cnel2.getId(), cnel2.getName());
									cmsLogMng.operating(request, "channel.log.save", "id=" + cnel2.getId()
											+ ";title=" + cnel2.getTitle());
									
								}
							}
					}
				}else if(q==2){//党务管理
					
						for (int index=0;index<m3.length;index++) {
							Channel cnel1=new Channel();
							ChannelExt ext1=new ChannelExt();
							cnel1.setDisplay(true);
							cnel1.setPath(c.getPath()+m3[index]);
							cnel1.setPriority(10);
							ext1.setAllowUpdown(true);
							ext1.setContentImgHeight(310);
							ext1.setContentImgWidth(310);
							ext1.setName(mn3[index]);
							
							ext1.setPageSize(10);
							ext1.setTitleImgHeight(139);
							ext1.setTitleImgWidth(139);
							
							
							// 加上模板前缀
							tplPath = site.getTplPath();
							if (!StringUtils.isBlank(ext1.getTplChannel())) {
								ext1.setTplChannel(tplPath + ext1.getTplChannel());
							}
							if (!StringUtils.isBlank(ext1.getTplContent())) {
								ext1.setTplContent(tplPath + ext1.getTplContent());
							}
							if (!StringUtils.isBlank(ext1.getTplMobileChannel())) {
								ext1.setTplMobileChannel(tplPath + ext1.getTplMobileChannel());
							}
							if(tpls!=null&&tpls.length>0){
								for(int t=0;t<tpls.length;t++){
									if (!StringUtils.isBlank(tpls[t])) {
										tpls[t]=tplPath+tpls[t];
									}
								}
							}
							if(mtpls!=null&&mtpls.length>0){
								for(int t=0;t<mtpls.length;t++){
									if (!StringUtils.isBlank(mtpls[t])) {
										mtpls[t]=tplPath+mtpls[t];
									}
								}
							}
							cnel1.setAttr(RequestUtils.getRequestMap(request, "attr_"));
							
							try {
								cnel1=manager.save(cnel1, ext1, new ChannelTxt(), null, contriGroupIds, null, siteValue, cnel.getId(), modelId, workflowId, modelIds, tpls, mtpls);
							} catch (Exception e) {
								e.printStackTrace();
							}
							
							log.info("save Channel id={}, name={}", cnel1.getId(), cnel1.getName());
							cmsLogMng.operating(request, "channel.log.save", "id=" + cnel1.getId()
									+ ";title=" + cnel1.getTitle());
							/*if(index==1){//党建文件
								String[] is={"djwjxt","djwjjg"};
								String[] in={"系统","机关"};
								for(int b=0;b<is.length;b++){
									Channel cnel2=new Channel();
									ChannelExt ext2=new ChannelExt();
									cnel2.setDisplay(true);
									cnel2.setPath(c.getPath()+is[b]);
									cnel2.setPriority(10);
									ext2.setAllowUpdown(true);
									ext2.setContentImgHeight(310);
									ext2.setContentImgWidth(310);
									ext2.setName(in[b]);
									
									ext2.setPageSize(10);
									ext2.setTitleImgHeight(139);
									ext2.setTitleImgWidth(139);
									
									
									// 加上模板前缀
									tplPath = site.getTplPath();
									if (!StringUtils.isBlank(ext2.getTplChannel())) {
										ext2.setTplChannel(tplPath + ext2.getTplChannel());
									}
									if (!StringUtils.isBlank(ext2.getTplContent())) {
										ext2.setTplContent(tplPath + ext2.getTplContent());
									}
									if (!StringUtils.isBlank(ext2.getTplMobileChannel())) {
										ext2.setTplMobileChannel(tplPath + ext2.getTplMobileChannel());
									}
									if(tpls!=null&&tpls.length>0){
										for(int t=0;t<tpls.length;t++){
											if (!StringUtils.isBlank(tpls[t])) {
												tpls[t]=tplPath+tpls[t];
											}
										}
									}
									if(mtpls!=null&&mtpls.length>0){
										for(int t=0;t<mtpls.length;t++){
											if (!StringUtils.isBlank(mtpls[t])) {
												mtpls[t]=tplPath+mtpls[t];
											}
										}
									}
									cnel2.setAttr(RequestUtils.getRequestMap(request, "attr_"));
									
									try {
										cnel2=manager.save(cnel2, ext2, new ChannelTxt(), null, contriGroupIds, null, siteValue, cnel1.getId(), modelId, workflowId, modelIds, tpls, mtpls);
									} catch (Exception e) {
										e.printStackTrace();
									}
									
									log.info("save Channel id={}, name={}", cnel2.getId(), cnel2.getName());
									cmsLogMng.operating(request, "channel.log.save", "id=" + cnel2.getId()
											+ ";title=" + cnel2.getTitle());
									
								}
							}*/
                            /*if(index==2){//党建会议
								String[] is={"djhyxt","djhyjg"};
								String[] in={"系统","机关"};
								for(int b=0;b<is.length;b++){
									Channel cnel2=new Channel();
									ChannelExt ext2=new ChannelExt();
									cnel2.setDisplay(true);
									cnel2.setPath(c.getPath()+is[b]);
									cnel2.setPriority(10);
									ext2.setAllowUpdown(true);
									ext2.setContentImgHeight(310);
									ext2.setContentImgWidth(310);
									ext2.setName(in[b]);
									
									ext2.setPageSize(10);
									ext2.setTitleImgHeight(139);
									ext2.setTitleImgWidth(139);
									
									
									// 加上模板前缀
									tplPath = site.getTplPath();
									if (!StringUtils.isBlank(ext2.getTplChannel())) {
										ext2.setTplChannel(tplPath + ext2.getTplChannel());
									}
									if (!StringUtils.isBlank(ext2.getTplContent())) {
										ext2.setTplContent(tplPath + ext2.getTplContent());
									}
									if (!StringUtils.isBlank(ext2.getTplMobileChannel())) {
										ext2.setTplMobileChannel(tplPath + ext2.getTplMobileChannel());
									}
									if(tpls!=null&&tpls.length>0){
										for(int t=0;t<tpls.length;t++){
											if (!StringUtils.isBlank(tpls[t])) {
												tpls[t]=tplPath+tpls[t];
											}
										}
									}
									if(mtpls!=null&&mtpls.length>0){
										for(int t=0;t<mtpls.length;t++){
											if (!StringUtils.isBlank(mtpls[t])) {
												mtpls[t]=tplPath+mtpls[t];
											}
										}
									}
									cnel2.setAttr(RequestUtils.getRequestMap(request, "attr_"));
									
									try {
										cnel2=manager.save(cnel2, ext2, new ChannelTxt(), null, contriGroupIds, null, siteValue, cnel1.getId(), modelId, workflowId, modelIds, tpls, mtpls);
									} catch (Exception e) {
										e.printStackTrace();
									}
									
									log.info("save Channel id={}, name={}", cnel2.getId(), cnel2.getName());
									cmsLogMng.operating(request, "channel.log.save", "id=" + cnel2.getId()
											+ ";title=" + cnel2.getTitle());
									
								}
							}*/		
							/*if(index==6){//党务公开
								String[] is={"dwgkxt","dwgkjg"};
								String[] in={"系统","机关"};
								for(int b=0;b<is.length;b++){
									Channel cnel2=new Channel();
									ChannelExt ext2=new ChannelExt();
									cnel2.setDisplay(true);
									cnel2.setPath(c.getPath()+is[b]);
									cnel2.setPriority(10);
									ext2.setAllowUpdown(true);
									ext2.setContentImgHeight(310);
									ext2.setContentImgWidth(310);
									ext2.setName(in[b]);
									
									ext2.setPageSize(10);
									ext2.setTitleImgHeight(139);
									ext2.setTitleImgWidth(139);
									
									
									// 加上模板前缀
									tplPath = site.getTplPath();
									if (!StringUtils.isBlank(ext2.getTplChannel())) {
										ext2.setTplChannel(tplPath + ext2.getTplChannel());
									}
									if (!StringUtils.isBlank(ext2.getTplContent())) {
										ext2.setTplContent(tplPath + ext2.getTplContent());
									}
									if (!StringUtils.isBlank(ext2.getTplMobileChannel())) {
										ext2.setTplMobileChannel(tplPath + ext2.getTplMobileChannel());
									}
									if(tpls!=null&&tpls.length>0){
										for(int t=0;t<tpls.length;t++){
											if (!StringUtils.isBlank(tpls[t])) {
												tpls[t]=tplPath+tpls[t];
											}
										}
									}
									if(mtpls!=null&&mtpls.length>0){
										for(int t=0;t<mtpls.length;t++){
											if (!StringUtils.isBlank(mtpls[t])) {
												mtpls[t]=tplPath+mtpls[t];
											}
										}
									}
									cnel2.setAttr(RequestUtils.getRequestMap(request, "attr_"));
									
									try {
										cnel2=manager.save(cnel2, ext2, new ChannelTxt(), null, contriGroupIds, null, siteValue, cnel1.getId(), modelId, workflowId, modelIds, tpls, mtpls);
									} catch (Exception e) {
										e.printStackTrace();
									}
									
									log.info("save Channel id={}, name={}", cnel2.getId(), cnel2.getName());
									cmsLogMng.operating(request, "channel.log.save", "id=" + cnel2.getId()
											+ ";title=" + cnel2.getTitle());
								}
							}*/
					}
				} else if (q==3) {//党建风采
						for (int index=0;index<m4.length;index++) {
							Channel cnel1=new Channel();
							ChannelExt ext1=new ChannelExt();
							cnel1.setDisplay(true);
							cnel1.setPath(c.getPath()+m4[index]);
							cnel1.setPriority(10);
							ext1.setAllowUpdown(true);
							ext1.setContentImgHeight(310);
							ext1.setContentImgWidth(310);
							ext1.setName(mn4[index]);
							ext1.setPageSize(10);
							ext1.setTitleImgHeight(139);
							ext1.setTitleImgWidth(139);
							
							
							// 加上模板前缀
							tplPath = site.getTplPath();
							if (!StringUtils.isBlank(ext1.getTplChannel())) {
								ext1.setTplChannel(tplPath + ext1.getTplChannel());
							}
							if (!StringUtils.isBlank(ext1.getTplContent())) {
								ext1.setTplContent(tplPath + ext1.getTplContent());
							}
							if (!StringUtils.isBlank(ext1.getTplMobileChannel())) {
								ext1.setTplMobileChannel(tplPath + ext1.getTplMobileChannel());
							}
							if(tpls!=null&&tpls.length>0){
								for(int t=0;t<tpls.length;t++){
									if (!StringUtils.isBlank(tpls[t])) {
										tpls[t]=tplPath+tpls[t];
									}
								}
							}
							if(mtpls!=null&&mtpls.length>0){
								for(int t=0;t<mtpls.length;t++){
									if (!StringUtils.isBlank(mtpls[t])) {
										mtpls[t]=tplPath+mtpls[t];
									}
								}
							}
							cnel1.setAttr(RequestUtils.getRequestMap(request, "attr_"));
							
							try {
								cnel1=manager.save(cnel1, ext1, new ChannelTxt(), null, contriGroupIds, null, siteValue, cnel.getId(), modelId, workflowId, modelIds, tpls, mtpls);
							} catch (Exception e) {
								e.printStackTrace();
							}
							
							log.info("save Channel id={}, name={}", cnel1.getId(), cnel1.getName());
							cmsLogMng.operating(request, "channel.log.save", "id=" + cnel1.getId()
									+ ";title=" + cnel1.getTitle());
							
							/*if(index==4){//创先争优
								String[] is={"sjdzbgzf","sjdjjtgr","sjdjsfckxfg"};
								String[] in={"十佳党支部工作法","十佳党建集体/个人","十佳党建师范窗口/先锋岗"};
								for(int b=0;b<is.length;b++){
									Channel cnel2=new Channel();
									ChannelExt ext2=new ChannelExt();
									cnel2.setDisplay(true);
									cnel2.setPath(c.getPath()+is[b]);
									cnel2.setPriority(10);
									ext2.setAllowUpdown(true);
									ext2.setContentImgHeight(310);
									ext2.setContentImgWidth(310);
									ext2.setName(in[b]);
									
									ext2.setPageSize(10);
									ext2.setTitleImgHeight(139);
									ext2.setTitleImgWidth(139);
									
									
									// 加上模板前缀
									tplPath = site.getTplPath();
									if (!StringUtils.isBlank(ext2.getTplChannel())) {
										ext2.setTplChannel(tplPath + ext2.getTplChannel());
									}
									if (!StringUtils.isBlank(ext2.getTplContent())) {
										ext2.setTplContent(tplPath + ext2.getTplContent());
									}
									if (!StringUtils.isBlank(ext2.getTplMobileChannel())) {
										ext2.setTplMobileChannel(tplPath + ext2.getTplMobileChannel());
									}
									if(tpls!=null&&tpls.length>0){
										for(int t=0;t<tpls.length;t++){
											if (!StringUtils.isBlank(tpls[t])) {
												tpls[t]=tplPath+tpls[t];
											}
										}
									}
									if(mtpls!=null&&mtpls.length>0){
										for(int t=0;t<mtpls.length;t++){
											if (!StringUtils.isBlank(mtpls[t])) {
												mtpls[t]=tplPath+mtpls[t];
											}
										}
									}
									cnel2.setAttr(RequestUtils.getRequestMap(request, "attr_"));
									
									try {
										cnel2=manager.save(cnel2, ext2, new ChannelTxt(), null, contriGroupIds, null, siteValue, cnel1.getId(), modelId, workflowId, modelIds, tpls, mtpls);
									} catch (Exception e) {
										e.printStackTrace();
									}
									
									log.info("save Channel id={}, name={}", cnel2.getId(), cnel2.getName());
									cmsLogMng.operating(request, "channel.log.save", "id=" + cnel2.getId()
											+ ";title=" + cnel2.getTitle());
								}
							}*/
					}
				}  else if (q==4) {//网上党校
					for (int index=0;index<m5.length;index++) {
						Channel cnel1=new Channel();
						ChannelExt ext1=new ChannelExt();
						cnel1.setDisplay(true);
						cnel1.setPath(c.getPath()+m5[index]);
						cnel1.setPriority(10);
						ext1.setAllowUpdown(true);
						ext1.setContentImgHeight(310);
						ext1.setContentImgWidth(310);
						ext1.setName(mn5[index]);
						ext1.setPageSize(10);
						ext1.setTitleImgHeight(139);
						ext1.setTitleImgWidth(139);
						
						
						// 加上模板前缀
						tplPath = site.getTplPath();
						if (!StringUtils.isBlank(ext1.getTplChannel())) {
							ext1.setTplChannel(tplPath + ext1.getTplChannel());
						}
						if (!StringUtils.isBlank(ext1.getTplContent())) {
							ext1.setTplContent(tplPath + ext1.getTplContent());
						}
						if (!StringUtils.isBlank(ext1.getTplMobileChannel())) {
							ext1.setTplMobileChannel(tplPath + ext1.getTplMobileChannel());
						}
						if(tpls!=null&&tpls.length>0){
							for(int t=0;t<tpls.length;t++){
								if (!StringUtils.isBlank(tpls[t])) {
									tpls[t]=tplPath+tpls[t];
								}
							}
						}
						if(mtpls!=null&&mtpls.length>0){
							for(int t=0;t<mtpls.length;t++){
								if (!StringUtils.isBlank(mtpls[t])) {
									mtpls[t]=tplPath+mtpls[t];
								}
							}
						}
						cnel1.setAttr(RequestUtils.getRequestMap(request, "attr_"));
						
						try {
							cnel1=manager.save(cnel1, ext1, new ChannelTxt(), null, contriGroupIds, null, siteValue, cnel.getId(), modelId, workflowId, modelIds, tpls, mtpls);
						} catch (Exception e) {
							e.printStackTrace();
						}
						
						log.info("save Channel id={}, name={}", cnel1.getId(), cnel1.getName());
						cmsLogMng.operating(request, "channel.log.save", "id=" + cnel1.getId()
								+ ";title=" + cnel1.getTitle());
					}
				}else if (q==5){//文化生活
					for (int index=0;index<m6.length;index++) {
						Channel cnel1=new Channel();
						ChannelExt ext1=new ChannelExt();
						cnel1.setDisplay(true);
						cnel1.setPath(c.getPath()+m6[index]);
						cnel1.setPriority(10);
						ext1.setAllowUpdown(true);
						ext1.setContentImgHeight(310);
						ext1.setContentImgWidth(310);
						ext1.setName(mn6[index]);
						ext1.setPageSize(10);
						ext1.setTitleImgHeight(139);
						ext1.setTitleImgWidth(139);
						
						
						// 加上模板前缀
						tplPath = site.getTplPath();
						if (!StringUtils.isBlank(ext1.getTplChannel())) {
							ext1.setTplChannel(tplPath + ext1.getTplChannel());
						}
						if (!StringUtils.isBlank(ext1.getTplContent())) {
							ext1.setTplContent(tplPath + ext1.getTplContent());
						}
						if (!StringUtils.isBlank(ext1.getTplMobileChannel())) {
							ext1.setTplMobileChannel(tplPath + ext1.getTplMobileChannel());
						}
						if(tpls!=null&&tpls.length>0){
							for(int t=0;t<tpls.length;t++){
								if (!StringUtils.isBlank(tpls[t])) {
									tpls[t]=tplPath+tpls[t];
								}
							}
						}
						if(mtpls!=null&&mtpls.length>0){
							for(int t=0;t<mtpls.length;t++){
								if (!StringUtils.isBlank(mtpls[t])) {
									mtpls[t]=tplPath+mtpls[t];
								}
							}
						}
						cnel1.setAttr(RequestUtils.getRequestMap(request, "attr_"));
						
						try {
							cnel1=manager.save(cnel1, ext1, new ChannelTxt(), null, contriGroupIds, null, siteValue, cnel.getId(), modelId, workflowId, modelIds, tpls, mtpls);
						} catch (Exception e) {
							e.printStackTrace();
						}
						
						log.info("save Channel id={}, name={}", cnel1.getId(), cnel1.getName());
						cmsLogMng.operating(request, "channel.log.save", "id=" + cnel1.getId()
								+ ";title=" + cnel1.getTitle());
					    }
				    }      
			    }	
		    }
		} catch (Exception e) {
			e.printStackTrace();
		}
		model.addAttribute("endTime", new SimpleDateFormat("HH:mm:ss").format(new Date()));
		model.addAttribute("message","添加栏目完成");
		
		return "channel/batchSaveArea";
		
	}
	
	//添加区县级的栏目
	private void addCountyChannel(String cid, 
			ChannelTxt txt, Integer[] viewGroupIds,
			Integer[] userIds,Integer workflowId,
			Integer[] modelIds,String[] tpls, String[] mtpls, HttpServletRequest request,ModelMap model,Integer siteId){
		String[]maps={"djzc","cyzd","dwgl","djfc","wsdx","whsh"};
		String[]mn={"党建之窗","从严治党","党务管理","党建风采","网上党校","文化生活"};

		String[]m1={"tpxw","djdt","ldjh","jyjl","mtsd","lyyt","dzqk"};
		String[]mn1={"图片新闻","党建动态","领导讲话","经验交流","媒体视点","理论研讨","电子期刊"};
		
		String[]m2={"qwsy","djzr","gfhdzbjs","jcdzzjs","sxdzzjs"};
		String[]mn2={"权威声音","党建责任","规范化党支部建设","基层党组织建设","三型党组织建设"};

		
		String[]m3={"tzgg","djwj","djhy","dwpx","dsdjt","dsj","dwgk"};
		String[]mn3={"通知公告","党建文件","党建会议","党务培训","地税大讲堂","大事记","党务公开"};
		
		String[]m4={"xjdx","shsydqh","ghzj","dsxf","qxzy","wsrys","yxdy"};
		String[]mn4={"先进典型","税徽闪耀党旗红","光辉足迹","地税先锋","创先争优","网上荣誉室","优秀党员"};
		
		String[]m5={"xjpxljh","dsbl","dwzn","dgdz","ddzs","dkhc","dbyl","gxjd","tszs","hstj"};
		String[]mn5={"习近平系列讲话","党史博览","党务指南","党规党章","党的知识","党课荟萃","党报要论","国学经典","他山之石","好书推荐"};
		String[]m6={"shzp","wxyd","hsly","jkys","syzp","gmsd"};
		String[]mn6={"书画作品","文学园地","红色旅游","健康养生","摄影作品","革命圣地"};
	
		Integer[] contriGroupIds={1,3};
		int modelId=1;
		String[] ids = cid.trim().split(",");
		try {	
			CmsSite site = CmsUtils.getSite(request);
			for (int j=0;j<ids.length;j++){
				int root=Integer.valueOf(ids[j]);
				Channel c=manager.findById(root);
			
			    for (int q=0;q<maps.length;q++){
				
				Channel cnel=new Channel();
				ChannelExt ext=new ChannelExt();
				cnel.setDisplay(true);
				cnel.setPath(c.getPath()+maps[q]);
				cnel.setPriority(10);
				ext.setAllowUpdown(true);
				ext.setContentImgHeight(310);
				ext.setContentImgWidth(310);
				ext.setName(mn[q]);
				ext.setPageSize(10);
				ext.setTitleImgHeight(139);
				ext.setTitleImgWidth(139);
				
				
				// 加上模板前缀
				String tplPath = site.getTplPath();
				if (!StringUtils.isBlank(ext.getTplChannel())) {
					ext.setTplChannel(tplPath + ext.getTplChannel());
				}
				if (!StringUtils.isBlank(ext.getTplContent())) {
					ext.setTplContent(tplPath + ext.getTplContent());
				}
				if (!StringUtils.isBlank(ext.getTplMobileChannel())) {
					ext.setTplMobileChannel(tplPath + ext.getTplMobileChannel());
				}
				if(tpls!=null&&tpls.length>0){
					for(int t=0;t<tpls.length;t++){
						if (!StringUtils.isBlank(tpls[t])) {
							tpls[t]=tplPath+tpls[t];
						}
					}
				}
				if(mtpls!=null&&mtpls.length>0){
					for(int t=0;t<mtpls.length;t++){
						if (!StringUtils.isBlank(mtpls[t])) {
							mtpls[t]=tplPath+mtpls[t];
						}
					}
				}
				cnel.setAttr(RequestUtils.getRequestMap(request, "attr_"));
				
				try {
					cnel=manager.save(cnel, ext, new ChannelTxt(), null, contriGroupIds, null, siteId, root, modelId, workflowId, modelIds, tpls, mtpls);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				log.info("save Channel id={}, name={}", cnel.getId(), cnel.getName());
				cmsLogMng.operating(request, "channel.log.save", "id=" + cnel.getId()
						+ ";title=" + cnel.getTitle());
				if(q==0){//党建之窗
					for (int index=0;index<m1.length;index++) {
						Channel cnel1=new Channel();
						ChannelExt ext1=new ChannelExt();
						cnel1.setDisplay(true);
						cnel1.setPath(c.getPath()+m1[index]);
						cnel1.setPriority(10);
						ext1.setAllowUpdown(true);
						ext1.setContentImgHeight(310);
						ext1.setContentImgWidth(310);
						ext1.setName(mn1[index]);
						ext1.setPageSize(10);
						ext1.setTitleImgHeight(139);
						ext1.setTitleImgWidth(139);
						
						
						// 加上模板前缀
						tplPath = site.getTplPath();
						if (!StringUtils.isBlank(ext1.getTplChannel())) {
							ext1.setTplChannel(tplPath + ext1.getTplChannel());
						}
						if (!StringUtils.isBlank(ext1.getTplContent())) {
							ext1.setTplContent(tplPath + ext1.getTplContent());
						}
						if (!StringUtils.isBlank(ext1.getTplMobileChannel())) {
							ext1.setTplMobileChannel(tplPath + ext1.getTplMobileChannel());
						}
						if(tpls!=null&&tpls.length>0){
							for(int t=0;t<tpls.length;t++){
								if (!StringUtils.isBlank(tpls[t])) {
									tpls[t]=tplPath+tpls[t];
								}
							}
						}
						if(mtpls!=null&&mtpls.length>0){
							for(int t=0;t<mtpls.length;t++){
								if (!StringUtils.isBlank(mtpls[t])) {
									mtpls[t]=tplPath+mtpls[t];
								}
							}
						}
						cnel1.setAttr(RequestUtils.getRequestMap(request, "attr_"));
						
						try {
							cnel1=manager.save(cnel1, ext1, new ChannelTxt(), null, contriGroupIds, null, siteId, cnel.getId(), modelId, workflowId, modelIds, tpls, mtpls);
						} catch (Exception e) {
							e.printStackTrace();
						}
						
						log.info("save Channel id={}, name={}", cnel1.getId(), cnel1.getName());
						cmsLogMng.operating(request, "channel.log.save", "id=" + cnel1.getId()
								+ ";title=" + cnel1.getTitle());
						if(index==5){//党建动态
							String[] is={"sj","xj","jcs"};
							String[] in={"市局","县局","基层所"};
							for(int b=0;b<is.length;b++){
								Channel cnel2=new Channel();
								ChannelExt ext2=new ChannelExt();
								cnel2.setDisplay(true);
								cnel2.setPath(c.getPath()+is[b]);
								cnel2.setPriority(10);
								ext2.setAllowUpdown(true);
								ext2.setContentImgHeight(310);
								ext2.setContentImgWidth(310);
								ext2.setName(in[b]);
								
								ext2.setPageSize(10);
								ext2.setTitleImgHeight(139);
								ext2.setTitleImgWidth(139);
								
								
								// 加上模板前缀
								tplPath = site.getTplPath();
								if (!StringUtils.isBlank(ext2.getTplChannel())) {
									ext2.setTplChannel(tplPath + ext2.getTplChannel());
								}
								if (!StringUtils.isBlank(ext2.getTplContent())) {
									ext2.setTplContent(tplPath + ext2.getTplContent());
								}
								if (!StringUtils.isBlank(ext2.getTplMobileChannel())) {
									ext2.setTplMobileChannel(tplPath + ext2.getTplMobileChannel());
								}
								cnel2.setAttr(RequestUtils.getRequestMap(request, "attr_"));
								
								try {
									cnel2=manager.save(cnel2, ext2, new ChannelTxt(), null, contriGroupIds, null, siteId, cnel1.getId(), modelId, workflowId, modelIds, tpls, mtpls);
								} catch (Exception e) {
									e.printStackTrace();
								}
								
								log.info("save Channel id={}, name={}", cnel2.getId(), cnel2.getName());
								cmsLogMng.operating(request, "channel.log.save", "id=" + cnel2.getId()
										+ ";title=" + cnel2.getTitle());
								
							}
						}
					}
				}else if(q==1){//从严治党
					for (int index=0;index<m2.length;index++) {
							Channel cnel1=new Channel();
							ChannelExt ext1=new ChannelExt();
							cnel1.setDisplay(true);
							cnel1.setPath(c.getPath()+m2[index]);
							cnel1.setPriority(10);
							ext1.setAllowUpdown(true);
							ext1.setContentImgHeight(310);
							ext1.setContentImgWidth(310);
							ext1.setName(mn2[index]);
							ext1.setPageSize(10);
							ext1.setTitleImgHeight(139);
							ext1.setTitleImgWidth(139);
							
							
							// 加上模板前缀
							tplPath = site.getTplPath();
							if (!StringUtils.isBlank(ext1.getTplChannel())) {
								ext1.setTplChannel(tplPath + ext1.getTplChannel());
							}
							if (!StringUtils.isBlank(ext1.getTplContent())) {
								ext1.setTplContent(tplPath + ext1.getTplContent());
							}
							if (!StringUtils.isBlank(ext1.getTplMobileChannel())) {
								ext1.setTplMobileChannel(tplPath + ext1.getTplMobileChannel());
							}
							if(tpls!=null&&tpls.length>0){
								for(int t=0;t<tpls.length;t++){
									if (!StringUtils.isBlank(tpls[t])) {
										tpls[t]=tplPath+tpls[t];
									}
								}
							}
							if(mtpls!=null&&mtpls.length>0){
								for(int t=0;t<mtpls.length;t++){
									if (!StringUtils.isBlank(mtpls[t])) {
										mtpls[t]=tplPath+mtpls[t];
									}
								}
							}
							cnel1.setAttr(RequestUtils.getRequestMap(request, "attr_"));
							
							try {
								cnel1=manager.save(cnel1, ext1, new ChannelTxt(), null, contriGroupIds, null, siteId, cnel.getId(), modelId, workflowId, modelIds, tpls, mtpls);
							} catch (Exception e) {
								e.printStackTrace();
							}
							
							log.info("save Channel id={}, name={}", cnel1.getId(), cnel1.getName());
							cmsLogMng.operating(request, "channel.log.save", "id=" + cnel1.getId()
									+ ";title=" + cnel1.getTitle());
							if(index==3){//基层组织建设
								String[] is={"sx","zz","zf","ffcl","zdjs"};
								String[] in={"思想","组织","作风","反腐倡廉","制度建设"};
								for(int b=0;b<is.length;b++){
									Channel cnel2=new Channel();
									ChannelExt ext2=new ChannelExt();
									cnel2.setDisplay(true);
									cnel2.setPath(c.getPath()+is[b]);
									cnel2.setPriority(10);
									ext2.setAllowUpdown(true);
									ext2.setContentImgHeight(310);
									ext2.setContentImgWidth(310);
									ext2.setName(in[b]);
									
									ext2.setPageSize(10);
									ext2.setTitleImgHeight(139);
									ext2.setTitleImgWidth(139);
									
									
									// 加上模板前缀
									tplPath = site.getTplPath();
									if (!StringUtils.isBlank(ext2.getTplChannel())) {
										ext2.setTplChannel(tplPath + ext2.getTplChannel());
									}
									if (!StringUtils.isBlank(ext2.getTplContent())) {
										ext2.setTplContent(tplPath + ext2.getTplContent());
									}
									if (!StringUtils.isBlank(ext2.getTplMobileChannel())) {
										ext2.setTplMobileChannel(tplPath + ext2.getTplMobileChannel());
									}
									if(tpls!=null&&tpls.length>0){
										for(int t=0;t<tpls.length;t++){
											if (!StringUtils.isBlank(tpls[t])) {
												tpls[t]=tplPath+tpls[t];
											}
										}
									}
									if(mtpls!=null&&mtpls.length>0){
										for(int t=0;t<mtpls.length;t++){
											if (!StringUtils.isBlank(mtpls[t])) {
												mtpls[t]=tplPath+mtpls[t];
											}
										}
									}
									cnel2.setAttr(RequestUtils.getRequestMap(request, "attr_"));
									
									try {
										cnel2=manager.save(cnel2, ext2, new ChannelTxt(), null, contriGroupIds, null, siteId, cnel1.getId(), modelId, workflowId, modelIds, tpls, mtpls);
									} catch (Exception e) {
										e.printStackTrace();
									}
									
									log.info("save Channel id={}, name={}", cnel2.getId(), cnel2.getName());
									cmsLogMng.operating(request, "channel.log.save", "id=" + cnel2.getId()
											+ ";title=" + cnel2.getTitle());
									
								}
							}	
							if(index==4){//三型党组织建设
								String[] is={"xxx","fwx","cxx"};
								String[] in={"学习型","服务型","创新型"};
								for(int b=0;b<is.length;b++){
									Channel cnel2=new Channel();
									ChannelExt ext2=new ChannelExt();
									cnel2.setDisplay(true);
									cnel2.setPath(c.getPath()+is[b]);
									cnel2.setPriority(10);
									ext2.setAllowUpdown(true);
									ext2.setContentImgHeight(310);
									ext2.setContentImgWidth(310);
									ext2.setName(in[b]);
									
									ext2.setPageSize(10);
									ext2.setTitleImgHeight(139);
									ext2.setTitleImgWidth(139);
									
									
									// 加上模板前缀
									tplPath = site.getTplPath();
									if (!StringUtils.isBlank(ext2.getTplChannel())) {
										ext2.setTplChannel(tplPath + ext2.getTplChannel());
									}
									if (!StringUtils.isBlank(ext2.getTplContent())) {
										ext2.setTplContent(tplPath + ext2.getTplContent());
									}
									if (!StringUtils.isBlank(ext2.getTplMobileChannel())) {
										ext2.setTplMobileChannel(tplPath + ext2.getTplMobileChannel());
									}
									if(tpls!=null&&tpls.length>0){
										for(int t=0;t<tpls.length;t++){
											if (!StringUtils.isBlank(tpls[t])) {
												tpls[t]=tplPath+tpls[t];
											}
										}
									}
									if(mtpls!=null&&mtpls.length>0){
										for(int t=0;t<mtpls.length;t++){
											if (!StringUtils.isBlank(mtpls[t])) {
												mtpls[t]=tplPath+mtpls[t];
											}
										}
									}
									cnel2.setAttr(RequestUtils.getRequestMap(request, "attr_"));
									
									try {
										cnel2=manager.save(cnel2, ext2, new ChannelTxt(), null, contriGroupIds, null, siteId, cnel1.getId(), modelId, workflowId, modelIds, tpls, mtpls);
									} catch (Exception e) {
										e.printStackTrace();
									}
									
									log.info("save Channel id={}, name={}", cnel2.getId(), cnel2.getName());
									cmsLogMng.operating(request, "channel.log.save", "id=" + cnel2.getId()
											+ ";title=" + cnel2.getTitle());
									
								}
							}
					}
				}else if(q==2){//党务管理
					
						for (int index=0;index<m3.length;index++) {
							Channel cnel1=new Channel();
							ChannelExt ext1=new ChannelExt();
							cnel1.setDisplay(true);
							cnel1.setPath(c.getPath()+m3[index]);
							cnel1.setPriority(10);
							ext1.setAllowUpdown(true);
							ext1.setContentImgHeight(310);
							ext1.setContentImgWidth(310);
							ext1.setName(mn3[index]);
							
							ext1.setPageSize(10);
							ext1.setTitleImgHeight(139);
							ext1.setTitleImgWidth(139);
							
							
							// 加上模板前缀
							tplPath = site.getTplPath();
							if (!StringUtils.isBlank(ext1.getTplChannel())) {
								ext1.setTplChannel(tplPath + ext1.getTplChannel());
							}
							if (!StringUtils.isBlank(ext1.getTplContent())) {
								ext1.setTplContent(tplPath + ext1.getTplContent());
							}
							if (!StringUtils.isBlank(ext1.getTplMobileChannel())) {
								ext1.setTplMobileChannel(tplPath + ext1.getTplMobileChannel());
							}
							if(tpls!=null&&tpls.length>0){
								for(int t=0;t<tpls.length;t++){
									if (!StringUtils.isBlank(tpls[t])) {
										tpls[t]=tplPath+tpls[t];
									}
								}
							}
							if(mtpls!=null&&mtpls.length>0){
								for(int t=0;t<mtpls.length;t++){
									if (!StringUtils.isBlank(mtpls[t])) {
										mtpls[t]=tplPath+mtpls[t];
									}
								}
							}
							cnel1.setAttr(RequestUtils.getRequestMap(request, "attr_"));
							
							try {
								cnel1=manager.save(cnel1, ext1, new ChannelTxt(), null, contriGroupIds, null, siteId, cnel.getId(), modelId, workflowId, modelIds, tpls, mtpls);
							} catch (Exception e) {
								e.printStackTrace();
							}
							
							log.info("save Channel id={}, name={}", cnel1.getId(), cnel1.getName());
							cmsLogMng.operating(request, "channel.log.save", "id=" + cnel1.getId()
									+ ";title=" + cnel1.getTitle());
							
					}
				} else if (q==3) {//党建风采
						for (int index=0;index<m4.length;index++) {
							Channel cnel1=new Channel();
							ChannelExt ext1=new ChannelExt();
							cnel1.setDisplay(true);
							cnel1.setPath(c.getPath()+m4[index]);
							cnel1.setPriority(10);
							ext1.setAllowUpdown(true);
							ext1.setContentImgHeight(310);
							ext1.setContentImgWidth(310);
							ext1.setName(mn4[index]);
							ext1.setPageSize(10);
							ext1.setTitleImgHeight(139);
							ext1.setTitleImgWidth(139);
							
							
							// 加上模板前缀
							tplPath = site.getTplPath();
							if (!StringUtils.isBlank(ext1.getTplChannel())) {
								ext1.setTplChannel(tplPath + ext1.getTplChannel());
							}
							if (!StringUtils.isBlank(ext1.getTplContent())) {
								ext1.setTplContent(tplPath + ext1.getTplContent());
							}
							if (!StringUtils.isBlank(ext1.getTplMobileChannel())) {
								ext1.setTplMobileChannel(tplPath + ext1.getTplMobileChannel());
							}
							if(tpls!=null&&tpls.length>0){
								for(int t=0;t<tpls.length;t++){
									if (!StringUtils.isBlank(tpls[t])) {
										tpls[t]=tplPath+tpls[t];
									}
								}
							}
							if(mtpls!=null&&mtpls.length>0){
								for(int t=0;t<mtpls.length;t++){
									if (!StringUtils.isBlank(mtpls[t])) {
										mtpls[t]=tplPath+mtpls[t];
									}
								}
							}
							cnel1.setAttr(RequestUtils.getRequestMap(request, "attr_"));
							
							try {
								cnel1=manager.save(cnel1, ext1, new ChannelTxt(), null, contriGroupIds, null, siteId, cnel.getId(), modelId, workflowId, modelIds, tpls, mtpls);
							} catch (Exception e) {
								e.printStackTrace();
							}
							
							log.info("save Channel id={}, name={}", cnel1.getId(), cnel1.getName());
							cmsLogMng.operating(request, "channel.log.save", "id=" + cnel1.getId()
									+ ";title=" + cnel1.getTitle());
							
							if(index==4){//创先争优
								String[] is={"sjdzbgzf","sjdjjtgr","sjdjsfckxfg"};
								String[] in={"十佳党支部工作法","十佳党建集体/个人","十佳党建师范窗口/先锋岗"};
								for(int b=0;b<is.length;b++){
									Channel cnel2=new Channel();
									ChannelExt ext2=new ChannelExt();
									cnel2.setDisplay(true);
									cnel2.setPath(c.getPath()+is[b]);
									cnel2.setPriority(10);
									ext2.setAllowUpdown(true);
									ext2.setContentImgHeight(310);
									ext2.setContentImgWidth(310);
									ext2.setName(in[b]);
									
									ext2.setPageSize(10);
									ext2.setTitleImgHeight(139);
									ext2.setTitleImgWidth(139);
									
									
									// 加上模板前缀
									tplPath = site.getTplPath();
									if (!StringUtils.isBlank(ext2.getTplChannel())) {
										ext2.setTplChannel(tplPath + ext2.getTplChannel());
									}
									if (!StringUtils.isBlank(ext2.getTplContent())) {
										ext2.setTplContent(tplPath + ext2.getTplContent());
									}
									if (!StringUtils.isBlank(ext2.getTplMobileChannel())) {
										ext2.setTplMobileChannel(tplPath + ext2.getTplMobileChannel());
									}
									if(tpls!=null&&tpls.length>0){
										for(int t=0;t<tpls.length;t++){
											if (!StringUtils.isBlank(tpls[t])) {
												tpls[t]=tplPath+tpls[t];
											}
										}
									}
									if(mtpls!=null&&mtpls.length>0){
										for(int t=0;t<mtpls.length;t++){
											if (!StringUtils.isBlank(mtpls[t])) {
												mtpls[t]=tplPath+mtpls[t];
											}
										}
									}
									cnel2.setAttr(RequestUtils.getRequestMap(request, "attr_"));
									
									try {
										cnel2=manager.save(cnel2, ext2, new ChannelTxt(), null, contriGroupIds, null, siteId, cnel1.getId(), modelId, workflowId, modelIds, tpls, mtpls);
									} catch (Exception e) {
										e.printStackTrace();
									}
									
									log.info("save Channel id={}, name={}", cnel2.getId(), cnel2.getName());
									cmsLogMng.operating(request, "channel.log.save", "id=" + cnel2.getId()
											+ ";title=" + cnel2.getTitle());
								}
							}
					}
				}  else if (q==4) {//网上党校
					for (int index=0;index<m5.length;index++) {
						Channel cnel1=new Channel();
						ChannelExt ext1=new ChannelExt();
						cnel1.setDisplay(true);
						cnel1.setPath(c.getPath()+m5[index]);
						cnel1.setPriority(10);
						ext1.setAllowUpdown(true);
						ext1.setContentImgHeight(310);
						ext1.setContentImgWidth(310);
						ext1.setName(mn5[index]);
						ext1.setPageSize(10);
						ext1.setTitleImgHeight(139);
						ext1.setTitleImgWidth(139);
						
						
						// 加上模板前缀
						tplPath = site.getTplPath();
						if (!StringUtils.isBlank(ext1.getTplChannel())) {
							ext1.setTplChannel(tplPath + ext1.getTplChannel());
						}
						if (!StringUtils.isBlank(ext1.getTplContent())) {
							ext1.setTplContent(tplPath + ext1.getTplContent());
						}
						if (!StringUtils.isBlank(ext1.getTplMobileChannel())) {
							ext1.setTplMobileChannel(tplPath + ext1.getTplMobileChannel());
						}
						if(tpls!=null&&tpls.length>0){
							for(int t=0;t<tpls.length;t++){
								if (!StringUtils.isBlank(tpls[t])) {
									tpls[t]=tplPath+tpls[t];
								}
							}
						}
						if(mtpls!=null&&mtpls.length>0){
							for(int t=0;t<mtpls.length;t++){
								if (!StringUtils.isBlank(mtpls[t])) {
									mtpls[t]=tplPath+mtpls[t];
								}
							}
						}
						cnel1.setAttr(RequestUtils.getRequestMap(request, "attr_"));
						
						try {
							cnel1=manager.save(cnel1, ext1, new ChannelTxt(), null, contriGroupIds, null, siteId, cnel.getId(), modelId, workflowId, modelIds, tpls, mtpls);
						} catch (Exception e) {
							e.printStackTrace();
						}
						
						log.info("save Channel id={}, name={}", cnel1.getId(), cnel1.getName());
						cmsLogMng.operating(request, "channel.log.save", "id=" + cnel1.getId()
								+ ";title=" + cnel1.getTitle());
					}
				}else if (q==5){//文化生活
					for (int index=0;index<m6.length;index++) {
						Channel cnel1=new Channel();
						ChannelExt ext1=new ChannelExt();
						cnel1.setDisplay(true);
						cnel1.setPath(c.getPath()+m6[index]);
						cnel1.setPriority(10);
						ext1.setAllowUpdown(true);
						ext1.setContentImgHeight(310);
						ext1.setContentImgWidth(310);
						ext1.setName(mn6[index]);
						ext1.setPageSize(10);
						ext1.setTitleImgHeight(139);
						ext1.setTitleImgWidth(139);
						
						
						// 加上模板前缀
						tplPath = site.getTplPath();
						if (!StringUtils.isBlank(ext1.getTplChannel())) {
							ext1.setTplChannel(tplPath + ext1.getTplChannel());
						}
						if (!StringUtils.isBlank(ext1.getTplContent())) {
							ext1.setTplContent(tplPath + ext1.getTplContent());
						}
						if (!StringUtils.isBlank(ext1.getTplMobileChannel())) {
							ext1.setTplMobileChannel(tplPath + ext1.getTplMobileChannel());
						}
						if(tpls!=null&&tpls.length>0){
							for(int t=0;t<tpls.length;t++){
								if (!StringUtils.isBlank(tpls[t])) {
									tpls[t]=tplPath+tpls[t];
								}
							}
						}
						if(mtpls!=null&&mtpls.length>0){
							for(int t=0;t<mtpls.length;t++){
								if (!StringUtils.isBlank(mtpls[t])) {
									mtpls[t]=tplPath+mtpls[t];
								}
							}
						}
						cnel1.setAttr(RequestUtils.getRequestMap(request, "attr_"));
						
						try {
							cnel1=manager.save(cnel1, ext1, new ChannelTxt(), null, contriGroupIds, null, siteId, cnel.getId(), modelId, workflowId, modelIds, tpls, mtpls);
						} catch (Exception e) {
							e.printStackTrace();
						}
						
						log.info("save Channel id={}, name={}", cnel1.getId(), cnel1.getName());
						cmsLogMng.operating(request, "channel.log.save", "id=" + cnel1.getId()
								+ ";title=" + cnel1.getTitle());
					    }
				    }      
			    }	
		    }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * 省、市批量添加栏目
	 */
	@RequiresPermissions("channel:o_singleLanMuSave.do")
	@RequestMapping("/channel/o_singleLanMuSave.do")
	public String batchLamMu(String cid, 
			ChannelTxt txt, Integer[] viewGroupIds,
			Integer[] userIds,Integer workflowId,
			Integer[] modelIds,String[] tpls, String[] mtpls, HttpServletRequest request,ModelMap model,String siteId) {
		model.addAttribute("beginTime", new SimpleDateFormat("HH:mm:ss").format(new Date()));
		/*String[]maps={"djzc","cyzd","dwgl","djfc","wsdx","whsh"};
		String[]mn={"党建之窗","从严治党","党务管理","党建风采","网上党校","文化生活"};

		String[]m1={"tpxw","djdt","ldjh","jyjl","mtsd","lyyt","dzqk"};
		String[]mn1={"图片新闻","党建动态","领导讲话","经验交流","媒体视点","理论研讨","电子期刊"};
		
		String[]m2={"qwsy","djzr","gfhdzbjs","jcdzzjs","sxdzzjs"};
		String[]mn2={"权威声音","党建责任","规范化党支部建设","基层党组织建设","三型党组织建设"};

		
		String[]m3={"tzgg","djwj","djhy","dwpx","dsdjt","dsj","dwgk"};
		String[]mn3={"通知公告","党建文件","党建会议","党务培训","地税大讲堂","大事记","党务公开"};*/
		
		String[] m4={"dyh"};
		String[] mn4={"订阅号"};
		
		/*String[]m5={"xjpxljh","dsbl","dwzn","dgdz","ddzs","dkhc","dbyl","gxjd","tszs","hstj"};
		String[]mn5={"习近平系列讲话","党史博览","党务指南","党规党章","党的知识","党课荟萃","党报要论","国学经典","他山之石","好书推荐"};
		String[]m6={"shzp","wxyd","hsly","jkys","syzp","gmsd"};
		String[]mn6={"书画作品","文学园地","红色旅游","健康养生","摄影作品","革命圣地"};
	    */
		Integer[] contriGroupIds={1,3};
		int modelId=1;
		String[] ids = cid.trim().split(",");
		try {	
			CmsSite site = CmsUtils.getSite(request);
			Integer userId = CmsUtils.getUserId(request);
			Integer siteValue = Integer.parseInt(siteId);
		    for (int j=0;j<ids.length;j++){
				int root = Integer.valueOf(ids[j]);
				Channel c = manager.findById(root);
			
			    for (int q=0;q<m4.length;q++){
				
					Channel cnel=new Channel();
					ChannelExt ext=new ChannelExt();
					cnel.setDisplay(true);
					cnel.setPath(c.getPath()+m4[q]);
					cnel.setPriority(10);
					ext.setAllowUpdown(true);
					ext.setContentImgHeight(310);
					ext.setContentImgWidth(310);
					ext.setName(mn4[q]);
					ext.setPageSize(10);
					ext.setTitleImgHeight(139);
					ext.setTitleImgWidth(139);
					
					
					// 加上模板前缀
					String tplPath = site.getTplPath();
					if (!StringUtils.isBlank(ext.getTplChannel())) {
						ext.setTplChannel(tplPath + ext.getTplChannel());
					}
					if (!StringUtils.isBlank(ext.getTplContent())) {
						ext.setTplContent(tplPath + ext.getTplContent());
					}
					if (!StringUtils.isBlank(ext.getTplMobileChannel())) {
						ext.setTplMobileChannel(tplPath + ext.getTplMobileChannel());
					}
					if(tpls!=null && tpls.length>0){
						for(int t=0;t<tpls.length;t++){
							if (!StringUtils.isBlank(tpls[t])) {
								tpls[t] = tplPath+tpls[t];
							}
						}
					}
					if(mtpls!=null && mtpls.length>0){
						for(int t=0;t<mtpls.length;t++){
							if (!StringUtils.isBlank(mtpls[t])) {
								mtpls[t] = tplPath+mtpls[t];
							}
						}
					}
				    cnel.setAttr(RequestUtils.getRequestMap(request, "attr_"));
				
				try {
					cnel=manager.save(cnel, ext, new ChannelTxt(), null, contriGroupIds, null, siteValue, root, modelId, workflowId, modelIds, tpls, mtpls);
				} catch (Exception e) {
					e.printStackTrace();
				}
				Integer did =c.getParent().getDepartId();
				CmsDepartment cmsDepartment =cmsDepartmentMng.findById(did);
				if(cmsDepartment.getSddspoOrgtype()!="支部"){
					Channel c1 = manager.getfindBychannel(root);
					manager.savechannel(c1.getId(), cmsDepartment.getId());
				}
				
				log.info("save Channel id={}, name={}", cnel.getId(), cnel.getName());
				cmsLogMng.operating(request, "channel.log.save", "id=" + cnel.getId()
						+ ";title=" + cnel.getTitle());
			    }
			   // String sql1 = "select * from jc";
			    //String sql = "insert into jc_channel_department values("+;
		    }
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
		model.addAttribute("endTime", new SimpleDateFormat("HH:mm:ss").format(new Date()));
		model.addAttribute("message","添加栏目完成");
		
		return "channel/branchLanMu";
	}
	//添加支部
	public void addBranches(String cid, 
			ChannelTxt txt, Integer[] viewGroupIds,
			Integer[] userIds,Integer workflowId,
			Integer[] modelIds,String[] tpls, String[] mtpls, HttpServletRequest request,ModelMap model,Integer siteId){
		String [] names={"图片新闻","支部动态","通知公告","党费缴纳","党务公开","特色工作","制度建设"};
		String [] path={"tpxw","zbdt","tzgo","dfjn","dwgk","tsgz","zdjs"};
		Integer [] contriGroupIds={1,3};
		int modelId=1;
		String[]ids=cid.trim().split(",");
		try {
			
			CmsSite site = CmsUtils.getSite(request);
		for (int j=0;j<ids.length;j++) {
			int root=Integer.valueOf(ids[j]);
			Channel c=manager.findById(root);
			
			for(int i=0;i<names.length;i++){
				Channel cnel=new Channel();
				ChannelExt ext=new ChannelExt();
				cnel.setDisplay(true);
				cnel.setPath(c.getPath()+path[i]);
				cnel.setPriority(10);
				ext.setAllowUpdown(true);
				ext.setContentImgHeight(310);
				ext.setContentImgWidth(310);
				ext.setName(names[i]);
				ext.setPageSize(10);
				ext.setTitleImgHeight(139);
				ext.setTitleImgWidth(139);
				
				
				// 加上模板前缀
				String tplPath = site.getTplPath();
				if (!StringUtils.isBlank(ext.getTplChannel())) {
					ext.setTplChannel(tplPath + ext.getTplChannel());
				}
				if (!StringUtils.isBlank(ext.getTplContent())) {
					ext.setTplContent(tplPath + ext.getTplContent());
				}
				if (!StringUtils.isBlank(ext.getTplMobileChannel())) {
					ext.setTplMobileChannel(tplPath + ext.getTplMobileChannel());
				}
				if(tpls!=null&&tpls.length>0){
					for(int t=0;t<tpls.length;t++){
						if (!StringUtils.isBlank(tpls[t])) {
							tpls[t]=tplPath+tpls[t];
						}
					}
				}
				if(mtpls!=null&&mtpls.length>0){
					for(int t=0;t<mtpls.length;t++){
						if (!StringUtils.isBlank(mtpls[t])) {
							mtpls[t]=tplPath+mtpls[t];
						}
					}
				}
				cnel.setAttr(RequestUtils.getRequestMap(request, "attr_"));
				
				try {
					manager.save(cnel, ext, new ChannelTxt(), null, contriGroupIds, null, siteId, root, modelId, workflowId, modelIds, tpls, mtpls);
				} catch (Exception e) {
					e.printStackTrace();
				}
				log.info("save Channel id={}, name={}", cnel.getId(), cnel.getName());
				cmsLogMng.operating(request, "channel.log.save", "id=" + cnel.getId()
						+ ";title=" + cnel.getTitle());			
			}		
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Autowired
	private CmsUserMng cmsUserMng;
	@Autowired
	private CmsModelMng cmsModelMng;
	@Autowired
	private CmsModelItemMng cmsModelItemMng;
	@Autowired
	private CmsGroupMng cmsGroupMng;
	@Autowired
	private TplManager tplManager;
	@Autowired
	private CmsLogMng cmsLogMng;
	@Autowired
	private ChannelMng manager;
	@Autowired
	private CmsWorkflowMng workflowMng;
	@Autowired
	private CmsDepartmentMng cmsDepartmentMng;
}