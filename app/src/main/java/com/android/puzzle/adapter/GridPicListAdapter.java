package com.android.puzzle.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.android.puzzle.util.ScreenUtil;

import java.util.List;


public class GridPicListAdapter extends BaseAdapter {

    //网格图像的集合
    private List<Bitmap> picList;
    private Context context;

    //获取网格数量
    @Override
    public int getCount() {
        return picList.size();
    }

    //构造函数
    public GridPicListAdapter(Context context, List<Bitmap> picList) {
        this.picList = picList;
        this.context = context;
    }

    //获取每个子项内容
    @Override
    public Bitmap getItem(int position) {
        return picList.get(position);
    }

    //获取子项的位置
    @Override
    public long getItemId(int position) {
        return position;
    }

    //创建子项的方法
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView iv_pic_item;
        int density = (int) ScreenUtil.getDeviceDensity(context);//获取屏幕密度
        if (convertView == null) {//没加载过此子项
            iv_pic_item = new ImageView(context);
            //设定图片的参数（长宽的参数80 * 100）
            iv_pic_item.setLayoutParams(new GridView.LayoutParams(80 * density, 100 * density));
            //设定图片显示比例类型，FIT_XY为对图片XY轴单独缩放，图片整体比例会有改变
            iv_pic_item.setScaleType(ImageView.ScaleType.FIT_XY);
        } else {
            iv_pic_item = (ImageView) convertView;//加载缓存
        }
        iv_pic_item.setBackgroundColor(Color.BLACK);//背景色为黑色
        //设置图片内容，前面都为设置参数并没有设置图片内容
        iv_pic_item.setImageBitmap(picList.get(position));
        return iv_pic_item;
    }
}
