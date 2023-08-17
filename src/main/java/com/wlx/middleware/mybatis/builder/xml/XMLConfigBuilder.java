package com.wlx.middleware.mybatis.builder.xml;

import com.wlx.middleware.mybatis.builder.BaseBuilder;
import com.wlx.middleware.mybatis.datasource.DataSourceFactory;
import com.wlx.middleware.mybatis.io.Resources;
import com.wlx.middleware.mybatis.mapping.Environment;
import com.wlx.middleware.mybatis.plugin.Interceptor;
import com.wlx.middleware.mybatis.session.Configuration;
import com.wlx.middleware.mybatis.session.LocalCacheScope;
import com.wlx.middleware.mybatis.transaction.TransactionFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;

import javax.sql.DataSource;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;
import java.util.Properties;

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
            // 添加插件
            pluginElement(root.element("plugins"));
            // 设置
            settingsElement(root.element("settings"));
            // 环境
            environmentsElement(root.element("environments"));
            // 解析映射器
            mapperElement(root.element("mappers"));
        } catch (Exception e) {
            throw new RuntimeException("Error parsing SQL Mapper Configuration. Cause: " + e, e);
        }
        return configuration;
    }

    private void settingsElement(Element context) {
        if (context == null)
            return;
        List<Element> elements = context.elements();
        Properties properties = new Properties();
        for (Element element : elements) {
            properties.setProperty(element.attributeValue("name"), element.attributeValue("value"));
        }
        configuration.setLocalCacheScope(LocalCacheScope.valueOf(properties.getProperty("localCacheScope")));
    }

    private void pluginElement(Element plugins) throws Exception {
        if (plugins == null)
            return;
        List<Element> elements = plugins.elements();
        for (Element element : elements) {
            String interceptor = element.attributeValue("interceptor");
            List<Element> propertyElementList = element.elements("property");

            Properties properties = new Properties();
            for (Element property : propertyElementList) {
                properties.setProperty(property.attributeValue("name"), property.attributeValue("value"));
            }

            Interceptor interceptorInstance = (Interceptor) resolveClass(interceptor).newInstance();
            interceptorInstance.setProperties(properties);
            configuration.addInterceptor(interceptorInstance);
        }
    }

    private void environmentsElement(Element environments) throws Exception {
        String environment = environments.attributeValue("default");

        List<Element> environmentList = environments.elements("environment");
        for (Element environmentElement : environmentList) {
            String id = environmentElement.attributeValue("id");
            if (environment.equals(id)) {
                // 事务管理器
                TransactionFactory transactionFactory = (TransactionFactory)
                        typeAliasRegistry.resolveAlias(environmentElement.element("transactionManager").attributeValue("type")).newInstance();

                // 数据源
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

                // 构建环境
                Environment.Builder environmentBuilder = new Environment.Builder(id)
                        .transactionFactory(transactionFactory).dataSource(dataSource);

                configuration.setEnvironment(environmentBuilder.build());
            }
        }
    }

    private void mapperElement(Element mappers) throws Exception {
        List<Element> mapperList = mappers.elements("mapper");
        for (Element mapper : mapperList) {
            String resource = mapper.attributeValue("resource");
            String mapperClass = mapper.attributeValue("class");

            if (resource != null && mapperClass == null) {
                InputStream inputStream = Resources.getResourceAsStream(resource);

                // 在for循环里每个mapper都重新new一个XMLMapperBuilder，来解析
                XMLMapperBuilder mapperBuilder = new XMLMapperBuilder(configuration, inputStream, resource);
                mapperBuilder.parse();
            } else if (resource == null && mapperClass != null) {
                Class<?> mapperInterface = Resources.classForName(mapperClass);
                configuration.addMapper(mapperInterface);
            }
        }

    }

}
