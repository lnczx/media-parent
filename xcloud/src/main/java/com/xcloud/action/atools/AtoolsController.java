package com.xcloud.action.atools;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.simi.po.model.op.AppTools;
import com.simi.po.model.op.UserAppTools;
import com.simi.service.op.AppToolsService;
import com.simi.service.op.UserAppToolsService;
import com.simi.vo.ApptoolsSearchVo;
import com.simi.vo.po.AppToolsVo;
import com.xcloud.action.BaseController;
import com.xcloud.auth.AccountAuth;
import com.xcloud.auth.AuthHelper;
import com.xcloud.auth.AuthPassport;
import com.xcloud.common.Constant;

@Controller
@RequestMapping(value = "/atools")
public class AtoolsController extends BaseController {
	
	@Autowired
	private AppToolsService appToolsService;
	
	@Autowired
	private UserAppToolsService userAppToolsService;
		
	@AuthPassport
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public String list(Model model, HttpServletRequest request) {
		
		model.addAttribute("requestUrl", request.getServletPath());
		model.addAttribute("requestQuery", request.getQueryString());
		
		int pageNo = ServletRequestUtils.getIntParameter(request, Constant.PAGE_NO_NAME, Constant.DEFAULT_PAGE_NO);
		int pageSize = ServletRequestUtils.getIntParameter(request, Constant.PAGE_SIZE_NAME, Constant.DEFAULT_PAGE_SIZE);
		// 获取登录的用户
		AccountAuth accountAuth = AuthHelper.getSessionAccountAuth(request);
		Long companyId = accountAuth.getCompanyId();
		Long userId = accountAuth.getUserId();
		model.addAttribute("companyId", companyId);
		model.addAttribute("userId", userId);
		
		String appType = "xcloud";
		ApptoolsSearchVo searchVo = new ApptoolsSearchVo();
		searchVo.setAppType(appType);
//		searchVo.setMenuType("t");
		searchVo.setIsOnline((short) 0);
		
		
		List<AppTools> list = appToolsService.selectBySearchVo(searchVo);
		List<AppToolsVo> result = new ArrayList<AppToolsVo>();
		for (int i = 0; i < list.size(); i++) {
			AppTools appTools = list.get(i);
			AppToolsVo vo = appToolsService.getAppToolsVo(appTools, userId);
			result.add(vo);
		}
		
		model.addAttribute("contentModel", result);
		return "atools/atools-index";
	}		
	
	/**
	 * 应用状态改变更新
	 * @param tId
	 * @param userId
	 * @param status
	 * @return
	 */
	@AuthPassport
	@RequestMapping(value = "/user_app_tools_oa", method = RequestMethod.GET)
	public String list(
			@RequestParam("t_id") Long tId,
			@RequestParam("user_id") Long userId,
			@RequestParam("status") Short status)  {
		
		
		UserAppTools userAppTools = userAppToolsService.selectByUserIdAndTid(userId,tId);
		
		UserAppTools record = userAppToolsService.initUserAppTools();
		if (userAppTools == null) {
			record.settId(tId);
			record.setUserId(userId);
			record.setStatus(status);
			userAppToolsService.insert(record);
		}else {
			userAppTools.setStatus(status);
			userAppToolsService.updateByPrimaryKeySelective(userAppTools);
		}
		return "redirect:index";
	}
	
	/*
	 *  社保公积金计算 form
	 * 
	 */
	@AuthPassport
	@RequestMapping(value = "tools-insu",method = RequestMethod.GET)
	public String toolsInsu(){
		return "atools/tools-insu";
	}
	
	@AuthPassport
	@RequestMapping(value = "tools-tax",method = RequestMethod.GET)
	public String toolsTax(){
		return "atools/tools-tax";
	}
	
	@AuthPassport
	@RequestMapping(value = "tools-year",method = RequestMethod.GET)
	public String toolsYear(){
		return "atools/tools-year";
	}
	
	@AuthPassport
	@RequestMapping(value = "tools-pay",method = RequestMethod.GET)
	public String toolsPay(){
		return "atools/tools-pay";
	}
	
	@AuthPassport
	@RequestMapping(value = "req",method = RequestMethod.GET)
	public String toolsReq(){
		return "atools/atools-req";
	}
	
	@AuthPassport
	@RequestMapping(value = "sys-intro",method = RequestMethod.GET)
	public String sysIntro(){
		return "atools/sys-intro";
	}
}
