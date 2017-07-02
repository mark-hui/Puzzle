package com.android.puzzle.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.List;


public class GridItemsAdapter extends BaseAdapter {

    private List<Bitmap> mBitmapItemLists;//碎片集合
    private Context mContext;

    public GridItemsAdapter(Context context, List<Bitmap> objects) {//构造函数
        mBitmapItemLists = objects;
        mContext = context;
    }



    @Override
    public int getCount() {
        return mBitmapItemLists.size();
    }

    @Override
    public Bitmap getItem(int position) {
        return mBitmapItemLists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView iv_pic_item;
        if (convertView == null) {//设置碎片在子项中显示的参数
            iv_pic_item = new ImageView(mContext);
            iv_pic_item.setLayoutParams(new GridView.LayoutParams(
                    mBitmapItemLists.get(position).getWidth(),
                    mBitmapItemLists.get(position).getHeight()));
            iv_pic_item.setScaleType(ImageView.ScaleType.FIT_CENTER);
        } else {
            iv_pic_item = (ImageView) convertView;
        }
        iv_pic_item.setImageBitmap(mBitmapItemLists.get(position));//加载碎片
        return iv_pic_item;
    }
}
