package com.wlx.middleware.mybatis.builder;

import com.sun.org.apache.xpath.internal.operations.Bool;
import com.wlx.middleware.mybatis.session.Configuration;
import com.wlx.middleware.mybatis.type.TypeAliasRegistry;
import com.wlx.middleware.mybatis.type.TypeHandlerRegistry;

public abstract class BaseBuilder {

    protected Configuration configuration;

    protected TypeAliasRegistry typeAliasRegistry;

    protected TypeHandlerRegistry typeHandlerRegistry;

    public BaseBuilder(Configuration configuration) {
        this.configuration = configuration;
        this.typeAliasRegistry = this.configuration.getTypeAliasRegistry();
        this.typeHandlerRegistry = this.configuration.getTypeHandlerRegistry();
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public TypeAliasRegistry getTypeAliasRegistry() {
        return typeAliasRegistry;
    }

    protected Class<?> resolveClass(String type) {
        if (type == null) {
            return null;
        }
        try {
            return resolveAlias(type);
        } catch (Exception e) {
            throw new RuntimeException("Error resolving class. Cause: " + e, e);
        }
    }

    public Class<?> resolveAlias(String alias) {
        return typeAliasRegistry.resolveAlias(alias);
    }

    protected Boolean booleanValueOf(String cacheEnabled, Boolean defaultValue) {
        return cacheEnabled == null ? defaultValue : Boolean.valueOf(cacheEnabled);
    }
}
