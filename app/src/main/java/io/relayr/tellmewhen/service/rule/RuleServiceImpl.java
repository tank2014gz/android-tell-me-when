package io.relayr.tellmewhen.service.rule;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import io.relayr.tellmewhen.TellMeWhenApplication;
import io.relayr.tellmewhen.model.Rule;
import io.relayr.tellmewhen.model.RuleNotification;
import io.relayr.tellmewhen.model.Status;
import io.relayr.tellmewhen.service.RuleService;
import io.relayr.tellmewhen.storage.Storage;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class RuleServiceImpl implements RuleService {

    @Inject @Named("ruleApi") RuleApi ruleApi;

    public RuleServiceImpl(){
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
                        boolean saveStatus = status.getOk().toLowerCase().equals("true");

                        if (saveStatus) {
                            rule.dbId = status.getId();
                            rule.drRev = status.getRev();
                            rule.save();
                        }

                        return saveStatus;
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
                        boolean saveStatus = status.getOk().toLowerCase().equals("true");

                        if (saveStatus) {
                            rule.drRev = status.getRev();
                            rule.save();
                        }

                        return saveStatus;
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
                        boolean deleteStatus = status.getOk().toLowerCase().equals("true");

                        if (deleteStatus) {
                            rule.delete();
                        }

                        return deleteStatus;
                    }
                });
    }

    @Override
    public List<Rule> getLocalRules() {
        return new Select().from(Rule.class).execute();
    }

    @Override
    public Observable<Boolean> loadRemoteRules() {
        new Delete().from(Rule.class).execute();
        new Delete().from(RuleNotification.class).execute();

        return ruleApi.getAllRules(new Search(Storage.loadUserId()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<Object, Boolean>() {
                    @Override
                    public Boolean call(Object rules) {
                        //TODO if there is some rules persist the to local DB
                        return true;
                    }
                });
    }
}
