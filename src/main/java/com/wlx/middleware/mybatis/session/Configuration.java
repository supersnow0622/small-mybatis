package com.wlx.middleware.mybatis.session;

import com.wlx.middleware.mybatis.binding.MapperRegistry;
import com.wlx.middleware.mybatis.datasource.druid.DruidDataSourceFactory;
import com.wlx.middleware.mybatis.datasource.pooled.PooledDataSourceFactory;
import com.wlx.middleware.mybatis.datasource.unpooled.UnpooledDataSourceFactory;
import com.wlx.middleware.mybatis.executor.Executor;
import com.wlx.middleware.mybatis.executor.SimpleExecutor;
import com.wlx.middleware.mybatis.executor.resultset.DefaultResultSetHandler;
import com.wlx.middleware.mybatis.executor.resultset.ResultSetHandler;
import com.wlx.middleware.mybatis.executor.statement.PreparedStatementHandler;
import com.wlx.middleware.mybatis.executor.statement.StatementHandler;
import com.wlx.middleware.mybatis.mapping.BoundSql;
import com.wlx.middleware.mybatis.mapping.Environment;
import com.wlx.middleware.mybatis.mapping.MappedStatement;
import com.wlx.middleware.mybatis.reflection.MetaObject;
import com.wlx.middleware.mybatis.reflection.factory.DefaultObjectFactory;
import com.wlx.middleware.mybatis.reflection.factory.ObjectFactory;
import com.wlx.middleware.mybatis.reflection.wrapper.DefaultObjectWrapperFactory;
import com.wlx.middleware.mybatis.reflection.wrapper.ObjectWrapperFactory;
import com.wlx.middleware.mybatis.scripting.LanguageDriverRegistry;
import com.wlx.middleware.mybatis.scripting.xmltags.XMLLanguageDriver;
import com.wlx.middleware.mybatis.transaction.Transaction;
import com.wlx.middleware.mybatis.transaction.jdbc.JdbcTransactionFactory;
import com.wlx.middleware.mybatis.type.TypeAliasRegistry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 配置项，管理所有mapperStatement
 */
public class Configuration {

    private Environment environment;

    // 映射的语句，key为namespace+id
    private Map<String, MappedStatement> mappedStatements = new HashMap<>();

    // 映射注册机
    private MapperRegistry mapperRegistry = new MapperRegistry();

    // 类型别名注册机
    private TypeAliasRegistry typeAliasRegistry = new TypeAliasRegistry();

    private ObjectFactory objectFactory = new DefaultObjectFactory();

    private ObjectWrapperFactory objectWrapperFactory = new DefaultObjectWrapperFactory();

    protected String databaseId;

    protected final Set<String> loadedResource = new HashSet<>();

    protected final LanguageDriverRegistry languageRegistry = new LanguageDriverRegistry();

    public Configuration() {
        typeAliasRegistry.registerAlias("DRUID", DruidDataSourceFactory.class);
        typeAliasRegistry.registerAlias("JDBC", JdbcTransactionFactory.class);
        typeAliasRegistry.registerAlias("POOLED", PooledDataSourceFactory.class);
        typeAliasRegistry.registerAlias("UNPOOLED", UnpooledDataSourceFactory.class);

        languageRegistry.setDefaultDriverClass(XMLLanguageDriver.class);
    }

    public Executor newExecutor(Transaction transaction) {
        return new SimpleExecutor(this, transaction);
    }

    public StatementHandler newStatementHandler(MappedStatement mappedStatement, ResultHandler resultHandler,
                                                BoundSql boundSql, Executor executor, Configuration configuration,
                                                Object parameterObject) {
        return new PreparedStatementHandler(mappedStatement, resultHandler, boundSql, executor, configuration, parameterObject);
    }

    public ResultSetHandler newResultSetHandler(Executor executor, MappedStatement mappedStatement, BoundSql boundSql) {
        return new DefaultResultSetHandler(executor, mappedStatement, boundSql);
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

    public MetaObject newMetaObject(Object object) {
        return MetaObject.forObject(object, objectFactory, objectWrapperFactory);
    }

    public String getDatabaseId() {
        return databaseId;
    }

    public boolean isResourceLoaded(String resource) {
        return loadedResource.contains(resource);
    }

    public void addLoadedResource(String resource) {
        loadedResource.add(resource);
    }

    public LanguageDriverRegistry getLanguageRegistry() {
        return languageRegistry;
    }
}
