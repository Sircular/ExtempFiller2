package org.zalgosircular.extempfiller2.messaging;

import org.zalgosircular.extempfiller2.research.Topic;

/**
 * Created by Walt on 7/8/2015.
 */
public enum InMessages {
    LOAD(Object.class), // because it's null
    RESEARCH(String.class),
    DELETE(Topic.class),
    OPEN(Object.class),
    CLOSE(Object.class);

    private Class c;
    InMessages(Class c) {
        this.c = c;
    }
}
