package io.relayr.tellmewhen.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name = "Push")
public class TMWPush extends Model {

    @Column(name = "ruleId") public String ruleId;
    @Column(name = "pkey") public String key;
    @Column(name = "ptype") public String type;
}
