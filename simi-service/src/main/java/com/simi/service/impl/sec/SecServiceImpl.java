package com.simi.service.impl.sec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.meijia.utils.TimeStampUtil;
import com.meijia.utils.huanxin.EasemobIMUsers;
import com.simi.common.Constants;
import com.simi.po.dao.sec.SecMapper;
import com.simi.po.dao.sec.SecRef3rdMapper;
import com.simi.po.dao.user.UserRef3rdMapper;
import com.simi.po.model.sec.Sec;
import com.simi.po.model.sec.SecRef3rd;
import com.simi.po.model.user.UserRef3rd;
import com.simi.po.model.user.Users;
import com.simi.service.sec.SecService;
import com.simi.vo.SecList;
import com.simi.vo.sec.SecVo;



@Service
public class SecServiceImpl implements SecService{

	@Autowired
	private SecRef3rdMapper secRef3rdMapper;
	@Autowired
	private SecMapper secMapper;

	@Override
	public int insertSelective(Sec record) {
		
	   return secMapper.insert(record);
		
	}
	@Autowired
	private UserRef3rdMapper userRef3rdMapper;
	
	@Override
	public PageInfo searchVoListPage(int pageNo, int pageSize) {


		 PageHelper.startPage(pageNo, pageSize);
         List<Sec> list = secMapper.selectByListPage();
         
         List<Sec> listNew = new ArrayList<Sec>();
	        for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				Sec sec = (Sec) iterator.next();
				SecVo dicAdNew = new SecVo();
				 String imgUrl = sec.getHeadImg();
	             String extensionName = imgUrl.substring(imgUrl.lastIndexOf("."));
	             String beforName = imgUrl.substring(0,(imgUrl.lastIndexOf(".")));
	             String newImgUrl = beforName+"_small"+extensionName;
	             sec.setHeadImg(newImgUrl);
	             listNew.add(sec);
			}
	        for(int i = 0; i < list.size(); i++) {
	       	 if (listNew.get(i) != null) {
	       		 list.set(i, listNew.get(i));
	       	 }
	        }
         
         PageInfo result = new PageInfo(list);
		 return result;
		 
	}
	/**
	 *  注册环信用户
	 */
	@Override
	public SecRef3rd genImSec(Sec sec) {
		SecRef3rd record = new SecRef3rd();
		Long secId =sec.getId();
		SecRef3rd secRef3rd = secRef3rdMapper.selectBySecId(secId);
		if (secRef3rd !=null) {
			return secRef3rd;
		}

		//如果不存在则新增.并且存入数据库
		String username = "yggj_user_"+ sec.getId().toString();
		String defaultPassword = com.meijia.utils.huanxin.comm.Constants.DEFAULT_PASSWORD;
		ObjectNode datanode = JsonNodeFactory.instance.objectNode();
        datanode.put("username", username);
        datanode.put("password", defaultPassword);
        ObjectNode createNewIMUserSingleNode = EasemobIMUsers.createNewIMUserSingle(datanode);

        JsonNode statusCode = createNewIMUserSingleNode.get("statusCode");
		if (!statusCode.toString().equals("200"))
			return record;

		JsonNode entity = createNewIMUserSingleNode.get("entities");
		String uuid = entity.get(0).get("uuid").toString();
//		username = entity.get(0).get("username").toString();

		record.setId(0L);
		record.setSecId(secId);
		record.setRefType(Constants.IM_PROVIDE);
		record.setMobile(sec.getMobile());
		record.setUsername(username);
		record.setPassword(defaultPassword);
		record.setRefPrimaryKey(uuid);
		record.setAddTime(TimeStampUtil.getNowSecond());
		secRef3rdMapper.insert(record);
        return record;
	}
	@Override
	public int deleteByPrimaryKey(Long id) {
		return secMapper.deleteByPrimaryKey(id);
	}
	@Override
	public Sec initSec() {
		Sec sec=new Sec();
		sec.setId(0L);
		sec.setName("");
		sec.setMobile("");
		sec.setNickName("");
		sec.setBirthDay(null);
		sec.setHeadImg("");
		sec.setCityId(0L);
		sec.setStatus(null);
		sec.setAddTime(TimeStampUtil.getNow()/1000);
	    sec.setUpdateTime(0L);
		
		return sec;
	}
	//判断秘书名称是否重复
	@Override
	public Sec selectByUserNameAndOtherId(String name, Long id) {
		HashMap map = new HashMap();
		map.put("username", name);
		map.put("id", id);
		return secMapper.selectByNameAndOtherId(map);
	}
	@Override
	public SecList selectByMobile(String mobile) { 
		
		return secMapper.selectByMobile(mobile);
		
	}

}
