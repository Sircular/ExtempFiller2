package org.zalgosircular.extempfiller2.research.storage;

import org.zalgosircular.extempfiller2.messaging.OutMessage;
import org.zalgosircular.extempfiller2.research.Article;
import org.zalgosircular.extempfiller2.research.Topic;

import java.util.List;
import java.util.Queue;

/**
 * Created by Walt on 7/8/2015.
 */
public abstract class StorageFacility {
    protected final Queue<OutMessage> outQueue;

    public StorageFacility(Queue<OutMessage> outQueue) {
        this.outQueue = outQueue;
    }

    public abstract boolean open();

    public abstract boolean close();

    public abstract boolean exists(String topic);

    public abstract List<Topic> load();

    public abstract boolean save(Topic topic, Article article);

    public abstract boolean saveMultiple(Topic topic, List<Article> articles);

    public abstract boolean delete(Topic topic);
}
