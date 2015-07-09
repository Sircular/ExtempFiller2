package org.zalgosircular.extempfiller2.research;

/**
 * Created by Walt on 7/8/2015.
 */
public class Topic {
    private final String topic;
    private int articleCount;

    public Topic(String topic) {
        this.topic = topic;
        this.articleCount = 0;
    }

    public String getTopic() {
        return topic;
    }

    public int getArticleCount() {
        return articleCount;
    }

    public void setArticleCount(int articleCount) {
        this.articleCount = articleCount;
    }
}
