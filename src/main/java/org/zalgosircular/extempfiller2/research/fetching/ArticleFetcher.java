package org.zalgosircular.extempfiller2.research.fetching;

import org.zalgosircular.extempfiller2.messaging.OutMessage;
import org.zalgosircular.extempfiller2.research.Article;
import org.zalgosircular.extempfiller2.research.Topic;

import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Logan Lembke on 7/12/2015.
 */
public abstract class ArticleFetcher {
    protected final BlockingQueue<OutMessage> outQueue;

    protected ArticleFetcher(BlockingQueue<OutMessage> outQueue) {
        this.outQueue = outQueue;
    }

    public abstract boolean open() throws InterruptedException;
    public abstract List<Article> fetchArticles(Topic topic, int maxResults, List<String> excludes) throws InterruptedException;
}
