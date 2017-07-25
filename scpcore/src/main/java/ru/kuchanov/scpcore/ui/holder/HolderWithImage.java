package ru.kuchanov.scpcore.ui.holder;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.flexbox.FlexboxLayout;

import butterknife.BindView;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.R2;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.db.model.ArticleTag;
import ru.kuchanov.scpcore.ui.adapter.ArticlesListRecyclerAdapter;
import ru.kuchanov.scpcore.ui.view.TagView;
import ru.kuchanov.scpcore.util.AttributeGetter;
import ru.kuchanov.scpcore.util.DateUtils;
import uk.co.chrisjenx.calligraphy.CalligraphyUtils;

/**
 * Created by mohax on 11.06.2017.
 * <p>
 * for ScpFoundationRu
 */
public class HolderWithImage extends HolderSimple {

    @BindView(R2.id.typeIcon)
    ImageView typeIcon;
    @BindView(R2.id.image)
    ImageView image;
    @BindView(R2.id.rating)
    TextView rating;
    @BindView(R2.id.date)
    TextView date;

    @BindView(R2.id.tags)
    FlexboxLayout mTagsContainer;
    @BindView(R2.id.tagsExpander)
    TextView mTagsExpander;

    public HolderWithImage(View itemView, ArticlesListRecyclerAdapter.ArticleClickListener clickListener) {
        super(itemView, clickListener);
    }

    @Override
    public void bind(Article article) {
        super.bind(article);
        Context context = itemView.getContext();

        CalligraphyUtils.applyFontToTextView(context, rating, mMyPreferenceManager.getFontPath());
        CalligraphyUtils.applyFontToTextView(context, date, mMyPreferenceManager.getFontPath());

        //TODO show them in ViewPager
        //set image
        if (article.imagesUrls != null && !article.imagesUrls.isEmpty()) {
            Glide.clear(image);
            Glide.with(context)
                    .load(article.imagesUrls.first().val)
                    .placeholder(AttributeGetter.getDrawableId(context, R.attr.iconEmptyImage))
                    .error(AttributeGetter.getDrawableId(context, R.attr.iconEmptyImage))
                    .animate(android.R.anim.fade_in)
                    .centerCrop()
                    .into(image);
        } else {
            Glide.clear(image);
            Glide.with(context)
                    .load(R.drawable.ic_default_image_big)
                    .placeholder(AttributeGetter.getDrawableId(context, R.attr.iconEmptyImage))
                    .error(AttributeGetter.getDrawableId(context, R.attr.iconEmptyImage))
                    .centerCrop()
                    .animate(android.R.anim.fade_in)
                    .into(image);
        }

        rating.setText(article.rating != 0 ? context.getString(R.string.rating, article.rating) : null);
        date.setText(article.updatedDate != null ? DateUtils.getArticleDateShortFormat(article.updatedDate) : null);

        showTags(article);
    }

    @Override
    protected void setTypesIcons(Article article) {
        switch (article.type) {
            default:
            case Article.ObjectType.NONE:
                typeIcon.setImageResource(R.drawable.ic_none_big);
                break;
            case Article.ObjectType.NEUTRAL_OR_NOT_ADDED:
                typeIcon.setImageResource(R.drawable.ic_not_add_big);
                break;
            case Article.ObjectType.SAFE:
                typeIcon.setImageResource(R.drawable.ic_safe_big);
                break;
            case Article.ObjectType.EUCLID:
                typeIcon.setImageResource(R.drawable.ic_euclid_big);
                break;
            case Article.ObjectType.KETER:
                typeIcon.setImageResource(R.drawable.ic_keter_big);
                break;
            case Article.ObjectType.THAUMIEL:
                typeIcon.setImageResource(R.drawable.ic_thaumiel_big);
                break;
        }
    }

    private void showTags(Article article) {
//            Timber.d("article.tags: %s", Arrays.toString(article.tags.toArray()));
        Context context = itemView.getContext();
//            Timber.d("mTagsContainer.getChildCount(): %s", mTagsContainer.getChildCount());
        int childCount = mTagsContainer.getChildCount();
        for (int i = childCount - 1; i > 0; i--) {
            mTagsContainer.removeViewAt(i);
        }
//            Timber.d("mTagsContainer.getChildCount(): %s", mTagsContainer.getChildCount());
        if (article.tags == null || article.tags.isEmpty()) {
            mTagsContainer.setVisibility(View.GONE);
        } else {
            mTagsContainer.setVisibility(View.VISIBLE);

            mTagsExpander.setCompoundDrawablesWithIntrinsicBounds(0, 0, AttributeGetter.getDrawableId(context, R.attr.iconArrowDown), 0);
            mTagsExpander.setOnClickListener(v -> {
                if (mTagsContainer.getChildAt(1).getVisibility() == View.GONE) {
                    mTagsExpander.setCompoundDrawablesWithIntrinsicBounds(0, 0, AttributeGetter.getDrawableId(context, R.attr.iconArrowUp), 0);
                    for (int i = 1; i < mTagsContainer.getChildCount(); i++) {
                        mTagsContainer.getChildAt(i).setVisibility(View.VISIBLE);
                    }
                } else {
                    mTagsExpander.setCompoundDrawablesWithIntrinsicBounds(0, 0, AttributeGetter.getDrawableId(context, R.attr.iconArrowDown), 0);
                    for (int i = 1; i < mTagsContainer.getChildCount(); i++) {
                        mTagsContainer.getChildAt(i).setVisibility(View.GONE);
                    }
                }
            });

            for (ArticleTag tag : article.tags) {
                TagView tagView = new TagView(context);
                tagView.setTag(tag);
                tagView.setTagTextSize(11);
                tagView.setActionImage(TagView.Action.NONE);

                tagView.setOnTagClickListener((tagView1, tag1) -> mArticleClickListener.onTagClicked(tag1));
                tagView.setVisibility(View.GONE);

                mTagsContainer.addView(tagView);
            }
        }
    }
}