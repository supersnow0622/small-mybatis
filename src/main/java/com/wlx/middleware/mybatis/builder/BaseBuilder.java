package com.wlx.middleware.mybatis.builder;

import com.wlx.middleware.mybatis.session.Configuration;
import com.wlx.middleware.mybatis.type.TypeAliasRegistry;

public abstract class BaseBuilder {

    protected Configuration configuration;

    protected TypeAliasRegistry typeAliasRegistry;

    public BaseBuilder(Configuration configuration) {
        this.configuration = configuration;
        this.typeAliasRegistry = this.configuration.getTypeAliasRegistry();
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public TypeAliasRegistry getTypeAliasRegistry() {
        return typeAliasRegistry;
    }

    public Class<?> resolveAlias(String alias) {
        return typeAliasRegistry.resolveAlias(alias);
    }
}
