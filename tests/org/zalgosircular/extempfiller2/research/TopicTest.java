package org.zalgosircular.extempfiller2.research;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TopicTest {

    @Test
    public void testEquals() throws Exception {
        String testString = "TESTING please hold";
        Topic a = new Topic(testString);
        Topic b = new Topic(testString);
        assertEquals("Topics with the same topic string are not equal", a, b);
    }
}