import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CdService extends IService {
    @Override
    public ReturnState service(Element element, String... args) {
        if (args[0].equals("..")) {
            if (element.tagName().equals("filesystem")) {
                return new ReturnState(null, null, ReturnState.ReturnNull);
            } else {
                Element elementParent = element.parent();
                return new ReturnState(elementParent, null, ReturnState.ReturnOK);
            }
        } else {
            Element element1 = findNameEquals(element, args[0], true);
            if (element1 == null) {
                return new ReturnState(null, null, ReturnState.ReturnNull);
            } else {
                return new ReturnState(element1, null, ReturnState.ReturnOK);
            }
        }
    }
}
