package com.wlx.middleware.mybatis.scripting.xmltags;

/**
 * SQL节点
 */
public interface SqlNode {

    boolean apply(DynamicContext context);
}
