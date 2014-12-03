package io.relayr.tellmewhen.service.model;

import java.io.Serializable;

public class DbStatus implements Serializable {

    private String id;
    private String ok;
    private String rev;

    public DbStatus(String id, String ok, String rev) {
        this.id = id;
        this.ok = ok;
        this.rev = rev;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOk() {
        return ok;
    }

    public String getRev() {
        return rev;
    }
}
