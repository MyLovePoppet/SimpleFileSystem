import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class LsService extends IService {
    @Override
    public ReturnState service(Element element, String... args) {
        StringBuilder stringBuilder = new StringBuilder();
        if (element.children().size() > 0) {
            Element elementFirst = element.child(0);
            stringBuilder.append(elementFirst.attributes()).append('\n');
            Elements elements = elementFirst.siblingElements();
            for (Element element1 : elements) {
                stringBuilder.append(element1.attributes()).append('\n');
            }
        }
        return new ReturnState(null, stringBuilder.toString(), ReturnState.ReturnOK);
    }
}
