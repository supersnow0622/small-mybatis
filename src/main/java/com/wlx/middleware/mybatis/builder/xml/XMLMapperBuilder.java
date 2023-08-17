package com.wlx.middleware.mybatis.builder.xml;

import com.wlx.middleware.mybatis.builder.BaseBuilder;
import com.wlx.middleware.mybatis.builder.MapperBuilderAssistant;
import com.wlx.middleware.mybatis.cache.Cache;
import com.wlx.middleware.mybatis.io.Resources;
import com.wlx.middleware.mybatis.mapping.ResultFlag;
import com.wlx.middleware.mybatis.mapping.ResultMap;
import com.wlx.middleware.mybatis.mapping.ResultMapping;
import com.wlx.middleware.mybatis.session.Configuration;
import com.wlx.middleware.mybatis.builder.ResultMapResolver;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * XML映射构建器
 */
public class XMLMapperBuilder extends BaseBuilder {

    private Element element;

    private String resource;

    private MapperBuilderAssistant builderAssistant;

    public XMLMapperBuilder(Configuration configuration, InputStream inputStream, String resource) throws DocumentException {
        this(configuration, new SAXReader().read(inputStream), resource);
    }

    public XMLMapperBuilder(Configuration configuration, Document document, String resource) {
        super(configuration);
        this.element = document.getRootElement();
        this.resource = resource;
        this.builderAssistant = new MapperBuilderAssistant(configuration, resource);
    }


    public void parse() throws ClassNotFoundException {
        if (!configuration.isResourceLoaded(resource)) {
            configurationElement(element);
            configuration.addLoadedResource(resource);
            configuration.addMapper(Resources.classForName(builderAssistant.getCurrentNamespace()));
        }

    }

    private void configurationElement(Element element) {
        // 1.配置namespace
        String currentNamespace = element.attributeValue("namespace");
        if (currentNamespace.equals("")) {
            throw new RuntimeException("Mapper's namespace cannot be empty");
        }
        builderAssistant.setCurrentNamespace(currentNamespace);

        // 2.配置cache
        cacheElement(element.element("cache"));

        // 3.解析resultMap
        resultMapElements(element.elements("resultMap"));

        // 4.配置select|insert|update|delete
        buildStatementFromContext(element.elements("select"),
                element.elements("insert"),
                element.elements("update"),
                element.elements("delete"));
    }

    /**
     * <cache eviction="FIFO" flushInterval="600000" size="512" readOnly="true"/>
     */
    private void cacheElement(Element context) {
        if (context == null)
            return;
        String type = context.attributeValue("type", "PERPETUAL");
        Class<? extends Cache> typeClass = typeAliasRegistry.resolveAlias(type);
        String eviction = context.attributeValue("eviction", "FIFO");
        Class<? extends Cache> evictionClass = typeAliasRegistry.resolveAlias(eviction);
        Long flushInterval = Long.valueOf(context.attributeValue("flushInterval"));
        Integer size = Integer.valueOf(context.attributeValue("size"));
        boolean readWrite = !Boolean.parseBoolean(context.attributeValue("readOnly", "false"));
        boolean blocking = !Boolean.parseBoolean(context.attributeValue("blocking", "false"));

        // 解析额外属性信息；<property name="cacheFile" value="/tmp/xxx-cache.tmp"/>
        List<Element> elements = context.elements();
        Properties props = new Properties();
        for (Element element : elements) {
            props.setProperty(element.attributeValue("name"), element.attributeValue("value"));
        }

        builderAssistant.useNewCache(typeClass, evictionClass, flushInterval, size, readWrite, blocking, props);
    }

    private void resultMapElements(List<Element> resultMap) {
        for (Element element : resultMap) {
            String id = element.attributeValue("id");
            String type = element.attributeValue("type");
            Class<?> typeClass = resolveClass(type);

            List<ResultMapping> resultMappings = new ArrayList<>();
            List<Element> resultChildren = element.elements();
            for (Element resultChild : resultChildren) {
                List<ResultFlag> flags = new ArrayList<>();
                if ("id".equals(resultChild.getName())) {
                    flags.add(ResultFlag.ID);
                }

                String property = resultChild.attributeValue("property");
                String column = resultChild.attributeValue("column");
                resultMappings.add(builderAssistant.buildResultMapping(typeClass, property, column, flags));
            }

            // 创建结果映射解析器
            ResultMapResolver resultMapResolver = new ResultMapResolver(builderAssistant, id, typeClass, resultMappings);
            resultMapResolver.resolve();
        }
    }

    private void buildStatementFromContext(List<Element>... lists) {
        for (List<Element> list : lists) {
            for (Element element : list) {
                final XMLStatementBuilder statementBuilder = new XMLStatementBuilder(configuration, builderAssistant, element);
                statementBuilder.parseStatementNode();
            }
        }
    }
}
