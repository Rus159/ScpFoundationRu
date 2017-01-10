package ru.kuchanov.scp2.ui.base;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import butterknife.BindView;
import ru.kuchanov.scp2.R;
import ru.kuchanov.scp2.manager.MyPreferenceManager;
import ru.kuchanov.scp2.mvp.base.BaseListMvp;
import ru.kuchanov.scp2.util.DimensionUtils;

/**
 * Created by mohax on 03.01.2017.
 * <p>
 * for scp_ru
 */
public abstract class BaseListFragment<V extends BaseListMvp.View, P extends BaseListMvp.Presenter<V>>
        extends BaseFragment<V, P>
        implements BaseListMvp.View, SharedPreferences.OnSharedPreferenceChangeListener {

    @BindView(R.id.root)
    protected View root;
    @Nullable
    @BindView(R.id.progressCenter)
    protected ProgressBar mProgressBarCenter;
    @Nullable
    @BindView(R.id.swipeRefresh)
    protected SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.recyclerView)
    protected RecyclerView mRecyclerView;

    protected abstract <A extends RecyclerView.Adapter> A getAdapter();

    protected abstract boolean isSwipeRefreshEnabled();

    @Override
    public void showSwipeProgress(boolean show) {
        if (!isAdded() || mSwipeRefreshLayout == null) {
            return;
        }
        if (!mSwipeRefreshLayout.isRefreshing() && !show) {
            return;
        }
        mSwipeRefreshLayout.setProgressViewEndTarget(false, getActionBarHeight());
        mSwipeRefreshLayout.setRefreshing(show);
    }

    @Override
    public void showCenterProgress(boolean show) {
        if (!isAdded() || mProgressBarCenter == null) {
            return;
        }
        if (show) {
            if (getAdapter() != null && getAdapter().getItemCount() != 0) {
                mProgressBarCenter.setVisibility(View.GONE);
                showSwipeProgress(true);
            } else {
                mProgressBarCenter.setVisibility(View.VISIBLE);
            }
        } else {
            mProgressBarCenter.setVisibility(View.GONE);
        }
//        mProgressBarCenter.setVisibility(
//                show && getAdapter() != null && getAdapter().getItemCount() != 0
//                        ? View.VISIBLE : View.GONE
//        );
    }

    @Override
    public void showBottomProgress(boolean show) {
        if (!isAdded() || mSwipeRefreshLayout == null) {
            return;
        }
//        if (!mSwipeRefreshLayout.isRefreshing() && !show) {
//            return;
//        }

        int screenHeight = DimensionUtils.getScreenHeight();
        mSwipeRefreshLayout.setProgressViewEndTarget(false, screenHeight - getActionBarHeight() * 2);

        mSwipeRefreshLayout.setRefreshing(show);
    }

    @Override
    public void enableSwipeRefresh(boolean enable) {
        if (!isAdded() || mSwipeRefreshLayout == null || !isSwipeRefreshEnabled()) {
            return;
        }
        mSwipeRefreshLayout.setEnabled(enable);
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        switch (s) {
            case MyPreferenceManager.Keys.TEXT_SCALE_UI:
                onTextSizeUiChanged();
                break;
            default:
                //do nothing
                break;
        }
    }

    protected abstract void onTextSizeUiChanged();
}