package com.wlx.middleware.mybatis.executor.keygen;

import com.wlx.middleware.mybatis.executor.Executor;
import com.wlx.middleware.mybatis.mapping.MappedStatement;
import com.wlx.middleware.mybatis.reflection.MetaObject;
import com.wlx.middleware.mybatis.session.Configuration;
import com.wlx.middleware.mybatis.session.RowBounds;

import java.sql.Statement;
import java.util.List;

/**
 * 键值生成器
 */
public class SelectKeyGenerator implements KeyGenerator {

    public static final String SELECT_KEY_SUFFIX = "!selectKey";
    private boolean executeBefore;
    private MappedStatement keyStatement;

    public SelectKeyGenerator(boolean executeBefore, MappedStatement keyStatement) {
        this.executeBefore = executeBefore;
        this.keyStatement = keyStatement;
    }

    @Override
    public void processBefore(Executor executor, MappedStatement mappedStatement, Statement statement, Object parameter) {
        if (executeBefore) {
            processGenerateKeys(executor, mappedStatement, parameter);
        }
    }

    @Override
    public void processAfter(Executor executor, MappedStatement mappedStatement, Statement statement, Object parameter) {
        if (!executeBefore) {
            processGenerateKeys(executor, mappedStatement, parameter);
        }
    }

    private void processGenerateKeys(Executor executor, MappedStatement mappedStatement, Object parameter) {
        try {
            if (parameter != null && keyStatement != null && keyStatement.getKeyProperties() != null) {
                String[] keyProperties = keyStatement.getKeyProperties();
                Configuration configuration = mappedStatement.getConfiguration();
                MetaObject metaObject = configuration.newMetaObject(parameter);
                Executor keyExecutor = configuration.newExecutor(executor.getTransaction());
                List<Object> values = keyExecutor.query(keyStatement, parameter, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER);
                if (values.size() == 0) {
                    throw new RuntimeException("SelectKey returned no data.");
                } else if (values.size() > 1) {
                    throw new RuntimeException("SelectKey returned more than one value.");
                } else {
                    MetaObject metaResult = configuration.newMetaObject(values.get(0));
                    if (keyProperties.length == 1) {
                        if (metaResult.hasGetter(keyProperties[0])) {
                            setValue(metaObject, keyProperties[0], metaResult.getValue(keyProperties[0]));
                        } else {
                            setValue(metaObject, keyProperties[0], values.get(0));
                        }
                    } else {
                        handleMultipleProperties(keyProperties, metaObject, metaResult);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error selecting key or setting result to parameter object. Cause: " + e);
        }

    }

    private void handleMultipleProperties(String[] keyProperties, MetaObject metaObject, MetaObject metaResult) {
        String[] keyColumns = keyStatement.getKeyColumns();
        if (keyColumns == null || keyColumns.length == 0) {
            for (String keyProperty : keyProperties) {
                setValue(metaObject, keyProperty, metaResult.getValue(keyProperty));
            }
        } else {
            if (keyColumns.length != keyProperties.length) {
                throw new RuntimeException("If SelectKey has key columns, the number must match the number of key properties.");
            }
            for (int i = 0; i < keyProperties.length; i++) {
                setValue(metaObject, keyProperties[i], metaResult.getValue(keyColumns[i]));
            }
        }
    }

    private void setValue(MetaObject metaParam, String property, Object value) {
        if (metaParam.hasSetter(property)) {
            metaParam.setValue(property, value);
        } else {
            throw new RuntimeException("No setter found for the keyProperty '" + property + "' in " +
                    metaParam.getOriginalObject().getClass().getName() + ".");
        }
    }
}
