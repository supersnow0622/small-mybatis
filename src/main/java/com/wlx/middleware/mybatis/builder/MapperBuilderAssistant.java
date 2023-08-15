package com.wlx.middleware.mybatis.builder;

import com.wlx.middleware.mybatis.mapping.*;
import com.wlx.middleware.mybatis.reflection.MetaClass;
import com.wlx.middleware.mybatis.session.Configuration;
import com.wlx.middleware.mybatis.type.TypeHandler;

import java.util.ArrayList;
import java.util.List;

public class MapperBuilderAssistant extends BaseBuilder {

    private String currentNamespace;

    private String resource;

    public MapperBuilderAssistant(Configuration configuration, String resource) {
        super(configuration);
        this.resource = resource;
    }

    public ResultMapping buildResultMapping(Class<?> resultType,
                                            String property,
                                            String column,
                                            List<ResultFlag> flags) {

        Class<?> javaTypeClass = resolveResultJavaType(resultType, property, null);
        TypeHandler<?> typeHandler = typeHandlerRegistry.getMappingTypeHandler(javaTypeClass);

        ResultMapping.Builder builder = new ResultMapping.Builder(configuration, property, column, javaTypeClass)
                .typeHandler(typeHandler).flags(flags);

        return builder.build();
    }

    private Class<?> resolveResultJavaType(Class<?> resultType, String property, Class<?> javaType) {
        if (javaType == null && property != null) {
            MetaClass metaClass = MetaClass.forClass(resultType);
            javaType = metaClass.getSetterType(property);
        }
        if (javaType == null) {
            javaType = Object.class;
        }
        return javaType;
    }

    public String getCurrentNamespace() {
        return currentNamespace;
    }

    public void setCurrentNamespace(String currentNamespace) {
        this.currentNamespace = currentNamespace;
    }

    public MappedStatement addMappedStatement(String id, SqlSource sqlSource, SqlCommandType sqlCommandType,
                                              String resultMap, Class<?> resultType) {
        id = applyCurrentNamespace(id, false);
        MappedStatement.Builder builder = new MappedStatement.Builder(configuration,
                id, sqlCommandType, sqlSource, resultType);

        // 结果映射，给 MappedStatement#resultMaps
        setStatementResultMap(resultMap, resultType, builder);

        MappedStatement mappedStatement = builder.build();
        configuration.addMappedStatement(mappedStatement);
        return mappedStatement;
    }

    public ResultMap addResultMap(String resultMapId, Class<?> returnType, List<ResultMapping> resultMappings) {
        resultMapId = applyCurrentNamespace(resultMapId, false);
        ResultMap.Builder inlineResultMapBuilder = new ResultMap.Builder(configuration, resultMapId,
                returnType, resultMappings);
        ResultMap resultMap = inlineResultMapBuilder.build();
        configuration.addResultMap(resultMap);

        return resultMap;
    }

    private void setStatementResultMap(String resultMap, Class<?> resultType, MappedStatement.Builder builder) {
        resultMap = applyCurrentNamespace(resultMap, true);

        List<ResultMap> resultMaps = new ArrayList<>();

        if (resultMap != null) {
            String[] resultMapNames = resultMap.split(",");
            for (String resultMapName : resultMapNames) {
                resultMaps.add(configuration.getResultMap(resultMapName.trim()));
            }
        } else if (resultType != null) {
            ResultMap.Builder inlineResultMapBuilder = new ResultMap.Builder(configuration, builder.id() + "-inline",
                    resultType, new ArrayList<>());
            resultMaps.add(inlineResultMapBuilder.build());
        }
        builder.resultMaps(resultMaps);
    }


    private String applyCurrentNamespace(String base, boolean isReference) {
        if (base == null) {
            return null;
        }
        if (isReference) {
            if (base.contains(".")) return base;
        } else {
            if (base.startsWith(currentNamespace + ".")) {
                return base;
            }
            if (base.contains(".")) {
                throw new RuntimeException("Dots are not allowed in element names, please remove it from " + base);
            }
        }
        return currentNamespace + "." + base;
    }
}
