package com.wlx.middleware.mybatis.reflection;

import com.wlx.middleware.mybatis.reflection.factory.ObjectFactory;
import com.wlx.middleware.mybatis.reflection.property.PropertyTokenizer;
import com.wlx.middleware.mybatis.reflection.wrapper.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class MetaObject {

    // 原对象
    private Object originalObject;
    // 对象包装器
    private ObjectWrapper objectWrapper;
    // 对象工厂
    private ObjectFactory objectFactory;
    // 对象包装工厂
    private ObjectWrapperFactory objectWrapperFactory;

    public MetaObject(Object object, ObjectFactory objectFactory, ObjectWrapperFactory objectWrapperFactory) {
        this.originalObject = object;
        this.objectFactory = objectFactory;
        this.objectWrapperFactory = objectWrapperFactory;

        if (object instanceof ObjectWrapper) {
            this.objectWrapper = (ObjectWrapper) object;
        } else if (objectWrapperFactory.hasWrapperFor(object)) {
            this.objectWrapper = objectWrapperFactory.getWrapperFor(this, object);
        } else if (object instanceof Map) {
            this.objectWrapper = new MapWrapper(this, (Map<String, Object>) object);
        } else if (object instanceof Collection) {
            this.objectWrapper = new CollectionWrapper(this, (Collection<Object>) object);
        } else {
            this.objectWrapper = new BeanWrapper(this, object);
        }
    }

    public static MetaObject forObject(Object object, ObjectFactory defaultObjectFactory, ObjectWrapperFactory defaultObjectWrapperFactory) {
        if (object == null) {
            return SystemMetaObject.NULL_META_OBJECT;
        }
        return new MetaObject(object, defaultObjectFactory, defaultObjectWrapperFactory);
    }

    /**
     * 获取属性对应的值
     * @param name 属性名称
     * @return
     */
    public Object getValue(String name) {
        PropertyTokenizer prop = new PropertyTokenizer(name);
        // 如果有多级，递归获取，比如：class[1].student.score
        if (prop.hasNext()) {
            // 获取属性对应的元对象
            MetaObject metaObject = metaObjectForProperty(prop.getIndexedName());
            if (metaObject == SystemMetaObject.NULL_META_OBJECT) {
                return null;
            }
            return metaObject.getValue(prop.getChildren());
        }
        // 没有多级，直接调用对应的对象包装器获取属性值
        return objectWrapper.get(prop);
    }

    /**
     * 设置属性对应的值
     * @param name
     * @param value
     */
    public void setValue(String name, Object value) {
        PropertyTokenizer prop = new PropertyTokenizer(name);
        if (prop.hasNext()) {
            // 如果有多级，递归设置，比如：class[1].student.score
            MetaObject metaObject = metaObjectForProperty(prop.getIndexedName());
            if (metaObject == SystemMetaObject.NULL_META_OBJECT) {
                // 比如需要设置class[1].student为null，但class[1]不存在，那么无需设置，直接返回；
                if (value == null) {
                    return;
                }
                // 如果设置值不为空，需要把上层数据new出来
                metaObject = objectWrapper.instantiatePropertyValue(name, prop, objectFactory);
            }
            metaObject.setValue(prop.getChildren(), value);
        } else {
            objectWrapper.set(prop, value);
        }
    }

    // 获取属性对应的元对象
    public MetaObject metaObjectForProperty(String indexedName) {
        Object value = getValue(indexedName);
        return MetaObject.forObject(value, objectFactory, objectWrapperFactory);
    }

    public ObjectFactory getObjectFactory() {
        return objectFactory;
    }

    public ObjectWrapperFactory getObjectWrapperFactory() {
        return objectWrapperFactory;
    }

    /* --------以下方法都是委派给 ObjectWrapper------ */
    // 查找属性
    public String findProperty(String propName, boolean useCamelCaseMapping) {
        return objectWrapper.findProperty(propName, useCamelCaseMapping);
    }

    // 取得getter的名字列表
    public String[] getGetterNames() {
        return objectWrapper.getGetterNames();
    }

    // 取得setter的名字列表
    public String[] getSetterNames() {
        return objectWrapper.getSetterNames();
    }

    // 取得setter的类型列表
    public Class<?> getSetterType(String name) {
        return objectWrapper.getSetterType(name);
    }

    // 取得getter的类型列表
    public Class<?> getGetterType(String name) {
        return objectWrapper.getGetterType(name);
    }

    //是否有指定的setter
    public boolean hasSetter(String name) {
        return objectWrapper.hasSetter(name);
    }

    // 是否有指定的getter
    public boolean hasGetter(String name) {
        return objectWrapper.hasGetter(name);
    }

    public ObjectWrapper getObjectWrapper() {
        return objectWrapper;
    }

    // 是否是集合
    public boolean isCollection() {
        return objectWrapper.isCollection();
    }

    // 添加属性
    public void add(Object element) {
        objectWrapper.add(element);
    }

    // 添加属性
    public <E> void addAll(List<E> list) {
        objectWrapper.addAll(list);
    }

    public Object getOriginalObject() {
        return originalObject;
    }
}
