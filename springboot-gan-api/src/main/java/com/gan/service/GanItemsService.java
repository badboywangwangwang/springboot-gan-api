
package com.gan.service;

import com.gan.entity.GanItems;
import com.gan.util.PageQueryUtil;
import com.gan.util.PageResult;

import com.gan.util.PageQueryUtil;
import com.gan.util.PageResult;

import java.util.List;

public interface GanItemsService {
    /**
     * 后台分页
     *
     * @param pageUtil
     * @return
     */
    PageResult getGanItemsPage(PageQueryUtil pageUtil);

    /**
     * 添加商品
     *
     * @param ganItems
     * @return
     */
    String saveGanItems(GanItems ganItems);

    /**
     * 批量新增商品数据
     *
     * @param ganItemsList
     * @return
     */
    void batchSaveGanItems(List<GanItems> ganItemsList);

    /**
     * 修改商品信息
     *
     * @param goods
     * @return
     */
    String updateGanItems(GanItems goods);

    /**
     * 批量修改销售状态(上架下架)
     *
     * @param ids
     * @return
     */
    Boolean batchUpdateSellStatus(Long[] ids, int sellStatus);

    /**
     * 获取商品详情
     *
     * @param id
     * @return
     */
    GanItems getGanItemsById(Long id);

    /**
     * 商品搜索
     *
     * @param pageUtil
     * @return
     */
    PageResult searchGanItems(PageQueryUtil pageUtil);
}
