package com.android.puzzle.bean;


import android.graphics.Bitmap;

public class ItemBean {//拼图界面中每个碎片的集合类

    private int itemId;//拼图格的id，即图片所在位置
    private int bitmapId;//图片的id，即图片本来应在的位置id
    private Bitmap bitmap;//图片的对象

    public ItemBean() {//无参构造方法

    }

    public ItemBean(int itemId, int bitmapId, Bitmap bitmap) {//有参构造方法
        this.itemId = itemId;
        this.bitmapId = bitmapId;
        this.bitmap = bitmap;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getBitmapId() {
        return bitmapId;
    }

    public void setBitmapId(int bitmapId) {
        this.bitmapId = bitmapId;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
