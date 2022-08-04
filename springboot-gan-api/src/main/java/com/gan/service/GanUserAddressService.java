
package com.gan.service;

import com.gan.api.gan.vo.GanUserAddressVO;
import com.gan.entity.GanUserAddress;

import java.util.List;

public interface GanUserAddressService {

    /**
     * 获取我的收货地址
     *
     * @param userId
     * @return
     */
    List<GanUserAddressVO> getMyAddresses(Long userId);

    /**
     * 保存收货地址
     *
     * @param mallUserAddress
     * @return
     */
    Boolean saveUserAddress(GanUserAddress mallUserAddress);

    /**
     * 修改收货地址
     *
     * @param mallUserAddress
     * @return
     */
    Boolean updateGanUserAddress(GanUserAddress mallUserAddress);

    /**
     * 获取收货地址详情
     *
     * @param addressId
     * @return
     */
    GanUserAddress getGanUserAddressById(Long addressId);

    /**
     * 获取我的默认收货地址
     *
     * @param userId
     * @return
     */
    GanUserAddress getMyDefaultAddressByUserId(Long userId);

    /**
     * 删除收货地址
     *
     * @param addressId
     * @return
     */
    Boolean deleteById(Long addressId);
}
