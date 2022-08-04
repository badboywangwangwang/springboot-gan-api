
package com.gan.api.gan;
import com.gan.api.gan.vo.GanUserAddressVO;
import com.gan.config.annotation.TokenToUser;
import com.gan.entity.GanUser;
import com.gan.entity.GanUserAddress;
import com.gan.service.GanUserAddressService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import com.gan.api.gan.param.SaveGanUserAddressParam;
import com.gan.api.gan.param.UpdateGanUserAddressParam;
import com.gan.common.ServiceResultEnum;
import com.gan.util.BeanUtil;
import com.gan.util.Result;
import com.gan.util.ResultGenerator;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;

@RestController
@Api(value = "v1", tags = "6.个人地址相关接口")
@RequestMapping("/api/v1")
public class GanUserAddressAPI {

    @Resource
    private GanUserAddressService ganUserAddressService;

    @GetMapping("/address")
    @ApiOperation(value = "我的收货地址列表", notes = "")
    public Result<List<GanUserAddressVO>> addressList(@TokenToUser GanUser loginGanUser) {
        return ResultGenerator.genSuccessResult(ganUserAddressService.getMyAddresses(loginGanUser.getUserId()));
    }

    @PostMapping("/address")
    @ApiOperation(value = "添加地址", notes = "")
    public Result<Boolean> saveUserAddress(@RequestBody SaveGanUserAddressParam saveGanUserAddressParam,
                                           @TokenToUser GanUser loginGanUser) {
        GanUserAddress userAddress = new GanUserAddress();
        BeanUtil.copyProperties(saveGanUserAddressParam, userAddress);
        userAddress.setUserId(loginGanUser.getUserId());
        Boolean saveResult = ganUserAddressService.saveUserAddress(userAddress);
        //添加成功
        if (saveResult) {
            return ResultGenerator.genSuccessResult();
        }
        //添加失败
        return ResultGenerator.genFailResult("添加失败");
    }

    @PutMapping("/address")
    @ApiOperation(value = "修改地址", notes = "")
    public Result<Boolean> updateGanUserAddress(@RequestBody UpdateGanUserAddressParam updateGanUserAddressParam,
                                                 @TokenToUser GanUser loginGanUser) {
        GanUserAddress mallUserAddressById = ganUserAddressService.getGanUserAddressById(updateGanUserAddressParam.getAddressId());
        if (!loginGanUser.getUserId().equals(mallUserAddressById.getUserId())) {
            return ResultGenerator.genFailResult(ServiceResultEnum.REQUEST_FORBIDEN_ERROR.getResult());
        }
        GanUserAddress userAddress = new GanUserAddress();
        BeanUtil.copyProperties(updateGanUserAddressParam, userAddress);
        userAddress.setUserId(loginGanUser.getUserId());
        Boolean updateResult = ganUserAddressService.updateGanUserAddress(userAddress);
        //修改成功
        if (updateResult) {
            return ResultGenerator.genSuccessResult();
        }
        //修改失败
        return ResultGenerator.genFailResult("修改失败");
    }

    @GetMapping("/address/{addressId}")
    @ApiOperation(value = "获取收货地址详情", notes = "传参为地址id")
    public Result<GanUserAddressVO> getGanUserAddress(@PathVariable("addressId") Long addressId,
                                                              @TokenToUser GanUser loginGanUser) {
        GanUserAddress mallUserAddressById = ganUserAddressService.getGanUserAddressById(addressId);
        GanUserAddressVO newBeeGanUserAddressVO = new GanUserAddressVO();
        BeanUtil.copyProperties(mallUserAddressById, newBeeGanUserAddressVO);
        if (!loginGanUser.getUserId().equals(mallUserAddressById.getUserId())) {
            return ResultGenerator.genFailResult(ServiceResultEnum.REQUEST_FORBIDEN_ERROR.getResult());
        }
        return ResultGenerator.genSuccessResult(newBeeGanUserAddressVO);
    }

    @GetMapping("/address/default")
    @ApiOperation(value = "获取默认收货地址", notes = "无传参")
    public Result getDefaultGanUserAddress(@TokenToUser GanUser loginGanUser) {
        GanUserAddress mallUserAddressById = ganUserAddressService.getMyDefaultAddressByUserId(loginGanUser.getUserId());
        return ResultGenerator.genSuccessResult(mallUserAddressById);
    }

    @DeleteMapping("/address/{addressId}")
    @ApiOperation(value = "删除收货地址", notes = "传参为地址id")
    public Result deleteAddress(@PathVariable("addressId") Long addressId,
                                @TokenToUser GanUser loginGanUser) {
        GanUserAddress mallUserAddressById = ganUserAddressService.getGanUserAddressById(addressId);
        if (!loginGanUser.getUserId().equals(mallUserAddressById.getUserId())) {
            return ResultGenerator.genFailResult(ServiceResultEnum.REQUEST_FORBIDEN_ERROR.getResult());
        }
        Boolean deleteResult = ganUserAddressService.deleteById(addressId);
        //删除成功
        if (deleteResult) {
            return ResultGenerator.genSuccessResult();
        }
        //删除失败
        return ResultGenerator.genFailResult(ServiceResultEnum.OPERATE_ERROR.getResult());
    }
}
