package com.wlx.middleware.mybatis.executor.statement;

import com.wlx.middleware.mybatis.executor.Executor;
import com.wlx.middleware.mybatis.executor.keygen.KeyGenerator;
import com.wlx.middleware.mybatis.mapping.BoundSql;
import com.wlx.middleware.mybatis.mapping.MappedStatement;
import com.wlx.middleware.mybatis.session.Configuration;
import com.wlx.middleware.mybatis.session.ResultHandler;
import com.wlx.middleware.mybatis.session.RowBounds;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class PreparedStatementHandler extends BaseStatementHandler {

    public PreparedStatementHandler(MappedStatement mappedStatement, ResultHandler resultHandler, RowBounds rowBounds, BoundSql boundSql,
                                    Executor executor, Configuration configuration, Object parameterObject) {
        super(mappedStatement, resultHandler, rowBounds, boundSql, executor, configuration, parameterObject);
    }

    @Override
    protected Statement instantiateStatement(Connection connection) throws SQLException {
        return connection.prepareStatement(boundSql.getSql());
    }

    @Override
    public <E> List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException {
        PreparedStatement preparedStatement = (PreparedStatement) statement;
        preparedStatement.execute();
        return resultSetHandler.<E> handleResultSets(preparedStatement);
    }

    @Override
    public int update(Statement statement) throws SQLException {
        // 1.执行insert/delete/update
        PreparedStatement preparedStatement = (PreparedStatement) statement;
        preparedStatement.execute();
        int rows = preparedStatement.getUpdateCount();

        // 2.执行selectKey语句
        Object parameterObject = boundSql.getParameterObject();
        KeyGenerator keyGenerator = mappedStatement.getKeyGenerator();
        keyGenerator.processAfter(executor, mappedStatement, statement, parameterObject);
        return rows;
    }

    @Override
    public void parameterize(Statement statement) throws SQLException {
        parameterHandler.setParameters((PreparedStatement) statement);
    }
}
