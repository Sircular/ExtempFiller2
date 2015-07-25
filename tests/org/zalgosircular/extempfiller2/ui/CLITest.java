package org.zalgosircular.extempfiller2.ui;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalgosircular.extempfiller2.messaging.InMessage;
import org.zalgosircular.extempfiller2.messaging.OutMessage;

import java.util.concurrent.ArrayBlockingQueue;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CLITest {

    private ArrayBlockingQueue<InMessage> inQueue;
    private ArrayBlockingQueue<OutMessage> outQueue;
    private CLI cli;

    @Before
    public void setUp() throws Exception {
        inQueue = new ArrayBlockingQueue<InMessage>(10);
        outQueue = new ArrayBlockingQueue<OutMessage>(10);
        cli = new CLI(inQueue, outQueue);
        cli.start();
    }

    @After
    public void tearDown() throws Exception {
        if (cli != null && cli.isRunning()) {
            cli.close();
        }
    }

    @Test
    public void testStart() throws Exception {
        InMessage message = inQueue.poll();
        assertTrue("Open message not received", message != null && message.getMessageType() == InMessage.Type.OPEN);
        message = inQueue.poll();
        assertTrue("Load message not received", message != null && message.getMessageType() == InMessage.Type.LOAD);
    }

    @Test
    public void testIsRunning() throws Exception {
        assertTrue("CLI never started", cli.isRunning());
    }

    @Test
    public void testClose() throws Exception {
        Thread.sleep(100);
        cli.close();
        Thread.sleep(100); //sucky but necessary
        assertFalse("CLI never stopped on close", cli.isRunning());
    }
}