package com.wlx.middleware.mybatis.cache.decorators;

import com.wlx.middleware.mybatis.cache.Cache;

import java.util.Deque;
import java.util.LinkedList;

/**
 * FIFO 缓存
 */
public class FifoCache  implements Cache {

    private final Cache delegate;

    private Deque<Object> keyList;
    private int size;

    public FifoCache(Cache delegate) {
        this.delegate = delegate;
        this.keyList = new LinkedList<>();
        this.size = 1024;
    }

    @Override
    public String getId() {
        return this.delegate.getId();
    }

    @Override
    public void putObject(Object key, Object value) {
        cycleKeyList(key);
        delegate.putObject(key, value);
    }

    @Override
    public Object getObject(Object key) {
        return delegate.getObject(key);
    }

    @Override
    public Object removeObject(Object key) {
        return delegate.removeObject(key);
    }

    @Override
    public void clear() {
        delegate.clear();
        keyList.clear();
    }

    @Override
    public int getSize() {
        return  delegate.getSize();
    }

    private void cycleKeyList(Object key) {
        keyList.add(key);
        if (keyList.size() > size) {
            Object cacheKey = keyList.removeFirst();
            this.delegate.removeObject(cacheKey);
        }
    }
}
