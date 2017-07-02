package com.android.puzzle.util;


import com.android.puzzle.PuzzleMain;
import com.android.puzzle.bean.ItemBean;

import java.util.ArrayList;
import java.util.List;

public class GameUtil {

    public static List<ItemBean> mItemBeans = new ArrayList<>();//碎片对象集合
    public static ItemBean mBlankItemBean = new ItemBean();//空白碎片对象

    //是否可以移动
    public static boolean isMoveable(int position) {
        int type = PuzzleMain.TYPE;
        int blankId = GameUtil.mBlankItemBean.getItemId() - 1;//取空格所在位置id
        if (Math.abs(blankId - position) == type) {//不同行相差为type可以移动
            return true;
        } else if ((blankId / type == position / type)//同行相差为1可以移动
                && Math.abs(blankId - position) == 1) {
            return true;
        } else {
            return false;
        }
    }

    //将可移动的图与空白图的图片内容和内容id交换
    public static void swapItems(ItemBean from, ItemBean blank) {
        ItemBean tempItemBean = new ItemBean();
        tempItemBean.setBitmapId(from.getBitmapId());
        from.setBitmapId(blank.getBitmapId());
        blank.setBitmapId(tempItemBean.getBitmapId());
        tempItemBean.setBitmap(from.getBitmap());
        from.setBitmap(blank.getBitmap());
        blank.setBitmap(tempItemBean.getBitmap());
        GameUtil.mBlankItemBean = from;
    }

    //判断随机生成的排列是否有解
    public static boolean canSolve(List<Integer> data) {
        int blankId = GameUtil.mBlankItemBean.getItemId();//获取空格的位置id
        //下面为求可行性原则
        if (data.size() % 2 == 1) {//若序列宽为奇数
            return getInversions(data) % 2 == 0;//倒置和必须为偶数
        } else {
            if (((blankId - 1) / PuzzleMain.TYPE) % 2 == 1) {//从下往上数空格位于奇数行
                return getInversions(data) % 2 == 0;//倒置和必须为偶数
            } else {//位于偶数行
                return getInversions(data) % 2 == 1;//倒置和为奇数
            }
        }
    }

    //计算倒置和的方法
    public static int getInversions(List<Integer> data) {
        int inversions = 0;//倒置和
        int inversionCount = 0;//每个元素的倒置数
        for (int i = 0; i < data.size(); i++) {//i为元素角标
            for (int j = i + 1; j < data.size(); j++) {//j为i后的数的角标
                int index = data.get(i);
                if (data.get(j) != 0 && data.get(j) < index) {//非0即不是空格，且小于i数
                    inversionCount++;//每有一个符合倒置数自增
                }
            }
            inversions += inversionCount;//累积倒置数
            inversionCount = 0;//清空每轮的倒置数
        }
        return inversions;//返回倒置和
    }

    //生成随机碎片排列的方法
    public static void getPuzzleGenerator() {
        int index;
        for (int i = 0; i < mItemBeans.size(); i++) {
            //random（）方法为随机返回大于等于0且小于1的double，
            //强转成int会向下取整，所以index永不会等于TYPE*TYPE，所以下面从集合取不会溢出
            index = (int) (Math.random() * PuzzleMain.TYPE * PuzzleMain.TYPE);
            swapItems(mItemBeans.get(index), GameUtil.mBlankItemBean);//与空白图内容交换
        }
        List<Integer> data = new ArrayList<>();//乱序图片id集合
        for (int i = 0; i < mItemBeans.size(); i++) {
            data.add(mItemBeans.get(i).getBitmapId());//取乱序图片id
        }
        if (canSolve(data)) {//若有解则结束这方法
            return;
        } else {
            getPuzzleGenerator();//无解则继续递归循环这方法直到有解为止
        }
    }

    //是否拼图成功的方法
    public static boolean isSuccess() {
        for (ItemBean tempBean : GameUtil.mItemBeans) {//判断此时顺序是否正确
            if (tempBean.getBitmapId() != 0 &&//不是空格的格内容id与位置id是否一致
                    (tempBean.getItemId()) == tempBean.getBitmapId()) {
                continue;//一致则执行下一个循环
            } else if (tempBean.getBitmapId() == 0 &&//是空格的话是不是在最后一格
                    tempBean.getItemId() == PuzzleMain.TYPE * PuzzleMain.TYPE) {
                continue;//是则继续执行下一个循环
            } else {
                return false;//若有其中一种情况不是则说明还没完全复原
            }
        }
        return true;//循环全部结束都是的话证明复原成功
    }
}
