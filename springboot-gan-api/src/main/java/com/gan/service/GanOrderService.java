
package com.gan.service;

import com.gan.api.gan.vo.GanOrderDetailVO;
import com.gan.api.gan.vo.GanOrderItemVO;
import com.gan.api.gan.vo.GanShoppingCartItemVO;
import com.gan.entity.GanUser;
import com.gan.entity.GanUserAddress;
import com.gan.entity.GanOrder;
import com.gan.util.PageQueryUtil;
import com.gan.util.PageResult;

import java.util.List;

public interface GanOrderService {
    /**
     * 获取订单详情
     *
     * @param orderId
     * @return
     */
    GanOrderDetailVO getOrderDetailByOrderId(Long orderId);

    /**
     * 获取订单详情
     *
     * @param orderNo
     * @param userId
     * @return
     */
    GanOrderDetailVO getOrderDetailByOrderNo(String orderNo, Long userId);

    /**
     * 我的订单列表
     *
     * @param pageUtil
     * @return
     */
    PageResult getMyOrders(PageQueryUtil pageUtil);

    /**
     * 手动取消订单
     *
     * @param orderNo
     * @param userId
     * @return
     */
    String cancelOrder(String orderNo, Long userId);

    /**
     * 确认收货
     *
     * @param orderNo
     * @param userId
     * @return
     */
    String finishOrder(String orderNo, Long userId);

    String paySuccess(String orderNo, int payType);

    String saveOrder(GanUser loginGanUser, GanUserAddress address, List<GanShoppingCartItemVO> itemsForSave);

    /**
     * 后台分页
     *
     * @param pageUtil
     * @return
     */
    PageResult getGanOrdersPage(PageQueryUtil pageUtil);

    /**
     * 订单信息修改
     *
     * @param ganOrder
     * @return
     */
    String updateOrderInfo(GanOrder ganOrder);

    /**
     * 配货
     *
     * @param ids
     * @return
     */
    String checkDone(Long[] ids);

    /**
     * 出库
     *
     * @param ids
     * @return
     */
    String checkOut(Long[] ids);

    /**
     * 关闭订单
     *
     * @param ids
     * @return
     */
    String closeOrder(Long[] ids);

    List<GanOrderItemVO> getOrderItems(Long orderId);
}
