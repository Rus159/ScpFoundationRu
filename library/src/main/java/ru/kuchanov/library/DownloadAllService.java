package ru.kuchanov.library;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by mohax on 11.01.2017.
 * <p>
 * for scp_ru
 */
public abstract class DownloadAllService<T extends ArticleModel> extends Service {

    private static final int NOTIFICATION_ID = 42;

    public static final int RANGE_NONE = Integer.MIN_VALUE;
    private static final String EXTRA_DOWNLOAD_TYPE = "EXTRA_DOWNLOAD_TYPE";
    private static final String EXTRA_RANGE_START = "EXTRA_RANGE_START";

    private static final String EXTRA_RANGE_END = "EXTRA_RANGE_END";
    private static final String ACTION_STOP = "ACTION_STOP";
    private static final String ACTION_START = "ACTION_START";

    protected static DownloadAllService instance = null;

    private int rangeStart;
    private int rangeEnd;

    private int mCurProgress;
    private int mMaxProgress;
    private int mNumOfErrors;

    private CompositeSubscription mCompositeSubscription;

    public static boolean isRunning() {
        return instance != null;
    }

    public static void startDownloadWithType(
            Context ctx,
            DownloadEntry type,
            int rangeStart,
            int rangeEnd,
            Class clazz
    ) {
        Intent intent = new Intent(ctx, clazz);
        intent.setAction(ACTION_START);
        intent.putExtra(EXTRA_DOWNLOAD_TYPE, type);
        intent.putExtra(EXTRA_RANGE_START, rangeStart);
        intent.putExtra(EXTRA_RANGE_END, rangeEnd);
        ctx.startService(intent);
    }

    public static void stopDownload(Context ctx, Class clazz) {
        Timber.d("stopDownload called");
        Intent intent = new Intent(ctx, clazz);
        intent.setAction(ACTION_STOP);
        ctx.startService(intent);
    }

    @Override
    public void onDestroy() {
        Timber.d("onDestroy");
        instance = null;
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Timber.d("onCreate");
        super.onCreate();
        instance = this;
    }

    private void stopDownloadAndRemoveNotif() {
        mCurProgress = 0;
        mMaxProgress = 0;
        mNumOfErrors = 0;
        if (mCompositeSubscription != null && !mCompositeSubscription.isUnsubscribed()) {
            mCompositeSubscription.unsubscribe();
        }
        stopForeground(true);
        stopSelf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.d("onStartCommand: %s, %s, %s", intent, false, startId);
        if (intent == null || TextUtils.isEmpty(intent.getAction())) {
            stopDownloadAndRemoveNotif();
            return super.onStartCommand(intent, flags, startId);
        }
        if (intent.getAction().equals(ACTION_STOP)) {
            stopDownloadAndRemoveNotif();
            return super.onStartCommand(intent, flags, startId);
        }

        //check for not being RANGE_NONE and use while download
        rangeStart = intent.getIntExtra(EXTRA_RANGE_START, RANGE_NONE);
        rangeEnd = intent.getIntExtra(EXTRA_RANGE_END, RANGE_NONE);
        Timber.d("rangeStart/rangeEnd: %s/%s", rangeStart, rangeEnd);

        DownloadEntry type = (DownloadEntry) intent.getSerializableExtra(EXTRA_DOWNLOAD_TYPE);
        download(type);

        return super.onStartCommand(intent, flags, startId);
    }

    protected abstract void download(DownloadEntry type);

    protected abstract Observable<Integer> getRecentArticlesPageCountObservable();

    protected abstract int getNumOfArticlesOnRecentPage();

    protected abstract Observable<List<T>> getRecentArticlesForPage(int page);

    protected abstract DbProviderModel<T> getDbProviderModel();

    protected abstract T getArticleFromApi(String id)  throws Exception, ScpParseException;

    protected void downloadAll() {
        Timber.d("downloadAll");
        showNotificationDownloadList();
        //download list
        Subscription subscription = getRecentArticlesPageCountObservable()
                //if we have limit we must not load all lists of articles
                .map(pageCount -> (rangeStart != RANGE_NONE && rangeEnd != RANGE_NONE)
                        ? (int) Math.ceil((double) rangeEnd / getNumOfArticlesOnRecentPage()) : pageCount)
                .doOnNext(pageCount -> mMaxProgress = pageCount)
                //FI XME for test do not load all arts lists
//                .doOnNext(pageCount -> mMaxProgress = 7)
                .doOnError(throwable -> showNotificationSimple(
                        getString(R.string.error_notification_title),
                        getString(R.string.error_notification_recent_list_download_content)
                ))
                .onExceptionResumeNext(Observable.<Integer>empty().delay(5, TimeUnit.SECONDS))
                .flatMap(integer -> Observable.range(1, mMaxProgress))
                .flatMap(integer -> getRecentArticlesForPage(integer)
                        .doOnNext(list -> {
                            mCurProgress = integer;
                            showNotificationDownloadProgress(getString(R.string.notification_recent_list_title),
                                    mCurProgress, mMaxProgress, mNumOfErrors);
                        })
                        .flatMap(Observable::from)
                        .doOnError(throwable -> {
                            mCurProgress = integer;
                            mNumOfErrors++;
                            showNotificationDownloadProgress(getString(R.string.notification_recent_list_title),
                                    mCurProgress, mMaxProgress, mNumOfErrors);
                        })
                        .onExceptionResumeNext(Observable.empty()))
                .toList()
//                //test value
//                .flatMap(list -> Observable.just(list.subList(0, mMaxProgress)))
                .map(limitArticles)
                .map(articles -> {
                    List<ArticleModel> articlesToDownload = new ArrayList<>();
                    DbProviderModel dbProvider = getDbProviderModel();
                    for (ArticleModel article : articles) {
                        ArticleModel articleInDb = dbProvider.getUnmanagedArticleSync(article.getUrl());
                        if (articleInDb == null || TextUtils.isEmpty(articleInDb.getText())) {
                            articlesToDownload.add(article);
                        } else {
                            mCurProgress++;
                            Timber.d("already downloaded: %s", article.getUrl());
                            Timber.d("mCurProgress %s, mMaxProgress: %s", mCurProgress, mMaxProgress);
//                            showNotificationDownloadProgress(getString(R.string.download_recent_title),
//                                    mCurProgress, mMaxProgress, mNumOfErrors);
                        }
                    }
                    dbProvider.close();
                    return articlesToDownload;
                })
                //download all articles and save them to DB
                .flatMap(articles -> {
                    DbProviderModel dbProvider = getDbProviderModel();
                    for (int i = 0; i < articles.size(); i++) {
                        ArticleModel articleToDownload = articles.get(i);
                        try {
                            ArticleModel articleDownloaded = getArticleFromApi(articleToDownload.getUrl());
                            if (articleDownloaded != null) {
                                dbProvider.saveArticleSync(articleDownloaded, false);
                                Timber.d("downloaded: %s", articleDownloaded.getUrl());
                                mCurProgress++;
                                Timber.d("mCurProgress %s, mMaxProgress: %s", mCurProgress, mMaxProgress);
                                showNotificationDownloadProgress(getString(R.string.download_objects_title),
                                        mCurProgress, mMaxProgress, mNumOfErrors);
                            } else {
                                mNumOfErrors++;
                                mCurProgress++;
                                showNotificationDownloadProgress(
                                        getString(R.string.download_objects_title),
                                        mCurProgress, mMaxProgress, mNumOfErrors
                                );
                            }
                        } catch (Exception | ScpParseException e) {
                            Timber.e(e);
                            mNumOfErrors++;
                            mCurProgress++;
                            showNotificationDownloadProgress(
                                    getString(R.string.download_objects_title),
                                    mCurProgress, mMaxProgress, mNumOfErrors
                            );
                        }
                    }
                    dbProvider.close();
                    return Observable.just(null);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(
                        article -> showNotificationSimple(
                                getString(R.string.download_complete_title),
                                getString(R.string.download_complete_title_content,
                                        mCurProgress - mNumOfErrors, mMaxProgress, mNumOfErrors)
                        ),
                        e -> {
                            Timber.e(e, "error download objects");
                            stopDownloadAndRemoveNotif();
                        },
                        () -> {
                            Timber.d("onCompleted");
                            stopDownloadAndRemoveNotif();
                        }
                );
        if (mCompositeSubscription == null) {
            mCompositeSubscription = new CompositeSubscription();
        }
        mCompositeSubscription.add(subscription);
    }

    private void downloadObjects(DownloadEntry type, String dbField) {
        showNotificationDownloadList();
        //download lists
        Observable<List<ArticleModel>> articlesObservable;
        switch (type) {
            case Constants.Urls.ARCHIVE:
                articlesObservable = mApiClient.getMaterialsArchiveArticles();
                break;
            case Constants.Urls.JOKES:
                articlesObservable = mApiClient.getMaterialsJokesArticles();
                break;
            case Constants.Urls.OBJECTS_1:
            case Constants.Urls.OBJECTS_2:
            case Constants.Urls.OBJECTS_3:
            case Constants.Urls.OBJECTS_RU:
                articlesObservable = mApiClient.getObjectsArticles(link);
                break;
            default:
                articlesObservable = mApiClient.getMaterialsArticles(link);
                break;
        }

       if (type.resId == R.string.type_archive) {
            articlesObservable = mApiClient.getMaterialsArchiveArticles();
            numOfArticlesObservable = articlesObservable.map(List::size);
        } else if (type.resId == R.string.type_jokes) {
            articlesObservable = mApiClient.getMaterialsJokesArticles();
            numOfArticlesObservable = articlesObservable.map(List::size);
        } else if (type.resId == R.string.type_1
                || type.resId == R.string.type_2
                || type.resId == R.string.type_3
                || type.resId == R.string.type_4
                || type.resId == R.string.type_ru) {
            articlesObservable = mApiClient.getObjectsArticles(type.url);
            numOfArticlesObservable = articlesObservable.map(List::size);
        } else {
            articlesObservable = mApiClient.getMaterialsArticles(type.url);
            numOfArticlesObservable = articlesObservable.map(List::size);
        }

        //just for test use just n elements
//        final int testMaxProgress = 8;
        Subscription subscription = articlesObservable
                .doOnError(throwable -> showNotificationSimple(
                        getString(R.string.error_notification_title),
                        getString(R.string.error_notification_objects_list_download_content)
                ))
                .onExceptionResumeNext(Observable.<List<Article>>empty().delay(5, TimeUnit.SECONDS))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(articles -> mDbProviderFactory.getDbProvider()
                        .<Pair<Integer, Integer>>saveObjectsArticlesList(articles, dbField)
                        .flatMap(integerIntegerPair -> Observable.just(articles)))
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .map(limitArticles)
                .map(articles -> {
                    List<Article> articlesToDownload = new ArrayList<>();
                    DbProvider dbProvider = mDbProviderFactory.getDbProvider();
                    for (Article article : articles) {
                        Article articleInDb = dbProvider.getUnmanagedArticleSync(article.url);
                        if (articleInDb == null || articleInDb.text == null) {
                            articlesToDownload.add(article);
                        } else {
                            mCurProgress++;
                            Timber.d("already downloaded: %s", article.url);
                            Timber.d("mCurProgress %s, mMaxProgress: %s", mCurProgress, mMaxProgress);
//                            showNotificationDownloadProgress(getString(R.string.download_objects_title), mCurProgress, mMaxProgress, mNumOfErrors);
                        }
                    }
                    dbProvider.close();
                    return articlesToDownload;
                })
                .flatMap(articles -> {
                    DbProvider dbProvider = mDbProviderFactory.getDbProvider();
                    for (int i = 0; i < articles.size(); i++) {
                        Article articleToDownload = articles.get(i);
                        try {
                            Article articleDownloaded = mApiClient.getArticleFromApi(articleToDownload.url);
                            if (articleDownloaded != null) {
                                dbProvider.saveArticleSync(articleDownloaded, false);
                                Timber.d("downloaded: %s", articleDownloaded.url);
                                mCurProgress++;
                                Timber.d("mCurProgress %s, mMaxProgress: %s", mCurProgress, mMaxProgress);
                                showNotificationDownloadProgress(getString(R.string.download_objects_title),
                                        mCurProgress, mMaxProgress, mNumOfErrors);
                            } else {
                                mNumOfErrors++;
                                mCurProgress++;
                                showNotificationDownloadProgress(
                                        getString(R.string.download_objects_title),
                                        mCurProgress, mMaxProgress, mNumOfErrors
                                );
                            }
                        } catch (Exception e) {
                            Timber.e(e);
                            mNumOfErrors++;
                            mCurProgress++;
                            showNotificationDownloadProgress(
                                    getString(R.string.download_objects_title),
                                    mCurProgress, mMaxProgress, mNumOfErrors
                            );
                        }
                    }
                    dbProvider.close();
                    return Observable.just(articles);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(
                        article -> showNotificationSimple(
                                getString(R.string.download_complete_title),
                                getString(R.string.download_complete_title_content,
                                        mCurProgress - mNumOfErrors, mMaxProgress, mNumOfErrors)
                        ),
                        e -> {
                            Timber.e(e, "error download objects");
                            stopDownloadAndRemoveNotif();
                        },
                        () -> {
                            Timber.d("onCompleted");
                            stopDownloadAndRemoveNotif();
                        }
                );
        if (mCompositeSubscription == null) {
            mCompositeSubscription = new CompositeSubscription();
        }
        mCompositeSubscription.add(subscription);
    }

    private Func1<List<T>, List<T>> limitArticles = articles -> {
        mCurProgress = 0;
        if (rangeStart == RANGE_NONE && rangeEnd == RANGE_NONE) {
            mMaxProgress = articles.size();
        } else {
            mMaxProgress = rangeEnd - rangeStart;
            articles = articles.subList(rangeStart, rangeEnd);
        }
        return articles;
    };

    private void showNotificationDownloadList() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setContentTitle(getString(R.string.download_objects_title))
                .setAutoCancel(false)
                .setContentText(getString(R.string.downlad_art_list))
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setSmallIcon(R.drawable.ic_download_white_24dp);

        startForeground(NOTIFICATION_ID, builder.build());
    }

    private void showNotificationDownloadProgress(String title, int cur, int max, int errorsCount) {
        NotificationCompat.Builder builderArticlesList = new NotificationCompat.Builder(this);
        String content = getString(R.string.download_progress_content, cur, max, errorsCount);
        builderArticlesList.setContentTitle(title)
                .setAutoCancel(false)
                .setContentText(content)
                .setProgress(max, cur, false)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setSmallIcon(R.drawable.ic_download_white_24dp);

        startForeground(NOTIFICATION_ID, builderArticlesList.build());
    }

    private void showNotificationSimple(String title, String content) {
        NotificationCompat.Builder builderArticlesList = new NotificationCompat.Builder(this);
        builderArticlesList
                .setContentTitle(title)
                .setContentText(content)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setSmallIcon(R.drawable.ic_bug_report_white_24dp);

        startForeground(NOTIFICATION_ID, builderArticlesList.build());
    }
}