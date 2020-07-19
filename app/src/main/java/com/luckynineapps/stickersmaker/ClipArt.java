package com.luckynineapps.stickersmaker;

import java.io.Serializable;

public class ClipArt implements Serializable {
    String path = "";

    public ClipArt(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
