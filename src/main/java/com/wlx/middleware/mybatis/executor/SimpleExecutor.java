package com.wlx.middleware.mybatis.executor;

import com.wlx.middleware.mybatis.executor.statement.StatementHandler;
import com.wlx.middleware.mybatis.mapping.BoundSql;
import com.wlx.middleware.mybatis.mapping.MappedStatement;
import com.wlx.middleware.mybatis.session.Configuration;
import com.wlx.middleware.mybatis.session.ResultHandler;
import com.wlx.middleware.mybatis.session.RowBounds;
import com.wlx.middleware.mybatis.transaction.Transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class SimpleExecutor extends BaseExecutor {

    public SimpleExecutor(Configuration configuration, Transaction transaction) {
        super(configuration, transaction);
    }

    @Override
    protected int doUpdate(MappedStatement mappedStatement, Object parameter) throws SQLException {
        Statement statement = null;
        try {
            Configuration configuration = mappedStatement.getConfiguration();
            StatementHandler handler = configuration.newStatementHandler(mappedStatement, null, RowBounds.DEFAULT,
                    null, this, configuration, parameter);
            statement = prepareStatement(handler);
            return handler.update(statement);
        } finally {
            closeStatement(statement);
        }
    }

    @Override
    protected <E> List<E> doQuery(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler,
                                  BoundSql boundSql) {
        try {
            Configuration configuration = ms.getConfiguration();
            StatementHandler statementHandler = configuration.newStatementHandler(ms, resultHandler, rowBounds, boundSql,
                    this, configuration, parameter);
            Statement statement = prepareStatement(statementHandler);
            return statementHandler.query(statement, resultHandler);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Statement prepareStatement(StatementHandler statementHandler) throws SQLException {
        Connection connection = transaction.getConnection();
        Statement statement = statementHandler.prepare(connection);
        statementHandler.parameterize(statement);
        return statement;
    }
}
