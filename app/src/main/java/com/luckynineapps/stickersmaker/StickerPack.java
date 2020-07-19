/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.luckynineapps.stickersmaker;

import android.widget.ProgressBar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class StickerPack implements Serializable {
    public static final int TYPE_AD = 1;
    public static final int TYPE_STICKER_PACK = 2;
    String identifier;
    String name;
    String publisher;
    String trayImageFile;
    String publisherEmail;
    String publisherWebsite;
    String privacyPolicyWebsite;
    String licenseAgreementWebsite;
    String iosAppStoreLink;
    private List<Sticker> stickers;
    private long totalSize;
    String androidPlayStoreLink;
    private boolean isWhitelisted;
    private String path = "";
    ArrayList<FileItem> files;
    WebStickerAdapter adapter;
    ProgressBar progress;
    int type = TYPE_STICKER_PACK;
    boolean progressEnabled = false;
    boolean canBeAddedToWhatsApp = false;

    public StickerPack() {
        files = new ArrayList<>();
    }

    public StickerPack(String identifier, String name, String publisher, String trayImageFile, String publisherEmail, String publisherWebsite, String privacyPolicyWebsite, String licenseAgreementWebsite) {
        this.identifier = identifier;
        this.name = name;
        this.publisher = publisher;
        this.trayImageFile = trayImageFile;
        this.publisherEmail = publisherEmail;
        this.publisherWebsite = publisherWebsite;
        this.privacyPolicyWebsite = privacyPolicyWebsite;
        this.licenseAgreementWebsite = licenseAgreementWebsite;
        files = new ArrayList<>();
    }

    void setIsWhitelisted(boolean isWhitelisted) {
        this.isWhitelisted = isWhitelisted;
    }

    boolean getIsWhitelisted() {
        return isWhitelisted;
    }

    void setStickers(List<Sticker> stickers) {
        this.stickers = stickers;
        totalSize = 0;
        for (Sticker sticker : stickers) {
            totalSize += sticker.size;
        }
    }

    public void setAndroidPlayStoreLink(String androidPlayStoreLink) {
        this.androidPlayStoreLink = androidPlayStoreLink;
    }

    public void setIosAppStoreLink(String iosAppStoreLink) {
        this.iosAppStoreLink = iosAppStoreLink;
    }

    public List<Sticker> getStickers() {
        return stickers;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getTrayImageFile() {
        return trayImageFile;
    }

    public void setTrayImageFile(String trayImageFile) {
        this.trayImageFile = trayImageFile;
    }

    public String getPublisherEmail() {
        return publisherEmail;
    }

    public String getPublisherWebsite() {
        return publisherWebsite;
    }

    public String getPrivacyPolicyWebsite() {
        return privacyPolicyWebsite;
    }

    public String getLicenseAgreementWebsite() {
        return licenseAgreementWebsite;
    }

    public String getIosAppStoreLink() {
        return iosAppStoreLink;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public String getAndroidPlayStoreLink() {
        return androidPlayStoreLink;
    }

    public boolean isWhitelisted() {
        return isWhitelisted;
    }

    public void setWhitelisted(boolean whitelisted) {
        isWhitelisted = whitelisted;
    }

    public ArrayList<FileItem> getFiles() {
        return files;
    }

    public void setPublisherEmail(String publisherEmail) {
        this.publisherEmail = publisherEmail;
    }

    public void setPublisherWebsite(String publisherWebsite) {
        this.publisherWebsite = publisherWebsite;
    }

    public void setPrivacyPolicyWebsite(String privacyPolicyWebsite) {
        this.privacyPolicyWebsite = privacyPolicyWebsite;
    }

    public void setLicenseAgreementWebsite(String licenseAgreementWebsite) {
        this.licenseAgreementWebsite = licenseAgreementWebsite;
    }

    public void setFiles(ArrayList<FileItem> files) {
        this.files = files;
    }

    public WebStickerAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(WebStickerAdapter adapter) {
        this.adapter = adapter;
    }

    public ProgressBar getProgress() {
        return progress;
    }

    public void setProgress(ProgressBar progress) {
        this.progress = progress;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isProgressEnabled() {
        return progressEnabled;
    }

    public void setProgressEnabled(boolean progressEnabled) {
        this.progressEnabled = progressEnabled;
    }

    public boolean canBeAddedToWhatsApp() {
        return canBeAddedToWhatsApp;
    }

    public void setCanBeAddedToWhatsApp(boolean canBeAddedToWhatsApp) {
        this.canBeAddedToWhatsApp = canBeAddedToWhatsApp;
    }
}
