package com.targroup.coolapkconsole.model;

import android.graphics.Bitmap;

/**
 * Created by rachel on 17-1-30.
 * Just a app object.
 */

public class AppItem {
    Bitmap icon;
    String name;
    String packageName;
    String version;
    String size;
    String apiVersion;
    String type;
    String tag;
    String author;
    String downloads;
    String creator;
    String updater;
    String lastUpdate;
    String status;

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
}
