package com.wlx.middleware.mybatis.datasource.pooled;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;

public class PooledConnection implements InvocationHandler {

    private static final String CLOSE = "close";
    private static final Class<?>[] IFACES = new Class<?>[]{Connection.class};
    private int hashcode = 0;
    private PooledDataSource pooledDataSource;
    private Connection realConnection;
    private Connection proxyConnection;
    // 检查连接时间戳
    private long checkoutTimestamp;
    // 创建连接时间戳
    private long createdTimestamp;
    // 上次使用时间戳
    private long lastUsedTimestamp;
    // 连接hashcode
    private int connectionTypeCode;
    // 是否有效
    private boolean valid;

    public PooledConnection(Connection connection, PooledDataSource dataSource) {
        this.hashcode = connection.hashCode();
        this.pooledDataSource = dataSource;
        this.realConnection = connection;
        this.proxyConnection = (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), IFACES, this);
        this.createdTimestamp = System.currentTimeMillis();
        this.lastUsedTimestamp = System.currentTimeMillis();
        this.valid = true;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (CLOSE.hashCode() == method.getName().hashCode() && CLOSE.equals(method.getName())) {
            pooledDataSource.pushConnection(this);
            return null;
        } else {
            if (!Object.class.equals(method.getDeclaringClass())) {
                checkConnection();
            }
            return method.invoke(realConnection, args);
        }
    }

    private void checkConnection() throws SQLException {
        if (!valid) {
            throw new SQLException("Error accessing PooledConnection. Connection is invalid.");
        }
    }

    @Override
    public int hashCode() {
        return hashcode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PooledConnection) {
            return realConnection.hashCode() == ((PooledConnection)obj).realConnection.hashCode();
        } else if (obj instanceof Connection) {
            return hashcode == obj.hashCode();
        } else {
            return false;
        }
    }

    public void invalidate() {
        this.valid = false;
    }

    public boolean isValid() {
        return this.valid && realConnection != null && pooledDataSource.pingConnection(this);
    }

    public Connection getRealConnection() {
        return realConnection;
    }

    public Connection getProxyConnection() {
        return proxyConnection;
    }

    public int getRealHashCode() {
        return realConnection.hashCode();
    }

    public long getTimeElapsedSinceLastUse() {
        return System.currentTimeMillis() - this.lastUsedTimestamp;
    }

    public long getAge() {
        return System.currentTimeMillis() - this.createdTimestamp;
    }

    public long getCheckoutTime() {
        return System.currentTimeMillis() - this.checkoutTimestamp;
    }

    public int getConnectionTypeCode() {
        return connectionTypeCode;
    }

    public void setConnectionTypeCode(int connectionTypeCode) {
        this.connectionTypeCode = connectionTypeCode;
    }

    public long getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(long createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public long getLastUsedTimestamp() {
        return lastUsedTimestamp;
    }

    public void setLastUsedTimestamp(long lastUsedTimestamp) {
        this.lastUsedTimestamp = lastUsedTimestamp;
    }

    public long getCheckoutTimestamp() {
        return checkoutTimestamp;
    }

    public void setCheckoutTimestamp(long checkoutTimestamp) {
        this.checkoutTimestamp = checkoutTimestamp;
    }

}
