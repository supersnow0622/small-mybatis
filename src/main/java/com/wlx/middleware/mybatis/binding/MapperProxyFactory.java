package com.wlx.middleware.mybatis.binding;

import com.wlx.middleware.mybatis.session.SqlSession;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * 映射器代理类生产的工厂
 * @param <T>
 */
public class MapperProxyFactory<T> {

    // 需要被代理的接口
    private Class<T> mapperInterface;

    public MapperProxyFactory(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    public T newInstance(SqlSession sqlSession) {
        InvocationHandler invocationHandler = new MapperProxy<>(sqlSession, mapperInterface);
        return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(),
                new Class[]{mapperInterface}, invocationHandler);
    }
}
