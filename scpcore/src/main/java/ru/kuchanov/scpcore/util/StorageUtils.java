package ru.kuchanov.scpcore.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import ru.kuchanov.scpcore.R;

import static ru.kuchanov.scpcore.util.IntentUtils.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE;

/**
 * Created by Ivan Semkin on 4/27/2017.
 *
 * for scp_ru
 */
public class StorageUtils {

    public static String saveImageToGallery(Activity activity, Bitmap image) {
        int permissionCheck = ContextCompat.checkSelfPermission(
                activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            return MediaStore.Images.Media.insertImage(activity.getContentResolver(), image,
                    activity.getString(R.string.image_title),
                    null); //todo wtf
        } else {
            ActivityCompat.requestPermissions(
                    activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
        return null;
    }
}