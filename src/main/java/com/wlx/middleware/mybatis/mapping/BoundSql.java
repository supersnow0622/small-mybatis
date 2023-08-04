package com.wlx.middleware.mybatis.mapping;

import java.util.HashMap;
import java.util.Map;

public class BoundSql {

    private String parameterType;
    private Map<Integer, String> parameter = new HashMap<>();
    private String returnType;
    private String sql;

    public BoundSql(String parameterType, Map<Integer, String> parameter, String returnType, String sql) {
        this.parameterType = parameterType;
        this.parameter = parameter;
        this.returnType = returnType;
        this.sql = sql;
    }

    public String getParameterType() {
        return parameterType;
    }

    public Map<Integer, String> getParameter() {
        return parameter;
    }

    public String getReturnType() {
        return returnType;
    }

    public String getSql() {
        return sql;
    }
}
