package com.simi.service.resume;

import java.util.List;

import com.github.pagehelper.PageInfo;
import com.simi.po.model.resume.HrResumeChange;
import com.simi.vo.resume.ResumeChangeVo;
import com.simi.vo.resume.ResumeChangeSearchVo;

/**
 *
 * @author :hulj
 * @Date : 2016年4月28日下午4:31:28
 * @Description: 
 */
public interface HrResumeChangeService {
	
	int deleteByPrimaryKey(Long id);

    int insert(HrResumeChange record);

    HrResumeChange selectByPrimaryKey(Long id);

    int updateByPrimaryKey(HrResumeChange record);
    
    List<HrResumeChange> selectBySearchVo(ResumeChangeSearchVo searchVo);

    PageInfo selectByListPage(ResumeChangeSearchVo searchVo, int pageNo, int pageSize);
	
   	HrResumeChange	initHrResumeChange();
   	
   	ResumeChangeVo initResumeChangeVo();
   	
   	ResumeChangeVo transToResumeVo(HrResumeChange change);
}
