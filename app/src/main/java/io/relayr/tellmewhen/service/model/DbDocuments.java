package io.relayr.tellmewhen.service.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class DbDocuments<T> implements Serializable {

    @SerializedName("docs")
    private List<T> documents;

    public DbDocuments(List<T> documents) {
        this.documents = documents;
    }

    public List<T> getDocuments() {
        return documents;
    }

    public void setDocuments(List<T> documents) {
        this.documents = documents;
    }
}
