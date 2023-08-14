package com.wlx.middleware.mybatis.session;

/**
 * 结果上下文
 */
public interface ResultContext {

    /**
     * 获取结果
     */
    Object getResultObject();

    /**
     * 获取记录数
     */
    int getResultCount();

}
