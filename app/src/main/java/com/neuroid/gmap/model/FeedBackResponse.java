package com.neuroid.gmap.model;

import java.util.List;

public class FeedBackResponse {

    public FeedBackResponse(List<FeedBack> feedBackList) {
        this.feedBackList = feedBackList;
    }

    List<FeedBack> feedBackList;

    public List<FeedBack> getFeedBackList() {
        return feedBackList;
    }

    public void setFeedBackList(List<FeedBack> feedBackList) {
        this.feedBackList = feedBackList;
    }
}
