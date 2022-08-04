
package com.gan.service;

import com.gan.api.gan.vo.GanIndexConfigItemsVO;
import com.gan.entity.IndexConfig;
import com.gan.util.PageQueryUtil;
import com.gan.util.PageResult;

import java.util.List;


public interface GanIndexConfigService {

    /**
     * 返回固定数量的首页配置商品对象(首页调用)
     *
     * @param number
     * @return
     */
    List<GanIndexConfigItemsVO> getConfigItemsesForIndex(int configType, int number);

    /**
     * 后台分页
     *
     * @param pageUtil
     * @return
     */
    PageResult getConfigsPage(PageQueryUtil pageUtil);

    String saveIndexConfig(IndexConfig indexConfig);

    String updateIndexConfig(IndexConfig indexConfig);

    IndexConfig getIndexConfigById(Long id);

    Boolean deleteBatch(Long[] ids);
}
