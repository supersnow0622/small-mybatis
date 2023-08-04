package com.wlx.middleware.mybatis.test.dao;

import com.wlx.middleware.mybatis.test.po.User;

public interface IUserDao {

    User queryUserInfoById(Long uId);

}
