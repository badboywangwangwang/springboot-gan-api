
package com.gan.dao;

import com.gan.entity.GanUserAddress;

import java.util.List;

public interface GanUserAddressMapper {
    int deleteByPrimaryKey(Long addressId);

    int insert(GanUserAddress record);

    int insertSelective(GanUserAddress record);

    GanUserAddress selectByPrimaryKey(Long addressId);

    GanUserAddress getMyDefaultAddress(Long userId);

    List<GanUserAddress> findMyAddressList(Long userId);

    int updateByPrimaryKeySelective(GanUserAddress record);

    int updateByPrimaryKey(GanUserAddress record);
}