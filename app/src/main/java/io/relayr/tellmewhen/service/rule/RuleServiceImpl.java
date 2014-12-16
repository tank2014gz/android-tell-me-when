package io.relayr.tellmewhen.service.rule;

import android.util.Log;

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
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class RuleServiceImpl implements RuleService {

    private final String mGcmRegistration;
    private final String mOldGcmRegistration;

    @Inject @Named("ruleApi") RuleApi ruleApi;

    public RuleServiceImpl() {
        TellMeWhenApplication.objectGraph.inject(this);
        mGcmRegistration = Storage.loadGmsRegId();
        mOldGcmRegistration = Storage.loadOldGmsRegId();
    }

    @Override
    public Observable<Boolean> createRule(final TMWRule rule) {
        return ruleApi.createRule(DataMapper.toDbRule(rule))
                .subscribeOn(Schedulers.io())
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
        DbRule.Notification notifToRemove = null;

        List<DbRule.Notification> notifications = dbRule.getNotifications();
        for (DbRule.Notification notification : notifications) {
            if (notification.getKey().equals(mGcmRegistration)) registered = true;

            if (mOldGcmRegistration != null && notification.getKey().equals(mOldGcmRegistration))
                notifToRemove = notification;
        }

        if (notifToRemove != null) notifications.remove(notifToRemove);

        if (!registered) notifications.add(new DbRule.Notification("gcm", Storage.loadGmsRegId()));

        if (!registered || notifToRemove != null)
            updateRuleNotifications(dbRule);
    }

    private void updateRuleNotifications(final DbRule rule) {
        ruleApi.updateRule(rule.getId(), rule.getRev(), rule)
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<DbStatus>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("RuleService", "Problem updating the rule");
                    }

                    @Override
                    public void onNext(DbStatus dbStatus) {
                        Log.e("RuleService", "Updated rule: " + rule.getDetails().getName());
                    }
                });
    }
}
