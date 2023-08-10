package com.wlx.middleware.mybatis.type;

import com.wlx.middleware.mybatis.io.Resources;

import java.util.HashMap;
import java.util.Locale;
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
        try {
            if (alias == null) {
                return null;
            }
            String key = alias.toLowerCase(Locale.ENGLISH);
            Class<T> value;
            if (TYPE_ALIASES.containsKey(key)) {
                value = (Class<T>) TYPE_ALIASES.get(key);
            } else {
                value = (Class<T>) Resources.classForName(alias);
            }
            return value;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not resolve type alias '" + alias + "'.  Cause: " + e, e);
        }
    }
}
