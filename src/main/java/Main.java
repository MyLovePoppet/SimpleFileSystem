import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.Semaphore;

public class Main {


    public static void main(String[] args) {
        try {
            Document document = Jsoup.parse(new File("FileSystem.xml"), "UTF-8");
            document.getAllElements().forEach(element -> {
                System.out.println(element.attributes().asList() + "\t" + element.text());
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

