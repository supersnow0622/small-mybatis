package com.wlx.middleware.mybatis.builder.xml;

import com.wlx.middleware.mybatis.builder.BaseBuilder;
import com.wlx.middleware.mybatis.builder.MapperBuilderAssistant;
import com.wlx.middleware.mybatis.executor.keygen.Jdbc3KeyGenerator;
import com.wlx.middleware.mybatis.executor.keygen.KeyGenerator;
import com.wlx.middleware.mybatis.executor.keygen.NoKeyGenerator;
import com.wlx.middleware.mybatis.executor.keygen.SelectKeyGenerator;
import com.wlx.middleware.mybatis.mapping.MappedStatement;
import com.wlx.middleware.mybatis.mapping.SqlCommandType;
import com.wlx.middleware.mybatis.mapping.SqlSource;
import com.wlx.middleware.mybatis.scripting.LanguageDriver;
import com.wlx.middleware.mybatis.scripting.LanguageDriverRegistry;
import com.wlx.middleware.mybatis.scripting.xmltags.XMLLanguageDriver;
import com.wlx.middleware.mybatis.session.Configuration;
import org.dom4j.Element;

import java.util.List;

/**
 * XML语句构建器
 */
public class XMLStatementBuilder extends BaseBuilder {

    private MapperBuilderAssistant builderAssistant;
    private Element element;

    public XMLStatementBuilder(Configuration configuration, MapperBuilderAssistant builderAssistant, Element element) {
        super(configuration);
        this.element = element;
        this.builderAssistant = builderAssistant;
    }

    public void parseStatementNode() {
        String id = element.attributeValue("id");

        String parameterType = element.attributeValue("parameterType");
        Class<?> parameterTypeClass = resolveAlias(parameterType);

        String resultMap = element.attributeValue("resultMap");
        String resultType = element.attributeValue("resultType");
        Class<?> resultTypeClass = resolveAlias(resultType);

        String name = element.getName();
        SqlCommandType sqlCommandType = SqlCommandType.valueOf(name.toUpperCase());

        LanguageDriverRegistry languageRegistry = configuration.getLanguageRegistry();
        LanguageDriver driver = languageRegistry.getDriver(XMLLanguageDriver.class);

        // 解析<selectKey>,放入configuration中
        processSelectKeyNodes(id, parameterTypeClass, driver);

        KeyGenerator keyGenerator = null;
        String keyProperty = element.attributeValue("keyProperty");
        String keyStatementId = builderAssistant.applyCurrentNamespace(id + SelectKeyGenerator.SELECT_KEY_SUFFIX, false);
        if (configuration.hasGenerator(keyStatementId)) {
            keyGenerator = configuration.getKeyGenerator(keyStatementId);
        } else {
            keyGenerator = configuration.isUseGeneratedKeys() && SqlCommandType.SELECT.equals(sqlCommandType)
                    ? new Jdbc3KeyGenerator() : new NoKeyGenerator();
        }

        SqlSource sqlSource = driver.createSqlSource(configuration, element, parameterTypeClass);

        builderAssistant.addMappedStatement(id, sqlSource, sqlCommandType, resultMap, resultTypeClass, keyGenerator, keyProperty);
    }

    private void processSelectKeyNodes(String id, Class<?> parameterTypeClass, LanguageDriver driver) {
        List<Element> selectKeyNodes = element.elements("selectKey");
        for (Element selectKeyNode : selectKeyNodes) {
            id = id + SelectKeyGenerator.SELECT_KEY_SUFFIX;
            parseSelectKeyNode(id, selectKeyNode, parameterTypeClass, driver);
        }
    }

    private void parseSelectKeyNode(String id, Element selectKeyNode, Class<?> parameterTypeClass, LanguageDriver driver) {
        String resultType = selectKeyNode.attributeValue("resultType");
        Class<?> resultTypeClass = resolveClass(resultType);
        boolean executeBefore = "BEFORE".equals(selectKeyNode.attributeValue("order", "AFTER"));
        String keyProperty = selectKeyNode.attributeValue("keyProperty");

        KeyGenerator keyGenerator = new NoKeyGenerator();

        SqlSource sqlSource = driver.createSqlSource(configuration, selectKeyNode, parameterTypeClass);
        SqlCommandType sqlCommandType = SqlCommandType.SELECT;

        builderAssistant.addMappedStatement(id, sqlSource, sqlCommandType, null, resultTypeClass,
                keyGenerator, keyProperty);
        id = builderAssistant.applyCurrentNamespace(id, false);

        MappedStatement keyStatement = configuration.getMappedStatement(id);
        configuration.addKeyGenerator(id, new SelectKeyGenerator(executeBefore, keyStatement));
    }
}
