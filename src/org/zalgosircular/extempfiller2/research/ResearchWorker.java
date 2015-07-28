package org.zalgosircular.extempfiller2.research;

import org.zalgosircular.extempfiller2.messaging.ErrorMessage;
import org.zalgosircular.extempfiller2.messaging.InMessage;
import org.zalgosircular.extempfiller2.messaging.OutMessage;
import org.zalgosircular.extempfiller2.messaging.SavedMessage;
import org.zalgosircular.extempfiller2.research.fetching.ArticleFetcher;
import org.zalgosircular.extempfiller2.research.storage.StorageFacility;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Walt on 7/9/2015.
 *
 * Comments from Logan: Never instantiate a thread of this runnable outside the get thread method.
 * The get thread method sets up important parameters for the thread
 */
public class ResearchWorker implements Runnable {

    private final static int MAX_ARTICLES = 10; // we'll add a way to change this later

    private Thread researchThread = null;
    public final static String THREAD_NAME = "Research Worker";

    private final BlockingQueue<InMessage> inQueue;
    private final BlockingQueue<OutMessage> outQueue;

    private final ArticleFetcher fetcher;
    private final StorageFacility storage;

    public ResearchWorker(BlockingQueue<InMessage> inQueue, BlockingQueue<OutMessage> outQueue,
                          ArticleFetcher fetcher, StorageFacility storage) {
        this.inQueue = inQueue;
        this.outQueue = outQueue;
        this.fetcher = fetcher;
        this.storage = storage;
    }

    public BlockingQueue<InMessage> getInQueue() {
        return inQueue;
    }

    public BlockingQueue<OutMessage> getOutQueue() {
        return outQueue;
    }

    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                final InMessage msg = inQueue.take();
                switch (msg.getMessageType()) {
                    case OPEN:
                        fetcher.open();
                        storage.open();
                        break;
                    case CLOSE:
                        storage.close();
                        throw new InterruptedException();
                    case LOAD:
                        outQueue.put(new OutMessage(OutMessage.Type.LOADING, null));
                        final List<Topic> topics = storage.loadResearched();
                        outQueue.put(new OutMessage(OutMessage.Type.LOADED, topics));
                        break;
                    case GET:
                        final List<Topic> topics1 = storage.getResearched();
                        outQueue.put(new OutMessage(OutMessage.Type.RETRIEVED, topics1));
                        break;
                    case RESEARCH:
                        final String topicStr = (String) msg.getData();
                        final Topic addTopic = new Topic(topicStr);
                        // check to see if it's been queued for deletion
                        boolean deleted = false;
                        final Iterator<InMessage> mIt = inQueue.iterator();
                        while (mIt.hasNext() && !deleted) {
                            final InMessage delMsg = mIt.next();
                            if (delMsg.getMessageType() == InMessage.Type.DELETE &&
                                    ((Topic) delMsg.getData()).equals(addTopic)) {
                                deleted = true;
                                mIt.remove();
                            }
                        }
                        if (deleted) {
                            outQueue.put(new OutMessage(OutMessage.Type.DELETED, addTopic));
                            break;
                        }

                        // check to see if it's already been done.
                        if (storage.exists(topicStr)) {
                            outQueue.put(new OutMessage(OutMessage.Type.ALREADY_RESEARCHED, addTopic));
                            break;
                        }

                        outQueue.put(new OutMessage(OutMessage.Type.SEARCHING, addTopic));
                        final List<Article> articles = fetcher.fetchArticles(addTopic, MAX_ARTICLES, null);
                        if (articles.size() > 0) {
                            outQueue.put(new OutMessage(OutMessage.Type.SAVING, addTopic));

                            for (Article article : articles) {
                                if (storage.save(addTopic, article)) {
                                    outQueue.put(new OutMessage(
                                            OutMessage.Type.SAVED,
                                            new SavedMessage(article, addTopic)
                                    ));
                                    addTopic.setArticleCount(addTopic.getArticleCount() + 1);
                                }
                                // if it doesn't work, the storage has already
                                // sent up an error message
                            }
                        }
                        outQueue.put(new OutMessage(OutMessage.Type.DONE, addTopic));
                        break;
                    case DELETE:
                        final String delString = (String) msg.getData();
                        final Topic delTopic = storage.getTopic(delString);
                        if (delTopic != null) {
                            outQueue.put(new OutMessage(OutMessage.Type.DELETING, delTopic));
                            if (storage.delete(delTopic))
                                outQueue.put(new OutMessage(OutMessage.Type.DELETED, delTopic));
                        } else {
                            final RuntimeException e = new RuntimeException("Delete failed: Topic does not exist.");
                            final Topic erredTopic = new Topic(delString);
                            erredTopic.setArticleCount(-1);
                            outQueue.put(
                                    new OutMessage(
                                            OutMessage.Type.ERROR,
                                            new ErrorMessage(
                                                    erredTopic,
                                                    e
                                            )
                                    )
                            );
                        }
                        //kept as if else for branch prediction optimization
                        break;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        outQueue.add(new OutMessage(OutMessage.Type.CLOSED, null));
    }

    public Thread getThread() {
        if (researchThread == null) {
            researchThread = new Thread(this);
            researchThread.setName(THREAD_NAME);
        }
        return researchThread;
    }
}
