package org.zalgosircular.extempfiller2.ui.cli;

import org.zalgosircular.extempfiller2.messaging.ErrorMessage;
import org.zalgosircular.extempfiller2.messaging.OutMessage;
import org.zalgosircular.extempfiller2.messaging.SavedMessage;
import org.zalgosircular.extempfiller2.research.Topic;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Logan Lembke on 7/25/2015.
 */
class OutputRunnable implements Runnable {
    private final BlockingQueue<OutMessage> outQueue;
    private final PrintStream out;
    private final PrintStream err;

    OutputRunnable(BlockingQueue<OutMessage> outQueue, OutputStream out, OutputStream err) {
        this.outQueue = outQueue;
        this.out = new PrintStream(out);
        this.err = new PrintStream(err);
    }

    public void run() {
        out.println("Starting program.");
        while (!Thread.currentThread().isInterrupted()) {
            try {
                OutMessage msg = outQueue.take();
                // switches are weird
                String msgStr;
                Topic topic;
                switch (msg.getMessageType()) {
                    case DEBUG:
                        msgStr = (String) msg.getData();
                        out.println("[DEBUG] " + msgStr);
                        break;
                    case SEARCHING:
                        topic = (Topic) msg.getData();
                        out.println("Now researching topic: " + topic.getTopic());
                        break;
                    case SAVING:
                        topic = (Topic) msg.getData();
                        out.println("Now saving articles for topic: " + topic.getTopic());
                        break;
                    case SAVED:
                        final SavedMessage savedMessage = (SavedMessage) msg.getData();
                        out.println(
                                "Saved " + savedMessage.getArticle().getTitle()
                                        + " under topic: " + savedMessage.getTopic().getTopic());
                        break;
                    case DONE:
                        topic = (Topic) msg.getData();
                        out.println(String.format("Found total of %d articles for topic: %s",
                                topic.getArticleCount(), topic.getTopic()));
                        break;
                    case ALREADY_RESEARCHED:
                        topic = (Topic) msg.getData();
                        out.println("Already researched topic: " + topic.getTopic());
                        break;
                    case ERROR:
                        final ErrorMessage e = (ErrorMessage) msg.getData();
                        err.println("[ERROR] Exception while researching " + e.getTopic().toString());
                        if (e.getException() != null)
                            e.getException().printStackTrace();
                        break;
                    case LOADING:
                        out.println("Loading saved topics...");
                        break;
                    case LOADED:
                        out.println("Loaded topics.");
                        break;
                    case RETRIEVED:
                        out.println("Currently researched topics:");
                        final List<Topic> topics = (List<Topic>) msg.getData();
                        for (Topic t : topics) {
                            System.out.println(String.format(
                                    "[%2d] %s", t.getArticleCount(), t.getTopic()
                            ));
                        }
                        break;
                    case CLOSED:
                        out.println("Researcher closed.");
                        throw new InterruptedException();
                    case DELETING:
                        topic = (Topic) msg.getData();
                        out.println("Now deleting topic: " + topic.getTopic());
                        break;
                    case DELETED:
                        topic = (Topic) msg.getData();
                        out.println("Successfully deleted topic: " + topic.getTopic());
                        break;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                //expected
            }
        }
        out.println("Exiting program.");
    }
}
