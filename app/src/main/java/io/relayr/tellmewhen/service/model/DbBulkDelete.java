package io.relayr.tellmewhen.service.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class DbBulkDelete implements Serializable {

    @SerializedName("_id") private String id;
    @SerializedName("_rev") private String rev;
    @SerializedName("_deleted") private boolean deleted = true;

    public DbBulkDelete(String id, String rev) {
        this.id = id;
        this.rev = rev;
    }

    public String getId() {
        return id;
    }

    public String getRev() {
        return rev;
    }

    public boolean getDeleted() {
        return deleted;
    }
}
