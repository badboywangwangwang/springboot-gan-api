
package com.gan.service;

import com.gan.api.gan.vo.GanIndexCarouselVO;
import com.gan.entity.Carousel;
import com.gan.util.PageQueryUtil;
import com.gan.util.PageResult;

import java.util.List;

public interface GanCarouselService {

    /**
     * 返回固定数量的轮播图对象(首页调用)
     *
     * @param number
     * @return
     */
    List<GanIndexCarouselVO> getCarouselsForIndex(int number);

    /**
     * 后台分页
     *
     * @param pageUtil
     * @return
     */
    PageResult getCarouselPage(PageQueryUtil pageUtil);

    String saveCarousel(Carousel carousel);

    String updateCarousel(Carousel carousel);

    Carousel getCarouselById(Integer id);

    Boolean deleteBatch(Long[] ids);
}
