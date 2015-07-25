package org.zalgosircular.extempfiller2.ui;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalgosircular.extempfiller2.messaging.InMessage;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.ArrayBlockingQueue;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class InputRunnableTest {
    private ArrayBlockingQueue<InMessage> inQueue;
    private InputRunnable input;
    private Thread thread;
    private PipedInputStream inputAdapter;
    private PipedOutputStream mockInputReader;

    @Before
    public void setUp() throws Exception {
        inQueue = new ArrayBlockingQueue<InMessage>(10);
        inputAdapter = new PipedInputStream();
        input = new InputRunnable(inQueue, inputAdapter);
        mockInputReader = new PipedOutputStream(inputAdapter);
        thread = new Thread(input);
        thread.start();
    }

    @After
    public void tearDown() throws Exception {
        mockInputReader.close();
        //5np4t adapter will be closed by inputRunnable
        if (thread.isAlive()) {
            thread.interrupt();
        }
    }

    @Test
    public void testResearch() throws Exception {
        String topicName = "abcABC123 !@#";
        mockInputReader.write(String.format("reSearch %s\r\n", topicName).getBytes("UTF-8"));
        mockInputReader.flush();
        Thread.sleep(100);
        InMessage message = inQueue.poll();

        assertTrue("Research command never issued", message != null && message.getMessageType() == InMessage.Type.RESEARCH);
        assertTrue("Topic name not preserved", message.getData().equals(topicName));
    }

    @Test
    public void testDelete() throws Exception {
        String topicName = "abcABC123 !@#";
        mockInputReader.write(String.format("deLete %s\r\n", topicName).getBytes("UTF-8"));
        mockInputReader.flush();
        Thread.sleep(100);
        InMessage message = inQueue.poll();

        assertTrue("Delete message never issued", message != null && message.getMessageType() == InMessage.Type.DELETE);
        assertTrue("Topic name not preserved", message.getData().equals(topicName));
    }

    @Test
    public void testExitCommand() throws Exception {
        mockInputReader.write("exit\r\n".getBytes("UTF-8"));
        mockInputReader.flush();
        Thread.sleep(100);
        InMessage message = inQueue.poll();
        assertTrue("Close message never issued", message != null && message.getMessageType() == InMessage.Type.CLOSE);
    }

    @Test
    public void testViewCommand() throws Exception {
        mockInputReader.write("view\r\n".getBytes("UTF-8"));
        mockInputReader.flush();
        Thread.sleep(100);
        InMessage message = inQueue.poll();
        assertTrue("Get message never issued", message != null && message.getMessageType() == InMessage.Type.GET);
    }

    @Test
    public void testClose() throws Exception {
        thread.interrupt();
        Thread.sleep(100);
        assertFalse("CLI never stopped on close", thread.isAlive());
    }
}