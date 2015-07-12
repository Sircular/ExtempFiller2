package org.zalgosircular.extempfiller2.research.formatting;

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
        if (article.getAuthor() != null && article.getAuthor().length() > 0 &&
                !article.getAuthor().equals("None")) {
            sb.append(article.getAuthor());
            sb.append(endl);
        }
        if (article.getDatePublished() != null) {
            sb.append(article.getDatePublished());
            sb.append(endl);
        }
        sb.append(endl);
        sb.append(article.getPlainText());
        return sb.toString();
    }

    @Override
    public String getDefaultFileExtension() {
        return ".txt";
    }
}
