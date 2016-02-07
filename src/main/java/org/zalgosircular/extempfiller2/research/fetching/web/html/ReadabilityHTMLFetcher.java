package org.zalgosircular.extempfiller2.research.fetching.web.html;

import org.zalgosircular.extempfiller2.authentication.AuthManager;
import org.zalgosircular.extempfiller2.authentication.AuthRequest;
import org.zalgosircular.extempfiller2.messaging.ErrorMessage;
import org.zalgosircular.extempfiller2.messaging.OutMessage;
import org.zalgosircular.extempfiller2.research.Topic;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Walt on 7/8/2015.
 *
 * Comment by Logan: In order to *properly* use this class one must call auth first.
 * KeyManager.requestAuth is a blocking operation Be warned
 */
public class ReadabilityHTMLFetcher extends HTMLFetcher {
    private static final String REQUEST_FORMAT = "https://readability.com/api/content/v1/parser?url=%s&token=%s&format=xml";
    private String key = null;
    public ReadabilityHTMLFetcher(BlockingQueue<OutMessage> outQueue) {
        super(outQueue);
    }

    public void auth() throws InterruptedException {
        key = AuthManager.requestAuth(outQueue, new AuthRequest(new String[]{"readability"})).getResponses()[0];
    }

    public String getResponse(URI location, Topic topic) throws InterruptedException {
        if (key == null) {
            auth();
        }
        try {
            final URI target = new URI(
                    String.format(REQUEST_FORMAT, location.toString(), key)
            );
            return super.getResponse(target, topic);
        } catch (URISyntaxException e) {
            outQueue.put(new OutMessage(OutMessage.Type.ERROR, new ErrorMessage(topic, ErrorMessage.SEVERITY.ERROR, e)));
        }
        return null;
    }
}
