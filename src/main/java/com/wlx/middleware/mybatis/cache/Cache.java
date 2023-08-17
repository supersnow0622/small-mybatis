package com.wlx.middleware.mybatis.cache;

/**
 * 缓存接口
 */
public interface Cache {

    // 获取Id，每个缓存都有唯一ID标识
    String getId();

    // 存入值
    void putObject(Object key, Object value);

    // 获取值
    Object getObject(Object key);

    // 删除值
    Object removeObject(Object key);

    // 清空
    void clear();

    // 获取缓存大小
    int getSize();

}
