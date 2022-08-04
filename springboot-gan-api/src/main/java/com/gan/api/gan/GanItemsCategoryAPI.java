
package com.gan.api.gan;

import com.gan.common.GanException;
import com.gan.service.GanCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import com.gan.api.gan.vo.GanIndexCategoryVO;
import com.gan.common.ServiceResultEnum;
import com.gan.util.Result;
import com.gan.util.ResultGenerator;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@Api(value = "v1", tags = "3.分类页面接口")
@RequestMapping("/api/v1")
public class GanItemsCategoryAPI {

    @Resource
    private GanCategoryService ganCategoryService;

    @GetMapping("/categories")
    @ApiOperation(value = "获取分类数据", notes = "分类页面使用")
    public Result<List<GanIndexCategoryVO>> getCategories() {
        List<GanIndexCategoryVO> categories = ganCategoryService.getCategoriesForIndex();
        if (CollectionUtils.isEmpty(categories)) {
            GanException.fail(ServiceResultEnum.DATA_NOT_EXIST.getResult());
        }
        return ResultGenerator.genSuccessResult(categories);
    }
}
