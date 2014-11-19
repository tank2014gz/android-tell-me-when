package io.relayr.tellmewhen.service;

import java.util.ArrayList;
import java.util.List;

import io.relayr.tellmewhen.model.Rule;
import io.relayr.tellmewhen.storage.Storage;

public class RuleService {

    private static List<Rule> sDbRules = new ArrayList<Rule>();

    public static void saveRule(){
        sDbRules.add(Storage.composeRule());
    }

    public static List<Rule> getsDbRules() {
        return sDbRules;
    }
}
