package ru.kuchanov.scpcore.di.module;

import android.support.annotation.NonNull;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ru.kuchanov.scpcore.ConstantValues;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.service.DownloadAllServiceDefault;
import ru.kuchanov.scpcore.ui.activity.MaterialsActivity;
import ru.kuchanov.scpcore.ui.util.DialogUtils;
import ru.kuchanov.scpcore.ui.util.DownloadAllChooserDefault;
import ru.kuchanov.scpcore.ui.util.MaterialClickListenerDefault;
import ru.kuchanov.scpcore.ui.util.SetTextViewHTML;

/**
 * Created by y.kuchanov on 22.12.16.
 * <p>
 * for scp_ru
 */
@Module
public class HelpersModule {

    @Provides
    @NonNull
    @Singleton
    DialogUtils providesDialogUtils(
            @NonNull MyPreferenceManager preferenceManager,
            @NonNull DbProviderFactory dbProviderFactory,
            @NonNull ApiClient apiClient
    ) {
        return new DialogUtils(preferenceManager, dbProviderFactory, apiClient);
    }

    @Provides
    @NonNull
    @Singleton
    ru.kuchanov.scp.downloads.DialogUtils<Article> providesDialogUtilsTest(
            @NonNull MyPreferenceManager preferenceManager,
            @NonNull DbProviderFactory dbProviderFactory,
            @NonNull ApiClient apiClient
    ) {
        return getDialogUtilsTest(preferenceManager, dbProviderFactory, apiClient);
    }

    protected ru.kuchanov.scp.downloads.DialogUtils<Article> getDialogUtilsTest(
            @NonNull MyPreferenceManager preferenceManager,
            @NonNull DbProviderFactory dbProviderFactory,
            @NonNull ApiClient apiClient
    ) {
        return new DownloadAllChooserDefault(preferenceManager, dbProviderFactory, apiClient, DownloadAllServiceDefault.class);
    }

    @Provides
    @NonNull
    @Singleton
    SetTextViewHTML providesSetTextViewHTML(@NonNull ConstantValues constantValues) {
        return new SetTextViewHTML(constantValues);
    }

    @Provides
    @NonNull
    @Singleton
    MaterialsActivity.MaterialClickListener providesMaterialClickListener(@NonNull ConstantValues constantValues) {
        return getMaterialClickListenerImpl(constantValues);
    }

    protected MaterialsActivity.MaterialClickListener getMaterialClickListenerImpl(@NonNull ConstantValues constantValues) {
        return new MaterialClickListenerDefault(constantValues);
    }
}