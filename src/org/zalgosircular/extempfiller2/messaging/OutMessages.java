package org.zalgosircular.extempfiller2.messaging;

import org.zalgosircular.extempfiller2.research.Topic;

import java.util.List;

/**
 * Created by Walt on 7/8/2015.
 */
public enum OutMessages {
    DEBUG(String.class),
    SEARCHING(Topic.class),
    SAVING(Topic.class),
    DONE(Topic.class),
    ERROR(Error.class),
    LOADING(Object.class), // because we pass null
    LOADED(List.class),
    DELETING(Topic.class),
    DELETED(Topic.class);

    private Class c;
    OutMessages(Class c) {
        this.c = c;
    }
}
