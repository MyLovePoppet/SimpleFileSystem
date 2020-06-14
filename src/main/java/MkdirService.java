import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MkdirService extends IService {
    @Override
    ReturnState service(Element element, String... args) {
        Element element1 = findNameEquals(element, args[0], true);
        if (element1 == null)
            return new ReturnState(null, null, ReturnState.ReturnOK);
        else
            return new ReturnState(null, null, ReturnState.ReturnNull);
    }
}