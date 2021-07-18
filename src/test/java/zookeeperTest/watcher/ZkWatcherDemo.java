package zookeeperTest.watcher;

import lombok.Data;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.junit.Test;
import zookeeperTest.factory.ClientFactory;
import zookeeperTest.factory.ZKclient;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/**
 * ZkWatcherDemo
 * Author: zirui liu
 * Date: 2021/7/18
 */
@Data
public class ZkWatcherDemo {
    private String workerPath = "/test/listener/remoteNode";
    private String subWorkerPath = "/test/listener/remoteNode/id-";

    @Test
    public void testWatcher() {
        CuratorFramework client = ZKclient.instance.getClient();

        // check whether node exists
        boolean isExist = ZKclient.instance.isNodeExist(workerPath);
        if (!isExist) {
            ZKclient.instance.createNode(workerPath, null);
        }

        try {
            Watcher w = new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    System.out.println("get changes, watched event = " + watchedEvent);
                }
            };

            byte[] content = client.getData()
                    .usingWatcher(w)
                    .forPath(workerPath);

            System.out.println("watch node content: " + new String(content));

            // first time node changing content
            client.setData().forPath(workerPath, "the first time changing".getBytes(StandardCharsets.UTF_8));

            // second time node changing content
            client.setData().forPath(workerPath, "the second time changing".getBytes(StandardCharsets.UTF_8));

            Thread.sleep(Integer.MAX_VALUE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testNoCache() {
        CuratorFramework client = ZKclient.instance.getClient();

        // check whether node exists
        boolean isExist = ZKclient.instance.isNodeExist(workerPath);
        if (!isExist) {
            ZKclient.instance.createNode(workerPath, null);
        }

        try {
            NodeCache nodeCache = new NodeCache(client, workerPath, false);
            NodeCacheListener listener = new NodeCacheListener() {
                @Override
                public void nodeChanged() throws Exception {
                    ChildData childData = nodeCache.getCurrentData();
                    System.out.println("znode change, path=" + childData.getPath());
                    System.out.println("znode change, data=" + new String(childData.getData(),
                            StandardCharsets.UTF_8));
                    System.out.println("znode change, stat=" + childData.getStat());
                }
            };

            nodeCache.getListenable().addListener(listener);
            nodeCache.start();

            // first time node changing content
            client.setData().forPath(workerPath, "the first time changing".getBytes(StandardCharsets.UTF_8));

            // second time node changing content
            client.setData().forPath(workerPath, "the second time changing".getBytes(StandardCharsets.UTF_8));

            Thread.sleep(1000);

            // second time node changing content
            client.setData().forPath(workerPath, "the third time changing".getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * can not watch further node
     */
    @Test
    public void testChildrenCache() {
        CuratorFramework client = ZKclient.instance.getClient();

        // check whether node exists
        boolean isExist = ZKclient.instance.isNodeExist(workerPath);
        if (!isExist) {
            ZKclient.instance.createNode(workerPath, null);
        }

        try {
            PathChildrenCache cache = new PathChildrenCache(client, workerPath, true);
            PathChildrenCacheListener listener = (curatorFramework, event) -> {
                ChildData data = event.getData();
                switch (event.getType()) {
                    case CHILD_ADDED:
                        System.out.println("child node added, path=" + data.getPath() +
                                " data=" + new String(data.getData(), StandardCharsets.UTF_8));
                        break;
                    case CHILD_UPDATED:
                        System.out.println("child node updated, path=" + data.getPath() +
                                " data=" + new String(data.getData(), StandardCharsets.UTF_8));
                        break;
                    case CHILD_REMOVED:
                        System.out.println("child node removed, path=" + data.getPath() +
                                " data=" + new String(data.getData(), StandardCharsets.UTF_8));
                        break;
                    default:
                        break;
                }
            };

            cache.getListenable().addListener(listener);

            /**
             * start mode:
             * normal: init cache in async way
             * build_initial_cache: init in sync way, and get data from server after init
             * post_initialized_event: async way, and listener will know
             */
            cache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);

            for (int i = 0; i < 3; i++) {
                ZKclient.instance.createNode(subWorkerPath + i, null);
            }

            for (int i = 0; i < 3; i++) {
                ZKclient.instance.deleteNode(subWorkerPath + i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testTreeCache() {
        CuratorFramework client = ZKclient.instance.getClient();

        // check whether node exists
        boolean isExist = ZKclient.instance.isNodeExist(workerPath);
        if (!isExist) {
            ZKclient.instance.createNode(workerPath, null);
        }

        try {
            TreeCache treeCache = new TreeCache(client, workerPath);
            TreeCacheListener listener = (curatorFramework, event) -> {
                ChildData data = event.getData();
                if (null == data) {
                    System.out.println("empty data");
                    return;
                }

                switch (event.getType()) {
                    case NODE_ADDED:
                        System.out.println("[TreeCache] node added, path=" + data.getPath() +
                                " data=" + new String(data.getData(), StandardCharsets.UTF_8));
                        break;
                    case NODE_UPDATED:
                        System.out.println("[TreeCache] node updated, path=" + data.getPath() +
                                " data=" + new String(data.getData(), StandardCharsets.UTF_8));
                        break;
                    case NODE_REMOVED:
                        System.out.println("[TreeCache] node removed, path=" + data.getPath() +
                                " data=" + new String(data.getData(), StandardCharsets.UTF_8));
                        break;
                    default:
                        break;
                }
            };

            treeCache.getListenable().addListener(listener);

            treeCache.start();

            for (int i = 0; i < 3; i++) {
                ZKclient.instance.createNode(subWorkerPath + i, null);
            }

            for (int i = 0; i < 3; i++) {
                ZKclient.instance.deleteNode(subWorkerPath + i);
            }

            ZKclient.instance.deleteNode(workerPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}