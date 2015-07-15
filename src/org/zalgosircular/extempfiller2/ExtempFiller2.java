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
        GUI gui = new GUI(inQueue, outQueue);
        gui.run();
    }
}
