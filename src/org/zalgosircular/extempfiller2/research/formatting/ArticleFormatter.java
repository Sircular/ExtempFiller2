package org.zalgosircular.extempfiller2.research.formatting;

import org.zalgosircular.extempfiller2.research.Article;

/**
 * Created by Logan Lembke on 7/8/2015.
 */
public interface ArticleFormatter {
    public String format(Article article);
    public String getDefaultFileExtension();
}
