package org.zalgosircular.extempfiller2;

import org.zalgosircular.extempfiller2.messaging.Message;
import org.zalgosircular.extempfiller2.messaging.OutMessages;

/**
 * Created by Walt on 7/8/2015.
 */
public class ExtempFiller2 {

    public static void main(String[] args) {
        System.out.println("Hello world!");
        Message m = new Message(OutMessages.DEBUG, "derp");
        System.out.println(m.getType().name());
    }
}
