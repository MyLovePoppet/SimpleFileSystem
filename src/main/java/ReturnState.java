import org.jsoup.nodes.Element;

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

    public ReturnState(Element returnElement, String returnMessage, int returnCode) {
        this.returnElement = returnElement;
        this.returnMessage = returnMessage;
        this.returnCode = returnCode;
    }

    @Override
    public String toString() {
        return "ReturnState{" +
                "returnElement=" + returnElement +
                ", returnMessage='" + returnMessage + '\'' +
                ", returnCode=" + returnCode +
                '}';
    }
}
