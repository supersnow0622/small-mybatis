package com.wlx.middleware.mybatis.executor.statement;

import com.wlx.middleware.mybatis.executor.Executor;
import com.wlx.middleware.mybatis.executor.resultset.ResultSetHandler;
import com.wlx.middleware.mybatis.mapping.BoundSql;
import com.wlx.middleware.mybatis.mapping.MappedStatement;
import com.wlx.middleware.mybatis.session.Configuration;
import com.wlx.middleware.mybatis.session.ResultHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class PreparedStatementHandler extends BaseStatementHandler {

    public PreparedStatementHandler(MappedStatement mappedStatement, ResultHandler resultHandler, BoundSql boundSql,
                                    Executor executor, Configuration configuration, Object parameterObject) {
        super(mappedStatement, resultHandler, boundSql, executor, configuration, parameterObject);
    }

    @Override
    protected Statement instantiateStatement(Connection connection) throws SQLException {
        return connection.prepareStatement(boundSql.getSql());
    }

    @Override
    public <E> List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException {
        PreparedStatement preparedStatement = (PreparedStatement) statement;
        preparedStatement.execute();
        return resultSetHandler.handleResultSets(preparedStatement);
    }

    @Override
    public void parameterize(Statement statement) throws SQLException {
        PreparedStatement preparedStatement = (PreparedStatement) statement;
        preparedStatement.setLong(1, Long.parseLong(((Object[]) parameterObject)[0].toString()));
    }
}
