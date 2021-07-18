package zookeeperTest.factory;

import lombok.Data;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.nio.charset.StandardCharsets;

/**
 * ZKclient
 * Author: zirui liu
 * Date: 2021/7/18
 */
@Data
public class ZKclient {
    private CuratorFramework client;

    private static final String ZK_ADDRESS = "127.0.0.1:2181";

    public static ZKclient instance = null;

    static {
        instance = new ZKclient();
        instance.init();
    }

    private ZKclient() {

    }

    public void init() {

        if (null != client) {
            return;
        }
        // create client
        client = ClientFactory.createSimple(ZK_ADDRESS);

        // start client
        client.start();
    }

    public void destroy() {
        CloseableUtils.closeQuietly(client);
    }


    /**
     * create node
     */
    public void createNode(String zkPath, String data) {
        try {
            byte[] payload = "to set content".getBytes(StandardCharsets.UTF_8);
            if (data != null) {
                payload = data.getBytes(StandardCharsets.UTF_8);
            }
            client.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT)
                    .forPath(zkPath, payload);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * deleteNode
     */
    public void deleteNode(String zkPath) {
        try {
            if (!isNodeExist(zkPath)) {
                return;
            }
            client.delete()
                    .forPath(zkPath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * check node
     */
    public boolean isNodeExist(String zkPath) {
        try {

            Stat stat = client.checkExists().forPath(zkPath);
            if (null == stat) {
                System.out.println("node does not exists: " + zkPath);
                return false;
            } else {
                System.out.println("node already existed, stat is: " + stat.toString());
                return true;

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * create eph seq node
     */
    public String createEphemeralSeqNode(String srcpath) {
        try {

            // create node
            String path = client.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                    .forPath(srcpath);

            return path;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
