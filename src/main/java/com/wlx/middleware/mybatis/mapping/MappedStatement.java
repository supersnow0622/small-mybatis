package com.wlx.middleware.mybatis.mapping;

import com.wlx.middleware.mybatis.session.Configuration;

public class MappedStatement {

    private Configuration configuration;

    private String id;
    private SqlCommandType sqlCommandType;

    private BoundSql boundSql;

    public MappedStatement() {
    }

    public static class Builder {

        private MappedStatement mappedStatement = new MappedStatement();

        public Builder(Configuration configuration, String id, BoundSql boundSql, SqlCommandType sqlCommandType) {
            mappedStatement.configuration = configuration;
            mappedStatement.id = id;
            mappedStatement.boundSql = boundSql;
            mappedStatement.sqlCommandType = sqlCommandType;
        }

        public MappedStatement build() {
            return mappedStatement;
        }
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public String getId() {
        return id;
    }

    public BoundSql getBoundSql() {
        return boundSql;
    }

    public SqlCommandType getSqlCommandType() {
        return sqlCommandType;
    }

}
