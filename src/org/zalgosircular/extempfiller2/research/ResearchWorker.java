package org.zalgosircular.extempfiller2.research;

import org.zalgosircular.extempfiller2.messaging.InMessage;
import org.zalgosircular.extempfiller2.messaging.OutMessage;
import org.zalgosircular.extempfiller2.messaging.SavedMessage;
import org.zalgosircular.extempfiller2.research.fetching.ArticleFetcher;
import org.zalgosircular.extempfiller2.research.fetching.web.WebArticleFetcher;
import org.zalgosircular.extempfiller2.research.fetching.web.urls.SEARCH_ENGINE;
import org.zalgosircular.extempfiller2.research.formatting.ENMLFormatter;
import org.zalgosircular.extempfiller2.research.storage.StorageFacility;
import org.zalgosircular.extempfiller2.research.storage.evernote.EvernoteStorage;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Walt on 7/9/2015.
 */
public class ResearchWorker implements Runnable {

    private final static int MAX_ARTICLES = 10; // we'll add a way to change this later

    private final BlockingQueue<InMessage> inQueue;
    private final BlockingQueue<OutMessage> outQueue;

    private final ArticleFetcher fetcher;
    private final StorageFacility storage;

    public ResearchWorker() {
        inQueue = new ArrayBlockingQueue<InMessage>(1024);
        outQueue = new ArrayBlockingQueue<OutMessage>(1024);

        // todo: add ways to change this at runtime
        fetcher = new WebArticleFetcher(outQueue, SEARCH_ENGINE.GOOGLE);
        storage = new EvernoteStorage(outQueue, new ENMLFormatter());
    }

    public BlockingQueue<InMessage> getInQueue() {
        return inQueue;
    }

    public BlockingQueue<OutMessage> getOutQueue() {
        return outQueue;
    }

    public void run() {
        boolean running = true;
        while (running) {
            try {
                final InMessage msg = inQueue.take();
                switch (msg.getMessageType()) {
                    case OPEN:
                        storage.open();
                        break;
                    case CLOSE:
                        storage.close();
                        running = false;
                        break;
                    case LOAD:
                        outQueue.add(new OutMessage(OutMessage.Type.LOADING, null));
                        final List<Topic> topics = storage.loadResearched();
                        outQueue.add(new OutMessage(OutMessage.Type.LOADED, topics));
                        break;
                    case RESEARCH:
                        final String topicStr = (String) msg.getData();
                        final Topic addTopic = new Topic(topicStr);
                        // check to see if it's already been done.
                        if (!storage.exists(topicStr)) {
                            // check to see if it's been queued for deletion
                            boolean deleted = false;
                            Iterator<InMessage> mIt = inQueue.iterator();
                            while (mIt.hasNext() && !deleted) {
                                InMessage delMsg = mIt.next();
                                if (delMsg.getMessageType() == InMessage.Type.DELETE &&
                                        ((Topic)delMsg.getData()).equals(addTopic)) {
                                    deleted = true;
                                    mIt.remove();
                                }
                            }
                            if (deleted) {
                                outQueue.add(new OutMessage(OutMessage.Type.SEARCHING, addTopic));
                                final List<Article> articles = fetcher.fetchArticles(addTopic, MAX_ARTICLES, null);
                                if (articles.size() > 0) {
                                    outQueue.add(new OutMessage(OutMessage.Type.SAVING, addTopic));
                                    int articleCount = 0;
                                    for (Article article : articles) {
                                        if (storage.save(addTopic, article)) {
                                            outQueue.add(new OutMessage(
                                                    OutMessage.Type.SAVED,
                                                    new SavedMessage(article, addTopic)
                                            ));
                                            articleCount++;
                                        }
                                        // if it doesn't work, the storage has already
                                        // sent up an error message
                                    }
                                    addTopic.setArticleCount(articleCount);
                                    outQueue.add(new OutMessage(OutMessage.Type.DONE, addTopic));
                                }
                            }
                        } else {
                            outQueue.add(new OutMessage(OutMessage.Type.ALREADY_RESEARCHED, addTopic));
                        }
                        break;
                    case DELETE:
                        final Topic delTopic = (Topic) msg.getData();
                        outQueue.add(new OutMessage(OutMessage.Type.DELETING, delTopic));
                        storage.delete(delTopic);
                        outQueue.add(new OutMessage(OutMessage.Type.DELETED, delTopic));
                }
            } catch (InterruptedException e) {
                outQueue.add(new OutMessage(OutMessage.Type.DEBUG, "Queue interrupted."));
                running = false;
            }
        }
        outQueue.add(new OutMessage(OutMessage.Type.CLOSED, null));
    }
}
