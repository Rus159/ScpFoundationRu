package ru.kuchanov.scpcore.mvp.contract;

/**
 * Created by y.kuchanov on 21.12.16.
 * <p>
 * for scp_ru
 */
public interface ArticleScreenMvp extends DrawerMvp {
    interface View extends DrawerMvp.View {
    }

    interface Presenter extends DrawerMvp.Presenter<View> {
        void toggleFavorite(String url);
    }
}