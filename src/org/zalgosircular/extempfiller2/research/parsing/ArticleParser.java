package org.zalgosircular.extempfiller2.research.parsing;

import org.zalgosircular.extempfiller2.research.Article;

import java.util.Queue;

/**
 * Created by Walt on 7/8/2015.
 */
public abstract class ArticleParser {
    protected Queue outQueue;

    public ArticleParser(Queue outQueue) {
        this.outQueue = outQueue;
    }

    public abstract Article parse(String contents);
}
