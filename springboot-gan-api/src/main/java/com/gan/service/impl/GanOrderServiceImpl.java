package com.gan.service.impl;

import com.gan.api.gan.vo.GanOrderDetailVO;
import com.gan.api.gan.vo.GanOrderItemVO;
import com.gan.api.gan.vo.GanOrderListVO;
import com.gan.api.gan.vo.GanShoppingCartItemVO;
import com.gan.common.*;
import com.gan.dao.*;
import com.gan.entity.*;
import com.gan.service.GanOrderService;
import com.gan.util.BeanUtil;
import com.gan.util.NumberUtil;
import com.gan.util.PageQueryUtil;
import com.gan.util.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Service
public class GanOrderServiceImpl implements GanOrderService {

    @Autowired
    private GanOrderMapper ganOrderMapper;
    @Autowired
    private GanOrderItemMapper ganOrderItemMapper;
    @Autowired
    private GanShoppingCartItemMapper ganShoppingCartItemMapper;
    @Autowired
    private GanItemsMapper ganItemsMapper;
    @Autowired
    private GanOrderAddressMapper ganOrderAddressMapper;


    @Override
    public String updateOrderInfo(GanOrder ganOrder) {
        GanOrder temp = ganOrderMapper.selectByPrimaryKey(ganOrder.getOrderId());
        //不为空且orderStatus>=0且状态为出库之前可以修改部分信息
        if (temp != null && temp.getOrderStatus() >= 0 && temp.getOrderStatus() < 3) {
            temp.setTotalPrice(ganOrder.getTotalPrice());
            temp.setUpdateTime(new Date());
            if (ganOrderMapper.updateByPrimaryKeySelective(temp) > 0) {
                return ServiceResultEnum.SUCCESS.getResult();
            }
            return ServiceResultEnum.DB_ERROR.getResult();
        }
        return ServiceResultEnum.DATA_NOT_EXIST.getResult();
    }
    @Override
    public GanOrderDetailVO getOrderDetailByOrderId(Long orderId) {
        GanOrder ganOrder = ganOrderMapper.selectByPrimaryKey(orderId);
        if (ganOrder == null) {
            GanException.fail(ServiceResultEnum.DATA_NOT_EXIST.getResult());
        }
        List<GanOrderItem> orderItems = ganOrderItemMapper.selectByOrderId(ganOrder.getOrderId());
        //获取订单项数据
        if (!CollectionUtils.isEmpty(orderItems)) {
            List<GanOrderItemVO> ganOrderItemVOS = BeanUtil.copyList(orderItems, GanOrderItemVO.class);
            GanOrderDetailVO ganOrderDetailVO = new GanOrderDetailVO();
            BeanUtil.copyProperties(ganOrder, ganOrderDetailVO);
            ganOrderDetailVO.setOrderStatusString(GanOrderStatusEnum.getGanOrderStatusEnumByStatus(ganOrderDetailVO.getOrderStatus()).getName());
            ganOrderDetailVO.setPayTypeString(PayTypeEnum.getPayTypeEnumByType(ganOrderDetailVO.getPayType()).getName());
            ganOrderDetailVO.setGanOrderItemVOS(ganOrderItemVOS);
            return ganOrderDetailVO;
        } else {
            GanException.fail(ServiceResultEnum.ORDER_ITEM_NULL_ERROR.getResult());
            return null;
        }
    }

    @Override
    public GanOrderDetailVO getOrderDetailByOrderNo(String orderNo, Long userId) {
        GanOrder ganOrder = ganOrderMapper.selectByOrderNo(orderNo);
        if (ganOrder == null) {
            GanException.fail(ServiceResultEnum.DATA_NOT_EXIST.getResult());
        }
        if (!userId.equals(ganOrder.getUserId())) {
            GanException.fail(ServiceResultEnum.REQUEST_FORBIDEN_ERROR.getResult());
        }
        List<GanOrderItem> orderItems = ganOrderItemMapper.selectByOrderId(ganOrder.getOrderId());
        //获取订单项数据
        if (CollectionUtils.isEmpty(orderItems)) {
            GanException.fail(ServiceResultEnum.ORDER_ITEM_NOT_EXIST_ERROR.getResult());
        }
        List<GanOrderItemVO> ganOrderItemVOS = BeanUtil.copyList(orderItems, GanOrderItemVO.class);
        GanOrderDetailVO ganOrderDetailVO = new GanOrderDetailVO();
        BeanUtil.copyProperties(ganOrder, ganOrderDetailVO);
        ganOrderDetailVO.setOrderStatusString(GanOrderStatusEnum.getGanOrderStatusEnumByStatus(ganOrderDetailVO.getOrderStatus()).getName());
        ganOrderDetailVO.setPayTypeString(PayTypeEnum.getPayTypeEnumByType(ganOrderDetailVO.getPayType()).getName());
        ganOrderDetailVO.setGanOrderItemVOS(ganOrderItemVOS);
        return ganOrderDetailVO;
    }

    @Override
    public PageResult getMyOrders(PageQueryUtil pageUtil) {
        int total = ganOrderMapper.getTotalGanOrders(pageUtil);
        List<GanOrder> ganOrders = ganOrderMapper.findGanOrderList(pageUtil);
        List<GanOrderListVO> orderListVOS = new ArrayList<>();
        if (total > 0) {
            //数据转换 将实体类转成vo
            orderListVOS = BeanUtil.copyList(ganOrders, GanOrderListVO.class);
            //设置订单状态中文显示值
            for (GanOrderListVO ganOrderListVO : orderListVOS) {
                ganOrderListVO.setOrderStatusString(GanOrderStatusEnum.getGanOrderStatusEnumByStatus(ganOrderListVO.getOrderStatus()).getName());
            }
            List<Long> orderIds = ganOrders.stream().map(GanOrder::getOrderId).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(orderIds)) {
                List<GanOrderItem> orderItems = ganOrderItemMapper.selectByOrderIds(orderIds);
                Map<Long, List<GanOrderItem>> itemByOrderIdMap = orderItems.stream().collect(groupingBy(GanOrderItem::getOrderId));
                for (GanOrderListVO ganOrderListVO : orderListVOS) {
                    //封装每个订单列表对象的订单项数据
                    if (itemByOrderIdMap.containsKey(ganOrderListVO.getOrderId())) {
                        List<GanOrderItem> orderItemListTemp = itemByOrderIdMap.get(ganOrderListVO.getOrderId());
                        //将GanOrderItem对象列表转换成GanOrderItemVO对象列表
                        List<GanOrderItemVO> ganOrderItemVOS = BeanUtil.copyList(orderItemListTemp, GanOrderItemVO.class);
                        ganOrderListVO.setGanOrderItemVOS(ganOrderItemVOS);
                    }
                }
            }
        }
        PageResult pageResult = new PageResult(orderListVOS, total, pageUtil.getLimit(), pageUtil.getPage());
        return pageResult;
    }

    @Override
    public String cancelOrder(String orderNo, Long userId) {
        GanOrder ganOrder = ganOrderMapper.selectByOrderNo(orderNo);
        if (ganOrder != null) {
            //验证是否是当前userId下的订单，否则报错
            if (!userId.equals(ganOrder.getUserId())) {
                GanException.fail(ServiceResultEnum.NO_PERMISSION_ERROR.getResult());
            }
            //订单状态判断
            if (ganOrder.getOrderStatus().intValue() == GanOrderStatusEnum.ORDER_SUCCESS.getOrderStatus()
                    || ganOrder.getOrderStatus().intValue() == GanOrderStatusEnum.ORDER_CLOSED_BY_MALLUSER.getOrderStatus()
                    || ganOrder.getOrderStatus().intValue() == GanOrderStatusEnum.ORDER_CLOSED_BY_EXPIRED.getOrderStatus()
                    || ganOrder.getOrderStatus().intValue() == GanOrderStatusEnum.ORDER_CLOSED_BY_JUDGE.getOrderStatus()) {
                return ServiceResultEnum.ORDER_STATUS_ERROR.getResult();
            }
            if (ganOrderMapper.closeOrder(Collections.singletonList(ganOrder.getOrderId()), GanOrderStatusEnum.ORDER_CLOSED_BY_MALLUSER.getOrderStatus()) > 0) {
                return ServiceResultEnum.SUCCESS.getResult();
            } else {
                return ServiceResultEnum.DB_ERROR.getResult();
            }
        }
        return ServiceResultEnum.ORDER_NOT_EXIST_ERROR.getResult();
    }

    @Override
    public String finishOrder(String orderNo, Long userId) {
        GanOrder ganOrder = ganOrderMapper.selectByOrderNo(orderNo);
        if (ganOrder != null) {
            //验证是否是当前userId下的订单，否则报错
            if (!userId.equals(ganOrder.getUserId())) {
                return ServiceResultEnum.NO_PERMISSION_ERROR.getResult();
            }
            //订单状态判断 非出库状态下不进行修改操作
            if (ganOrder.getOrderStatus().intValue() != GanOrderStatusEnum.ORDER_EXPRESS.getOrderStatus()) {
                return ServiceResultEnum.ORDER_STATUS_ERROR.getResult();
            }
            ganOrder.setOrderStatus((byte) GanOrderStatusEnum.ORDER_SUCCESS.getOrderStatus());
            ganOrder.setUpdateTime(new Date());
            if (ganOrderMapper.updateByPrimaryKeySelective(ganOrder) > 0) {
                return ServiceResultEnum.SUCCESS.getResult();
            } else {
                return ServiceResultEnum.DB_ERROR.getResult();
            }
        }
        return ServiceResultEnum.ORDER_NOT_EXIST_ERROR.getResult();
    }

    @Override
    public String paySuccess(String orderNo, int payType) {
        GanOrder ganOrder = ganOrderMapper.selectByOrderNo(orderNo);
        if (ganOrder != null) {
            //订单状态判断 非待支付状态下不进行修改操作
            if (ganOrder.getOrderStatus().intValue() != GanOrderStatusEnum.ORDER_PRE_PAY.getOrderStatus()) {
                return ServiceResultEnum.ORDER_STATUS_ERROR.getResult();
            }
            ganOrder.setOrderStatus((byte) GanOrderStatusEnum.ORDER_PAID.getOrderStatus());
            ganOrder.setPayType((byte) payType);
            ganOrder.setPayStatus((byte) PayStatusEnum.PAY_SUCCESS.getPayStatus());
            ganOrder.setPayTime(new Date());
            ganOrder.setUpdateTime(new Date());
            if (ganOrderMapper.updateByPrimaryKeySelective(ganOrder) > 0) {
                return ServiceResultEnum.SUCCESS.getResult();
            } else {
                return ServiceResultEnum.DB_ERROR.getResult();
            }
        }
        return ServiceResultEnum.ORDER_NOT_EXIST_ERROR.getResult();
    }

    @Override
    public String saveOrder(GanUser loginGanUser, GanUserAddress address, List<GanShoppingCartItemVO> itemsForSave) {
        List<Long> itemIdList = itemsForSave.stream().map(GanShoppingCartItemVO::getCartItemId).collect(Collectors.toList());
        List<Long> itemsIds = itemsForSave.stream().map(GanShoppingCartItemVO::getGoodsId).collect(Collectors.toList());
        List<GanItems> ganItems = ganItemsMapper.selectByPrimaryKeys(itemsIds);
        //检查是否包含已下架商品
        List<GanItems> itemsListNotSelling = ganItems.stream()
                .filter(ganItemsTemp -> ganItemsTemp.getGoodsSellStatus() != Constants.SELL_STATUS_UP)
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(itemsListNotSelling)) {
            //goodsListNotSelling 对象非空则表示有下架商品
            GanException.fail(itemsListNotSelling.get(0).getGoodsName() + "已下架，无法生成订单");
        }
        Map<Long, GanItems> ganItemsMap = ganItems.stream().collect(Collectors.toMap(GanItems::getGoodsId, Function.identity(), (entity1, entity2) -> entity1));
        //判断商品库存
        for (GanShoppingCartItemVO shoppingCartItemVO : itemsForSave) {
            //查出的商品中不存在购物车中的这条关联商品数据，直接返回错误提醒
            if (!ganItemsMap.containsKey(shoppingCartItemVO.getGoodsId())) {
                GanException.fail(ServiceResultEnum.SHOPPING_ITEM_ERROR.getResult());
            }
            //存在数量大于库存的情况，直接返回错误提醒
            if (shoppingCartItemVO.getGoodsCount() > ganItemsMap.get(shoppingCartItemVO.getGoodsId()).getStockNum()) {
                GanException.fail(ServiceResultEnum.SHOPPING_ITEM_COUNT_ERROR.getResult());
            }
        }
        //删除购物项
        if (!CollectionUtils.isEmpty(itemIdList) && !CollectionUtils.isEmpty(ganItems) && !CollectionUtils.isEmpty(ganItems)) {
            if (ganShoppingCartItemMapper .deleteBatch(itemIdList) > 0) {
                List<StockNumDTO> stockNumDTOS = BeanUtil.copyList(itemsForSave, StockNumDTO.class);
                int updateStockNumResult = ganItemsMapper.updateStockNum(stockNumDTOS);
                if (updateStockNumResult < 1) {
                    GanException.fail(ServiceResultEnum.SHOPPING_ITEM_COUNT_ERROR.getResult());
                }
                //生成订单号
                String orderNo = NumberUtil.genOrderNo();
                int priceTotal = 0;
                //保存订单
                GanOrder ganOrder = new GanOrder();
                ganOrder.setOrderNo(orderNo);
                ganOrder.setUserId(loginGanUser.getUserId());
                //总价
                for (GanShoppingCartItemVO ganShoppingCartItemVO : itemsForSave) {
                    priceTotal += ganShoppingCartItemVO.getGoodsCount() * ganShoppingCartItemVO.getSellingPrice();
                }
                if (priceTotal < 1) {
                    GanException.fail(ServiceResultEnum.ORDER_PRICE_ERROR.getResult());
                }
                ganOrder.setTotalPrice(priceTotal);
                String extraInfo = "";
                ganOrder.setExtraInfo(extraInfo);
                //生成订单项并保存订单项纪录
                if (ganOrderMapper.insertSelective(ganOrder) > 0) {
                    //生成订单收货地址快照，并保存至数据库
                    GanOrderAddress ganOrderAddress = new GanOrderAddress();
                    BeanUtil.copyProperties(address, ganOrderAddress);
                    ganOrderAddress.setOrderId(ganOrder.getOrderId());
                    //生成所有的订单项快照，并保存至数据库
                    List<GanOrderItem> ganOrderItems = new ArrayList<>();
                    for (GanShoppingCartItemVO ganShoppingCartItemVO : itemsForSave) {
                        GanOrderItem ganOrderItem = new GanOrderItem();
                        //使用BeanUtil工具类将ganShoppingCartItemVO中的属性复制到ganOrderItem对象中
                        BeanUtil.copyProperties(ganShoppingCartItemVO, ganOrderItem);
                        //GanOrderMapper文件insert()方法中使用了useGeneratedKeys因此orderId可以获取到
                        ganOrderItem.setOrderId(ganOrder.getOrderId());
                        ganOrderItems.add(ganOrderItem);
                    }
                    //保存至数据库
                    if (ganOrderItemMapper.insertBatch(ganOrderItems) > 0 && ganOrderAddressMapper.insertSelective(ganOrderAddress) > 0) {
                        //所有操作成功后，将订单号返回，以供Controller方法跳转到订单详情
                        return orderNo;
                    }
                    GanException.fail(ServiceResultEnum.ORDER_PRICE_ERROR.getResult());
                }
                GanException.fail(ServiceResultEnum.DB_ERROR.getResult());
            }
            GanException.fail(ServiceResultEnum.DB_ERROR.getResult());
        }
        GanException.fail(ServiceResultEnum.SHOPPING_ITEM_ERROR.getResult());
        return ServiceResultEnum.SHOPPING_ITEM_ERROR.getResult();
    }

    @Override
    public String checkDone(Long[] ids) {
        //查询所有的订单 判断状态 修改状态和更新时间
        List<GanOrder> orders = ganOrderMapper.selectByPrimaryKeys(Arrays.asList(ids));
        String errorOrderNos = "";
        if (!CollectionUtils.isEmpty(orders)) {
            for (GanOrder ganOrder : orders) {
                if (ganOrder.getIsDeleted() == 1) {
                    errorOrderNos += ganOrder.getOrderNo() + " ";
                    continue;
                }
                if (ganOrder.getOrderStatus() != 1) {
                    errorOrderNos += ganOrder.getOrderNo() + " ";
                }
            }
            if (StringUtils.isEmpty(errorOrderNos)) {
                //订单状态正常 可以执行配货完成操作 修改订单状态和更新时间
                if (ganOrderMapper.checkDone(Arrays.asList(ids)) > 0) {
                    return ServiceResultEnum.SUCCESS.getResult();
                } else {
                    return ServiceResultEnum.DB_ERROR.getResult();
                }
            } else {
                //订单此时不可执行出库操作
                if (errorOrderNos.length() > 0 && errorOrderNos.length() < 100) {
                    return errorOrderNos + "订单的状态不是支付成功无法执行出库操作";
                } else {
                    return "你选择了太多状态不是支付成功的订单，无法执行配货完成操作";
                }
            }
        }
        //未查询到数据 返回错误提示
        return ServiceResultEnum.DATA_NOT_EXIST.getResult();
    }

    @Override
    public String checkOut(Long[] ids) {
        //查询所有的订单 判断状态 修改状态和更新时间
        List<GanOrder> orders = ganOrderMapper.selectByPrimaryKeys(Arrays.asList(ids));
        String errorOrderNos = "";
        if (!CollectionUtils.isEmpty(orders)) {
            for (GanOrder ganOrder : orders) {
                if (ganOrder.getIsDeleted() == 1) {
                    errorOrderNos += ganOrder.getOrderNo() + " ";
                    continue;
                }
                if (ganOrder.getOrderStatus() != 1 && ganOrder.getOrderStatus() != 2) {
                    errorOrderNos += ganOrder.getOrderNo() + " ";
                }
            }
            if (StringUtils.isEmpty(errorOrderNos)) {
                //订单状态正常 可以执行出库操作 修改订单状态和更新时间
                if (ganOrderMapper.checkOut(Arrays.asList(ids)) > 0) {
                    return ServiceResultEnum.SUCCESS.getResult();
                } else {
                    return ServiceResultEnum.DB_ERROR.getResult();
                }
            } else {
                //订单此时不可执行出库操作
                if (errorOrderNos.length() > 0 && errorOrderNos.length() < 100) {
                    return errorOrderNos + "订单的状态不是支付成功或配货完成无法执行出库操作";
                } else {
                    return "你选择了太多状态不是支付成功或配货完成的订单，无法执行出库操作";
                }
            }
        }
        //未查询到数据 返回错误提示
        return ServiceResultEnum.DATA_NOT_EXIST.getResult();
    }

    @Override
    public String closeOrder(Long[] ids) {
        //查询所有的订单 判断状态 修改状态和更新时间
        List<GanOrder> orders = ganOrderMapper.selectByPrimaryKeys(Arrays.asList(ids));
        String errorOrderNos = "";
        if (!CollectionUtils.isEmpty(orders)) {
            for (GanOrder ganOrder : orders) {
                // isDeleted=1 一定为已关闭订单
                if (ganOrder.getIsDeleted() == 1) {
                    errorOrderNos += ganOrder.getOrderNo() + " ";
                    continue;
                }
                //已关闭或者已完成无法关闭订单
                if (ganOrder.getOrderStatus() == 4 || ganOrder.getOrderStatus() < 0) {
                    errorOrderNos += ganOrder.getOrderNo() + " ";
                }
            }
            if (StringUtils.isEmpty(errorOrderNos)) {
                //订单状态正常 可以执行关闭操作 修改订单状态和更新时间
                if (ganOrderMapper.closeOrder(Arrays.asList(ids), GanOrderStatusEnum.ORDER_CLOSED_BY_JUDGE.getOrderStatus()) > 0) {
                    return ServiceResultEnum.SUCCESS.getResult();
                } else {
                    return ServiceResultEnum.DB_ERROR.getResult();
                }
            } else {
                //订单此时不可执行关闭操作
                if (errorOrderNos.length() > 0 && errorOrderNos.length() < 100) {
                    return errorOrderNos + "订单不能执行关闭操作";
                } else {
                    return "你选择的订单不能执行关闭操作";
                }
            }
        }
        //未查询到数据 返回错误提示
        return ServiceResultEnum.DATA_NOT_EXIST.getResult();
    }


    @Override
    public PageResult getGanOrdersPage(PageQueryUtil pageUtil) {
        List<GanOrder> ganOrders = ganOrderMapper.findGanOrderList(pageUtil);
        int total = ganOrderMapper.getTotalGanOrders(pageUtil);
        PageResult pageResult = new PageResult(ganOrders, total, pageUtil.getLimit(), pageUtil.getPage());
        return pageResult;
    }

    @Override
    public List<GanOrderItemVO> getOrderItems(Long orderId) {
        GanOrder ganOrder = ganOrderMapper.selectByPrimaryKey(orderId);
        if (ganOrder != null) {
            List<GanOrderItem> orderItems = ganOrderItemMapper.selectByOrderId(ganOrder.getOrderId());
            //获取订单项数据
            if (!CollectionUtils.isEmpty(orderItems)) {
                List<GanOrderItemVO> ganOrderItemVOS = BeanUtil.copyList(orderItems, GanOrderItemVO.class);
                return ganOrderItemVOS;
            }
        }
        return null;
    }
}



