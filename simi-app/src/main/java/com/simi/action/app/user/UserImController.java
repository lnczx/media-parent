package com.simi.action.app.user;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageInfo;
import com.meijia.utils.MobileUtil;
import com.meijia.utils.StringUtil;
import com.meijia.utils.huanxin.EasemobMessages;
import com.simi.action.app.BaseController;
import com.simi.common.ConstantMsg;
import com.simi.common.Constants;
import com.simi.po.model.user.UserImHistory;
import com.simi.po.model.user.UserImLast;
import com.simi.po.model.user.UserRef3rd;
import com.simi.po.model.user.Users;
import com.simi.service.user.UserImHistoryService;
import com.simi.service.user.UserImLastService;
import com.simi.service.user.UserRef3rdService;
import com.simi.service.user.UsersService;
import com.simi.vo.AppResultData;
import com.simi.vo.user.UserImLastSearchVo;
import com.simi.vo.user.UserImLastVo;


@Controller
@RequestMapping(value = "/app/user")
public class UserImController extends BaseController {
	
	@Autowired
	private UsersService userService;
	
	@Autowired
	private UserImHistoryService userImHistoryService;	
	
	@Autowired
	private UserRef3rdService userRef3rdService;	
	
	@Autowired
	private UserImLastService userImLastService;
	
	/**
	 * 发送IM消息
	 */
	@RequestMapping(value = "send_im", method = RequestMethod.GET)
	public AppResultData<Object> sendImToRobot(
			@RequestParam("user_id") Long userId,
			@RequestParam("im_username_from") String imUsernameFrom,
			@RequestParam("im_username_to") String imUsernameTo,
			@RequestParam("msg") String msg) {

		AppResultData<Object> result = new AppResultData<Object>(
				Constants.SUCCESS_0, ConstantMsg.SUCCESS_0_MSG, new String());

		Users u = userService.selectByPrimaryKey(userId);
		if (u == null) {
			result.setStatus(Constants.ERROR_999);
			result.setMsg(ConstantMsg.USER_NOT_EXIST_MG);
			return result;
		}

		if (StringUtil.isEmpty(imUsernameFrom)
				|| StringUtil.isEmpty(imUsernameTo)) {
			result.setStatus(Constants.ERROR_999);
			result.setMsg(ConstantMsg.IM_USER_NOT_EXIST_MG);
			return result;
		}

		try {
			msg = URLDecoder.decode(msg, Constants.URL_ENCODE);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// 给用户发一条文本消息
		EasemobMessages.sendMessageTxt(imUsernameFrom, imUsernameTo, msg);
		return result;
	}
	
	
	
	/**
	 * 两个用户的聊天记录
	 * 
	 * @param from_im_user
	 * @param to_im_user
	 * @return
	 */

	@RequestMapping(value = "get_im_history", method = RequestMethod.GET)
	public AppResultData<Object> getImHistory(
			@RequestParam("from_im_user") String fromImUser,
			@RequestParam("to_im_user") String toImUser,
			@RequestParam(value = "page", required = false, defaultValue = "0") int page) {

		AppResultData<Object> result = new AppResultData<Object>(Constants.SUCCESS_0, "", "");

		int pageNo = page;
		int pageSize = Constants.PAGE_MAX_NUMBER;
		
		PageInfo list = userImHistoryService.selectByImUserListPage(fromImUser, toImUser, pageNo, pageSize);
		
		List<UserImHistory> imHistoryList = list.getList();
		
		result.setData(imHistoryList);
		
		return result;
	}
	
	/**
	 * 获取当前用户与好友的最新一条聊天记录
	 * 
	 * @param user_id
	 * @return
	 */

	@RequestMapping(value = "get_im_last", method = RequestMethod.GET)
	public AppResultData<Object> getUserAndIm(
			@RequestParam("user_id") Long userId) {

		AppResultData<Object> result = new AppResultData<Object>(Constants.SUCCESS_0, "", "");

		List<UserImLastVo> list = userImLastService.getLastIm(userId);
		result.setData(list);
		
		return result;
	}	
	
	/**
	 * 获取当前用户与好友的最新一条聊天记录
	 * 
	 * @param user_id
	 * @return
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */

	@RequestMapping(value = "get_im_profile", method = RequestMethod.GET)
	public AppResultData<Object> getImProfile(
			@RequestParam("im_username") String imUserName) throws JsonParseException, JsonMappingException, IOException {

		AppResultData<Object> result = new AppResultData<Object>(Constants.SUCCESS_0, "", "");

		UserRef3rd userRef3rd = userRef3rdService.selectByUserNameAnd3rdType(imUserName, "huanxin");
		
		if (userRef3rd == null) {
			return result;
		}
		
		
		Long userId = userRef3rd.getUserId();
		
		Users u = userService.selectByPrimaryKey(userId);
		
		if (u == null) return result;
		
		HashMap<String, String> imProfile = new HashMap<String, String>();
		imProfile.put("user_id", u.getId().toString());
		imProfile.put("mobile", u.getMobile());
		imProfile.put("sex", u.getSex());
		imProfile.put("im_username", imUserName);
		imProfile.put("name", StringUtil.isEmpty(u.getName()) ? MobileUtil.getMobileStar(u.getMobile()) : u.getName());
		imProfile.put("head_img", userService.getHeadImg(u));
		imProfile.put("user_type", u.getUserType().toString());
		result.setData(imProfile);
		
		return result;
	}		
	
	
	@RequestMapping(value = "gen_last_im", method = RequestMethod.GET)
	public AppResultData<Object> genLastIm() throws JsonParseException, JsonMappingException, IOException {

		AppResultData<Object> result = new AppResultData<Object>(Constants.SUCCESS_0, "", "");

		List<UserImHistory> list = userImHistoryService.getAllImUserLastIm();
		
		ObjectMapper mapper = new ObjectMapper();
		UserImHistory vo = null;
		for (int i =0; i < list.size(); i++) {
			vo  = list.get(i);
			String fromImUser = vo.getFromImUser();
			String toImUser = vo.getToImUser();
			
			if (StringUtil.isEmpty(fromImUser)) continue;
			if (StringUtil.isEmpty(toImUser)) continue;
			
			
			UserRef3rd fromUser = 	userRef3rdService.selectByUserNameAnd3rdType(fromImUser, "huanxin");
	        UserRef3rd toUser = 	userRef3rdService.selectByUserNameAnd3rdType(toImUser, "huanxin");
	        
	        if (fromUser == null) continue;
	        if (toUser == null) continue;
	        
	        Long fromUserId = fromUser.getUserId();
	        Long toUserId = toUser.getUserId();
	        
	        String chatType = vo.getChatType();
	        String msgId = vo.getMsgId();
	        String content = vo.getImContent();
	        Long addTime = vo.getAddTime();
	        String uuid = vo.getUuid();
	        
	        JsonNode dataJson = mapper.readValue(content, JsonNode.class);
	        String imContent = userImHistoryService.getImMsg(dataJson);
	        //先插入fromUser -> toUser.
	          UserImLastSearchVo searchVo = new UserImLastSearchVo();
	          searchVo.setFromUserId(fromUserId);
	          searchVo.setToUserId(toUserId);
	          UserImLast fromUserPo = userImLastService.selectBySearchVo(searchVo);
	          Boolean isNew = false;
	          if (fromUserPo == null) {
	        	  fromUserPo = userImLastService.initUserImLast();
	        	  isNew = true;
	          }
	          
	          fromUserPo.setFromUserId(fromUserId);
	          fromUserPo.setToUserId(toUserId);
	          fromUserPo.setChatType(chatType);
	          fromUserPo.setFromImUser(fromImUser);
	          fromUserPo.setToImUser(toImUser);
	          fromUserPo.setMsgId(msgId);
	          fromUserPo.setImContent(imContent);
	          fromUserPo.setUuid(uuid);
	          
	          fromUserPo.setAddTime(addTime);
	          
	          if (isNew) {
	        	  userImLastService.insert(fromUserPo);
	          } else {
	        	  userImLastService.updateByPrimaryKeySelective(fromUserPo);
	          }
	          
	          //再插入 toUser -> fromuser;
	          searchVo.setFromUserId(toUserId);
	          searchVo.setToUserId(fromUserId);
	          UserImLast toUserPo = userImLastService.selectBySearchVo(searchVo);
	          isNew = false;
	          if (toUserPo == null) {
	        	  toUserPo = userImLastService.initUserImLast();
	        	  isNew = true;
	          }
	          
	          toUserPo.setFromUserId(toUserId);
	          toUserPo.setToUserId(fromUserId);
	          toUserPo.setChatType(chatType);
	          toUserPo.setFromImUser(fromImUser);
	          toUserPo.setToImUser(toImUser);
	          toUserPo.setMsgId(msgId);
	          toUserPo.setImContent(imContent);
	          toUserPo.setUuid(uuid);
	          
	          toUserPo.setAddTime(addTime);
	          
	          if (isNew) {
	        	  userImLastService.insert(toUserPo);
	          } else {
	        	  userImLastService.updateByPrimaryKeySelective(toUserPo);
	          }
	        
		}
		
		
		
		return result;
	}		

}