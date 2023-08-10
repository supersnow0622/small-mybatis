package com.wlx.middleware.mybatis.builder.xml;

import com.wlx.middleware.mybatis.builder.BaseBuilder;
import com.wlx.middleware.mybatis.mapping.MappedStatement;
import com.wlx.middleware.mybatis.mapping.SqlCommandType;
import com.wlx.middleware.mybatis.mapping.SqlSource;
import com.wlx.middleware.mybatis.scripting.LanguageDriver;
import com.wlx.middleware.mybatis.scripting.LanguageDriverRegistry;
import com.wlx.middleware.mybatis.scripting.xmltags.XMLLanguageDriver;
import com.wlx.middleware.mybatis.session.Configuration;
import org.dom4j.Element;

/**
 * XML语句构建器
 */
public class XMLStatementBuilder extends BaseBuilder {

    private Element element;

    private String currentNamespace;

    public XMLStatementBuilder(Configuration configuration, Element element, String currentNamespace) {
        super(configuration);
        this.element = element;
        this.currentNamespace = currentNamespace;
    }

    public void parseStatementNode() {
        String id = element.attributeValue("id");

        String parameterType = element.attributeValue("parameterType");
        Class<?> parameterTypeClass = resolveAlias(parameterType);

        String resultType = element.attributeValue("resultType");
        Class<?> resultTypeClass = resolveAlias(resultType);

        String name = element.getName();
        SqlCommandType sqlCommandType = SqlCommandType.valueOf(name.toUpperCase());

        LanguageDriverRegistry languageRegistry = configuration.getLanguageRegistry();
        LanguageDriver driver = languageRegistry.getDriver(XMLLanguageDriver.class);

        SqlSource sqlSource = driver.createSqlSource(configuration, element, parameterTypeClass);

        MappedStatement.Builder builder = new MappedStatement.Builder(configuration,
                currentNamespace + "." + id, sqlCommandType, sqlSource, resultTypeClass);
        configuration.addMappedStatement(builder.build());
    }
}
