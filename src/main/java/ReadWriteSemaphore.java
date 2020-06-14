import java.util.concurrent.Semaphore;

public class ReadWriteSemaphore {
    int readPeople;
    Semaphore mutexSemaphore;
    Semaphore writeSemaphore;

    ReadWriteSemaphore() {
        readPeople = 0;
        mutexSemaphore = new Semaphore(1);
        writeSemaphore = new Semaphore(1);
    }

    void acquireRead() throws InterruptedException {
        mutexSemaphore.acquire();
        readPeople++;
        if (readPeople == 1) {
            if(!writeSemaphore.tryAcquire()){
                System.out.println("waiting for write OK...");
                writeSemaphore.acquire();
            }
        }
        mutexSemaphore.release();
    }

    void releaseRead() throws InterruptedException {
        mutexSemaphore.acquire();
        readPeople--;
        if (readPeople == 0) {
            writeSemaphore.release();
        }
        mutexSemaphore.release();
    }

    void acquireWrite() throws InterruptedException {
        if(!writeSemaphore.tryAcquire()){
            System.out.println("waiting for read or write OK...");
            writeSemaphore.acquire();
        }

    }
    void releaseWrite() {
        writeSemaphore.release();
    }
}
