package org.zalgosircular.extempfiller2;

import org.zalgosircular.extempfiller2.research.Article;
import org.zalgosircular.extempfiller2.research.Topic;
import org.zalgosircular.extempfiller2.research.fetching.HTMLFetcher;
import org.zalgosircular.extempfiller2.research.fetching.ReadabilityHTMLFetcher;
import org.zalgosircular.extempfiller2.research.parsing.ReadabilityArticleParser;

import java.net.URI;

/**
 * Created by Walt on 7/8/2015.
 */
public class ExtempFiller2 {

    public static void main(String[] args) {
        HTMLFetcher fetcher = new ReadabilityHTMLFetcher(null);
        try {
            Topic t = new Topic("The xx");
            URI wiki = new URI("https://en.wikipedia.org/wiki/The_xx");
            String contents = fetcher.getResponse(wiki, t);
            Article article = new ReadabilityArticleParser(null).parse(contents);
            System.out.println(article.getContents());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
