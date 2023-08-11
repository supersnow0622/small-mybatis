package com.wlx.middleware.mybatis.type;


import java.sql.PreparedStatement;
import java.sql.SQLException;

public class LongTypeHandler extends BaseTypeHandler<Long> {


    @Override
    protected void setNonNullParameter(PreparedStatement ps, int i, Long parameter, JdbcType jdbcType) throws SQLException {
        ps.setLong(i, parameter);
    }
}
