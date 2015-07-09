package org.zalgosircular.extempfiller2.research.parsing;

import com.sun.javaws.exceptions.InvalidArgumentException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.omg.CORBA.DynAnyPackage.Invalid;
import org.zalgosircular.extempfiller2.messaging.OutMessage;
import org.zalgosircular.extempfiller2.research.Article;
import org.zalgosircular.extempfiller2.research.fetching.ReadabilityHTMLFetcher;
import sun.java2d.pipe.SpanShapeRenderer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Queue;

/**
 * Created by Walt on 7/9/2015.
 */
public class ReadabilityArticleParser extends ArticleParser {

    private final SimpleDateFormat formatter;

    public ReadabilityArticleParser(Queue outQueue) {
        super(outQueue);
        formatter = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
    }
    public Article parse(String htmlContents) {
        // create a jsoup document
        Document doc = Jsoup.parse(htmlContents);
        String url = doc.select("url").text();
        //String content = ; // we'll do this later
        String author = doc.select("author").text();
        String rawDate = doc.select("date_published").text();
        Date published = null;
        try {
            published = formatter.parse(rawDate);
        } catch (ParseException e) {
            // send up a debug message
            this.outQueue.add(new OutMessage(OutMessage.Type.DEBUG, String.format(
                    "Could not parse date for page '%s'", url)));
        }
        String title = doc.select("title").text();
        String contents = doc.select("content").text(); // this also converts HTML entities
        Article article = new Article(url, title, author, published, contents);
        return article;
    }
}
