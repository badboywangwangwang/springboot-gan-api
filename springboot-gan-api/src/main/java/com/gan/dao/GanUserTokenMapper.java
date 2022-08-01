
package com.gan.dao;

import com.gan.entity.GanUserToken;

public interface GanUserTokenMapper {

    int deleteByPrimaryKey(Long userId);

    int insert(GanUserToken record);

    int insertSelective(GanUserToken record);

    GanUserToken selectByPrimaryKey(Long userId);

    GanUserToken selectByToken(String token);

    int updateByPrimaryKeySelective(GanUserToken record);

    int updateByPrimaryKey(GanUserToken record);
}