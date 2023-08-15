package com.wlx.middleware.mybatis.executor.keygen;

import com.wlx.middleware.mybatis.executor.Executor;
import com.wlx.middleware.mybatis.mapping.MappedStatement;

import java.sql.Statement;

/**
 * 使用 JDBC3 Statement.getGeneratedKeys
 */
public class Jdbc3KeyGenerator implements KeyGenerator {
    @Override
    public void processBefore(Executor executor, MappedStatement mappedStatement, Statement statement, Object parameter) {

    }

    @Override
    public void processAfter(Executor executor, MappedStatement mappedStatement, Statement statement, Object parameter) {

    }
}
