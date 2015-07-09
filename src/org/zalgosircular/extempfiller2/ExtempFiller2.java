package org.zalgosircular.extempfiller2;

import org.zalgosircular.extempfiller2.messaging.InMessage;
import org.zalgosircular.extempfiller2.messaging.OutMessage;
import org.zalgosircular.extempfiller2.research.Article;
import org.zalgosircular.extempfiller2.research.ResearchWorker;
import org.zalgosircular.extempfiller2.research.Topic;
import org.zalgosircular.extempfiller2.research.fetching.DDGURLFetcher;
import org.zalgosircular.extempfiller2.research.fetching.HTMLFetcher;
import org.zalgosircular.extempfiller2.research.fetching.ReadabilityHTMLFetcher;
import org.zalgosircular.extempfiller2.research.fetching.URLFetcher;
import org.zalgosircular.extempfiller2.research.formatting.ArticleFormatter;
import org.zalgosircular.extempfiller2.research.formatting.TextFormatter;
import org.zalgosircular.extempfiller2.research.parsing.ArticleParser;
import org.zalgosircular.extempfiller2.research.parsing.ReadabilityArticleParser;
import org.zalgosircular.extempfiller2.research.storage.LocalTextStorage;
import org.zalgosircular.extempfiller2.research.storage.StorageFacility;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by Walt on 7/8/2015.
 */
public class ExtempFiller2 {

    public static void main(String[] args) {
        ResearchWorker worker = new ResearchWorker();
        BlockingQueue<InMessage> inQueue = worker.getInQueue();
        BlockingQueue<OutMessage> outQueue = worker.getOutQueue();
        Thread workerThread = new Thread(worker);
        workerThread.start();

        inQueue.add(new InMessage(InMessage.Type.OPEN, null));
        inQueue.add(new InMessage(InMessage.Type.LOAD, null));

        String[] topics = {
                "What is the meaning of life?",
                "What's it like to be from outer space?",
                "How do you like Bornmouth?"
        };
        for (String topic : topics) {
            inQueue.add(new InMessage(InMessage.Type.RESEARCH, topic));
        }
        inQueue.add(new InMessage(InMessage.Type.CLOSE, null));

        while (workerThread.isAlive()) {
            try {
                OutMessage msg = outQueue.poll(100, TimeUnit.MILLISECONDS);
                if (msg != null) {
                    System.out.println(msg);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Finished.");
    }
}
