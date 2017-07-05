package ru.kuchanov.library;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import java.io.Serializable;

/**
 * Created by mohax on 27.06.2017.
 * <p>
 * for ScpDownloads
 */
public class DownloadEntry implements Serializable {

    @StringRes
    public int resId;
    public String name;
    public String url;
    public String dbField;

    public DownloadEntry(@StringRes int resId, @NonNull String name, @NonNull String url, String dbField) {
        this.resId = resId;
        this.name = name;
        this.url = url;
        this.dbField = dbField;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DownloadEntry that = (DownloadEntry) o;

        return resId == that.resId && name.equals(that.name);
    }

    @Override
    public int hashCode() {
        int result = resId;
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return name;
    }
}