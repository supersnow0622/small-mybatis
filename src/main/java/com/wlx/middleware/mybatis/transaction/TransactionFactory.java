package com.wlx.middleware.mybatis.transaction;

import com.wlx.middleware.mybatis.session.TransactionIsolationLevel;

import javax.sql.DataSource;
import java.sql.Connection;

public interface TransactionFactory {

    Transaction newTransaction(Connection connection);

    Transaction newTransaction(DataSource dataSource, TransactionIsolationLevel level, boolean isAutoCommit);
}
