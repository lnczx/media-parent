package com.simi.po.dao.partners;

import java.util.List;

import com.simi.po.model.partners.PartnerServiceType;

public interface PartnerServiceTypeMapper {
    int deleteByPrimaryKey(Long serviceTypeId);

    int insert(PartnerServiceType record);

    int insertSelective(PartnerServiceType record);

    PartnerServiceType selectByPrimaryKey(Long serviceTypeId);

    int updateByPrimaryKeySelective(PartnerServiceType record);

    int updateByPrimaryKey(PartnerServiceType record);
    
    List<PartnerServiceType> selectByParentId(Long  partnerId);
}