package com.wlx.middleware.mybatis.executor.statement;

import com.wlx.middleware.mybatis.executor.Executor;
import com.wlx.middleware.mybatis.executor.parameter.ParameterHandler;
import com.wlx.middleware.mybatis.executor.resultset.ResultSetHandler;
import com.wlx.middleware.mybatis.mapping.BoundSql;
import com.wlx.middleware.mybatis.mapping.MappedStatement;
import com.wlx.middleware.mybatis.session.Configuration;
import com.wlx.middleware.mybatis.session.ResultHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class BaseStatementHandler implements StatementHandler {

    protected MappedStatement mappedStatement;

    protected ResultSetHandler resultSetHandler;

    protected BoundSql boundSql;

    protected Executor executor;

    protected Configuration configuration;

    protected Object parameterObject;

    protected final ParameterHandler parameterHandler;

    public BaseStatementHandler(MappedStatement mappedStatement, ResultHandler resultHandler,
                                BoundSql boundSql, Executor executor, Configuration configuration, Object parameterObject) {
        this.mappedStatement = mappedStatement;
        this.resultSetHandler = configuration.newResultSetHandler(executor, mappedStatement, boundSql);
        this.boundSql = boundSql;
        this.executor = executor;
        this.configuration = configuration;
        this.parameterObject = parameterObject;
        this.parameterHandler = configuration.newParameterHandler(mappedStatement, parameterObject, boundSql);
    }

    @Override
    public Statement prepare(Connection connection) {
        Statement statement = null;
        try {
            statement = instantiateStatement(connection);
            statement.setQueryTimeout(350);
            statement.setFetchSize(10000);
            return statement;
        } catch (Exception e) {
            throw new RuntimeException("Error preparing statement.  Cause: " + e, e);
        }
    }

    protected abstract Statement instantiateStatement(Connection connection) throws SQLException;

}
