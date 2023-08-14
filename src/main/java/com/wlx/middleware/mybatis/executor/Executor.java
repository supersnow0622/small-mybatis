package com.wlx.middleware.mybatis.executor;

import com.wlx.middleware.mybatis.mapping.BoundSql;
import com.wlx.middleware.mybatis.mapping.MappedStatement;
import com.wlx.middleware.mybatis.session.ResultHandler;
import com.wlx.middleware.mybatis.session.RowBounds;
import com.wlx.middleware.mybatis.transaction.Transaction;

import java.sql.SQLException;
import java.util.List;

public interface Executor {

    ResultHandler NO_RESULT_HANDLER = null;

    int update(MappedStatement mappedStatement, Object parameter) throws SQLException;

    <E> List<E> query(MappedStatement statement, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql);

    void rollback(boolean required) throws SQLException;

    void close(boolean required);

    void commit(boolean required) throws SQLException;

    Transaction getTransaction();

}
