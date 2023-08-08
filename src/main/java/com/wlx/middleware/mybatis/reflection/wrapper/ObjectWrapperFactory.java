package com.wlx.middleware.mybatis.reflection.wrapper;

import com.wlx.middleware.mybatis.reflection.MetaObject;

/**
 * 对象包装工厂
 */
public interface ObjectWrapperFactory {

    // 判断有没有包装器
    boolean hasWrapperFor(Object object);

    // 得到包装器
    ObjectWrapper getWrapperFor(MetaObject metaObject, Object object);

}
