package ru.kuchanov.scpcore.ui.holder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.R2;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.ui.util.SetTextViewHTML;
import ru.kuchanov.scpcore.util.AttributeGetter;
import ru.kuchanov.scpcore.util.DimensionUtils;
import timber.log.Timber;
import uk.co.chrisjenx.calligraphy.CalligraphyUtils;

/**
 * Created by mohax on 11.06.2017.
 * <p>
 * for ScpFoundationRu
 */
public class ArticleImageHolder extends RecyclerView.ViewHolder {

    @Inject
    MyPreferenceManager mMyPreferenceManager;
    @Inject
    SetTextViewHTML mSetTextViewHTML;

    private SetTextViewHTML.TextItemsClickListener mTextItemsClickListener;

    @BindView(R2.id.image)
    ImageView imageView;
    @BindView(R2.id.title)
    TextView titleTextView;

    public ArticleImageHolder(View itemView, SetTextViewHTML.TextItemsClickListener clickListener) {
        super(itemView);
        BaseApplication.getAppComponent().inject(this);
        ButterKnife.bind(this, itemView);

        mTextItemsClickListener = clickListener;
    }

    public void bind(String articleTextPart) {
        Context context = itemView.getContext();
        Document document = Jsoup.parse(articleTextPart);
        Element imageTag = document.getElementsByTag("img").first();
        String imageUrl = imageTag == null ? null : imageTag.attr("src");

        CalligraphyUtils.applyFontToTextView(context, titleTextView, mMyPreferenceManager.getFontPath());

        Glide.with(context)
                .load(imageUrl)
                .error(AttributeGetter.getDrawableId(context, R.attr.iconEmptyImage))
                .fitCenter()
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        Timber.e(e, "error while download image by glide");
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        int width = resource.getIntrinsicWidth();
                        int height = resource.getIntrinsicHeight();

                        float multiplier = (float) width / height;

//                        width = DimensionUtils.getScreenWidth();
                        width = imageView.getMeasuredWidth();
                        Timber.d("width: %s", width);
                        Timber.d("DimensionUtils.getScreenWidth(): %s", DimensionUtils.getScreenWidth());

                        height = (int) (width / multiplier);

                        imageView.getLayoutParams().width = width;
                        imageView.getLayoutParams().height = height;

                        imageView.setScaleType(ImageView.ScaleType.FIT_XY);

                        imageView.setOnClickListener(v -> mTextItemsClickListener.onImageClicked(imageUrl));
                        return false;
                    }
                })
                .into(imageView);

        String title = null;
        if (!document.getElementsByTag("span").isEmpty()) {
            title = document.getElementsByTag("span").html();
        } else if (!document.getElementsByClass("scp-image-caption").isEmpty()) {
            title = document.getElementsByClass("scp-image-caption").first().html();
        }

        if (!TextUtils.isEmpty(title)) {
            titleTextView.setLinksClickable(true);
            titleTextView.setMovementMethod(LinkMovementMethod.getInstance());
            //TODO add settings for it
//            textView.setTextIsSelectable(true);
            mSetTextViewHTML.setText(titleTextView, title, mTextItemsClickListener);
        }
    }
}