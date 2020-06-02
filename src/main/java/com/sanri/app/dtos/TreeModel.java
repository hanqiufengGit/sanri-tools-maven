package com.sanri.app.dtos;

import java.util.List;

public interface TreeModel<T> {
    /**
     * 获取编号
     * @return
     */
    String getId();

    /**
     * 父级编号
     * @return
     */
    String getParentId();

    /**
     * 获取文本
     * @return
     */
    String getLabel();

    /**
     * 获取当前原始对象
     * @return
     */
    T getOrigin();

    List<? extends TreeModel> getChildrens();

    void setChildrens(List<? extends TreeModel> data);
}
