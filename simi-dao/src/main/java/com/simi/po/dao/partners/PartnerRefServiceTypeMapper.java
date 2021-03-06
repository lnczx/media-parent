package com.simi.po.dao.partners;

import java.util.List;

import com.simi.po.model.partners.PartnerRefServiceType;
import com.simi.vo.partners.PartnersSearchVo;

public interface PartnerRefServiceTypeMapper {
    int deleteByPrimaryKey(Long id);

    int deleteByPartnerId(Long partnerId);

    int insert(PartnerRefServiceType record);

    int insertSelective(PartnerRefServiceType record);

    PartnerRefServiceType selectByPrimaryKey(Long id);

    List<PartnerRefServiceType> selectByPartnerId(Long partnerId);

    int updateByPrimaryKeySelective(PartnerRefServiceType record);

    int updateByPrimaryKey(PartnerRefServiceType record);
    
	List<PartnerRefServiceType> selectBySearchVo(PartnersSearchVo searchVo);
    
}