package com.wlx.middleware.mybatis.datasource.pooled;

import com.wlx.middleware.mybatis.datasource.unpooled.UnpooledDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.*;


public class PooledDataSource implements DataSource {

    private Logger logger = LoggerFactory.getLogger(PooledDataSource.class);

    private final UnpooledDataSource dataSource;

    // 最大活跃连接数
    protected int poolMaximumActiveConnections = 10;
    // 最大空闲连接数
    protected int poolMaximumIdleConnections = 5;
    // 在被强制回收前，连接被检查的最大时间
    protected int poolMaximumCheckoutTime = 20000;
    private int expectedConnectionTypeCode;
    // ping检测发送内容
    protected String poolPingQuery = "NO PING QUERY SET";
    // ping检测是否开启
    protected boolean poolPingEnabled = false;
    // ping检测多久进行一次
    protected int poolPingConnectionsNotUsedFor = 0;
    // 等待获取连接的时间
    protected int poolTimeToWait = 20000;

    private final PoolState poolState = new PoolState(this);

    public PooledDataSource() {
        this.dataSource = new UnpooledDataSource();
    }

    // 回收连接
    public void pushConnection(PooledConnection connection) throws SQLException {
        synchronized (poolState) {
            poolState.activeConnections.remove(connection);
            if (connection.isValid()) {
                poolState.accumulatedCheckoutTime += connection.getCheckoutTime();
                if (!connection.getRealConnection().getAutoCommit()) {
                    connection.getRealConnection().rollback();
                }
                // 空闲连接数少于最大空闲连接数，则创建新的连接
                if (poolState.idleConnections.size() < poolMaximumIdleConnections &&
                        connection.getConnectionTypeCode() == expectedConnectionTypeCode) {
                    PooledConnection newConnection = new PooledConnection(connection.getRealConnection(), this);
                    poolState.idleConnections.add(newConnection);
                    newConnection.setCreatedTimestamp(connection.getCreatedTimestamp());
                    newConnection.setLastUsedTimestamp(connection.getLastUsedTimestamp());
                    connection.invalidate();
                    logger.info("Returned connection " + newConnection.getRealHashCode() + "to Pool.");
                } else {
                    connection.getRealConnection().close();
                    logger.info("Closed connection " + connection.getRealHashCode() + ".");
                    connection.invalidate();
                }
            } else {
                logger.info("A bad connection (" + connection.getRealHashCode() + ") attempted to return to the pool, discarding connection.");
                poolState.badConnectionCount++;
            }
        }
    }

    // 获取连接
    private PooledConnection popConnection(String username, String password) throws SQLException {
        long requestStartTime = System.currentTimeMillis();
        PooledConnection connection = null;
        boolean countedWait = false;
        int localBadConnectionCount = 0;

        while (connection == null) {
            synchronized (poolState) {
                // 有空闲连接，返回第一个；无空闲连接，创建新的连接
                if (!poolState.idleConnections.isEmpty()) {
                    connection = poolState.idleConnections.get(0);
                    logger.info("Checked out connection " + connection.getRealHashCode() + " from pool.");
                } else {
                    // 活跃数不足创建新连接；活跃数满了检查是否有超时的连接，如果有则回收超时连接创建新连接；如果没有等待释放连接
                    if (poolState.activeConnections.size() < poolMaximumActiveConnections) {
                        connection = new PooledConnection(dataSource.getConnection(), this);
                        logger.info("Created connection " + connection.getRealHashCode() + ".");
                    } else {
                        PooledConnection oldestActiveConnection = poolState.activeConnections.get(0);
                        long longestCheckoutTime = oldestActiveConnection.getCheckoutTime();
                        if (longestCheckoutTime > poolMaximumCheckoutTime) {
                            poolState.claimedOverdueConnectionCount++;
                            poolState.accumulatedCheckoutTimeOfOverdueConnections += longestCheckoutTime;
                            poolState.accumulatedCheckoutTime += longestCheckoutTime;
                            poolState.activeConnections.remove(oldestActiveConnection);
                            if (!oldestActiveConnection.getRealConnection().getAutoCommit()) {
                                oldestActiveConnection.getRealConnection().rollback();
                            }
                            connection = new PooledConnection(oldestActiveConnection.getRealConnection(), this);
                            oldestActiveConnection.invalidate();
                            logger.info("Claimed overdue connection " + connection.getRealHashCode() + ".");
                        } else {
                            try {
                                if (!countedWait) {
                                    poolState.hadToWaitCount++;
                                    countedWait = true;
                                }
                                logger.info("Waiting as long as " + poolTimeToWait + " milliseconds for connection.");
                                long beforeWaitTimestamp = System.currentTimeMillis();
                                poolState.wait(poolTimeToWait);
                                poolState.accumulatedWaitTime += System.currentTimeMillis() - beforeWaitTimestamp;
                            } catch (InterruptedException e) {
                                break;
                            }
                        }
                    }
                }

                // 连接不为空时，检查连接是否有效，有效则设置检查时间戳，使用时间戳，累加请求时间和次数，添加活跃连接；连接无效则计算坏的连接数
                if (connection != null) {
                    if (connection.isValid()) {
                        connection.setConnectionTypeCode(assembleConnectionTypeCode(dataSource.getUrl(), username, password));
                        connection.setCheckoutTimestamp(System.currentTimeMillis());
                        connection.setLastUsedTimestamp(System.currentTimeMillis());
                        poolState.activeConnections.add(connection);
                        poolState.requestCount++;
                        poolState.accumulatedRequestTime += System.currentTimeMillis() - requestStartTime;
                    } else {
                        logger.info("A bad connection (" + connection.getRealHashCode() + ") was returned from the pool, getting another connection.");
                        poolState.badConnectionCount++;
                        localBadConnectionCount++;
                        connection = null;
                        if (localBadConnectionCount > (poolMaximumIdleConnections + 3)) {
                            logger.debug("PooledDataSource: Could not get a good connection to the database.");
                            throw new SQLException("PooledDataSource: Could not get a good connection to the database.");
                        }
                    }
                }
            }
        }

        if (connection == null) {
            logger.debug("PooledDataSource: Unknown severe error condition.  The connection pool returned a null connection.");
            throw new SQLException("PooledDataSource: Unknown severe error condition.  The connection pool returned a null connection.");
        }

        return connection;
    }

    // 强制回收所有的连接
    public void forceCloseAll() {
        synchronized (poolState) {
            // 关闭活跃连接
            for (int i = poolState.activeConnections.size(); i > 0; i--) {
                try {
                    PooledConnection connection = poolState.activeConnections.get(i);
                    connection.invalidate();

                    Connection realConnection = connection.getRealConnection();
                    if (!realConnection.getAutoCommit()) {
                        realConnection.rollback();
                    }
                    realConnection.close();
                } catch (Exception e) {

                }
            }

            // 关闭空闲连接
            for (int i = poolState.idleConnections.size(); i > 0; i--) {
                try {
                    PooledConnection connection = poolState.idleConnections.get(i);
                    connection.invalidate();

                    Connection realConnection = connection.getRealConnection();
                    if (!realConnection.getAutoCommit()) {
                        realConnection.rollback();
                    }
                    realConnection.close();
                } catch (Exception e) {

                }
            }
            logger.info("PooledDataSource forcefully closed/removed all connections.");
        }
    }

    public boolean pingConnection(PooledConnection pooledConnection) {
        boolean result;

        try {
            result = !pooledConnection.getRealConnection().isClosed();
        } catch (SQLException e) {
            logger.info("Connection " + pooledConnection.getRealHashCode() + " is BAD: " + e.getMessage());
            result = false;
        }

        if (!result || !poolPingEnabled) {
            return result;
        }

        if (poolPingConnectionsNotUsedFor >= 0 && pooledConnection.getTimeElapsedSinceLastUse() > poolPingConnectionsNotUsedFor) {
            try {
                Connection realConn = pooledConnection.getRealConnection();
                Statement statement = realConn.createStatement();
                ResultSet resultSet = statement.executeQuery(poolPingQuery);
                resultSet.close();
                if (!realConn.getAutoCommit()) {
                    realConn.rollback();
                }
                result = true;
                logger.info("Connection " + pooledConnection.getRealHashCode() + " is GOOD!");
            } catch (SQLException e) {
                result = false;
                logger.info("Execution of ping query '" + poolPingQuery + "' failed: " + e.getMessage());
                try {
                    pooledConnection.getRealConnection().close();
                } catch (SQLException ex) {
                }
            }
        }
        return result;
    }

    private int assembleConnectionTypeCode(String url, String username, String password) {
        return ("" + url + username + password).hashCode();
    }

    @Override
    protected void finalize() throws Throwable {
        forceCloseAll();
        super.finalize();
    }

    @Override
    public Connection getConnection() throws SQLException {
        return popConnection(dataSource.getUsername(), dataSource.getPassword()).getProxyConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return popConnection(username, password).getProxyConnection();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLException(getClass().getName() + " is not a wrapper.");
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return DriverManager.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        DriverManager.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int loginTimeout) throws SQLException {
        DriverManager.setLoginTimeout(loginTimeout);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return DriverManager.getLoginTimeout();
    }

    @Override
    public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return java.util.logging.Logger.getLogger(java.util.logging.Logger.GLOBAL_LOGGER_NAME);
    }

    public void setDriver(String driver) {
        dataSource.setDriver(driver);
        forceCloseAll();
    }

    public void setUrl(String url) {
        dataSource.setUrl(url);
        forceCloseAll();
    }

    public void setUsername(String username) {
        dataSource.setUsername(username);
        forceCloseAll();
    }

    public void setPassword(String password) {
        dataSource.setPassword(password);
        forceCloseAll();
    }

    public void setDefaultAutoCommit(boolean defaultAutoCommit) {
        dataSource.setAutoCommit(defaultAutoCommit);
        forceCloseAll();
    }

    public int getPoolMaximumActiveConnections() {
        return poolMaximumActiveConnections;
    }

    public void setPoolMaximumActiveConnections(int poolMaximumActiveConnections) {
        this.poolMaximumActiveConnections = poolMaximumActiveConnections;
    }

    public int getPoolMaximumIdleConnections() {
        return poolMaximumIdleConnections;
    }

    public void setPoolMaximumIdleConnections(int poolMaximumIdleConnections) {
        this.poolMaximumIdleConnections = poolMaximumIdleConnections;
    }

    public int getPoolMaximumCheckoutTime() {
        return poolMaximumCheckoutTime;
    }

    public void setPoolMaximumCheckoutTime(int poolMaximumCheckoutTime) {
        this.poolMaximumCheckoutTime = poolMaximumCheckoutTime;
    }

    public int getExpectedConnectionTypeCode() {
        return expectedConnectionTypeCode;
    }

    public void setExpectedConnectionTypeCode(int expectedConnectionTypeCode) {
        this.expectedConnectionTypeCode = expectedConnectionTypeCode;
    }

    public String getPoolPingQuery() {
        return poolPingQuery;
    }

    public void setPoolPingQuery(String poolPingQuery) {
        this.poolPingQuery = poolPingQuery;
    }

    public boolean isPoolPingEnabled() {
        return poolPingEnabled;
    }

    public void setPoolPingEnabled(boolean poolPingEnabled) {
        this.poolPingEnabled = poolPingEnabled;
    }

    public int getPoolPingConnectionsNotUsedFor() {
        return poolPingConnectionsNotUsedFor;
    }

    public void setPoolPingConnectionsNotUsedFor(int poolPingConnectionsNotUsedFor) {
        this.poolPingConnectionsNotUsedFor = poolPingConnectionsNotUsedFor;
    }

    public int getPoolTimeToWait() {
        return poolTimeToWait;
    }

    public void setPoolTimeToWait(int poolTimeToWait) {
        this.poolTimeToWait = poolTimeToWait;
    }
}
