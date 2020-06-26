姓名：舒钦瑜    学号：2017152044    操作系统大作业

1.	创建一个100M的文件或者创建一个100M的共享内存
2.	尝试自行设计一个C语言小程序，使用步骤1分配的100M空间（共享内存或mmap），然后假设这100M空间为一个空白磁盘，设计一个简单的文件系统管理这个空白磁盘，给出文件和目录管理的基本数据结构，并画出文件系统基本结构图，以及基本操作接口。（20分）
3.	在步骤1的基础上实现部分文件操作接口操作，创建目录mkdir，删除目录rmdir，修改名称，创建文件open，修改文件，删除文件rm，查看文件系统目录结构ls。（30分）
4.	参考进程同步的相关章节，通过信号量机制实现多个终端对上述文件系统的互斥访问，系统中的一个文件允许多个进程读，不允许写操作；或者只允许一个写操作，不允许读。（20分）
5.	实验报告书写质量（30分）

## 创建一个100M的文件
我们除了需要创建这个100MB的文件之外，我们还得创建如之前所说的一个xml配置文件来记录信息。我们设计一个FileSystem类来进行文件系统的操作，我们可以在其构造函数内进行初始化是读入以前创建好的文件系统或者是创建一个新的文件系统，即函数申明如下：
```Java
/**
  * 构造函数
  * @param fileSystemName 文件系统命名
  * @param isCreate 是否为新建文件系统，false时为读入，true为新创建
  * @throws IOException 可能会抛出IOException
  */
public FileSystem(String fileSystemName, boolean isCreate) throws IOException 
```
而创建文件在Java内为的操作`File.createNewFile()`，创建完成后，我们还需要将文件系统文件的大小设置为100MB，这里我们可以使用`RandomAccessFile.setLength()`方法来设置文件的长度，参数为100 * 1024 * 1024，表示100MB。代码实现如下：
```Java
if (isCreate) {
    //创建新文件
    xmlFile.createNewFile();
    fileSystem.createNewFile();
    //xml读入配置文件
    fileSystemXMLDoc = Jsoup.parse(xmlFileInputStream, "UTF-8", "", Parser.xmlParser());
    //"rw"表示可读可写
    fileSystemFile = new RandomAccessFile(fileSystem, "rw");
    //设置大小为为100MB
    fileSystemFile.setLength(FileSystemSize);
} else {
    //否则直接读入数据即可
    fileSystemXMLDoc = Jsoup.parse(xmlFileInputStream, "UTF-8", "", Parser.xmlParser());
    fileSystemFile = new RandomAccessFile(fileSystem, "rw");
    parseFileOffsets();
}
```
如下图为我们创建的xml配置文件以及100MB的文件系统文件：

![](https://i.niupic.com/images/2020/06/26/8jZu.png)

# 文件系统数据结构以及操作接口

对于我们的文件系统的操作主要是维护一个xml信息文件和一个100MB的文件系统文件。我们需要使用一个xml文件来模拟我们的文件系统的记录的文件信息，文件内的真正的数据在我们的100MB的文件系统内。

后续增加文件，删除文件的时候主要重新更新该xml文件以及在我们的100MB的文件系统内进行更新数据即可。一个xml的例子如下图所示：
```xml
<?xml version="1.0" encoding="utf-8"?>
<FileSystem>
    <文件 文件大小="200" 文件起始="0" 文件结束="200" 名称="A文件"></文件>
    <文件夹 名称="A文件夹">
        <文件 文件大小="1024" 文件起始="200" 文件结束="1224" 名称="B文件.txt"></文件>
        <文件夹 名称="B文件夹">
            <文件 文件大小="64" 文件起始="1224" 文件结束="1288" 名称="test.cpp"></文件>
        </文件夹>
        <文件 文件大小="2048" 文件起始="1288" 文件结束="3336" 名称="Hello world.exe"></文件>
    </文件夹>
</FileSystem>
```
该xml文件表示主目录下存在一个大小为200的以“A文件”为文件名的文件。同样的，在根目录下还存在一个A文件夹，A文件夹的子节点就代表A文件夹内的数据信息，有一个文件名为“B文件.txt”和一个名为“B文件夹”的文件夹，在该B文件夹内还有一个test.cpp的文件。除此之外，与B文件夹同级的还有一个Hello world.exe的文件。

对于每个文件节点，我们都需要在这个节点的Attribute属性上记录起始地址，结束地址，文件大小以及文件名称等必要信息，我们在我们的100MB的文件系统文件内的起始地址和结束地址之间的数据就是A文件的数据所在的位置，以便后续记录数据。但是对于文件夹节点而言，我们只需要对其进行记录一个文件名即可。

对于xml文件的修改我们使用的是Java的Jsoup库，其能够比较方便的进行xml文件数据的读取以及写入。对于100MB的文件记录数据的话，我们使用的是Java的RandomAccessFile，其可以比较方便的在文件的任意位置进行数据的读取和写入。

对于文件系统的操作接口我们主要有一个IService接口，用于后续的真实进行文件操作进行继承。
```Java
public abstract class IService{
    /**
    * 文件系统操作接口
    * @param element 对象目录（XML文件内对应一个节点，即为Element）
    * @param args 操作的参数，如ls操作就没有参数，cd 操作要有目标目录名，故有参数
    * @return  返回的结果，不同接口返回的数据虽然不同但是共同的地方抽象成了一个类。
    */
    abstract ReturnState service(Element element, String... args);
}
```
其中ReturnState作为不同操作类型返回的数据的集合，如ls操作只需要返回一个字符串即可，cd操作还需要返回目标目录的对象，等等。虽然这些不同的操作返回不同的数据，但是我们将其抽象成了一个集合对象，各个不同的操作接口按需要进行填充返回数据，其定义如下：
```Java
/**
 * 不同的操作返回的不同的数据的一个集合
 */
public class ReturnState {
    //总体是否成功操作的一个标志
    final static int ReturnOK = 0;
    final static int ReturnNull = -1;
    //返回的节点（目录或者是对象，cd和open等操作都需要用到）
    final public Element returnElement;
    //返回的消息，ls操作等需要用到
    final public String returnMessage;
    //总体是否成功操作的一个标志，取值为之前的ReturnOK与ReturnNull
    final public int returnCode;
}
```
其中类的结构关系图如下：

![](https://i.niupic.com/images/2020/06/26/8k0j.jpg)

后续我们的真正的操作只需要对各自的接口进行操作即可。

# 文件系统接口操作实现
## ls操作
ls的命令操作为`ls`，表示输出当前目录下的文件夹和文件信息。

ls操作主要是输出当前目录下的所有的文件或者是文件夹信息。Jsoup进行解析xml文件时没有直接获取下一层的所有节点（只能获取所有的子节点（即下层节点的子节点也被返回）），所以如果我们需要找到所有的下面一层的子节点的话可以先找到其第一个子节点，然后在找出其所有的兄弟节点，作为我们下一层的所有的节点，然后我们将这个节点的所有的Attribute信息输出即可（因为我们文件的具体信息是存放在Attribute属性内的）。具体的代码实现如下：
```Java
public class LsService extends IService {
    @Override
    public ReturnState service(Element element, String... args) {
        StringBuilder stringBuilder = new StringBuilder();
        //如果当前节点的子节点的数目大于0
        if (element.children().size() > 0) {
            //获取第一个子节点
            Element elementFirst = element.child(0);
            //将第一个子节点的Attribute信息输出
            stringBuilder.append(elementFirst.attributes()).append('\n');
            //第一个子节点的所有兄弟节点，即为当前的目录下所有的文件和文件夹信息
            Elements elements = elementFirst.siblingElements();
            for (Element element1 : elements) {
                stringBuilder.append(element1.attributes()).append('\n');
            }
        }
        //输出空信息，此时不用返回null，直接输出的信息为空信息
        return new ReturnState(null, stringBuilder.toString(), ReturnState.ReturnOK);
    }
}
```
我们用之前的测试用的xml文件进行测试使用ls命令运行结果如下：

![](https://i.imgur.com/E8CZb3q.png)

## cd操作
我们的cd操作命令类似于`cd A文件夹`或者`cd ..`分别表示进入A文件夹和返回上一层。

cd操作主要有两大块内容，一个是`"cd .."`这种返回上一层目录的信息，还有一种是正常的`"cd A文件夹"`这种进入下一层目录的信息。所以我们先进行判断cd的参数（即需要进入的目录）是不是“..”表示上一层，或者是进入下一层目录来进行不同的操作。

如果是进入上一层，还需要判断目录是不是根目录，如果是根目录的话就不用继续进入，否则使用Element的parent()方法返回其父亲节点，即为上一层目录。

如果是进入下一层，那么需要先判断我们的cd的参数（即需要进入的目录）是不是在我们的文件系统内存在，如果不存在也要返回错误信息，以便处理，如果存在，那么将该找到的需要进入的目录节点赋值给当前的目录节点。具体的代码实现如下：

```Java
public class CdService extends IService {
    @Override
    public ReturnState service(Element element, String... args) {
        //如果是返回上一层节点
        if (args[0].equals("..")) {
            //是不是根目录
            if (element.tagName().equals("filesystem")) {
                return new ReturnState(null, "当前为根目录", ReturnState.ReturnNull);
            } else {
                //返回上一层
                Element elementParent = element.parent();
                return new ReturnState(elementParent, "OK", ReturnState.ReturnOK);
            }
        } else {
            //先找到是不是存在这个目录名，再进行对应的操作
            Element element1 = findNameEquals(element, args[0], true);
            if (element1 == null) {
                return new ReturnState(null, "目标目录为空", ReturnState.ReturnNull);
            } else {
                return new ReturnState(element1, "OK", ReturnState.ReturnOK);
            }
        }
    }
}
```
除了基本的cd目录功能实现之外，我们还需要进行输出当前的目录信息，和Linux的cmd是类似的功能。这个实现起来也比较简单，我们使用一个容器来存放我们的当前的目录信息，如果使用cd 进入下一层目录，那么我们就可以将下一层目录的名称加到容器的结尾，否则如果是进入上一层目录的话，我们就将容器最后的一个元素删除，来达到输出当前目录信息的目的。我们使用cd命令运行如下图所示：

![](https://i.imgur.com/IjfIfyT.png)

## mkdir操作
我们的mkdir的命令为`mkdir A文件夹`表示在当前目录下创建一个A文件夹。

这里我们的做法就比较简单了，在函数内先进行判断是否有相同的目录名，如果有相同的目录名，那么就不能创建，否则就可以创建目录。具体的代码如下：
```Java
public class MkdirService extends IService {
    @Override
    ReturnState service(Element element, String... args) {
        //找到是否有相同的目录名
        Element element1 = findNameEquals(element, args[0], true);
        //没有相同的目录名，可创建
        if (element1 == null)
            return new ReturnState(null, "OK", ReturnState.ReturnOK);
        //有相同的目录，不可创建
        else
            return new ReturnState(null, "重名文件夹", ReturnState.ReturnNull);
    }
}
```
接着我们进行添加文件夹操作，我们只需要在当前目录节点下创建一个如下的节点即可：
```xml
<文件夹 名称="$用户输入的名称$"></文件夹>
```
而插入节点的方法我们可以直接使用Element.appendChild()方法进行添加节点，具体的代码如下：
```Java
//如果可以创建
if (mkdirReturnState.returnCode == ReturnState.ReturnOK) {
    Element element = new Element("文件夹");
    element.attr("名称", strings[1]);
    //添加文件夹
    currentElement.appendChild(element);
    updateXMLFile();
    //否则提示已经有同名目录了
} else {
    System.out.println("Already has a dir named as '" + strings[1] + "'! Please check!");
}
```
我们的测试运行结果如下图所示：

![](https://i.imgur.com/bBG4G6w.png)
## open操作
我们的open操作的命令类似于`open test1.cpp Hello,world,2017152044_舒钦瑜`表示我们创建一个在当前目录下创建一个test1.cpp，并初始化数据为`Hello,world,2017152044_舒钦瑜`

前面都是只涉及到目录变化，而目录的变化无需反映到文件系统的文件内，但是open操作需要结合xml配置文件和文件系统的文件，所以我们需要先在xml文件内创建文件节点，然后在文件系统文件内将用户输入出要写入到文件信息的数据写入到文件系统内。

首先我们需要判断用户需要创建的文件是否已经存在，然后我们需要在我们的文件系统内找到合适用户创建文件大小的位置，这里使用的是首次适应算法，即从头开始找，看有没有空出来的位置给用户进行创建新文件。具体的寻找位置的代码如下：
```Java
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
```
找到可以进行放入用户文件大小的位置之后，我们还需要先判断该目录下有没有同名的文件，如果有同名的文件就提示文件名冲突，否则我们就在xml文件内更新我们的配置文件。
```Java
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
    writeService.service(element1, name, writeValue);
    //更新xml文件
    updateXMLFile();
}
```
接下来我们将数据写入到我们的文件系统内，这里使用的是`RandomAccessFile`的write方法，就可以将用户需要输入的数据输入到文件系统内。具体的代码实现如下：
```Java
//需要写入的数据
byte[] bytes = args[1].getBytes();
//文件指针移到目标位置。
randomAccessFile.seek(newPosition);
//写入数据
randomAccessFile.write(bytes);
```
我们的测试与后面的read操作一起进行。
## read操作
read操作的命令类似于`read test1.cpp`，表示将test1.cpp内的内容输出到控制台。

这一部分也比较简单了，和之前的Open操作是相类似的，都是先找到有没有用户输入的文件，如果找到了，那么我们再通过xml配置文件内的信息找到文件系统内的起始地址，文件大小和文件结束，然后将其全部读取出来，然后输出到控制台上。具体的代码实现如下：
```Java
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

/**
 * 从一个节点读取文件信息
 * @param element 需要读取数据的文件节点
 * @return 文件内的数据
 */
byte[] read(Element element){
    //找到位置
    long start = Long.parseLong(element.attr("文件起始"));
    long end = Long.parseLong(element.attr("文件结束"));
    long size = Long.parseLong(element.attr("文件大小"));
    byte[]bytes=new byte[(int)size];
    try {
        //移到对应的位置
        randomAccessFile.seek(start);
        //读取数据
        randomAccessFile.read(bytes,0,(int)size);
    } catch (IOException e) {
        e.printStackTrace();
    }
    return bytes;
}
```
我们与之前的Open操作一起测试如下：

![](https://i.imgur.com/mqy4PlV.png)

## write操作
我们的write操作命令类似于`write test1.cpp 舒钦瑜_2017152044`

其实这部分内容就是之前的open操作的后半部分，省去了创建文件的过程，所以我们的操作也是类似的，需要先检查文件是否存在，如果存在的话我们需要检查用户输入的数据所改变的文件大小，同样的，我们也需要进行寻找一块可以容纳用户输入数据大小的地址，然后将我们的文件挂载到目的区域。具体的代码实现如下：
```Java
String name = strings[1];
StringBuilder stringBuilder = new StringBuilder();
for (int i = 2; i < strings.length; i++) {
    stringBuilder.append(strings[i]);
}
//需要写入的数据
String writeValue = stringBuilder.toString();
//找到xml所在的位置
Element element = IService.findNameEquals(currentElement, name, false);
if (element == null) {
    System.out.println("No file named as '" + name + "'! Please check!");
} else {
    long start = Long.parseLong(element.attr("文件起始"));
    long end = Long.parseLong(element.attr("文件结束"));
    long size = Long.parseLong(element.attr("文件大小"));
    //先把原来位置删除
    fileOffsets.remove(new FileOffset(start, end));
    long newSize = writeValue.getBytes().length;
    long newStart = findLocationForFile(newSize);
    long newEnd = newStart + newSize;
    //再找到新的位置
    fileOffsets.add(findIndexOfLocation(newStart), new FileOffset(newStart, newEnd));
    //写入数据
    writeService = new WriteService(fileSystemFile, newStart);
    writeService.service(element, name, writeValue);
    //更新xml文件
    updateXMLFile();
}
```
我们的测试结果如下：

![](https://i.imgur.com/Q6wzT6H.png)

# 信号量互斥访问
读者进程只读数据区中的数据，写者进程只往数据区写数据，要求满足的条件有：
1. 允许多个读者同时执行读操作
2. 不允许多个写者同时操作
3. 不允许读者、写者同时操作
   
我们使用的是读者优先模式，其特点如下：

1. 无其他读者、写者，该读者可以读
2. 若已有写者等待，但有其他读者正在读，则该读者也可以读
3. 若有写者正在写，则读者或者其他写者必须等

反应到代码里面，Java也支持信号量的创建与使用，对应到Java内的对象为Semaphore，通过acquire进行P操作，通过release进行V操作。我们将读写锁写成一个类如下：

```Java
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
        //只有第一次获得读锁时才将写锁锁上
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
        //没有人读了，释放写锁
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
```
反应到文件系统内，我们为每一个文件（以当前目录+文件名作为键），然后对应的一个上述的读写锁。这里我使用的是哈希表进行存储。
`static Map<String,ReadWriteSemaphore>mutex=new Hashtable<>();`
然后每次进行读取或者写入操作时均需要获得对应的锁，然后再进行操作。
读取的操作表示为：
```Java 
//如果改文件没有对应的读写锁，创建一个新的读写锁
if(!mutex.containsKey(currentDir+strings[1])){
    mutex.put(currentDir+strings[1],new ReadWriteSemaphore());
}
//获取读锁
mutex.get(currentDir+strings[1]).acquireRead();
//读取操作...

//释放读锁
mutex.get(currentDir+strings[1]).releaseRead();
```
写入的操作如下：
```Java
//如果改文件没有对应的读写锁，创建一个新的读写锁
if(!mutex.containsKey(currentDir+name)){
    mutex.put(currentDir+name,new ReadWriteSemaphore());
}
//获取写锁
mutex.get(currentDir+name).acquireWrite();
//写入操作...

//释放写锁
mutex.get(currentDir+name).releaseWrite();
```
我们同时对该文件系统创建两个线程进行读取和写入操作，进行测试我们的读写锁是否有效：


![](https://i.imgur.com/Ngj2ZsX.png)

第一行绿色为第一个线程先写入数据，
第二行绿色为第二个线程尝试读入数据，然后输出等待写完成的提示信息。

![](https://i.imgur.com/iMYJXs8.png)

第一行绿色为第一个线程读入数据，第二行绿色为第二个线程读入数据，可以看到没有冲突信息的提示。

![](https://i.imgur.com/6RjbtWx.png)

此时为第一行绿色表示读先进行，然后第二行表示第二个线程进行读取，然后提示等待读取或者写入完成，因为是读者优先的情况，所以写入线程在等待时有可能是其他线程在读数据，也有可能是其他线程在写入数据，所以才会提示等待读取或者写入数据。

到此为止我们的文件系统已经完成了。

# 实验感想
本次实验由于时间有限，而且不是特别熟悉C语言，所以在征得老师的同意下才选择使用比较熟悉的Java进行文件系统的实现，在实现过程中也学会到了很多内容，如基本的文件系统的管理，文件的创建，读取和写入，以及对基本的文件系统的命令操作也有所了解了，以及通过信号量来实现了对文件的互斥访问读写，总的来说收获还是很大，对整个操作系统学期学到的内容都复习了一遍，后续如果有时间的话还是想使用C语言来自己实现一遍，来巩固一下操作系统的知识。