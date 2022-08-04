
package com.gan.api.admin;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import com.gan.api.admin.param.BatchIdParam;
import com.gan.api.gan.vo.GanOrderDetailVO;
import com.gan.common.ServiceResultEnum;
import com.gan.config.annotation.TokenToAdminUser;
import com.gan.entity.AdminUserToken;
import com.gan.service.GanOrderService;
import com.gan.util.PageQueryUtil;
import com.gan.util.Result;
import com.gan.util.ResultGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RestController
@Api(value = "v1", tags = "8-5.后台管理系统订单模块接口")
@RequestMapping("/manage-api/v1")
public class GanAdminOrderAPI {

    private static final Logger logger = LoggerFactory.getLogger(GanAdminOrderAPI.class);

    @Resource
    private GanOrderService ganOrderService;

    /**
     * 列表
     */
    @RequestMapping(value = "/orders", method = RequestMethod.GET)
    @ApiOperation(value = "订单列表", notes = "可根据订单号和订单状态筛选")
    public Result list(@RequestParam(required = false) @ApiParam(value = "页码") Integer pageNumber,
                       @RequestParam(required = false) @ApiParam(value = "每页条数") Integer pageSize,
                       @RequestParam(required = false) @ApiParam(value = "订单号") String orderNo,
                       @RequestParam(required = false) @ApiParam(value = "订单状态") Integer orderStatus, @TokenToAdminUser AdminUserToken adminUser) {
        logger.info("adminUser:{}", adminUser.toString());
        if (pageNumber == null || pageNumber < 1 || pageSize == null || pageSize < 10) {
            return ResultGenerator.genFailResult("分页参数异常！");
        }
        Map params = new HashMap(8);
        params.put("page", pageNumber);
        params.put("limit", pageSize);
        if (!StringUtils.isEmpty(orderNo)) {
            params.put("orderNo", orderNo);
        }
        if (orderStatus != null) {
            params.put("orderStatus", orderStatus);
        }
        PageQueryUtil pageUtil = new PageQueryUtil(params);
        return ResultGenerator.genSuccessResult(ganOrderService.getGanOrdersPage(pageUtil));
    }

    @GetMapping("/orders/{orderId}")
    @ApiOperation(value = "订单详情接口", notes = "传参为订单号")
    public Result<GanOrderDetailVO> orderDetailPage(@ApiParam(value = "订单号") @PathVariable("orderId") Long orderId, @TokenToAdminUser AdminUserToken adminUser) {
        logger.info("adminUser:{}", adminUser.toString());
        return ResultGenerator.genSuccessResult(ganOrderService.getOrderDetailByOrderId(orderId));
    }

    /**
     * 配货
     */
    @RequestMapping(value = "/orders/checkDone", method = RequestMethod.PUT)
    @ApiOperation(value = "修改订单状态为配货成功", notes = "批量修改")
    public Result checkDone(@RequestBody BatchIdParam batchIdParam, @TokenToAdminUser AdminUserToken adminUser) {
        logger.info("adminUser:{}", adminUser.toString());
        if (batchIdParam==null||batchIdParam.getIds().length < 1) {
            return ResultGenerator.genFailResult("参数异常！");
        }
        String result = ganOrderService.checkDone(batchIdParam.getIds());
        if (ServiceResultEnum.SUCCESS.getResult().equals(result)) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult(result);
        }
    }

    /**
     * 出库
     */
    @RequestMapping(value = "/orders/checkOut", method = RequestMethod.PUT)
    @ApiOperation(value = "修改订单状态为已出库", notes = "批量修改")
    public Result checkOut(@RequestBody BatchIdParam batchIdParam, @TokenToAdminUser AdminUserToken adminUser) {
        logger.info("adminUser:{}", adminUser.toString());
        if (batchIdParam==null||batchIdParam.getIds().length < 1) {
            return ResultGenerator.genFailResult("参数异常！");
        }
        String result = ganOrderService.checkOut(batchIdParam.getIds());
        if (ServiceResultEnum.SUCCESS.getResult().equals(result)) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult(result);
        }
    }

    /**
     * 关闭订单
     */
    @RequestMapping(value = "/orders/close", method = RequestMethod.PUT)
    @ApiOperation(value = "修改订单状态为商家关闭", notes = "批量修改")
    public Result closeOrder(@RequestBody BatchIdParam batchIdParam, @TokenToAdminUser AdminUserToken adminUser) {
        logger.info("adminUser:{}", adminUser.toString());
        if (batchIdParam==null||batchIdParam.getIds().length < 1) {
            return ResultGenerator.genFailResult("参数异常！");
        }
        String result = ganOrderService.closeOrder(batchIdParam.getIds());
        if (ServiceResultEnum.SUCCESS.getResult().equals(result)) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult(result);
        }
    }
}