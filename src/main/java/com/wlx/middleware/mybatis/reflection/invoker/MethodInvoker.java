package com.wlx.middleware.mybatis.reflection.invoker;

import java.lang.reflect.Method;

public class MethodInvoker implements Invoker {

    private Method method;

    private Class<?> type;

    public MethodInvoker(Method method) {
        this.method = method;

        if (method.getParameterTypes().length == 1) {
            this.type = method.getParameterTypes()[0];
        } else {
            this.type = method.getReturnType();
        }
    }

    @Override
    public Object invoke(Object target, Object[] args) throws Exception {
        return method.invoke(target, args);
    }

    @Override
    public Class<?> getType() {
        return type;
    }
}
