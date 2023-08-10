package com.wlx.middleware.mybatis.scripting.xmltags;

import com.wlx.middleware.mybatis.builder.BaseBuilder;
import com.wlx.middleware.mybatis.mapping.SqlSource;
import com.wlx.middleware.mybatis.scripting.defaults.RawSqlSource;
import com.wlx.middleware.mybatis.session.Configuration;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * XML脚本构建器
 */
public class XMLScriptBuilder extends BaseBuilder {

    private Element element;

    private boolean isDynamic;

    private Class<?> parameterType;

    public XMLScriptBuilder(Configuration configuration, Element element, Class<?> parameterType) {
        super(configuration);
        this.element = element;
        this.parameterType = parameterType;
    }

    public SqlSource parseScriptNode() {
        List<SqlNode> contents = parseDynamicTags(element);
        MixedSqlNode mixedSqlNode = new MixedSqlNode(contents);
        return new RawSqlSource(configuration, mixedSqlNode, parameterType);
    }

    private List<SqlNode> parseDynamicTags(Element element) {
        List<SqlNode> contents = new ArrayList<>();
        contents.add(new StaticTextSqlNode(element.getText()));
        return contents;
    }
}
