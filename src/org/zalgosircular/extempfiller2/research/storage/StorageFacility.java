package org.zalgosircular.extempfiller2.research.storage;

import org.zalgosircular.extempfiller2.messaging.OutMessage;
import org.zalgosircular.extempfiller2.research.Article;
import org.zalgosircular.extempfiller2.research.Topic;
import org.zalgosircular.extempfiller2.research.formatting.ArticleFormatter;

import java.util.List;
import java.util.Queue;

/**
 * Created by Walt on 7/8/2015.
 */
public abstract class StorageFacility {
    protected final Queue<OutMessage> outQueue;
    protected final ArticleFormatter formatter;

    // so we can use different formatters with the same facility
    protected StorageFacility(Queue<OutMessage> outQueue, ArticleFormatter formatter) {
        this.formatter = formatter;
        this.outQueue = outQueue;
    }

    public abstract boolean open();

    public abstract boolean close();

    public abstract boolean exists(String topic);

    public abstract List<Topic> loadResearched();

    public abstract List<Topic> getResearched(); // this one (should) load from cache

    public abstract boolean save(Topic topic, Article article);

    public abstract boolean saveMultiple(Topic topic, List<Article> articles);

    public abstract boolean delete(Topic topic);
}
