package com.wlx.middleware.mybatis.type;

import java.util.HashMap;
import java.util.Map;

/**
 * 类型别名注册机
 */
public class TypeAliasRegistry {

    private Map<String, Class<?>> TYPE_ALIASES = new HashMap<>();

    public TypeAliasRegistry() {
        registerAlias("string", String.class);
        registerAlias("long", Long.class);
        registerAlias("short", Short.class);
        registerAlias("int", Integer.class);
        registerAlias("integer", Integer.class);
        registerAlias("double", Double.class);
        registerAlias("float", Float.class);
        registerAlias("boolean", Boolean.class);
    }

    public void registerAlias(String alias, Class<?> clazz) {
        TYPE_ALIASES.put(alias.toLowerCase(), clazz);
    }

    public <T> Class<T> resolveAlias(String alias) {
        return (Class<T>) TYPE_ALIASES.get(alias.toLowerCase());
    }
}
