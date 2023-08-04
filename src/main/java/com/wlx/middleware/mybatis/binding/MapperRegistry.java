package com.wlx.middleware.mybatis.binding;

import cn.hutool.core.lang.ClassScanner;
import com.wlx.middleware.mybatis.session.SqlSession;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 映射器注册机，管理所有映射器代理类的工厂
 */
public class MapperRegistry {

    private Map<Class<?>, MapperProxyFactory<?>> knownMappers = new HashMap<>();

    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) knownMappers.get(type);
        if (mapperProxyFactory == null) {
            throw new RuntimeException("Type" + type + "is not known to the MapperRegistry");
        }
        return (T) mapperProxyFactory.newInstance(sqlSession);
    }

    public <T> void addMapper(Class<T> type) {
        if (!type.isInterface()) {
            return;
        }
        if (hasMapper(type)) {
            throw new RuntimeException("Type " + type + " is already known to the MapperRegistry.");
        }
        knownMappers.put(type, new MapperProxyFactory<T>(type));
    }

    public <T> boolean hasMapper(Class<T> type) {
        return knownMappers.containsKey(type);
    }

    public void addMappers(String packageName) {
        Set<Class<?>> classes = ClassScanner.scanPackage(packageName);
        for (Class<?> clazz : classes) {
            addMapper(clazz);
        }
    }
}
