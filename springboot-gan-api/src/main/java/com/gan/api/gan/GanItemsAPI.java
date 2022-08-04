
package com.gan.api.gan;

import com.gan.api.gan.vo.GanItemsDetailVO;
import com.gan.api.gan.vo.GanSearchItemsVO;
import com.gan.common.Constants;
import com.gan.common.GanException;
import com.gan.common.ServiceResultEnum;
import com.gan.config.annotation.TokenToUser;
import com.gan.entity.GanItems;
import com.gan.entity.GanUser;
import com.gan.service.GanItemsService;
import com.gan.util.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Api(value = "v1", tags = "4.商品相关接口")
@RequestMapping("/api/v1")
public class GanItemsAPI {

    private static final Logger logger = LoggerFactory.getLogger(GanItemsAPI.class);

    @Resource
    private GanItemsService ganItemsService;

    @GetMapping("/search")
    @ApiOperation(value = "商品搜索接口", notes = "根据关键字和分类id进行搜索")
    public Result<PageResult<List<GanSearchItemsVO>>> search(@RequestParam(required = false) @ApiParam(value = "搜索关键字") String keyword,
                                                                           @RequestParam(required = false) @ApiParam(value = "分类id") Long goodsCategoryId,
                                                                           @RequestParam(required = false) @ApiParam(value = "orderBy") String orderBy,
                                                                           @RequestParam(required = false) @ApiParam(value = "页码") Integer pageNumber,
                                                                           @TokenToUser GanUser loginGanUser) {
        
        logger.info("goods search api,keyword={},goodsCategoryId={},orderBy={},pageNumber={},userId={}", keyword, goodsCategoryId, orderBy, pageNumber, loginGanUser.getUserId());

        Map params = new HashMap(8);
        //两个搜索参数都为空，直接返回异常
        if (goodsCategoryId == null && StringUtils.isEmpty(keyword)) {
            GanException.fail("非法的搜索参数");
        }
        if (pageNumber == null || pageNumber < 1) {
            pageNumber = 1;
        }
        params.put("goodsCategoryId", goodsCategoryId);
        params.put("page", pageNumber);
        params.put("limit", Constants.GOODS_SEARCH_PAGE_LIMIT);
        //对keyword做过滤 去掉空格
        if (!StringUtils.isEmpty(keyword)) {
            params.put("keyword", keyword);
        }
        if (!StringUtils.isEmpty(orderBy)) {
            params.put("orderBy", orderBy);
        }
        //搜索上架状态下的商品
        params.put("goodsSellStatus", Constants.SELL_STATUS_UP);
        //封装商品数据
        PageQueryUtil pageUtil = new PageQueryUtil(params);
        return ResultGenerator.genSuccessResult(ganItemsService.searchGanItems(pageUtil));
    }

    @GetMapping("/goods/detail/{goodsId}")
    @ApiOperation(value = "商品详情接口", notes = "传参为商品id")
    public Result<GanItemsDetailVO> goodsDetail(@ApiParam(value = "商品id") @PathVariable("goodsId") Long goodsId, @TokenToUser GanUser loginGanUser) {
        logger.info("goods detail api,goodsId={},userId={}", goodsId, loginGanUser.getUserId());
        if (goodsId < 1) {
            return ResultGenerator.genFailResult("参数异常");
        }
        GanItems ganItems = ganItemsService.getGanItemsById(goodsId);
        if (Constants.SELL_STATUS_UP != ganItems.getGoodsSellStatus()) {
            GanException.fail(ServiceResultEnum.GOODS_PUT_DOWN.getResult());
        }
        GanItemsDetailVO goodsDetailVO = new GanItemsDetailVO();
        BeanUtil.copyProperties(ganItems, goodsDetailVO);
        goodsDetailVO.setGoodsCarouselList(ganItems.getGoodsCarousel().split(","));
        return ResultGenerator.genSuccessResult(goodsDetailVO);
    }

}
