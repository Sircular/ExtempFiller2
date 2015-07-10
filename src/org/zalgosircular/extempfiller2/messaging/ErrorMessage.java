package org.zalgosircular.extempfiller2.messaging;

import org.zalgosircular.extempfiller2.research.Topic;

/**
 * Created by Walt on 7/8/2015.
 */
public class ErrorMessage {
    private final Topic topic;
    private final Exception ex;

    public ErrorMessage(Topic topic, Exception ex) {
        this.topic = topic;
        this.ex = ex;
    }

    public Topic getTopic() {
        return topic;
    }

    public Exception getException() {
        return ex;
    }

    @Override
    public String toString() {
        return String.format("Error(%s, %s)",
                topic == null ? "null" : topic.toString(),
                ex == null ? "null" : ex.toString());
    }
}
