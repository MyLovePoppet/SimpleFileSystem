import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CdService extends IService {
    @Override
    public ReturnState service(Element element, String... args) {
        //如果是返回上一层节点
        if (args[0].equals("..")) {
            //是不是根目录
            if (element.tagName().toLowerCase().equals("filesystem")) {
                return new ReturnState(null, "根目录", ReturnState.ReturnNull);
            } else {
                //返回上一层
                Element elementParent = element.parent();
                return new ReturnState(elementParent, "OK", ReturnState.ReturnOK);
            }
        } else {
            //先找到是不是存在这个文件名，再进行对应的操作
            Element element1 = findNameEquals(element, args[0], true);
            if (element1 == null) {
                return new ReturnState(null, "目标目录为空", ReturnState.ReturnNull);
            } else {
                return new ReturnState(element1, "OK", ReturnState.ReturnOK);
            }
        }
    }
}
