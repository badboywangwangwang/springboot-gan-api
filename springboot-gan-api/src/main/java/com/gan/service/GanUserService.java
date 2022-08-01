
package com.gan.service;

import com.gan.api.gan.param.GanUserUpdateParam;
import com.gan.util.PageQueryUtil;
import com.gan.util.PageResult;

public interface GanUserService {

    /**
     * 用户注册
     *
     * @param loginName
     * @param password
     * @return
     */
    String register(String loginName, String password);


    /**
     * 登录
     *
     * @param loginName
     * @param passwordMD5
     * @return
     */
    String login(String loginName, String passwordMD5);

    /**
     * 用户信息修改
     *
     * @param ganUser
     * @return
     */
    Boolean updateUserInfo(GanUserUpdateParam ganUser, Long userId);

    /**
     * 登出接口
     * @param userId
     * @return
     */
    Boolean logout(Long userId);

    /**
     * 用户禁用与解除禁用(0-未锁定 1-已锁定)
     *
     * @param ids
     * @param lockStatus
     * @return
     */
    Boolean lockUsers(Long[] ids, int lockStatus);

    /**
     * 后台分页
     *
     * @param pageUtil
     * @return
     */
    PageResult getGanUsersPage(PageQueryUtil pageUtil);
}
