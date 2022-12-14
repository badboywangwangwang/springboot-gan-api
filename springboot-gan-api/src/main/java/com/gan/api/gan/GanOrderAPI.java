package com.gan.api.gan;

import com.gan.api.gan.param.SaveOrderParam;
import com.gan.api.gan.vo.GanOrderDetailVO;
import com.gan.api.gan.vo.GanOrderListVO;
import com.gan.api.gan.vo.GanShoppingCartItemVO;
import com.gan.common.Constants;
import com.gan.common.GanException;
import com.gan.common.ServiceResultEnum;
import com.gan.config.annotation.TokenToUser;
import com.gan.entity.GanUser;
import com.gan.entity.GanUserAddress;
import com.gan.service.GanOrderService;
import com.gan.service.GanShoppingCartService;
import com.gan.service.GanUserAddressService;
import com.gan.util.PageQueryUtil;
import com.gan.util.PageResult;
import com.gan.util.Result;
import com.gan.util.ResultGenerator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//
//package com.gan.api.gan;
//
//import com.gan.api.gan.param.SaveOrderParam;
//import com.gan.api.gan.vo.GanOrderDetailVO;
//import com.gan.api.gan.vo.GanOrderListVO;
//import com.gan.api.gan.vo.GanShoppingCartItemVO;
//import com.gan.common.Constants;
//import com.gan.common.GanException;
//import com.gan.common.ServiceResultEnum;
//import com.gan.config.annotation.TokenToUser;
//import com.gan.entity.GanUser;
//import com.gan.entity.GanUserAddress;
//import com.gan.service.GanOrderService;
//import com.gan.service.GanShoppingCartService;
//import com.gan.service.GanUserAddressService;
//import com.gan.util.PageQueryUtil;
//import com.gan.util.PageResult;
//import com.gan.util.Result;
//import com.gan.util.ResultGenerator;
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
//import io.swagger.annotations.ApiParam;
//import org.springframework.util.CollectionUtils;
//import org.springframework.web.bind.annotation.*;
//
//import javax.annotation.Resource;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
@RestController
@Api(value = "v1", tags = "7.????????????????????????")
@RequestMapping("/api/v1")
public class GanOrderAPI {


    @Resource
    private GanShoppingCartService ganShoppingCartService;
    @Resource
    private GanOrderService ganOrderService;
    @Resource
    private GanUserAddressService ganUserAddressService;

    @PostMapping("/saveOrder")
    @ApiOperation(value = "??????????????????", notes = "???????????????id????????????????????????id??????")
    public Result<String> saveOrder(@ApiParam(value = "????????????") @RequestBody SaveOrderParam saveOrderParam, @TokenToUser GanUser loginGanUser) {
        int priceTotal = 0;
        if (saveOrderParam == null || saveOrderParam.getCartItemIds() == null || saveOrderParam.getAddressId() == null) {
            GanException.fail(ServiceResultEnum.PARAM_ERROR.getResult());
        }
        if (saveOrderParam.getCartItemIds().length < 1) {
            GanException.fail(ServiceResultEnum.PARAM_ERROR.getResult());
        }
        List<GanShoppingCartItemVO> itemsForSave = ganShoppingCartService.getCartItemsForSettle(Arrays.asList(saveOrderParam.getCartItemIds()), loginGanUser.getUserId());
        if (CollectionUtils.isEmpty(itemsForSave)) {
            //?????????
            GanException.fail("????????????");
        } else {
            //??????
            for (GanShoppingCartItemVO ganShoppingCartItemVO : itemsForSave) {
                priceTotal += ganShoppingCartItemVO.getGoodsCount() * ganShoppingCartItemVO.getSellingPrice();
            }
            if (priceTotal < 1) {
                GanException.fail("????????????");
            }
            GanUserAddress address = ganUserAddressService.getGanUserAddressById(saveOrderParam.getAddressId());
            if (!loginGanUser.getUserId().equals(address.getUserId())) {
                return ResultGenerator.genFailResult(ServiceResultEnum.REQUEST_FORBIDEN_ERROR.getResult());
            }
            //??????????????????????????????
            String saveOrderResult = ganOrderService.saveOrder(loginGanUser, address, itemsForSave);
            Result result = ResultGenerator.genSuccessResult();
            result.setData(saveOrderResult);
            return result;
        }
        return ResultGenerator.genFailResult("??????????????????");
    }

    @GetMapping("/order/{orderNo}")
    @ApiOperation(value = "??????????????????", notes = "??????????????????")
    public Result<GanOrderDetailVO> orderDetailPage(@ApiParam(value = "?????????") @PathVariable("orderNo") String orderNo, @TokenToUser GanUser loginGanUser) {
        return ResultGenerator.genSuccessResult(ganOrderService.getOrderDetailByOrderNo(orderNo, loginGanUser.getUserId()));
    }

    @GetMapping("/order")
    @ApiOperation(value = "??????????????????", notes = "???????????????")
    public Result<PageResult<List<GanOrderListVO>>> orderList(@ApiParam(value = "??????") @RequestParam(required = false) Integer pageNumber,
                                                              @ApiParam(value = "????????????:0.????????? 1.????????? 2.????????? 3:????????? 4.????????????") @RequestParam(required = false) Integer status,
                                                              @TokenToUser GanUser loginGanUser) {
        Map params = new HashMap(8);
        if (pageNumber == null || pageNumber < 1) {
            pageNumber = 1;
        }
        params.put("userId", loginGanUser.getUserId());
        params.put("orderStatus", status);
        params.put("page", pageNumber);
        params.put("limit", Constants.ORDER_SEARCH_PAGE_LIMIT);
        //????????????????????????
        PageQueryUtil pageUtil = new PageQueryUtil(params);
        return ResultGenerator.genSuccessResult(ganOrderService.getMyOrders(pageUtil));
    }

    @PutMapping("/order/{orderNo}/cancel")
    @ApiOperation(value = "??????????????????", notes = "??????????????????")
    public Result cancelOrder(@ApiParam(value = "?????????") @PathVariable("orderNo") String orderNo, @TokenToUser GanUser loginGanUser) {
        String cancelOrderResult = ganOrderService.cancelOrder(orderNo, loginGanUser.getUserId());
        if (ServiceResultEnum.SUCCESS.getResult().equals(cancelOrderResult)) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult(cancelOrderResult);
        }
    }

    @PutMapping("/order/{orderNo}/finish")
    @ApiOperation(value = "??????????????????", notes = "??????????????????")
    public Result finishOrder(@ApiParam(value = "?????????") @PathVariable("orderNo") String orderNo, @TokenToUser GanUser loginGanUser) {
        String finishOrderResult = ganOrderService.finishOrder(orderNo, loginGanUser.getUserId());
        if (ServiceResultEnum.SUCCESS.getResult().equals(finishOrderResult)) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult(finishOrderResult);
        }
    }

    @GetMapping("/paySuccess")
    @ApiOperation(value = "?????????????????????????????????", notes = "?????????????????????????????????")
    public Result paySuccess(@ApiParam(value = "?????????") @RequestParam("orderNo") String orderNo, @ApiParam(value = "????????????") @RequestParam("payType") int payType) {
        String payResult = ganOrderService.paySuccess(orderNo, payType);
        if (ServiceResultEnum.SUCCESS.getResult().equals(payResult)) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult(payResult);
        }
    }

}
