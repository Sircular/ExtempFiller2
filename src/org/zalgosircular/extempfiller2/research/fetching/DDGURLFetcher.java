package org.zalgosircular.extempfiller2.research.fetching;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * Created by Walt on 7/8/2015.
 */
public class DDGURLFetcher extends URLFetcher {
    private final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.132 Safari/537.36";
    private final String QUERY_STRING = "http://duckduckgo.com/html/?q=%s";

    public DDGURLFetcher(Queue<OutMessage> outQueue) {
        super(outQueue);
    }

    public List<URI> fetchURLs(Topic topic, int maxArticles, List<String> excludes) {
        final List<URI> urls = new ArrayList<URI>();
        try {
            // convert the topic to a duckduckgo search query
            final String queryURL = String.format(QUERY_STRING,
                    URLEncoder.encode(topic.getTopic(), "UTF-8"));
            final Document contentsDoc = Jsoup.connect(queryURL).userAgent(USER_AGENT).get();
            // duckduckgo is better about this than google
            // Logan: An explanation of the selection would be nice
            final Elements searchResults = contentsDoc.select("div#links div.links_main.links_deep a");
            // get their target urls
            String urlTarget;
            outer:
            for (Element el : searchResults) {
                // JSOUP HANDLES REDIRECTS
                urlTarget = el.attr("href").toLowerCase();
                if (excludes != null) {
                    for (String exclude : excludes) {
                        if (urlTarget.contains(exclude.toLowerCase())) {
                            continue outer;
                        }
                    }
                }
                urls.add(new URI(urlTarget));
                if (urls.size() >= maxArticles)
                    break;
            }
            return urls;
        } catch (URISyntaxException e) {
            // extremely possible, but probably not
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            // this shouldn't happen, except in initial writing
            e.printStackTrace();
        } catch (IOException e) {
            final ErrorMessage err = new ErrorMessage(topic, e);
            outQueue.add(new OutMessage(OutMessage.Type.ERROR, err));
        }
        return null;
    }
}
