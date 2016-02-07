package org.zalgosircular.extempfiller2.research.fetching.web;

import org.zalgosircular.extempfiller2.messaging.ErrorMessage;
import org.zalgosircular.extempfiller2.messaging.OutMessage;
import org.zalgosircular.extempfiller2.research.Article;
import org.zalgosircular.extempfiller2.research.Topic;
import org.zalgosircular.extempfiller2.research.fetching.ArticleFetcher;
import org.zalgosircular.extempfiller2.research.fetching.web.html.ReadabilityHTMLFetcher;
import org.zalgosircular.extempfiller2.research.fetching.web.parsing.ReadabilityArticleParser;
import org.zalgosircular.extempfiller2.research.fetching.web.urls.SEARCH_ENGINE;
import org.zalgosircular.extempfiller2.research.fetching.web.urls.SearchEngineFetcher;
import org.zalgosircular.extempfiller2.research.fetching.web.urls.URLFetcher;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Logan Lembke on 7/12/2015.
 */
public class ReadabilityArticleFetcher extends ArticleFetcher {
    private final SEARCH_ENGINE searchEngine;
    private final ReadabilityHTMLFetcher readabilityFetcher;
    private final ReadabilityArticleParser readabilityArticleParser;

    public ReadabilityArticleFetcher(BlockingQueue<OutMessage> outQueue, SEARCH_ENGINE searchEngine) {
        super(outQueue);
        this.searchEngine = searchEngine;
        this.readabilityFetcher = new ReadabilityHTMLFetcher(outQueue);
        this.readabilityArticleParser = new ReadabilityArticleParser(outQueue);
    }

    public boolean open() throws InterruptedException {
        readabilityFetcher.auth();
        return true;
    }

    public List<Article> fetchArticles(Topic topic, int maxResults, List<String> excludes) throws InterruptedException {
        final List<Article> articles = new ArrayList<Article>(maxResults);
        final URLFetcher urlFetcher = new SearchEngineFetcher(outQueue, searchEngine, topic, excludes);
        int articlesFound = 0;
        while (articlesFound < maxResults && urlFetcher.hasNext()) {
            final URI url = urlFetcher.getNext();
            final String response = readabilityFetcher.getResponse(url, topic);
            if (response != null) {
                final Article article = readabilityArticleParser.parse(response);
                if (article != null) {
                    articles.add(article);
                    articlesFound++;
                }
            }
        }
        if (articles.size() == 0)
            outQueue.put(
                    new OutMessage(
                            OutMessage.Type.ERROR,
                            new ErrorMessage(
                                    topic,
                                    ErrorMessage.SEVERITY.CRITICAL,
                                    new RuntimeException("No articles found")
                            )
                    )
            );
        return articles;
    }
}
