package com.wlx.middleware.mybatis.reflection.invoker;

/**
 * 方法的反射调用
 */
public interface Invoker {

    Object invoke(Object target, Object[] args) throws Exception;

    Class<?> getType();
}
