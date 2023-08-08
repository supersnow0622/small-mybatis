package com.wlx.middleware.mybatis.session.defaults;

import com.wlx.middleware.mybatis.executor.Executor;
import com.wlx.middleware.mybatis.mapping.Environment;
import com.wlx.middleware.mybatis.session.Configuration;
import com.wlx.middleware.mybatis.session.SqlSession;
import com.wlx.middleware.mybatis.session.SqlSessionFactory;
import com.wlx.middleware.mybatis.session.TransactionIsolationLevel;
import com.wlx.middleware.mybatis.transaction.Transaction;
import com.wlx.middleware.mybatis.transaction.TransactionFactory;

import java.sql.SQLException;

public class DefaultSqlSessionFactory implements SqlSessionFactory {

    private Configuration configuration;

    public DefaultSqlSessionFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public SqlSession openSession() {
        Transaction tx = null;
        try {
            Environment environment = configuration.getEnvironment();
            TransactionFactory transactionFactory = environment.getTransactionFactory();
            tx = transactionFactory.newTransaction(environment.getDataSource(),
                    TransactionIsolationLevel.READ_COMMITTED, false);
            Executor executor = configuration.newExecutor(tx);
            return new DefaultSqlSession(configuration, executor);
        } catch (Exception e) {
            try {
                assert tx != null;
                tx.close();
            } catch (SQLException ignore) {
            }
            throw new RuntimeException("Error opening session.  Cause: " + e);
        }
    }
}
