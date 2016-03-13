package org.zalgosircular.extempfiller2.research.fetching.web.parsing;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.zalgosircular.extempfiller2.messaging.OutMessage;
import org.zalgosircular.extempfiller2.research.Article;

import java.net.URL;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Walt on 3/10/2016.
 * A magic class that will take raw HTML and return a sane POJO with
 * content, title, and metadata.
 */
public class NoCruftArticleParser extends ArticleParser {

    public NoCruftArticleParser(BlockingQueue<OutMessage> outQueue) {
        super(outQueue);
    }

    @Override
    public Article parse(String contents) throws InterruptedException {
        Document doc = Jsoup.parse(contents);
        clean(doc);
        return new Article(null, doc.title(), null, null, doc.body().html());
    }

    // recursively eliminate cruft elements
    private void clean(Document doc) {
        // get rid of all scripts
        doc.select("script").remove();

        // select bottom-level, then work our way up
        Elements contents = doc.select("body *");
        Queue<Element> processQueue = new LinkedList<Element>();
        for (Element e : contents) {
            if (e.text().trim().length() == 0)
                e.remove();
            else if(e.isBlock() && isBottomLevelBlock(e))
                processQueue.add(e);
        }
        while (processQueue.size() > 0) {
            final Element current = processQueue.remove();
            if (current != null) {
                final Element parent = current.parent();
                if (!isContent(current) && parent != null) {
                    if (!processQueue.contains(parent))
                        processQueue.add(parent);
                    current.remove();
                }
            }
        }
    }

    private boolean isBottomLevelBlock(Element element) {
        float totalWords = element.text().split("[\\s]+").length;
        float ownWords = element.ownText().split("[\\s]+").length;
        return totalWords > 0 && ownWords/totalWords > 0.1;
    }

    private boolean isContent(Element element) {
        int totalWords = element.text().split("[\\s]+").length;
        return (getLinkDensity(element) < 0.15);
    }

    private float getLinkDensity(Element element) {
        int totalWords = element.text().split("\\s+").length;
        if (totalWords == 0)
            return 0;
        int linkWords = 0;
        for (Element e : element.select("a")) {
            //System.out.println(e.parent());
            linkWords += e.text().split("\\s+").length;
        }
        return ((float)linkWords)/((float)totalWords);
    }
}

