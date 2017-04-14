package com.targroup.coolapkconsole.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.graphics.Palette;

import com.bumptech.glide.Glide;
import com.targroup.coolapkconsole.R;
import com.targroup.coolapkconsole.activities.DetailActivity;
import com.targroup.coolapkconsole.activities.SplashActivity;
import com.targroup.coolapkconsole.model.AppDetail;
import com.targroup.coolapkconsole.model.AppItem;
import com.targroup.coolapkconsole.model.DownloadStatItem;
import com.targroup.coolapkconsole.model.UserSave;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Trumeet on 2017/4/2.
 */

public class CoolapkApi {
    public static Observable<Boolean> checkLogin (final Context context) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    subscriber.onStart();
                    Document loginDocument = JsoupUtil.getDocument("developer.coolapk.com", true);
                    Elements cardElements = JsoupUtil.select(loginDocument, "div[class=mdl-card__supporting-text]");
                    String alertText = cardElements.text();
                    if (cardElements.size() > 0 && "你还没有登录，请先登录！".equals(alertText)) {
                        subscriber.onNext(Boolean.FALSE);
                    } else if ("你没有权限登录开发者中心，请先申请开发者认证！".equals(alertText)) {
                        UserSave.logout(context.getApplicationContext());
                        throw new SecurityException("User no permission to access developer console");
                    } else {
                        subscriber.onNext(Boolean.TRUE);
                    }
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<Object> getAppDetail (final Context context,
                                                   final AppItem appItem) {
        return Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call (Subscriber<? super Object> subscriber) {
                try {
                    subscriber.onStart();
                    Document detailDoc = JsoupUtil.getDocument("developer.coolapk.com/do?c=apk&m=edit&id="+
                                    appItem.getId()
                            , true);
                    String type = detailDoc.select("select[name=softtype]").select("[selected]").text();
                    String catid = detailDoc.select("select[name=catid]").select("[selected]").text();
                    String keywords = detailDoc.select("input[class=mdl-textfield__input][name=keywords]").attr("value");
                    Elements detailElements = detailDoc.select("script[id=ue-apk-editor]");
                    String detail = detailElements.select("p").html();
                    String language = detailDoc.select("select[name=language]").select("[selected]").text();
                    Elements imageDiv = detailDoc.select("div[id=apkImageUploaderList]");
                    ArrayList<String> imageUrls = new ArrayList<>();
                    for (Element element : imageDiv) {
                        imageUrls.add(element.select("img").attr("src"));
                    }
                    Elements downloads = detailDoc.select("div[id^=apk_edit__tab-panel-3]");
                    Elements downloadsTr = downloads.select("tbody").get(0).select("tr");
                    AppDetail mDetail = new AppDetail();
                    for (Element element : downloadsTr) {
                        Elements td = element.select("td");
                        DownloadStatItem item = new DownloadStatItem();
                        item.setmDate(td.get(0).text());
                        item.setmAppName(td.get(1).text());
                        item.setmVersion(td.get(2).text());
                        item.setmDownloads(td.get(3).text());
                        item.setmDownloadsStation(td.get(3).text());
                        item.setmDownloadsOutsideStation(td.get(4).text());
                        item.setmDownloadsNew(td.get(5).text());
                        item.setmInstalls(td.get(6).text());
                        item.setmInstallsNew(td.get(7).text());
                        item.setmSize(td.get(8).text());
                        mDetail.getmStats().add(item);
                    }
                    Collections.sort(mDetail.getmStats(), new Comparator<DownloadStatItem>() {
                        @Override
                        public int compare(DownloadStatItem downloadStatItem
                                , DownloadStatItem t1) {
                            int date1 = Integer.parseInt(downloadStatItem.getmDate());
                            int date2 = Integer.parseInt(t1.getmDate());
                            if (date1 > date2)
                                return 1;
                            if (date1 < date2)
                                return -1;
                            return 0;
                        }
                    });
                    mDetail.setType(type);
                    mDetail.setCatId(catid);
                    mDetail.setKeyWords(keywords.split(","));
                    mDetail.setDetail(detail);
                    mDetail.setImageUrls(imageUrls);
                    mDetail.setLanguage(language);
                    subscriber.onNext(mDetail);

                    Bitmap icon = Glide.with(context)
                            .load(appItem.getIcon())
                            .asBitmap()
                            .into(125, 125).get();
                    subscriber.onNext(icon);
                    int color = Palette.from(icon)
                            .generate().getVibrantColor(context.getResources()
                                    .getColor(R.color.colorPrimary));
                    subscriber.onNext(color);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
