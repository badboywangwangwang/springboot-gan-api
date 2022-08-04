
package com.gan.service.impl;

import com.gan.api.gan.vo.GanUserAddressVO;
import com.gan.common.GanException;
import com.gan.common.ServiceResultEnum;
import com.gan.dao.GanUserAddressMapper;
import com.gan.entity.GanUserAddress;
import com.gan.service.GanUserAddressService;
import com.gan.util.BeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class GanUserAddressServiceImpl implements GanUserAddressService {

    @Autowired
    private GanUserAddressMapper ganUserAddressMapper;

    @Override
    public List<GanUserAddressVO> getMyAddresses(Long userId) {
        List<GanUserAddress> myAddressList = ganUserAddressMapper.findMyAddressList(userId);
        List<GanUserAddressVO> ganUserAddressVOS = BeanUtil.copyList(myAddressList, GanUserAddressVO.class);
        return ganUserAddressVOS;
    }

    @Override
    @Transactional
    public Boolean saveUserAddress(GanUserAddress mallUserAddress) {
        Date now = new Date();
        if (mallUserAddress.getDefaultFlag().intValue() == 1) {
            //添加默认地址，需要将原有的默认地址修改掉
            GanUserAddress defaultAddress = ganUserAddressMapper.getMyDefaultAddress(mallUserAddress.getUserId());
            if (defaultAddress != null) {
                defaultAddress.setDefaultFlag((byte) 0);
                defaultAddress.setUpdateTime(now);
                int updateResult = ganUserAddressMapper.updateByPrimaryKeySelective(defaultAddress);
                if (updateResult < 1) {
                    //未更新成功
                    GanException.fail(ServiceResultEnum.DB_ERROR.getResult());
                }
            }
        }
        return ganUserAddressMapper.insertSelective(mallUserAddress) > 0;
    }

    @Override
    public Boolean updateGanUserAddress(GanUserAddress mallUserAddress) {
        GanUserAddress tempAddress = getGanUserAddressById(mallUserAddress.getAddressId());
        Date now = new Date();
        if (mallUserAddress.getDefaultFlag().intValue() == 1) {
            //修改为默认地址，需要将原有的默认地址修改掉
            GanUserAddress defaultAddress = ganUserAddressMapper.getMyDefaultAddress(mallUserAddress.getUserId());
            if (defaultAddress != null && !defaultAddress.getAddressId().equals(tempAddress)) {
                //存在默认地址且默认地址并不是当前修改的地址
                defaultAddress.setDefaultFlag((byte) 0);
                defaultAddress.setUpdateTime(now);
                int updateResult = ganUserAddressMapper.updateByPrimaryKeySelective(defaultAddress);
                if (updateResult < 1) {
                    //未更新成功
                    GanException.fail(ServiceResultEnum.DB_ERROR.getResult());
                }
            }
        }
        mallUserAddress.setUpdateTime(now);
        return ganUserAddressMapper.updateByPrimaryKeySelective(mallUserAddress) > 0;
    }

    @Override
    public GanUserAddress getGanUserAddressById(Long addressId) {
        GanUserAddress mallUserAddress = ganUserAddressMapper.selectByPrimaryKey(addressId);
        if (mallUserAddress == null) {
            GanException.fail(ServiceResultEnum.DATA_NOT_EXIST.getResult());
        }
        return mallUserAddress;
    }

    @Override
    public GanUserAddress getMyDefaultAddressByUserId(Long userId) {
        return ganUserAddressMapper.getMyDefaultAddress(userId);
    }

    @Override
    public Boolean deleteById(Long addressId) {
        return ganUserAddressMapper.deleteByPrimaryKey(addressId) > 0;
    }
}
