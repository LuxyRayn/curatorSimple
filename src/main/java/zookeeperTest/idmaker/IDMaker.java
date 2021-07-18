package zookeeperTest.idmaker;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import zookeeperTest.factory.ClientFactory;

/**
 * zookeeper uuid maker
 * Author: zirui liu
 * Date: 2021/7/18
 */
public class IDMaker {
    private static final String ZK_ADDRESS = "127.0.0.1:2181";
    // Zk client
    CuratorFramework client = null;

    public void init() {
        // create zk client
        client = ClientFactory.createSimple(ZK_ADDRESS);

        client.start();
    }

    public void destory() {
        if (null != client) {
            client.close();
        }
    }

    private String createSeqNode(String pathPrefix) {
        try {
            // create zNode seqNode
            return client
                    .create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                    .forPath(pathPrefix);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public String makeId(String nodeName) {
        String str = createSeqNode(nodeName);

        if (null == str) {
            return null;
        }

        // get zkNode index
        int index = str.lastIndexOf(nodeName);
        if (index >= 0) {
            index += nodeName.length();
            return index <= str.length() ? str.substring(index) : "";
        }

        return str;
    }
}
