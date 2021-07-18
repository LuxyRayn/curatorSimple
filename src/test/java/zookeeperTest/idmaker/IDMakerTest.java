package zookeeperTest.idmaker;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * zookeeper uuid maker tester
 * Author: zirui liu
 * Date: 2021/7/18
 */
public class IDMakerTest {
    @Test
    public void testMakeId() {
        IDMaker idMaker = new IDMaker();
        idMaker.init();
        String nodeName = "/test/IDMake/ID-";

        for (int i = 0; i < 10; i++) {
            String id = idMaker.makeId(nodeName);
            System.out.println("No." + i + " id is :" + id);
        }
        idMaker.destory();
    }
}