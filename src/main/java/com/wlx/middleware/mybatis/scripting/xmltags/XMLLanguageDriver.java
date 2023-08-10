package com.wlx.middleware.mybatis.scripting.xmltags;

import com.wlx.middleware.mybatis.mapping.SqlSource;
import com.wlx.middleware.mybatis.scripting.LanguageDriver;
import com.wlx.middleware.mybatis.session.Configuration;
import org.dom4j.Element;


/**
 * XML语言驱动器
 */
public class XMLLanguageDriver implements LanguageDriver {
    @Override
    public SqlSource createSqlSource(Configuration configuration, Element script, Class<?> parameterType) {
        XMLScriptBuilder xmlScriptBuilder = new XMLScriptBuilder(configuration, script, parameterType);
        return xmlScriptBuilder.parseScriptNode();
    }
}
