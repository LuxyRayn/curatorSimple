package zookeeperTest.distributedlock;

/**
 * Lock interface
 * Author: zirui liu
 * Date: 2021/7/18
 */
public interface Lock {
    boolean lock();

    boolean unlock();
}
