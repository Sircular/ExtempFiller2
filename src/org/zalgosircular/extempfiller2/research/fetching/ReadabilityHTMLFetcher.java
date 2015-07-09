package org.zalgosircular.extempfiller2.research.fetching;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;
import org.zalgosircular.extempfiller2.authentication.KeyManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.Queue;

/**
 * Created by Walt on 7/8/2015.
 */
public class ReadabilityHTMLFetcher extends HTMLFetcher{
    private final String REQUEST_FORMAT = "https://readability.com/api/content/v1/parser?url=%s&token=%s&format=xml";
    public ReadabilityHTMLFetcher(Queue outQueue) {
        super(outQueue);
    }

    public String getResponse(URI location) throws IOException {
        try {
            URI target = new URI(
                    String.format(REQUEST_FORMAT, location.toString(), KeyManager.getKey("readability"))
            );
            return super.getResponse(target);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }
}
