/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.luckynineapps.stickersmaker;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StickerContentProvider extends ContentProvider {
    public static final String CONTENT_PROVIDER_AUTHORITY = "com.luckynineapps.stickersmaker.stickercontentprovider";

    /**
     * Do not change the strings listed below, as these are used by WhatsApp. And changing these will break the interface between local_sticker app and WhatsApp.
     */
    public static final String STICKER_PACK_IDENTIFIER_IN_QUERY = "sticker_pack_identifier";
    public static final String STICKER_PACK_NAME_IN_QUERY = "sticker_pack_name";
    public static final String STICKER_PACK_PUBLISHER_IN_QUERY = "sticker_pack_publisher";
    public static final String STICKER_PACK_ICON_IN_QUERY = "sticker_pack_icon";
    public static final String ANDROID_APP_DOWNLOAD_LINK_IN_QUERY = "android_play_store_link";
    public static final String IOS_APP_DOWNLOAD_LINK_IN_QUERY = "ios_app_download_link";
    public static final String PUBLISHER_EMAIL = "sticker_pack_publisher_email";
    public static final String PUBLISHER_WEBSITE = "sticker_pack_publisher_website";
    public static final String PRIVACY_POLICY_WEBSITE = "sticker_pack_privacy_policy_website";
    public static final String LICENSE_AGREENMENT_WEBSITE = "sticker_pack_license_agreement_website";

    public static final String STICKER_FILE_NAME_IN_QUERY = "sticker_file_name";
    public static final String STICKER_FILE_EMOJI_IN_QUERY = "sticker_emoji";
    public static final String CONTENT_FILE_NAME = "contents.json";

    public static Uri AUTHORITY_URI = new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(CONTENT_PROVIDER_AUTHORITY).appendPath(StickerContentProvider.METADATA).build();

    /**
     * Do not change the values in the UriMatcher because otherwise, WhatsApp will not be able to fetch the stickers from the ContentProvider.
     */
    private static final UriMatcher MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    static final String METADATA = "metadata";
    private static final int METADATA_CODE = 1;

    private static final int METADATA_CODE_FOR_SINGLE_PACK = 2;

    static final String STICKERS = "stickers";
    private static final int STICKERS_CODE = 3;

    static final String STICKERS_ASSET = "stickers_asset";
    private static final int STICKERS_ASSET_CODE = 4;

    private static final int STICKER_PACK_TRAY_ICON_CODE = 5;

    private List<StickerPack> stickerPackList;

    @SuppressWarnings("unchecked")
    @Override
    public boolean onCreate() {
        final String authority = CONTENT_PROVIDER_AUTHORITY;
        if (!authority.startsWith(getContext().getPackageName())) {
            throw new IllegalStateException("your authority (" + authority + ") for the content provider should start with your package name: " + getContext().getPackageName());
        }

        SharedPreferences sp = getContext().getSharedPreferences("data", Context.MODE_PRIVATE);
        String stickerPacksString = sp.getString("sticker_packs", "");
        if (stickerPacksString.equals("")) {
            stickerPackList = new ArrayList<>();
        } else {
            stickerPackList = (ArrayList<StickerPack>)ObjectSerializer.deserialize(stickerPacksString);
        }

        //the call to get the metadata for the local_sticker packs.
        MATCHER.addURI(authority, METADATA, METADATA_CODE);

        //the call to get the metadata for single local_sticker pack. * represent the identifier
        MATCHER.addURI(authority, METADATA + "/*", METADATA_CODE_FOR_SINGLE_PACK);

        //gets the list of stickers for a local_sticker pack, * respresent the identifier.
        MATCHER.addURI(authority, STICKERS + "/*", STICKERS_CODE);

        try {
            List<StickerPack> stickerPacks = getStickerPackList();
            if (stickerPacks != null) {
                for (StickerPack stickerPack : getStickerPackList()) {
                    MATCHER.addURI(authority, STICKERS_ASSET + "/" + stickerPack.identifier + "/" + stickerPack.trayImageFile, STICKER_PACK_TRAY_ICON_CODE);
                    List<Sticker> stickers = stickerPack.getStickers();
                    if (stickers != null) {
                        for (Sticker sticker : stickers) {
                            MATCHER.addURI(authority, STICKERS_ASSET + "/" + stickerPack.identifier + "/" + sticker.imageFileName, STICKERS_ASSET_CODE);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        final int code = MATCHER.match(uri);
        if (code == METADATA_CODE) {
            return getPackForAllStickerPacks(uri);
        } else if (code == METADATA_CODE_FOR_SINGLE_PACK) {
            return getCursorForSingleStickerPack(uri);
        } else if (code == STICKERS_CODE) {
            return getStickersForAStickerPack(uri);
        } else {
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    /*@Nullable
    @Override
    public AssetFileDescriptor openAssetFile(@NonNull Uri uri, @NonNull String mode) {
        /*final int matchCode = MATCHER.match(uri);
        if (matchCode == STICKERS_ASSET_CODE || matchCode == STICKER_PACK_TRAY_ICON_CODE) {
            return getImageAsset(uri);
        }
        try {
            return getContext().getAssets().openFd("1/02_Cuppy_lol.webp");
        } catch (Exception exp) {
            exp.printStackTrace();
        }
        return null;
    }*/

    @Nullable
    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode) throws FileNotFoundException {
        String identifier = uri.getPath().split("/")[2];
        String fileName = uri.getPath().split("/")[3];
        String folderPath = "";
        List<StickerPack> stickerPacks = getStickerPackList();
        for (StickerPack stickerPack:stickerPacks) {
            if (stickerPack.identifier.equals(identifier)) {
                for (Sticker sticker:stickerPack.getStickers()) {
                    folderPath = new File(sticker.getPath()).getParentFile().getAbsolutePath();
                    String path = sticker.getPath();
                    if (path.endsWith("/")) {
                        path = path.substring(0, path.length()-1);
                    }
                    String name = path.substring(path.lastIndexOf("/")+1, path.length());
                    if (fileName.equals(name)) {
                        return ParcelFileDescriptor.open(new File(path), ParcelFileDescriptor.MODE_READ_WRITE);
                    }
                }
            }
        }
        String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (rootPath.endsWith("/")) {
            rootPath = rootPath.substring(0, rootPath.length()-1);
        }
        return ParcelFileDescriptor.open(new File(folderPath+"/tray.webp"), ParcelFileDescriptor.MODE_READ_WRITE);
        //return ParcelFileDescriptor.open(new File(rootPath+"/Android/data/com.dn.whatsappstickers/stickers/Android/tray.webp"), ParcelFileDescriptor.MODE_READ_WRITE);
        //return ParcelFileDescriptor.open(new File("/sdcard/02_Cuppy_lol.webp"), ParcelFileDescriptor.MODE_READ_WRITE);
        //return null;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        final int matchCode = MATCHER.match(uri);
        switch (matchCode) {
            case METADATA_CODE:
                return "vnd.android.cursor.dir/vnd." + CONTENT_PROVIDER_AUTHORITY + "." + METADATA;
            case METADATA_CODE_FOR_SINGLE_PACK:
                return "vnd.android.cursor.item/vnd." + CONTENT_PROVIDER_AUTHORITY + "." + METADATA;
            case STICKERS_CODE:
                return "vnd.android.cursor.dir/vnd." + CONTENT_PROVIDER_AUTHORITY + "." + STICKERS;
            case STICKERS_ASSET_CODE:
                return "image/webp";
            case STICKER_PACK_TRAY_ICON_CODE:
                return "image/png";
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    private synchronized void readContentFile(@NonNull Context context) {
        try {
            InputStream contentsInputStream = context.getAssets().open(CONTENT_FILE_NAME);
            stickerPackList = ContentFileParser.parseStickerPacks(contentsInputStream);
        } catch (IOException | IllegalStateException e) {
            throw new RuntimeException(CONTENT_FILE_NAME + " file has some issues: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<StickerPack> getStickerPackList() {
        SharedPreferences sp = getContext().getSharedPreferences("data", Context.MODE_PRIVATE);
        String stickerPacksString = sp.getString("sticker_packs", "");
        if (stickerPacksString.equals("")) {
            stickerPackList = new ArrayList<>();
        } else {
            stickerPackList = (ArrayList<StickerPack>)ObjectSerializer.deserialize(stickerPacksString);
        }
        if (stickerPackList == null) {
            stickerPackList = new ArrayList<>();
            //readContentFile(Objects.requireNonNull(getContext()));
        }
        return stickerPackList;
    }

    private Cursor getPackForAllStickerPacks(@NonNull Uri uri) {
        return getStickerPackInfo(uri, getStickerPackList());
    }

    private Cursor getCursorForSingleStickerPack(@NonNull Uri uri) {
        final String identifier = uri.getLastPathSegment();
        for (StickerPack stickerPack : getStickerPackList()) {
            if (identifier.equals(stickerPack.identifier)) {
                return getStickerPackInfo(uri, Collections.singletonList(stickerPack));
            }
        }

        return getStickerPackInfo(uri, new ArrayList<StickerPack>());
    }

    @NonNull
    private Cursor getStickerPackInfo(@NonNull Uri uri, @NonNull List<StickerPack> stickerPackList) {
        MatrixCursor cursor = new MatrixCursor(
                new String[]{
                        STICKER_PACK_IDENTIFIER_IN_QUERY,
                        STICKER_PACK_NAME_IN_QUERY,
                        STICKER_PACK_PUBLISHER_IN_QUERY,
                        STICKER_PACK_ICON_IN_QUERY,
                        ANDROID_APP_DOWNLOAD_LINK_IN_QUERY,
                        IOS_APP_DOWNLOAD_LINK_IN_QUERY,
                        PUBLISHER_EMAIL,
                        PUBLISHER_WEBSITE,
                        PRIVACY_POLICY_WEBSITE,
                        LICENSE_AGREENMENT_WEBSITE
                });
        for (StickerPack stickerPack : stickerPackList) {
            MatrixCursor.RowBuilder builder = cursor.newRow();
            builder.add(stickerPack.identifier);
            builder.add(stickerPack.name);
            builder.add(stickerPack.publisher);
            builder.add(stickerPack.trayImageFile);
            builder.add(stickerPack.androidPlayStoreLink);
            builder.add(stickerPack.iosAppStoreLink);
            builder.add(stickerPack.publisherEmail);
            builder.add(stickerPack.publisherWebsite);
            builder.add(stickerPack.privacyPolicyWebsite);
            builder.add(stickerPack.licenseAgreementWebsite);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @NonNull
    private Cursor getStickersForAStickerPack(@NonNull Uri uri) {
        final String identifier = uri.getLastPathSegment();
        MatrixCursor cursor = new MatrixCursor(new String[]{STICKER_FILE_NAME_IN_QUERY, STICKER_FILE_EMOJI_IN_QUERY});
        for (StickerPack stickerPack : getStickerPackList()) {
            if (identifier.equals(stickerPack.identifier)) {
                for (Sticker sticker : stickerPack.getStickers()) {
                    cursor.addRow(new Object[]{sticker.imageFileName, TextUtils.join(",", sticker.emojis)});
                }
            }
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        throw new UnsupportedOperationException("Not supported");
    }
}
