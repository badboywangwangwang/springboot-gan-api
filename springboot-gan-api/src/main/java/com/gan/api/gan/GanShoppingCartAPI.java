import com.gan.api.gan.param.SaveCartItemParam;
import com.gan.api.gan.param.UpdateCartItemParam;
import com.gan.api.gan.vo.GanShoppingCartItemVO;
import com.gan.common.Constants;
import com.gan.common.GanException;
import com.gan.common.ServiceResultEnum;
import com.gan.config.annotation.TokenToUser;
import com.gan.entity.GanShoppingCartItem;
import com.gan.entity.GanUser;
import com.gan.service.GanShoppingCartService;
import com.gan.util.PageQueryUtil;
import com.gan.util.PageResult;
import com.gan.util.Result;
import com.gan.util.ResultGenerator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
//import com.gan.api.gan.param.SaveCartItemParam;
//import com.gan.api.gan.param.UpdateCartItemParam;
//import com.gan.api.gan.vo.GanShoppingCartItemVO;
//import com.gan.common.Constants;
//import com.gan.common.GanException;
//import com.gan.common.ServiceResultEnum;
//import com.gan.config.annotation.TokenToUser;
//import com.gan.entity.GanShoppingCartItem;
//import com.gan.entity.GanUser;
//import com.gan.service.GanShoppingCartService;
//import com.gan.util.PageQueryUtil;
//import com.gan.util.PageResult;
//import com.gan.util.Result;
//import com.gan.util.ResultGenerator;
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
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
@Api(value = "v1", tags = "5.新蜂商城购物车相关接口")
@RequestMapping("/api/v1")
public class GanShoppingCartAPI {

    @Resource
    private GanShoppingCartService ganShoppingCartService;

    @GetMapping("/shop-cart/page")
    @ApiOperation(value = "购物车列表(每页默认5条)", notes = "传参为页码")
    public Result<PageResult<List<GanShoppingCartItemVO>>> cartItemPageList(Integer pageNumber, @TokenToUser GanUser loginGanUser) {
        Map params = new HashMap(8);
        if (pageNumber == null || pageNumber < 1) {
            pageNumber = 1;
        }
        params.put("userId", loginGanUser.getUserId());
        params.put("page", pageNumber);
        params.put("limit", Constants.SHOPPING_CART_PAGE_LIMIT);
        //封装分页请求参数
        PageQueryUtil pageUtil = new PageQueryUtil(params);
        return ResultGenerator.genSuccessResult(ganShoppingCartService.getMyShoppingCartItems(pageUtil));
    }

    @GetMapping("/shop-cart")
    @ApiOperation(value = "购物车列表(网页移动端不分页)", notes = "")
    public Result<List<GanShoppingCartItemVO>> cartItemList(@TokenToUser GanUser loginGanUser) {
        return ResultGenerator.genSuccessResult(ganShoppingCartService.getMyShoppingCartItems(loginGanUser.getUserId()));
    }

    @PostMapping("/shop-cart")
    @ApiOperation(value = "添加商品到购物车接口", notes = "传参为商品id、数量")
    public Result saveGanShoppingCartItem(@RequestBody SaveCartItemParam saveCartItemParam,
                                                 @TokenToUser GanUser loginGanUser) {
        String saveResult = ganShoppingCartService.saveGanCartItem(saveCartItemParam, loginGanUser.getUserId());
        //添加成功
        if (ServiceResultEnum.SUCCESS.getResult().equals(saveResult)) {
            return ResultGenerator.genSuccessResult();
        }
        //添加失败
        return ResultGenerator.genFailResult(saveResult);
    }

    @PutMapping("/shop-cart")
    @ApiOperation(value = "修改购物项数据", notes = "传参为购物项id、数量")
    public Result updateGanShoppingCartItem(@RequestBody UpdateCartItemParam updateCartItemParam,
                                                   @TokenToUser GanUser loginGanUser) {
        String updateResult = ganShoppingCartService.updateGanCartItem(updateCartItemParam, loginGanUser.getUserId());
        //修改成功
        if (ServiceResultEnum.SUCCESS.getResult().equals(updateResult)) {
            return ResultGenerator.genSuccessResult();
        }
        //修改失败
        return ResultGenerator.genFailResult(updateResult);
    }

    @DeleteMapping("/shop-cart/{newBeeMallShoppingCartItemId}")
    @ApiOperation(value = "删除购物项", notes = "传参为购物项id")
    public Result updateGanShoppingCartItem(@PathVariable("newBeeMallShoppingCartItemId") Long newBeeMallShoppingCartItemId,
                                                   @TokenToUser GanUser loginGanUser) {
        GanShoppingCartItem ganShoppingCartItemById = ganShoppingCartService.getGanCartItemById(newBeeMallShoppingCartItemId);
        if (!loginGanUser.getUserId().equals(ganShoppingCartItemById.getUserId())) {
            return ResultGenerator.genFailResult(ServiceResultEnum.REQUEST_FORBIDEN_ERROR.getResult());
        }
        Boolean deleteResult = ganShoppingCartService.deleteById(newBeeMallShoppingCartItemId,loginGanUser.getUserId());
        //删除成功
        if (deleteResult) {
            return ResultGenerator.genSuccessResult();
        }
        //删除失败
        return ResultGenerator.genFailResult(ServiceResultEnum.OPERATE_ERROR.getResult());
    }

    @GetMapping("/shop-cart/settle")
    @ApiOperation(value = "根据购物项id数组查询购物项明细", notes = "确认订单页面使用")
    public Result<List<GanShoppingCartItemVO>> toSettle(Long[] cartItemIds, @TokenToUser GanUser loginGanUser) {
        if (cartItemIds.length < 1) {
            GanException.fail("参数异常");
        }
        int priceTotal = 0;
        List<GanShoppingCartItemVO> itemsForSettle = ganShoppingCartService.getCartItemsForSettle(Arrays.asList(cartItemIds), loginGanUser.getUserId());
        if (CollectionUtils.isEmpty(itemsForSettle)) {
            //无数据则抛出异常
            GanException.fail("参数异常");
        } else {
            //总价
            for (GanShoppingCartItemVO newBeeMallShoppingCartItemVO : itemsForSettle) {
                priceTotal += newBeeMallShoppingCartItemVO.getGoodsCount() * newBeeMallShoppingCartItemVO.getSellingPrice();
            }
            if (priceTotal < 1) {
                GanException.fail("价格异常");
            }
        }
        return ResultGenerator.genSuccessResult(itemsForSettle);
    }
}
