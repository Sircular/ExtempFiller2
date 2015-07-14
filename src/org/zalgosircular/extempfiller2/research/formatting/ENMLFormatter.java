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
        final Element body = new Element(Tag.valueOf("en-note"), "");
        final Element infoDiv = generateInfoDiv(article);
        final Element contentDiv = new Element(Tag.valueOf("div"), "");
        contentDiv.append(normalizeContent(article.getHTML()));
        body.appendChild(infoDiv);
        body.appendChild(contentDiv);
        final String header =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                        System.getProperty("line.separator") +
                        "<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">" +
                        System.getProperty("line.separator");
        String bodyOutput = body.toString();
        // we have to do this for evernote; wtf
        return header + bodyOutput.replaceAll("<br( /)?>", "<br></br>");
    }

    @Override
    public String getDefaultFileExtension() {
        return ""; // this is evernote
    }
}
