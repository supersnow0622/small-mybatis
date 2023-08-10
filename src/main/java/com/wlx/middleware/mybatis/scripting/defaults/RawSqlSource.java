package com.wlx.middleware.mybatis.scripting.defaults;

import com.wlx.middleware.mybatis.builder.SqlSourceBuilder;
import com.wlx.middleware.mybatis.mapping.BoundSql;
import com.wlx.middleware.mybatis.mapping.SqlSource;
import com.wlx.middleware.mybatis.scripting.xmltags.DynamicContext;
import com.wlx.middleware.mybatis.scripting.xmltags.SqlNode;
import com.wlx.middleware.mybatis.session.Configuration;

import java.util.HashMap;

/**
 * 原始SQL源码，比如：select * from user where id=#{id}
 */
public class RawSqlSource implements SqlSource {

    // 解析后的静态SQL
    private final SqlSource sqlSource;

    public RawSqlSource(Configuration configuration, SqlNode rootSqlNode, Class<?> parameterType) {
        this(configuration, getSql(configuration, rootSqlNode), parameterType);
    }

    public RawSqlSource(Configuration configuration, String sql, Class<?> parameterType) {
        SqlSourceBuilder sqlSourceBuilder = new SqlSourceBuilder(configuration);
        Class<?> clazz = parameterType == null ? Object.class : parameterType;
        sqlSource = sqlSourceBuilder.parse(sql, clazz, new HashMap<>());
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        return sqlSource.getBoundSql(parameterObject);
    }

    private static String getSql(Configuration configuration, SqlNode rootSqlNode) {
        DynamicContext context = new DynamicContext(configuration, null);
        rootSqlNode.apply(context);
        return context.getSql();
    }
}
