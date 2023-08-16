package com.wlx.middleware.mybatis.test.dao;

import com.wlx.middleware.mybatis.test.po.Activity;

public interface IActivityDao {

    Activity queryActivityById(Activity activity);

    Integer insert(Activity activity);

}