package com.wlx.middleware.mybatis.executor.keygen;

import com.wlx.middleware.mybatis.executor.Executor;
import com.wlx.middleware.mybatis.mapping.MappedStatement;

import java.sql.Statement;

/**
 * 不用键值生成器
 */
public class NoKeyGenerator implements KeyGenerator{

    @Override
    public void processBefore(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
        // Do Nothing
    }

    @Override
    public void processAfter(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
        // Do Nothing
    }

}