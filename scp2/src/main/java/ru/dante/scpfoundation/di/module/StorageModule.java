package ru.dante.scpfoundation.di.module;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.google.gson.Gson;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.realm.RealmConfiguration;
import ru.dante.scpfoundation.db.DbProviderFactory;
import ru.dante.scpfoundation.manager.MyPreferenceManager;

/**
 * Created by y.kuchanov on 22.12.16.
 *
 * for scp_ru
 */
@Module
public class StorageModule {

    @Provides
    @NonNull
    @Singleton
    SharedPreferences providesSharedPreferences(@NonNull Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Provides
    @NonNull
    @Singleton
    MyPreferenceManager providesPreferencesManager(@NonNull Context context, Gson gson) {
        return new MyPreferenceManager(context, gson);
    }

    @Provides
    @NonNull
    @Singleton
    RealmConfiguration providesRealmConfiguration() {
        return new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
    }

    @Provides
    @NonNull
    @Singleton
    DbProviderFactory providesDbProviderFactory(@NonNull RealmConfiguration configuration) {
        return new DbProviderFactory(configuration);
    }
}