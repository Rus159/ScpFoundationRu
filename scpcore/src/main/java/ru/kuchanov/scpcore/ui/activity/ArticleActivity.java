package ru.kuchanov.scpcore.ui.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.List;

import butterknife.BindView;
import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.BuildConfig;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.R2;
import ru.kuchanov.scpcore.mvp.contract.ArticleScreenMvp;
import ru.kuchanov.scpcore.mvp.contract.DataSyncActions;
import ru.kuchanov.scpcore.ui.adapter.ArticlesPagerAdapter;
import ru.kuchanov.scpcore.ui.base.BaseDrawerActivity;
import ru.kuchanov.scpcore.ui.dialog.TextSizeDialogFragment;
import ru.kuchanov.scpcore.ui.fragment.ArticleFragment;
import ru.kuchanov.scpcore.util.IntentUtils;
import ru.kuchanov.scpcore.util.SystemUtils;
import timber.log.Timber;

import static ru.kuchanov.scpcore.Constants.Firebase.RemoteConfigKeys.ARTICLE_BANNER_DISABLED;
import static ru.kuchanov.scpcore.ui.activity.MainActivity.EXTRA_SHOW_DISABLE_ADS;

public class ArticleActivity
        extends BaseDrawerActivity<ArticleScreenMvp.View, ArticleScreenMvp.Presenter>
        implements ArticleScreenMvp.View, ArticleFragment.ToolbarStateSetter {

    @BindView(R2.id.content)
    ViewPager mViewPager;

    @BindView(R2.id.banner)
    AdView mAdView;

    private int mCurPosition;
    private List<String> mUrls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().hasExtra(EXTRA_ARTICLES_URLS_LIST)) {
            mUrls = getIntent().getStringArrayListExtra(EXTRA_ARTICLES_URLS_LIST);
            mCurPosition = getIntent().getIntExtra(EXTRA_POSITION, 0);
        }
        ArticlesPagerAdapter adapter = new ArticlesPagerAdapter(getSupportFragmentManager());
        adapter.setData(getIntent().getStringArrayListExtra(EXTRA_ARTICLES_URLS_LIST));
        mViewPager.setAdapter(adapter);

        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mCurPosition = position;
                if (isTimeToShowAds()) {
                    if (isAdsLoaded()) {
                        showInterstitial();
                    } else {
                        requestNewInterstitial();
                    }
                }
            }
        });

        mViewPager.setCurrentItem(mCurPosition);

        if (getIntent().hasExtra(EXTRA_SHOW_DISABLE_ADS)) {
            showSnackBarWithAction(Constants.Firebase.CallToActionReason.REMOVE_ADS);
            getIntent().removeExtra(EXTRA_SHOW_DISABLE_ADS);

            @DataSyncActions.ScoreAction
            String action = DataSyncActions.ScoreAction.INTERSTITIAL_SHOWN;
            mPresenter.updateUserScoreForScoreAction(action);
        }
    }

    @Override
    public void initAds() {
        super.initAds();

        if (!isAdsLoaded()) {
            requestNewInterstitial();
        }

        AdRequest.Builder adRequest = new AdRequest.Builder();

        if (BuildConfig.DEBUG) {
            @SuppressLint("HardwareIds")
            String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            String deviceId;
            deviceId = SystemUtils.MD5(androidId);
            if (deviceId != null) {
                deviceId = deviceId.toUpperCase();
                adRequest.addTestDevice(deviceId);
            }
            adRequest.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
        }
        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
        if (mMyPreferenceManager.isHasSubscription()
                || mMyPreferenceManager.isHasNoAdsSubscription()
                || remoteConfig.getBoolean(ARTICLE_BANNER_DISABLED)) {
            mAdView.setVisibility(View.GONE);
        } else {
            mAdView.setVisibility(View.VISIBLE);
            mAdView.loadAd(adRequest.build());
        }
    }

    @Override
    protected boolean isDrawerIndicatorEnabled() {
        return false;
    }

    @Override
    protected int getDefaultNavItemId() {
        return SELECTED_DRAWER_ITEM_NONE;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_article;
    }

    @Override
    protected int getMenuResId() {
        return R.menu.menu_article;
    }

    @Override
    public boolean onNavigationItemClicked(int id) {
        Timber.d("onNavigationItemClicked with id: %s", id);
        String link = null;
        if (id == R.id.about) {
            link = mConstantValues.getUrlsValues().getAbout();
        } else if (id == R.id.news) {
            link = mConstantValues.getUrlsValues().getNews();
        } else if (id == R.id.mostRatedArticles) {
            link = mConstantValues.getUrlsValues().getMostRated();
        } else if (id == R.id.mostRecentArticles) {
            link = mConstantValues.getUrlsValues().getNewArticles();
        } else if (id == R.id.random_page) {
            mPresenter.getRandomArticleUrl();
        } else if (id == R.id.objects_I) {
            link = mConstantValues.getUrlsValues().getObjects1();
        } else if (id == R.id.objects_II) {
            link = mConstantValues.getUrlsValues().getObjects2();
        } else if (id == R.id.objects_III) {
            link = mConstantValues.getUrlsValues().getObjects3();
        } else if (id == R.id.objects_IV) {
            link = mConstantValues.getUrlsValues().getObjects4();
        } else if (id == R.id.files) {
            startMaterialsActivity();
        } else if (id == R.id.favorite) {
            link = Constants.Urls.FAVORITES;
        } else if (id == R.id.offline) {
            link = Constants.Urls.OFFLINE;
        } else if (id == R.id.gallery) {
            startGalleryActivity();
        } else if (id == R.id.siteSearch) {
            link = Constants.Urls.SEARCH;
        } else if (id == R.id.tagsSearch) {
            startTagsSearchActivity();
            return true;
        } else if (id == R2.id.objects_RU) {
            link = mConstantValues.getUrlsValues().getObjectsRu();
        } else if (id == R2.id.stories) {
            link = Constants.Urls.STORIES;
        } else {
            Timber.e("unexpected item ID");
        }
        if (link != null) {
            MainActivity.startActivity(this, link);
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Timber.d("onOptionsItemSelected with id: %s", item);
        int i = item.getItemId();
        if (i == R.id.menuItemShare) {
            IntentUtils.shareUrl(mUrls.get(mCurPosition));
            return true;
        } else if (i == R.id.menuItemBrowser) {
            IntentUtils.openUrl(mUrls.get(mCurPosition));
            return true;
        } else if (i == R.id.menuItemFavorite) {
            mPresenter.toggleFavorite(mUrls.get(mCurPosition));
            return true;
        } else if (i == R.id.text_size) {
            BottomSheetDialogFragment fragmentDialogTextAppearance = TextSizeDialogFragment.newInstance(TextSizeDialogFragment.TextSizeType.ARTICLE);
            fragmentDialogTextAppearance.show(getSupportFragmentManager(), TextSizeDialogFragment.TAG);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void setTitle(String title) {
        if (mToolbar != null) {
            mToolbar.setTitle(title);
        }
    }

    @Override
    public void setFavoriteState(boolean isInFavorite) {
//        Timber.d("setFavoriteState: %s", isInFavorite);
        if (mToolbar != null && mToolbar.getMenu() != null) {
            MenuItem item = mToolbar.getMenu().findItem(R.id.menuItemFavorite);
            if (item != null) {
                item.setIcon(isInFavorite ? R.drawable.ic_favorite_white_24dp : R.drawable.ic_favorite_border_white_24dp);
                item.setTitle(isInFavorite ? R.string.favorites_remove : R.string.favorites_add);
            }
        }
    }

    @Override
    protected void callInjections() {
        BaseApplication.getAppComponent().inject(this);
    }
}