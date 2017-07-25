package ru.kuchanov.scpcore.ui.base;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.android.gms.auth.api.Auth;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;

import javax.inject.Inject;

import butterknife.BindView;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.R2;
import ru.kuchanov.scpcore.api.model.remoteconfig.LevelsJson;
import ru.kuchanov.scpcore.api.model.response.LeaderBoardResponse;
import ru.kuchanov.scpcore.db.model.User;
import ru.kuchanov.scpcore.monetization.model.PurchaseData;
import ru.kuchanov.scpcore.monetization.util.InappHelper;
import ru.kuchanov.scpcore.mvp.contract.DrawerMvp;
import ru.kuchanov.scpcore.ui.dialog.LeaderboardDialogFragment;
import ru.kuchanov.scpcore.ui.dialog.SubscriptionsFragmentDialog;
import ru.kuchanov.scpcore.ui.holder.HeaderViewHolderLogined;
import ru.kuchanov.scpcore.ui.holder.HeaderViewHolderUnlogined;
import ru.kuchanov.scpcore.util.SecureUtils;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by mohax on 02.01.2017.
 * <p>
 * for scp_ru
 */
public abstract class BaseDrawerActivity<V extends DrawerMvp.View, P extends DrawerMvp.Presenter<V>>
        extends BaseActivity<V, P>
        implements DrawerMvp.View, SharedPreferences.OnSharedPreferenceChangeListener {

    public static final int REQUEST_CODE_INAPP = 1421;

    private static final String STATE_CUR_DRAWER_ITEM_ID = "STATE_CUR_DRAWER_ITEM_ID";
    protected static final int SELECTED_DRAWER_ITEM_NONE = -1;

    @Inject
    Gson mGson;

    @BindView(R2.id.root)
    protected DrawerLayout mDrawerLayout;
    @BindView(R2.id.navigationView)
    protected NavigationView mNavigationView;

    protected MaterialDialog dialog;

    protected ActionBarDrawerToggle mDrawerToggle;

    protected int mCurrentSelectedDrawerItemId;

    protected abstract int getDefaultNavItemId();

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_CUR_DRAWER_ITEM_ID, mCurrentSelectedDrawerItemId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCurrentSelectedDrawerItemId = getDefaultNavItemId();

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);

            actionBar.setDisplayHomeAsUpEnabled(true);

            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.app_name, R.string.app_name) {
                public void onDrawerClosed(View view) {
                    supportInvalidateOptionsMenu();
                }

                public void onDrawerOpened(View drawerView) {
                }
            };
            mDrawerToggle.setDrawerIndicatorEnabled(isDrawerIndicatorEnabled());

            mDrawerLayout.addDrawerListener(mDrawerToggle);
        }

        mNavigationView.setNavigationItemSelectedListener(item -> {
            mPresenter.onNavigationItemClicked(item.getItemId());
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return onNavigationItemClicked(item.getItemId());
        });

        if (savedInstanceState != null) {
            mCurrentSelectedDrawerItemId = savedInstanceState.getInt(STATE_CUR_DRAWER_ITEM_ID);
        }
        if (mCurrentSelectedDrawerItemId != SELECTED_DRAWER_ITEM_NONE) {
            mNavigationView.setCheckedItem(mCurrentSelectedDrawerItemId);
        } else {
            mNavigationView.getMenu().setGroupCheckable(0, false, true);
        }

        updateUser(mPresenter.getUser());
    }

    /**
     * @return true if need to show hamburger. False if want show arrow
     */
    protected abstract boolean isDrawerIndicatorEnabled();

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Timber.d("onOptionsItemSelected with id: %s", item);
        switch (item.getItemId()) {
            case android.R.id.home:
                if (isDrawerIndicatorEnabled()) {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                } else {
                    finish();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onReceiveRandomUrl(String url) {
        startArticleActivity(url);
    }

    @Override
    public void showProgressDialog(boolean show) {
        if (show) {
            MaterialDialog.Builder builder = new MaterialDialog.Builder(this);
            builder.title(R.string.dialog_random_page_title);
            builder.content(R.string.dialog_random_page_message);
            builder.progress(true, 0);
            builder.cancelable(false);
            dialog = builder.build();
            dialog.show();
        } else if (dialog != null) {
            dialog.dismiss();
        }
    }

    @Override
    public void updateUser(User user) {
        Timber.d("updateUser: %s", user);
        if (user != null) {
            for (int i = 0; i < mNavigationView.getHeaderCount(); i++) {
                mNavigationView.removeHeaderView(mNavigationView.getHeaderView(i));
            }
            View headerLogined = LayoutInflater.from(this).inflate(R.layout.drawer_header_logined, mNavigationView, false);
            mNavigationView.addHeaderView(headerLogined);

            HeaderViewHolderLogined headerViewHolder = new HeaderViewHolderLogined(headerLogined);

            headerViewHolder.logout.setOnClickListener(view -> new MaterialDialog.Builder(BaseDrawerActivity.this)
                    .title(R.string.warning)
                    .content(R.string.dialog_logout_content)
                    .negativeText(R.string.close)
                    .onNegative((dialog, which) -> dialog.dismiss())
                    .positiveText(R.string.logout)
                    .onPositive((dialog, which) -> {
                        dialog.dismiss();
                        //logout from google, then logout from other
                        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(status -> mPresenter.logoutUser());
                    })
                    .show()
            );

//            if (VKSdk.isLoggedIn() && FirebaseAuth.getInstance().getCurrentUser() == null) {
//                headerViewHolder.relogin.setVisibility(View.VISIBLE);
//                headerViewHolder.relogin.setOnClickListener(view -> showNeedReloginPopup());
//            } else {
                headerViewHolder.relogin.setVisibility(View.GONE);
//            }

            headerViewHolder.levelUp.setOnClickListener(view -> InappHelper.getInappsListToBuyObserveble(getIInAppBillingService()).subscribe(
                    items -> new MaterialDialog.Builder(view.getContext())
                            .title(R.string.dialog_level_up_title)
                            .content(R.string.dialog_level_up_content)
                            .neutralText(android.R.string.cancel)
                            .positiveText(R.string.dialog_level_up_ok_text)
                            .onPositive((dialog1, which) -> {
                                Timber.d("onPositive");
                                try {
                                    Bundle buyIntentBundle = getIInAppBillingService().getBuyIntent(
                                            3,
                                            getPackageName(),
                                            items.get(0).productId,
                                            "inapp",
                                            String.valueOf(System.currentTimeMillis()));
                                    PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                                    for (String key : buyIntentBundle.keySet()) {
                                        Timber.d("%s: %s", key, buyIntentBundle.get(key));
                                    }
                                    if (pendingIntent != null) {
                                        Timber.d("startIntentSenderForResult");
                                        startIntentSenderForResult(pendingIntent.getIntentSender(), REQUEST_CODE_INAPP, new Intent(), 0, 0, 0, null);
                                    } else {
                                        Timber.e("pendingIntent is NULL!");
                                        InappHelper.getOwnedInappsObserveble(getIInAppBillingService())
                                                .flatMap(itemsOwned -> InappHelper.consumeInapp(itemsOwned.get(0).purchaseData.purchaseToken, getIInAppBillingService()))
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(
                                                        result -> Timber.d("consumed result: %s", result),
                                                        Timber::e
                                                );
                                    }
                                } catch (Exception e) {
                                    Timber.e(e, "error ");
                                    Snackbar.make(mRoot, e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                }
                            })
                            .show(),
                    this::showError
            ));

            headerViewHolder.inapp.setOnClickListener(view -> {
                BottomSheetDialogFragment subsDF = SubscriptionsFragmentDialog.newInstance();
                subsDF.show(getSupportFragmentManager(), subsDF.getTag());

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, Constants.Firebase.Analitics.StartScreen.DRAWER_HEADER_LOGINED);
                FirebaseAnalytics.getInstance(this).logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            });

            headerViewHolder.name.setText(user.fullName);
            Glide.with(this)
                    .load(user.avatar)
                    .asBitmap()
                    .centerCrop()
                    .into(new BitmapImageViewTarget(headerViewHolder.avatar) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            headerViewHolder.avatar.setImageDrawable(circularBitmapDrawable);
                        }
                    });

            //score and level
            LevelsJson.Level level = LevelsJson.getLevelForScore(user.score);
            if (level.id == LevelsJson.MAX_LEVEL_ID) {
                headerViewHolder.circleProgress.setMaxValue(level.score);
                headerViewHolder.circleProgress.setValue(level.score);

                headerViewHolder.level.setText(level.title);
                headerViewHolder.levelNum.setText(String.valueOf(level.id));

                headerViewHolder.avatar.setOnClickListener(view -> {
                    showMessageLong(getString(R.string.profile_score_info_max_level, user.score));
                    mPresenter.onAvatarClicked();
                });
            } else {
                String levelsJsonString = FirebaseRemoteConfig.getInstance().getString(Constants.Firebase.RemoteConfigKeys.LEVELS_JSON);
                LevelsJson levelsJson = mGson.fromJson(levelsJsonString, LevelsJson.class);
                LevelsJson.Level nextLevel = levelsJson.levels.get(level.id + 1);

                int levelNum = level.id;
                String levelTitle = level.title;

                int nextLevelScore = nextLevel.score;

                int max = nextLevelScore - level.score;
                int value = user.score - level.score;
                headerViewHolder.circleProgress.setMaxValue(max);
                headerViewHolder.circleProgress.setValue(value);

                headerViewHolder.level.setText(levelTitle);
                headerViewHolder.levelNum.setText(String.valueOf(levelNum));

                headerViewHolder.avatar.setOnClickListener(view -> {
                    showMessageLong(getString(R.string.profile_score_info, user.score, max - value));
                    mPresenter.onAvatarClicked();
                });
            }
            if (mMyPreferenceManager.isAppCracked()) {
                headerViewHolder.circleProgress.setMaxValue(42);
                headerViewHolder.circleProgress.setValue(0);

                headerViewHolder.level.setText(R.string.cracked_level_title);
                headerViewHolder.levelNum.setText(String.valueOf(-1));

                headerViewHolder.avatar.setOnClickListener(view -> showMessage(R.string.cracked_avatar_message));
            }
        } else {
            for (int i = 0; i < mNavigationView.getHeaderCount(); i++) {
                mNavigationView.removeHeaderView(mNavigationView.getHeaderView(i));
            }
            View headerUnlogined = LayoutInflater.from(this).inflate(R.layout.drawer_header_unlogined, mNavigationView, false);
            mNavigationView.addHeaderView(headerUnlogined);

            HeaderViewHolderUnlogined headerViewHolder = new HeaderViewHolderUnlogined(headerUnlogined);

            headerViewHolder.mLogin.setOnClickListener(view -> showLoginProvidersPopup());

            headerViewHolder.mLoginInfo.setOnClickListener(view -> new MaterialDialog.Builder(this)
                    .content(R.string.login_advantages)
                    .title(R.string.login_advantages_title)
                    .positiveText(android.R.string.ok)
                    .show());
        }
    }

    @Override
    public void showLeaderboard(LeaderBoardResponse leaderBoardResponse) {
        DialogFragment dialogFragment = LeaderboardDialogFragment.newInstance(leaderBoardResponse);
        dialogFragment.show(getSupportFragmentManager(), LeaderboardDialogFragment.TAG);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Timber.d("onActivityResult requestCode/resultCode: %s/%s", requestCode, resultCode);
        if (requestCode == REQUEST_CODE_INAPP) {
            if (resultCode == Activity.RESULT_OK) {
                if (data == null) {
                    Timber.d("error_inapp data is NULL");
                    showMessage(R.string.error_inapp);
                    return;
                }
//            int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
                String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
//            String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");
                Timber.d("purchaseData %s", purchaseData);
                PurchaseData item = mGson.fromJson(purchaseData, PurchaseData.class);
                Timber.d("You have bought the %s", item.productId);

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, item.productId);
                bundle.putFloat(FirebaseAnalytics.Param.VALUE, .5f);
                FirebaseAnalytics.getInstance(this).logEvent(FirebaseAnalytics.Event.ECOMMERCE_PURCHASE, bundle);

                if (SecureUtils.checkCrack(this)) {
                    Bundle args = new Bundle();
                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    args.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "CRACK_" + item.productId + ((firebaseUser != null) ? firebaseUser.getUid() : ""));
                    FirebaseAnalytics.getInstance(this).logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, args);
                }
                if (item.productId.equals(getString(R.string.inapp_skus).split(",")[0])) {
                    //levelUp 5
                    //add 10 000 score
                    InappHelper.consumeInapp(item.purchaseToken, getIInAppBillingService())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    result -> {
                                        Timber.d("consume inapp successful, so update user score");
                                        mPresenter.updateUserScoreForInapp(item.productId);
                                    },
                                    e -> {
                                        Timber.e(e, "error while consume inapp... X3 what to do)))");
                                        showError(e);
                                    }
                            );
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}