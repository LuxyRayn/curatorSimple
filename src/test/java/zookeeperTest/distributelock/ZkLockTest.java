package zookeeperTest.distributelock;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.log4j.Logger;
import org.junit.Test;
import zookeeperTest.concurrent.FutureTaskScheduler;
import zookeeperTest.factory.ZKclient;

import java.util.concurrent.FutureTask;

import static org.junit.Assert.*;

/**
 * Lock implement tester
 * Author: zirui liu
 * Date: 2021/7/18
 */
@Slf4j
public class ZkLockTest {
    // variant
    int count = 0;

    @Test
    public void testLock() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            FutureTaskScheduler.add(() -> {
                ZkLock lock = new ZkLock();
                lock.lock();

                for (int j = 0; j < 10; j++) {
                    count++;
                }

                lock.unlock();
            });
        }
        log.info("count = " + count);
        Thread.sleep(1000000);
    }

    /**
     * official lock (InterProcessMutex)
     */
    @Test
    public void testzkMutex() throws InterruptedException {
        CuratorFramework client = ZKclient.instance.getClient();

        // create lock
        final InterProcessMutex zkMutex = new InterProcessMutex(client, "/mutex");

        for (int i = 0; i < 10; i++) {
            FutureTaskScheduler.add(() -> {
                try {
                    zkMutex.acquire();

                    for (int j = 0; j < 10; j++) {
                        count++;
                    }

                    log.warn("count = " + count);
                    zkMutex.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        log.warn("count = " + count);
    }
}