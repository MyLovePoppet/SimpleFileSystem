import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public abstract class IService {

    /**
     * 文件系统操作接口
     * @param element 对象目录（XML文件内对应一个节点，即为Element）
     * @param args 操作的参数，如ls操作就没有参数，cd 操作要有目标目录名，故有参数
     * @return  返回的结果，不同接口返回的数据虽然不同但是共同的地方抽象成了一个类。
     */
    abstract ReturnState service(Element element, String... args);

    static Element findNameEquals(Element element, String name, boolean isDir) {
        if (element.children().size() > 0) {
            Element elementFirst = element.child(0);
            if (isNameEquals(elementFirst, name, isDir))
                return elementFirst;
            Elements elements = elementFirst.siblingElements();
            if (isDir) {
                for (Element element1 : elements) {
                    if ("文件夹".equals(element1.tagName()) && element1.attr("名称").equals(name)) {
                        return element1;
                    }
                }
            } else {
                for (Element element1 : elements) {
                    if ("文件".equals(element1.tagName()) && element1.attr("名称").equals(name)) {
                        return element1;
                    }
                }
            }
        }
        return null;
    }

    static Element findNameEquals(Elements elements, String name, boolean isDir) {
        if (isDir) {
            for (Element element : elements) {
                if ("文件夹".equals(element.tagName()) && element.attr("名称").equals(name)) {
                    return element;
                }
            }
        } else {
            for (Element element : elements) {
                if ("文件".equals(element.tagName()) && element.attr("名称").equals(name)) {
                    return element;
                }
            }
        }
        return null;
    }

    static boolean isNameEquals(Element element, String name, boolean isDir) {
        if (isDir) {
            return "文件夹".equals(element.tagName()) && element.attr("名称").equals(name);
        } else {
            return "文件".equals(element.tagName()) && element.attr("名称").equals(name);
        }
    }
}

