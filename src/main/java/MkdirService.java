import org.jsoup.nodes.Element;

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