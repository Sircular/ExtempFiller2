package org.zalgosircular.extempfiller2.ui.gui;

import org.zalgosircular.extempfiller2.messaging.InMessage;
import org.zalgosircular.extempfiller2.messaging.OutMessage;

import java.util.concurrent.BlockingQueue;

/**
 * Created by Walt on 7/15/2015.
 */
public class GUI {
    private final GUIWindow window;
    private final Thread outputThread;

    public GUI(BlockingQueue<InMessage> inQueue, BlockingQueue<OutMessage> outQueue) {
        this.window = new GUIWindow(inQueue);
        this.outputThread = new Thread(new OutputRunnable(window, outQueue));
        this.outputThread.setName("OutMessage Loop");
    }

    public void start() {
        window.init();
        window.start();
        outputThread.start();
    }

}
