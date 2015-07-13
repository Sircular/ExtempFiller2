package org.zalgosircular.extempfiller2.research.fetching.web.html;

import org.jsoup.Jsoup;
import org.zalgosircular.extempfiller2.messaging.ErrorMessage;
import org.zalgosircular.extempfiller2.messaging.OutMessage;
import org.zalgosircular.extempfiller2.research.Topic;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Queue;

/**
 * Created by Walt on 7/9/2015.
 */
public class HTMLFetcher {
    protected final Queue<OutMessage> outQueue;
    private static final int TIMEOUT = 5000;

    public HTMLFetcher(Queue<OutMessage> outQueue) {
        this.outQueue = outQueue;
    }

    public String getResponse(URI location, Topic topic) {
        try {
            return Jsoup.connect(location.toString()).timeout(TIMEOUT).get().html();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            // this shouldn't happen
        } catch (UnsupportedEncodingException e) {
            // this shouldn't happen either
            e.printStackTrace();
        } catch (IOException e) {
            // this will happen quite a bit
            final ErrorMessage err = new ErrorMessage(topic, e);
            outQueue.add(new OutMessage(OutMessage.Type.ERROR, err));
        }
        return null;
    }
}