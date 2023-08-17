package com.wlx.middleware.mybatis.executor;

import com.alibaba.fastjson.JSON;
import com.wlx.middleware.mybatis.cache.Cache;
import com.wlx.middleware.mybatis.cache.CacheKey;
import com.wlx.middleware.mybatis.cache.TransactionalCacheManager;
import com.wlx.middleware.mybatis.mapping.BoundSql;
import com.wlx.middleware.mybatis.mapping.MappedStatement;
import com.wlx.middleware.mybatis.session.ResultHandler;
import com.wlx.middleware.mybatis.session.RowBounds;
import com.wlx.middleware.mybatis.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

/**
 * 二级缓存执行器
 */
public class CachingExecutor implements Executor {

    private Logger logger = LoggerFactory.getLogger(CachingExecutor.class);

    private Executor delegate;

    public TransactionalCacheManager  cacheManager = new TransactionalCacheManager();

    public CachingExecutor(Executor delegate) {
        this.delegate = delegate;
        delegate.setExecutorWrapper(this);
    }
    @Override
    public int update(MappedStatement mappedStatement, Object parameter) throws SQLException {
        return delegate.update(mappedStatement, parameter);
    }

    @Override
    public <E> List<E> query(MappedStatement statement, Object parameter, RowBounds rowBounds, ResultHandler resultHandler,
                             BoundSql boundSql, CacheKey cacheKey) {
        Cache cache = statement.getCache();
        if (cache != null) {
            flushCacheIfRequired(statement);
            if (statement.isUseCache() && resultHandler == null) {
                List<E> list = (List<E>) cacheManager.getObject(cache, cacheKey);
                if (list == null) {
                    list = delegate.<E>query(statement, parameter, rowBounds, resultHandler, boundSql, cacheKey);
                    cacheManager.putObject(cache, cacheKey, list);
                }
                if (logger.isDebugEnabled() && cache.getSize() > 0) {
                    logger.debug("二级缓存：{}", JSON.toJSONString(list));
                }
                return list;
            }
        }
        return delegate.<E>query(statement, parameter, rowBounds, resultHandler, boundSql, cacheKey);
    }

    @Override
    public <E> List<E> query(MappedStatement statement, Object parameter, RowBounds rowBounds, ResultHandler resultHandler) {
        BoundSql boundSql = statement.getSqlSource().getBoundSql(parameter);
        CacheKey key = createCacheKey(statement, parameter, rowBounds, boundSql);
        return query(statement, parameter,rowBounds, resultHandler, boundSql, key);
    }

    @Override
    public void rollback(boolean required) throws SQLException {
        try {
            delegate.rollback(required);
        } finally {
            if (required) {
                cacheManager.rollback();
            }
        }
    }

    @Override
    public void close(boolean required) {
        try {
            if (required) {
                cacheManager.rollback();
            } else {
                cacheManager.commit();
            }
        } finally {
            delegate.close(required);
        }
    }

    @Override
    public void commit(boolean required) throws SQLException {
        delegate.commit(required);
        cacheManager.commit();
    }

    @Override
    public Transaction getTransaction() {
        return delegate.getTransaction();
    }

    @Override
    public void clearLocalCache() {
        delegate.clearLocalCache();
    }

    @Override
    public CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql) {
        return delegate.createCacheKey(ms, parameterObject, rowBounds, boundSql);
    }

    @Override
    public void setExecutorWrapper(Executor executor) {
        throw new UnsupportedOperationException("This method should not be called");
    }

    private void flushCacheIfRequired(MappedStatement statement) {
        Cache cache = statement.getCache();
        if (cache != null && statement.isFlushCacheRequired()) {
            cacheManager.clear(cache);
        }
    }
}
