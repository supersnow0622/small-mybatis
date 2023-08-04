package com.wlx.middleware.mybatis.session;

/**
 * 用来执行SQL，获取映射器，管理事务。
 */
public interface SqlSession {

    <T> T selectOne(String statement);

    <T> T selectOne(String statement, Object params);

    <T> T getMapper(Class<T> type);

    Configuration getConfiguration();
}
