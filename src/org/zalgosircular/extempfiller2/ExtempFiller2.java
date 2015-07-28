package org.zalgosircular.extempfiller2;

import org.zalgosircular.extempfiller2.messaging.InMessage;
import org.zalgosircular.extempfiller2.messaging.OutMessage;
import org.zalgosircular.extempfiller2.research.ResearchWorker;
import org.zalgosircular.extempfiller2.research.fetching.ArticleFetcher;
import org.zalgosircular.extempfiller2.research.fetching.web.ReadabilityArticleFetcher;
import org.zalgosircular.extempfiller2.research.fetching.web.urls.SEARCH_ENGINE;
import org.zalgosircular.extempfiller2.research.formatting.ENMLFormatter;
import org.zalgosircular.extempfiller2.research.storage.StorageFacility;
import org.zalgosircular.extempfiller2.research.storage.evernote.EvernoteStorage;
import org.zalgosircular.extempfiller2.ui.cli.CLI;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Walt on 7/8/2015.
 */
public class ExtempFiller2 {

    public static void main(String[] args) {
        final BlockingQueue<InMessage> inQueue = new ArrayBlockingQueue<InMessage>(1024);
        final BlockingQueue<OutMessage> outQueue = new ArrayBlockingQueue<OutMessage>(1024);
        final ArticleFetcher fetcher = new ReadabilityArticleFetcher(outQueue, SEARCH_ENGINE.GOOGLE);
        final StorageFacility storage = new EvernoteStorage(outQueue, new ENMLFormatter());
        final ResearchWorker worker = new ResearchWorker(inQueue, outQueue, fetcher, storage);
        worker.getThread().start();

        // initialize the UI
        CLI cli = new CLI(inQueue, outQueue);
        cli.start();
    }
}
