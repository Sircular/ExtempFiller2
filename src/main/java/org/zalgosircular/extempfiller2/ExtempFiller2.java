package org.zalgosircular.extempfiller2;

import org.zalgosircular.extempfiller2.messaging.InMessage;
import org.zalgosircular.extempfiller2.messaging.OutMessage;
import org.zalgosircular.extempfiller2.research.ResearchWorker;
import org.zalgosircular.extempfiller2.research.fetching.ArticleFetcher;
import org.zalgosircular.extempfiller2.research.fetching.web.ReadabilityArticleFetcher;
import org.zalgosircular.extempfiller2.research.fetching.web.urls.SEARCH_ENGINE;
import org.zalgosircular.extempfiller2.research.formatting.HTMLFormatter;
import org.zalgosircular.extempfiller2.research.storage.HTMLIndexStorage;
import org.zalgosircular.extempfiller2.research.storage.StorageFacility;
import org.zalgosircular.extempfiller2.ui.cli.CLI;
import org.zalgosircular.extempfiller2.ui.gui.GUI;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Walt on 7/8/2015.
 */
public class ExtempFiller2 {

    //TODO: allow switching between backends at runtime
    public static void main(String[] args) {
        final BlockingQueue<InMessage> inQueue = new ArrayBlockingQueue<InMessage>(1024);
        final BlockingQueue<OutMessage> outQueue = new ArrayBlockingQueue<OutMessage>(1024);
        final ArticleFetcher fetcher = new ReadabilityArticleFetcher(outQueue, SEARCH_ENGINE.DDG);
        final StorageFacility storage = new HTMLIndexStorage(outQueue, new HTMLFormatter());
        final ResearchWorker worker = new ResearchWorker(inQueue, outQueue, fetcher, storage);
        worker.getThread().start();

        // initialize the UI
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("gui")) {
                runGUI(inQueue, outQueue);
            } else if (args[0].equalsIgnoreCase("cli")) {
                runCLI(inQueue, outQueue);
            } else {
                // output a debug message
                System.out.println(String.format("Unrecognized parameter: %s.\n", args[0])+
                "Recognized options: gui, cli.\n"+
                "Initializing GUI (default.)");
                runGUI(inQueue, outQueue);
            }
        } else {
            runGUI(inQueue, outQueue);
        }
    }

    private static void runCLI(BlockingQueue<InMessage> inQueue,
                               BlockingQueue<OutMessage> outQueue) {
        CLI cli = new CLI(inQueue, outQueue);
        cli.start();
    }

    private static void runGUI(BlockingQueue<InMessage> inQueue,
                               BlockingQueue<OutMessage> outQueue) {
        GUI gui = new GUI(inQueue, outQueue);
        gui.start();
    }
}
