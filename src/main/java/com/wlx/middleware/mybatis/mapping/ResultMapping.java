package com.wlx.middleware.mybatis.mapping;

import com.wlx.middleware.mybatis.session.Configuration;
import com.wlx.middleware.mybatis.type.JdbcType;
import com.wlx.middleware.mybatis.type.TypeHandler;
import com.wlx.middleware.mybatis.type.TypeHandlerRegistry;

import java.util.ArrayList;
import java.util.List;

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

    private List<ResultFlag> flags;

    ResultMapping() {
    }

    public static class Builder {
        private ResultMapping resultMapping = new ResultMapping();

        public Builder(Configuration configuration, String property, String column, Class<?> javaType) {
            resultMapping.configuration = configuration;
            resultMapping.property = property;
            resultMapping.column = column;
            resultMapping.javaType = javaType;
            resultMapping.flags = new ArrayList<>();
        }

        public Builder typeHandler(TypeHandler<?> typeHandler) {
            resultMapping.typeHandler = typeHandler;
            return this;
        }

        public Builder flags(List<ResultFlag> flags) {
            resultMapping.flags = flags;
            return this;
        }

        public ResultMapping build() {
            resolveTypeHandler();
            return resultMapping;
        }

        private void resolveTypeHandler() {
            if (resultMapping.typeHandler == null && resultMapping.javaType != null) {
                Configuration configuration = resultMapping.configuration;
                TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
                resultMapping.typeHandler = typeHandlerRegistry.getTypeHandler(resultMapping.javaType, resultMapping.jdbcType);
            }
        }
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public String getProperty() {
        return property;
    }

    public String getColumn() {
        return column;
    }

    public TypeHandler<?> getTypeHandler() {
        return typeHandler;
    }
}
