package com.wlx.middleware.mybatis.type;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LongTypeHandler extends BaseTypeHandler<Long> {


    @Override
    protected Long getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return rs.getLong(columnIndex);
    }

    @Override
    protected Long getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return rs.getLong(columnName);
    }

    @Override
    protected void setNonNullParameter(PreparedStatement ps, int i, Long parameter, JdbcType jdbcType) throws SQLException {
        ps.setLong(i, parameter);
    }
}
