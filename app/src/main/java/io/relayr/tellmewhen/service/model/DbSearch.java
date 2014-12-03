package io.relayr.tellmewhen.service.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class DbSearch implements Serializable {

    @SerializedName("selector")
    private Selector selector;

    public DbSearch(String userId) {
        selector = new Selector(userId);
    }

    static class Selector implements Serializable {

        @SerializedName("user_id") private String userId;

        public Selector(String userId) {
            this.userId = userId;
        }

    }

}
