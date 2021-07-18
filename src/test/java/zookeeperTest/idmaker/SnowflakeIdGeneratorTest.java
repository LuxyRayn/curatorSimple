package zookeeperTest.idmaker;

import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Snowflake uuid Generator tester
 * Author: zirui liu
 * Date: 2021/7/18
 */
public class SnowflakeIdGeneratorTest {
    @Test
    public void snowflakeIdTest() {
        long workId = SnowflakeIdWorker.instance.getId();
        System.out.println("working at worker-" + workId);

        // init generator
        SnowflakeIdGenerator.instance.init(workId);

        // create new thread to gen id
        ExecutorService es = Executors.newFixedThreadPool(10);

        final Set<Long> idSet = new HashSet<>();
        Collections.synchronizedCollection(idSet);

        long start = System.currentTimeMillis();
        System.out.println("begin to gen uuid");
        for (int i = 0; i < 10; i++) {
            es.execute(() -> {
                for (long j = 0; j < 5000000; j++) {
                    long id = SnowflakeIdGenerator.instance.nextId();
                    synchronized (idSet) {
                        idSet.add(id);
                    }
                }
            });
        }
        es.shutdown();

        try {
            es.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();

        System.out.println("gen uuid end, cost time: " + (end - start) + "(ms)");
    }
}