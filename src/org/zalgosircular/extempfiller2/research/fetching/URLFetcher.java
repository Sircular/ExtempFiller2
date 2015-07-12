package org.zalgosircular.extempfiller2.research.fetching;

import org.zalgosircular.extempfiller2.messaging.OutMessage;
import org.zalgosircular.extempfiller2.research.Topic;

import java.net.URI;
import java.util.List;
import java.util.Queue;

/**
 * Created by Walt on 7/8/2015.
 */
public abstract class URLFetcher {
    protected final Queue<OutMessage> outQueue;

    public URLFetcher(Queue<OutMessage> outQueue) {
        this.outQueue = outQueue;
    }

    public abstract List<URI> fetchURLs(Topic topic, List<String> excludes);
}
