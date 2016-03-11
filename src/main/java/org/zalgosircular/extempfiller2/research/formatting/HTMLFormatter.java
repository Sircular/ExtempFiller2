package org.zalgosircular.extempfiller2.research.formatting;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.zalgosircular.extempfiller2.research.Article;

import java.text.Normalizer;
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
    protected static final List<String> DISALLOWED_TAGS = Arrays.asList(("applet base basefont bgsound blink button dir embed fieldset form " +
            "frame frameset iframe ilayer input isindex label layer legend link marquee " +
            "menu meta noframes noscript object optgroup option param plaintext script " +
            "select style textarea xml").split(" +"));

    protected static final List<String> ALLOED_IMG_ATTRS = Arrays.asList(("src width height").split(" +"));

    protected static final List<String> ALLOWED_ATTRS =
            Arrays.asList(("href style rel").split(" +"));

    // all tags that are not these will be
    // replaced with <div>
    protected static final List<String> ALLOWED_TAGS =
            Arrays.asList(("a abbr acronym address area b bdo big blockquote br caption center " +
                    "cite code col colgroup dd del dfn div dl dt em font h1 h2 h3 h4 h5 h6 hr i " +
                    "img ins kbd li map ol p pre q s samp small span strike strong sub sup table " +
                    "tbody td tfoot th thead title tr tt u ul var xmp").split(" +"));

    // parse it into a standard date format
    // the M is capitalized because a lowercase m
    // means minutes
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM-dd-yyyy");

    // generate a <div> containing information
    // about the article
    protected Element generateInfoDiv(Article article) {
        final Element infoDiv = new Element(Tag.valueOf("div"), "");

        // add the information to the information paragraph
        infoDiv.append(String.format("Title: %s<br/>", article.getTitle()));
        final String author = article.getAuthor();
        if (author != null && author.length() > 0 &&
                !author.equals("None")) {
            infoDiv.append(String.format("Author: %s<br/>", author));
        }
        final Date date = article.getDatePublished();
        if (date != null) {
            infoDiv.append(String.format("Date published: %s<br/>", DATE_FORMAT.format(date)));
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
    protected String normalizeContent(String rawContent) {
        // normalize from UTF-8 to ascii
        final String asciiContent = Normalizer.normalize(rawContent, Normalizer.Form.NFD)
                .replaceAll("\\p{InCOMBINING_DIACRITICAL_MARKS}+", "");
        final Document doc = Jsoup.parse(asciiContent);
        // Evernote is strict, and this is just the
        // HTML equivalent
        doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);

        final Element body = doc.body();
        // loop through all elements
        final Elements all = body.getAllElements();
        for (Element el : all) {
            if (DISALLOWED_TAGS.contains(el.tagName())) {
                el.remove();
            } else {
                if (!ALLOWED_TAGS.contains(el.tagName())) {
                    if (!el.tag().isSelfClosing()) {
                        el.tagName("div");
                    } else {
                        el.remove();
                    }
                }
                if (el.tagName().equals("img")) {
                    for (Attribute attr : el.attributes()) {
                        if (!ALLOED_IMG_ATTRS.contains(attr.getKey().toLowerCase())) {
                            el.removeAttr(attr.getKey());
                        }
                    }
                } else {
                    // remove disallowed attributes
                    for (Attribute attr : el.attributes()) {
                        if (!ALLOWED_ATTRS.contains(attr.getKey().toLowerCase()) || attr.getValue().equals("")) {
                            el.removeAttr(attr.getKey());
                        }
                    }
                }
            }
        }
        return body.html();
    }

    public String format(Article article) {
        final Document doc = Jsoup.parse("");
        doc.title(article.getTitle());
        doc.head().appendElement("meta").attr("charset", "UTF-8");
        final Element body = doc.body();

        final Element infoDiv = generateInfoDiv(article);
        body.appendChild(infoDiv);

        final Element contentDiv = new Element(Tag.valueOf("div"), "");
        contentDiv.append(normalizeContent(article.getHTML()));

        body.appendChild(contentDiv);
        return doc.toString();
    }

    public String getDefaultFileExtension() {
        return ".html";
    }
}
