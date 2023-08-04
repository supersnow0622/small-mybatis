package com.wlx.middleware.mybatis.builder.xml;

import com.wlx.middleware.mybatis.builder.BaseBuilder;
import com.wlx.middleware.mybatis.datasource.DataSourceFactory;
import com.wlx.middleware.mybatis.datasource.druid.DruidDataSourceFactory;
import com.wlx.middleware.mybatis.io.Resources;
import com.wlx.middleware.mybatis.mapping.BoundSql;
import com.wlx.middleware.mybatis.mapping.Environment;
import com.wlx.middleware.mybatis.mapping.MappedStatement;
import com.wlx.middleware.mybatis.mapping.SqlCommandType;
import com.wlx.middleware.mybatis.session.Configuration;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;

import javax.sql.DataSource;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XMLConfigBuilder extends BaseBuilder {
    private Element root;

    public XMLConfigBuilder(Reader reader) {
        super(new Configuration());
        SAXReader saxReader = new SAXReader();
        try {
            Document document = saxReader.read(new InputSource(reader));
            root = document.getRootElement();
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }
    }

    public Configuration parse() {
        try {
            environmentsElement(root.element("environments"));
            mapperElement(root.element("mappers"));
        } catch (Exception e) {
            throw new RuntimeException("Error parsing SQL Mapper Configuration. Cause: " + e, e);
        }
        return configuration;
    }

    private void environmentsElement(Element environments) throws Exception {
        List<Element> environmentList = environments.elements("environment");
        for (Element environmentElement : environmentList) {
            String id = environmentElement.attributeValue("id");
            Element dataSourceElement = environmentElement.element("dataSource");

            String type = dataSourceElement.attributeValue("type");
            DataSourceFactory dataSourceFactory = (DataSourceFactory) typeAliasRegistry.resolveAlias(type).newInstance();
            Properties props = new Properties();
            List<Element> properties = dataSourceElement.elements("property");
            for (Element property : properties) {
                props.setProperty(property.attributeValue("name"), property.attributeValue("value"));
            }
            dataSourceFactory.setProperties(props);
            DataSource dataSource = dataSourceFactory.getDataSource();

            Environment.Builder environmentBuilder = new Environment.Builder(id).dataSource(dataSource);

            Environment environment = environmentBuilder.build();
            configuration.setEnvironment(environment);
        }
    }

    private void mapperElement(Element mappers) throws Exception {
        List<Element> mapperList = mappers.elements("mapper");
        for (Element mapper : mapperList) {
            String resource = mapper.attributeValue("resource");
            Reader reader = Resources.getResourceAsReader(resource);
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(new InputSource(reader));
            Element mapperRoot = document.getRootElement();
            // 命名空间
            String namespace = mapperRoot.attributeValue("namespace");
            List<Element> selectNodes = mapperRoot.elements("select");
            for (Element node : selectNodes) {
                String id = node.attributeValue("id");
                String parameterType = node.attributeValue("parameterType");
                String resultType = node.attributeValue("resultType");
                String sql = node.getText();

                Map<Integer, String> parameter = new HashMap<>();
                Pattern pattern = Pattern.compile("(#\\{(.*?)})");
                Matcher matcher = pattern.matcher(sql);
                for (int i = 1; matcher.find(); i++) {
                    String group1 = matcher.group(1);
                    String group2 = matcher.group(2);
                    parameter.put(i, group2);
                    sql = sql.replace(group1, "?");
                }

                String msId = namespace + "." + id;
                String nodeName = node.getName();
                SqlCommandType sqlCommandType = SqlCommandType.valueOf(nodeName.toUpperCase());
                BoundSql boundSql = new BoundSql(parameterType, parameter, resultType, sql);
                MappedStatement mappedStatement = new MappedStatement.Builder(msId, boundSql, sqlCommandType).build();

                configuration.addMappedStatement(mappedStatement);
            }

            configuration.addMapper(Resources.classForName(namespace));
        }
    }

}
