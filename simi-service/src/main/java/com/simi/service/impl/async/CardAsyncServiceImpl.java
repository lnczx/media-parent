package com.simi.service.impl.async;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meijia.utils.GsonUtil;
import com.meijia.utils.RegexUtil;
import com.meijia.utils.SmsUtil;
import com.meijia.utils.StringUtil;
import com.meijia.utils.TimeStampUtil;
import com.meijia.utils.push.PushUtil;
import com.simi.common.Constants;
import com.simi.po.model.card.CardAttend;
import com.simi.po.model.card.CardLog;
import com.simi.po.model.card.Cards;
import com.simi.po.model.user.UserFriends;
import com.simi.po.model.user.UserPushBind;
import com.simi.po.model.user.UserRef;
import com.simi.po.model.user.Users;
import com.simi.service.ValidateService;
import com.simi.service.async.CardAsyncService;
import com.simi.service.card.CardAttendService;
import com.simi.service.card.CardLogService;
import com.simi.service.card.CardService;
import com.simi.service.dict.CityService;
import com.simi.service.user.UserFriendService;
import com.simi.service.user.UserPushBindService;
import com.simi.service.user.UserRefService;
import com.simi.service.user.UsersService;
import com.simi.utils.CardUtil;
import com.simi.vo.AppResultData;
import com.simi.vo.user.UserFriendSearchVo;
import com.simi.vo.user.UserRefSearchVo;
import com.simi.vo.user.UserSearchVo;

@Service
public class CardAsyncServiceImpl implements CardAsyncService {

	@Autowired
	public UsersService usersService;

	@Autowired
	private CardService cardService;

	@Autowired
	private CardLogService cardLogService;

	@Autowired
	CardAttendService cardAttendService;

	@Autowired
	private UserPushBindService userPushBindService;

	@Autowired
	private CityService cityService;

	@Autowired
	private UserFriendService userFriendService;

	@Autowired
	private ValidateService validateService;
	
	@Autowired
	private UserRefService userRefService;

	/**
	 * 卡片日志记录
	 */
	@Async
	@Override
	public Future<Boolean> cardLog(Long userId, Long cardId, String remarks) {

		Users createUser = usersService.selectByPrimaryKey(userId);

		Cards card = cardService.selectByPrimaryKey(cardId);

		CardLog cardLog = cardLogService.initCardLog();
		cardLog.setCardId(cardId);
		cardLog.setUserId(userId);

		String userName = createUser.getName().equals("") ? createUser.getMobile() : createUser.getName();
		cardLog.setUserName(userName);
		cardLog.setStatus(card.getStatus());
		cardLog.setRemarks(remarks);

		cardLogService.insert(cardLog);

		return new AsyncResult<Boolean>(true);
	}

	/**
	 * 卡片发送推送消息和短信消息.
	 * 
	 * @param card
	 *            卡片对象
	 * @param pushToApp
	 *            是否给app发送消息
	 * @param pushToSms
	 *            是否发送sms短信消息
	 * @param pushToIos
	 *            是否给app发送离线消息，此场景应用为当ios app被杀死的情况下，ios无法设定闹钟，则进行后台推送提醒。
	 */
	@Async
	@Override
	public Future<Boolean> cardNotification(Cards card) {

		// 1找出所有需要通知的用户集合 users
		Long cardId = card.getCardId();
		List<CardAttend> attends = cardAttendService.selectByCardId(cardId);

		if (attends.isEmpty())
			return new AsyncResult<Boolean>(true);

		List<Long> userIds = new ArrayList<Long>();
		for (CardAttend item : attends) {
			if (!userIds.equals(item.getUserId()))
				userIds.add(item.getUserId());
		}

		// 2.找出可以发推送消息的用户集合 userPushBinds
		List<UserPushBind> userPushBinds = userPushBindService.selectByUserIds(userIds);
		List<Long> userPushIds = new ArrayList<Long>();
		if (!userPushBinds.isEmpty()) {
			for (UserPushBind p : userPushBinds) {
				if (!userPushIds.contains(p.getUserId())) {
					userPushIds.add(p.getUserId());
				}
			}
			// 进行消息推送.
			pushToApp(card, userPushBinds);
		}

		// 如果不需要立即推送，则不需要发送短信了
		if (card.getSetNowSend().equals((short) 0)) {
			return new AsyncResult<Boolean>(true);
		}

		// 3.根据集合users 和集合userPushBinds，找出不能推送，只能发短信的用户集合C
		List<Long> userSmsIds = new ArrayList<Long>();
		userSmsIds = userIds;
		if (!userPushBinds.isEmpty()) {
			userSmsIds.removeAll(userPushIds);
		}

		if (!userSmsIds.isEmpty() && card.getSetNowSend().equals((short) 1)) {
			// 进行短信发送
			pushToSms(card, userSmsIds);
		}

		return new AsyncResult<Boolean>(true);
	}

	/**
	 * 卡片发送推送消息和短信消息.
	 * 
	 * @param card
	 *            卡片对象 给ios app发送离线消息，此场景应用为当ios app被杀死的情况下，ios无法设定闹钟，则进行后台推送提醒。
	 */
	@Async
	@Override
	public Future<Boolean> cardAlertClock(Cards card) {

		// 1找出所有需要通知的用户集合 users
		Long cardId = card.getCardId();
		List<CardAttend> attends = cardAttendService.selectByCardId(cardId);

		if (attends.isEmpty())
			return new AsyncResult<Boolean>(true);

		List<Long> userIds = new ArrayList<Long>();
		for (CardAttend item : attends) {

			// 如果已经设置过本地闹钟，则不需要服务器推送提醒
			if (item.getLocalAlarm().equals((short) 1))
				continue;

			if (!userIds.equals(item.getUserId())) {
				userIds.add(item.getUserId());
			}
		}
		
		if (userIds.isEmpty()) return new AsyncResult<Boolean>(true);

		// 2.找出可以发推送消息的用户集合 userPushBinds
		List<UserPushBind> userPushBinds = userPushBindService.selectByUserIds(userIds);
		List<Long> userPushIds = new ArrayList<Long>();
		if (!userPushBinds.isEmpty()) {
			for (UserPushBind p : userPushBinds) {
				if (!userPushIds.contains(p.getUserId())) {
					userPushIds.add(p.getUserId());
				}
			}

			// 进行闹钟消息推送.
			pushToAlarm(card, userPushBinds);
		}

		// 3.根据集合users 和集合userPushBinds，找出不能推送，只能发短信的用户集合C
		List<Long> userSmsIds = new ArrayList<Long>();
		userSmsIds = userIds;
		if (!userPushBinds.isEmpty()) {
			userSmsIds.removeAll(userPushIds);
		}

		if (!userSmsIds.isEmpty() && card.getSetNowSend().equals((short) 1)) {
			// 进行短信发送
			pushToSms(card, userSmsIds);
		}

		return new AsyncResult<Boolean>(true);
	}
	
	
	/**
	 * 卡片发送推送消息和短信消息.
	 * 
	 * @param card
	 *            卡片对象 给ios app发送离线消息，此场景应用为当ios app被杀死的情况下，ios无法设定闹钟，则进行后台推送提醒。
	 */
	@Async
	@Override
	public Future<Boolean> cardCancelClock(Cards card) {
		
		Long createUserId = card.getCreateUserId();
		// 1找出所有需要通知的用户集合 users
		Long cardId = card.getCardId();
		List<CardAttend> attends = cardAttendService.selectByCardId(cardId);

		if (attends.isEmpty())
			return new AsyncResult<Boolean>(true);

		List<Long> userIds = new ArrayList<Long>();
		for (CardAttend item : attends) {
			
			if (item.getUserId().equals(createUserId)) continue;
			
			if (item.getLocalAlarm().equals((short) 1)) {
				if (!userIds.equals(item.getUserId())) {
					userIds.add(item.getUserId());
				}
			}
		}
		
		if (userIds.isEmpty()) return new AsyncResult<Boolean>(true);
		
		// 2.找出可以发推送消息的用户集合 userPushBinds
		List<UserPushBind> userPushBinds = userPushBindService.selectByUserIds(userIds);
		
		if (userPushBinds.isEmpty()) return new AsyncResult<Boolean>(true);
		
		HashMap<String, String> params = new HashMap<String, String>();

		HashMap<String, String> tranParams = new HashMap<String, String>();

		Short cardType = card.getCardType();
		String cardTypeName = CardUtil.getCardTypeName(cardType);
		Long serviceTime = card.getServiceTime();


		String isShow = "false";
		tranParams.put("is", isShow);
		tranParams.put("ac", "d");
		tranParams.put("ci", card.getCardId().toString());
		tranParams.put("ct", card.getCardType().toString());
		tranParams.put("st", serviceTime.toString());
		tranParams.put("re", "");
		tranParams.put("rt", cardTypeName);
		tranParams.put("rc", "");

		ObjectMapper objectMapper = new ObjectMapper();

		String jsonParams = "";
		try {
			jsonParams = objectMapper.writeValueAsString(tranParams);
		} catch (JsonProcessingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		for (UserPushBind p : userPushBinds) {
			// if (p.getUserId().equals(card.getCreateUserId())) continue;

			String clientId = p.getClientId();
			
			System.out.println("====================push card alarm=======================================");
			System.out.println("userId= " + p.getUserId() + "---cid = " + p.getClientId());

			params.put("transmissionContent", jsonParams);
			params.put("cid", clientId);
			System.out.println(params);
//				String userStatus = PushUtil.getUserStatus(p.getClientId());
			if (p.getDeviceType().equals("ios")) {
				try {
					// if (userStatus.equals("Offline")) {
					PushUtil.IOSPushToSingle(params, "alertClock");
					// }
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (p.getDeviceType().equals("android")) {
				try {
					// if (userStatus.equals("Offline")) {
					PushUtil.AndroidPushToSingle(params);
					// }
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		return new AsyncResult<Boolean>(true);
	}	

	private boolean pushToApp(Cards card, List<UserPushBind> userPushBinds) {

		HashMap<String, String> params = new HashMap<String, String>();

		HashMap<String, String> tranParams = new HashMap<String, String>();

		Short cardType = card.getCardType();
		Long serviceTime = card.getServiceTime();
		String cardTypeName = CardUtil.getCardTypeName(cardType);
		String pushContent = "";
		if (!StringUtil.isEmpty(card.getServiceContent())) {
			if (card.getServiceContent().length() > 20) {
				pushContent = card.getServiceContent().substring(0, 20);
			} else {
				pushContent = card.getServiceContent();
			}
		}

		// 获得提醒时间
		Short setRemind = card.getSetRemind();
		int remindMin = CardUtil.getRemindMin(setRemind);

		Long remindTime = serviceTime - remindMin * 60;

		String isShow = "true";

		if (card.getSetNowSend().equals((short) 0))
			isShow = "false";

		tranParams.put("is", isShow);
		tranParams.put("ac", "s");
		tranParams.put("ci", card.getCardId().toString());
		tranParams.put("ct", card.getCardType().toString());
		tranParams.put("st", serviceTime.toString());
		tranParams.put("re", remindTime.toString());
		tranParams.put("rt", cardTypeName);
		tranParams.put("rc", pushContent);

		// 跳转信息
		if (isShow.equals("true")) {
			// 跳转信息
			tranParams.put("ca", "app");
			tranParams.put("aj", "card_detail");
			tranParams.put("pa", card.getCardId().toString());
			tranParams.put("go", "");
		}

		System.out.println(tranParams.toString());

		ObjectMapper objectMapper = new ObjectMapper();

		String jsonParams = "";
		try {
			jsonParams = objectMapper.writeValueAsString(tranParams);
		} catch (JsonProcessingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		for (UserPushBind p : userPushBinds) {
			Long createUserId = card.getCreateUserId();
			Long toUserId = p.getUserId();
			String clientId = p.getClientId();

			if (StringUtil.isEmpty(clientId))
				continue;

//			 本人就不发了
			 if (createUserId.equals(toUserId)) continue;

			// 若果不是好友以及不是同一家团队不能发推送消息

			if (!createUserId.equals(toUserId)) {
				Boolean isFriend = false;
				AppResultData<Object> v = validateService.validateFriend(createUserId, toUserId);
				isFriend = (v.getStatus() != Constants.ERROR_999);
				if (isFriend == false) {
					v = validateService.validateSameCompany(createUserId, toUserId);
					if (v.getStatus() == Constants.ERROR_999) {
						continue;
					}
				}
			}

			params.put("transmissionContent", jsonParams);
			params.put("cid", clientId);

			if (p.getDeviceType().equals("ios")) {
				try {
					PushUtil.IOSPushToSingle(params, "notification");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (p.getDeviceType().equals("android")) {
				try {
					PushUtil.AndroidPushToSingle(params);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return true;
	}
	
	/**
	 * 卡片如果选择秘书处理，则给秘书发送信息
	 */
	@Async
	@Override
	public Future<Boolean> cardSecDo(Long userId, Cards card) {
		
		Long createUserId = card.getCreateUserId();
		Users createUser = usersService.selectByPrimaryKey(createUserId);
		String userName = createUser.getName().equals("") ? createUser.getMobile() : createUser.getName();
		
		UserRefSearchVo searchVo = new UserRefSearchVo();
		searchVo.setUserId(userId);
		searchVo.setRefType("sec");
		List<UserRef> rs  = userRefService.selectBySearchVo(searchVo);
		UserRef userRef = null;
		if (!rs.isEmpty()) userRef = rs.get(0);
		if (userRef != null) {
			Users secUser = usersService.selectByPrimaryKey(userRef.getRefId());
			String secMobile = secUser.getMobile();
			if (RegexUtil.isMobile(secMobile)) {
				String[] content = new String[] { userName, CardUtil.getCardTypeName(card.getCardType()),  ""};
				HashMap<String, String> sendSmsResult = SmsUtil.SendSms(secMobile, "44658", content);
			}
		}

		return new AsyncResult<Boolean>(true);
	}	

	/**
	 * 因为app后台进程被杀死的情况下，无法设定闹钟，所以必须后台定时提醒 1. 后台做相应检测，如果为离线状态则需要发送
	 * 
	 * @param card
	 * @param userPushBinds
	 * @return
	 */
	private boolean pushToAlarm(Cards card, List<UserPushBind> userPushBinds) {

		HashMap<String, String> params = new HashMap<String, String>();

		HashMap<String, String> tranParams = new HashMap<String, String>();

		Short cardType = card.getCardType();
		Long serviceTime = card.getServiceTime();
		String cardTypeName = CardUtil.getCardTypeName(cardType);
		String pushContent = "";
		if (card.getServiceContent().length() > 20) {
			pushContent = card.getServiceContent().substring(0, 20);
		} else {
			pushContent = card.getServiceContent();
		}

		// 获得提醒时间
		Short setRemind = card.getSetRemind();
		int remindMin = CardUtil.getRemindMin(setRemind);

		Long remindTime = serviceTime - remindMin * 60;

		String isShow = "true";
		tranParams.put("is", isShow);
		tranParams.put("ac", "a");
		tranParams.put("ci", card.getCardId().toString());
		tranParams.put("ct", card.getCardType().toString());
		tranParams.put("st", serviceTime.toString());
		tranParams.put("re", remindTime.toString());
		tranParams.put("rt", cardTypeName);
		tranParams.put("rc", pushContent);

		ObjectMapper objectMapper = new ObjectMapper();

		String jsonParams = "";
		try {
			jsonParams = objectMapper.writeValueAsString(tranParams);
		} catch (JsonProcessingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		for (UserPushBind p : userPushBinds) {
			// if (p.getUserId().equals(card.getCreateUserId())) continue;

			Long createUserId = card.getCreateUserId();
			Long toUserId = p.getUserId();
			String clientId = p.getClientId();
			// 若果不是好友以及不是同一家团队不能发推送消息
			if (!createUserId.equals(toUserId)) {
				Boolean isFriend = false;
				AppResultData<Object> v = validateService.validateFriend(createUserId, toUserId);
				isFriend = (v.getStatus() != Constants.ERROR_999);
				if (isFriend == false) {
					v = validateService.validateSameCompany(createUserId, toUserId);
					if (v.getStatus() == Constants.ERROR_999) {
						continue;
					}
				}
			}
			System.out.println("====================push card alarm=======================================");
			System.out.println("userId= " + p.getUserId() + "---cid = " + p.getClientId());

			params.put("transmissionContent", jsonParams);
			params.put("cid", clientId);
			System.out.println(params);
//			String userStatus = PushUtil.getUserStatus(p.getClientId());
			if (p.getDeviceType().equals("ios")) {
				try {
					// if (userStatus.equals("Offline")) {
					PushUtil.IOSPushToSingle(params, "alertClock");
					// }
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (p.getDeviceType().equals("android")) {
				try {
					// if (userStatus.equals("Offline")) {
					PushUtil.AndroidPushToSingle(params);
					// }
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return true;
	}

	private boolean pushToSms(Cards card, List<Long> userSmsIds) {

		List<Users> userSms = new ArrayList<Users>();

		if (userSmsIds.isEmpty())
			return true;

		UserSearchVo userSearchVo = new UserSearchVo();
		userSearchVo.setUserIds(userSmsIds);
		userSms = usersService.selectBySearchVo(userSearchVo);

		Users createUsers = usersService.selectByPrimaryKey(card.getCreateUserId());

		Users user = usersService.selectByPrimaryKey(card.getUserId());

		String createUserName = createUsers.getName();

		String createUserMobile = createUsers.getMobile();

		if (createUserName.equals(createUserMobile)) {
			createUserName = "";
		} else {
			createUserName = createUserName + " ";
		}

		Long serviceTime = card.getServiceTime();
		String serviceTimeStr = TimeStampUtil.timeStampToDateStr(serviceTime * 1000, "yyyy-MM-dd HH:mm");
		String serviceAddr = card.getServiceAddr();
		String serviceContent = card.getServiceContent();

		if (serviceContent.length() > 20) {
			serviceContent = serviceContent.substring(0, 20) + "...";
		}

		String fromCityName = "";
		String toCityName = "";

		if (card.getCardType().equals((short) 5)) {
			String cardExtra = card.getCardExtra();
			Map<String, Object> cardExtraMap = GsonUtil.GsonToMaps(cardExtra);
			fromCityName = cardExtraMap.get("ticket_from_city_name").toString();
			toCityName = cardExtraMap.get("ticket_to_city_name").toString();
		}

		String[] content;
		String mobile = "";
		HashMap<String, String> sendSmsResult;
		// 开始发送短信
		for (Users item : userSms) {
			// 是好友才发短信，若不是好友continue!
			UserFriendSearchVo searchVo = new UserFriendSearchVo();
			searchVo.setUserId(user.getId());
			searchVo.setFriendId(createUsers.getId());
			UserFriends userFriend = userFriendService.selectByIsFirend(searchVo);
			if (userFriend == null)
				continue;

			if (StringUtil.isEmpty(item.getMobile()))
				continue;
			mobile = item.getMobile();
			switch (card.getCardType()) {
			case 0:
				// statusName = "通用";
				break;
			case 1:
				// statusName = "会议安排";
				content = new String[] { createUserName, createUserMobile, serviceTimeStr, serviceAddr, serviceContent };
				sendSmsResult = SmsUtil.SendSms(mobile, "44665", content);

				break;
			case 2:
				// statusName = "通知公告";
				content = new String[] { createUserName, createUserMobile, serviceTimeStr, serviceContent };
				sendSmsResult = SmsUtil.SendSms(mobile, "44668", content);
				break;
			case 3:
				// statusName = "事务提醒";
				content = new String[] { createUserName, createUserMobile, serviceTimeStr, serviceContent };
				sendSmsResult = SmsUtil.SendSms(mobile, "44666", content);
				break;
			case 4:
				// statusName = "面试邀约";
				content = new String[] { createUserName, createUserMobile, serviceTimeStr, serviceContent };
				sendSmsResult = SmsUtil.SendSms(mobile, "44667", content);
				break;
			case 5:
				// statusName = "差旅规划";
				content = new String[] { createUserName, createUserMobile, fromCityName, toCityName, serviceTimeStr };
				sendSmsResult = SmsUtil.SendSms(mobile, "44664", content);
				break;
			default:
				// statusName = "";
			}

		}

		return true;
	}

}
