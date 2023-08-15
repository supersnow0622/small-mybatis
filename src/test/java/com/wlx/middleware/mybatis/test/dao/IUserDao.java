package com.wlx.middleware.mybatis.test.dao;

import com.wlx.middleware.mybatis.annotations.Delete;
import com.wlx.middleware.mybatis.annotations.Insert;
import com.wlx.middleware.mybatis.annotations.Select;
import com.wlx.middleware.mybatis.annotations.Update;
import com.wlx.middleware.mybatis.test.po.User;

import java.util.List;

public interface IUserDao {

    @Select(values = "SELECT id, userId, userName, userHead\n" +
            "FROM user\n" +
            "where id = #{id}")
    User queryUserInfoById(Long uId);

    @Select(values = "SELECT id, userId, userName, userHead\n" +
            "        FROM user\n" +
            "        where id = #{id}")
    User queryUserInfo(User req);

    @Select(values = "SELECT id, userId, userName, userHead\n" +
            "FROM user")
    List<User> queryUserInfoList();

    @Update(values = "UPDATE user\n" +
            "SET userName = #{userName}\n" +
            "WHERE id = #{id}")
    int updateUserInfo(User req);

    @Insert(values = "INSERT INTO user\n" +
            "(userId, userName, userHead, createTime, updateTime)\n" +
            "VALUES (#{userId}, #{userName}, #{userHead}, now(), now())")
    int insertUserInfo(User req);

    @Delete(values = "DELETE FROM user WHERE userId = #{userId}")
    int deleteUserInfoByUserId(String userId);

}
