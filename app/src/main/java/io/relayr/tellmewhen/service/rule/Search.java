package io.relayr.tellmewhen.service.rule;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Search implements Serializable {

    @SerializedName("selector")
    private Selector selector;

    public Search(String userId) {
        selector = new Selector(userId);
    }

    static class Selector implements Serializable {

        @SerializedName("user_id") private String userId;

        public Selector(String userId) {
            this.userId = userId;
        }

    }

}
