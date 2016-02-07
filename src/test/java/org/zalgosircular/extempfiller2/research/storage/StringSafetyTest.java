package org.zalgosircular.extempfiller2.research.storage;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StringSafetyTest {

    @Test
    public void testCharNumScore255() throws Exception {
        String testString = " abc ABC 123 !@# \"a\" .";
        String resultString = "_abc_ABC_123__a_";
        assertEquals("String sanitization failed!", StringSafety.charNumScore255(testString), resultString);
    }
}