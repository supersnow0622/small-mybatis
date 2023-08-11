package com.wlx.middleware.mybatis.scripting.defaults;

import com.alibaba.fastjson.JSON;
import com.wlx.middleware.mybatis.executor.parameter.ParameterHandler;
import com.wlx.middleware.mybatis.mapping.BoundSql;
import com.wlx.middleware.mybatis.mapping.MappedStatement;
import com.wlx.middleware.mybatis.mapping.ParameterMapping;
import com.wlx.middleware.mybatis.reflection.MetaObject;
import com.wlx.middleware.mybatis.session.Configuration;
import com.wlx.middleware.mybatis.type.JdbcType;
import com.wlx.middleware.mybatis.type.TypeHandler;
import com.wlx.middleware.mybatis.type.TypeHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class DefaultParameterHandler implements ParameterHandler {

    private Logger logger = LoggerFactory.getLogger(DefaultParameterHandler.class);

    private final Object parameterObject;

    private BoundSql boundSql;

    private final TypeHandlerRegistry typeHandlerRegistry;

    private final Object mappedStatement;

    private Configuration configuration;

    public DefaultParameterHandler(Object parameterObject, BoundSql boundSql, MappedStatement mappedStatement) {
        this.parameterObject = parameterObject;
        this.boundSql = boundSql;
        this.typeHandlerRegistry = mappedStatement.getConfiguration().getTypeHandlerRegistry();
        this.mappedStatement = mappedStatement;
        this.configuration = mappedStatement.getConfiguration();
    }

    @Override
    public Object getParameterObject() {
        return parameterObject;
    }

    @Override
    public void setParameters(PreparedStatement ps) throws SQLException {
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        if (parameterMappings == null) {
            return;
        }
        for (int i = 0; i < parameterMappings.size(); i++) {
            ParameterMapping parameterMapping = parameterMappings.get(i);
            String propertyName = parameterMapping.getProperty();
            Object value;
            if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                value = parameterObject;
            } else {
                MetaObject metaObject = configuration.newMetaObject(parameterObject);
                value = metaObject.getValue(propertyName);
            }
            JdbcType jdbcType = parameterMapping.getJdbcType();

            logger.info("根据每个ParameterMapping中的TypeHandler设置对应的参数信息 value：{}", JSON.toJSONString(value));
            TypeHandler typeHandler = parameterMapping.getTypeHandler();
            typeHandler.setParameter(ps, i + 1, value, jdbcType);
        }
    }
}
