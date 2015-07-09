package org.zalgosircular.extempfiller2.research.storage;

import org.zalgosircular.extempfiller2.research.Article;
import org.zalgosircular.extempfiller2.research.Topic;
import org.zalgosircular.extempfiller2.research.formatting.TextFormatter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * Created by Logan Lembke on 7/8/2015.
 */
public class LocalTextStorage extends StorageFacility{
    private final static String DIR = "topics";
    private final static String TOPICS_FILE = DIR + File.separator + "topicsList.txt";
    private final static String SEP = "||";
    private final LinkedList<Topic> topics;
    private final HashMap<Topic, String> shortened;
    private final TextFormatter formatter;
    private boolean loaded = false;
    public LocalTextStorage(Queue outQueue) {
        super(outQueue);
        this.topics = new LinkedList<Topic>();
        this.shortened = new HashMap<Topic, String>();
        this.formatter = new TextFormatter();
    }

    @Override
    public boolean open() {
        if(!Files.exists(Paths.get(DIR))) {
            try {
                Files.createDirectory(Paths.get(DIR));
            } catch (IOException e) {
                return false;
            }
        }
        if(!Files.exists(Paths.get(TOPICS_FILE))) {
            try {
                Files.createFile(Paths.get(TOPICS_FILE));
            }
            catch (IOException e) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean close() {
        final StringBuilder sb = new StringBuilder();
        for (Topic t : topics)
        {
            sb.append(t.getTopic());
            sb.append(SEP);
            sb.append(shortened.get(t));
            sb.append(SEP);
            sb.append(t.getArticleCount());
            sb.append(System.getProperty("line.separator"));
        }
        final String cache = sb.toString();
        try {
            Files.write(Paths.get(TOPICS_FILE), cache.getBytes("utf-8"),
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean exists(final String topic) {
        if (!loaded) {
        //implementation 1
            final String safeName = StringSafety.charNumUnderscore(topic);
            return Files.exists(Paths.get(DIR + File.separator + safeName));
        }
        //implementation 2
        for (Topic t : topics) {
            if (t.getTopic().equals(topic))
                return true;
        }
        return false;
    }

    @Override
    public List<Topic> load() {
        try {
            Scanner sc = new Scanner(Paths.get(TOPICS_FILE));
            String line = "";
            while((line = sc.nextLine()) != null) {
                final String[] tokens = line.trim().split(SEP);
                final String longName = tokens[0];
                final String folderName = tokens[1];
                final int articleCount = Integer.parseInt(tokens[2]);
                final Topic t = new Topic(longName);
                t.setArticleCount(articleCount);
                topics.add(t);
                shortened.put(t, folderName);
            }
        }
        catch (IOException e) {
            return null;
        }
        loaded = true;
        return topics;
    }

    @Override
    public boolean save(Topic topic, Article article) {
        //Save article
        //keep cache current
        String safeFolderName = "";
        //New topic
        if (!topics.contains(topic)) {
            safeFolderName = StringSafety.charNumUnderscore(topic.getTopic());
            topics.add(topic);
            shortened.put(topic, safeFolderName);
        }
        //Just more articles
        else {
            safeFolderName = shortened.get(topic);
        }
        final String text = formatter.format(article);

        final String articleDir = DIR + File.separator + safeFolderName;
        final String safeFileName = StringSafety.charNumUnderscore(article.getTitle()) + ".txt";
        final String fileName = articleDir + File.separator + safeFileName;
        try {
            Files.write(Paths.get(fileName), text.getBytes("utf-8"),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        }
        catch (IOException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean saveMultiple(Topic topic, List<Article> articles) {
        boolean result = true;
        for (Article a : articles)
        {
            result = save(topic, a);
            if (!result)
                return false;
        }
        return true;
    }

    @Override
    public boolean delete(Topic topic) {
        final String folderName = DIR + File.separator + shortened.get(topic);
        topics.remove(topic);
        shortened.remove(topic);
        try {
            Files.delete(Paths.get(folderName));
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
