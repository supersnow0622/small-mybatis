package com.wlx.middleware.mybatis.executor.resultset;

import com.wlx.middleware.mybatis.executor.Executor;
import com.wlx.middleware.mybatis.mapping.BoundSql;
import com.wlx.middleware.mybatis.mapping.MappedStatement;

import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DefaultResultSetHandler implements ResultSetHandler {

    private final BoundSql boundSql;

    public DefaultResultSetHandler(Executor executor, MappedStatement mappedStatement, BoundSql boundSql) {
        this.boundSql = boundSql;
    }

    @Override
    public <E> List<E> handleResultSets(Statement stmt) throws SQLException {
        ResultSet resultSet = stmt.getResultSet();
        try {
            return resultSet2Obj(resultSet, Class.forName(boundSql.getReturnType()));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private <T> List<T> resultSet2Obj(ResultSet resultSet, Class<?> clazz) {
        List<T> results = new ArrayList<>();
        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnSize = metaData.getColumnCount();
            while (resultSet.next()) {
                T obj = (T) clazz.newInstance();
                for (int i = 1; i < columnSize; i++) {
                    Object value = resultSet.getObject(i);
                    String columnName = metaData.getColumnName(i);
                    String setMethod = "set" + columnName.substring(0, 1).toUpperCase() + columnName.substring(1);
                    Method method;
                    if (value instanceof Timestamp) {
                        method = clazz.getMethod(setMethod, Date.class);
                    } else {
                        method = clazz.getMethod(setMethod, value.getClass());
                    }
                    method.invoke(obj, value);
                }
                results.add(obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }
}
