package ru.kuchanov.scpcore;

import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKAccessTokenTracker;
import com.vk.sdk.VKSdk;
import com.yandex.metrica.YandexMetrica;

import io.realm.Realm;
import ru.kuchanov.scpcore.di.AppComponent;
import ru.kuchanov.scpcore.util.SystemUtils;
import timber.log.Timber;

/**
 * Created by mohax on 01.01.2017.
 * <p>
 * for scp_ru
 */
public abstract class BaseApplication extends MultiDexApplication {

    private static AppComponent sAppComponent;
    private static BaseApplication sAppInstance;

    public static AppComponent getAppComponent() {
        return sAppComponent;
    }

    public static BaseApplication getAppInstance() {
        return sAppInstance;
    }

    public RefWatcher getRefWatcher() {
        return refWatcher;
    }

    private RefWatcher refWatcher;

    VKAccessTokenTracker vkAccessTokenTracker = new VKAccessTokenTracker() {
        @Override
        public void onVKAccessTokenChanged(VKAccessToken oldToken, VKAccessToken newToken) {
            if (newToken == null) {
                //VKAccessToken is invalid
                //TODO
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        refWatcher = LeakCanary.install(this);
        // Инициализация AppMetrica SDK
        YandexMetrica.activate(getApplicationContext(), getString(R.string.yandex_metrica_api_key));
        // Отслеживание активности пользователей
        YandexMetrica.enableActivityAutoTracking(this);

        sAppInstance = this;
//        sAppComponent = DaggerAppComponent.builder()
//                .storageModule(new StorageModule())
//                .appModule(new AppModule(this))
//                .presentersModule(new PresentersModule())
//                .helpersModule(getHelpersModule())
//                .build();

        sAppComponent = buildAppComponentImpl();

        if (BuildConfig.TIMBER_ENABLE) {
            Timber.plant(new Timber.DebugTree() {
                @Override
                protected void log(int priority, String tag, String message, Throwable t) {
                    message = formatLogs(message);
                    super.log(priority, tag, message, t);
                }

                private String formatLogs(String message) {
                    if (!message.startsWith("{")) {
                        return message;
                    }
                    try {
                        return new GsonBuilder().setPrettyPrinting().create().toJson(new JsonParser().parse(message));
                    } catch (JsonSyntaxException m) {
                        return message;
                    }
                }
            });
        } else {
            Timber.plant(new Timber.DebugTree() {
                @Override
                protected void log(int priority, String tag, String message, Throwable t) {
                    if (priority == Log.ERROR) {
                        //maybe send error via some service, i.e. firebase or googleAnalitics
                        super.log(priority, tag, message, t);
                    }
                }
            });
        }

        vkAccessTokenTracker.startTracking();
        VKSdk.initialize(this);

        SystemUtils.printCertificateFingerprints();

        Realm.init(this);

        //print versionCode
        Timber.d("VERSION_CODE: %s", BuildConfig.VERSION_CODE);

//        //secure
//        if (SecureUtils.checkCrack(this)) {
//            MyPreferenceManager myPreferenceManager = new MyPreferenceManager(this, null);
//            myPreferenceManager.setAppCracked(true);
//        }
    }

    protected abstract AppComponent buildAppComponentImpl();
}