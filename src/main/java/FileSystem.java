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

    public FileSystem(String fileSystemName, boolean isCreate) throws IOException {
        this.fileSystemName = fileSystemName;
        fileOffsets = new ArrayList<>();
        File fileSystem = new File(fileSystemName + FileSystemPrefix);
        File xmlFile = new File(fileSystemName + XMLPrefix);
        FileInputStream xmlFileInputStream = new FileInputStream(xmlFile);
        if (isCreate) {
            xmlFile.createNewFile();
            fileSystem.createNewFile();
            fileSystemXMLDoc = Jsoup.parse(xmlFileInputStream, "UTF-8", "", Parser.xmlParser());
            fileSystemFile = new RandomAccessFile(fileSystem, "rw");
            fileSystemFile.setLength(FileSystemSize);
        } else {
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

    public long findLocationForFile(long fileSize) {
        if (fileOffsets.size() == 0)
            return 0;
        for (int i = 0; i < fileOffsets.size() - 1; i++) {
            if ((fileOffsets.get(i + 1).start - fileOffsets.get(i).end) >= fileSize) {
                return fileOffsets.get(i).end;
            }
        }
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
            ReturnState readReturnState = mkdirService.service(currentElement, strings[1]);
            if (readReturnState.returnCode == ReturnState.ReturnOK) {
                Attributes attributes = new Attributes();
                Element element = new Element("文件夹");
                element.attr("名称", strings[1]);
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
                Elements elements = rmdirReturnState.returnElement.getAllElements();
                elements.forEach(element -> {
                    if ("文件".equals(element.tagName())) {
                        long start = Long.parseLong(element.attr("文件起始"));
                        long end = Long.parseLong(element.attr("文件结束"));
                        long size = Long.parseLong(element.attr("文件大小"));
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
            Element element = IService.findNameEquals(currentElement, name, false);
            if (element != null) {
                System.out.println("Already has file named as '" + name + "'! Please check!");
            } else {
                Element element1 = new Element("文件");
                long newSize = writeValue.getBytes().length;
                long newStart = findLocationForFile(newSize);
                long newEnd = newStart + newSize;
                currentElement.appendChild(element1);
                fileOffsets.add(findIndexOfLocation(newStart), new FileOffset(newStart, newEnd));
                writeService = new WriteService(fileSystemFile, newStart);
                writeService.service(element1, name, writeValue);
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
