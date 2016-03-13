package org.zalgosircular.extempfiller2.research.fetching.web;

import org.jsoup.HttpStatusException;
import org.zalgosircular.extempfiller2.messaging.OutMessage;
import org.zalgosircular.extempfiller2.research.Article;
import org.zalgosircular.extempfiller2.research.Topic;
import org.zalgosircular.extempfiller2.research.fetching.ArticleFetcher;
import org.zalgosircular.extempfiller2.research.fetching.web.html.HTMLFetcher;
import org.zalgosircular.extempfiller2.research.fetching.web.parsing.NoCruftArticleParser;
import org.zalgosircular.extempfiller2.research.fetching.web.urls.SEARCH_ENGINE;
import org.zalgosircular.extempfiller2.research.fetching.web.urls.SearchEngineFetcher;
import org.zalgosircular.extempfiller2.research.fetching.web.urls.URLFetcher;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Walt on 3/11/2016.
 */
public class NoCruftArticleFetcher extends ArticleFetcher {
    private final SEARCH_ENGINE engine;
    private final NoCruftArticleParser articleParser;

    public NoCruftArticleFetcher(BlockingQueue<OutMessage> outQueue, SEARCH_ENGINE engine) {
        super(outQueue);
        this.engine = engine;
        this.articleParser = new NoCruftArticleParser(outQueue);
    }

    @Override
    public boolean open() throws InterruptedException {
        return true;
    }

    @Override
    public List<Article> fetchArticles(Topic topic, int maxResults, List<String> excludes) throws InterruptedException {
        final List<Article> articles = new ArrayList<Article>(maxResults);
        final URLFetcher urlFetcher = new SearchEngineFetcher(outQueue, engine, topic, excludes);
        final HTMLFetcher htmlFetcher = new HTMLFetcher(outQueue);
        while (articles.size() < maxResults && urlFetcher.hasNext()) {
            final URI url = urlFetcher.getNext();
            try {
                final String response = htmlFetcher.getResponse(url, topic);
                if (response != null) {
                    final Article article = articleParser.parse(response);
                    if (article != null)
                        articles.add(article);

                }
            } catch (HttpStatusException e) {
                outQueue.add(new OutMessage(OutMessage.Type.DEBUG, "HTTP error "+e.getStatusCode()));
            }
        }
        return articles;
    }
}
