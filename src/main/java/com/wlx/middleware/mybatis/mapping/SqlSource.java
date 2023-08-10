package com.wlx.middleware.mybatis.mapping;

public interface SqlSource {

    BoundSql getBoundSql(Object parameterObject);
}
