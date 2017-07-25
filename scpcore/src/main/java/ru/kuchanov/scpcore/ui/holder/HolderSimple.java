package ru.kuchanov.scpcore.ui.holder;

import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.R2;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.ui.adapter.ArticlesListRecyclerAdapter;
import ru.kuchanov.scpcore.util.AttributeGetter;
import uk.co.chrisjenx.calligraphy.CalligraphyUtils;

/**
 * Created by mohax on 11.06.2017.
 * <p>
 * for ScpFoundationRu
 */
public class HolderSimple extends RecyclerView.ViewHolder {

    @Inject
    protected MyPreferenceManager mMyPreferenceManager;

    ArticlesListRecyclerAdapter.ArticleClickListener mArticleClickListener;

    protected Article mData;

    @BindView(R2.id.favorite)
    ImageView favorite;
    @BindView(R2.id.read)
    ImageView read;
    @BindView(R2.id.offline)
    ImageView offline;
    @BindView(R2.id.title)
    TextView title;
    @BindView(R2.id.preview)
    TextView preview;

    @BindView(R2.id.typeIcon)
    ImageView typeIcon;

    public HolderSimple(View itemView, ArticlesListRecyclerAdapter.ArticleClickListener clickListener) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        BaseApplication.getAppComponent().inject(this);

        mArticleClickListener = clickListener;
    }

    public void bind(Article article) {
        this.mData = article;
        Context context = itemView.getContext();

        float uiTextScale = mMyPreferenceManager.getUiTextScale();
        int textSizePrimary = context.getResources().getDimensionPixelSize(R.dimen.text_size_primary);

        CalligraphyUtils.applyFontToTextView(context, title, mMyPreferenceManager.getFontPath());
        CalligraphyUtils.applyFontToTextView(context, preview, mMyPreferenceManager.getFontPath());

        itemView.setOnClickListener(v -> mArticleClickListener.onArticleClicked(article, getAdapterPosition()));

        title.setTextSize(TypedValue.COMPLEX_UNIT_PX, uiTextScale * textSizePrimary);
        title.setText(Html.fromHtml(article.title));

        //(отмечание прочитанного)
        int readIconId;
        int readColorId;
        if (article.isInReaden) {
            readColorId = AttributeGetter.getColor(context, R.attr.readTextColor);
            readIconId = AttributeGetter.getDrawableId(context, R.attr.readIconUnselected);
        } else {
            readColorId = AttributeGetter.getColor(context, R.attr.newArticlesTextColor);
            readIconId = AttributeGetter.getDrawableId(context, R.attr.readIcon);
        }
        title.setTextColor(readColorId);
        read.setImageResource(readIconId);
        read.setOnClickListener(v -> mArticleClickListener.toggleReadenState(article));
        //(отмтка избранных статей)
        int favsIconId;
        if (article.isInFavorite != Article.ORDER_NONE) {
            favsIconId = AttributeGetter.getDrawableId(context, R.attr.favoriteIcon);
        } else {
            favsIconId = AttributeGetter.getDrawableId(context, R.attr.favoriteIconUnselected);
        }
        favorite.setImageResource(favsIconId);

        //Кнопки Offline
        int offlineIconId;
        if (article.text != null) {
            offlineIconId = AttributeGetter.getDrawableId(context, R.attr.iconOfflineRemove);
        } else {
            offlineIconId = AttributeGetter.getDrawableId(context, R.attr.iconOfflineAdd);
        }
        offline.animate().cancel();
        offline.setRotation(0f);
        offline.setImageResource(offlineIconId);
        offline.setOnClickListener(v -> {
            if (mArticleClickListener != null) {
                if (article.text != null) {
                    PopupMenu popup = new PopupMenu(context, offline);
                    popup.getMenu().add(0, 0, 0, R.string.delete);
                    popup.setOnMenuItemClickListener(item -> {
                        mArticleClickListener.onOfflineClicked(article);
                        return true;
                    });
                    popup.show();
                } else {
                    mArticleClickListener.onOfflineClicked(article);
                }
            }
        });

        if(context.getResources().getBoolean(R.bool.filter_by_type_enabled)) {
            setTypesIcons(article);
        } else {
            typeIcon.setVisibility(View.GONE);
        }
    }

    protected void setTypesIcons(Article article) {
        switch (article.type) {
            default:
            case Article.ObjectType.NONE:
                typeIcon.setImageResource(R.drawable.ic_none_small);
                break;
            case Article.ObjectType.NEUTRAL_OR_NOT_ADDED:
                typeIcon.setImageResource(R.drawable.ic_not_add_small);
                break;
            case Article.ObjectType.SAFE:
                typeIcon.setImageResource(R.drawable.ic_safe_small);
                break;
            case Article.ObjectType.EUCLID:
                typeIcon.setImageResource(R.drawable.ic_euclid_small);
                break;
            case Article.ObjectType.KETER:
                typeIcon.setImageResource(R.drawable.ic_keter_small);
                break;
            case Article.ObjectType.THAUMIEL:
                typeIcon.setImageResource(R.drawable.ic_thaumiel_small);
                break;
        }
    }

    public void setShouldShowPreview(boolean shouldShowPreview) {
        Context context = itemView.getContext();
        float uiTextScale = mMyPreferenceManager.getUiTextScale();
        int textSizeTertiary = context.getResources().getDimensionPixelSize(R.dimen.text_size_tertiary);
        //show preview only on siteSearch fragment
        if (shouldShowPreview) {
            preview.setVisibility(View.VISIBLE);
            preview.setTextSize(TypedValue.COMPLEX_UNIT_PX, uiTextScale * textSizeTertiary);
            preview.setText(Html.fromHtml(mData.preview));
        } else {
            preview.setVisibility(View.GONE);
        }
    }

    public void setShouldShowPopupOnFavoriteClick(boolean shouldShowPopupOnFavoriteClick) {
        Context context = itemView.getContext();
        favorite.setOnClickListener(v -> {
            if (shouldShowPopupOnFavoriteClick && mData.isInFavorite != Article.ORDER_NONE) {
                PopupMenu popup = new PopupMenu(context, favorite);
                popup.getMenu().add(0, 0, 0, R.string.delete);
                popup.setOnMenuItemClickListener(item -> {
                    mArticleClickListener.toggleFavoriteState(mData);
                    return true;
                });
                popup.show();
            } else {
                mArticleClickListener.toggleFavoriteState(mData);
            }
        });
    }
}