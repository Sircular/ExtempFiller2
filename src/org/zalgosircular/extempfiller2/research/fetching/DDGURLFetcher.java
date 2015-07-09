package org.zalgosircular.extempfiller2.research.fetching;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
    private final String QUERY_STRING = "http://duckduckgo.com/?q=%s";

    public DDGURLFetcher(Queue outQueue) {
        super (outQueue);
    }

    public List<URI> fetchURLs(String topic, int maxArticles, List<String> excludes) throws IOException {
        List<URI> urls = new ArrayList<>();
        try {
            // convert the topic to a duckduckgo search query
            String queryURL = String.format(QUERY_STRING,
                    URLEncoder.encode(topic, "UTF-8"));
            Document contentsDoc = Jsoup.connect(queryURL).userAgent(USER_AGENT).get();
            // duckduckgo is better about this than google
            Elements searchResults = contentsDoc.select("a.result__a");
            // get their target urls
            for (Element el : searchResults) {
                // YES
                // JSOUP HANDLES REDIRECTS
                String urlTarget = el.attr("href").toLowerCase();
                boolean excluded = false;
                for (String exclude : excludes) {
                    if (urlTarget.contains(exclude.toLowerCase())) {
                        excluded = true;
                        break;
                    }
                }
                if (!excluded) {
                    urls.add(new URI(urlTarget));
                    if (urls.size() >= maxArticles)
                        break;
                }
            }
            return urls;
        } catch (URISyntaxException e) {
            // extremely possible, but probably not
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            // this shouldn't happen, except in initial writing
            e.printStackTrace();
        }
        return null;
    }
}
