package zookeeperTest.idmaker;

import lombok.Data;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import zookeeperTest.factory.ClientFactory;

import java.nio.charset.StandardCharsets;

/**
 * Snowflake uuid worker
 * Author: zirui liu
 * Date: 2021/7/18
 */
@Data
public class SnowflakeIdWorker {
    private final static String ZK_ADDRESS = "127.0.0.1:2181";

    //zk client
    transient private CuratorFramework zkClient = null;

    // worker path
    private String pathPrefix = "/test/IDMaker/worker-";
    private String pathRegistered = null;

    public static SnowflakeIdWorker instance = new SnowflakeIdWorker();

    private SnowflakeIdWorker() {
        this.zkClient = ClientFactory.createSimple(ZK_ADDRESS);
        this.zkClient.start();
        this.init();
    }

    public void init() {
        try {
            byte[] payload = pathPrefix.getBytes();

            pathRegistered = zkClient.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                    .forPath(pathPrefix, payload);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long getId() {
        String sid = null;
        if (null == pathRegistered) {
            throw new RuntimeException("regis node fail");
        }

        int index = pathRegistered.lastIndexOf(pathPrefix);
        if (index >= 0) {
            index += pathPrefix.length();
            sid = index <= pathRegistered.length() ? pathRegistered.substring(index) : null;
        }

        return Long.parseLong(sid);
    }
}
