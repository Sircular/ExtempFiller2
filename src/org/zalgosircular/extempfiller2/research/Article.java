package org.zalgosircular.extempfiller2.research;

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

    public Article(String url, String title, String author, Date datePublished, String html) {
        this.url = url;
        this.title = title;
        this.author = author;
        this.datePublished = datePublished;
        this.html = html;
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
}
