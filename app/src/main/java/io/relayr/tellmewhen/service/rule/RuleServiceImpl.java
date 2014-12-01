package io.relayr.tellmewhen.service.rule;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import io.relayr.tellmewhen.TellMeWhenApplication;
import io.relayr.tellmewhen.model.Rule;
import io.relayr.tellmewhen.model.Status;
import io.relayr.tellmewhen.service.RuleService;
import io.relayr.tellmewhen.storage.Storage;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class RuleServiceImpl implements RuleService {

    @Inject @Named("ruleApi") RuleApi ruleApi;

    public RuleServiceImpl() {
        TellMeWhenApplication.objectGraph.inject(this);
    }

    @Override
    public Observable<Boolean> createRule(final Rule rule) {
        return ruleApi
                .createRule(RuleMapper.toDbRule(rule))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<Status, Boolean>() {
                    @Override
                    public Boolean call(Status status) {
                        return status.getOk().toLowerCase().equals("true");
                    }
                });
    }

    @Override
    public Observable<Boolean> updateRule(final Rule rule) {
        return ruleApi
                .updateRule(rule.dbId, rule.drRev, RuleMapper.toDbRule(rule))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<Status, Boolean>() {
                    @Override
                    public Boolean call(Status status) {
                        return status.getOk().toLowerCase().equals("true");
                    }
                });
    }

    @Override
    public Observable<Boolean> deleteRule(final Rule rule) {
        return ruleApi
                .deleteRule(rule.dbId, rule.drRev)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<Status, Boolean>() {
                    @Override
                    public Boolean call(Status status) {
                        return status.getOk().toLowerCase().equals("true");
                    }
                });
    }

    @Override
    public Observable<List<Rule>> loadRemoteRules() {
        return ruleApi.getAllRules(new Search(Storage.loadUserId()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<Documents, List<Rule>>() {
                    @Override
                    public List<Rule> call(Documents docs) {
                        List<Rule> rules = new ArrayList<Rule>();

                        for (DbRule dbRule : docs.getDocuments()) {
                            rules.add(RuleMapper.toRule(dbRule));
                        }

                        return rules;
                    }
                });
    }
//
//    @Override
//    public Observable<Boolean> populateLocalDatabase() {
//        new Delete().from(Rule.class).execute();
//
//        return loadRemoteRules()
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .map(new Func1<List<Rule>, Boolean>() {
//                    @Override
//                    public Boolean call(List<Rule> rules) {
//                        for (Rule rule : rules) {
//                            rule.save();
//                        }
//
//                        return !rules.isEmpty();
//                    }
//                });
//    }
}
