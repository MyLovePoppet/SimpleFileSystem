import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.*;

public class FileSystem {
    final static long FileSystemSize = 100 * 1024 * 1024;
    final static String XMLPrefix = ".filesystem.xml";
    final static String FileSystemPrefix = ".filesystem";

    final IService lsService;
    final IService cdService;
    final IService readService;
    IService writeService;
    final IService rmdirService;
    final IService mkdirService;

    Document fileSystemXMLDoc;
    RandomAccessFile fileSystemFile;
    String fileSystemName;

    List<FileOffset> fileOffsets;
    Element currentElement;
    List<String> currentDir;

    static Map<String,ReadWriteSemaphore>mutex=new Hashtable<>();

    /**
     * 构造函数
     * @param fileSystemName 文件系统命名
     * @param isCreate 是否为新建文件系统
     * @throws IOException 可能会抛出IOException
     */
    public FileSystem(String fileSystemName, boolean isCreate) throws IOException {
        this.fileSystemName = fileSystemName;
        fileOffsets = new ArrayList<>();
        File fileSystem = new File(fileSystemName + FileSystemPrefix);
        File xmlFile = new File(fileSystemName + XMLPrefix);
        //创建新文件
        if (isCreate) {
            xmlFile.createNewFile();
            fileSystem.createNewFile();
            FileInputStream xmlFileInputStream = new FileInputStream(xmlFile);
            //xml读入配置文件
            fileSystemXMLDoc = Jsoup.parse(xmlFileInputStream, "UTF-8", "", Parser.xmlParser());
            //"rw"表示可读可写
            fileSystemFile = new RandomAccessFile(fileSystem, "rw");
            //设置大小为为100MB
            fileSystemFile.setLength(FileSystemSize);
        } else {
            //否则直接读入数据即可
            FileInputStream xmlFileInputStream = new FileInputStream(xmlFile);
            fileSystemXMLDoc = Jsoup.parse(xmlFileInputStream, "UTF-8", "", Parser.xmlParser());
            fileSystemFile = new RandomAccessFile(fileSystem, "rw");
            parseFileOffsets();
        }
        currentElement = fileSystemXMLDoc.getAllElements().get(1);
        currentDir = new ArrayList<>();
        lsService = new LsService();
        cdService = new CdService();
        readService = new ReadService(fileSystemFile);
        rmdirService = new RmdirService();
        mkdirService = new MkdirService();
    }

    public void updateXMLFile() {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(new File(fileSystemName + XMLPrefix));
            //System.out.println(fileSystemXMLDoc.outerHtml());
            fileOutputStream.write(fileSystemXMLDoc.outerHtml().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 在文件系统内找需要fileSize的文件位置
     * @param fileSize 需要的文件大小
     * @return 在文件系统内的首地址偏移，找不到返回-1
     */
    public long findLocationForFile(long fileSize) {
        //文件系统内没有文件
        if (fileOffsets.size() == 0)
            return 0;
        for (int i = 0; i < fileOffsets.size() - 1; i++) {
            //如果空位置大于给定的大小，返回该地址即可
            if ((fileOffsets.get(i + 1).start - fileOffsets.get(i).end) >= fileSize) {
                return fileOffsets.get(i).end;
            }
        }
        //最后的还有没有给定大小的位置
        return fileOffsets.get(fileOffsets.size() - 1).end + fileSize <= FileSystemSize
                ? fileOffsets.get(fileOffsets.size() - 1).end : -1;
    }

    public int findIndexOfLocation(long start) {
        for (int i = 0; i < fileOffsets.size(); i++) {
            if (fileOffsets.get(i).end == start)
                return i+1;
        }
        return -1;
    }

    public void parseFileOffsets() {
        fileSystemXMLDoc.getAllElements().forEach(element -> {
            if ("文件".equals(element.tagName())) {
                long start = Long.parseLong(element.attr("文件起始"));
                long end = Long.parseLong(element.attr("文件结束"));
                long size = Long.parseLong(element.attr("文件大小"));
                fileOffsets.add(new FileOffset(start, end));
            }
        });
        fileOffsets.sort(Comparator.comparingLong(fileOffset -> fileOffset.start));
    }

    public void doService(String nextOp) throws InterruptedException {
        if (nextOp.equals("ls")) {
            ReturnState lsReturnState = lsService.service(currentElement);
            System.out.println(lsReturnState.returnMessage);
            return;
        }
        if (nextOp.startsWith("cd")) {
            String[] strings = nextOp.split("\\s+");
            ReturnState cdReturnState = cdService.service(currentElement, strings[1]);
            if (cdReturnState.returnCode == ReturnState.ReturnNull) {
                if (strings[1].equals("..")) {
                    System.out.println("No parent dir! Please check!");
                } else {
                    System.out.println("No dir named '" + strings[1] + "'! Please check!");
                }
            } else {
                currentElement = cdReturnState.returnElement;
                if (strings[1].equals("..")) {
                    int size = currentDir.size() - 1;
                    currentDir.remove(size);
                } else {
                    currentDir.add(currentElement.attr("名称"));
                }
            }
            return;
        }
        if (nextOp.startsWith("read")) {
            String[] strings = nextOp.split("\\s+");
            if(!mutex.containsKey(currentDir+strings[1])){
                mutex.put(currentDir+strings[1],new ReadWriteSemaphore());
            }
            mutex.get(currentDir+strings[1]).acquireRead();

            ReturnState readReturnState = readService.service(currentElement, strings[1]);
            if (readReturnState.returnCode == ReturnState.ReturnOK) {
                System.out.println(readReturnState.returnMessage);
            } else {
                System.out.println("No file named as '" + strings[1] + "'! Please check!");
            }

            mutex.get(currentDir+strings[1]).releaseRead();
            return;
        }
        if (nextOp.startsWith("mkdir")) {
            String[] strings = nextOp.split("\\s+");
            ReturnState mkdirReturnState = mkdirService.service(currentElement, strings[1]);
            if (mkdirReturnState.returnCode == ReturnState.ReturnOK) {
                Element element = new Element("文件夹");
                element.attr("名称", strings[1]);
                //添加文件夹
                currentElement.appendChild(element);
                updateXMLFile();
            } else {
                System.out.println("Already has a dir named as '" + strings[1] + "'! Please check!");
            }
            return;
        }
        if (nextOp.startsWith("rmdir")) {
            String[] strings = nextOp.split("\\s+");
            ReturnState rmdirReturnState = rmdirService.service(currentElement, strings[1]);
            if (rmdirReturnState.returnCode == ReturnState.ReturnOK) {
                //获取所有的子节点
                Elements elements = rmdirReturnState.returnElement.getAllElements();
                //对每个子节点进行遍历
                elements.forEach(element -> {
                    //对文件节点执行删除操作
                    if ("文件".equals(element.tagName())) {
                        long start = Long.parseLong(element.attr("文件起始"));
                        long end = Long.parseLong(element.attr("文件结束"));
                        long size = Long.parseLong(element.attr("文件大小"));
                        //移除文件所占空间，标记为空白处
                        fileOffsets.remove(new FileOffset(start, end));
                    }
                });
                rmdirReturnState.returnElement.remove();
                updateXMLFile();
            } else {
                System.out.println("No dir named as '" + strings[1] + "'! Please check!");
            }
            return;
        }
        if (nextOp.startsWith("write")) {
            String[] strings = nextOp.split("\\s+");
            String name = strings[1];
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 2; i < strings.length; i++) {
                stringBuilder.append(strings[i]);
            }
            String writeValue = stringBuilder.toString();
            if(!mutex.containsKey(currentDir+name)){
                mutex.put(currentDir+name,new ReadWriteSemaphore());
            }
            mutex.get(currentDir+name).acquireWrite();
            Element element = IService.findNameEquals(currentElement, name, false);
            if (element == null) {
                System.out.println("No file named as '" + name + "'! Please check!");
            } else {
                long start = Long.parseLong(element.attr("文件起始"));
                long end = Long.parseLong(element.attr("文件结束"));
                long size = Long.parseLong(element.attr("文件大小"));
                fileOffsets.remove(new FileOffset(start, end));
                long newSize = writeValue.getBytes().length;
                long newStart = findLocationForFile(newSize);
                long newEnd = newStart + newSize;
                fileOffsets.add(findIndexOfLocation(newStart), new FileOffset(newStart, newEnd));
                writeService = new WriteService(fileSystemFile, newStart);
                writeService.service(element, name, writeValue);
                updateXMLFile();
            }
            mutex.get(currentDir+name).releaseWrite();
            return;
        }
        if (nextOp.startsWith("open")) {
            String[] strings = nextOp.split("\\s+");
            String name = strings[1];
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 2; i < strings.length; i++) {
                stringBuilder.append(strings[i]);
            }
            String writeValue = stringBuilder.toString();
            if(!mutex.containsKey(currentDir+name)){
                mutex.put(currentDir+name,new ReadWriteSemaphore());
            }
            mutex.get(currentDir+name).acquireWrite();
            //找到是否有同名文件
            Element element = IService.findNameEquals(currentElement, name, false);
            if (element != null) {
                System.out.println("Already has file named as '" + name + "'! Please check!");
            } else {
                Element element1 = new Element("文件");
                //Attribute信息
                long newSize = writeValue.getBytes().length;
                long newStart = findLocationForFile(newSize);
                long newEnd = newStart + newSize;
                //xml文件加入节点
                currentElement.appendChild(element1);
                fileOffsets.add(findIndexOfLocation(newStart), new FileOffset(newStart, newEnd));
                writeService = new WriteService(fileSystemFile, newStart);
                writeService.service(element1, name, writeValue);
                //更新xml文件
                updateXMLFile();
            }
            mutex.get(currentDir+name).releaseWrite();
            return;
        }
        if(nextOp.startsWith("rmfile")){
            String[] strings = nextOp.split("\\s+");
            String name = strings[1];
            if(!mutex.containsKey(currentDir+name)){
                mutex.put(currentDir+name,new ReadWriteSemaphore());
            }
            mutex.get(currentDir+name).acquireWrite();
            Element element=IService.findNameEquals(currentElement,name,false);
            if(element==null){
                System.out.println("No file named as '"+name+"'! Please check!");
            }else {
                long start = Long.parseLong(element.attr("文件起始"));
                long end = Long.parseLong(element.attr("文件结束"));
                long size = Long.parseLong(element.attr("文件大小"));
                fileOffsets.remove(new FileOffset(start, end));
                element.remove();
                updateXMLFile();
            }
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mutex.get(currentDir+name).releaseWrite();
            return;
        }
    }

    @Override
    public String toString() {
        return "FileSystem{" +
                "fileSystemName=" + fileSystemName +
                ",fileOffsets=" + fileOffsets +
                '}';
    }
}
