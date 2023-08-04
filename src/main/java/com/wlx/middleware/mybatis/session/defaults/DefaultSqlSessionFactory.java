package com.wlx.middleware.mybatis.session.defaults;

import com.wlx.middleware.mybatis.session.Configuration;
import com.wlx.middleware.mybatis.session.SqlSession;
import com.wlx.middleware.mybatis.session.SqlSessionFactory;

public class DefaultSqlSessionFactory implements SqlSessionFactory {

    private Configuration configuration;

    public DefaultSqlSessionFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public SqlSession openSession() {
        return new DefaultSqlSession(configuration);
    }
}
