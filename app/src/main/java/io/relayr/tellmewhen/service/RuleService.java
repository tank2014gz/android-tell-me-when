package io.relayr.tellmewhen.service;

import java.util.List;

import io.relayr.tellmewhen.model.TMWRule;
import rx.Observable;

public interface RuleService {

    public Observable<Boolean> createRule(TMWRule rule);

    public Observable<Boolean> updateRule(TMWRule rule);

    public Observable<Boolean> deleteRule(TMWRule rule);

    public Observable<List<TMWRule>> loadRemoteRules();

}
