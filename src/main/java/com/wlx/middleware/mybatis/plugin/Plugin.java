package com.wlx.middleware.mybatis.plugin;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 代理模式插件
 */
public class Plugin implements InvocationHandler {

    private Object target;
    private Interceptor interceptor;
    private Map<Class<?>, Set<Method>> signatureMap;

    public Plugin(Object target, Interceptor interceptor, Map<Class<?>, Set<Method>> signatureMap) {
        this.target = target;
        this.interceptor = interceptor;
        this.signatureMap = signatureMap;
    }

    /**
     * 用代理把自定义插件行为包裹到目标方法中，也就是 Plugin.invoke 的过滤调用
     * @param target
     * @param interceptor
     * @return
     */
    public static Object wrap(Object target, Interceptor interceptor) {
        Map<Class<?>, Set<Method>> signatureMap = getSignatureMap(interceptor);
        Class<?> type = target.getClass();
        Class<?>[] interfaces = getAllInterfaces(type, signatureMap);
        if (interfaces.length > 0) {
            return Proxy.newProxyInstance(type.getClassLoader(), interfaces,
                    new Plugin(target, interceptor, signatureMap));
        }
        return target;
    }

    private static Map<Class<?>, Set<Method>> getSignatureMap(Interceptor interceptor) {
        Intercepts interceptsAnnotation = interceptor.getClass().getAnnotation(Intercepts.class);
        if (interceptsAnnotation == null) {
            throw new RuntimeException("No @Intercepts annotation was found in interceptor " + interceptor.getClass().getName());
        }

        Map<Class<?>, Set<Method>> signatureMap = new HashMap<>();
        Signature[] sigs = interceptsAnnotation.value();
        for (Signature sig : sigs) {
            Set<Method> methods = signatureMap.computeIfAbsent(sig.type(), k -> new HashSet<>());
            try {
                Method method = sig.type().getMethod(sig.method(), sig.args());
                methods.add(method);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        return signatureMap;
    }

    private static Class<?>[] getAllInterfaces(Class<?> type, Map<Class<?>, Set<Method>> signatureMap) {
        Set<Class<?>> interfaces = new HashSet<>();
        while (type != null) {
            for (Class<?> c : type.getInterfaces()) {
                if (signatureMap.containsKey(c)) {
                    interfaces.add(c);
                }
            }
            type = type.getSuperclass();
        }
        return interfaces.toArray(new Class<?>[interfaces.size()]);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Set<Method> methods = signatureMap.get(method.getDeclaringClass());
        if (methods != null && methods.contains(method)) {
            return interceptor.intercept(new Invocation(target, method, args));
        }
        return method.invoke(target, args);
    }
}
