package ru.dante.scpfoundation.util;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.List;

import ru.kuchanov.scp.downloads.ApiClientModel;
import ru.kuchanov.scp.downloads.ConstantValues;
import ru.kuchanov.scp.downloads.DbProviderFactoryModel;
import ru.kuchanov.scp.downloads.DialogUtils;
import ru.kuchanov.scp.downloads.DownloadEntry;
import ru.kuchanov.scp.downloads.MyPreferenceManagerModel;
import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.service.DownloadAllServiceDefault;
import ru.kuchanov.scpcore.ui.dialog.SubscriptionsFragmentDialog;
import timber.log.Timber;

/**
 * Created by mohax on 01.07.2017.
 * <p>
 * for ScpFoundationRu
 */
public class DialogUtilsImpl extends DialogUtils<Article> {

    public DialogUtilsImpl(
            MyPreferenceManagerModel preferenceManager,
            DbProviderFactoryModel dbProviderFactory,
            ApiClientModel<Article> apiClient,
            ConstantValues constantValues,
            Class clazz
    ) {
        super(preferenceManager, dbProviderFactory, apiClient, constantValues, clazz);
    }

    @Override
    public List<DownloadEntry> getDownloadTypesEntries(Context context) {
        List<DownloadEntry> downloadEntries = new ArrayList<>();

        downloadEntries.add(new DownloadEntry(R.string.type_1, context.getString(R.string.type_1),mConstantValues.getObjects1(), Article.FIELD_IS_IN_OBJECTS_1));
        downloadEntries.add(new DownloadEntry(R.string.type_2, context.getString(R.string.type_2), mConstantValues.getObjects2(), Article.FIELD_IS_IN_OBJECTS_2));
        downloadEntries.add(new DownloadEntry(R.string.type_3, context.getString(R.string.type_3), mConstantValues.getObjects3(), Article.FIELD_IS_IN_OBJECTS_3));
//        downloadEntries.add(new DownloadEntry(R.string.type_4, context.getString(R.string.type_4), Constants.Urls.OBJECTS_4, Article.FIELD_IS_IN_OBJECTS_4));
        downloadEntries.add(new DownloadEntry(R.string.type_ru, context.getString(R.string.type_ru), mConstantValues.getObjectsRu(), Article.FIELD_IS_IN_OBJECTS_RU));

//        downloadEntries.add(new DownloadEntry(R.string.type_experiments, context.getString(R.string.type_experiments), Constants.Urls.EXPERIMENTS, Article.FIELD_IS_IN_EXPERIMETS));
//        downloadEntries.add(new DownloadEntry(R.string.type_incidents, context.getString(R.string.type_incidents), Constants.Urls.INCEDENTS, Article.FIELD_IS_IN_INCIDENTS));
//        downloadEntries.add(new DownloadEntry(R.string.type_interviews, context.getString(R.string.type_interviews), Constants.Urls.INTERVIEWS, Article.FIELD_IS_IN_INTERVIEWS));
//        downloadEntries.add(new DownloadEntry(R.string.type_jokes, context.getString(R.string.type_jokes), Constants.Urls.JOKES, Article.FIELD_IS_IN_JOKES));
//        downloadEntries.add(new DownloadEntry(R.string.type_archive, context.getString(R.string.type_archive), Constants.Urls.ARCHIVE, Article.FIELD_IS_IN_ARCHIVE));
//        downloadEntries.add(new DownloadEntry(R.string.type_other, context.getString(R.string.type_other), Constants.Urls.OTHERS, Article.FIELD_IS_IN_OTHER));

        downloadEntries.add(new DownloadEntry(R.string.type_all, context.getString(R.string.type_all), mConstantValues.getNewArticles(), Article.FIELD_IS_IN_RECENT));
        return downloadEntries;
    }

    @Override
    protected boolean isServiceRunning() {
        return DownloadAllServiceDefault.isRunning();
    }

    @Override
    protected void onIncreaseLimitClick(Context context) {
        Timber.d("onIncreaseLimitClick");
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, Constants.Firebase.Analitics.StartScreen.DOWNLOAD_DIALOG);
        FirebaseAnalytics.getInstance(context).logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        BottomSheetDialogFragment subsDF = SubscriptionsFragmentDialog.newInstance();
        subsDF.show(((AppCompatActivity) context).getSupportFragmentManager(), subsDF.getTag());
    }

    @Override
    protected void logDownloadAttempt(DownloadEntry type) {
        Timber.d("logDownloadAttempt: %s", type);

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, Constants.Firebase.Analitics.StartScreen.DOWNLOAD_DIALOG);
        FirebaseAnalytics.getInstance(BaseApplication.getAppInstance()).logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }
}