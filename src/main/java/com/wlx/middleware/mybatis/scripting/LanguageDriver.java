package com.wlx.middleware.mybatis.scripting;

import com.wlx.middleware.mybatis.mapping.SqlSource;
import com.wlx.middleware.mybatis.session.Configuration;
import org.dom4j.Element;


/**
 * 脚本语言驱动
 */
public interface LanguageDriver {

    SqlSource createSqlSource(Configuration configuration, Element script, Class<?> parameterType);
}
