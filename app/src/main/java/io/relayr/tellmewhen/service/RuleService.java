package io.relayr.tellmewhen.service;

import java.util.List;
import java.util.Objects;

import io.relayr.tellmewhen.model.Rule;
import io.relayr.tellmewhen.model.Status;
import rx.Observable;

public interface RuleService {

    public Observable<Boolean> createRule(Rule rule);

    public Observable<Boolean> updateRule(Rule rule);

    public Observable<Boolean> deleteRule(Rule rule);

    public Observable<List<Rule>> loadRemoteRules();

//    public Observable<Boolean> populateLocalDatabase();

}
