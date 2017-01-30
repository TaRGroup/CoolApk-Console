package com.targroup.coolapkconsole.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;


/**
 * Created by rachel on 17-1-30.
 */

public class JsoupUtil {

    public static Document getDocument(String url) throws IOException {
        return Jsoup.connect(url).get();
    }

    public static Elements select(Document document, String tag) {
        return document.select(tag);
    }
}
