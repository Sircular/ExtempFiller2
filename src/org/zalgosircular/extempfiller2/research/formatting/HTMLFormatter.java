package org.zalgosircular.extempfiller2.research.formatting;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.zalgosircular.extempfiller2.research.Article;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by Walt on 7/9/2015.
 * Creates a simplified HTML document from an Article.
 */
public class HTMLFormatter implements ArticleFormatter {

    // these tags will be completely removed
    private final List<String> DISALLOWED_TAGS = Arrays.asList(("applet base basefont bgsound blink button dir embed fieldset form " +
            "frame frameset iframe ilayer input isindex label layer legend link marquee " +
            "menu meta noframes noscript object optgroup option param plaintext script " +
            "select style textarea xml").split(" +"));

    private final String[] DISALLOWED_ATTRS =
            ("id class onclic ondblclick accesskey data dynsrc tabindex").split(" +");

    // all tags that are not these will be
    // replaced with <div>
    private final List<String> ALLOWED_TAGS =
            Arrays.asList(("a abbr acronym address area b bdo big blockquote br caption center " +
                    "cite code col colgroup dd del dfn div dl dt em font h1 h2 h3 h4 h5 h6 hr i " +
                    "img ins kbd li map ol p pre q s samp small span strike strong sub sup table " +
                    "tbody td tfoot th thead title tr tt u ul var xmp").split(" +"));

    // generate a <div> containing information
    // about the article
    protected Element generateInfoDiv(Article article) {
        Element infoDiv = new Element(Tag.valueOf("div"), "");

        // add the information to the information paragraph
        infoDiv.append(String.format("Title: %s<br/>", article.getTitle()));
        String author = article.getAuthor();
        if (author != null && author.length() > 0 &&
                !author.equals("None")) {
            infoDiv.append(String.format("Author: %s<br/>", author));
        }
        Date date = article.getDatePublished();
        if (date != null) {
            // parse it into a standard date format
            // the M is capitalized because a lowercase m
            // means minutes
            SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy");
            infoDiv.append(String.format("Date published: %s<br/>", format.format(date)));
        }
        infoDiv.append(String.format("URL: <a href=\"%s\">%s</a>", article.getUrl(),
                article.getUrl()));
        return infoDiv;
    }

    // we want to create static stuff
    // we also want to keep it to only
    // a few standard tags
    // to generate something like "printer-friendly"
    // pages

    protected String normalizeContent(String content) {
        Document doc = Jsoup.parse(content);
        Element body = doc.body();
        // loop through all elements
        Elements all = body.select("*");
        for (Element el : all) {
            if (DISALLOWED_TAGS.contains(el.tagName())) {
                el.remove();
            } else {
                if (!ALLOWED_TAGS.contains(el.tagName())) {
                    el.tagName("div");
                }
                // remove disallowed ids
                for (String attr : DISALLOWED_ATTRS) {
                    el.removeAttr(attr);
                }
            }
        }
        return body.html();
    }

    @Override
    public String format(Article article) {
        Document doc = Jsoup.parse("");
        doc.title(article.getTitle());
        Element body = doc.body();

        Element infoDiv = generateInfoDiv(article);
        body.appendChild(infoDiv);
        Element contentDiv = new Element(Tag.valueOf("div"), "");
        contentDiv.append(normalizeContent(article.getHTML()));

        body.appendChild(contentDiv);
        return doc.toString();
    }

    public String getDefaultFileExtension() {
        return ".html";
    }
}
