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

    //TODO: Get rid of this.... strings in java are always unicode...

    /*public static Charset getEncoding(String s) {
        // basic trial and error
        // we only need to test for ISO-8859-1
        final String[] possibles = {"ISO-8859-1"};
        Charset charset;
        String testStr;
        for (String enc : possibles) {
            charset = Charset.forName(enc);
            testStr = new String(s.getBytes(charset), charset);
            if (testStr.equals(s))
                return charset;
        }
        // utf-8 is the default
        return Charset.forName("utf-8");
    }*/
}
