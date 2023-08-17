package com.wlx.middleware.mybatis.session;

import com.wlx.middleware.mybatis.binding.MapperRegistry;
import com.wlx.middleware.mybatis.datasource.druid.DruidDataSourceFactory;
import com.wlx.middleware.mybatis.datasource.pooled.PooledDataSourceFactory;
import com.wlx.middleware.mybatis.datasource.unpooled.UnpooledDataSourceFactory;
import com.wlx.middleware.mybatis.executor.Executor;
import com.wlx.middleware.mybatis.executor.SimpleExecutor;
import com.wlx.middleware.mybatis.executor.keygen.KeyGenerator;
import com.wlx.middleware.mybatis.executor.parameter.ParameterHandler;
import com.wlx.middleware.mybatis.executor.resultset.DefaultResultSetHandler;
import com.wlx.middleware.mybatis.executor.resultset.ResultSetHandler;
import com.wlx.middleware.mybatis.executor.statement.PreparedStatementHandler;
import com.wlx.middleware.mybatis.executor.statement.StatementHandler;
import com.wlx.middleware.mybatis.mapping.BoundSql;
import com.wlx.middleware.mybatis.mapping.Environment;
import com.wlx.middleware.mybatis.mapping.MappedStatement;
import com.wlx.middleware.mybatis.mapping.ResultMap;
import com.wlx.middleware.mybatis.plugin.Interceptor;
import com.wlx.middleware.mybatis.plugin.InterceptorChain;
import com.wlx.middleware.mybatis.reflection.MetaObject;
import com.wlx.middleware.mybatis.reflection.factory.DefaultObjectFactory;
import com.wlx.middleware.mybatis.reflection.factory.ObjectFactory;
import com.wlx.middleware.mybatis.reflection.wrapper.DefaultObjectWrapperFactory;
import com.wlx.middleware.mybatis.reflection.wrapper.ObjectWrapperFactory;
import com.wlx.middleware.mybatis.scripting.LanguageDriver;
import com.wlx.middleware.mybatis.scripting.LanguageDriverRegistry;
import com.wlx.middleware.mybatis.scripting.xmltags.XMLLanguageDriver;
import com.wlx.middleware.mybatis.transaction.Transaction;
import com.wlx.middleware.mybatis.transaction.jdbc.JdbcTransactionFactory;
import com.wlx.middleware.mybatis.type.TypeAliasRegistry;
import com.wlx.middleware.mybatis.type.TypeHandlerRegistry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 配置项，管理所有mapperStatement
 */
public class Configuration {

    protected Environment environment;

    protected boolean useGeneratedKeys = false;
    // 缓存机制，默认不配置的情况是 SESSION
    protected LocalCacheScope localCacheScope = LocalCacheScope.SESSION;

    // 映射的语句，key为namespace+id
    private Map<String, MappedStatement> mappedStatements = new HashMap<>();

    // 映射注册机
    private MapperRegistry mapperRegistry = new MapperRegistry(this);

    // 类型别名注册机
    private TypeAliasRegistry typeAliasRegistry = new TypeAliasRegistry();

    protected final TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry();

    private ObjectFactory objectFactory = new DefaultObjectFactory();

    private ObjectWrapperFactory objectWrapperFactory = new DefaultObjectWrapperFactory();

    protected String databaseId;

    protected final Set<String> loadedResource = new HashSet<>();

    protected final LanguageDriverRegistry languageRegistry = new LanguageDriverRegistry();

    protected final Map<String, ResultMap> resultMaps = new HashMap<>();

    protected final Map<String, KeyGenerator> keyGenerators = new HashMap<>();

    protected final InterceptorChain interceptorChain = new InterceptorChain();

    public Configuration() {
        typeAliasRegistry.registerAlias("DRUID", DruidDataSourceFactory.class);
        typeAliasRegistry.registerAlias("JDBC", JdbcTransactionFactory.class);
        typeAliasRegistry.registerAlias("POOLED", PooledDataSourceFactory.class);
        typeAliasRegistry.registerAlias("UNPOOLED", UnpooledDataSourceFactory.class);

        languageRegistry.setDefaultDriverClass(XMLLanguageDriver.class);
    }

    public Executor newExecutor(Transaction transaction) {
        Executor executor = new SimpleExecutor(this, transaction);
        executor = (Executor) interceptorChain.pluginAll(executor);
        return executor;
    }

    public StatementHandler newStatementHandler(MappedStatement mappedStatement, ResultHandler resultHandler, RowBounds rowBounds,
                                                BoundSql boundSql, Executor executor, Configuration configuration,
                                                Object parameterObject) {
        StatementHandler statementHandler = new PreparedStatementHandler(mappedStatement, resultHandler,
                rowBounds, boundSql, executor, configuration, parameterObject);
        statementHandler = (StatementHandler) interceptorChain.pluginAll(statementHandler);
        return statementHandler;
    }

    public ParameterHandler newParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
        ParameterHandler parameterHandler = mappedStatement.getLanguageDriver().createParameterHandler(mappedStatement, parameterObject, boundSql);
        parameterHandler = (ParameterHandler) interceptorChain.pluginAll(parameterHandler);
        return parameterHandler;
    }

    public ResultSetHandler newResultSetHandler(Executor executor, MappedStatement mappedStatement, RowBounds rowBounds,
                                                ResultHandler resultHandler, BoundSql boundSql) {
        ResultSetHandler resultSetHandler = new DefaultResultSetHandler(executor, mappedStatement, resultHandler, rowBounds, boundSql);
        resultSetHandler = (ResultSetHandler) interceptorChain.pluginAll(resultSetHandler);
        return resultSetHandler;
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

    public TypeHandlerRegistry getTypeHandlerRegistry() {
        return typeHandlerRegistry;
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

    public LanguageDriver getDefaultScriptingLanguageInstance() {
        return languageRegistry.getDefaultDriver();
    }

    public ObjectFactory getObjectFactory() {
        return objectFactory;
    }

    public ResultMap getResultMap(String id) {
        return resultMaps.get(id);
    }

    public void addResultMap(ResultMap resultMap) {
        resultMaps.put(resultMap.getId(), resultMap);
    }

    public void addKeyGenerator(String id, KeyGenerator keyGenerator) {
        keyGenerators.put(id, keyGenerator);
    }

    public KeyGenerator getKeyGenerator(String id) {
        return keyGenerators.get(id);
    }

    public boolean hasGenerator(String id) {
        return keyGenerators.containsKey(id);
    }

    public boolean isUseGeneratedKeys() {
        return useGeneratedKeys;
    }

    public void addInterceptor(Interceptor interceptor) {
        interceptorChain.addInterceptor(interceptor);
    }

    public LocalCacheScope getLocalCacheScope() {
        return localCacheScope;
    }

    public void setLocalCacheScope(LocalCacheScope localCacheScope) {
        this.localCacheScope = localCacheScope;
    }
}
