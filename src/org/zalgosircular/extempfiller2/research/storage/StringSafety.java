package org.zalgosircular.extempfiller2.research.storage;

/**
 * Created by Logan Lembke on 7/8/2015.
 */
//Converts topic strings to filesystem save versions
public class StringSafety {
    public static String charNumUnderscore(String s) {
        int len = s.length();
        return s.replaceAll(" ", "_").replaceAll("\\W+", "").substring(0, len > 255 ? 255 : len);
    }
}
