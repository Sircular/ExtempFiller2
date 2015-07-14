package org.zalgosircular.extempfiller2.research.storage.evernote;

import com.evernote.auth.EvernoteService;
import com.evernote.edam.type.Tag;
import org.zalgosircular.extempfiller2.authentication.KeyManager;
import org.zalgosircular.extempfiller2.messaging.ErrorMessage;
import org.zalgosircular.extempfiller2.messaging.OutMessage;
import org.zalgosircular.extempfiller2.research.Article;
import org.zalgosircular.extempfiller2.research.Topic;
import org.zalgosircular.extempfiller2.research.formatting.ENMLFormatter;
import org.zalgosircular.extempfiller2.research.storage.StorageFacility;

import java.util.*;

/**
 * Created by Logan Lembke on 7/8/2015.
 */
public class EvernoteStorage extends StorageFacility {
    private EvernoteClient client;

    public EvernoteStorage(Queue<OutMessage> outQueue, ENMLFormatter formatter) {
        super(outQueue, formatter);
    }

    @Override
    public boolean open() {
        // because of the constructor, it's already open
        try {
            client = new EvernoteClient(EvernoteService.SANDBOX, KeyManager.getKey("evernote")); // sandbox for now
        } catch (Exception e) { // we'll have to fix this
            outQueue.add(
                    new OutMessage(
                            OutMessage.Type.ERROR,
                            new ErrorMessage(null, e)
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
    public boolean exists(String topic) {
        try {
            return client.getTag(topic) != null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<Topic> load() {
        try {
            Collection<Map.Entry<String, Tag>> names = client.getFullyNamedTags();
            List<Topic> topics = new LinkedList<Topic>();
            for (Map.Entry<String, Tag> entry : names) {
                Topic t = new Topic(entry.getKey());
                Tag tag = entry.getValue();
                //minus one for note with full name
                int articleCount = client.getNotesByTag(tag, 1000).size()-1;
                t.setArticleCount(articleCount);
                topics.add(t);
            }
            return topics;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean save(Topic topic, Article article) {
        try {
            Tag tag = client.getTag(topic.getTopic());
            boolean wasNull = false;
            if (tag == null) {
                wasNull = true;
                tag = client.createTag(topic.getTopic());
            }
            String contents = formatter.format(article);
            // methinks we need to reimplement this
            LinkedList<Tag> tagList = new LinkedList<Tag>();
            tagList.add(tag);
            if (wasNull) // generate skeleton ENML here
                client.createTextNote("Desired Tag Name", "<en-note><p>"+topic.getTopic()+"</p></en-note>",
                        client.getNotebook("Tag Names"), tagList);
            client.createTextNote(article.getTitle(), contents,
                    client.getNotebook("Web Notes"), tagList);
            return true;
        } catch (Exception e) { // NEEDS TO BE FIXED
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean saveMultiple(Topic topic, List<Article> articles) {
        // it's just as fast
        for (Article a : articles) {
            if(!save(topic, a))
                return false;
        }
        return true;
    }

    @Override
    public boolean delete(Topic topic) {
        try {
            Tag tag = client.getTag(topic.getTopic());
            if (tag != null) {
                // conveniently done for us
                client.deleteTag(tag);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
