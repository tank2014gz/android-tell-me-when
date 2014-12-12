package io.relayr.tellmewhen.service.rule;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import io.relayr.tellmewhen.TellMeWhenApplication;
import io.relayr.tellmewhen.model.TMWNotification;
import io.relayr.tellmewhen.model.TMWRule;
import io.relayr.tellmewhen.service.RuleService;
import io.relayr.tellmewhen.service.model.DataMapper;
import io.relayr.tellmewhen.service.model.DbDocuments;
import io.relayr.tellmewhen.service.model.DbRule;
import io.relayr.tellmewhen.service.model.DbSearch;
import io.relayr.tellmewhen.service.model.DbStatus;
import io.relayr.tellmewhen.storage.Storage;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class RuleServiceImpl implements RuleService {

    private final String mGcmRegistration;

    @Inject @Named("ruleApi") RuleApi ruleApi;

    public RuleServiceImpl() {
        TellMeWhenApplication.objectGraph.inject(this);
        mGcmRegistration = Storage.loadGmsRegistrationId();
    }

    @Override
    public Observable<Boolean> createRule(final TMWRule rule) {
        return ruleApi.createRule(DataMapper.toDbRule(rule))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<DbStatus, Boolean>() {
                    @Override
                    public Boolean call(DbStatus status) {
                        return status.getOk().toLowerCase().equals("true");
                    }
                });
    }

    @Override
    public Observable<Boolean> updateRule(final TMWRule rule) {
        return ruleApi.updateRule(rule.dbId, rule.drRev, DataMapper.toDbRule(rule))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<DbStatus, Boolean>() {
                    @Override
                    public Boolean call(DbStatus status) {
                        rule.drRev = status.getRev();
                        rule.save();

                        return status.getOk().toLowerCase().equals("true");
                    }
                });
    }

    @Override
    public Observable<Boolean> deleteRule(final TMWRule rule) {
        return ruleApi.deleteRule(rule.dbId, rule.drRev)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<DbStatus, Boolean>() {
                    @Override
                    public Boolean call(DbStatus status) {
                        new Delete().from(TMWNotification.class).where("ruleId = ?", rule.dbId).execute();

                        return status.getOk().toLowerCase().equals("true");
                    }
                });
    }

    @Override
    public Observable<List<TMWRule>> loadRemoteRules() {
        return ruleApi.getAllRules(new DbSearch(Storage.loadUserId()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<DbDocuments<DbRule>, List<TMWRule>>() {
                    @Override
                    public List<TMWRule> call(DbDocuments<DbRule> docs) {
                        new Delete().from(TMWRule.class).execute();

                        for (DbRule dbRule : docs.getDocuments()) {
                            checkGcmNotification(dbRule);

                            TMWRule rule = DataMapper.toRule(dbRule);
                            rule.save();
                        }

                        return new Select().from(TMWRule.class).execute();
                    }
                });
    }

    private void checkGcmNotification(DbRule dbRule) {
        boolean registered = false;

        List<DbRule.Notification> notifications = dbRule.getNotifications();
        for (DbRule.Notification notification : notifications) {
            if (notification.getKey().equals(mGcmRegistration)) {
                registered = true;
            }
        }

        if (!registered) {
            notifications.add(new DbRule.Notification("gcm", Storage.loadGmsRegistrationId()));
            updateRuleNotifications(dbRule);
        }
    }

    private void updateRuleNotifications(DbRule rule) {
        ruleApi.updateRule(rule.getId(), rule.getRev(), rule)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
