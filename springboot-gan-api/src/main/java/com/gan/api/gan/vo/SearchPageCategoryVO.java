
package com.gan.api.gan.vo;

import com.gan.entity.ItemsCategory;
import java.io.Serializable;
import java.util.List;

/**
 * 搜索页面分类数据VO
 */
public class SearchPageCategoryVO implements Serializable {

    private String firstLevelCategoryName;

    private List<ItemsCategory> secondLevelCategoryList;

    private String secondLevelCategoryName;

    private List<ItemsCategory> thirdLevelCategoryList;

    private String currentCategoryName;

    public String getFirstLevelCategoryName() {
        return firstLevelCategoryName;
    }

    public void setFirstLevelCategoryName(String firstLevelCategoryName) {
        this.firstLevelCategoryName = firstLevelCategoryName;
    }

    public List<ItemsCategory> getSecondLevelCategoryList() {
        return secondLevelCategoryList;
    }

    public void setSecondLevelCategoryList(List<ItemsCategory> secondLevelCategoryList) {
        this.secondLevelCategoryList = secondLevelCategoryList;
    }

    public String getSecondLevelCategoryName() {
        return secondLevelCategoryName;
    }

    public void setSecondLevelCategoryName(String secondLevelCategoryName) {
        this.secondLevelCategoryName = secondLevelCategoryName;
    }

    public List<ItemsCategory> getThirdLevelCategoryList() {
        return thirdLevelCategoryList;
    }

    public void setThirdLevelCategoryList(List<ItemsCategory> thirdLevelCategoryList) {
        this.thirdLevelCategoryList = thirdLevelCategoryList;
    }

    public String getCurrentCategoryName() {
        return currentCategoryName;
    }

    public void setCurrentCategoryName(String currentCategoryName) {
        this.currentCategoryName = currentCategoryName;
    }
}
