package com.targroup.coolapkconsole.model;

import java.util.ArrayList;

/**
 * Created by Trumeet on 2017/2/4.
 * An object which can describe the app like in the detail page.
 * @author Trumeet
 */

public class AppDetail {
    private String mType = "";
    private String mLanguage = "";
    private String mCatId = "";
    private String mDetail = "";
    private ArrayList<DownloadStatItem> mStats = new ArrayList<>();
    private ArrayList<String> mImageUrls = new ArrayList<>();

    public ArrayList<DownloadStatItem> getmStats() {
        return mStats;
    }

    public void setmStats(ArrayList<DownloadStatItem> mStats) {
        this.mStats = mStats;
    }

    public ArrayList<String> getImageUrls() {
        return mImageUrls;
    }

    public void setImageUrls(ArrayList<String> mImageUrls) {
        this.mImageUrls = mImageUrls;
    }

    public String[] getKeyWords() {
        return mKeyWords;
    }

    public void setKeyWords(String[] mKeyWords) {
        this.mKeyWords = mKeyWords;
    }

    private String[] mKeyWords;

    public String getDetail() {
        return mDetail;
    }

    public void setDetail(String mDetail) {
        this.mDetail = mDetail;
    }

    public String getCatId() {
        return mCatId;
    }

    public void setCatId(String mCatId) {
        this.mCatId = mCatId;
    }

    public String getLanguage() {
        return mLanguage;
    }

    public void setLanguage(String mLanguage) {
        this.mLanguage = mLanguage;
    }

    public String getType() {
        return mType;
    }

    public void setType(String mType) {
        this.mType = mType;
    }
}
