import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.RandomAccessFile;

public class ReadService extends IService{
    final RandomAccessFile randomAccessFile;

    public ReadService(RandomAccessFile randomAccessFile) {
        this.randomAccessFile = randomAccessFile;
    }
    @Override
    public ReturnState service(Element element, String... args) {
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Element element1=findNameEquals(element,args[0],false);
        if(element1==null){
            return new ReturnState(null,null,ReturnState.ReturnNull);
        }
        else {
            StringBuilder stringBuilder = new StringBuilder();
            byte []bytes=read(element1);
            stringBuilder.append(new String(bytes));
            return new ReturnState(null,stringBuilder.toString(),ReturnState.ReturnOK);
        }
    }
    byte[] read(Element element){
        long start = Long.parseLong(element.attr("文件起始"));
        long end = Long.parseLong(element.attr("文件结束"));
        long size = Long.parseLong(element.attr("文件大小"));
        byte[]bytes=new byte[(int)size];
        try {
            randomAccessFile.seek(start);
            randomAccessFile.read(bytes,0,(int)size);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }
}
