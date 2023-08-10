package com.wlx.middleware.mybatis.builder.xml;

import com.wlx.middleware.mybatis.builder.BaseBuilder;
import com.wlx.middleware.mybatis.io.Resources;
import com.wlx.middleware.mybatis.session.Configuration;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.util.List;

/**
 * XML映射构建器
 */
public class XMLMapperBuilder extends BaseBuilder {

    private Element element;

    private String resource;

    private String currentNamespace;

    public XMLMapperBuilder(Configuration configuration, InputStream inputStream, String resource) throws DocumentException {
        this(configuration, new SAXReader().read(inputStream), resource);
    }

    public XMLMapperBuilder(Configuration configuration, Document document, String resource) {
        super(configuration);
        this.element = document.getRootElement();
        this.resource = resource;
    }


    public void parse() throws ClassNotFoundException {
        if (!configuration.isResourceLoaded(resource)) {
            configurationElement(element);
            configuration.addLoadedResource(resource);
            configuration.addMapper(Resources.classForName(currentNamespace));
        }

    }

    private void configurationElement(Element element) {
        currentNamespace = element.attributeValue("namespace");
        if (currentNamespace.equals("")) {
            throw new RuntimeException("Mapper's namespace cannot be empty");
        }

        buildStatementFromContext(element.elements("select"));
    }

    private void buildStatementFromContext(List<Element> list) {
        for (Element element : list) {
            final XMLStatementBuilder statementBuilder = new XMLStatementBuilder(configuration, element, currentNamespace);
            statementBuilder.parseStatementNode();
        }
    }
}
