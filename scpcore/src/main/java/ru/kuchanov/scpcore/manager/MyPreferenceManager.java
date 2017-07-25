package ru.kuchanov.scpcore.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;

import java.util.ArrayList;

import ru.kuchanov.scp.downloads.MyPreferenceManagerModel;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.monetization.model.ApplicationsResponse;
import ru.kuchanov.scpcore.monetization.model.PlayMarketApplication;
import ru.kuchanov.scpcore.monetization.model.VkGroupToJoin;
import ru.kuchanov.scpcore.monetization.model.VkGroupsToJoinResponse;
import ru.kuchanov.scpcore.ui.dialog.SetttingsBottomSheetDialogFragment;
import timber.log.Timber;

/**
 * Created by y.kuchanov on 22.12.16.
 * <p>
 * for scp_ru
 */
public class MyPreferenceManager implements MyPreferenceManagerModel {

    /**
     * check if user joined app vk group each 2 hours
     */
    private static final long PERIOD_BETWEEN_APP_VK_GROUP_JOINED_CHECK_IN_MILLIS = 1000 * 60 * 60 * 2;
//    private static final long PERIOD_BETWEEN_NEED_RELOGIN_POPUP_IN_MILLIS = 1000 * 60 * 5;

    public interface Keys {
        String NIGHT_MODE = "NIGHT_MODE";
        String TEXT_SCALE_UI = "TEXT_SCALE_UI";
        String TEXT_SCALE_ARTICLE = "TEXT_SCALE_ARTICLE";
        String DESIGN_LIST_TYPE = "DESIGN_LIST_TYPE";

        String NOTIFICATION_IS_ON = "NOTIFICATION_IS_ON";
        String NOTIFICATION_PERIOD = "NOTIFICATION_PERIOD";
        String NOTIFICATION_VIBRATION_IS_ON = "NOTIFICATION_VIBRATION_IS_ON";
        String NOTIFICATION_LED_IS_ON = "NOTIFICATION_LED_IS_ON";
        String NOTIFICATION_SOUND_IS_ON = "NOTIFICATION_SOUND_IS_ON";

        String ADS_LAST_TIME_SHOWS = "ADS_LAST_TIME_SHOWS";
        String ADS_REWARDED_DESCRIPTION_IS_SHOWN = "ADS_REWARDED_DESCRIPTION_IS_SHOWN";
        String ADS_NUM_OF_INTERSTITIALS_SHOWN = "ADS_NUM_OF_INTERSTITIALS_SHOWN";

        String LICENCE_ACCEPTED = "LICENCE_ACCEPTED";
        String CUR_APP_VERSION = "CUR_APP_VERSION";
        String DESIGN_FONT_PATH = "DESIGN_FONT_PATH";
        String PACKAGE_INSTALLED = "PACKAGE_INSTALLED";
        String VK_GROUP_JOINED = "VK_GROUP_JOINED";
        //        String USER_UID = "USER_UID";
        String HAS_SUBSCRIPTION = "HAS_SUBSCRIPTION";
        String HAS_NO_ADS_SUBSCRIPTION = "HAS_NO_ADS_SUBSCRIPTION";
        String APP_IS_CRACKED = "APP_IS_CRACKED";
        String AUTO_SYNC_ATTEMPTS = "AUTO_SYNC_ATTEMPTS";
        //        String VK_GROUP_APP_JOINED = "VK_GROUP_APP_JOINED";
        String UNSYNCED_SCORE = "UNSYNCED_SCORE";
        String UNSYNCED_VK_GROUPS = "UNSYNCED_VK_GROUPS";
        String UNSYNCED_APPS = "UNSYNCED_APPS";
        //        String HAS_LEVEL_UP_INAPP = "HAS_LEVEL_UP_INAPP";
        String APP_VK_GROUP_JOINED_LAST_TIME_CHECKED = "APP_VK_GROUP_JOINED_LAST_TIME_CHECKED";
        String APP_VK_GROUP_JOINED = "APP_VK_GROUP_JOINED";
        String DATA_RESTORED = "DATA_RESTORED";
//        String NEED_RELOGIN_POPUP_LAST_TIME_CHECKED = "NEED_RELOGIN_POPUP_LAST_TIME_CHECKED";
    }

    private Gson mGson;

    private SharedPreferences mPreferences;

    public MyPreferenceManager(Context context, Gson gson) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mGson = gson;
    }

    public void setIsNightMode(boolean isInNightMode) {
        mPreferences.edit().putBoolean(Keys.NIGHT_MODE, isInNightMode).apply();
    }

    public boolean isNightMode() {
        return mPreferences.getBoolean(Keys.NIGHT_MODE, false);
    }

    public void setUiTextScale(float uiTextScale) {
        mPreferences.edit().putFloat(Keys.TEXT_SCALE_UI, uiTextScale).apply();
    }

    public float getUiTextScale() {
        return mPreferences.getFloat(Keys.TEXT_SCALE_UI, .75f);
    }

    public float getArticleTextScale() {
        return mPreferences.getFloat(Keys.TEXT_SCALE_ARTICLE, .75f);
    }

    public void setArticleTextScale(float textScale) {
        mPreferences.edit().putFloat(Keys.TEXT_SCALE_ARTICLE, textScale).apply();
    }

    //design settings
    public boolean isDesignListNewEnabled() {
        return !mPreferences.getString(Keys.DESIGN_LIST_TYPE, SetttingsBottomSheetDialogFragment.ListItemType.MIDDLE).equals(SetttingsBottomSheetDialogFragment.ListItemType.MIN);
    }

    public void setListDesignType(@SetttingsBottomSheetDialogFragment.ListItemType String type) {
        mPreferences.edit().putString(Keys.DESIGN_LIST_TYPE, type).apply();
    }

    @SetttingsBottomSheetDialogFragment.ListItemType
    public String getListDesignType() {
        @SetttingsBottomSheetDialogFragment.ListItemType
        String type = mPreferences.getString(Keys.DESIGN_LIST_TYPE, SetttingsBottomSheetDialogFragment.ListItemType.MIDDLE);
        return type;
    }

    public void setFontPath(String type) {
        mPreferences.edit().putString(Keys.DESIGN_FONT_PATH, type).apply();
    }

    public String getFontPath() {
        return mPreferences.getString(Keys.DESIGN_FONT_PATH, "fonts/Roboto-Regular.ttf");
    }

    //new arts notifications
    int getNotificationPeriodInMinutes() {
        return mPreferences.getInt(Keys.NOTIFICATION_PERIOD, 60);
    }

//    public void setNotificationPeriodInMinutes(int minutes) {
//        mPreferences.edit().putInt(Keys.NOTIFICATION_PERIOD, minutes).apply();
//    }

    public boolean isNotificationEnabled() {
        return mPreferences.getBoolean(Keys.NOTIFICATION_IS_ON, true);
    }

    public void setNotificationEnabled(boolean enabled) {
        mPreferences.edit().putBoolean(Keys.NOTIFICATION_IS_ON, enabled).apply();
    }

    public boolean isNotificationVibrationEnabled() {
        return mPreferences.getBoolean(Keys.NOTIFICATION_VIBRATION_IS_ON, false);
    }

    public void setNotificationVibrationEnabled(boolean enabled) {
        mPreferences.edit().putBoolean(Keys.NOTIFICATION_VIBRATION_IS_ON, enabled).apply();
    }

    public boolean isNotificationLedEnabled() {
        return mPreferences.getBoolean(Keys.NOTIFICATION_LED_IS_ON, false);
    }

    public void setNotificationLedEnabled(boolean enabled) {
        mPreferences.edit().putBoolean(Keys.NOTIFICATION_LED_IS_ON, enabled).apply();
    }

    public boolean isNotificationSoundEnabled() {
        return mPreferences.getBoolean(Keys.NOTIFICATION_SOUND_IS_ON, false);
    }

    public void setNotificationSoundEnabled(boolean enabled) {
        mPreferences.edit().putBoolean(Keys.NOTIFICATION_SOUND_IS_ON, enabled).apply();
    }

    //ads
    public boolean isTimeToShowAds() {
        return System.currentTimeMillis() - getLastTimeAdsShows() >=
                FirebaseRemoteConfig.getInstance().getLong(Constants.Firebase.RemoteConfigKeys.PERIOD_BETWEEN_INTERSTITIAL_IN_MILLIS);
    }

    public void applyRewardFromAds() {
        setLastTimeAdsShows(System.currentTimeMillis() +
                FirebaseRemoteConfig.getInstance().getLong(Constants.Firebase.RemoteConfigKeys.REWARDED_VIDEO_COOLDOWN_IN_MILLIS));
    }

    public boolean isRewardedDescriptionShown() {
        return mPreferences.getBoolean(Keys.ADS_REWARDED_DESCRIPTION_IS_SHOWN, false);
    }

    public void setRewardedDescriptionIsNotShown(boolean isShown) {
        mPreferences.edit().putBoolean(Keys.ADS_REWARDED_DESCRIPTION_IS_SHOWN, isShown).apply();
    }

    public void setLastTimeAdsShows(long timeInMillis) {
        mPreferences.edit().putLong(Keys.ADS_LAST_TIME_SHOWS, timeInMillis).apply();
    }

    private long getLastTimeAdsShows() {
        long timeFromLastShow = mPreferences.getLong(Keys.ADS_LAST_TIME_SHOWS, 0);
        if (timeFromLastShow == 0) {
            setLastTimeAdsShows(System.currentTimeMillis());
        }
        return timeFromLastShow;
    }

    public void setNumOfInterstitialsShown(int numOfInterstitialsShown) {
        mPreferences.edit().putInt(Keys.ADS_NUM_OF_INTERSTITIALS_SHOWN, numOfInterstitialsShown).apply();
    }

    public int getNumOfInterstitialsShown() {
        return mPreferences.getInt(Keys.ADS_NUM_OF_INTERSTITIALS_SHOWN, 0);
    }

    public boolean isTimeToShowVideoInsteadOfInterstitial() {
        return getNumOfInterstitialsShown() >=
                FirebaseRemoteConfig.getInstance().getLong(Constants.Firebase.RemoteConfigKeys.NUM_OF_INTERSITIAL_BETWEEN_REWARDED);
    }

    //app installs
    public boolean isAppInstalledForPackage(String packageName) {
        return mPreferences.getBoolean(Keys.PACKAGE_INSTALLED + packageName, false);
    }

    public void setAppInstalledForPackage(String packageName) {
        mPreferences.edit().putBoolean(Keys.PACKAGE_INSTALLED + packageName, true).apply();
    }

    public void applyAwardForAppInstall() {
        setLastTimeAdsShows((System.currentTimeMillis() +
                FirebaseRemoteConfig.getInstance().getLong(Constants.Firebase.RemoteConfigKeys.APP_INSTALL_REWARD_IN_MILLIS)));
    }

    //vk groups join
    public boolean isVkGroupJoined(String id) {
        return mPreferences.getBoolean(Keys.VK_GROUP_JOINED + id, false);
    }

    public void setVkGroupJoined(String id) {
        mPreferences.edit().putBoolean(Keys.VK_GROUP_JOINED + id, true).apply();
        if (id.equals(FirebaseRemoteConfig.getInstance().getString(Constants.Firebase.RemoteConfigKeys.VK_APP_GROUP_ID))) {
            setAppVkGroupJoined(true);
        }
    }

    public boolean isAppVkGroupJoined() {
        return mPreferences.getBoolean(Keys.APP_VK_GROUP_JOINED, false);
    }

    public void setAppVkGroupJoined(boolean joined) {
        mPreferences.edit().putBoolean(Keys.APP_VK_GROUP_JOINED, joined).apply();
    }

    public void applyAwardVkGroupJoined() {
        setLastTimeAdsShows((System.currentTimeMillis() +
                FirebaseRemoteConfig.getInstance().getLong(Constants.Firebase.RemoteConfigKeys.FREE_VK_GROUPS_JOIN_REWARD)));
    }

    //subscription
    public void setHasSubscription(boolean hasSubscription) {
        mPreferences.edit().putBoolean(Keys.HAS_SUBSCRIPTION, hasSubscription).apply();
    }

    public boolean isHasSubscription() {
        return mPreferences.getBoolean(Keys.HAS_SUBSCRIPTION, false);
////       FIX ME test
//        return true;
    }

    /**
     * its a subscription that only removes ads
     */
    public void setHasNoAdsSubscription(boolean hasSubscription) {
        mPreferences.edit().putBoolean(Keys.HAS_NO_ADS_SUBSCRIPTION, hasSubscription).apply();
    }

    /**
     * its a subscription that only removes ads
     */
    public boolean isHasNoAdsSubscription() {
        return mPreferences.getBoolean(Keys.HAS_NO_ADS_SUBSCRIPTION, false);
    }
    //subscriptions end

    @Override
    public boolean isDownloadAllEnabledForFree() {
        return FirebaseRemoteConfig.getInstance().getBoolean(Constants.Firebase.RemoteConfigKeys.DOWNLOAD_ALL_ENABLED_FOR_FREE);
    }

    @Override
    public int getScorePerArt() {
        return (int) FirebaseRemoteConfig.getInstance().getLong(Constants.Firebase.RemoteConfigKeys.DOWNLOAD_SCORE_PER_ARTICLE);
    }

    @Override
    public int getFreeOfflineLimit() {
        return (int) FirebaseRemoteConfig.getInstance().getLong(Constants.Firebase.RemoteConfigKeys.DOWNLOAD_FREE_ARTICLES_LIMIT);
    }

    //auto sync
    public void setNumOfAttemptsToAutoSync(long numOfAttemptsToAutoSync) {
        mPreferences.edit().putLong(Keys.AUTO_SYNC_ATTEMPTS, numOfAttemptsToAutoSync).apply();
    }

    public long getNumOfAttemptsToAutoSync() {
        return mPreferences.getLong(Keys.AUTO_SYNC_ATTEMPTS, 0);
    }

    public void addUnsyncedScore(int scoreToAdd) {
        int newTotalScore = getNumOfUnsyncedScore() + scoreToAdd;
        mPreferences.edit().putInt(Keys.UNSYNCED_SCORE, newTotalScore).apply();
    }

    public void addUnsyncedVkGroup(String id) {
        VkGroupsToJoinResponse data = getUnsyncedVkGroupsJson();
        if (data == null) {
            data = new VkGroupsToJoinResponse();
            data.items = new ArrayList<>();
        }
        VkGroupToJoin item = new VkGroupToJoin(id);
        if (!data.items.contains(item)) {
            data.items.add(item);
            mPreferences.edit().putString(Keys.UNSYNCED_VK_GROUPS, mGson.toJson(data)).apply();
        }
    }

    public void deleteUnsyncedVkGroups() {
        mPreferences.edit().remove(Keys.UNSYNCED_VK_GROUPS).apply();
    }

    public VkGroupsToJoinResponse getUnsyncedVkGroupsJson() {
        VkGroupsToJoinResponse data = null;
        try {
            data = mGson.fromJson(mPreferences.getString(Keys.UNSYNCED_VK_GROUPS, null), VkGroupsToJoinResponse.class);
        } catch (Exception e) {
            Timber.e(e);
        }
        return data;
    }

    public void addUnsyncedApp(String id) {
        ApplicationsResponse data = getUnsyncedAppsJson();
        if (data == null) {
            data = new ApplicationsResponse();
            data.items = new ArrayList<>();
        }
        PlayMarketApplication item = new PlayMarketApplication(id);
        if (!data.items.contains(item)) {
            data.items.add(item);
            mPreferences.edit().putString(Keys.UNSYNCED_APPS, mGson.toJson(data)).apply();
        }
    }

    public void deleteUnsyncedApps() {
        mPreferences.edit().remove(Keys.UNSYNCED_APPS).apply();
    }

    public ApplicationsResponse getUnsyncedAppsJson() {
        ApplicationsResponse data = null;
        try {
            data = mGson.fromJson(mPreferences.getString(Keys.UNSYNCED_APPS, null), ApplicationsResponse.class);
        } catch (Exception e) {
            Timber.e(e);
        }
        return data;
    }

    public void setNumOfUnsyncedScore(int totalScore) {
        mPreferences.edit().putInt(Keys.UNSYNCED_SCORE, totalScore).apply();
    }

    public int getNumOfUnsyncedScore() {
        return mPreferences.getInt(Keys.UNSYNCED_SCORE, 0);
    }

    public void setLastTimeAppVkGroupJoinedChecked(long timeInMillis) {
        mPreferences.edit().putLong(Keys.APP_VK_GROUP_JOINED_LAST_TIME_CHECKED, timeInMillis).apply();
    }

    private long getLastTimeAppVkGroupJoinedChecked() {
        long timeFromLastShow = mPreferences.getLong(Keys.APP_VK_GROUP_JOINED_LAST_TIME_CHECKED, 0);
        if (timeFromLastShow == 0) {
            setLastTimeAdsShows(System.currentTimeMillis());
        }
        return timeFromLastShow;
    }

    public boolean isTimeToCheckAppVkGroupJoined() {
        return System.currentTimeMillis() - getLastTimeAppVkGroupJoinedChecked() >= PERIOD_BETWEEN_APP_VK_GROUP_JOINED_CHECK_IN_MILLIS;
    }

//    public void setLastTimeNeedReloginPopupShown(long timeInMillis) {
//        mPreferences.edit().putLong(Keys.NEED_RELOGIN_POPUP_LAST_TIME_CHECKED, timeInMillis).apply();
//    }
//
//    private long getLastTimeNeedReloginPopupShown() {
//        long timeFromLastShow = mPreferences.getLong(Keys.NEED_RELOGIN_POPUP_LAST_TIME_CHECKED, 0);
//        if (timeFromLastShow == 0) {
//            long curTime = System.currentTimeMillis();
//            setLastTimeAdsShows(curTime);
//            timeFromLastShow = curTime;
//        }
//        return timeFromLastShow;
//    }
//
//    public boolean isTimeToShowNeedReloginPopup() {
//        return System.currentTimeMillis() - getLastTimeNeedReloginPopupShown() >= PERIOD_BETWEEN_NEED_RELOGIN_POPUP_IN_MILLIS;
//    }

    // secure
    public boolean isAppCracked() {
        return mPreferences.getBoolean(Keys.APP_IS_CRACKED, false);
    }

    public void setAppCracked(boolean cracked) {
        mPreferences.edit().putBoolean(Keys.APP_IS_CRACKED, cracked).apply();
    }

    //utils
    public boolean isLicenceAccepted() {
        return mPreferences.getBoolean(Keys.LICENCE_ACCEPTED, false);
    }

    public void setLicenceAccepted(boolean accepted) {
        mPreferences.edit().putBoolean(Keys.LICENCE_ACCEPTED, accepted).apply();
    }

    public boolean isDataRestored() {
        return mPreferences.getBoolean(Keys.DATA_RESTORED, false);
    }

    public void setDataIsRestored(boolean restored) {
        mPreferences.edit().putBoolean(Keys.DATA_RESTORED, restored).apply();
    }

    public int getCurAppVersion() {
        return mPreferences.getInt(Keys.CUR_APP_VERSION, 0);
    }

    public void setCurAppVersion(int versionCode) {
        mPreferences.edit().putInt(Keys.CUR_APP_VERSION, versionCode).apply();
    }
}