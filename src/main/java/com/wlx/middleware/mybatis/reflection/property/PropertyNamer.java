package com.wlx.middleware.mybatis.reflection.property;

/**
 * 属性命名器
 */
public class PropertyNamer {

    private PropertyNamer() {
    }

    /**
     * 方法转为属性
     * @param methodName
     * @return
     */
    public static String methodToProperty(String methodName) {
        String propName;
        if (methodName.startsWith("is")) {
            propName = methodName.substring(2);
        } else if (methodName.startsWith("get") || methodName.startsWith("set")) {
            propName = methodName.substring(3);
        } else {
            throw new RuntimeException("Error parsing property name '\" + name + \"'.  Didn't start with 'is', 'get' or 'set'.");
        }

        if (propName.length() == 1 || (propName.length() > 1 && !Character.isUpperCase(propName.charAt(1)))) {
            propName = propName.substring(0, 1).toLowerCase() + propName.substring(1);
        }
        return propName;
    }
}
