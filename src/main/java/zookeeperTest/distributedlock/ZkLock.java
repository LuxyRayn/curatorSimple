package zookeeperTest.distributedlock;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.Watcher;
import zookeeperTest.factory.ZKclient;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Lock implement
 * just for demonstrate how distributed lock works
 * in production, just use the official dis lock (InterProcessMutex)
 *
 * Author: zirui liu
 * Date: 2021/7/18
 */
public class ZkLock implements Lock{
    private static final String ZK_PATH = "/test/lock";
    private static final String LOCK_PREFIX = ZK_PATH + "/";
    private static final long WAIT_TIME = 1000;

    private String locked_short_path = null;
    private String locked_path = null;
    private String prior_path = null;
    private Thread thread;

    final AtomicInteger lockCount = new AtomicInteger(0);
    CuratorFramework client = null;

    public ZkLock() {
        ZKclient.instance.init();
        if (!ZKclient.instance.isNodeExist(ZK_PATH)) {
            ZKclient.instance.createNode(ZK_PATH, null);
        }
        client = ZKclient.instance.getClient();
    }

    @Override
    public boolean lock() {
        synchronized (this) {
            if (lockCount.get() == 0) {
                thread = Thread.currentThread();
                lockCount.incrementAndGet();
            } else {
                if (!thread.equals(Thread.currentThread())) {
                    return false;
                }
                lockCount.incrementAndGet();
                return true;
            }
        }

        try {
            boolean locked = false;

            // first time to lock(try)
            locked = tryLock();

            if (locked) {
                return true;
            }

            while (!locked) {
                await();

                List<String> waiters = getWaiters();

                if (checkLocked(waiters)) {
                    locked =  true;
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            unlock();
        }
        return false;
    }

    @Override
    public boolean unlock() {
        if (!thread.equals(Thread.currentThread())) {
            return false;
        }

        int newLockCount = lockCount.decrementAndGet();

        if (newLockCount < 0) {
            throw new IllegalMonitorStateException("counting error: " + locked_path);
        }

        if (newLockCount != 0) {
            return true;
        }

        try {
            if (ZKclient.instance.isNodeExist(locked_path)) {
                client.delete().forPath(locked_path);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private boolean tryLock() throws Exception {
        locked_path = ZKclient.instance.createEphemeralSeqNode(LOCK_PREFIX);
        if (null == locked_path) {
            throw new Exception("zk error");
        }

        // get index in queue
        locked_short_path = getShortPath(locked_path);

        // get queue
        List<String> waiters = getWaiters();

        // check if the first of the queue
        if (checkLocked(waiters)) {
            return true;
        }

        int index = Collections.binarySearch(waiters, locked_short_path);
        if (index < 0) {
            // not exists
            throw new Exception("can not find node: " + locked_short_path);
        }

        prior_path = ZK_PATH + "/" + waiters.get(index - 1);
        return false;
    }

    private boolean checkLocked(List<String> waiters) {
        Collections.sort(waiters);

        if (locked_short_path.equals(waiters.get(0))) {
            System.out.println("get lock successfully, node=" + locked_short_path);
            return true;
        }
        return false;
    }

    private void await() throws Exception {
        if (null == prior_path) {
            throw new Exception("prior_path error");
        }

        final CountDownLatch latch = new CountDownLatch(1);

        Watcher w = watchedEvent -> {
            System.out.println("get change = " + watchedEvent);
            System.out.println("node deleted");
            latch.countDown();
        };
        client.getData().usingWatcher(w).forPath(prior_path);

        /*
        TreeCache treeCache = new TreeCache(client, prior_path);
        TreeCacheListener l = (curatorFramework, event) -> {
            ChildData data = event.getData();
            if (data != null) {
                switch (event.getType()) {
                    case NODE_REMOVED:
                        System.out.println("[TreeCache] node delete, path=" + data.getPath() +
                                " data=" + Arrays.toString(data.getData()));
                        break;
                    default:
                        break;
                }
            }
        };

        treeCache.getListenable().addListener(l);
        treeCache.start();
        */


        latch.await(WAIT_TIME, TimeUnit.SECONDS);
    }

    private String getShortPath(String locked_path) {

        int index = locked_path.lastIndexOf(ZK_PATH + "/");
        if (index >= 0) {
            index += ZK_PATH.length() + 1;
            return index <= locked_path.length() ? locked_path.substring(index) : "";
        }
        return null;
    }

    /**
     * get all waiting node from zk
     */
    protected List<String> getWaiters() {
        List<String> children = null;
        try {
            children = client.getChildren().forPath(ZK_PATH);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return children;
    }

}
