package com.luckynineapps.stickersmaker;

import java.io.Serializable;

public class FileItem implements Serializable {
    public static final int TYPE_IMAGE = 1;
    public static final int TYPE_VIEW_MORE = 2;
    public static final int TYPE_AD = 4;
    private String path = "";
    private String folderPath = "";
    private int type = TYPE_IMAGE;
    private long time = 0L;
    String stickerPackID = "";

    public FileItem() {
    }

    public FileItem(String path, String folderPath, int type, long time, String stickerPackID) {
        this.path = path;
        this.folderPath = folderPath;
        this.type = type;
        this.time = time;
        this.stickerPackID = stickerPackID;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getStickerPackID() {
        return stickerPackID;
    }

    public void setStickerPackID(String stickerPackID) {
        this.stickerPackID = stickerPackID;
    }
}
