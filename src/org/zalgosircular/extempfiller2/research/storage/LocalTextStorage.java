package org.zalgosircular.extempfiller2.research.storage;

import org.zalgosircular.extempfiller2.messaging.ErrorMessage;
import org.zalgosircular.extempfiller2.messaging.OutMessage;
import org.zalgosircular.extempfiller2.research.Article;
import org.zalgosircular.extempfiller2.research.Topic;
import org.zalgosircular.extempfiller2.research.formatting.ArticleFormatter;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Pattern;

/**
 * Created by Logan Lembke on 7/8/2015.
 */
public class LocalTextStorage extends StorageFacility {
    private final static String DIR = "topics";
    private final static String TOPICS_FILE = DIR + File.separator + "topicsList.txt";
    private final static String SEP = "||";
    private final LinkedList<Topic> topics;
    private final HashMap<Topic, String> shortened;
    private boolean loaded = false;

    public LocalTextStorage(BlockingQueue<OutMessage> outQueue, ArticleFormatter formatter) {
        super(outQueue, formatter);
        this.topics = new LinkedList<Topic>();
        this.shortened = new HashMap<Topic, String>();
    }

    @Override
    public boolean open() throws InterruptedException {
        try {
            if (!Files.exists(Paths.get(DIR))) {
                Files.createDirectory(Paths.get(DIR));
            }
            if (!Files.exists(Paths.get(TOPICS_FILE))) {
                Files.createFile(Paths.get(TOPICS_FILE));
            }
        } catch (IOException e) {
            outQueue.put(new OutMessage(OutMessage.Type.ERROR, new ErrorMessage(null, e)));
            return false;
        }
        return true;
    }

    @Override
    public boolean close() throws InterruptedException {
        try {
            saveCache();
        } catch (IOException e) {
            outQueue.add(new OutMessage(OutMessage.Type.ERROR, new ErrorMessage(null, e)));
        }
        return true;
    }

    @Override
    public boolean exists(final String topic) {
        if (!loaded) {
            //implementation 1
            final String safeName = StringSafety.charNumScore255(topic);
            return Files.exists(Paths.get(DIR + File.separator + safeName));
        }
        //implementation 2
        for (Topic t : topics) {
            if (t.getTopic().equals(topic))
                return true;
        }
        return false;
    }

    //loads cache
    @Override
    public List<Topic> loadResearched() throws InterruptedException {
        try {
            Scanner sc = new Scanner(Paths.get(TOPICS_FILE));
            String line;
            topics.clear();
            shortened.clear();
            while (sc.hasNextLine()) {
                line = sc.nextLine();
                final String[] tokens = line.trim().split(Pattern.quote(SEP));
                final String longName = tokens[0];
                final String folderName = tokens[1];
                final int articleCount = Integer.parseInt(tokens[2]);
                final Topic t = new Topic(longName);

                t.setArticleCount(articleCount);
                topics.add(t);
                shortened.put(t, folderName);
            }
        } catch (IOException e) {
            outQueue.put(new OutMessage(OutMessage.Type.ERROR, new ErrorMessage(null, e)));
            return null;
        }
        loaded = true;
        return topics;
    }

    @Override
    public List<Topic> getResearched() throws InterruptedException {
        if (topics.size() == 0)
            loadResearched();
        return topics; // maybe this should be cloned?
    }

    @Override
    public Topic getTopic(String s) throws InterruptedException {
        if (topics.size() == 0)
            loadResearched();
        for (Topic t : topics) {
            if (t.getTopic().equals(s))
                return t;
        }
        return null;
    }

    @Override
    public boolean save(Topic topic, Article article) throws InterruptedException {
        try {
            String safeFolderName;
            //New topic
            if (!topics.contains(topic)) {
                //create dir
                safeFolderName = StringSafety.charNumScore255(topic.getTopic());
                Files.createDirectory(Paths.get(DIR + File.separator + safeFolderName));

                //update cache
                topics.add(topic);
                shortened.put(topic, safeFolderName);
            } else {
                safeFolderName = shortened.get(topic);
            }
            //article data
            final String text = formatter.format(article);

            final String articleDir = DIR + File.separator + safeFolderName;

            //article file
            final String safeFileName = StringSafety.charNumScore255(article.getTitle()) +
                    formatter.getDefaultFileExtension();
            //full path
            final String fileName = articleDir + File.separator + safeFileName;
            Files.write(Paths.get(fileName), text.getBytes("utf-8"),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            outQueue.put(new OutMessage(OutMessage.Type.ERROR, new ErrorMessage(topic, e)));
            return false;
        }
        //update topic
        topic.setArticleCount(topic.getArticleCount() + 1);
        try {
            // this might seem a little expensive to be doing every
            // time that an article is saved, but it's the only way
            // to make sure that the cache is up to date if we choose
            // to reload
            saveCache();
        } catch (IOException e) {
            // saving the article worked properly, but saving the cache didn't
            // we can still return true, but also pass up the error
            outQueue.put(new OutMessage(OutMessage.Type.ERROR, new ErrorMessage(topic, e)));
        }
        return true;
    }

    @Override
    public boolean saveMultiple(Topic topic, List<Article> articles) throws InterruptedException {
        boolean result;
        for (Article a : articles) {
            result = save(topic, a);
            if (!result)
                return false;
        }
        return true;
    }

    @Override
    public boolean delete(Topic topic) throws InterruptedException {
        final String folderName = DIR + File.separator + shortened.get(topic);
        //update cache
        topics.remove(topic);
        shortened.remove(topic);
        //update filesystem
        try {
            DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(folderName));
            for (Path p : stream) {
                Files.delete(p);
            }
            Files.delete(Paths.get(folderName));
        } catch (IOException e) {
            outQueue.put(new OutMessage(OutMessage.Type.ERROR, new ErrorMessage(topic, e)));
            return false;
        }
        return true;
    }

    private void saveCache() throws IOException {
        final StringBuilder sb = new StringBuilder();
        final String endl = System.getProperty("line.separator");
        for (Topic t : topics) {
            sb.append(t.getTopic());
            sb.append(SEP);
            sb.append(shortened.get(t));
            sb.append(SEP);
            sb.append(t.getArticleCount());
            sb.append(endl);
        }
        final String cache = sb.toString();
        try {
            Files.write(Paths.get(TOPICS_FILE), cache.getBytes("utf-8"),
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (UnsupportedEncodingException e) {
            // will not happen
        }
    }
}
