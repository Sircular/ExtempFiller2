package org.zalgosircular.extempfiller2.research.fetching;

import org.zalgosircular.extempfiller2.authentication.KeyManager;
import org.zalgosircular.extempfiller2.messaging.Error;
import org.zalgosircular.extempfiller2.messaging.OutMessage;
import org.zalgosircular.extempfiller2.research.Topic;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Queue;

/**
 * Created by Walt on 7/8/2015.
 */
public class ReadabilityHTMLFetcher extends HTMLFetcher {
    private final String REQUEST_FORMAT = "https://readability.com/api/content/v1/parser?url=%s&token=%s&format=xml";

    public ReadabilityHTMLFetcher(Queue outQueue) {
        super(outQueue);
    }

    public String getResponse(URI location, Topic topic) throws IOException {
        try {
            URI target = new URI(
                    String.format(REQUEST_FORMAT, location.toString(), KeyManager.getKey("readability"))
            );
            return super.getResponse(target, topic);
        } catch (URISyntaxException e) {
            this.outQueue.add(new OutMessage(OutMessage.Type.ERROR, new Error(topic, e)));
        }
        return null;
    }
}
