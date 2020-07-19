package com.luckynineapps.stickersmaker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

public class Example {
    Context context;

    public Example(Context context) {
        this.context = context;
        uploadStickerPacks();
        addStickersToWhatsApp();
    }

    public void listAllStickerPacks() {
        Cursor c = context.getContentResolver().query(Uri.parse("content://com.dn.whatsappsticker.stickercontentprovider/metadata"), null, null, null, null);
        c.moveToFirst();
        int i = 0;
        while (i < c.getCount()) {
            String identifier = c.getString(c.getColumnIndexOrThrow("sticker_pack_identifier"));
            Log.e("Log", "ID: "+identifier);
            c.moveToNext();
            i++;
        }
    }

    public void queryStickers() {
        Cursor c = context.getContentResolver().query(Uri.parse("content://com.dn.whatsappsticker.stickercontentprovider/stickers/1"), null, null, null, null);
        c.moveToFirst();
        int count = c.getCount();
        int i = 0;
        while (i < count) {
            String fileName = c.getString(c.getColumnIndexOrThrow("sticker_file_name"));
            Log.e("Log", "File name: "+fileName);
            c.moveToNext();
            i++;
        }
    }

    public void addStickersToWhatsApp() {
        Intent i = new Intent();
        i.setAction("com.whatsapp.intent.action.ENABLE_STICKER_PACK");
        i.putExtra("sticker_pack_id", "1");
        i.putExtra("sticker_pack_authority", "com.dn.whatsappsticker.stickercontentprovider");
        i.putExtra("sticker_pack_name", "DanaOS 1");
        context.startActivity(i);
    }

    @SuppressWarnings("unchecked")
    public void testStringSetWrite() {
        Log.e("Log", "Adding data...");
        SharedPreferences sp = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor e = sp.edit();
        String dataString = sp.getString("keys", "");
        ArrayList<String> data;
        if (dataString.equals("")) {
            data = new ArrayList<>();
        } else {
            data = (ArrayList<String>)ObjectSerializer.deserialize(dataString);
        }
        data.add("key1");
        data.add("key2");
        data.add("key3");
        data.add("key4");
        data.add("key5");
        data.add("key6");
        e.putString("keys", ObjectSerializer.serialize(data));
        e.commit();
    }

    @SuppressWarnings("unchecked")
    public void testStringSetRead() {
        SharedPreferences sp = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        String dataString = sp.getString("keys", "");
        ArrayList<String> data = (ArrayList<String>)ObjectSerializer.deserialize(dataString);
        for (String singleData:data) {
            Log.e("Log", "Key: "+singleData);
        }
    }

    @SuppressWarnings("unchecked")
    public void readStickerPacks() {
        SharedPreferences sp = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        String stickerPacksString = sp.getString("sticker_packs", "");
        ArrayList<StickerPack> stickerPacks;
        if (stickerPacksString.equals("")) {
            stickerPacks = new ArrayList<>();
        } else {
            stickerPacks = (ArrayList<StickerPack>)ObjectSerializer.deserialize(stickerPacksString);
        }
        for (StickerPack pack:stickerPacks) {
            Log.e("Log", "ID: "+pack.identifier);
            for (Sticker sticker:pack.getStickers()) {
                Log.e("Log", "File name: "+sticker.imageFileName);
            }
        }
    }

    @SuppressWarnings("all")
    public void uploadStickerPacks() {
        ArrayList<Sticker> stickers = new ArrayList<>();
        Sticker sticker1 = new Sticker("file1.webp", Arrays.asList(new String[] {
                "☕", "☕"
        }));
        Sticker sticker2 = new Sticker("file2.webp", Arrays.asList(new String[] {
                "☕", "☕"
        }));
        Sticker sticker3 = new Sticker("file3.webp", Arrays.asList(new String[] {
                "☕", "☕"
        }));
        stickers.add(sticker1);
        stickers.add(sticker2);
        stickers.add(sticker3);
        /*for (int i=1; i<10; i++) {
            addStickerPack(Integer.toString(i+1), "DanaOS "+(i+1), "Dana", "tray.png", "danaoscompany@gmail.com", "danaos.com", "danaos.com", "danaos.com", stickers);
        }*/
        addStickerPack("1", "DanaOS 1", "Dana", "tray.png", "danaoscompany@gmail.com", "danaos.com", "danaos.com", "danaos.com", stickers);
    }

    @SuppressWarnings({"all", "unchecked"})
    public void addStickerPack(String identifier, String name, String publisher, String trayImageFile, String publisherEmail, String publisherWebsite, String privacyPolicyURL, String licenseAgreementURL, ArrayList<Sticker> stickers) {
        SharedPreferences sp = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor e = sp.edit();
        String stickerPacksString = sp.getString("sticker_packs", "");
        ArrayList<StickerPack> stickerPacks;
        if (stickerPacksString.equals("")) {
            stickerPacks = new ArrayList<>();
        } else {
            stickerPacks = (ArrayList<StickerPack>)ObjectSerializer.deserialize(stickerPacksString);
        }
        StickerPack pack = new StickerPack(identifier, name, publisher, trayImageFile, publisherEmail, publisherWebsite, privacyPolicyURL, licenseAgreementURL);
        pack.setStickers(stickers);
        boolean packAlreadyAdded = false;
        int previousPackIndex = 0;
        for (int i=0; i<stickerPacks.size(); i++) {
            StickerPack stickerPack = stickerPacks.get(i);
            if (stickerPack.identifier.equals(identifier)) {
                previousPackIndex = i;
                packAlreadyAdded = true;
                break;
            }
        }
        if (packAlreadyAdded) {
            stickerPacks.set(previousPackIndex, pack);
        } else {
            stickerPacks.add(pack);
        }
        e.putString("sticker_packs", ObjectSerializer.serialize(stickerPacks));
        e.commit();
    }
}
