import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.RandomAccessFile;

public class WriteService extends IService {
    final RandomAccessFile randomAccessFile;
    long newPosition;

    public WriteService(RandomAccessFile randomAccessFile, long newPosition) {
        this.randomAccessFile = randomAccessFile;
        this.newPosition = newPosition;
    }

    @Override
    public ReturnState service(Element element, String... args) {
        /*
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        */
        byte[] bytes = args[1].getBytes();
        long newStart = newPosition;
        long newSize = bytes.length;
        long newEnd = newPosition + newSize;
        element.attr("文件大小", String.valueOf(newSize));
        element.attr("文件起始", String.valueOf(newStart));
        element.attr("文件结束", String.valueOf(newEnd));
        element.attr("名称", args[0]);
        try {
            write(bytes);
        } catch (IOException e) {
            return new ReturnState(null, e.toString(), ReturnState.ReturnNull);
        }
        return new ReturnState(null, null, ReturnState.ReturnOK);
    }

    void write(byte[] bytes) throws IOException {
        randomAccessFile.seek(newPosition);
        randomAccessFile.write(bytes);
    }
}
