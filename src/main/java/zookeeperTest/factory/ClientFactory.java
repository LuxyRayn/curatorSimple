package zookeeperTest.factory;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * zookeeper client factory
 * Author: zirui liu
 * Date: 2021/7/18
 */
public class ClientFactory {
    /**
     * create a simple connection
     *
     * @param connectionString zk address
     * @return curator instance
     */
    public static CuratorFramework createSimple(String connectionString) {
        ExponentialBackoffRetry retry = new ExponentialBackoffRetry(1000, 3);

        return CuratorFrameworkFactory.newClient(connectionString, retry);
    }

    /**
     * create a connection with options
     *
     * @param connectionString address
     * @param retryPolicy retry policy
     * @param connectionTimeoutMs timeout
     * @param sessionTimeoutMs session timeout
     * @return curator framework instance
     */
    public static CuratorFramework createWithOptions(String connectionString, RetryPolicy retryPolicy,
                                                     int connectionTimeoutMs, int sessionTimeoutMs) {
        return CuratorFrameworkFactory
                .builder()
                .connectString(connectionString)
                .retryPolicy(retryPolicy)
                .connectionTimeoutMs(connectionTimeoutMs)
                .sessionTimeoutMs(sessionTimeoutMs)
                .build();
    }
}
