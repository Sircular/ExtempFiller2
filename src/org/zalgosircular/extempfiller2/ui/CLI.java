package org.zalgosircular.extempfiller2.ui;

import org.zalgosircular.extempfiller2.messaging.InMessage;
import org.zalgosircular.extempfiller2.messaging.OutMessage;

import java.util.concurrent.BlockingQueue;

/**
 * Created by Walt on 7/10/2015.
 */
public class CLI {
    private final Thread outputThread;
    private final Thread inputThread;
    private final BlockingQueue<InMessage> inQueue;
    private final BlockingQueue<OutMessage> outQueue;

    public CLI(BlockingQueue<InMessage> inQueue, BlockingQueue<OutMessage> outQueue) {
        final InputRunnable inputRunnable = new InputRunnable(inQueue, System.in);
        final OutputRunnable outputRunnable = new OutputRunnable(outQueue, System.out, System.err);
        this.inputThread = new Thread(inputRunnable);
        this.outputThread = new Thread(outputRunnable);
        this.outQueue = outQueue;
        this.inQueue = inQueue;
    }

    public void start() {
        inQueue.add(new InMessage(InMessage.Type.OPEN, null));
        inQueue.add(new InMessage(InMessage.Type.LOAD, null));
        outputThread.start();
        inputThread.start();
    }

    public boolean isRunning() {
        return inputThread.isAlive() || outputThread.isAlive();
    }

    public void close() {
        inputThread.interrupt();
        outputThread.interrupt();
    }

}
