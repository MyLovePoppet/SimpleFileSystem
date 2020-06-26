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
        /*
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        */
        //找到文件的xml节点位置
        Element element1=findNameEquals(element,args[0],false);
        if(element1==null){
            return new ReturnState(null,"no file",ReturnState.ReturnNull);
        }
        else {
            StringBuilder stringBuilder = new StringBuilder();
            //读取数据
            byte []bytes=read(element1);
            stringBuilder.append(new String(bytes));
            //返回结果
            return new ReturnState(null,stringBuilder.toString(),ReturnState.ReturnOK);
        }
    }

    /**
     * 从一个节点读取文件信息
     * @param element 需要读取数据的文件节点
     * @return 文件内的数据
     */
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
