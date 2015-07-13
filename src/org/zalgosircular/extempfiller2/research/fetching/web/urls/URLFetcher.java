package org.zalgosircular.extempfiller2.research.fetching.web.urls;

import org.zalgosircular.extempfiller2.messaging.OutMessage;
import org.zalgosircular.extempfiller2.research.Topic;

import java.net.URI;
import java.util.List;
import java.util.Queue;

/**
 * Created by Walt on 7/8/2015.
 */
public abstract class URLFetcher {
    // todo: redo this class to be more "stream-like"
    // each implementation can handle this stream differently
    protected final Queue<OutMessage> outQueue;
    protected final Topic topic;
    protected final List<String> excludes;

    protected URLFetcher(Queue<OutMessage> outQueue,
                         Topic topic,
                         List<String> excludes) {
        this.outQueue = outQueue;
        this.topic = topic;
        this.excludes = excludes;
    }

    public abstract URI getNext();

    public abstract boolean hasNext();
}
