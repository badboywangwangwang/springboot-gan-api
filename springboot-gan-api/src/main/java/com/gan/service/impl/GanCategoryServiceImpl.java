
package com.gan.service.impl;

import com.gan.api.gan.vo.GanIndexCategoryVO;
import com.gan.api.gan.vo.SecondLevelCategoryVO;
import com.gan.api.gan.vo.ThirdLevelCategoryVO;
import com.gan.common.Constants;
import com.gan.common.GanCategoryLevelEnum;
import com.gan.common.ServiceResultEnum;
import com.gan.dao.ItemsCategoryMapper;
import com.gan.entity.ItemsCategory;
import com.gan.service.GanCategoryService;
import com.gan.util.BeanUtil;
import com.gan.util.PageQueryUtil;
import com.gan.util.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Service
public class GanCategoryServiceImpl implements GanCategoryService {

    @Autowired
    private ItemsCategoryMapper itemsCategoryMapper;

    @Override
    public String saveCategory(ItemsCategory goodsCategory) {
        ItemsCategory temp = itemsCategoryMapper.selectByLevelAndName(goodsCategory.getCategoryLevel(), goodsCategory.getCategoryName());
        if (temp != null) {
            return ServiceResultEnum.SAME_CATEGORY_EXIST.getResult();
        }
        if (itemsCategoryMapper.insertSelective(goodsCategory) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public String updateItemsCategory(ItemsCategory goodsCategory) {
        ItemsCategory temp = itemsCategoryMapper.selectByPrimaryKey(goodsCategory.getCategoryId());
        if (temp == null) {
            return ServiceResultEnum.DATA_NOT_EXIST.getResult();
        }
        ItemsCategory temp2 = itemsCategoryMapper.selectByLevelAndName(goodsCategory.getCategoryLevel(), goodsCategory.getCategoryName());
        if (temp2 != null && !temp2.getCategoryId().equals(goodsCategory.getCategoryId())) {
            //同名且不同id 不能继续修改
            return ServiceResultEnum.SAME_CATEGORY_EXIST.getResult();
        }
        goodsCategory.setUpdateTime(new Date());
        if (itemsCategoryMapper.updateByPrimaryKeySelective(goodsCategory) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public ItemsCategory getItemsCategoryById(Long id) {
        return itemsCategoryMapper.selectByPrimaryKey(id);
    }

    @Override
    public Boolean deleteBatch(Long[] ids) {
        if (ids.length < 1) {
            return false;
        }
        //删除分类数据
        return itemsCategoryMapper.deleteBatch(ids) > 0;
    }

    @Override
    public List<GanIndexCategoryVO> getCategoriesForIndex() {
        List<GanIndexCategoryVO> ganIndexCategoryVOS = new ArrayList<>();
        //获取一级分类的固定数量的数据
        List<ItemsCategory> firstLevelCategories = itemsCategoryMapper.selectByLevelAndParentIdsAndNumber(Collections.singletonList(0L), GanCategoryLevelEnum.LEVEL_ONE.getLevel(), Constants.INDEX_CATEGORY_NUMBER);
        if (!CollectionUtils.isEmpty(firstLevelCategories)) {
            List<Long> firstLevelCategoryIds = firstLevelCategories.stream().map(ItemsCategory::getCategoryId).collect(Collectors.toList());
            //获取二级分类的数据
            List<ItemsCategory> secondLevelCategories = itemsCategoryMapper.selectByLevelAndParentIdsAndNumber(firstLevelCategoryIds, GanCategoryLevelEnum.LEVEL_TWO.getLevel(), 0);
            if (!CollectionUtils.isEmpty(secondLevelCategories)) {
                List<Long> secondLevelCategoryIds = secondLevelCategories.stream().map(ItemsCategory::getCategoryId).collect(Collectors.toList());
                //获取三级分类的数据
                List<ItemsCategory> thirdLevelCategories = itemsCategoryMapper.selectByLevelAndParentIdsAndNumber(secondLevelCategoryIds, GanCategoryLevelEnum.LEVEL_THREE.getLevel(), 0);
                if (!CollectionUtils.isEmpty(thirdLevelCategories)) {
                    //根据 parentId 将 thirdLevelCategories 分组
                    Map<Long, List<ItemsCategory>> thirdLevelCategoryMap = thirdLevelCategories.stream().collect(groupingBy(ItemsCategory::getParentId));
                    List<SecondLevelCategoryVO> secondLevelCategoryVOS = new ArrayList<>();
                    //处理二级分类
                    for (ItemsCategory secondLevelCategory : secondLevelCategories) {
                        SecondLevelCategoryVO secondLevelCategoryVO = new SecondLevelCategoryVO();
                        BeanUtil.copyProperties(secondLevelCategory, secondLevelCategoryVO);
                        //如果该二级分类下有数据则放入 secondLevelCategoryVOS 对象中
                        if (thirdLevelCategoryMap.containsKey(secondLevelCategory.getCategoryId())) {
                            //根据二级分类的id取出thirdLevelCategoryMap分组中的三级分类list
                            List<ItemsCategory> tempGoodsCategories = thirdLevelCategoryMap.get(secondLevelCategory.getCategoryId());
                            secondLevelCategoryVO.setThirdLevelCategoryVOS((BeanUtil.copyList(tempGoodsCategories, ThirdLevelCategoryVO.class)));
                            secondLevelCategoryVOS.add(secondLevelCategoryVO);
                        }
                    }
                    //处理一级分类
                    if (!CollectionUtils.isEmpty(secondLevelCategoryVOS)) {
                        //根据 parentId 将 thirdLevelCategories 分组
                        Map<Long, List<SecondLevelCategoryVO>> secondLevelCategoryVOMap = secondLevelCategoryVOS.stream().collect(groupingBy(SecondLevelCategoryVO::getParentId));
                        for (ItemsCategory firstCategory : firstLevelCategories) {
                            GanIndexCategoryVO ganIndexCategoryVO = new GanIndexCategoryVO();
                            BeanUtil.copyProperties(firstCategory, ganIndexCategoryVO);
                            //如果该一级分类下有数据则放入 ganIndexCategoryVO 对象中
                            if (secondLevelCategoryVOMap.containsKey(firstCategory.getCategoryId())) {
                                //根据一级分类的id取出secondLevelCategoryVOMap分组中的二级级分类list
                                List<SecondLevelCategoryVO> tempGoodsCategories = secondLevelCategoryVOMap.get(firstCategory.getCategoryId());
                                ganIndexCategoryVO.setSecondLevelCategoryVOS(tempGoodsCategories);
                                ganIndexCategoryVOS.add(ganIndexCategoryVO);
                            }
                        }
                    }
                }
            }
            return ganIndexCategoryVOS;
        } else {
            return null;
        }
    }

    @Override
    public PageResult getCategorisPage(PageQueryUtil pageUtil) {
        List<ItemsCategory> goodsCategories = itemsCategoryMapper.findItemsCategoryList(pageUtil);
        int total = itemsCategoryMapper.getTotalGoodsCategories(pageUtil);
        PageResult pageResult = new PageResult(goodsCategories, total, pageUtil.getLimit(), pageUtil.getPage());
        return pageResult;
    }

    @Override
    public List<ItemsCategory> selectByLevelAndParentIdsAndNumber(List<Long> parentIds, int categoryLevel) {
        return itemsCategoryMapper.selectByLevelAndParentIdsAndNumber(parentIds, categoryLevel, 0);//0代表查询所有
    }
}
