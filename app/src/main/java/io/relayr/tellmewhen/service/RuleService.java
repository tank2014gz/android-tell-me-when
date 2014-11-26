package io.relayr.tellmewhen.service;

import java.util.List;

import io.relayr.tellmewhen.model.Rule;
import io.relayr.tellmewhen.model.Status;
import rx.Observable;

public interface RuleService {

    public Observable<Status> saveRule();

    public Observable<List<Rule>> getRules();

}
