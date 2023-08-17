package com.wlx.middleware.mybatis.mapping;

import com.wlx.middleware.mybatis.cache.Cache;
import com.wlx.middleware.mybatis.cache.decorators.FifoCache;
import com.wlx.middleware.mybatis.cache.impl.PerpetualCache;
import com.wlx.middleware.mybatis.reflection.MetaObject;
import com.wlx.middleware.mybatis.reflection.SystemMetaObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 缓存构建器
 */
public class CacheBuilder {

    private String id;
    private Class<? extends Cache> implementation;
    private List<Class<? extends Cache>> decorators;
    private Integer size;
    private Long clearInterval;
    private boolean readWrite;
    private Properties properties;
    private boolean blocking;

    public CacheBuilder(String id) {
        this.id = id;
        this.decorators = new ArrayList<>();
    }

    public CacheBuilder implementation(Class<? extends Cache> implementation) {
        this.implementation = implementation;
        return this;
    }

    public CacheBuilder addDecorator(Class<? extends Cache> decorator) {
        if (decorator != null) {
            this.decorators.add(decorator);
        }
        return this;
    }

    public CacheBuilder size(Integer size) {
        this.size = size;
        return this;
    }

    public CacheBuilder clearInterval(Long clearInterval) {
        this.clearInterval = clearInterval;
        return this;
    }

    public CacheBuilder readWrite(boolean readWrite) {
        this.readWrite = readWrite;
        return this;
    }

    public CacheBuilder blocking(boolean blocking) {
        this.blocking = blocking;
        return this;
    }

    public CacheBuilder properties(Properties properties) {
        this.properties = properties;
        return this;
    }

    // 先创建一级缓存，然后对一级缓存进行包装，构造出二级缓存
    public Cache build() {
        setDefaultImplementations();
        Cache cache = newBaseCacheInstance(implementation, id);
        setCacheProperties(cache);
        if (PerpetualCache.class.equals(cache.getClass())) {
            for (Class<? extends Cache> decorator : decorators) {
                cache = newCacheDecoratorInstance(decorator, cache);
                setCacheProperties(cache);
            }
        }
        return cache;
    }

    private void setDefaultImplementations() {
        if (implementation == null) {
            implementation = PerpetualCache.class;
            if (decorators.isEmpty()) {
                decorators.add(FifoCache.class);
            }
        }
    }

    // 创建一级缓存
    private Cache newBaseCacheInstance(Class<? extends Cache> implementation, String id) {
        Constructor<? extends Cache> cacheConstructor;
        try {
            cacheConstructor = implementation.getConstructor(String.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Invalid base cache implementation (" + implementation + ").  " +
                    "Base cache implementations must have a constructor that takes a String id as a parameter.  Cause: " + e, e);
        }
        try {
            return cacheConstructor.newInstance(id);
        } catch (Exception e) {
            throw new RuntimeException("Could not instantiate cache implementation (" + implementation + "). Cause: " + e, e);
        }
    }

    // 创建二级缓存，建立在一级缓存之上，是对一级缓存的包装
    private Cache newCacheDecoratorInstance(Class<? extends Cache> decorator, Cache cache) {
        Constructor<? extends Cache> cacheConstructor;
        try {
            cacheConstructor = decorator.getConstructor(Cache.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Invalid cache decorator (" + decorator + ").  " +
                    "Cache decorators must have a constructor that takes a Cache instance as a parameter.  Cause: " + e, e);
        }
        try {
            return cacheConstructor.newInstance(cache);
        } catch (Exception e) {
            throw new RuntimeException("Could not instantiate cache decorator (" + decorator + "). Cause: " + e, e);
        }
    }

    private void setCacheProperties(Cache cache) {
        if (properties.isEmpty()) {
            return;
        }
        MetaObject metaCache = SystemMetaObject.forObject(cache);
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String name = (String) entry.getKey();
            String value = (String) entry.getValue();
            if (!metaCache.hasSetter(name)) {
                continue;
            }
            Class<?> type = metaCache.getSetterType(name);
            if (String.class == type) {
                metaCache.setValue(name, value);
            } else if (int.class == type
                    || Integer.class == type) {
                metaCache.setValue(name, Integer.valueOf(value));
            } else if (long.class == type
                    || Long.class == type) {
                metaCache.setValue(name, Long.valueOf(value));
            } else if (short.class == type
                    || Short.class == type) {
                metaCache.setValue(name, Short.valueOf(value));
            } else if (byte.class == type
                    || Byte.class == type) {
                metaCache.setValue(name, Byte.valueOf(value));
            } else if (float.class == type
                    || Float.class == type) {
                metaCache.setValue(name, Float.valueOf(value));
            } else if (boolean.class == type
                    || Boolean.class == type) {
                metaCache.setValue(name, Boolean.valueOf(value));
            } else if (double.class == type
                    || Double.class == type) {
                metaCache.setValue(name, Double.valueOf(value));
            } else {
                throw new RuntimeException("Unsupported property type for cache: '" + name + "' of type " + type);
            }
        }
    }


}
