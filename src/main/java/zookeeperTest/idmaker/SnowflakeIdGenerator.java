package zookeeperTest.idmaker;

/**
 * Snowflake uuid Generator
 * Author: zirui liu
 * Date: 2021/7/18
 */
public class SnowflakeIdGenerator {
    /**
     * START_TIME: 2017-01-01 00:00:00
     */
    private static final long START_TIME = 1483200000000L;

    /**
     * worker id bit
     */
    private static final int WORKER_ID_BITS = 13;

    /**
     * seq number
     */
    private final static int SEQUENCE_BITS = 10;

    /**
     * max worker id ，8091
     * Complement of -1 shift right for 13 bit, and change the bit(flip)
     */
    private final static long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);

    /**
     * max seq number，1023
     * Complement of -1 shift right for 10 bit, and change the bit(flip)
     */
    private final static long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

    /**
     * worker NodeId offset
     */
    private final static long APP_HOST_ID_SHIFT = SEQUENCE_BITS;

    /**
     * timestamp offset
     */
    private final static long TIMESTAMP_LEFT_SHIFT = WORKER_ID_BITS + APP_HOST_ID_SHIFT;

    /**
     * workerId
     */
    private long workerId;

    /**
     * last Timestamp
     */
    private long lastTimestamp = -1L;

    /**
     * 当前毫秒生成的序列
     */
    private long sequence = 0L;

    /**
     * unique instance
     */
    public static SnowflakeIdGenerator instance = new SnowflakeIdGenerator();

    /**
     * instance init
     *
     * @param workerId workerId
     */
    public synchronized void init(long workerId) {
        if (workerId > MAX_WORKER_ID) {
            // zk workerId is bigger than MAX_WORKER_ID
            throw new IllegalArgumentException("worker id wrong: " + workerId);
        }
        instance.workerId = workerId;
    }

    /**
     * next id long
     *
     * @return the nextid
     */
    public Long nextId() {
        return generateId();
    }

    /**
     * generate the uuid
     *
     * @return uuid
     */
    private synchronized long generateId() {
        long current = System.currentTimeMillis();

        if (current < lastTimestamp) {
            // if current id time < lastTimestamp, that means the system clock have been rollback
            return -1;
        }

        if (current == lastTimestamp) {
            // add 1 to sequence
            sequence = (sequence + 1) & MAX_SEQUENCE;

            if (sequence == MAX_SEQUENCE) {
                // if current sequence already > MAX_SEQUENCE, then get next timeStamp
                current = this.nextMs(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = current;

        // generate the int64 uuid
        long time = (current - START_TIME) << TIMESTAMP_LEFT_SHIFT;

        long workerId = this.workerId << APP_HOST_ID_SHIFT;

        return time | workerId | sequence;
    }

    /**
     * get next ms
     *
     * @param timeStamp timeStamp
     * @return next timeStamp
     */
    private long nextMs(long timeStamp) {
        long current = System.currentTimeMillis();
        while (current <= timeStamp) {
            current = System.currentTimeMillis();
        }
        return current;
    }

    private SnowflakeIdGenerator() {}
}
