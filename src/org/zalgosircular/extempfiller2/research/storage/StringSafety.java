package org.zalgosircular.extempfiller2.research.storage;

/**
 * Created by Logan Lembke on 7/8/2015.
 */
//Converts topic strings to filesystem save versions
public class StringSafety {
    public static String charNumUnderscore(String s) {
        return s.replaceAll(" ", "_").replaceAll("\\W+", "").substring(0, 255);
    }
}
