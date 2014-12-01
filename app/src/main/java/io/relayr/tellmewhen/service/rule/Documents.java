package io.relayr.tellmewhen.service.rule;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class Documents implements Serializable {

    @SerializedName("docs")
    private List<DbRule> documents;

    public Documents(List<DbRule> documents) {
        this.documents = documents;
    }

    public List<DbRule> getDocuments() {
        return documents;
    }

    public void setDocuments(List<DbRule> documents) {
        this.documents = documents;
    }
}
