package org.zalgosircular.extempfiller2.messaging;

import org.zalgosircular.extempfiller2.research.Topic;

/**
 * Created by Walt on 7/8/2015.
 */
public class InMessage {
    private Type messageType;
    private Object data;

    public InMessage(Type messageType, Object data) throws RuntimeException {
        if (data != null && !messageType.getDataType().isInstance(data)) {
            throw new RuntimeException("Improper data type for message type");
        }
        this.messageType = messageType;
        this.data = data;
    }

    public Type getMessageType() {
        return messageType;
    }

    public Object getData() {
        return data;
    }

    @Override
    public String toString() {
        return String.format("Message(%s, %s)", messageType.name(),
                data == null ? "null" :
                        messageType.getDataType().cast(data).toString());
    }

    /**
     * Created by Walt on 7/8/2015.
     */
    public static enum Type {
        LOAD(Object.class), // reload from "filesystem"
        GET(Object.class), // get from cache if possible
        RESEARCH(Topic.class),
        DELETE(String.class),
        OPEN(Object.class),
        CLOSE(Object.class);

        private final Class dataType;

        Type(Class dataType) {
            this.dataType = dataType;
        }

        public Class getDataType() {
            return dataType;
        }
    }
}
