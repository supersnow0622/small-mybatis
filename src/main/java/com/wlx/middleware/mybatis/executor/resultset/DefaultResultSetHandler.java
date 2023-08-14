package com.wlx.middleware.mybatis.executor.resultset;

import com.wlx.middleware.mybatis.executor.Executor;
import com.wlx.middleware.mybatis.executor.result.DefaultResultContext;
import com.wlx.middleware.mybatis.executor.result.DefaultResultHandler;
import com.wlx.middleware.mybatis.mapping.BoundSql;
import com.wlx.middleware.mybatis.mapping.MappedStatement;
import com.wlx.middleware.mybatis.mapping.ResultMap;
import com.wlx.middleware.mybatis.reflection.MetaClass;
import com.wlx.middleware.mybatis.reflection.MetaObject;
import com.wlx.middleware.mybatis.reflection.factory.ObjectFactory;
import com.wlx.middleware.mybatis.session.Configuration;
import com.wlx.middleware.mybatis.session.ResultHandler;
import com.wlx.middleware.mybatis.session.RowBounds;
import com.wlx.middleware.mybatis.type.TypeHandler;
import com.wlx.middleware.mybatis.type.TypeHandlerRegistry;

import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DefaultResultSetHandler implements ResultSetHandler {

    private final BoundSql boundSql;
    private final RowBounds rowBounds;
    private final MappedStatement mappedStatement;
    private final Configuration configuration;
    private final ResultHandler resultHandler;
    private final ObjectFactory objectFactory;
    private final TypeHandlerRegistry typeHandlerRegistry;

    public DefaultResultSetHandler(Executor executor, MappedStatement mappedStatement, ResultHandler resultHandler, RowBounds rowBounds, BoundSql boundSql) {
        this.configuration = mappedStatement.getConfiguration();
        this.rowBounds = rowBounds;
        this.boundSql = boundSql;
        this.mappedStatement = mappedStatement;
        this.resultHandler = resultHandler;
        this.objectFactory = configuration.getObjectFactory();
        this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();
    }

    /**
     * 处理结果集，映射为java对象
     * @param stmt
     * @return
     * @param <E>
     * @throws SQLException
     */
    @Override
    public List<Object> handleResultSets(Statement stmt) throws SQLException {
        List<Object> multipleResults = new ArrayList<>();

        // 结果集执行行数
        int resultSetCount = 0;
        // 数据表结果集包装类
        ResultSetWrapper rsw = new ResultSetWrapper(stmt.getResultSet(), configuration);
        // Java代码中返回的类型
        List<ResultMap> resultMaps = mappedStatement.getResultMaps();

        while(rsw != null && resultMaps.size() > resultSetCount) {
            ResultMap resultMap = resultMaps.get(resultSetCount);
            handleResultSet(rsw, resultMap, multipleResults, null);
            rsw = getNextResultSet(stmt);
            resultSetCount++;
        }

        return multipleResults.size() == 1 ? (List<Object>) multipleResults.get(0) : multipleResults;
    }

    private void handleResultSet(ResultSetWrapper rsw, ResultMap resultMap, List<Object> multipleResults, Object o) throws SQLException {
        if (resultHandler == null) {
            // 1. 新创建结果处理器
            DefaultResultHandler defaultResultHandler = new DefaultResultHandler(objectFactory);
            // 2. 封装数据
            handleRowValuesForSimpleResultMap(rsw, resultMap, defaultResultHandler, rowBounds);
            // 3. 保存结果
            multipleResults.add(defaultResultHandler.getResultList());
        }
    }

    private ResultSetWrapper getNextResultSet(Statement stmt) throws SQLException {
        if (stmt.getConnection().getMetaData().supportsMultipleResultSets()) {
            if (!((!stmt.getMoreResults()) && (stmt.getUpdateCount() == -1))) {
                ResultSet rs = stmt.getResultSet();
                return rs != null ? new ResultSetWrapper(rs, configuration) : null;
            }
        }

        return null;
    }

    private void handleRowValuesForSimpleResultMap(ResultSetWrapper rsw, ResultMap resultMap, DefaultResultHandler defaultResultHandler, RowBounds rowBounds) throws SQLException {
        DefaultResultContext resultContext = new DefaultResultContext();
        while (resultContext.getResultCount() < rowBounds.getLimit() && rsw.getResultSet().next()) {
            // 获取一行的数据
            Object rowValue = getRowValue(rsw, resultMap);
            // 记录一行的数据
            resultContext.nextResultObject(rowValue);
            defaultResultHandler.handleResult(resultContext);
        }
    }

    private Object getRowValue(ResultSetWrapper rsw, ResultMap resultMap) throws SQLException {
        // 根据返回类型，实例化对象
        Object resultObject = createResultObject(rsw, resultMap);

        // 设置对象属性值
        if (resultObject != null && !typeHandlerRegistry.hasTypeHandler(resultMap.getType())) {
            final MetaObject metaObject = configuration.newMetaObject(resultObject);
            applyAutomaticMappings(rsw, resultMap, metaObject, null);
        }

        return resultObject;
    }

    private Object createResultObject(ResultSetWrapper rsw, ResultMap resultMap) {
        Class<?> resultType = resultMap.getType();
        MetaClass metaClass = MetaClass.forClass(resultType);
        if (resultType.isInterface() || metaClass.hasDefaultConstructor()) {
            return objectFactory.create(resultType);
        }
        throw new RuntimeException("Do not know how to create an instance of " + resultType);
    }

    /**
     * 通过反射设置返回对象的属性字段
     * @param rsw 提供数据表中的数据
     * @param resultMap 包含返回的java对象
     * @param metaObject 返回的java元对象
     * @param columnPrefix 列名称前缀
     * @return
     * @throws SQLException
     */
    private boolean applyAutomaticMappings(ResultSetWrapper rsw, ResultMap resultMap, MetaObject metaObject, String columnPrefix) throws SQLException {
        List<String> unmappedColumnNames = rsw.getUnmappedColumnNames(resultMap, columnPrefix);
        boolean foundValue = false;
        for (String columnName : unmappedColumnNames) {
            String propertyName = columnName;
            if (columnPrefix != null && !columnPrefix.isEmpty()) {
                if (columnName.toUpperCase(Locale.ENGLISH).startsWith(columnPrefix)) {
                    propertyName = columnName.substring(columnPrefix.length());
                } else {
                    continue;
                }
            }
            String property = metaObject.findProperty(propertyName, false);
            if (property != null && metaObject.hasSetter(property)) {
                Class<?> propertyType = metaObject.getSetterType(property);
                if (typeHandlerRegistry.hasTypeHandler(propertyType)) {
                    TypeHandler<?> typeHandler = rsw.getTypeHandler(propertyType, columnName);
                    Object value = typeHandler.getResult(rsw.getResultSet(), columnName);
                    if (value != null) {
                        foundValue = true;
                    }
                    if (value != null && !propertyType.isPrimitive()) {
                        metaObject.setValue(property, value);
                    }
                }
            }
        }
        return foundValue;
    }

}
