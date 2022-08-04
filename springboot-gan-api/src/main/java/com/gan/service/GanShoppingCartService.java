
package com.gan.service;

import com.gan.api.gan.param.SaveCartItemParam;
import com.gan.api.gan.param.UpdateCartItemParam;
import com.gan.api.gan.vo.GanShoppingCartItemVO;
import com.gan.entity.GanShoppingCartItem;
import com.gan.util.PageQueryUtil;
import com.gan.util.PageResult;

import java.util.List;

public interface GanShoppingCartService {
    /**
     * 保存商品至购物车中
     *
     * @param saveCartItemParam
     * @param userId
     * @return
     */
    String saveGanCartItem(SaveCartItemParam saveCartItemParam, Long userId);

    /**
     * 修改购物车中的属性
     *
     * @param updateCartItemParam
     * @param userId
     * @return
     */
    String updateGanCartItem(UpdateCartItemParam updateCartItemParam, Long userId);

    /**
     * 获取购物项详情
     *
     * @param ganShoppingCartItemId
     * @return
     */
    GanShoppingCartItem getGanCartItemById(Long ganShoppingCartItemId);

    /**
     * 删除购物车中的商品
     *
     *
     * @param shoppingCartItemId
     * @param userId
     * @return
     */
    Boolean deleteById(Long shoppingCartItemId, Long userId);

    /**
     * 获取我的购物车中的列表数据
     *
     * @param ganUserId
     * @return
     */
    List<GanShoppingCartItemVO> getMyShoppingCartItems(Long ganUserId);

    /**
     * 根据userId和cartItemIds获取对应的购物项记录
     *
     * @param cartItemIds
     * @param ganUserId
     * @return
     */
    List<GanShoppingCartItemVO> getCartItemsForSettle(List<Long> cartItemIds, Long ganUserId);

    /**
     * 我的购物车(分页数据)
     *
     * @param pageUtil
     * @return
     */
    PageResult getMyShoppingCartItems(PageQueryUtil pageUtil);
}
