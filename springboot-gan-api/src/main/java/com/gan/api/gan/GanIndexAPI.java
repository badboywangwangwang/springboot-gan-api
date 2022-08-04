/**
 * 严肃声明：
 * 开源版本请务必保留此注释头信息，若删除我方将保留所有法律责任追究！
 * 本软件已申请软件著作权，受国家版权局知识产权以及国家计算机软件著作权保护！
 * 可正常分享和学习源码，不得用于违法犯罪活动，违者必究！
 * Copyright (c) 2019-2021 十三 all rights reserved.
 * 版权所有，侵权必究！
 */
package com.gan.api.gan;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import com.gan.api.gan.vo.IndexInfoVO;
import com.gan.api.gan.vo.GanIndexCarouselVO;
import com.gan.api.gan.vo.GanIndexConfigItemsVO;
import com.gan.common.Constants;
import com.gan.common.IndexConfigTypeEnum;
import com.gan.service.GanCarouselService;
import com.gan.service.GanIndexConfigService;
import com.gan.util.Result;
import com.gan.util.ResultGenerator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@Api(value = "v1", tags = "1.首页接口")
@RequestMapping("/api/v1")
public class GanIndexAPI {

    @Resource
    private GanCarouselService ganCarouselService;

    @Resource
    private GanIndexConfigService ganIndexConfigService;

    @GetMapping("/index-infos")
    @ApiOperation(value = "获取首页数据", notes = "轮播图、新品、推荐等")
    public Result<IndexInfoVO> indexInfo() {
        IndexInfoVO indexInfoVO = new IndexInfoVO();
        List<GanIndexCarouselVO> carousels = ganCarouselService.getCarouselsForIndex(Constants.INDEX_CAROUSEL_NUMBER);
        List<GanIndexConfigItemsVO> hotGoodses = ganIndexConfigService.getConfigItemsesForIndex(IndexConfigTypeEnum.INDEX_GOODS_HOT.getType(), Constants.INDEX_GOODS_HOT_NUMBER);
        List<GanIndexConfigItemsVO> newGoodses = ganIndexConfigService.getConfigItemsesForIndex(IndexConfigTypeEnum.INDEX_GOODS_NEW.getType(), Constants.INDEX_GOODS_NEW_NUMBER);
        List<GanIndexConfigItemsVO> recommendGoodses = ganIndexConfigService.getConfigItemsesForIndex(IndexConfigTypeEnum.INDEX_GOODS_RECOMMOND.getType(), Constants.INDEX_GOODS_RECOMMOND_NUMBER);
        indexInfoVO.setCarousels(carousels);
        indexInfoVO.setHotGoodses(hotGoodses);
        indexInfoVO.setNewGoodses(newGoodses);
        indexInfoVO.setRecommendGoodses(recommendGoodses);
        return ResultGenerator.genSuccessResult(indexInfoVO);
    }
}
