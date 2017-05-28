package ru.dante.scpfoundation.ui.fragment;

import ru.dante.scpfoundation.MyApplication;
import ru.dante.scpfoundation.mvp.contract.Objects4Articles;

/**
 * Created by mohax on 03.01.2017.
 * <p>
 * for scp_ru
 */
public class Objects4ArticlesFragment
        extends BaseListArticlesWithSearchFragment<Objects4Articles.View, Objects4Articles.Presenter>
        implements Objects4Articles.View {

    public static final String TAG = Objects4ArticlesFragment.class.getSimpleName();

    public static Objects4ArticlesFragment newInstance() {
        return new Objects4ArticlesFragment();
    }

    @Override
    protected void callInjections() {
        MyApplication.getAppComponent().inject(this);
    }

    @Override
    public void resetOnScrollListener() {
        //we do not have paging
    }

    @Override
    protected boolean shouldUpdateThisListOnLaunch() {
        return false;
    }
}