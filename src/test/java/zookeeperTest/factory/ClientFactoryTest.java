package zookeeperTest.factory;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.*;

/**
 * create node
 * Author: zirui liu
 * Date: 2021/7/18
 */
public class ClientFactoryTest {
    private final static String ZK_ADDRESS = "127.0.0.1:2181";
    
    /**
     * create new zkNode
     */
    @Test
    public void createNode() {
        // create zk client
        CuratorFramework client = ClientFactory.createSimple(ZK_ADDRESS);

        try {
            client.start();

            // create zNode
            // data = payload
            String data = "hello";
            byte[] payload = data.getBytes(StandardCharsets.UTF_8);
            String zkPath = "/test/CRUD/node-1";
            client.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT)
                    .forPath(zkPath, payload);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CloseableUtils.closeQuietly(client);
        }
    }

    /**
     * read info from zkNode
     */
    @Test
    public void readNode() {
        // create zk client
        CuratorFramework client = ClientFactory.createSimple(ZK_ADDRESS);

        try {
            client.start();

            String zkPath = "/test/CRUD/node-1";
            Stat stat = client.checkExists().forPath(zkPath);
            if (null != stat) {
                byte[] payload = client.getData().forPath(zkPath);
                String data = new String(payload, StandardCharsets.UTF_8);
                System.out.println("read data: " + data);

                String parentPath = "/test";
                List<String> children = client.getChildren().forPath(parentPath);

                for (String child : children) {
                    System.out.println("child: " + child);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CloseableUtils.closeQuietly(client);
        }
    }

    /**
     * update zkNode
     */
    @Test
    public void updateNode() {
        // create zk client
        CuratorFramework client = ClientFactory.createSimple(ZK_ADDRESS);

        try {
            client.start();

            String data = "hello world";
            byte[] payload = data.getBytes(StandardCharsets.UTF_8);
            String zkPath = "/test/CRUD/node-1";
            client.setData().forPath(zkPath, payload);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CloseableUtils.closeQuietly(client);
        }
    }

    /**
     * update zkNode in async way
     */
    @Test
    public void updataNodeAsync() {
        // create zk client
        CuratorFramework client = ClientFactory.createSimple(ZK_ADDRESS);

        try {
            AsyncCallback.StringCallback callback = (i, s, o, s1) -> System.out.println(
                    "i = " + i + " | " +
                    "s = " + s + " | " +
                    "o = " + o + " | " +
                    "s1 = " + s1
            );

            client.start();

            String data = "hello, every body!";
            byte[] payload = data.getBytes(StandardCharsets.UTF_8);
            String zkPath = "/test/CRUD/node-1";
            client.setData()
                    .inBackground(callback)
                    .forPath(zkPath, payload);
            Thread.sleep(10000);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CloseableUtils.closeQuietly(client);
        }
    }

    /**
     * delete node from zk
     */
    @Test
    public void deleteNode() {
        // create zk client
        CuratorFramework client = ClientFactory.createSimple(ZK_ADDRESS);

        try {
            client.start();

            // delete operation
            String zkPath = "/test/CRUD/node-1";
            client.delete().forPath(zkPath);

            String parentPath = "/test";
            List<String> children = client.getChildren().forPath(parentPath);

            for (String child : children) {
                System.out.println("child: " + child);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CloseableUtils.closeQuietly(client);
        }
    }
}