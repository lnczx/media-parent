package com.simi.service.impl.user;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.simi.service.user.UserDetailScoreService;
import com.simi.utils.UserUtil;
import com.simi.vo.user.UserDetailScoreSearchVo;
import com.simi.vo.user.UserDetailScoreVo;
import com.simi.vo.user.UserMsgSearchVo;
import com.simi.po.dao.user.UserDetailScoreMapper;
import com.simi.po.dao.user.UsersMapper;
import com.simi.po.model.user.UserDetailScore;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.meijia.utils.BeanUtilsExp;
import com.meijia.utils.TimeStampUtil;

@Service
public class UserDetailScoreServiceImpl implements UserDetailScoreService {

	@Autowired
	private UsersMapper usersMapper;

	@Autowired
	private UserDetailScoreMapper userDetailScoreMapper;

	@Override
	public int deleteByPrimaryKey(Long id) {
		return userDetailScoreMapper.deleteByPrimaryKey(id);
	}

	@Override
	public int insert(UserDetailScore record) {
		return userDetailScoreMapper.insert(record);
	}

	@Override
	public int insertSelective(UserDetailScore record) {
		return userDetailScoreMapper.insertSelective(record);
	}

	@Override
	public PageInfo selectByListPage(UserDetailScoreSearchVo searchVo, int pageNo, int pageSize) {
		PageHelper.startPage(pageNo, pageSize);
		List<UserDetailScore> list = userDetailScoreMapper.selectByListPage(searchVo);
		PageInfo result = new PageInfo(list);
		return result;
	}
	
	@Override
	public List<UserDetailScore> selectBySearchVo(UserDetailScoreSearchVo searchVo) {
		return userDetailScoreMapper.selectBySearchVo(searchVo);
	}

	@Override
	public UserDetailScore initUserDetailScore() {
		UserDetailScore record = new UserDetailScore();
		record.setId(0L);
		record.setUserId(0L);
		record.setMobile("");
		record.setAction("");
		record.setParams("");
		record.setIsConsume((short) 0);
		record.setRemarks("");
		record.setScore(0);
		record.setAddTime(TimeStampUtil.getNowSecond());
		return record;
	}

	@Override
	public UserDetailScore selectByPrimaryKey(Long id) {
		return userDetailScoreMapper.selectByPrimaryKey(id);
	}

	@Override
	public int updateByPrimaryKeySelective(UserDetailScore record) {
		
		return userDetailScoreMapper.updateByPrimaryKeySelective(record);
	}
	
	@Override
	public UserDetailScoreVo getVo(UserDetailScore item) {
		UserDetailScoreVo vo = new UserDetailScoreVo();
		BeanUtilsExp.copyPropertiesIgnoreNull(item, vo);
		
		String scoreStr = "";
		if (vo.getIsConsume().equals((short)0)) scoreStr = "+" + vo.getScore();
		if (vo.getIsConsume().equals((short)1)) scoreStr = "-" + vo.getScore();
		vo.setScoreStr(scoreStr);
		
		String actionName = UserUtil.getScoreActionName(vo.getAction());
		vo.setActionName(actionName);
		
		String addTimeStr = TimeStampUtil.timeStampToDateStr(vo.getAddTime() * 1000, "yyyy-MM-dd HH:mm");
		vo.setAddTimeStr(addTimeStr);
				
		return vo;
	}
	
	@Override
	public List<HashMap> scoreRanking(UserDetailScoreSearchVo searchVo) {
		return userDetailScoreMapper.scoreRanking(searchVo);
	}
}