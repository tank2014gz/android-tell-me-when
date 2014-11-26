package io.relayr.tellmewhen.model;

import java.io.Serializable;

public class Status implements Serializable {

    private String id;
    private String ok;
    private String rev;

    public Status(String id, String ok, String rev) {
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

    public void setOk(String ok) {
        this.ok = ok;
    }

    public String getRev() {
        return rev;
    }

    public void setRev(String rev) {
        this.rev = rev;
    }

    @Override
    public String toString() {
        return "Status{" +
                "id='" + id + '\'' +
                ", ok='" + ok + '\'' +
                ", rev='" + rev + '\'' +
                '}';
    }
}
