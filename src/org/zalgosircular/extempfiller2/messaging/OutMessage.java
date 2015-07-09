package org.zalgosircular.extempfiller2.messaging;

import com.sun.javaws.exceptions.InvalidArgumentException;
import org.zalgosircular.extempfiller2.research.Topic;

import java.util.List;

/**
 * Created by Walt on 7/8/2015.
 */
public class OutMessage {
    private Type messageType;
    private Object data;
    public OutMessage(Type messageType, Object data) throws InvalidArgumentException {
        if (!messageType.getDataType().isInstance(data)) {
            throw new InvalidArgumentException(new String[] {"Improper data type for message type"});
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
    public  String toString() {
        return String.format("Message(%s, %s)", messageType.name(), messageType.getDataType().cast(data).toString());
    }

    /**
     * Created by Walt on 7/8/2015.
     */
    public static enum Type {
        DEBUG(String.class),
        SEARCHING(Topic.class),
        SAVING(Topic.class),
        DONE(Topic.class),
        ERROR(Error.class),
        LOADING(Object.class), // because we pass null
        LOADED(List.class),
        DELETING(Topic.class),
        DELETED(Topic.class);

        private final Class dataType;
        Type(Class dataType) {
            this.dataType = dataType;
        }

        public Class getDataType() {
            return dataType;
        }
    }
}