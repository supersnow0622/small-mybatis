package com.wlx.middleware.mybatis.executor.statement;

import com.wlx.middleware.mybatis.executor.Executor;
import com.wlx.middleware.mybatis.executor.resultset.ResultSetHandler;
import com.wlx.middleware.mybatis.mapping.BoundSql;
import com.wlx.middleware.mybatis.mapping.MappedStatement;
import com.wlx.middleware.mybatis.session.Configuration;
import com.wlx.middleware.mybatis.session.ResultHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * 简单语句处理
 */
public class SimpleStatementHandler extends BaseStatementHandler {

    public SimpleStatementHandler(MappedStatement mappedStatement, ResultHandler resultHandler, BoundSql boundSql, Executor executor, Configuration configuration, Object parameterObject) {
        super(mappedStatement, resultHandler, boundSql, executor, configuration, parameterObject);
    }

    @Override
    protected Statement instantiateStatement(Connection connection) throws SQLException {
        return connection.createStatement();
    }

    @Override
    public <E> List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException {
        statement.execute(boundSql.getSql());
        return resultSetHandler.handleResultSets(statement);
    }

    @Override
    public void parameterize(Statement statement) {

    }
}
