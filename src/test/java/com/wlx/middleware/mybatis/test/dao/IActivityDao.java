package com.wlx.middleware.mybatis.test.dao;

import com.wlx.middleware.mybatis.test.po.Activity;

public interface IActivityDao {

    Activity queryActivityById(Long activityId);

    Integer insert(Activity activity);

}