
package com.gan.dao;

import com.gan.entity.ItemsCategory;
import com.gan.util.PageQueryUtil;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ItemsCategoryMapper {
    int deleteByPrimaryKey(Long categoryId);

    int insert(ItemsCategory record);

    int insertSelective(ItemsCategory record);

    ItemsCategory selectByPrimaryKey(Long categoryId);

    ItemsCategory selectByLevelAndName(@Param("categoryLevel") Byte categoryLevel, @Param("categoryName") String categoryName);

    int updateByPrimaryKeySelective(ItemsCategory record);

    int updateByPrimaryKey(ItemsCategory record);

    List<ItemsCategory> findItemsCategoryList(PageQueryUtil pageUtil);

    int getTotalGoodsCategories(PageQueryUtil pageUtil);

    int deleteBatch(Long[] ids);

    List<ItemsCategory> selectByLevelAndParentIdsAndNumber(@Param("parentIds") List<Long> parentIds, @Param("categoryLevel") int categoryLevel, @Param("number") int number);
}