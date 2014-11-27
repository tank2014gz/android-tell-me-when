package io.relayr.tellmewhen.service.rule;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Search implements Serializable {

    private List<String> fields;
    private Map<String, String> selector;

    public Search(String userId) {
        this.fields = Arrays.asList("user_id");
        this.selector = new HashMap<String, String>();
        selector.put("user_id", userId);
    }

    public void addField(String fieldId, String fieldValue) {
        this.fields.add(fieldId);
        this.selector.put(fieldId, fieldValue);
    }

}
