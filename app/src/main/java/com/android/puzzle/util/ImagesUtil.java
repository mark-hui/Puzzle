package com.android.puzzle.util;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import com.android.puzzle.PuzzleMain;
import com.android.puzzle.R;
import com.android.puzzle.bean.ItemBean;


public class ImagesUtil {

    public ItemBean itemBean;

    //将图片切割成碎片的方法
    public void createInitBitmaps(int type, Bitmap picSelected, Context context) {
        Bitmap bitmap = null;//碎片图
        int itemWidth = picSelected.getWidth() / type;
        int itemHeight = picSelected.getHeight() / type;
        for (int i =1; i <= type; i++) {
            for (int j = 1; j <= type; j++) {
                bitmap = Bitmap.createBitmap(
                        picSelected,//图片源
                        (j - 1) * itemWidth,
                        (i - 1) * itemHeight,//XY轴截取的起点，以左上角为原点计算
                        itemWidth,
                        itemHeight);//然后共截取多长
                itemBean = new ItemBean(
                        (i - 1) * type + j,
                        (i - 1) * type + j,
                        bitmap);//为每个碎片新建一个碎片对象
                GameUtil.mItemBeans.add(itemBean);//加入碎片对象集合
            }
        }
        //将碎片对象集合中最后一张图对象替换成空白图对象
        PuzzleMain.mLastBitmap = GameUtil.mItemBeans.get(type * type - 1).getBitmap();
        GameUtil.mItemBeans.remove(type * type - 1);
        Bitmap blankBitmap = BitmapFactory.decodeResource(
                context.getResources(), R.drawable.blank);
        blankBitmap = Bitmap.createBitmap(blankBitmap, 0, 0, itemWidth, itemHeight);
        GameUtil.mItemBeans.add(new ItemBean(type * type, 0, blankBitmap));
        GameUtil.mBlankItemBean = GameUtil.mItemBeans.get(type * type - 1);
    }

    //调整图片大小，将相机或相册或默认图缩放至跟屏幕大小合适的大小，方便后面切割
    public Bitmap resizeBitmap(float newWidth, float newHeight, Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.postScale(newWidth / bitmap.getWidth(), newHeight / bitmap.getHeight());
        return Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);//整图缩放
    }
}
