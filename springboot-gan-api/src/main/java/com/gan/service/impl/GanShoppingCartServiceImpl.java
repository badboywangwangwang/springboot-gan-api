package com.gan.service.impl;

import com.gan.api.gan.param.SaveCartItemParam;
import com.gan.api.gan.param.UpdateCartItemParam;
import com.gan.api.gan.vo.GanShoppingCartItemVO;
import com.gan.common.Constants;
import com.gan.common.GanException;
import com.gan.common.ServiceResultEnum;
import com.gan.dao.GanItemsMapper;
import com.gan.dao.GanShoppingCartItemMapper;
import com.gan.entity.GanItems;
import com.gan.entity.GanShoppingCartItem;
import com.gan.service.GanShoppingCartService;
import com.gan.util.BeanUtil;
import com.gan.util.PageQueryUtil;
import com.gan.util.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
public class GanShoppingCartServiceImpl implements GanShoppingCartService {



    @Autowired
    private GanShoppingCartItemMapper ganShoppingCartItemMapper;

    @Autowired
    private GanItemsMapper ganItemsMapper;


    @Override
    public String saveGanCartItem(SaveCartItemParam saveCartItemParam, Long userId) {
        GanShoppingCartItem temp = ganShoppingCartItemMapper.selectByUserIdAndGoodsId(userId, saveCartItemParam.getGoodsId());
        if (temp != null) {
            //已存在则修改该记录
            GanException.fail(ServiceResultEnum.SHOPPING_CART_ITEM_EXIST_ERROR.getResult());
        }
        GanItems ganItems = ganItemsMapper.selectByPrimaryKey(saveCartItemParam.getGoodsId());
        //商品为空
        if (ganItems == null) {
            return ServiceResultEnum.GOODS_NOT_EXIST.getResult();
        }
        int totalItem = ganShoppingCartItemMapper.selectCountByUserId(userId);
        //超出单个商品的最大数量
        if (saveCartItemParam.getGoodsCount() < 1) {
            return ServiceResultEnum.SHOPPING_CART_ITEM_NUMBER_ERROR.getResult();
        }
        //超出单个商品的最大数量
        if (saveCartItemParam.getGoodsCount() > Constants.SHOPPING_CART_ITEM_LIMIT_NUMBER) {
            return ServiceResultEnum.SHOPPING_CART_ITEM_LIMIT_NUMBER_ERROR.getResult();
        }
        //超出最大数量
        if (totalItem > Constants.SHOPPING_CART_ITEM_TOTAL_NUMBER) {
            return ServiceResultEnum.SHOPPING_CART_ITEM_TOTAL_NUMBER_ERROR.getResult();
        }
        GanShoppingCartItem ganShoppingCartItem = new GanShoppingCartItem();
        BeanUtil.copyProperties(saveCartItemParam, ganShoppingCartItem);
        ganShoppingCartItem.setUserId(userId);
        //保存记录
        if (ganShoppingCartItemMapper.insertSelective(ganShoppingCartItem) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public String updateGanCartItem(UpdateCartItemParam updateCartItemParam, Long userId) {
        GanShoppingCartItem ganShoppingCartItemUpdate = ganShoppingCartItemMapper.selectByPrimaryKey(updateCartItemParam.getCartItemId());
        if (ganShoppingCartItemUpdate == null) {
            return ServiceResultEnum.DATA_NOT_EXIST.getResult();
        }
        if (!ganShoppingCartItemUpdate.getUserId().equals(userId)) {
            GanException.fail(ServiceResultEnum.REQUEST_FORBIDEN_ERROR.getResult());
        }
        //超出单个商品的最大数量
        if (updateCartItemParam.getGoodsCount() > Constants.SHOPPING_CART_ITEM_LIMIT_NUMBER) {
            return ServiceResultEnum.SHOPPING_CART_ITEM_LIMIT_NUMBER_ERROR.getResult();
        }
        //当前登录账号的userId与待修改的cartItem中userId不同，返回错误
        if (!ganShoppingCartItemUpdate.getUserId().equals(userId)) {
            return ServiceResultEnum.NO_PERMISSION_ERROR.getResult();
        }
        //数值相同，则不执行数据操作
        if (updateCartItemParam.getGoodsCount().equals(ganShoppingCartItemUpdate.getGoodsCount())) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        ganShoppingCartItemUpdate.setGoodsCount(updateCartItemParam.getGoodsCount());
        ganShoppingCartItemUpdate.setUpdateTime(new Date());
        //修改记录
        if (ganShoppingCartItemMapper.updateByPrimaryKeySelective(ganShoppingCartItemUpdate) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public GanShoppingCartItem getGanCartItemById(Long ganShoppingCartItemId) {
        GanShoppingCartItem ganShoppingCartItem = ganShoppingCartItemMapper.selectByPrimaryKey(ganShoppingCartItemId);
        if (ganShoppingCartItem == null) {
            GanException.fail(ServiceResultEnum.DATA_NOT_EXIST.getResult());
        }
        return ganShoppingCartItem;
    }

    @Override
    public Boolean deleteById(Long shoppingCartItemId, Long userId) {
        GanShoppingCartItem ganShoppingCartItem = ganShoppingCartItemMapper.selectByPrimaryKey(shoppingCartItemId);
        if (ganShoppingCartItem == null) {
            return false;
        }
        //userId不同不能删除
        if (!userId.equals(ganShoppingCartItem.getUserId())) {
            return false;
        }
        return ganShoppingCartItemMapper.deleteByPrimaryKey(shoppingCartItemId) > 0;
    }


    @Override
    public List<GanShoppingCartItemVO> getMyShoppingCartItems(Long ganUserId) {
        List<GanShoppingCartItemVO> ganShoppingCartItemVOS = new ArrayList<>();
        List<GanShoppingCartItem> ganShoppingCartItems = ganShoppingCartItemMapper.selectByUserId(ganUserId, Constants.SHOPPING_CART_ITEM_TOTAL_NUMBER);
        return getGanShoppingCartItemVOS(ganShoppingCartItemVOS, ganShoppingCartItems);
    }

    @Override
    public List<GanShoppingCartItemVO> getCartItemsForSettle(List<Long> cartItemIds, Long ganUserId) {
        List<GanShoppingCartItemVO> ganShoppingCartItemVOS = new ArrayList<>();
        if (CollectionUtils.isEmpty(cartItemIds)) {
            GanException.fail("购物项不能为空");
        }
        List<GanShoppingCartItem> ganShoppingCartItems = ganShoppingCartItemMapper.selectByUserIdAndCartItemIds(ganUserId, cartItemIds);
        if (CollectionUtils.isEmpty(ganShoppingCartItems)) {
            GanException.fail("购物项不能为空");
        }
        if (ganShoppingCartItems.size() != cartItemIds.size()) {
            GanException.fail("参数异常");
        }
        return getGanShoppingCartItemVOS(ganShoppingCartItemVOS, ganShoppingCartItems);
    }



    @Override
    public PageResult getMyShoppingCartItems(PageQueryUtil pageUtil) {
        List<GanShoppingCartItemVO> ganShoppingCartItemVOS = new ArrayList<>();
        List<GanShoppingCartItem> ganShoppingCartItems = ganShoppingCartItemMapper.findMyganCartItems(pageUtil);
        int total = ganShoppingCartItemMapper.getTotalMyganCartItems(pageUtil);
        PageResult pageResult = new PageResult(getGanShoppingCartItemVOS(ganShoppingCartItemVOS, ganShoppingCartItems), total, pageUtil.getLimit(), pageUtil.getPage());
        return pageResult;
    }

    /**
     * 数据转换
     *
     * @param ganShoppingCartItemVOS
     * @param ganShoppingCartItems
     * @return
     */
    private List<GanShoppingCartItemVO> getGanShoppingCartItemVOS(List<GanShoppingCartItemVO> ganShoppingCartItemVOS, List<GanShoppingCartItem> ganShoppingCartItems) {
        if (!CollectionUtils.isEmpty(ganShoppingCartItems)) {
            //查询商品信息并做数据转换
            List<Long> ganItemsIds = ganShoppingCartItems.stream().map(GanShoppingCartItem::getGoodsId).collect(Collectors.toList());
            List<GanItems> ganItems = ganItemsMapper.selectByPrimaryKeys(ganItemsIds);
            Map<Long, GanItems> ganItemsMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(ganItems)) {
                ganItemsMap = ganItems.stream().collect(Collectors.toMap(GanItems::getGoodsId, Function.identity(), (entity1, entity2) -> entity1));
            }
            for (GanShoppingCartItem ganShoppingCartItem : ganShoppingCartItems) {
                GanShoppingCartItemVO ganShoppingCartItemVO = new GanShoppingCartItemVO();
                BeanUtil.copyProperties(ganShoppingCartItem, ganShoppingCartItemVO);
                if (ganItemsMap.containsKey(ganShoppingCartItem.getGoodsId())) {
                    GanItems ganItemsTemp = ganItemsMap.get(ganShoppingCartItem.getGoodsId());
                    ganShoppingCartItemVO.setGoodsCoverImg(ganItemsTemp.getGoodsCoverImg());
                    String goodsName = ganItemsTemp.getGoodsName();
                    // 字符串过长导致文字超出的问题
                    if (goodsName.length() > 28) {
                        goodsName = goodsName.substring(0, 28) + "...";
                    }
                    ganShoppingCartItemVO.setGoodsName(goodsName);
                    ganShoppingCartItemVO.setSellingPrice(ganItemsTemp.getSellingPrice());
                    ganShoppingCartItemVOS.add(ganShoppingCartItemVO);
                }
            }
        }
        return ganShoppingCartItemVOS;
    }
}
