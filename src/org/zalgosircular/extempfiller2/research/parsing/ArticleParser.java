package org.zalgosircular.extempfiller2.research.parsing;

import org.zalgosircular.extempfiller2.messaging.OutMessage;
import org.zalgosircular.extempfiller2.research.Article;

import java.util.Queue;

/**
 * Created by Walt on 7/8/2015.
 */
public abstract class ArticleParser {
    protected final Queue<OutMessage> outQueue;

    public ArticleParser(Queue<OutMessage> outQueue) {
        this.outQueue = outQueue;
    }

    public abstract Article parse(String contents);
}
