
package com.gan.dao;

import com.gan.entity.GanOrderAddress;

public interface GanOrderAddressMapper {
    int deleteByPrimaryKey(Long orderId);

    int insert(GanOrderAddress record);

    int insertSelective(GanOrderAddress record);

    GanOrderAddress selectByPrimaryKey(Long orderId);

    int updateByPrimaryKeySelective(GanOrderAddress record);

    int updateByPrimaryKey(GanOrderAddress record);
}