
package com.gan.dao;

import com.gan.entity.GanOrder;
import com.gan.util.PageQueryUtil;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface GanOrderMapper {
    int deleteByPrimaryKey(Long orderId);

    int insert(GanOrder record);

    int insertSelective(GanOrder record);

    GanOrder selectByPrimaryKey(Long orderId);

    GanOrder selectByOrderNo(String orderNo);

    int updateByPrimaryKeySelective(GanOrder record);

    int updateByPrimaryKey(GanOrder record);

    List<GanOrder> findGanOrderList(PageQueryUtil pageUtil);

    int getTotalGanOrders(PageQueryUtil pageUtil);

    List<GanOrder> selectByPrimaryKeys(@Param("orderIds") List<Long> orderIds);

    int checkOut(@Param("orderIds") List<Long> orderIds);

    int closeOrder(@Param("orderIds") List<Long> orderIds, @Param("orderStatus") int orderStatus);

    int checkDone(@Param("orderIds") List<Long> asList);
}