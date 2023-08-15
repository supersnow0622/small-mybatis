package com.wlx.middleware.mybatis.builder.annotation;

import com.wlx.middleware.mybatis.annotations.Delete;
import com.wlx.middleware.mybatis.annotations.Insert;
import com.wlx.middleware.mybatis.annotations.Select;
import com.wlx.middleware.mybatis.annotations.Update;
import com.wlx.middleware.mybatis.binding.MapperMethod;
import com.wlx.middleware.mybatis.builder.MapperBuilderAssistant;
import com.wlx.middleware.mybatis.executor.keygen.Jdbc3KeyGenerator;
import com.wlx.middleware.mybatis.executor.keygen.KeyGenerator;
import com.wlx.middleware.mybatis.executor.keygen.NoKeyGenerator;
import com.wlx.middleware.mybatis.mapping.SqlCommandType;
import com.wlx.middleware.mybatis.mapping.SqlSource;
import com.wlx.middleware.mybatis.scripting.LanguageDriver;
import com.wlx.middleware.mybatis.session.Configuration;
import com.wlx.middleware.mybatis.session.ResultHandler;
import com.wlx.middleware.mybatis.session.RowBounds;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

/**
 * 注解配置构建器 Mapper
 */
public class MapperAnnotationBuilder {

    private final Set<Class<? extends Annotation>> sqlAnnotationTypes = new HashSet<>();

    private Configuration configuration;

    private MapperBuilderAssistant assistant;

    private Class<?> type;

    public MapperAnnotationBuilder(Configuration configuration, Class<?> type) {
        String resource = type.getName().replace(".", "/") + ".java(best guess)";
        this.assistant = new MapperBuilderAssistant(configuration, resource);
        this.configuration = configuration;
        this.type = type;

        sqlAnnotationTypes.add(Select.class);
        sqlAnnotationTypes.add(Insert.class);
        sqlAnnotationTypes.add(Update.class);
        sqlAnnotationTypes.add(Delete.class);
    }

    public void parse() {
        String resource = type.toString();
        if (!configuration.isResourceLoaded(resource)) {
            assistant.setCurrentNamespace(type.getName());

            Method[] methods = type.getMethods();
            for (Method method : methods) {
                if (!method.isBridge()) {
                    parseStatement(method);
                }
            }
        }
    }

    private void parseStatement(Method method) {
        Class<?> parameterTypeClass = getParameterType(method);
        LanguageDriver languageDriver = configuration.getLanguageRegistry().getDefaultDriver();
        SqlSource sqlSource = getSqlSourceFromAnnotations(method, parameterTypeClass, languageDriver);

        if (sqlSource != null) {
            final String mappedStatementId = type.getName() + "." + method.getName();
            SqlCommandType sqlCommandType = getSqlCommandType(method);

            KeyGenerator keyGenerator = configuration.isUseGeneratedKeys() && SqlCommandType.INSERT.equals(sqlCommandType)
                    ? new Jdbc3KeyGenerator() : new NoKeyGenerator();
            String keyProperty = "id";

            boolean isSelect = sqlCommandType == SqlCommandType.SELECT;
            String resultMapId = null;
            if (isSelect) {
                resultMapId = parseResultMap(method);
            }

            assistant.addMappedStatement(mappedStatementId, sqlSource, sqlCommandType,
                    resultMapId, getReturnType(method), keyGenerator, keyProperty);
        }
    }

    private Class<?> getParameterType(Method method) {
        Class<?> parameterType = null;
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (Class<?> clazz : parameterTypes) {
            if (!RowBounds.class.isAssignableFrom(clazz) && !ResultHandler.class.isAssignableFrom(clazz)) {
                if (parameterType == null) {
                    parameterType = clazz;
                } else {
                    parameterType = MapperMethod.ParamMap.class;
                }
            }
        }
        return parameterType;
    }

    private SqlSource getSqlSourceFromAnnotations(Method method, Class<?> parameterTypeClass, LanguageDriver languageDriver) {
        try {
            Class<? extends Annotation> sqlAnnotationType = getSqlAnnotationType(method);
            if (sqlAnnotationType != null) {
                Annotation sqlAnnotation = method.getAnnotation(sqlAnnotationType);
                String[] strings = (String[]) sqlAnnotation.getClass().getMethod("values").invoke(sqlAnnotation);
                StringBuilder sql = new StringBuilder();
                for (String fragment : strings) {
                    sql.append(fragment);
                    sql.append(" ");
                }
                return languageDriver.createSqlSource(configuration, sql.toString(), parameterTypeClass);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not find value method on SQL annotation.  Cause: " + e);
        }

        return null;
    }

    private Class<? extends Annotation> getSqlAnnotationType(Method method) {
        for (Class<? extends Annotation> type : sqlAnnotationTypes) {
            Annotation annotation = method.getAnnotation(type);
            if (annotation != null) {
                return type;
            }
        }
        return null;
    }

    private SqlCommandType getSqlCommandType(Method method) {
        Class<? extends Annotation> type = getSqlAnnotationType(method);
        if (type == null) {
            return SqlCommandType.UNKNOWN;
        }
        return SqlCommandType.valueOf(type.getSimpleName().toUpperCase(Locale.ENGLISH));
    }

    private String parseResultMap(Method method) {
        StringBuilder suffix = new StringBuilder();
        for (Class<?> c : method.getParameterTypes()) {
            suffix.append("-");
            suffix.append(c.getSimpleName());
        }
        if (suffix.length() < 1) {
            suffix.append("-void");
        }
        String resultMapId = type.getName() + "." + method.getName() + suffix;

        Class<?> returnType = getReturnType(method);
        assistant.addResultMap(resultMapId, returnType, new ArrayList<>());
        return resultMapId;
    }

    private Class<?> getReturnType(Method method) {
        Class<?> returnType = method.getReturnType();
        if (Collection.class.isAssignableFrom(returnType)) {
            Type returnTypeParameter = method.getGenericReturnType();
            if (returnTypeParameter instanceof ParameterizedType) {
                Type[] actualTypeArguments = ((ParameterizedType)returnTypeParameter).getActualTypeArguments();
                if (actualTypeArguments != null && actualTypeArguments.length == 1) {
                    returnTypeParameter = actualTypeArguments[0];
                    if (returnTypeParameter instanceof Class) {
                        returnType = (Class<?>) returnTypeParameter;
                    } else if (returnTypeParameter instanceof ParameterizedType) {
                        // (issue #443) actual type can be a also a parameterized type
                        returnType = (Class<?>) ((ParameterizedType) returnTypeParameter).getRawType();
                    } else if (returnTypeParameter instanceof GenericArrayType) {
                        Class<?> componentType = (Class<?>) ((GenericArrayType) returnTypeParameter).getGenericComponentType();
                        // (issue #525) support List<byte[]>
                        returnType = Array.newInstance(componentType, 0).getClass();
                    }
                }
            }
        }
        return returnType;
    }

}
