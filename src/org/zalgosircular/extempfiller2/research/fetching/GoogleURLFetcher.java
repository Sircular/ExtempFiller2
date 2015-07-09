package org.zalgosircular.extempfiller2.research.fetching;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * Created by Walt on 7/8/2015.
 */
public class GoogleURLFetcher extends URLFetcher {

    public GoogleURLFetcher(Queue outQueue) {
        super(outQueue);
    }

    public List<URL> fetchURLs(String topic, int maxArticles, List<String> excludes) throws IOException {
        List<URL> urls = new ArrayList<URL>();
        try {
            // convert the topic to a google search query
            String queryURL = String.format("http://www.google.com/search?q=%s",
                    URLEncoder.encode(topic, "UTF-8"));
            Document contentsDoc = Jsoup.connect(queryURL).get();
            // get just the search result <cite> tags
            // the <a> tags contain google redirection links,
            // but <cite> tags don't
            Elements searchResults = contentsDoc.select("div#ires li.g div.s div.kv cite");
            // get their target urls
            for (Element el : searchResults) {
                String target = el.text();
                urls.add(new URL(target));
            }
            return urls;
        } catch (UnsupportedEncodingException e) {
            // this shouldn't happen, except in initial writing
            e.printStackTrace();
        }
        return null;
    }
}
