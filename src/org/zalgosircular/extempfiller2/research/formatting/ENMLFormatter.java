package org.zalgosircular.extempfiller2.research.formatting;

import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.zalgosircular.extempfiller2.research.Article;

/**
 * Created by Walt on 7/11/2015.
 */
public class ENMLFormatter extends HTMLFormatter implements ArticleFormatter {
    @Override
    public String format(Article article) {
        // we'll just reuse the methods from the HTMLFormatter
        Element body = new Element(Tag.valueOf("en-note"), "");
        Element infoDiv = generateInfoDiv(article);
        Element contentDiv = new Element(Tag.valueOf("div"), "");
        contentDiv.append(normalizeContent(article.getHTML()));
        body.appendChild(infoDiv);
        body.appendChild(contentDiv);

        return body.toString();
    }

    @Override
    public String getDefaultFileExtension() {
        return ""; // this is evernote
    }
}
