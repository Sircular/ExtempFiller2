package org.zalgosircular.extempfiller2.research;

/**
 * Created by Walt on 7/8/2015.
 */
public class Topic implements Comparable<Topic> {
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

    @Override
    public String toString() {
        return topic;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Topic && ((Topic) o).getTopic().equals(this.topic));
    }

    public int compareTo(Topic t) {
        return topic.compareTo(t.getTopic());
    }
}
