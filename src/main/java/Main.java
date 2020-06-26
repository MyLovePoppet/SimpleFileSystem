import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

public class Main {

    public static void main(String[] args){
        /*try {
            Scanner scanner = new Scanner(System.in);
            FileSystem fileSystem = new FileSystem("shuqy-file-system", false);
            while (true) {
                System.out.print(fileSystem.currentDir+"->");
                String nextOp = scanner.nextLine();
                if (nextOp.equals("quit")) {
                    System.out.println("Thread: " + Thread.currentThread().getId() + " quit!");
                    break;
                }
                fileSystem.doService(nextOp);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

        Semaphore semaphore=new Semaphore(1);
        Thread thread1 = new Thread(() -> {
            try {
                FileSystem fileSystem = new FileSystem("shuqy-file-system", false);
                Scanner scanner = new Scanner(System.in);
                while (true) {
                    semaphore.acquire();
                    System.out.println("Thread: " + Thread.currentThread().getId()+" "+fileSystem.currentDir+"->");
                    String nextOp = scanner.nextLine();
                    if (nextOp.equals("quit")) {
                        System.out.println("Thread: " + Thread.currentThread().getId() + " quit!");
                        break;
                    }
                    semaphore.release();
                    fileSystem.doService(nextOp);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread1.start();
        Thread thread2 = new Thread(() -> {
            try {
                semaphore.acquire();
                FileSystem fileSystem = new FileSystem("shuqy-file-system", false);
                Scanner scanner = new Scanner(System.in);
                while (true) {
                    System.out.print("Thread: " + Thread.currentThread().getId()+" "+fileSystem.currentDir+"->");
                    String nextOp = scanner.nextLine();
                    if (nextOp.equals("quit")) {
                        System.out.println("Thread: " + Thread.currentThread().getId() + " quit!");
                        break;
                    }
                    semaphore.release();
                    fileSystem.doService(nextOp);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread2.start();

    }
}

