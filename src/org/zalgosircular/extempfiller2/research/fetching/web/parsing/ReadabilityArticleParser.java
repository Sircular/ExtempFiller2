package org.zalgosircular.extempfiller2.research.fetching.web.parsing;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.zalgosircular.extempfiller2.messaging.OutMessage;
import org.zalgosircular.extempfiller2.research.Article;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Walt on 7/9/2015.
 */
public class ReadabilityArticleParser extends ArticleParser {

    private final SimpleDateFormat formatter;

    public ReadabilityArticleParser(BlockingQueue<OutMessage> outQueue) {
        super(outQueue);
        formatter = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
    }

    public Article parse(String readabilityHTML) throws InterruptedException {
        // create a jsoup document
        final Document doc = Jsoup.parse(readabilityHTML);
        final String url = doc.select("url").text();
        //String content = ; // we'll do this later
        final String author = doc.select("author").text();
        final String rawDate = doc.select("date_published").text();
        Date published = null;
        try {
            published = formatter.parse(rawDate);
        } catch (ParseException e) {
            // send up a debug message
            // it's not critical
            this.outQueue.put(new OutMessage(OutMessage.Type.DEBUG, String.format(
                    "Could not parse date for page '%s'", url)));
        }
        final String title = doc.select("title").text();
        final String html = doc.select("content").text(); // this also converts HTML entities

        return new Article(url, title, author, published, html);
    }
}
