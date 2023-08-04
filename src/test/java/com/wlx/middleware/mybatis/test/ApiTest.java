package com.wlx.middleware.mybatis.test;

import com.alibaba.fastjson.JSON;
import com.wlx.middleware.mybatis.io.Resources;
import com.wlx.middleware.mybatis.session.SqlSession;
import com.wlx.middleware.mybatis.session.SqlSessionFactory;
import com.wlx.middleware.mybatis.session.SqlSessionFactoryBuilder;
import com.wlx.middleware.mybatis.test.dao.IUserDao;
import com.wlx.middleware.mybatis.test.po.User;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;

public class ApiTest {

    private Logger logger = LoggerFactory.getLogger(ApiTest.class);

    @Test
    public void test_SqlSessionFactory() throws IOException {
        Reader reader = Resources.getResourceAsReader("mybatis-config-datasource.xml");
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
        SqlSession sqlSession = sqlSessionFactory.openSession();

        IUserDao userDao = sqlSession.getMapper(IUserDao.class);

        User user = userDao.queryUserInfoById(1L);
        logger.info("测试结果：{}", JSON.toJSONString(user));
    }
}
