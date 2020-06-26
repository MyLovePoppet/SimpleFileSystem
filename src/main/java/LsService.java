import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
