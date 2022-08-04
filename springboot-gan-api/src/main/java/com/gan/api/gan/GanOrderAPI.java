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
@Api(value = "v1", tags = "7.订单操作相关接口")
@RequestMapping("/api/v1")
public class GanOrderAPI {


    @Resource
    private GanShoppingCartService ganShoppingCartService;
    @Resource
    private GanOrderService ganOrderService;
    @Resource
    private GanUserAddressService ganUserAddressService;

    @PostMapping("/saveOrder")
    @ApiOperation(value = "生成订单接口", notes = "传参为地址id和待结算的购物项id数组")
    public Result<String> saveOrder(@ApiParam(value = "订单参数") @RequestBody SaveOrderParam saveOrderParam, @TokenToUser GanUser loginGanUser) {
        int priceTotal = 0;
        if (saveOrderParam == null || saveOrderParam.getCartItemIds() == null || saveOrderParam.getAddressId() == null) {
            GanException.fail(ServiceResultEnum.PARAM_ERROR.getResult());
        }
        if (saveOrderParam.getCartItemIds().length < 1) {
            GanException.fail(ServiceResultEnum.PARAM_ERROR.getResult());
        }
        List<GanShoppingCartItemVO> itemsForSave = ganShoppingCartService.getCartItemsForSettle(Arrays.asList(saveOrderParam.getCartItemIds()), loginGanUser.getUserId());
        if (CollectionUtils.isEmpty(itemsForSave)) {
            //无数据
            GanException.fail("参数异常");
        } else {
            //总价
            for (GanShoppingCartItemVO newBeeMallShoppingCartItemVO : itemsForSave) {
                priceTotal += newBeeMallShoppingCartItemVO.getGoodsCount() * newBeeMallShoppingCartItemVO.getSellingPrice();
            }
            if (priceTotal < 1) {
                GanException.fail("价格异常");
            }
            GanUserAddress address = ganUserAddressService.getGanUserAddressById(saveOrderParam.getAddressId());
            if (!loginGanUser.getUserId().equals(address.getUserId())) {
                return ResultGenerator.genFailResult(ServiceResultEnum.REQUEST_FORBIDEN_ERROR.getResult());
            }
            //保存订单并返回订单号
            String saveOrderResult = ganOrderService.saveOrder(loginGanUser, address, itemsForSave);
            Result result = ResultGenerator.genSuccessResult();
            result.setData(saveOrderResult);
            return result;
        }
        return ResultGenerator.genFailResult("生成订单失败");
    }

    @GetMapping("/order/{orderNo}")
    @ApiOperation(value = "订单详情接口", notes = "传参为订单号")
    public Result<GanOrderDetailVO> orderDetailPage(@ApiParam(value = "订单号") @PathVariable("orderNo") String orderNo, @TokenToUser GanUser loginGanUser) {
        return ResultGenerator.genSuccessResult(ganOrderService.getOrderDetailByOrderNo(orderNo, loginGanUser.getUserId()));
    }

    @GetMapping("/order")
    @ApiOperation(value = "订单列表接口", notes = "传参为页码")
    public Result<PageResult<List<GanOrderListVO>>> orderList(@ApiParam(value = "页码") @RequestParam(required = false) Integer pageNumber,
                                                              @ApiParam(value = "订单状态:0.待支付 1.待确认 2.待发货 3:已发货 4.交易成功") @RequestParam(required = false) Integer status,
                                                              @TokenToUser GanUser loginGanUser) {
        Map params = new HashMap(8);
        if (pageNumber == null || pageNumber < 1) {
            pageNumber = 1;
        }
        params.put("userId", loginGanUser.getUserId());
        params.put("orderStatus", status);
        params.put("page", pageNumber);
        params.put("limit", Constants.ORDER_SEARCH_PAGE_LIMIT);
        //封装分页请求参数
        PageQueryUtil pageUtil = new PageQueryUtil(params);
        return ResultGenerator.genSuccessResult(ganOrderService.getMyOrders(pageUtil));
    }

    @PutMapping("/order/{orderNo}/cancel")
    @ApiOperation(value = "订单取消接口", notes = "传参为订单号")
    public Result cancelOrder(@ApiParam(value = "订单号") @PathVariable("orderNo") String orderNo, @TokenToUser GanUser loginGanUser) {
        String cancelOrderResult = ganOrderService.cancelOrder(orderNo, loginGanUser.getUserId());
        if (ServiceResultEnum.SUCCESS.getResult().equals(cancelOrderResult)) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult(cancelOrderResult);
        }
    }

    @PutMapping("/order/{orderNo}/finish")
    @ApiOperation(value = "确认收货接口", notes = "传参为订单号")
    public Result finishOrder(@ApiParam(value = "订单号") @PathVariable("orderNo") String orderNo, @TokenToUser GanUser loginGanUser) {
        String finishOrderResult = ganOrderService.finishOrder(orderNo, loginGanUser.getUserId());
        if (ServiceResultEnum.SUCCESS.getResult().equals(finishOrderResult)) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult(finishOrderResult);
        }
    }

    @GetMapping("/paySuccess")
    @ApiOperation(value = "模拟支付成功回调的接口", notes = "传参为订单号和支付方式")
    public Result paySuccess(@ApiParam(value = "订单号") @RequestParam("orderNo") String orderNo, @ApiParam(value = "支付方式") @RequestParam("payType") int payType) {
        String payResult = ganOrderService.paySuccess(orderNo, payType);
        if (ServiceResultEnum.SUCCESS.getResult().equals(payResult)) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult(payResult);
        }
    }

}
