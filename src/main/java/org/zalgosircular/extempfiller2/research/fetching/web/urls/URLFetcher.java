package org.zalgosircular.extempfiller2.research.fetching.web.urls;

import org.zalgosircular.extempfiller2.messaging.OutMessage;
import org.zalgosircular.extempfiller2.research.Topic;

import java.net.URI;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Walt on 7/8/2015.
 */
public abstract class URLFetcher {
    // each implementation can handle this stream differently
    protected final BlockingQueue<OutMessage> outQueue;
    protected final Topic topic;
    protected final List<String> excludes;

    protected URLFetcher(BlockingQueue<OutMessage> outQueue,
                         Topic topic,
                         List<String> excludes) {
        this.outQueue = outQueue;
        this.topic = topic;
        this.excludes = excludes;
    }

    public abstract URI getNext() throws InterruptedException;

    public abstract boolean hasNext() throws InterruptedException;
}
