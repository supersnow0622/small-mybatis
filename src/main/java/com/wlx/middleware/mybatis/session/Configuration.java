package com.wlx.middleware.mybatis.session;

import com.wlx.middleware.mybatis.binding.MapperRegistry;
import com.wlx.middleware.mybatis.datasource.druid.DruidDataSourceFactory;
import com.wlx.middleware.mybatis.mapping.Environment;
import com.wlx.middleware.mybatis.mapping.MappedStatement;
import com.wlx.middleware.mybatis.transaction.jdbc.JdbcTransactionFactory;
import com.wlx.middleware.mybatis.type.TypeAliasRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * 配置项，管理所有mapperStatement
 */
public class Configuration {

    private Environment environment;

    private Map<String, MappedStatement> mappedStatements = new HashMap<>();

    private MapperRegistry mapperRegistry = new MapperRegistry();

    private TypeAliasRegistry typeAliasRegistry = new TypeAliasRegistry();

    public Configuration() {
        typeAliasRegistry.registerAlias("DRUID", DruidDataSourceFactory.class);
        typeAliasRegistry.registerAlias("JDBC", JdbcTransactionFactory.class);
    }

    public MappedStatement getMappedStatement(String name) {
        return mappedStatements.get(name);
    }

    public MapperRegistry getMapperRegistry() {
        return mapperRegistry;
    }

    public TypeAliasRegistry getTypeAliasRegistry() {
        return typeAliasRegistry;
    }

    public void addMappedStatement(MappedStatement mappedStatement) {
        mappedStatements.put(mappedStatement.getId(), mappedStatement);
    }

    public <T> void addMapper(Class<T> clazz) {
        mapperRegistry.addMapper(clazz);
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
