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
import org.springframework.transaction.annotation.Transactional;
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
        GanOrder newBeeMallOrder = ganOrderMapper.selectByOrderNo(orderNo);
        if (newBeeMallOrder == null) {
            GanException.fail(ServiceResultEnum.DATA_NOT_EXIST.getResult());
        }
        if (!userId.equals(newBeeMallOrder.getUserId())) {
            GanException.fail(ServiceResultEnum.REQUEST_FORBIDEN_ERROR.getResult());
        }
        List<GanOrderItem> orderItems = ganOrderItemMapper.selectByOrderId(newBeeMallOrder.getOrderId());
        //获取订单项数据
        if (CollectionUtils.isEmpty(orderItems)) {
            GanException.fail(ServiceResultEnum.ORDER_ITEM_NOT_EXIST_ERROR.getResult());
        }
        List<GanOrderItemVO> ganOrderItemVOS = BeanUtil.copyList(orderItems, GanOrderItemVO.class);
        GanOrderDetailVO newBeeMallOrderDetailVO = new GanOrderDetailVO();
        BeanUtil.copyProperties(newBeeMallOrder, newBeeMallOrderDetailVO);
        newBeeMallOrderDetailVO.setOrderStatusString(GanOrderStatusEnum.getGanOrderStatusEnumByStatus(newBeeMallOrderDetailVO.getOrderStatus()).getName());
        newBeeMallOrderDetailVO.setPayTypeString(PayTypeEnum.getPayTypeEnumByType(newBeeMallOrderDetailVO.getPayType()).getName());
        newBeeMallOrderDetailVO.setGanOrderItemVOS(ganOrderItemVOS);
        return newBeeMallOrderDetailVO;
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
                for (GanOrderListVO newBeeMallOrderListVO : orderListVOS) {
                    //封装每个订单列表对象的订单项数据
                    if (itemByOrderIdMap.containsKey(newBeeMallOrderListVO.getOrderId())) {
                        List<GanOrderItem> orderItemListTemp = itemByOrderIdMap.get(newBeeMallOrderListVO.getOrderId());
                        //将NewBeeMallOrderItem对象列表转换成NewBeeMallOrderItemVO对象列表
                        List<GanOrderItemVO> ganOrderItemVOS = BeanUtil.copyList(orderItemListTemp, GanOrderItemVO.class);
                        newBeeMallOrderListVO.setGanOrderItemVOS(ganOrderItemVOS);
                    }
                }
            }
        }
        PageResult pageResult = new PageResult(orderListVOS, total, pageUtil.getLimit(), pageUtil.getPage());
        return pageResult;
    }

    @Override
    public String cancelOrder(String orderNo, Long userId) {
        GanOrder newBeeMallOrder = ganOrderMapper.selectByOrderNo(orderNo);
        if (newBeeMallOrder != null) {
            //验证是否是当前userId下的订单，否则报错
            if (!userId.equals(newBeeMallOrder.getUserId())) {
                GanException.fail(ServiceResultEnum.NO_PERMISSION_ERROR.getResult());
            }
            //订单状态判断
            if (newBeeMallOrder.getOrderStatus().intValue() == GanOrderStatusEnum.ORDER_SUCCESS.getOrderStatus()
                    || newBeeMallOrder.getOrderStatus().intValue() == GanOrderStatusEnum.ORDER_CLOSED_BY_MALLUSER.getOrderStatus()
                    || newBeeMallOrder.getOrderStatus().intValue() == GanOrderStatusEnum.ORDER_CLOSED_BY_EXPIRED.getOrderStatus()
                    || newBeeMallOrder.getOrderStatus().intValue() == GanOrderStatusEnum.ORDER_CLOSED_BY_JUDGE.getOrderStatus()) {
                return ServiceResultEnum.ORDER_STATUS_ERROR.getResult();
            }
            if (ganOrderMapper.closeOrder(Collections.singletonList(newBeeMallOrder.getOrderId()), GanOrderStatusEnum.ORDER_CLOSED_BY_MALLUSER.getOrderStatus()) > 0) {
                return ServiceResultEnum.SUCCESS.getResult();
            } else {
                return ServiceResultEnum.DB_ERROR.getResult();
            }
        }
        return ServiceResultEnum.ORDER_NOT_EXIST_ERROR.getResult();
    }

    @Override
    public String finishOrder(String orderNo, Long userId) {
        GanOrder newBeeMallOrder = ganOrderMapper.selectByOrderNo(orderNo);
        if (newBeeMallOrder != null) {
            //验证是否是当前userId下的订单，否则报错
            if (!userId.equals(newBeeMallOrder.getUserId())) {
                return ServiceResultEnum.NO_PERMISSION_ERROR.getResult();
            }
            //订单状态判断 非出库状态下不进行修改操作
            if (newBeeMallOrder.getOrderStatus().intValue() != GanOrderStatusEnum.ORDER_EXPRESS.getOrderStatus()) {
                return ServiceResultEnum.ORDER_STATUS_ERROR.getResult();
            }
            newBeeMallOrder.setOrderStatus((byte) GanOrderStatusEnum.ORDER_SUCCESS.getOrderStatus());
            newBeeMallOrder.setUpdateTime(new Date());
            if (ganOrderMapper.updateByPrimaryKeySelective(newBeeMallOrder) > 0) {
                return ServiceResultEnum.SUCCESS.getResult();
            } else {
                return ServiceResultEnum.DB_ERROR.getResult();
            }
        }
        return ServiceResultEnum.ORDER_NOT_EXIST_ERROR.getResult();
    }

    @Override
    public String paySuccess(String orderNo, int payType) {
        GanOrder newBeeMallOrder = ganOrderMapper.selectByOrderNo(orderNo);
        if (newBeeMallOrder != null) {
            //订单状态判断 非待支付状态下不进行修改操作
            if (newBeeMallOrder.getOrderStatus().intValue() != GanOrderStatusEnum.ORDER_PRE_PAY.getOrderStatus()) {
                return ServiceResultEnum.ORDER_STATUS_ERROR.getResult();
            }
            newBeeMallOrder.setOrderStatus((byte) GanOrderStatusEnum.ORDER_PAID.getOrderStatus());
            newBeeMallOrder.setPayType((byte) payType);
            newBeeMallOrder.setPayStatus((byte) PayStatusEnum.PAY_SUCCESS.getPayStatus());
            newBeeMallOrder.setPayTime(new Date());
            newBeeMallOrder.setUpdateTime(new Date());
            if (ganOrderMapper.updateByPrimaryKeySelective(newBeeMallOrder) > 0) {
                return ServiceResultEnum.SUCCESS.getResult();
            } else {
                return ServiceResultEnum.DB_ERROR.getResult();
            }
        }
        return ServiceResultEnum.ORDER_NOT_EXIST_ERROR.getResult();
    }

    @Override
    @Transactional
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
        Map<Long, GanItems> newBeeMallGoodsMap = ganItems.stream().collect(Collectors.toMap(GanItems::getGoodsId, Function.identity(), (entity1, entity2) -> entity1));
        //判断商品库存
        for (GanShoppingCartItemVO shoppingCartItemVO : itemsForSave) {
            //查出的商品中不存在购物车中的这条关联商品数据，直接返回错误提醒
            if (!newBeeMallGoodsMap.containsKey(shoppingCartItemVO.getGoodsId())) {
                GanException.fail(ServiceResultEnum.SHOPPING_ITEM_ERROR.getResult());
            }
            //存在数量大于库存的情况，直接返回错误提醒
            if (shoppingCartItemVO.getGoodsCount() > newBeeMallGoodsMap.get(shoppingCartItemVO.getGoodsId()).getStockNum()) {
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
                for (GanShoppingCartItemVO newBeeMallShoppingCartItemVO : itemsForSave) {
                    priceTotal += newBeeMallShoppingCartItemVO.getGoodsCount() * newBeeMallShoppingCartItemVO.getSellingPrice();
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
                    GanOrderAddress newBeeMallOrderAddress = new GanOrderAddress();
                    BeanUtil.copyProperties(address, newBeeMallOrderAddress);
                    newBeeMallOrderAddress.setOrderId(ganOrder.getOrderId());
                    //生成所有的订单项快照，并保存至数据库
                    List<GanOrderItem> newBeeMallOrderItems = new ArrayList<>();
                    for (GanShoppingCartItemVO newBeeMallShoppingCartItemVO : itemsForSave) {
                        GanOrderItem newBeeMallOrderItem = new GanOrderItem();
                        //使用BeanUtil工具类将newBeeMallShoppingCartItemVO中的属性复制到newBeeMallOrderItem对象中
                        BeanUtil.copyProperties(newBeeMallShoppingCartItemVO, newBeeMallOrderItem);
                        //GanOrderMapper文件insert()方法中使用了useGeneratedKeys因此orderId可以获取到
                        newBeeMallOrderItem.setOrderId(ganOrder.getOrderId());
                        newBeeMallOrderItems.add(newBeeMallOrderItem);
                    }
                    //保存至数据库
                    if (ganOrderItemMapper.insertBatch(newBeeMallOrderItems) > 0 && ganOrderAddressMapper.insertSelective(newBeeMallOrderAddress) > 0) {
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
    @Transactional
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
    @Transactional
    public String checkDone(Long[] ids) {
        //查询所有的订单 判断状态 修改状态和更新时间
        List<GanOrder> orders = ganOrderMapper.selectByPrimaryKeys(Arrays.asList(ids));
        String errorOrderNos = "";
        if (!CollectionUtils.isEmpty(orders)) {
            for (GanOrder newBeeMallOrder : orders) {
                if (newBeeMallOrder.getIsDeleted() == 1) {
                    errorOrderNos += newBeeMallOrder.getOrderNo() + " ";
                    continue;
                }
                if (newBeeMallOrder.getOrderStatus() != 1) {
                    errorOrderNos += newBeeMallOrder.getOrderNo() + " ";
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
    @Transactional
    public String checkOut(Long[] ids) {
        //查询所有的订单 判断状态 修改状态和更新时间
        List<GanOrder> orders = ganOrderMapper.selectByPrimaryKeys(Arrays.asList(ids));
        String errorOrderNos = "";
        if (!CollectionUtils.isEmpty(orders)) {
            for (GanOrder newBeeMallOrder : orders) {
                if (newBeeMallOrder.getIsDeleted() == 1) {
                    errorOrderNos += newBeeMallOrder.getOrderNo() + " ";
                    continue;
                }
                if (newBeeMallOrder.getOrderStatus() != 1 && newBeeMallOrder.getOrderStatus() != 2) {
                    errorOrderNos += newBeeMallOrder.getOrderNo() + " ";
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
    @Transactional
    public String closeOrder(Long[] ids) {
        //查询所有的订单 判断状态 修改状态和更新时间
        List<GanOrder> orders = ganOrderMapper.selectByPrimaryKeys(Arrays.asList(ids));
        String errorOrderNos = "";
        if (!CollectionUtils.isEmpty(orders)) {
            for (GanOrder newBeeMallOrder : orders) {
                // isDeleted=1 一定为已关闭订单
                if (newBeeMallOrder.getIsDeleted() == 1) {
                    errorOrderNos += newBeeMallOrder.getOrderNo() + " ";
                    continue;
                }
                //已关闭或者已完成无法关闭订单
                if (newBeeMallOrder.getOrderStatus() == 4 || newBeeMallOrder.getOrderStatus() < 0) {
                    errorOrderNos += newBeeMallOrder.getOrderNo() + " ";
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



