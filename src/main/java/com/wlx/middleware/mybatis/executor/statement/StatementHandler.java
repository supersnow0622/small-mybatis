package com.wlx.middleware.mybatis.executor.statement;

import com.wlx.middleware.mybatis.session.ResultHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * 语句处理器
 */
public interface StatementHandler {

    Statement prepare(Connection connection);

    <E> List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException;

    int update(Statement statement) throws SQLException;

    void parameterize(Statement statement) throws SQLException;
}
