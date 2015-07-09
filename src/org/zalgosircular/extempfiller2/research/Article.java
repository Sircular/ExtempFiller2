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
    private final String contents;

    public Article(String url, String title, String author, Date datePublished, String contents) {
        this.url = url;
        this.title = title;
        this.author = author;
        this.datePublished = datePublished;
        this.contents = contents;
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

    public String getContents() {
        return contents;
    }
}
