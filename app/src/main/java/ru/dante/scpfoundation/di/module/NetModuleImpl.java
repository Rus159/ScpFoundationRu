package ru.dante.scpfoundation.di.module;

import android.support.annotation.NonNull;

import com.google.gson.Gson;

import javax.inject.Named;

import dagger.Module;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import ru.dante.scpfoundation.api.ApiClientImpl;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.di.module.NetModule;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;

/**
 * Created by mohax on 13.07.2017.
 * <p>
 * for ScpFoundationRu
 */
@Module
public class NetModuleImpl extends NetModule {

    @Override
    protected ApiClient getApiClient(@NonNull OkHttpClient okHttpClient, @Named("vps") @NonNull Retrofit vpsRetrofit, @Named("scp") @NonNull Retrofit scpRetrofit, @NonNull MyPreferenceManager preferencesManager, @NonNull Gson gson) {
        return new ApiClientImpl(okHttpClient, vpsRetrofit, scpRetrofit, preferencesManager, gson);
    }

    //    @Provides
//    @NonNull
//    @Singleton
//    ApiClient providesApiClient(
//            @NonNull OkHttpClient okHttpClient,
//            @Named("vps") @NonNull Retrofit vpsRetrofit,
//            @Named("scp") @NonNull Retrofit scpRetrofit,
//            @NonNull MyPreferenceManager preferencesManager,
//            @NonNull Gson gson
//    ) {
//        return new ApiClientImpl(okHttpClient, vpsRetrofit, scpRetrofit, preferencesManager, gson);
//    }
}