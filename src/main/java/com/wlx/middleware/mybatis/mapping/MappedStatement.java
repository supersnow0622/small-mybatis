package com.wlx.middleware.mybatis.mapping;

import com.wlx.middleware.mybatis.executor.keygen.Jdbc3KeyGenerator;
import com.wlx.middleware.mybatis.executor.keygen.KeyGenerator;
import com.wlx.middleware.mybatis.executor.keygen.NoKeyGenerator;
import com.wlx.middleware.mybatis.executor.keygen.SelectKeyGenerator;
import com.wlx.middleware.mybatis.scripting.LanguageDriver;
import com.wlx.middleware.mybatis.session.Configuration;

import java.util.List;

public class MappedStatement {

    private String resource;
    private Configuration configuration;
    private String id;
    private SqlCommandType sqlCommandType;
    private SqlSource sqlSource;
    Class<?> resultType;
    private LanguageDriver languageDriver;
    private List<ResultMap> resultMaps;
    private KeyGenerator keyGenerator;
    private String[] keyProperties;
    private String[] keyColumns;

    private boolean flushCacheRequired;

    public MappedStatement() {
    }

    public boolean isFlushCacheRequired() {
        return flushCacheRequired;
    }

    public static class Builder {

        private MappedStatement mappedStatement = new MappedStatement();

        public Builder(Configuration configuration, String id, SqlCommandType sqlCommandType, SqlSource sqlSource, Class<?> resultType) {
            mappedStatement.configuration = configuration;
            mappedStatement.id = id;
            mappedStatement.sqlCommandType = sqlCommandType;
            mappedStatement.sqlSource = sqlSource;
            mappedStatement.resultType = resultType;
            mappedStatement.keyGenerator = configuration.isUseGeneratedKeys() && SqlCommandType.SELECT.equals(sqlCommandType)
                    ? new Jdbc3KeyGenerator() : new NoKeyGenerator();
            mappedStatement.languageDriver = configuration.getDefaultScriptingLanguageInstance();
        }

        public MappedStatement build() {
            return mappedStatement;
        }

        public Builder resource(String resource) {
            mappedStatement.resource = resource;
            return this;
        }

        public String id() {
            return mappedStatement.id;
        }

        public Builder resultMaps(List<ResultMap> resultMaps) {
            mappedStatement.resultMaps = resultMaps;
            return this;
        }

        public Builder keyGenerator(KeyGenerator keyGenerator) {
            mappedStatement.keyGenerator = keyGenerator;
            return this;
        }

        public Builder keyProperty(String keyProperty) {
            mappedStatement.keyProperties = delimitedStringToArray(keyProperty);
            return this;
        }
    }

    private static String[] delimitedStringToArray(String in) {
        if (in == null || in.trim().length() == 0) {
            return null;
        } else {
            return in.split(",");
        }
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public String getId() {
        return id;
    }

    public SqlCommandType getSqlCommandType() {
        return sqlCommandType;
    }

    public SqlSource getSqlSource() {
        return sqlSource;
    }

    public Class<?> getResultType() {
        return resultType;
    }

    public LanguageDriver getLanguageDriver() {
        return languageDriver;
    }

    public List<ResultMap> getResultMaps() {
        return resultMaps;
    }

    public String getResource() {
        return resource;
    }

    public KeyGenerator getKeyGenerator() {
        return keyGenerator;
    }

    public String[] getKeyProperties() {
        return keyProperties;
    }

    public String[] getKeyColumns() {
        return keyColumns;
    }
}
