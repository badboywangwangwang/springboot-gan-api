
package com.gan.service.impl;

import com.gan.api.gan.vo.GanSearchItemsVO;
import com.gan.common.GanCategoryLevelEnum;
import com.gan.common.GanException;
import com.gan.common.ServiceResultEnum;
import com.gan.dao.GanItemsMapper;
import com.gan.dao.ItemsCategoryMapper;
import com.gan.entity.GanItems;
import com.gan.entity.ItemsCategory;
import com.gan.service.GanItemsService;
import com.gan.util.BeanUtil;
import com.gan.util.PageQueryUtil;
import com.gan.util.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class GanItemsServiceImpl implements GanItemsService {

    @Autowired
    private GanItemsMapper ganItemsMapper;
    @Autowired
    private ItemsCategoryMapper itemsCategoryMapper;

    @Override
    public PageResult getGanItemsPage(PageQueryUtil pageUtil) {
        List<GanItems> ganItemsList = ganItemsMapper.findGanItemsList(pageUtil);
        int total = ganItemsMapper.getTotalGanItems(pageUtil);
        PageResult pageResult = new PageResult(ganItemsList, total, pageUtil.getLimit(), pageUtil.getPage());
        return pageResult;
    }

    @Override
    public String saveGanItems(GanItems ganItems) {
        ItemsCategory itemsCategory = itemsCategoryMapper.selectByPrimaryKey(ganItems.getGoodsCategoryId());
        // 分类不存在或者不是三级分类，则该参数字段异常
        if (itemsCategory == null || itemsCategory.getCategoryLevel().intValue() != GanCategoryLevelEnum.LEVEL_THREE.getLevel()) {
            return ServiceResultEnum.GOODS_CATEGORY_ERROR.getResult();
        }
        if (ganItemsMapper.selectByCategoryIdAndName(ganItems.getGoodsName(), ganItems.getGoodsCategoryId()) != null) {
            return ServiceResultEnum.SAME_GOODS_EXIST.getResult();
        }
        if (ganItemsMapper.insertSelective(ganItems) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public void batchSaveGanItems(List<GanItems> ganItemsList) {
        if (!CollectionUtils.isEmpty(ganItemsList)) {
            ganItemsMapper.batchInsert(ganItemsList);
        }
    }

    @Override
    public String updateGanItems(GanItems ganItems) {
        ItemsCategory itemsCategory = itemsCategoryMapper.selectByPrimaryKey(ganItems.getGoodsCategoryId());
        // 分类不存在或者不是三级分类，则该参数字段异常
        if (itemsCategory == null || itemsCategory.getCategoryLevel().intValue() != GanCategoryLevelEnum.LEVEL_THREE.getLevel()) {
            return ServiceResultEnum.GOODS_CATEGORY_ERROR.getResult();
        }
        GanItems temp = ganItemsMapper.selectByPrimaryKey(ganItems.getGoodsId());
        if (temp == null) {
            return ServiceResultEnum.DATA_NOT_EXIST.getResult();
        }
        GanItems temp2 = ganItemsMapper.selectByCategoryIdAndName(ganItems.getGoodsName(), ganItems.getGoodsCategoryId());
        if (temp2 != null && !temp2.getGoodsId().equals(ganItems.getGoodsId())) {
            //name和分类id相同且不同id 不能继续修改
            return ServiceResultEnum.SAME_GOODS_EXIST.getResult();
        }
        ganItems.setUpdateTime(new Date());
        if (ganItemsMapper.updateByPrimaryKeySelective(ganItems) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public GanItems getGanItemsById(Long id) {
        GanItems ganItems = ganItemsMapper.selectByPrimaryKey(id);
        if (ganItems == null) {
            GanException.fail(ServiceResultEnum.GOODS_NOT_EXIST.getResult());
        }
        return ganItems;
    }

    @Override
    public Boolean batchUpdateSellStatus(Long[] ids, int sellStatus) {
        return ganItemsMapper.batchUpdateSellStatus(ids, sellStatus) > 0;
    }

    @Override
    public PageResult searchGanItems(PageQueryUtil pageUtil) {
        List<GanItems> ganItems = ganItemsMapper.findGanItemsListBySearch(pageUtil);
        int total = ganItemsMapper.getTotalGanItemsBySearch(pageUtil);
        List<GanSearchItemsVO> ganSearchItemsVOS = new ArrayList<>();
        if (!CollectionUtils.isEmpty(ganItems)) {
            ganSearchItemsVOS = BeanUtil.copyList(ganItems, GanSearchItemsVO.class);
            for (GanSearchItemsVO ganSearchItemsVO : ganSearchItemsVOS) {
                String goodsName = ganSearchItemsVO.getGoodsName();
                String goodsIntro = ganSearchItemsVO.getGoodsIntro();
                // 字符串过长导致文字超出的问题
                if (goodsName.length() > 28) {
                    goodsName = goodsName.substring(0, 28) + "...";
                    ganSearchItemsVO.setGoodsName(goodsName);
                }
                if (goodsIntro.length() > 30) {
                    goodsIntro = goodsIntro.substring(0, 30) + "...";
                    ganSearchItemsVO.setGoodsIntro(goodsIntro);
                }
            }
        }
        PageResult pageResult = new PageResult(ganSearchItemsVOS, total, pageUtil.getLimit(), pageUtil.getPage());
        return pageResult;
    }
}
