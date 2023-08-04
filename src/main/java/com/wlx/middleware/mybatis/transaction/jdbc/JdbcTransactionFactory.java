package com.wlx.middleware.mybatis.transaction.jdbc;

import com.wlx.middleware.mybatis.session.TransactionIsolationLevel;
import com.wlx.middleware.mybatis.transaction.Transaction;
import com.wlx.middleware.mybatis.transaction.TransactionFactory;

import javax.sql.DataSource;
import java.sql.Connection;

public class JdbcTransactionFactory implements TransactionFactory {
    @Override
    public Transaction newTransaction(Connection connection) {
        return new JdbcTransaction(connection);
    }

    @Override
    public Transaction newTransaction(DataSource dataSource, TransactionIsolationLevel level, boolean isAutoCommit) {
        return new JdbcTransaction(dataSource, level, isAutoCommit);
    }
}
