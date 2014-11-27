package io.relayr.tellmewhen.service.rule;

import android.util.Base64;

import com.activeandroid.query.Select;

import java.util.List;

import io.relayr.tellmewhen.model.Rule;
import io.relayr.tellmewhen.model.Status;
import io.relayr.tellmewhen.storage.Storage;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class RuleServiceImpl implements RuleService {

    public static final String API_ENDPOINT = "https://relayr.cloudant.com";

    private RuleApi sRuleApi;

    private RuleApi getRuleApi() {
        if (sRuleApi == null) {
            sRuleApi = new RestAdapter.Builder()
                    .setEndpoint(API_ENDPOINT)
                    .setRequestInterceptor(new RequestInterceptor() {
                        @Override
                        public void intercept(RequestFacade request) {
                            final String authorizationValue = encodeCredentialsForBasicAuthorization();
                            request.addHeader("Authorization", authorizationValue);
                            request.addHeader("Content-Type", "application/json");
                        }

                        private String encodeCredentialsForBasicAuthorization() {
                            final String userAndPassword = "therfectaredurprederferi:cPGohDvGOlO1Ifg7aCQLkhPP";
                            return "Basic " + Base64.encodeToString(userAndPassword.getBytes(), Base64.NO_WRAP);
                        }
                    })
                    .build()
                    .create(RuleApi.class);
        }

        return sRuleApi;
    }

    @Override
    public Observable<Boolean> createRule(final Rule rule) {
        return getRuleApi()
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
        return getRuleApi()
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
        return getRuleApi()
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
    public Observable<Object> getAllRules() {
        return getRuleApi().getAllRules(new Search(Storage.loadUserId()));
    }
}
