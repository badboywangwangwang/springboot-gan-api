
package com.gan.service;

import com.gan.api.gan.vo.GanIndexCategoryVO;
import com.gan.entity.ItemsCategory;
import com.gan.util.PageQueryUtil;
import com.gan.util.PageResult;

import java.util.List;

public interface GanCategoryService {

    String saveCategory(ItemsCategory goodsCategory);

    String updateItemsCategory(ItemsCategory goodsCategory);

    ItemsCategory getItemsCategoryById(Long id);

    Boolean deleteBatch(Long[] ids);

    /**
     * 返回分类数据(首页调用)
     *
     * @return
     */
    List<GanIndexCategoryVO> getCategoriesForIndex();

    /**
     * 后台分页
     *
     * @param pageUtil
     * @return
     */
    PageResult getCategorisPage(PageQueryUtil pageUtil);

    /**
     * 根据parentId和level获取分类列表
     *
     * @param parentIds
     * @param categoryLevel
     * @return
     */
    List<ItemsCategory> selectByLevelAndParentIdsAndNumber(List<Long> parentIds, int categoryLevel);
}
