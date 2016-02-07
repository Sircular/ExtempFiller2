package org.zalgosircular.extempfiller2.research.storage;

import org.zalgosircular.extempfiller2.messaging.OutMessage;
import org.zalgosircular.extempfiller2.research.Article;
import org.zalgosircular.extempfiller2.research.Topic;
import org.zalgosircular.extempfiller2.research.formatting.ArticleFormatter;

import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Walt on 7/8/2015.
 */
public abstract class StorageFacility {
    protected final BlockingQueue<OutMessage> outQueue;
    protected final ArticleFormatter formatter;

    // so we can use different formatters with the same facility
    protected StorageFacility(BlockingQueue<OutMessage> outQueue, ArticleFormatter formatter) {
        this.formatter = formatter;
        this.outQueue = outQueue;
    }

    public abstract boolean open() throws InterruptedException;

    public abstract boolean close() throws InterruptedException;

    public abstract boolean exists(String topic) throws InterruptedException;

    public abstract List<Topic> loadResearched() throws InterruptedException;

    public abstract List<Topic> getResearched() throws InterruptedException; // this one (should) load from cache

    public abstract Topic getTopic(String topic) throws InterruptedException;

    public abstract boolean save(Topic topic, Article article) throws InterruptedException;

    public abstract boolean saveMultiple(Topic topic, List<Article> articles) throws InterruptedException;

    public abstract boolean delete(Topic topic) throws InterruptedException;
}
