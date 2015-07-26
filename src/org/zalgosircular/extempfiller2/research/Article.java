package org.zalgosircular.extempfiller2.research;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;

import java.util.Date;

/**
 * Created by Walt on 7/8/2015.
 */
public class Article {
    private final String url;
    private final String title;
    private final String author;
    private final Date datePublished;
    private final String html;
    private final String plainText;

    public Article(String url, String title, String author, Date datePublished, String html) {
        this.url = url;
        this.title = title;
        this.author = author;
        this.datePublished = datePublished;
        this.html = html;
        this.plainText = htmlToText(html);
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public Date getDatePublished() {
        return datePublished;
    }

    public String getHTML() {
        return html;
    }

    private String htmlToText(String html) {
        //stupid hoops to go through to get new lines to work. assumes br2n is not in the text
        final String endl = System.getProperty("line.separator");
        String cleanHtml = Jsoup.clean(html, "", Whitelist.none().addTags("br"));
        cleanHtml = cleanHtml.replaceAll("\\s*(?i)<br*>\\s*", "br2n");

        final Document clean = Jsoup.parse(cleanHtml);
        String plainText = clean.text();
        plainText = plainText.replaceAll("br2n", endl);
        return plainText;
    }

    public String getPlainText() {
        return plainText;
    }
}
