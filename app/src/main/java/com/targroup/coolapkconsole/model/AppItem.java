package com.targroup.coolapkconsole.model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by rachel on 17-1-30.
 * Just a app object.
 * @author Rachel
 */

public class AppItem implements Parcelable{
    private Bitmap icon;
    private String name;
    private String packageName;
    private String version;
    private String size;
    private String apiVersion;
    private String type;
    private String tag;
    private String author;
    private String downloads;
    private String creator;
    private String updater;
    private String lastUpdate;
    private String status;

    public AppItem(Bitmap icon, String name, String packageName, String version, String size, String apiVersion,
                   String type, String tag, String author, String downloads, String creator, String updater, String lastUpdate, String status) {
        this.icon = icon;
        this.name = name;
        this.packageName = packageName;
        this.version = version;
        this.size = size;
        this.apiVersion = apiVersion;
        this.type = type;
        this.tag = tag;
        this.author = author;
        this.downloads = downloads;
        this.creator = creator;
        this.updater = updater;
        this.lastUpdate = lastUpdate;
        this.status = status;
    }

    protected AppItem(Parcel in) {
        icon = in.readParcelable(Bitmap.class.getClassLoader());
        name = in.readString();
        packageName = in.readString();
        version = in.readString();
        size = in.readString();
        apiVersion = in.readString();
        type = in.readString();
        tag = in.readString();
        author = in.readString();
        downloads = in.readString();
        creator = in.readString();
        updater = in.readString();
        lastUpdate = in.readString();
        status = in.readString();
    }

    public static final Creator<AppItem> CREATOR = new Creator<AppItem>() {
        @Override
        public AppItem createFromParcel(Parcel in) {
            return new AppItem(in);
        }

        @Override
        public AppItem[] newArray(int size) {
            return new AppItem[size];
        }
    };

    public Bitmap getIcon() {
        return icon;
    }

    public void setIcon(Bitmap icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDownloads() {
        return downloads;
    }

    public void setDownloads(String downloads) {
        this.downloads = downloads;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getUpdater() {
        return updater;
    }

    public void setUpdater(String updater) {
        this.updater = updater;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(icon, flags);
        dest.writeString(name);
        dest.writeString(packageName);
        dest.writeString(version);
        dest.writeString(size);
        dest.writeString(apiVersion);
        dest.writeString(type);
        dest.writeString(tag);
        dest.writeString(author);
        dest.writeString(downloads);
        dest.writeString(creator);
        dest.writeString(updater);
        dest.writeString(lastUpdate);
        dest.writeString(status);
    }
}
