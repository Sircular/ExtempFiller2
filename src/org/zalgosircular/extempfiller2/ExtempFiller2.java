package org.zalgosircular.extempfiller2;

import org.zalgosircular.extempfiller2.messaging.InMessage;
import org.zalgosircular.extempfiller2.research.Topic;

/**
 * Created by Walt on 7/8/2015.
 */
public class ExtempFiller2 {

    public static void main(String[] args) {
        System.out.println("Hello world!");
        Topic t = new Topic("my topeka");
        InMessage m = new InMessage(InMessage.Type.DELETE, t);
        System.out.println(m.toString());
    }
}
