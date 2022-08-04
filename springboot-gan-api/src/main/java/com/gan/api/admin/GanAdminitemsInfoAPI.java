
package com.gan.api.admin;

import com.gan.api.admin.param.ItemAddParam;
import com.gan.api.admin.param.ItemEditParam;
import com.gan.entity.GanItems;
import com.gan.entity.ItemsCategory;
import com.gan.service.GanCategoryService;
import com.gan.service.GanItemsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import com.gan.api.admin.param.BatchIdParam;
import com.gan.common.Constants;
import com.gan.common.ServiceResultEnum;
import com.gan.config.annotation.TokenToAdminUser;
import com.gan.entity.AdminUserToken;
import com.gan.util.BeanUtil;
import com.gan.util.PageQueryUtil;
import com.gan.util.Result;
import com.gan.util.ResultGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;


@RestController
@Api(value = "v1", tags = "8-3.后台管理系统商品模块接口")
@RequestMapping("/manage-api/v1")
public class GanAdminitemsInfoAPI {

    private static final Logger logger = LoggerFactory.getLogger(GanAdminitemsInfoAPI.class);

    @Resource
    private GanItemsService ganItemsService;
    @Resource
    private GanCategoryService ganCategoryService;

    /**
     * 列表
     */
    @RequestMapping(value = "/goods/list", method = RequestMethod.GET)
    @ApiOperation(value = "商品列表", notes = "可根据名称和上架状态筛选")
    public Result list(@RequestParam(required = false) @ApiParam(value = "页码") Integer pageNumber,
                       @RequestParam(required = false) @ApiParam(value = "每页条数") Integer pageSize,
                       @RequestParam(required = false) @ApiParam(value = "商品名称") String goodsName,
                       @RequestParam(required = false) @ApiParam(value = "上架状态 0-上架 1-下架") Integer goodsSellStatus, @TokenToAdminUser AdminUserToken adminUser) {
        logger.info("adminUser:{}", adminUser.toString());
        if (pageNumber == null || pageNumber < 1 || pageSize == null || pageSize < 10) {
            return ResultGenerator.genFailResult("分页参数异常！");
        }
        Map params = new HashMap(8);
        params.put("page", pageNumber);
        params.put("limit", pageSize);
        if (!StringUtils.isEmpty(goodsName)) {
            params.put("goodsName", goodsName);
        }
        if (goodsSellStatus != null) {
            params.put("goodsSellStatus", goodsSellStatus);
        }
        PageQueryUtil pageUtil = new PageQueryUtil(params);
        return ResultGenerator.genSuccessResult(ganItemsService.getGanItemsPage(pageUtil));
    }

    /**
     * 添加
     */
    @RequestMapping(value = "/goods", method = RequestMethod.POST)
    @ApiOperation(value = "新增商品信息", notes = "新增商品信息")
    public Result save(@RequestBody @Valid ItemAddParam itemAddParam, @TokenToAdminUser AdminUserToken adminUser) {
        logger.info("adminUser:{}", adminUser.toString());
        GanItems ganItems = new GanItems();
        BeanUtil.copyProperties(itemAddParam, ganItems);
        String result = ganItemsService.saveGanItems(ganItems);
        if (ServiceResultEnum.SUCCESS.getResult().equals(result)) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult(result);
        }
    }


    /**
     * 修改
     */
    @RequestMapping(value = "/goods", method = RequestMethod.PUT)
    @ApiOperation(value = "修改商品信息", notes = "修改商品信息")
    public Result update(@RequestBody @Valid ItemEditParam itemEditParam, @TokenToAdminUser AdminUserToken adminUser) {
        logger.info("adminUser:{}", adminUser.toString());
        GanItems ganItems = new GanItems();
        BeanUtil.copyProperties(itemEditParam, ganItems);
        String result = ganItemsService.updateGanItems(ganItems);
        if (ServiceResultEnum.SUCCESS.getResult().equals(result)) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult(result);
        }
    }

    /**
     * 详情
     */
    @GetMapping("/goods/{id}")
    @ApiOperation(value = "获取单条商品信息", notes = "根据id查询")
    public Result info(@PathVariable("id") Long id, @TokenToAdminUser AdminUserToken adminUser) {
        logger.info("adminUser:{}", adminUser.toString());
        Map goodsInfo = new HashMap(8);
        GanItems ganItems = ganItemsService.getGanItemsById(id);
        if (ganItems == null) {
            return ResultGenerator.genFailResult(ServiceResultEnum.DATA_NOT_EXIST.getResult());
        }
        goodsInfo.put("ganItems", ganItems);
        ItemsCategory thirdCategory;
        ItemsCategory secondCategory;
        ItemsCategory firstCategory;
        thirdCategory = ganCategoryService.getItemsCategoryById(ganItems.getGoodsCategoryId());
        if (thirdCategory != null) {
            goodsInfo.put("thirdCategory", thirdCategory);
            secondCategory = ganCategoryService.getItemsCategoryById(thirdCategory.getParentId());
            if (secondCategory != null) {
                goodsInfo.put("secondCategory", secondCategory);
                firstCategory = ganCategoryService.getItemsCategoryById(secondCategory.getParentId());
                if (firstCategory != null) {
                    goodsInfo.put("firstCategory", firstCategory);
                }
            }
        }
        return ResultGenerator.genSuccessResult(goodsInfo);
    }

    /**
     * 批量修改销售状态
     */
    @RequestMapping(value = "/goods/status/{sellStatus}", method = RequestMethod.PUT)
    @ApiOperation(value = "批量修改销售状态", notes = "批量修改销售状态")
    public Result delete(@RequestBody BatchIdParam batchIdParam, @PathVariable("sellStatus") int sellStatus, @TokenToAdminUser AdminUserToken adminUser) {
        logger.info("adminUser:{}", adminUser.toString());
        if (batchIdParam == null || batchIdParam.getIds().length < 1) {
            return ResultGenerator.genFailResult("参数异常！");
        }
        if (sellStatus != Constants.SELL_STATUS_UP && sellStatus != Constants.SELL_STATUS_DOWN) {
            return ResultGenerator.genFailResult("状态异常！");
        }
        if (ganItemsService.batchUpdateSellStatus(batchIdParam.getIds(), sellStatus)) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult("修改失败");
        }
    }

}