package com.wlx.middleware.mybatis.datasource;

import javax.sql.DataSource;
import java.util.Properties;

public interface DataSourceFactory {

    void setProperties(Properties properties);

    DataSource getDataSource();

}
