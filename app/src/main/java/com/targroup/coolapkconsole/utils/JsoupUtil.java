package com.targroup.coolapkconsole.utils;

import com.targroup.coolapkconsole.model.UserSave;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;


/**
 * Created by rachel on 17-1-30.
 * A tool to parse urls
 * @author Rachel
 */

public class JsoupUtil {

    public static Document getDocument(String url, boolean loginCoolApk) throws IOException {
        if (!url.startsWith("https://") || !url.startsWith("http://"))
            url = "http://" + url;
        Connection connection = Jsoup.connect(url);
        if (loginCoolApk) {
            connection.cookies(new UserSave().buildWebRequestCookie());
        }
        return connection.get();
    }

    public static Elements select(Document document, String tag) {
        return document.select(tag);
    }
}
