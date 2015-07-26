package org.zalgosircular.extempfiller2;

import org.zalgosircular.extempfiller2.messaging.InMessage;
import org.zalgosircular.extempfiller2.messaging.OutMessage;
import org.zalgosircular.extempfiller2.research.ResearchWorker;
import org.zalgosircular.extempfiller2.ui.CLI;
import org.zalgosircular.extempfiller2.ui.gui.GUI;

import java.util.concurrent.BlockingQueue;

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
        cli.run();
    }

    private static void runGUI(BlockingQueue<InMessage> inQueue,
                               BlockingQueue<OutMessage> outQueue) {
        GUI cli = new GUI(inQueue, outQueue);
        cli.run();
    }
}
