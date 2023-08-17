package com.wlx.middleware.mybatis.cache;

import com.wlx.middleware.mybatis.cache.decorators.TransactionCache;

import java.util.HashMap;
import java.util.Map;

/**
 * 事务缓存管理器
 */
public class TransactionalCacheManager {

    private Map<Cache, TransactionCache> transactionCaches = new HashMap<>();

    public void clear(Cache cache) {
        getTransactionCache(cache).clear();
    }

    public Object getObject(Cache cache, CacheKey key) {
        return getTransactionCache(cache).getObject(key);
    }

    public void putObject(Cache cache, CacheKey key, Object value) {
        getTransactionCache(cache).putObject(key, value);
    }

    public void commit() {
        for (TransactionCache transactionCache : transactionCaches.values()) {
            transactionCache.commit();
        }
    }

    public void rollback() {
        for (TransactionCache transactionCache : transactionCaches.values()) {
            transactionCache.rollback();
        }
    }

    private TransactionCache getTransactionCache(Cache cache) {
        TransactionCache transactionCache = transactionCaches.get(cache);
        if (transactionCache == null) {
            transactionCache = new TransactionCache(cache);
            transactionCaches.put(cache, transactionCache);
        }
        return transactionCache;
    }
}
