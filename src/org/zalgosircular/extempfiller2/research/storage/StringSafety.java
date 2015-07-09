package org.zalgosircular.extempfiller2.research.storage;

/**
 * Created by Logan Lembke on 7/8/2015.
 */
//Converts topic strings to filesystem save versions
public class StringSafety {
    public static String charNumScore255(String s) {
        final String tempStr = s.replaceAll(" ", "_").replaceAll("\\W+", "");
        int len = tempStr.length();
        return tempStr.substring(0, len > 255 ? 255 : len);
    }
}
