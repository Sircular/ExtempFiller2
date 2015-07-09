package org.zalgosircular.extempfiller2.research.fetching;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Queue;

/**
 * Created by Walt on 7/8/2015.
 */
public abstract class URLFetcher {
    protected Queue outQueue;

    public URLFetcher(Queue outQueue) {
        this.outQueue = outQueue;
    }

    public abstract List<URI> fetchURLs(String topic, int maxResults, List<String> excludes) throws IOException;
}
