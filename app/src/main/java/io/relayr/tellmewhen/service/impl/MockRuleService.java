package io.relayr.tellmewhen.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.relayr.tellmewhen.model.Rule;
import io.relayr.tellmewhen.model.Status;
import io.relayr.tellmewhen.service.RuleService;
import io.relayr.tellmewhen.storage.Storage;
import rx.Observable;
import rx.Subscriber;

public class MockRuleService implements RuleService {

    private static List<Rule> sDbRules = new ArrayList<Rule>();

    @Override
    public Observable<Status> saveRule() {
        sDbRules.add(Storage.getRule());
        Storage.clearRuleData();

        return Observable.create(new Observable.OnSubscribe<Status>() {
            @Override
            public void call(Subscriber<? super Status> subscriber) {
                subscriber.onNext(new Status("", "200", ""));
            }
        });
    }

    @Override
    public List<Rule> getRules() {
       return sDbRules;
    }
}
