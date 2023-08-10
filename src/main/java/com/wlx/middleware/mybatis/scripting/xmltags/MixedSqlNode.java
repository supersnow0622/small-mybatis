package com.wlx.middleware.mybatis.scripting.xmltags;

import java.util.List;

/**
 * 混合SQL节点
 */
public class MixedSqlNode implements SqlNode {

    //组合模式，拥有一个SqlNode的List
    private List<SqlNode> contents;

    public MixedSqlNode(List<SqlNode> contents) {
        this.contents = contents;
    }

    @Override
    public boolean apply(DynamicContext context) {
        contents.forEach(x -> x.apply(context));
        return true;
    }
}
