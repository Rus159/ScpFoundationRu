package ru.dante.scpfoundation.mvp.base;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.hannesdorfmann.mosby.mvp.MvpNullObjectBasePresenter;

import java.util.List;

import ru.dante.scpfoundation.Constants;
import ru.dante.scpfoundation.MyApplication;
import ru.dante.scpfoundation.R;
import ru.dante.scpfoundation.api.ApiClient;
import ru.dante.scpfoundation.db.DbProviderFactory;
import ru.dante.scpfoundation.db.model.Article;
import ru.dante.scpfoundation.db.model.User;
import ru.dante.scpfoundation.manager.MyPreferenceManager;
import ru.dante.scpfoundation.mvp.contract.LoginActions;
import rx.Observable;
import timber.log.Timber;

/**
 * Created by y.kuchanov on 21.12.16.
 * <p>
 * for scp_ru
 */
public abstract class BasePresenter<V extends BaseMvp.View>
        extends MvpNullObjectBasePresenter<V>
        implements BaseMvp.Presenter<V> {

    MyPreferenceManager mMyPreferencesManager;
    protected DbProviderFactory mDbProviderFactory;
    protected ApiClient mApiClient;

    private User mUser;

    public BasePresenter(MyPreferenceManager myPreferencesManager, DbProviderFactory dbProviderFactory, ApiClient apiClient) {
        mMyPreferencesManager = myPreferencesManager;
        mDbProviderFactory = dbProviderFactory;
        mApiClient = apiClient;

        getUserFromDb();
    }

    @Override
    public void onCreate() {
        Timber.d("onCreate");
    }

    @Override
    public void getUserFromDb() {
        mDbProviderFactory.getDbProvider().getUserAsync().subscribe(
                user -> {
                    mUser = user;
                    if (getView() instanceof LoginActions.View) {
                        ((LoginActions.View) getView()).updateUser(mUser);
                    }
                    onUserChanged(mUser);
                },
                error -> Timber.e(error, "error while get user from DB")
        );
    }

    @Override
    public void onUserChanged(User user) {
        //empty implemetation
    }

    @Override
    public User getUser() {
        return mUser;
    }

    @Override
    public void updateArticleInFirebase(Article article, boolean showResultMessage) {
        Timber.d("updateArticleInFirebase: %s", article.url);
        if (!mMyPreferencesManager.isHasSubscription()) {
            long curNumOfAttempts = mMyPreferencesManager.getNumOfAttemptsToAutoSync();
            long maxNumOfAttempts = FirebaseRemoteConfig.getInstance()
                    .getLong(Constants.Firebase.RemoteConfigKeys.NUM_OF_SYNC_ATTEMPTS_BEFORE_CALL_TO_ACTION);

            Timber.d("does not have subscription, so no auto sync: %s/%s", curNumOfAttempts, maxNumOfAttempts);

            if (curNumOfAttempts >= maxNumOfAttempts) {
                //show call to action
                mMyPreferencesManager.setNumOfAttemptsToAutoSync(0);
                getView().showSnackBarWithAction(Constants.Firebase.CallToActionReason.ENABLE_AUTO_SYNC);
            } else {
                mMyPreferencesManager.setNumOfAttemptsToAutoSync(curNumOfAttempts + 1);
            }
            return;
        }
        mApiClient.writeArticleToFirebase(article)
                .flatMap(article1 -> mDbProviderFactory.getDbProvider().setArticleSynced(article, true))
                .subscribe(
                        article1 -> {
                            Timber.d("sync article onComplete: %s", article1.url);
                            //show only for favorites
                            if (showResultMessage) {
                                getView().showMessage(R.string.sync_fav_success);
                            }
                        },
                        e -> {
                            Timber.e(e);
                            if (showResultMessage) {
                                getView().showError(new Throwable(MyApplication.getAppInstance().getString(R.string.error_while_sync)));
                            }
                        }
                );
    }

    @Override
    public void syncArticles(boolean showResultMessage) {
        Timber.d("syncArticles showResultMessage: %s", showResultMessage);
        //get unsynced articles from DB
        //write them to firebase
        //mark them as synced
        Observable.<List<Article>>create(subscriber -> mDbProviderFactory.getDbProvider().getUnsynedArticlesUnmanaged()
                .subscribe(
                        data -> {
                            subscriber.onNext(data);
                            subscriber.onCompleted();
                        },
                        subscriber::onError
                ))
                .flatMap(articles -> mApiClient.writeArticlesToFirebase(articles))
                .flatMap(articles -> mDbProviderFactory.getDbProvider().setArticlesSynced(articles, true))
                .subscribe(
                        data -> {
                            Timber.d("articles saved to firebase: %s", data);
                            if (showResultMessage) {
                                getView().showMessage(R.string.all_data_sync_success);
                            }
                        },
                        e -> {
                            Timber.e(e);
                            if (showResultMessage) {
                                getView().showMessage(R.string.error_while_all_data_sync);
                            }
                        }
                );
    }
}