package io.relayr.tellmewhen.service.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class DbSearch implements Serializable {

    @SerializedName("selector")
    private Selector selector;

    public DbSearch(String userId) {
        selector = new Selector(userId);
    }

    public DbSearch(String userId, String ruleId) {
        selector = new Selector(userId, ruleId);
    }

    static class Selector implements Serializable {

        @SerializedName("user_id") private String userId;
        @SerializedName("rule_id") private String ruleId;

        public Selector(String userId) {
            this.userId = userId;
        }

        public Selector(String userId, String ruleId) {
            this.userId = userId;
            this.ruleId = ruleId;
        }
    }

}
