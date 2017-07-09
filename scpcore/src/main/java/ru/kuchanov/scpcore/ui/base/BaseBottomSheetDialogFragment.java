package ru.kuchanov.scpcore.ui.base;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.google.firebase.analytics.FirebaseAnalytics;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.R2;
import ru.kuchanov.scpcore.ui.dialog.SubscriptionsFragmentDialog;
import timber.log.Timber;

/**
 * Created by mohax on 14.01.2017.
 * <p>
 * for scp_ru
 */
public abstract class BaseBottomSheetDialogFragment extends BottomSheetDialogFragment {

    @BindView(R2.id.root)
    protected View mRoot;

    private Unbinder unbinder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        callInjection();
    }

    protected abstract void callInjection();

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    protected BaseActivity getBaseActivity() {
        return (BaseActivity) getActivity();
    }

    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback;

    protected BottomSheetBehavior.BottomSheetCallback getBottomSheetBehaviorCallback() {
        if (mBottomSheetBehaviorCallback == null) {
            mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {

                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                    if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                        dismiss();
                    }
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                }
            };
        }
        return mBottomSheetBehaviorCallback;
    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), getLayoutResId(), null);
        dialog.setContentView(contentView);

        unbinder = ButterKnife.bind(this, dialog);

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        if (behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(getBottomSheetBehaviorCallback());
        }
    }

    protected abstract int getLayoutResId();

    /**
     * do not use activities method as SnackBar can't be shown over bottomsheet dialog
     */
    public void showSnackBarWithAction(Constants.Firebase.CallToActionReason reason) {
        Timber.d("showSnackBarWithAction: %s", reason);
        Snackbar snackbar;
        switch (reason) {
            case REMOVE_ADS:
                snackbar = Snackbar.make(mRoot, R.string.remove_ads, Snackbar.LENGTH_LONG);
                snackbar.setAction(R.string.yes_bliad, v -> {
                    BottomSheetDialogFragment subsDF = SubscriptionsFragmentDialog.newInstance();
                    subsDF.show(getChildFragmentManager(), subsDF.getTag());

                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, Constants.Firebase.Analitics.StartScreen.SNACK_BAR);
                    FirebaseAnalytics.getInstance(getActivity()).logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                });
                break;
            case ENABLE_FONTS:
                snackbar = Snackbar.make(mRoot, R.string.only_premium, Snackbar.LENGTH_LONG);
                snackbar.setAction(R.string.activate, action -> {
                    BottomSheetDialogFragment subsDF = SubscriptionsFragmentDialog.newInstance();
                    subsDF.show(getChildFragmentManager(), subsDF.getTag());

                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, Constants.Firebase.Analitics.StartScreen.FONT);
                    FirebaseAnalytics.getInstance(getActivity()).logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                });
                break;
            case ENABLE_AUTO_SYNC:
                snackbar = Snackbar.make(mRoot, R.string.auto_sync_disabled, Snackbar.LENGTH_LONG);
                snackbar.setAction(R.string.turn_on, v -> {
                    BottomSheetDialogFragment subsDF = SubscriptionsFragmentDialog.newInstance();
                    subsDF.show(getChildFragmentManager(), subsDF.getTag());

                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, Constants.Firebase.Analitics.StartScreen.AUTO_SYNC_SNACKBAR);
                    FirebaseAnalytics.getInstance(getActivity()).logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                });
                break;
            case SYNC_NEED_AUTH:
                snackbar = Snackbar.make(mRoot, R.string.sync_need_auth, Snackbar.LENGTH_LONG);
                snackbar.setAction(R.string.authorize, v -> {
                    snackbar.dismiss();
                    getBaseActivity().showLoginProvidersPopup();
                });
                break;
            default:
                throw new IllegalArgumentException("unexpected callToActionReason");
        }
        snackbar.setActionTextColor(ContextCompat.getColor(getActivity(), R.color.material_green_500));
        snackbar.show();
    }
}