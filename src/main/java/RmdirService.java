import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class RmdirService extends IService{

    @Override
    ReturnState service(Element element, String... args) {
        Element element1=findNameEquals(element,args[0],true);
        if(element1==null)
            return new ReturnState(null,null,ReturnState.ReturnNull);
        else
            return new ReturnState(element1,null,ReturnState.ReturnOK);
    }
}
