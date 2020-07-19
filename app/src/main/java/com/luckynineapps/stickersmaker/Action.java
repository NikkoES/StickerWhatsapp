package com.luckynineapps.stickersmaker;

import android.graphics.Bitmap;

public class Action {
    public static final int ADD_IMAGE = 1;
    public static final int ADD_CLIP_ART = 2;
    public static final int ADD_TEXT = 3;
    public static final int DELETE = 4;
    public static final int ROOT = 1;
    public static final int ELEMENT = 2;
    int id = 0;
    Action action;
    int type = ADD_IMAGE;
    double rotation = 0;
    // Only for ADD_IMAGE type
    Bitmap image;
    Bitmap previousImage = null;
    int x = 0;
    int y = 0;
    int width = 0;
    int height = 0;
    // Only for ADD_TEXT type
    String text = "";
    int textSize = 0;
    int textColor = 0;
    String fontPath = "";
    Bitmap bitmap;
    Bitmap previousBitmap;
    int elementType = ELEMENT;

    public Action() {}

    public Action(Bitmap b, int x, int y, int width, int height) {
        bitmap = b;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Action(int id, int type, Bitmap image, int x, int y, int width, int height) {
        this.id = id;
        this.type = type;
        this.image = image;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Action(int id, int type, String text, int textColor, int textSize, String fontPath, int x, int y, int width, int height) {
        this.id = id;
        this.type = type;
        this.text = text;
        this.textColor = textColor;
        this.textSize = textSize;
        this.fontPath = fontPath;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Action(int id, int type, Action action) {
        this.id = id;
        this.type = type;
        this.action = action;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public double getRotation() {
        return rotation;
    }

    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public String getFontPath() {
        return fontPath;
    }

    public void setFontPath(String fontPath) {
        this.fontPath = fontPath;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public Bitmap getPreviousImage() {
        return previousImage;
    }

    public void setPreviousImage(Bitmap previousImage) {
        this.previousImage = previousImage;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Bitmap getPreviousBitmap() {
        return previousBitmap;
    }

    public void setPreviousBitmap(Bitmap previousBitmap) {
        this.previousBitmap = previousBitmap;
    }

    public int getElementType() {
        return elementType;
    }

    public void setElementType(int elementType) {
        this.elementType = elementType;
    }
}
