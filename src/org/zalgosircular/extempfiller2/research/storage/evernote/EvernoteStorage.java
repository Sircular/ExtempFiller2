package org.zalgosircular.extempfiller2.research.storage.evernote;

import org.zalgosircular.extempfiller2.research.Article;
import org.zalgosircular.extempfiller2.research.Topic;
import org.zalgosircular.extempfiller2.research.storage.StorageFacility;

import java.util.List;
import java.util.Queue;

/**
 * Created by Logan Lembke on 7/8/2015.
 */
public class EvernoteStorage extends StorageFacility {
    public EvernoteStorage(Queue outQueue) {
        super(outQueue);
    }

    @Override
    public boolean open() {
        return false;
    }

    @Override
    public boolean close() {
        return false;
    }

    @Override
    public boolean exists(String topic) {
        return false;
    }

    @Override
    public List<Topic> load() {
        return null;
    }

    @Override
    public boolean save(Topic topic, Article article) {
        return false;
    }

    @Override
    public boolean saveMultiple(Topic topic, List<Article> articles) {
        return false;
    }

    @Override
    public boolean delete(Topic topic) {
        return false;
    }
}
