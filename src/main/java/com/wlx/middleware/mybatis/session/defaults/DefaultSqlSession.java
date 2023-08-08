package com.wlx.middleware.mybatis.session.defaults;

import com.wlx.middleware.mybatis.executor.Executor;
import com.wlx.middleware.mybatis.mapping.MappedStatement;
import com.wlx.middleware.mybatis.session.Configuration;
import com.wlx.middleware.mybatis.session.SqlSession;

import java.util.List;

public class DefaultSqlSession implements SqlSession {

    private Configuration configuration;

    private Executor executor;

    public DefaultSqlSession(Configuration configuration, Executor executor) {
        this.configuration = configuration;
        this.executor = executor;
    }

    @Override
    public <T> T selectOne(String statement) {
        return this.selectOne(statement, null);
    }

    @Override
    public <T> T selectOne(String statement, Object parameter) {
        try {
            MappedStatement mappedStatement = configuration.getMappedStatement(statement);
            List<T> list = executor.query(mappedStatement, parameter, Executor.NO_RESULT_HANDLER, mappedStatement.getBoundSql());
            return list.get(0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public <T> T getMapper(Class<T> type) {
        return configuration.getMapperRegistry().getMapper(type, this);
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

}
