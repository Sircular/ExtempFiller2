package org.zalgosircular.extempfiller2.research.parsing;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.zalgosircular.extempfiller2.messaging.OutMessage;
import org.zalgosircular.extempfiller2.research.Article;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Queue;

/**
 * Created by Walt on 7/9/2015.
 */
public class ReadabilityArticleParser extends ArticleParser {

    private final SimpleDateFormat formatter;

    public ReadabilityArticleParser(Queue<OutMessage> outQueue) {
        super(outQueue);
        formatter = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
    }

    public Article parse(String readabilityHTML) {
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
            this.outQueue.add(new OutMessage(OutMessage.Type.DEBUG, String.format(
                    "Could not parse date for page '%s'", url)));
        }
        final String title = doc.select("title").text();
        final String contents = doc.select("content").text(); // this also converts HTML entities
        // convert HTML to readable plaintext (some estimation techniques used)
        final Document contentDoc = Jsoup.parse(contents);
        // select all <p> tags and select <div> tags that are direct
        // children of a <div> that has no sibling <divs>.
        // for further explanation, see https://css-tricks.com/child-and-sibling-selectors/
        final Elements paraElems = contentDoc.select("p, div:only-child > div");

        final StringBuilder textBuilder = new StringBuilder();
        final String endl = System.getProperty("line.separator");

        for (Element e : paraElems) {
            // filter out the divs with children
            if (!(e.nodeName().equals("div") && e.select("p, div").size() > 0))
                textBuilder.append(e.text()).append(endl);
        }


        return new Article(url, title, author, published, textBuilder.toString());
    }
}
