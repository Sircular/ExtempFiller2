package org.zalgosircular.extempfiller2.research.parsing;

import com.sun.javaws.exceptions.InvalidArgumentException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.omg.CORBA.DynAnyPackage.Invalid;
import org.zalgosircular.extempfiller2.messaging.OutMessage;
import org.zalgosircular.extempfiller2.research.Article;
import org.zalgosircular.extempfiller2.research.fetching.ReadabilityHTMLFetcher;
import sun.java2d.pipe.SpanShapeRenderer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
            // it's not critical
            this.outQueue.add(new OutMessage(OutMessage.Type.DEBUG, String.format(
                    "Could not parse date for page '%s'", url)));
        }
        String title = doc.select("title").text();
        String contents = doc.select("content").text(); // this also converts HTML entities
        // convert HTML to readable plaintext (some estimation techniques used)
        Document contentDoc = Jsoup.parse(contents);
        Elements paraElems = contentDoc.select("p, div:only-child > div");

        StringBuilder textBuilder = new StringBuilder();
        final String endl = System.getProperty("line.separator");

        for (Element e : paraElems) {
            // filter out the divs with children
            if (!(e.nodeName() == "div" && e.select("p, div").size() > 0))
                textBuilder.append(e.text()).append(endl).append(endl);
        }


        return new Article(url, title, author, published, textBuilder.toString());
    }
}
