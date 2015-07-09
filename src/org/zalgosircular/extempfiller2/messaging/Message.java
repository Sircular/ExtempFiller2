package org.zalgosircular.extempfiller2.messaging;

/**
 * Created by Walt on 7/8/2015.
 */
public class Message {
    private Enum e;
    private Object data;
    public Message(Enum e, Object data) {
        this.e = e;
        this.data = data;
    }
    public Enum getType() {
        return e;
    }
    public Object getData() {
        return data;
    }
    @Override
    public  String toString() {
        return String.format("Message(%s, %s)", e.name(), data.toString());
    }
}
