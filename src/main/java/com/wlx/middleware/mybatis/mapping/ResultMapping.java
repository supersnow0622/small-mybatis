package com.wlx.middleware.mybatis.mapping;

import com.wlx.middleware.mybatis.session.Configuration;
import com.wlx.middleware.mybatis.type.JdbcType;
import com.wlx.middleware.mybatis.type.TypeHandler;

/**
 * 结果映射
 */
public class ResultMapping {

    private Configuration configuration;

    // java中的属性名称，即 #{property}
    private String property;

    private Class<?> javaType;

    // 数据表中的列名称
    private String column;

    private JdbcType jdbcType;

    // 类型处理器
    private TypeHandler<?> typeHandler;

    ResultMapping() {
    }

    public static class Builder {
        private ResultMapping resultMapping = new ResultMapping();

    }
}
