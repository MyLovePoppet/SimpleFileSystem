import java.util.concurrent.Semaphore;

public class ReadWriteSemaphore {
    //读的人
    int readPeople;
    //mutex
    Semaphore mutexSemaphore;
    //读取锁
    Semaphore writeSemaphore;

    /**
     * 构造函数
     */
    ReadWriteSemaphore() {
        readPeople = 0;
        mutexSemaphore = new Semaphore(1);
        writeSemaphore = new Semaphore(1);
    }

    /**
     * 尝试获得读取权限
     *
     * @throws InterruptedException 可能会抛出异常
     */
    void acquireRead() throws InterruptedException {
        mutexSemaphore.acquire();
        readPeople++;
        if (readPeople == 1) {
            if (!writeSemaphore.tryAcquire()) {
                System.out.println("waiting for write OK...");
                writeSemaphore.acquire();
            }
        }
        mutexSemaphore.release();
    }

    /**
     * 释放读锁
     *
     * @throws InterruptedException 可能会抛出异常
     */
    void releaseRead() throws InterruptedException {
        mutexSemaphore.acquire();
        readPeople--;
        if (readPeople == 0) {
            writeSemaphore.release();
        }
        mutexSemaphore.release();
    }

    /**
     * 获取写锁
     *
     * @throws InterruptedException 可能会抛出异常
     */
    void acquireWrite() throws InterruptedException {
        if (!writeSemaphore.tryAcquire()) {
            System.out.println("waiting for read or write OK...");
            writeSemaphore.acquire();
        }

    }

    /**
     * 释放写锁
     */
    void releaseWrite() {
        writeSemaphore.release();
    }
}
