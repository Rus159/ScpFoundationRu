package ru.kuchanov.scpcore.ui.holder;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.ConstantValues;
import ru.kuchanov.scpcore.R2;
import ru.kuchanov.scpcore.ui.util.SetTextViewHTML;
import timber.log.Timber;

/**
 * Created by mohax on 11.06.2017.
 * <p>
 * for ScpFoundationRu
 */
public class ArticleTableHolder extends RecyclerView.ViewHolder {

    private SetTextViewHTML.TextItemsClickListener mTextItemsClickListener;

    @BindView(R2.id.webView)
    WebView webView;

    @Inject
    ConstantValues mConstantValues;

    public ArticleTableHolder(View itemView, SetTextViewHTML.TextItemsClickListener clickListener) {
        super(itemView);

        BaseApplication.getAppComponent().inject(this);

        ButterKnife.bind(this, itemView);

        mTextItemsClickListener = clickListener;
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void bind(String tableContent) {
        String fullHtml = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "    <head>\n" +
                "        <meta charset=\"utf-8\">\n" +
                "        <style>table.wiki-content-table{border-collapse:collapse;border-spacing:0;margin:.5em auto}table.wiki-content-table td{border:1px solid #888;padding:.3em .7em}table.wiki-content-table th{border:1px solid #888;padding:.3em .7em;background-color:#eee}</style>\n" +
                "    </head>\n" +
                "    <body>";
        fullHtml += tableContent;
        fullHtml += "</body>\n" +
                "</html>";

        webView.getSettings().setJavaScriptEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Timber.d("onPageFinished: %s", url);

                int indexOfHashTag = url.lastIndexOf("#");
                if (indexOfHashTag != -1) {
                    String link = url.substring(indexOfHashTag);
                    Timber.d("link: %s", link);

                    if (checkUrl(link)) {
                        Timber.d("Link clicked: %s", link);
                    }
                }
            }

            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String link) {
                Timber.d("Link clicked: %s", link);

                return checkUrl(link);
            }

            @TargetApi(Build.VERSION_CODES.N)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Timber.d("Link clicked: %s", request.getUrl().toString());
                String link = request.getUrl().toString();

                return checkUrl(link);
            }

            private boolean checkUrl(String link) {
                if (link.contains("javascript")) {
                    if (mTextItemsClickListener != null) {
                        mTextItemsClickListener.onUnsupportedLinkPressed(link);
                    }
                    return true;
                }
                if (TextUtils.isDigitsOnly(link)) {
                    if (mTextItemsClickListener != null) {
                        mTextItemsClickListener.onSnoskaClicked(link);
                    }
                    return true;
                }
                if (link.startsWith("scp://")) {
                    if (mTextItemsClickListener != null) {
                        mTextItemsClickListener.onSnoskaClicked(link.replace("scp://", ""));
                    }
                    return true;
                }
                if (link.startsWith("bibitem-")) {
                    if (mTextItemsClickListener != null) {
                        mTextItemsClickListener.onBibliographyClicked(link);
                    }
                    return true;
                }
                if (link.startsWith("#")) {
                    if (mTextItemsClickListener != null) {
                        mTextItemsClickListener.onTocClicked(link);
                    }
                    return true;
                }
                if (!link.startsWith("http")) {
                    link = mConstantValues.getUrlsValues().getBaseApiUrl() + link;
                }

                if (link.endsWith(".mp3")) {
                    if (mTextItemsClickListener != null) {
                        mTextItemsClickListener.onMusicClicked(link);
                    }
                    return true;
                }

                if (!link.startsWith(mConstantValues.getUrlsValues().getBaseApiUrl())) {
                    if (mTextItemsClickListener != null) {
                        mTextItemsClickListener.onExternalDomenUrlClicked(link);
                    }
                    return true;
                }

                if (mTextItemsClickListener != null) {
                    mTextItemsClickListener.onLinkClicked(link);
                    return true;
                }

                return false;
            }
        });

        webView.loadUrl("about:blank");
        webView.loadData(fullHtml, "text/html; charset=UTF-8", null);
    }
}