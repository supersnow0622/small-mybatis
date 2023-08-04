package com.wlx.middleware.mybatis.binding;

import com.wlx.middleware.mybatis.session.SqlSession;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 映射器代理类
 * @param <T>
 */
public class MapperProxy<T> implements InvocationHandler, Serializable {

    private SqlSession sqlSession;

    private Class<T> mapperInterface;

    private Map<Method, MapperMethod> methodCache = new HashMap<>();

    public MapperProxy(SqlSession sqlSession, Class<T> mapperInterface) {
        this.sqlSession = sqlSession;
        this.mapperInterface = mapperInterface;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 如果是Object的toString(),equals(),hashcode()方法，则不需要代理
        if (Object.class.equals(method.getDeclaringClass())) {
            return method.invoke(this, args);
        }

        // 获取方法的具体执行器
        MapperMethod mapperMethod = cacheMapperMethod(method);
        return mapperMethod.execute(sqlSession, args);
    }

    private MapperMethod cacheMapperMethod(Method method) {
        if (!methodCache.containsKey(method)) {
            methodCache.put(method, new MapperMethod(sqlSession.getConfiguration(), mapperInterface, method));
        }
        return methodCache.get(method);
    }
}
