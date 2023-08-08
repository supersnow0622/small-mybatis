package com.wlx.middleware.mybatis.executor;

import com.wlx.middleware.mybatis.executor.statement.StatementHandler;
import com.wlx.middleware.mybatis.mapping.BoundSql;
import com.wlx.middleware.mybatis.mapping.MappedStatement;
import com.wlx.middleware.mybatis.session.Configuration;
import com.wlx.middleware.mybatis.session.ResultHandler;
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
    protected <E> List<E> doQuery(MappedStatement ms, Object parameter, ResultHandler resultHandler,
                                  BoundSql boundSql) {
        try {
            Configuration configuration = ms.getConfiguration();
            StatementHandler statementHandler = configuration.newStatementHandler(ms, resultHandler, boundSql,
                    this, configuration, parameter);
            Connection connection = transaction.getConnection();
            Statement statement = statementHandler.prepare(connection);
            statementHandler.parameterize(statement);
            return statementHandler.query(statement, resultHandler);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
