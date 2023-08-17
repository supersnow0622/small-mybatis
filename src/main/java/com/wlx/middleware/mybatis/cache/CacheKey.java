package com.wlx.middleware.mybatis.cache;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * 缓存 Key，一般缓存框架的数据结构基本上都是 Key->Value 方式存储
 * MyBatis 对于其 Key 的生成采取规则为：[mappedStatementId + offset + limit + SQL + queryParams + environment]生成一个哈希码
 */
public class CacheKey implements Cloneable, Serializable {

    private static final long serialVersionUID = -5591611338353286093L;

    private static final int DEFAULT_MULTIPLIER = 37;
    private static final int DEFAULT_HASHCODE = 17;

    private int multiplier;
    private int hashcode;
    private int checksum;
    private int count;
    private List<Object> updateList;

    public CacheKey() {
        this.hashcode = DEFAULT_HASHCODE;
        this.multiplier = DEFAULT_MULTIPLIER;
        this.count = 0;
        this.updateList = new ArrayList<>();
    }

    public CacheKey(Object[] objects) {
        this();
        updateAll(objects);
    }

    private void updateAll(Object[] objects) {
        for (Object object : objects) {
            update(object);
        }
    }

    public void update(Object object) {
        if (object != null && object.getClass().isArray()) {
            int length = Array.getLength(object);
            for (int i = 0; i < length; i++) {
                Object element = Array.get(object, i);
                doUpdate(element);
            }
        } else {
            doUpdate(object);
        }
    }

    private void doUpdate(Object object) {
        int baseHashCode = object == null ? 1 : object.hashCode();
        count++;
        checksum += baseHashCode;
        baseHashCode *= count;

        hashcode = multiplier * hashcode + baseHashCode;
        updateList.add(object);
    }

    @Override
    public int hashCode() {
        return hashcode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof CacheKey)) {
            return false;
        }

        CacheKey other = (CacheKey) obj;
        if (hashcode != other.hashcode) {
            return false;
        }
        if (count != other.count) {
            return false;
        }
        if (checksum != other.checksum) {
            return false;
        }

        for (int i = 0; i < updateList.size(); i++) {
            Object object = updateList.get(i);
            Object otherObject = other.updateList.get(i);
            if (object == null) {
                if (otherObject != null) {
                    return false;
                }
            } else {
                if (!object.equals(otherObject)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        CacheKey cloneCacheKey = (CacheKey) super.clone();
        cloneCacheKey.updateList = new ArrayList<>(updateList);
        return cloneCacheKey;
    }

    @Override
    public String toString() {
        StringBuilder returnValue = new StringBuilder().append(hashcode).append(":").append(checksum);
        for (Object object : updateList) {
            returnValue.append(":").append(object);
        }
        return returnValue.toString();
    }
}


