import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class FileSystem {
    final static long FileSystemSize = 100 * 1024 * 1024;
    final static String XMLPrefix = ".filesystem";
    Document fileSystemXMLDoc;
    RandomAccessFile fileSystemFile;
    String fileSystemName;

    public FileSystem(String fileSystemName, boolean isCreate) throws IOException {
        this.fileSystemName = fileSystemName;
        if (isCreate) {
            File xmlFile = new File(fileSystemName + XMLPrefix);
            xmlFile.createNewFile();
            fileSystemXMLDoc = Jsoup.parse(xmlFile, "UTF-8");
            fileSystemFile = new RandomAccessFile(fileSystemName, "rw");
            fileSystemFile.setLength(FileSystemSize);
        } else {
            fileSystemXMLDoc = Jsoup.parse(fileSystemName + XMLPrefix);
            fileSystemFile = new RandomAccessFile(fileSystemName, "rw");
        }
    }

    @Override
    public String toString() {
        return "FileSystem{" +
                "fileSystemName='" + fileSystemName + '\'' +
                ", fileSystemFile=" + fileSystemFile +
                '}';
    }
}
