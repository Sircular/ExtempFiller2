package org.zalgosircular.extempfiller2.research.storage;

import com.box.sdk.BoxAPIConnection;
import org.zalgosircular.extempfiller2.research.Article;
import org.zalgosircular.extempfiller2.research.Topic;

import java.util.List;
import java.util.Queue;

/**
 * Created by Walt on 7/9/2015.
 */
public class BoxStorage extends StorageFacility {

    private BoxAPIConnection conn;

    public BoxStorage(Queue outQueue) {
        super(outQueue);
        // yet to be properly implemented
        conn = new BoxAPIConnection("");
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
