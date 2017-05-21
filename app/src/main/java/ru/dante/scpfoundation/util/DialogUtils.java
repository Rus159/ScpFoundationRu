package ru.dante.scpfoundation.util;

import android.app.Dialog;
import android.content.Context;

import com.bumptech.glide.Glide;

import ru.dante.scpfoundation.R;
import timber.log.Timber;
import com.github.chrisbanes.photoview.PhotoView;

/**
 * Created by mohax on 05.01.2017.
 * <p>
 * for scp_ru
 */
public class DialogUtils {

    //TODO think how to restore image dialog Maybe use fragment dialog?..
    public static void showImageDialog(final Context ctx, final String imgUrl) {
        Timber.d("showImageDialog");
        Dialog nagDialog = new Dialog(ctx, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        nagDialog.setCancelable(true);
        nagDialog.setContentView(R.layout.preview_image);

        final PhotoView photoView = (PhotoView) nagDialog.findViewById(R.id.image_view_touch);
        photoView.setMaximumScale(5f);

        Glide.with(ctx)
                .load(imgUrl)
                .placeholder(R.drawable.ic_image_white_48dp)
                .into(photoView);

        nagDialog.show();
    }
}