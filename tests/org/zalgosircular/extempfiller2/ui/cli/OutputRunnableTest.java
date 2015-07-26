package org.zalgosircular.extempfiller2.ui.cli;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalgosircular.extempfiller2.messaging.OutMessage;
import org.zalgosircular.extempfiller2.messaging.SavedMessage;
import org.zalgosircular.extempfiller2.research.Article;
import org.zalgosircular.extempfiller2.research.Topic;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.ArrayBlockingQueue;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OutputRunnableTest {
    private ArrayBlockingQueue<OutMessage> outQueue;
    private OutputRunnable outputRunnable;
    private Thread thread;
    private ByteArrayOutputStream mockOutput;
    private ByteArrayOutputStream mockErrOutput;
    private static final String endl = System.getProperty("line.separator");

    @Before
    public void setUp() throws Exception {
        outQueue = new ArrayBlockingQueue<OutMessage>(10);
        mockOutput = new ByteArrayOutputStream(512);
        mockErrOutput = new ByteArrayOutputStream(128);
        outputRunnable = new OutputRunnable(outQueue, mockOutput, mockErrOutput);
        thread = new Thread(outputRunnable);
        thread.start();
    }

    @After
    public void tearDown() throws Exception {
        mockOutput.close();
        mockErrOutput.close();

        if (thread.isAlive()) {
            thread.interrupt();
        }
    }

    @Test
    public void testDebug() throws Exception {
        String debugString = "test string";
        outQueue.add(new OutMessage(OutMessage.Type.DEBUG, debugString));
        Thread.sleep(100);
        String out = mockOutput.toString();
        String test = "Starting program." + endl + "[DEBUG] " + debugString + endl;
        assertTrue("Debug message failed", out.equals(test));
    }

    @Test
    public void testSearching() throws Exception {
        String debugString = "test string";
        Topic t = new Topic(debugString);
        String test = "Starting program." + endl + "Now researching topic: " + t.getTopic() + endl;
        outQueue.add(new OutMessage(OutMessage.Type.SEARCHING, t));
        Thread.sleep(100);
        String out = mockOutput.toString("UTF-8");
        assertTrue("Searching message failed", out.equals(test));
    }

    @Test
    public void testSaving() throws Exception {
        String debugString = "test string";
        Topic t = new Topic(debugString);
        String test = "Starting program." + endl + "Now saving articles for topic: " + t.getTopic() + endl;
        outQueue.add(new OutMessage(OutMessage.Type.SAVING, t));
        Thread.sleep(100);
        String out = mockOutput.toString("UTF-8");
        assertTrue("Saving message failed", out.equals(test));
    }

    @Test
    public void testSaved() throws Exception {
        String debugString1 = "test string";
        String debugString2 = "aaaa";
        Article a = new Article("http://dsf.com", debugString1, "asdf", null, "adsf");
        Topic t = new Topic(debugString2);
        String test = "Starting program." + endl + "Saved " + debugString1
                + " under topic: " + debugString2 + endl;
        outQueue.add(new OutMessage(OutMessage.Type.SAVED, new SavedMessage(a, t)));
        Thread.sleep(100);
        String out = mockOutput.toString("UTF-8");
        assertTrue("Saving message failed", out.equals(test));
    }

    @Test
    public void testDoneMessage() throws Exception {
        String debugString = "test string";
        int debugCount = 3;
        Topic t = new Topic(debugString);
        t.setArticleCount(debugCount);
        String test = "Starting program." + endl + String.format("Found total of %d articles for topic: %s",
                debugCount, debugString) + endl;
        outQueue.add(new OutMessage(OutMessage.Type.DONE, t));
        Thread.sleep(100);
        String out = mockOutput.toString("UTF-8");
        assertTrue("Saving message failed", out.equals(test));
    }

    @Test
    public void testCloseByMessage() throws Exception {
        outQueue.add(new OutMessage(OutMessage.Type.CLOSED, null));
        Thread.sleep(100);
        assertFalse("Output Runnable never closed via message", thread.isAlive());
    }

    /*
        These so far are just sample tests for the possible messages.
        If these work, it is fairly safe to assume the rest work as well.
        As time becomes available, more of these tests may be written.
        However they are very low on the priority list.
     */

    @Test
    public void testCloseByInterrupt() throws Exception {
        thread.interrupt();
        Thread.sleep(100);
        assertFalse("Output Runnable never closed via interrupt", thread.isAlive());
    }
}