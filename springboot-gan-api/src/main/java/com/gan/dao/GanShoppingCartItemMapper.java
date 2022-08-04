
package com.gan.dao;

import com.gan.entity.GanShoppingCartItem;
import com.gan.util.PageQueryUtil;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface GanShoppingCartItemMapper {
    int deleteByPrimaryKey(Long cartItemId);

    int insert(GanShoppingCartItem record);

    int insertSelective(GanShoppingCartItem record);

    GanShoppingCartItem selectByPrimaryKey(Long cartItemId);

    GanShoppingCartItem selectByUserIdAndGoodsId(@Param("newBeeMallUserId") Long newBeeMallUserId, @Param("goodsId") Long goodsId);

    List<GanShoppingCartItem> selectByUserId(@Param("newBeeMallUserId") Long newBeeMallUserId, @Param("number") int number);

    List<GanShoppingCartItem> selectByUserIdAndCartItemIds(@Param("newBeeMallUserId") Long newBeeMallUserId, @Param("cartItemIds") List<Long> cartItemIds);

    int selectCountByUserId(Long newBeeMallUserId);

    int updateByPrimaryKeySelective(GanShoppingCartItem record);

    int updateByPrimaryKey(GanShoppingCartItem record);

    int deleteBatch(List<Long> ids);

    List<GanShoppingCartItem> findMyganCartItems(PageQueryUtil pageUtil);

    int getTotalMyganCartItems(PageQueryUtil pageUtil);
}