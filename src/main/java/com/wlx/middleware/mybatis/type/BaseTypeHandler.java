package com.wlx.middleware.mybatis.type;

import com.wlx.middleware.mybatis.session.Configuration;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class BaseTypeHandler<T> implements TypeHandler<T> {

    protected Configuration configuration;

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void setParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException {
        setNonNullParameter(ps, i, parameter, jdbcType);
    }

    @Override
    public T getResult(ResultSet rs, String columnName) throws SQLException {
        return getNullableResult(rs, columnName);
    }

    @Override
    public T getResult(ResultSet rs, int columnIndex) throws SQLException {
        return getNullableResult(rs, columnIndex);
    }

    protected abstract T getNullableResult(ResultSet rs, int columnIndex) throws SQLException;

    protected abstract T getNullableResult(ResultSet rs, String columnName) throws SQLException;

    protected abstract void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException;
}
