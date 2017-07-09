package ru.kuchanov.scpcore.api;

import android.support.annotation.StringDef;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mohax on 05.01.2017.
 * <p>
 * for scp_ru
 */
public class ParseHtmlUtils {

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({TextType.TEXT, TextType.SPOILER, TextType.IMAGE, TextType.TABLE})
    public @interface TextType {
        String TEXT = "TEXT";
        String SPOILER = "SPOILER";
        String IMAGE = "IMAGE";
        String TABLE = "TABLE";
    }

    public static ArrayList<String> getArticlesTextParts(String html) {
        ArrayList<String> articlesTextParts = new ArrayList<>();
        Document document = Jsoup.parse(html);
//        Timber.d(document.outerHtml());
        Element contentPage = document.getElementById("page-content");
        if (contentPage == null) {
            contentPage = document.body();
        }
        for (Element element : contentPage.children()) {
            articlesTextParts.add(element.outerHtml());
        }
        return articlesTextParts;
    }

    @TextType
    public static List<String> getListOfTextTypes(List<String> articlesTextParts) {
        @TextType
        List<String> listOfTextTypes = new ArrayList<>();
        for (String textPart : articlesTextParts) {
            Element element = Jsoup.parse(textPart);
            Element ourElement = element.getElementsByTag("body").first().children().first();
            if (ourElement == null) {
                listOfTextTypes.add(TextType.TEXT);
                continue;
            }
            if (ourElement.tagName().equals("p")) {
                listOfTextTypes.add(TextType.TEXT);
                continue;
            }
            if (ourElement.className().equals("collapsible-block")) {
                listOfTextTypes.add(TextType.SPOILER);
                continue;
            }
            if (ourElement.tagName().equals("table")) {
                listOfTextTypes.add(TextType.TABLE);
                continue;
            }
            if (ourElement.className().equals("rimg")
                    || ourElement.className().equals("limg")
                    || ourElement.className().equals("cimg")
                    || ourElement.classNames().contains("scp-image-block")) {
                listOfTextTypes.add(TextType.IMAGE);
                continue;
            }
            listOfTextTypes.add(TextType.TEXT);
        }

        return listOfTextTypes;
    }

    public static ArrayList<String> getSpoilerParts(String html) {
        ArrayList<String> spoilerParts = new ArrayList<>();
        Document document = Jsoup.parse(html);
        Element element = document.getElementsByClass("collapsible-block-folded").first();
        Element elementA = element.getElementsByTag("a").first();
        spoilerParts.add(elementA.text());

        Element elementUnfolded = document.getElementsByClass("collapsible-block-unfolded").first();
        Element elementContent = elementUnfolded.getElementsByClass("collapsible-block-content").first();
        spoilerParts.add(elementContent.html());
        return spoilerParts;
    }
}