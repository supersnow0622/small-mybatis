package com.wlx.middleware.mybatis.test.dao;

import com.wlx.middleware.mybatis.test.po.User;

import java.util.List;

public interface IUserDao {

    User queryUserInfoById(Long uId);

    User queryUserInfo(User req);

    List<User> queryUserInfoList();

    int updateUserInfo(User req);

    int insertUserInfo(User req);

    int deleteUserInfoByUserId(String userId);

}
