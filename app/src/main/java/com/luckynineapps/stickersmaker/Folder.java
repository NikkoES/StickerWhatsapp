package com.luckynineapps.stickersmaker;

import java.util.ArrayList;

public class Folder {
    private String path = "";
    ArrayList<FileItem> files;

    public Folder() {
        files = new ArrayList<>();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public ArrayList<FileItem> getFiles() {
        return files;
    }

    public void setFiles(ArrayList<FileItem> files) {
        this.files = files;
    }
}
