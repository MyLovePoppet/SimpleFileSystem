import org.jsoup.nodes.Element;

public class ReturnState {
    final static int ReturnOK = 0;
    final static int ReturnNull = -1;
    final public Element returnElement;
    final public String returnMessage;
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
