
package com.gan.dao;

import com.gan.entity.GanUser;
import com.gan.util.PageQueryUtil;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface GanUserMapper {

    int deleteByPrimaryKey(Long userId);

    int insert(GanUser record);

    int insertSelective(GanUser record);

    GanUser selectByPrimaryKey(Long userId);

    GanUser selectByLoginName(String loginName);

    GanUser selectByLoginNameAndPasswd(@Param("loginName") String loginName, @Param("password") String password);

    int updateByPrimaryKeySelective(GanUser record);

    int updateByPrimaryKey(GanUser record);

    List<GanUser> findGanUserList(PageQueryUtil pageUtil);

    int getTotalGanUsers(PageQueryUtil pageUtil);

    int lockUserBatch(@Param("ids") Long[] ids, @Param("lockStatus") int lockStatus);
}