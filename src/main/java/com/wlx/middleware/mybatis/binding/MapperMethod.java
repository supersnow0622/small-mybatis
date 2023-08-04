package com.wlx.middleware.mybatis.binding;

import com.wlx.middleware.mybatis.mapping.MappedStatement;
import com.wlx.middleware.mybatis.mapping.SqlCommandType;
import com.wlx.middleware.mybatis.session.Configuration;
import com.wlx.middleware.mybatis.session.SqlSession;

import java.lang.reflect.Method;

/**
 * 具体方法的执行
 */
public class MapperMethod {

    private SqlCommand command;

    public MapperMethod(Configuration configuration, Class<?> mapperInterface, Method method) {
        command = new SqlCommand(configuration, mapperInterface, method);
    }

    public Object execute(SqlSession sqlSession, Object args) {
        SqlCommandType sqlCommandType = command.getSqlCommandType();
        switch (sqlCommandType) {
            case INSERT: break;
            case DELETE: break;
            case UPDATE: break;
            case SELECT:
                return sqlSession.selectOne(command.getName(), args);
        }
        return null;
    }

    private static class SqlCommand {
        // namespace + id
        private String name;

        private SqlCommandType sqlCommandType;

        public SqlCommand(Configuration configuration, Class<?> mapperInterface, Method method) {
            String id = mapperInterface.getName() + "." + method.getName();
            MappedStatement mappedStatement = configuration.getMappedStatement(id);
            name = mappedStatement.getId();
            sqlCommandType = mappedStatement.getSqlCommandType();
        }

        public String getName() {
            return name;
        }

        public SqlCommandType getSqlCommandType() {
            return sqlCommandType;
        }

    }
}
