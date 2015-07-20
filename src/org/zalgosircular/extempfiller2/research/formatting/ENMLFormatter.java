package org.zalgosircular.extempfiller2.research.formatting;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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

        // we have to make this document because that's
        // the only way to get the body element to output
        // xhtml.
        final Document doc = Jsoup.parse("");
        final Element body = doc.body();
        doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
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
        String bodyOutput = "<en-note>"+body.html()+"</en-note>";
        return header + bodyOutput;
    }

    @Override
    public String getDefaultFileExtension() {
        return ""; // this is evernote
    }
}
