
package com.gan.api.gan;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import com.gan.api.gan.param.GanUserLoginParam;
import com.gan.api.gan.param.GanUserRegisterParam;
import com.gan.api.gan.param.GanUserUpdateParam;
import com.gan.api.gan.vo.GanUserVO;
import com.gan.common.Constants;
import com.gan.common.ServiceResultEnum;
import com.gan.config.annotation.TokenToUser;
import com.gan.entity.GanUser;
import com.gan.service.GanUserService;
import com.gan.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@Api(value = "v1", tags = "2-0.前台用户操作相关接口")
@RequestMapping("/api/v1")
public class GanPersonalAPI {

    @Resource
    private GanUserService ganUserService;

    private static final Logger logger = LoggerFactory.getLogger(GanPersonalAPI.class);

    @PostMapping("/user/login")
    @ApiOperation(value = "登录接口", notes = "返回token")
    public Result<String> login(@RequestBody @Valid GanUserLoginParam ganUserLoginParam) {
        if (!NumberUtil.isPhone(ganUserLoginParam.getLoginName())){
            return ResultGenerator.genFailResult(ServiceResultEnum.LOGIN_NAME_IS_NOT_PHONE.getResult());
        }
        String loginResult = ganUserService.login(ganUserLoginParam.getLoginName(), ganUserLoginParam.getPasswordMd5());

        logger.info("login api,loginName={},loginResult={}", ganUserLoginParam.getLoginName(), loginResult);

        //登录成功
        if (!StringUtils.isEmpty(loginResult) && loginResult.length() == Constants.TOKEN_LENGTH) {
            Result result = ResultGenerator.genSuccessResult();
            result.setData(loginResult);
            return result;
        }
        //登录失败
        return ResultGenerator.genFailResult(loginResult);
    }


    @PostMapping("/user/logout")
    @ApiOperation(value = "登出接口", notes = "清除token")
    public Result<String> logout(@TokenToUser GanUser loginGanUser) {
        Boolean logoutResult = ganUserService.logout(loginGanUser.getUserId());

        logger.info("logout api,loginGanUser={}", loginGanUser.getUserId());

        //登出成功
        if (logoutResult) {
            return ResultGenerator.genSuccessResult();
        }
        //登出失败
        return ResultGenerator.genFailResult("logout error");
    }


    @PostMapping("/user/register")
    @ApiOperation(value = "用户注册", notes = "")
    public Result register(@RequestBody @Valid GanUserRegisterParam ganUserRegisterParam) {
        if (!NumberUtil.isPhone(ganUserRegisterParam.getLoginName())){
            return ResultGenerator.genFailResult(ServiceResultEnum.LOGIN_NAME_IS_NOT_PHONE.getResult());
        }
        String registerResult = ganUserService.register(ganUserRegisterParam.getLoginName(), ganUserRegisterParam.getPassword());

        logger.info("register api,loginName={},loginResult={}", ganUserRegisterParam.getLoginName(), registerResult);

        //注册成功
        if (ServiceResultEnum.SUCCESS.getResult().equals(registerResult)) {
            return ResultGenerator.genSuccessResult();
        }
        //注册失败
        return ResultGenerator.genFailResult(registerResult);
    }

    @PutMapping("/user/info")
    @ApiOperation(value = "修改用户信息", notes = "")
    public Result updateInfo(@RequestBody @ApiParam("用户信息") GanUserUpdateParam ganUserUpdateParam, @TokenToUser GanUser loginGanUser) {
        Boolean flag = ganUserService.updateUserInfo(ganUserUpdateParam, loginGanUser.getUserId());
        if (flag) {
            //返回成功
            Result result = ResultGenerator.genSuccessResult();
            return result;
        } else {
            //返回失败
            Result result = ResultGenerator.genFailResult("修改失败");
            return result;
        }
    }

    @GetMapping("/user/info")
    @ApiOperation(value = "获取用户信息", notes = "")
    public Result<GanUserVO> getUserDetail(@TokenToUser GanUser loginGanUser) {
        //已登录则直接返回
        GanUserVO ganUserVO = new GanUserVO();
        BeanUtil.copyProperties(loginGanUser, ganUserVO);
        return ResultGenerator.genSuccessResult(ganUserVO);
    }
}
