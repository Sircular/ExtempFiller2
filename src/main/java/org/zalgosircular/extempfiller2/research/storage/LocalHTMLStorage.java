package org.zalgosircular.extempfiller2.research.storage;

import com.sun.xml.internal.bind.api.impl.NameConverter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.zalgosircular.extempfiller2.messaging.ErrorMessage;
import org.zalgosircular.extempfiller2.messaging.OutMessage;
import org.zalgosircular.extempfiller2.research.Article;
import org.zalgosircular.extempfiller2.research.Topic;
import org.zalgosircular.extempfiller2.research.formatting.ArticleFormatter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.util.*;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Walt on 2/25/2016.
 */
public class LocalHTMLStorage extends StorageFacility {
    private final static Path HOME = Paths.get("./extemp");
    private final static Map<String, Topic> topics = new HashMap<String, Topic>();

    public LocalHTMLStorage(BlockingQueue<OutMessage> outQueue, ArticleFormatter formatter) {
        super(outQueue, formatter);
    }

    @Override
    public boolean open() throws InterruptedException {
        try {
            if (!Files.exists(HOME)) {
                Files.createDirectories(HOME);
            }
            return true;
        } catch (IOException e) {
            outQueue.add(new OutMessage(OutMessage.Type.ERROR, new ErrorMessage(null, ErrorMessage.SEVERITY.CRITICAL, e)));
        }
        return false;
    }

    private List<Topic> loadTopicsFromIndex() throws IOException {
        final Path indPath = getGlobalIndexPath();
        final List<Topic> indexTopics = new LinkedList<Topic>();
        if (Files.exists(indPath)) {
            Document doc = Jsoup.parse(indPath.toFile(), "UTF-8");
            final Elements articleLinks = doc.select("ul#topics li a");
            for (Element e : articleLinks) {
                indexTopics.add(new Topic(e.text()));
            }
        }
        return indexTopics;
    }

    @Override
    public boolean close() throws InterruptedException {
        // just for safety
        rebuildGlobalIndex();
        return true;
    }

    @Override
    public boolean exists(String topic) throws InterruptedException {
        return getTopic(topic) != null;
    }

    @Override
    public List<Topic> loadResearched() throws InterruptedException {
        try {
            List<Topic> loadedTopics = loadTopicsFromIndex();
            boolean indexInvalid = false; // in case some folders were deleted
            // populate the hash map for fast searching
            final Iterator<Topic> topicIt = loadedTopics.iterator();
            while (topicIt.hasNext()) {
                final Topic t = topicIt.next();
                // populate article counts
                final Path topicDir = getSafeFolderPath(t);
                if (Files.exists(topicDir)) {
                    DirectoryStream<Path> dirStream = Files.newDirectoryStream(topicDir);
                    int count = -1; // discount index
                    for (Path p : dirStream)
                        count++;
                    t.setArticleCount(count);
                    dirStream.close();
                    topics.put(t.getTopic(), t);
                } else {
                    topicIt.remove();
                    indexInvalid = true;
                }
            }
            if (indexInvalid)
                rebuildGlobalIndex();
            return loadedTopics;
        } catch (IOException e) {
            outQueue.add(new OutMessage(OutMessage.Type.ERROR, new ErrorMessage(null, ErrorMessage.SEVERITY.CRITICAL, e)));
        }
        return null;
    }

    @Override
    public List<Topic> getResearched() throws InterruptedException {
        if (topics == null)
            loadResearched();
        return new LinkedList<Topic>(topics.values());
    }

    @Override
    public Topic getTopic(String topic) throws InterruptedException {
        if (topics == null)
            loadResearched();
        if (topics.containsKey(topic))
            return topics.get(topic);
        return null;
    }

    @Override
    public boolean save(Topic topic, Article article) throws InterruptedException {
        if (!topics.containsKey(topic.getTopic())) {
            topics.put(topic.getTopic(), topic);
            rebuildGlobalIndex();
        }
        if (saveArticle(topic, article)) {
            rebuildTopicIndex(topic, Collections.singletonList(article));
            return true;
        }
        return false;
    }

    @Override
    public boolean saveMultiple(Topic topic, List<Article> articles) throws InterruptedException {
        if (!topics.containsKey(topic.getTopic())) {
            topics.put(topic.getTopic(), topic);
            rebuildGlobalIndex();
        }
        for (Article a : articles) {
            saveArticle(topic, a);
        }
        rebuildTopicIndex(topic, articles);
        return true;
    }

    // used to prevent code duplication in save and saveMultiple
    private boolean saveArticle(Topic topic, Article article) {
        Path articlePath = getSafeArticlePath(topic, article);
        return saveData(formatter.format(article), articlePath);
    }

    @Override
    public boolean delete(Topic topic) throws InterruptedException {
        Path dir = getSafeFolderPath(topic);
        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }
                public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
            topics.remove(topic.getTopic());
            rebuildGlobalIndex();
            return true;
        } catch (IOException e) {
            outQueue.add(new OutMessage(OutMessage.Type.ERROR, new ErrorMessage(topic, ErrorMessage.SEVERITY.ERROR, e)));
        }
        return false;
    }

    // this needs to be built incrementally
    private boolean rebuildTopicIndex(Topic topic, List<Article> newArticles) {
        Path indexPath = getSafeTopicIndexPath(topic);
        try {
            Document index = null;
            if (Files.exists(indexPath)) {
                index = Jsoup.parse(indexPath.toFile(), "UTF-8");
            } else {
                index = Jsoup.parse("<html><head><title>"+topic.getTopic()+"</title></head>"+
                                    "<body><h1>"+topic.getTopic()+"</h1><ul id='articles'></ul>"+
                                    "</body></html>");
            }
            Element ul = index.select("ul#articles").first();
            // prevent duplicates
            List<String> prevArticles = new LinkedList<String>();
            for (Element li : ul.select("li")) {
                prevArticles.add(li.text().trim());
            }
            for (Article a : newArticles) {
                if (!prevArticles.contains(a.getTitle())) {
                    String article = a.getTitle();
                    String articlePath = getSafeFolderPath(topic).relativize(getSafeArticlePath(topic, a)).toString();
                    ul.append("<li><a href='"+articlePath+"'>"+article+"</a></li>");
                }
            }
            return saveData(index.toString(), indexPath);
        } catch (IOException e) {
            outQueue.add(new OutMessage(OutMessage.Type.ERROR, new ErrorMessage(topic, ErrorMessage.SEVERITY.ERROR, e)));
        }
        return false;
    }

    private boolean rebuildGlobalIndex() {
        Path indPath = getGlobalIndexPath();
        Document newDoc = Jsoup.parse("<html></html>");
        newDoc.head().append("<title>Index</title>");
        newDoc.body().append("<h1>Index</h1>");
        Element ul = newDoc.body().appendElement("ul").attr("id", "topics");
        for (Topic t : topics.values()) {
            final String topic = t.getTopic();
            final String topicIndex = HOME.relativize(getSafeTopicIndexPath(t)).toString();
            ul.append("<li><a href='"+topicIndex+"'>"+topic+"</a></li>");
        }
        return saveData(newDoc.toString(), indPath);
    }

    private Path getSafeFolderPath(Topic t) {
        return HOME.resolve(t.getTopic().replaceAll("[\\\\/?<>\"'|:*]", "").trim()
                .replace(" ", "_"));
    }

    private Path getSafeTopicIndexPath(Topic t) {
        return getSafeFolderPath(t).resolve("index.html");
    }

    private Path getSafeArticlePath(Topic t, Article a) {
        return getSafeFolderPath(t).resolve(a.getTitle().replaceAll("[\\\\/?<>\"'|:*]", "").trim()
                .replace(" ", "_")+formatter.getDefaultFileExtension());
    }

    private Path getGlobalIndexPath() {
        return HOME.resolve("index.html");
    }

    private boolean saveData(String data, Path path) {
        try {
            if (!Files.exists(path.getParent()))
                Files.createDirectories(path.getParent());
            BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
            writer.write(data);
            writer.close();
            return true;
        } catch (IOException e) {
            outQueue.add(new OutMessage(OutMessage.Type.ERROR, new ErrorMessage(null, ErrorMessage.SEVERITY.ERROR, e)));
        }
        return false;
    }

}
