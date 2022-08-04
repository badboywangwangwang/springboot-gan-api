
package com.gan.dao;

import com.gan.entity.GanOrderItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface GanOrderItemMapper {
    int deleteByPrimaryKey(Long orderItemId);

    int insert(GanOrderItem record);

    int insertSelective(GanOrderItem record);

    GanOrderItem selectByPrimaryKey(Long orderItemId);

    /**
     * 根据订单id获取订单项列表
     *
     * @param orderId
     * @return
     */
    List<GanOrderItem> selectByOrderId(Long orderId);

    /**
     * 根据订单ids获取订单项列表
     *
     * @param orderIds
     * @return
     */
    List<GanOrderItem> selectByOrderIds(@Param("orderIds") List<Long> orderIds);

    /**
     * 批量insert订单项数据
     *
     * @param orderItems
     * @return
     */
    int insertBatch(@Param("orderItems") List<GanOrderItem> orderItems);

    int updateByPrimaryKeySelective(GanOrderItem record);

    int updateByPrimaryKey(GanOrderItem record);
}