package org.zalgosircular.extempfiller2.research.fetching.web.html;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.zalgosircular.extempfiller2.messaging.ErrorMessage;
import org.zalgosircular.extempfiller2.messaging.OutMessage;
import org.zalgosircular.extempfiller2.research.Topic;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Walt on 7/9/2015.
 */
public class HTMLFetcher {
    private static final int TIMEOUT = 10 * 1000;
    protected final BlockingQueue<OutMessage> outQueue;

    public HTMLFetcher(BlockingQueue<OutMessage> outQueue) {
        this.outQueue = outQueue;
    }

    public String getResponse(URI location, Topic topic) throws InterruptedException, HttpStatusException, SocketTimeoutException {
        try {
            return Jsoup.connect(location.toString()).timeout(TIMEOUT).get().html();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            // this shouldn't happen
        } catch (UnsupportedEncodingException e) {
            // this shouldn't happen either
            e.printStackTrace();
        } catch (HttpStatusException e) { // extends IOException
            throw e;
        } catch (IOException e) {
            // this will happen quite a bit
            if (e instanceof SocketTimeoutException) {
                throw (SocketTimeoutException)e;
            } else {
                final ErrorMessage err = new ErrorMessage(topic, ErrorMessage.SEVERITY.CRITICAL, e);
                outQueue.put(new OutMessage(OutMessage.Type.ERROR, err));
            }
        }
        return null;
    }
}
