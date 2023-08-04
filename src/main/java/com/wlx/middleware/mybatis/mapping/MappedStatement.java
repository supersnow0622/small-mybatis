package com.wlx.middleware.mybatis.mapping;

public class MappedStatement {

    private String id;
    private SqlCommandType sqlCommandType;

    private BoundSql boundSql;

    public static class Builder {

        private MappedStatement mappedStatement = new MappedStatement();

        public Builder(String id, BoundSql boundSql, SqlCommandType sqlCommandType) {
            mappedStatement.id = id;
            mappedStatement.boundSql = boundSql;
            mappedStatement.sqlCommandType = sqlCommandType;
        }

        public MappedStatement build() {
            return mappedStatement;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BoundSql getBoundSql() {
        return boundSql;
    }

    public void setBoundSql(BoundSql boundSql) {
        this.boundSql = boundSql;
    }

    public SqlCommandType getSqlCommandType() {
        return sqlCommandType;
    }

    public void setSqlCommandType(SqlCommandType sqlCommandType) {
        this.sqlCommandType = sqlCommandType;
    }
}
