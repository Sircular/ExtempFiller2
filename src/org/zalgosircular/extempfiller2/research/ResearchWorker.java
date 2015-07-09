package org.zalgosircular.extempfiller2.research;

import org.zalgosircular.extempfiller2.messaging.InMessage;
import org.zalgosircular.extempfiller2.messaging.OutMessage;
import org.zalgosircular.extempfiller2.messaging.Error;
import org.zalgosircular.extempfiller2.research.fetching.GoogleURLFetcher;
import org.zalgosircular.extempfiller2.research.fetching.HTMLFetcher;
import org.zalgosircular.extempfiller2.research.fetching.ReadabilityHTMLFetcher;
import org.zalgosircular.extempfiller2.research.fetching.URLFetcher;
import org.zalgosircular.extempfiller2.research.parsing.ArticleParser;
import org.zalgosircular.extempfiller2.research.parsing.ReadabilityArticleParser;
import org.zalgosircular.extempfiller2.research.storage.LocalTextStorage;
import org.zalgosircular.extempfiller2.research.storage.StorageFacility;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Walt on 7/9/2015.
 */
public class ResearchWorker implements Runnable {

    private final BlockingQueue<InMessage> inQueue;
    private final BlockingQueue<OutMessage> outQueue;

    private final URLFetcher urlFetcher;
    private final HTMLFetcher htmlFetcher;
    private final ArticleParser parser;
    private final StorageFacility storage;

    public ResearchWorker() {
        inQueue = new ArrayBlockingQueue<InMessage>(1024);
        outQueue = new ArrayBlockingQueue<OutMessage>(1024);

        urlFetcher = new GoogleURLFetcher(outQueue);
        htmlFetcher = new ReadabilityHTMLFetcher(outQueue);
        parser = new ReadabilityArticleParser(outQueue);
        storage = new LocalTextStorage(outQueue);
    }

    public BlockingQueue<InMessage> getInQueue() {
        return inQueue;
    }

    public BlockingQueue<OutMessage> getOutQueue() {
        return outQueue;
    }

    public void run() {
        boolean running = true;
        while(running) {
            try {
                InMessage msg = inQueue.take();
                switch (msg.getMessageType()) {
                    case OPEN:
                        storage.open();
                        break;
                    case CLOSE:
                        storage.close();
                        // we're done here
                        running = false;
                        break;
                    case LOAD:
                        outQueue.add(new OutMessage(OutMessage.Type.LOADING, null));
                        outQueue.add(new OutMessage(OutMessage.Type.LOADED, storage.load()));
                        break;
                    case RESEARCH:
                        // TODO: the thing we actually did
                        String topicStr = (String)msg.getData();
                        Topic addTopic = new Topic(topicStr);
                        outQueue.add(new OutMessage(OutMessage.Type.SEARCHING, addTopic));
                        List<URI> urls = urlFetcher.fetchURLs(topicStr, 10, null);
                        outQueue.add(new OutMessage(OutMessage.Type.SAVING, addTopic));
                        for (URI url : urls) {
                            String html = htmlFetcher.getResponse(url, addTopic);
                            Article article = parser.parse(html);
                            storage.save(addTopic, article);
                        }
                        outQueue.add(new OutMessage(OutMessage.Type.DONE, addTopic));
                        break;
                    case DELETE:
                        Topic delTopic = (Topic)msg.getData();
                        outQueue.add(new OutMessage(OutMessage.Type.DELETING, delTopic));
                        storage.delete(delTopic);
                        outQueue.add(new OutMessage(OutMessage.Type.DELETED, delTopic));
                }
            } catch (InterruptedException e) {
                outQueue.add(new OutMessage(OutMessage.Type.DEBUG, "Queue interrupted. Fatal? Maybe not."));
            } catch (IOException e) {
                // we have no topic, so it is null
                outQueue.add(new OutMessage(OutMessage.Type.ERROR, new Error(null, e)));
            }
        }
    }
}
