package org.zalgosircular.extempfiller2.messaging;

import org.zalgosircular.extempfiller2.research.Article;
import org.zalgosircular.extempfiller2.research.Topic;

/**
 * Created by Logan Lembke on 7/10/2015.
 */
public class SavedMessage {
    private final Article article;
    private final Topic topic;

    public SavedMessage(Article article, Topic topic) {
        this.article = article;
        this.topic = topic;
    }

    public Article getArticle() {
        return article;
    }

    public Topic getTopic() {
        return topic;
    }
}
