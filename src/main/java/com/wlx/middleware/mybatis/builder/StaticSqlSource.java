package com.wlx.middleware.mybatis.builder;

import com.wlx.middleware.mybatis.mapping.BoundSql;
import com.wlx.middleware.mybatis.mapping.ParameterMapping;
import com.wlx.middleware.mybatis.mapping.SqlSource;
import com.wlx.middleware.mybatis.session.Configuration;

import java.util.List;

/**
 * 静态SQL源码，即不含动态参数的SQL，如：select * from user where id=？
 */
public class StaticSqlSource implements SqlSource {
    private Configuration configuration;

    private String sql;

    private List<ParameterMapping> parameterMappings;

    public StaticSqlSource(Configuration configuration, String sql, List<ParameterMapping> parameterMappings) {
        this.configuration = configuration;
        this.sql = sql;
        this.parameterMappings = parameterMappings;
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        return new BoundSql(configuration, sql, parameterMappings, parameterObject);
    }
}
