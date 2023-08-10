package com.wlx.middleware.mybatis.builder;

import com.wlx.middleware.mybatis.mapping.ParameterMapping;
import com.wlx.middleware.mybatis.mapping.SqlSource;
import com.wlx.middleware.mybatis.parsing.GenericTokenParser;
import com.wlx.middleware.mybatis.parsing.TokenHandler;
import com.wlx.middleware.mybatis.reflection.MetaObject;
import com.wlx.middleware.mybatis.session.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * SQL源码构建器
 */
public class SqlSourceBuilder extends BaseBuilder {
    public SqlSourceBuilder(Configuration configuration) {
        super(configuration);
    }

    public SqlSource parse(String originalSql, Class<?> parameterType, Map<String, Object> additionalParameters) {
        ParameterMappingTokenHandler tokenHandler = new ParameterMappingTokenHandler(configuration, parameterType, additionalParameters);
        GenericTokenParser parser = new GenericTokenParser("#{", "}", tokenHandler);
        String sql = parser.parse(originalSql);
        return new StaticSqlSource(configuration, sql, tokenHandler.getParameterMappings());
    }


    /**
     * sql语句中的参数处理器
     */
    private static class ParameterMappingTokenHandler extends BaseBuilder implements TokenHandler {

        private List<ParameterMapping> parameterMappings = new ArrayList<>();

        private Class<?> parameterType;

        private MetaObject metaParameters;

        public ParameterMappingTokenHandler(Configuration configuration, Class<?> parameterType,
                                            Map<String, Object> additionalParameters) {
            super(configuration);
            this.parameterType = parameterType;
            this.metaParameters = configuration.newMetaObject(additionalParameters);
        }

        public List<ParameterMapping> getParameterMappings() {
            return parameterMappings;
        }

        @Override
        public String handleToken(String content) {
            // 将参数解析出来放在map中
            Map<String, String> propertiesMap = new ParameterExpression(content);
            String property = propertiesMap.get("property");
            ParameterMapping.Builder builder = new ParameterMapping.Builder(configuration, property, parameterType);
            ParameterMapping parameterMapping = builder.build();
            parameterMappings.add(parameterMapping);
            return "?";
        }
    }
}
