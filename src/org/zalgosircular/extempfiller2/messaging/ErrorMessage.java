package org.zalgosircular.extempfiller2.messaging;

import org.zalgosircular.extempfiller2.research.Topic;

/**
 * Created by Walt on 7/8/2015.
 */
public class ErrorMessage {
    private final Topic topic;
    private final SEVERITY severity;
    private final Exception ex;

    public ErrorMessage(Topic topic, SEVERITY severity, Exception ex) {
        this.topic = topic;
        this.severity = severity;
        this.ex = ex;
    }

    public Topic getTopic() {
        return topic;
    }

    public SEVERITY getSeverity() {
        return severity;
    }

    public Exception getException() {
        return ex;
    }

    @Override
    public String toString() {
        return String.format("[%s] (%s, %s)",
                severity.name(),
                topic == null ? "null" : topic.toString(),
                ex == null ? "null" : ex.toString());
    }

    public enum SEVERITY {
        ALERT,
        CRITICAL,
        ERROR,
        WARNING,
        NOTICE
    }
}
