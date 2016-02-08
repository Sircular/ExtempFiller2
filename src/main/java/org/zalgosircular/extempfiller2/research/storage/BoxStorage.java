package org.zalgosircular.extempfiller2.research.storage;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;
import org.zalgosircular.extempfiller2.authentication.AuthManager;
import org.zalgosircular.extempfiller2.authentication.AuthRequest;
import org.zalgosircular.extempfiller2.messaging.ErrorMessage;
import org.zalgosircular.extempfiller2.messaging.OutMessage;
import org.zalgosircular.extempfiller2.research.Article;
import org.zalgosircular.extempfiller2.research.Topic;
import org.zalgosircular.extempfiller2.research.formatting.ArticleFormatter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Walt on 2/7/2016.
 */
public class BoxStorage extends StorageFacility {
    private static final AuthRequest keyRequest = new AuthRequest(
            new String[]{"boxKey"}
    );
    BoxAPIConnection api;
    Map<String, Topic> topicMap = new HashMap<String, Topic>();

    public BoxStorage(BlockingQueue<OutMessage> outQueue, ArticleFormatter formatter) {
        super(outQueue, formatter);
    }

    @Override
    public boolean open() throws InterruptedException {
        try {
            String key = AuthManager.requestAuth(
                    outQueue, keyRequest
            ).getResponses()[0];
            api = new BoxAPIConnection(key);
        } catch (Exception ex) {
            outQueue.put(
                    new OutMessage(
                            OutMessage.Type.ERROR,
                            new ErrorMessage(
                                    null, ErrorMessage.SEVERITY.ERROR, ex
                            )
                    )
            );
        }
        return api == null;
    }

    @Override
    public boolean close() throws InterruptedException {
        return true; // nothing to do
    }

    @Override
    public boolean exists(String topic) throws InterruptedException {
        return topicMap.containsKey(topic);
    }

    @Override
    public List<Topic> loadResearched() throws InterruptedException {
        topicMap.clear();
        BoxFolder root = getExtempFolder();
        for (com.box.sdk.BoxItem.Info child : root.getChildren()) {
            String topicName = child.getName();
            int articleCount = 0;
            if (child instanceof BoxFolder.Info) {
                BoxFolder folder = (BoxFolder)child.getResource();
                // there should be a better way to do this
                for (BoxItem.Info article : folder.getChildren())
                    articleCount++;
            }
            if (articleCount > 0)
                topicMap.put(topicName, new Topic(topicName, articleCount));
        }
        return new ArrayList<Topic>(topicMap.values());
    }

    @Override
    public List<Topic> getResearched() throws InterruptedException {
        if (topicMap.size() == 0)
            loadResearched();
        return new ArrayList<Topic>(topicMap.values());
    }

    @Override
    public Topic getTopic(String topic) throws InterruptedException {
        return topicMap.get(topic);
    }

    @Override
    public boolean save(Topic topic, Article article) throws InterruptedException {
        topicMap.put(topic.getTopic(), topic);
        BoxFolder destFolder = getTopicFolder(topic);
        try {
            String content = formatter.format(article);
            InputStream articleStream = new ByteArrayInputStream(content.getBytes());
            destFolder.uploadFile(articleStream, sanitizeFileName(article.getTitle()) +
                    formatter.getDefaultFileExtension());
        } catch (Exception ex) {
            outQueue.add(new OutMessage(OutMessage.Type.ERROR,
                    new ErrorMessage(topic, ErrorMessage.SEVERITY.ERROR, ex)));
        }
        return true;
    }

    @Override
    public boolean saveMultiple(Topic topic, List<Article> articles) throws InterruptedException {
        BoxFolder destFolder = getTopicFolder(topic);
        String content;
        InputStream articleStream;
        for (Article a : articles) {
            try {
                content = formatter.format(a);
                articleStream = new ByteArrayInputStream(content.getBytes());
                destFolder.uploadFile(articleStream, sanitizeFileName(a.getTitle()) +
                        formatter.getDefaultFileExtension());
            } catch (Exception ex) {
                outQueue.add(new OutMessage(OutMessage.Type.ERROR,
                        new ErrorMessage(topic, ErrorMessage.SEVERITY.ERROR, ex)));
            }
        }
        return true;
    }

    @Override
    public boolean delete(Topic topic) throws InterruptedException {
        BoxFolder destFolder = getTopicFolder(topic);
        destFolder.delete(true);
        return true;
    }

    private String sanitizeFolderName(String name) {
        name = name.replaceAll("[\\\\/]", "").trim();
        if (name.length() > 99)
            return name.substring(0, 96)+"...";
        else
            return name;
    }

    private String sanitizeFileName(String name) {
        return name.replaceAll("[\\\\/]", "").trim();
    }

    private BoxFolder getTopicFolder(Topic topic) {
        BoxFolder topicFolder = null;

        try {
            BoxFolder root = getExtempFolder();
            String folderName = sanitizeFolderName(topic.getTopic());

            for (BoxItem.Info child : root.getChildren()) {
                if (child.getName().equals(folderName) &&
                        child instanceof BoxFolder.Info) {
                    topicFolder = ((BoxFolder.Info) child).getResource();
                    break;
                }
            }

            if (topicFolder == null)
                topicFolder = root.createFolder(folderName).getResource();
        } catch (BoxAPIException ex) {
            String response = ex.getResponse();
            System.out.println("    "+response);
        }
        return topicFolder;
    }

    private BoxFolder getExtempFolder() {
        BoxFolder root = BoxFolder.getRootFolder(api);
        BoxFolder extemp = null;
        for (BoxItem.Info child : root.getChildren()) {
            if (child.getName().equals("Extemp") && child instanceof BoxFolder.Info) {
                extemp = (BoxFolder)child.getResource();
                break;
            }
        }
        if (extemp == null)
            extemp = root.createFolder("Extemp").getResource();
        return extemp;
    }
}
