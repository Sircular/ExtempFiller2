package org.zalgosircular.extempfiller2.research.formatting;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.zalgosircular.extempfiller2.research.Article;

/**
 * Created by Logan Lembke on 7/8/2015.
 */
public class TextFormatter implements ArticleFormatter {
    @Override
    public String format(Article article) {
        final StringBuilder sb = new StringBuilder();
        final String endl = System.getProperty("line.separator");
        sb.append(article.getTitle());
        sb.append(endl);
        sb.append(article.getAuthor());
        sb.append(endl);
        sb.append(article.getDatePublished());
        sb.append(endl);
        Document doc = Jsoup.parse(article.getHTML());
        String text = doc.body().text();
        sb.append(text);
        return sb.toString();
    }

    @Override
    public String getDefaultFileExtension() {
        return ".txt";
    }
}
