package org.zalgosircular.extempfiller2.research.storage.evernote;

import com.evernote.auth.EvernoteService;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.type.Tag;
import org.zalgosircular.extempfiller2.authentication.KeyManager;
import org.zalgosircular.extempfiller2.messaging.ErrorMessage;
import org.zalgosircular.extempfiller2.messaging.OutMessage;
import org.zalgosircular.extempfiller2.research.Article;
import org.zalgosircular.extempfiller2.research.Topic;
import org.zalgosircular.extempfiller2.research.formatting.ENMLFormatter;
import org.zalgosircular.extempfiller2.research.storage.StorageFacility;

import java.util.*;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Logan Lembke on 7/8/2015.
 */
public class EvernoteStorage extends StorageFacility {
    private EvernoteClient client;
    private static final String RESEARCH_NOTEBOOK = "Web Notes";
    private List<Topic> topicCache = null;

    public EvernoteStorage(BlockingQueue<OutMessage> outQueue, ENMLFormatter formatter) {
        super(outQueue, formatter);
    }

    @Override
    public boolean open() throws InterruptedException {
        try {
            client = new EvernoteClient(EvernoteService.SANDBOX, KeyManager.getKey("evernote")); // sandbox for now
            final Notebook HTMLNotebook = client.getNotebook(RESEARCH_NOTEBOOK);
            if (HTMLNotebook == null) {
                client.createNotebook(RESEARCH_NOTEBOOK);
            }
        } catch (Exception e) { // we'll have to fix this
            outQueue.put(
                    new OutMessage(
                            OutMessage.Type.ERROR,
                            new ErrorMessage(null, ErrorMessage.SEVERITY.ERROR, e)
                    )
            );
        }
        return client != null;
    }

    @Override
    public boolean close() {
        // nothing to do...?
        return true;
    }

    @Override
    public boolean exists(String topic) throws InterruptedException {
        try {
            //Cached in the client
            return client.getTag(topic) != null;
        } catch (Exception e) {
            final Topic erredTopic = new Topic(topic);
            erredTopic.setArticleCount(-1);
            outQueue.put(
                    new OutMessage(
                            OutMessage.Type.ERROR,
                            new ErrorMessage(erredTopic, ErrorMessage.SEVERITY.ERROR, e)
                    )
            );
        }
        return false;
    }

    @Override
    public List<Topic> loadResearched() throws InterruptedException {
        try {
            final Collection<Map.Entry<String, Tag>> names = client.getFullyNamedTags();
            final List<Topic> topics = new LinkedList<Topic>();
            for (Map.Entry<String, Tag> entry : names) {
                Topic t = new Topic(entry.getKey());
                Tag tag = entry.getValue();
                //minus one for note with full name
                int articleCount = client.getNotesByTag(tag, 1000).size() - 1;
                t.setArticleCount(articleCount);
                topics.add(t);
            }
            // store the cache
            topicCache = topics;
            return topics;
        } catch (Exception e) {
            outQueue.put(
                    new OutMessage(
                            OutMessage.Type.ERROR,
                            new ErrorMessage(null, ErrorMessage.SEVERITY.ERROR, e)
                    )
            );
        }
        return null;
    }

    @Override
    public List<Topic> getResearched() throws InterruptedException {
        if (topicCache == null) {
            loadResearched();
        }
        return topicCache;
    }

    @Override
    public Topic getTopic(String s) throws InterruptedException {
        if (topicCache == null)
            loadResearched();
        for (Topic t : topicCache) {
            if (t.getTopic().equals(s))
                return t;
        }
        return null;
    }

    @Override
    public boolean save(Topic topic, Article article) throws InterruptedException {
        try {
            Tag tag = client.getTag(topic.getTopic());
            if (tag == null) {
                tag = client.createTag(topic.getTopic());
                topicCache.add(topic);
            }
            final String contents = formatter.format(article);
            client.createENMLNote(article.getTitle(), contents,
                    client.getNotebook(RESEARCH_NOTEBOOK), Arrays.asList(tag));
            return true;
        } catch (Exception e) {
            outQueue.put(
                    new OutMessage(
                            OutMessage.Type.ERROR,
                            new ErrorMessage(topic, ErrorMessage.SEVERITY.ERROR, e)
                    )
            );
        }
        return false;
    }

    @Override
    public boolean saveMultiple(Topic topic, List<Article> articles) throws InterruptedException {
        // it's just as fast
        for (Article a : articles) {
            if (!save(topic, a))
                return false;
        }
        return true;
    }

    @Override
    public boolean delete(Topic topic) throws InterruptedException {
        try {
            //cached in the client
            final Tag tag = client.getTag(topic.getTopic());
            if (tag != null) {
                // conveniently done for us
                client.deleteTag(tag);
            }
            topicCache.remove(topic);
            return true;
        } catch (Exception e) {
            outQueue.put(new OutMessage(OutMessage.Type.ERROR, new ErrorMessage(topic, ErrorMessage.SEVERITY.ERROR, e)));
        }
        return false;
    }
}
