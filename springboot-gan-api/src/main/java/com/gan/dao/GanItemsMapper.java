/**
 * 严肃声明：
 * 开源版本请务必保留此注释头信息，若删除我方将保留所有法律责任追究！
 * 本软件已申请软件著作权，受国家版权局知识产权以及国家计算机软件著作权保护！
 * 可正常分享和学习源码，不得用于违法犯罪活动，违者必究！
 * Copyright (c) 2019-2021 十三 all rights reserved.
 * 版权所有，侵权必究！
 */
package com.gan.dao;

import com.gan.entity.GanItems;
import com.gan.entity.StockNumDTO;
import com.gan.util.PageQueryUtil;
import com.gan.entity.StockNumDTO;
import com.gan.util.PageQueryUtil;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface GanItemsMapper {
    int deleteByPrimaryKey(Long goodsId);

    int insert(GanItems record);

    int insertSelective(GanItems record);

    GanItems selectByPrimaryKey(Long goodsId);

    GanItems selectByCategoryIdAndName(@Param("goodsName") String goodsName, @Param("goodsCategoryId") Long goodsCategoryId);

    int updateByPrimaryKeySelective(GanItems record);

    int updateByPrimaryKeyWithBLOBs(GanItems record);

    int updateByPrimaryKey(GanItems record);

    List<GanItems> findGanItemsList(PageQueryUtil pageUtil);

    int getTotalGanItems(PageQueryUtil pageUtil);

    List<GanItems> selectByPrimaryKeys(List<Long> goodsIds);

    List<GanItems> findGanItemsListBySearch(PageQueryUtil pageUtil);

    int getTotalGanItemsBySearch(PageQueryUtil pageUtil);

    int batchInsert(@Param("newBeeMallGoodsList") List<GanItems> newBeeMallGoodsList);

    int updateStockNum(@Param("stockNumDTOS") List<StockNumDTO> stockNumDTOS);

    int batchUpdateSellStatus(@Param("orderIds")Long[] orderIds,@Param("sellStatus") int sellStatus);

}