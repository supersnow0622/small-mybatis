package com.wlx.middleware.mybatis.mapping;

import javax.sql.DataSource;

public class Environment {

    private final String id;

    private final DataSource dataSource;

    public Environment(String id, DataSource dataSource) {
        this.id = id;
        this.dataSource = dataSource;
    }

    public static class Builder {
        private String id;

        private DataSource dataSource;

        public Builder(String id) {
            this.id = id;
        }

        public Builder dataSource(DataSource dataSource) {
            this.dataSource = dataSource;
            return this;
        }

        public Environment build() {
            return new Environment(this.id, this.dataSource);
        }
    }

    public String getId() {
        return id;
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
