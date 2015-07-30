package org.zalgosircular.extempfiller2.research.fetching.web.urls;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.zalgosircular.extempfiller2.messaging.ErrorMessage;
import org.zalgosircular.extempfiller2.messaging.OutMessage;
import org.zalgosircular.extempfiller2.research.Topic;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Logan Lembke on 7/12/2015.
 */
public class SearchEngineFetcher extends URLFetcher {
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.132 Safari/537.36";
    private static final int TIMEOUT = 10 * 1000;
    private final SEARCH_ENGINE searchEngine;
    private final Queue<URI> urls;
    private int results;
    private boolean done;
    private boolean opened;

    public SearchEngineFetcher(BlockingQueue<OutMessage> outQueue, SEARCH_ENGINE searchEngine, Topic topic, List<String> excludes) {
        super(outQueue, topic, excludes);
        this.searchEngine = searchEngine;
        this.urls = new LinkedList<URI>();
        this.results = 0;
        this.done = false;
        this.opened = false;
    }

    @Override
    public URI getNext() throws InterruptedException {
        if (urls.size() == 0)
            fetchMore();
        return urls.poll();
    }

    @Override
    public boolean hasNext() throws InterruptedException {
        if (!opened) {
            fetchMore();
        }
        return !done || urls.size() != 0;
    }

    public void fetchMore() throws InterruptedException {
        try {
            final String queryURL = String.format(searchEngine.QUERY_STRING,
                    URLEncoder.encode(topic.getTopic(), "UTF-8"), results);
            final Document contentsDoc = Jsoup.connect(queryURL).userAgent(USER_AGENT).timeout(TIMEOUT).get();

            final Elements searchResults = contentsDoc.select(searchEngine.RESULT_SELECTOR);
            // get their target urls
            String urlTarget;
            outer:
            for (Element el : searchResults) {
                // JSOUP HANDLES REDIRECTS
                urlTarget = el.attr("abs:href").toLowerCase();
                if (excludes != null) {
                    for (String exclude : excludes) {
                        if (urlTarget.contains(exclude.toLowerCase())) {
                            continue outer;
                        }
                    }
                }
                urls.add(new URI(urlTarget));
                results++;
            }

            done = !nextPageExists(contentsDoc);

            if (!opened)
                opened = true;

        } catch (URISyntaxException e) {
            // extremely possible, but probably not
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            // this shouldn't happen, except in initial writing
            e.printStackTrace();
        } catch (IOException e) {
            ErrorMessage.SEVERITY severity;
            if (e.getMessage().contains("HTTP Error"))
                severity = ErrorMessage.SEVERITY.WARNING;
            else
                severity = ErrorMessage.SEVERITY.CRITICAL;
            final ErrorMessage err = new ErrorMessage(topic, severity, e);
            outQueue.put(new OutMessage(OutMessage.Type.ERROR, err));
        }
    }

    private boolean nextPageExists(Document contentsDoc) {
        final Elements nextPageElem = contentsDoc.select(searchEngine.NEXT_SELECTOR);
        for (Element e : nextPageElem) {
            if (e.toString().contains("Next"))
                return true;
        }
        return false;
    }
}
