package com.android.puzzle;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.android.puzzle.adapter.GridItemsAdapter;
import com.android.puzzle.bean.ItemBean;
import com.android.puzzle.util.GameUtil;
import com.android.puzzle.util.ImagesUtil;
import com.android.puzzle.util.ScreenUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PuzzleMain extends AppCompatActivity implements View.OnClickListener {

    public static Bitmap mLastBitmap;//最后一张图片
    public static int TYPE = 2;//难度，也作为拼图的边长使用
    public static int COUNT_INDEX = 0;//步数
    public static int TIMER_INDEX = 0;//时间

    public Bitmap mPicSelected;//调整大小后的图片，适应网格

    private Button mBtnBack;//返回按钮
    private Button mBtnImage;//原图按钮
    private Button mBtnRestart;//重开按钮

    private boolean isShowImg = false;//是否已显示原图的标记

    private GridView mGvPuzzleMainDetail;//拼图界面

    private TextView mTvPuzzleMainCounts;//步数控件
    private TextView mTvTimer;//时间

    private ImageView mImageView;//弹出原图的view

    private List<Bitmap> mBitmapItemLists = new ArrayList<>();//碎片图片的集合

    private GridItemsAdapter mAdapter;//网格布局的适配器

    private Timer mTimer;//计时器
    private TimerTask mTimerTask;//计时器线程

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    TIMER_INDEX++;
                    mTvTimer.setText("" + TIMER_INDEX);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.puzzle_detail_main);
        Bitmap picSelectedTemp;//获取选择的图片
        int mResId = getIntent().getIntExtra("picSelectedID", 0);//默认图片
        String mPicPath = getIntent().getStringExtra("mPicPath");//拍照或相册里的图片
        if (mResId != 0) {
            picSelectedTemp = BitmapFactory.decodeResource(getResources(), mResId);
        } else {
            picSelectedTemp = BitmapFactory.decodeFile(mPicPath);
        }
        TYPE = getIntent().getIntExtra("mType", 2);//获取难度，若无则为默认2
        handlerImage(picSelectedTemp);//缩放原图
        initViews();
        generateGame();
        mGvPuzzleMainDetail.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (GameUtil.isMoveable(position)) {//如果点击的碎片可以移动
                    GameUtil.swapItems(GameUtil.mItemBeans.get(position),
                            GameUtil.mBlankItemBean);//则跟空白图交换位置
                    recreateData();
                    mAdapter.notifyDataSetChanged();//更新ui
                    COUNT_INDEX++;
                    mTvPuzzleMainCounts.setText("" + COUNT_INDEX);//更新步数
                    if (GameUtil.isSuccess()) {
                        recreateData();
                        mBitmapItemLists.remove(TYPE * TYPE - 1);//移除最后一张空白图片
                        mBitmapItemLists.add(mLastBitmap);//加上有内容的最后一张图片
                        mAdapter.notifyDataSetChanged();//更新ui
                        Toast.makeText(PuzzleMain.this, "拼图成功！", Toast.LENGTH_LONG).show();
                        mGvPuzzleMainDetail.setEnabled(false);//拼图区域设置为不能点击
                        mTimer.cancel();
                        mTimerTask.cancel();//关闭计时器与其线程
                    }
                }
            }
        });
        mBtnBack.setOnClickListener(this);
        mBtnImage.setOnClickListener(this);
        mBtnRestart.setOnClickListener(this);
    }

    //下面三个按钮的点击事件
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_puzzle_main_back:
                finish();
                break;
            case R.id.btn_puzzle_main_img:
                Animation animShow = AnimationUtils.loadAnimation(
                        PuzzleMain.this, R.anim.image_show_anim);
                Animation animHide = AnimationUtils.loadAnimation(
                        PuzzleMain.this, R.anim.image_hide_anim);
                if (isShowImg) {
                    mImageView.startAnimation(animHide);
                    mImageView.setVisibility(View.GONE);
                    isShowImg = false;
                } else {
                    mImageView.startAnimation(animShow);
                    mImageView.setVisibility(View.VISIBLE);
                    isShowImg = true;
                }
                break;
            case R.id.btn_puzzle_main_restart:
                cleanConfig();
                generateGame();
                recreateData();
                mTvPuzzleMainCounts.setText("" + COUNT_INDEX);//设置步数显示为0
                mAdapter.notifyDataSetChanged();//更新ui为新的游戏
                mGvPuzzleMainDetail.setEnabled(true);//此项设置为在完成游戏后按重开可以玩新游戏
                break;
            default:
                break;
        }
    }

    //在关闭活动时结束所有设置
    @Override
    protected void onDestroy() {
        super.onDestroy();
        cleanConfig();
        finish();
    }

    //清空设置重新来和关闭活动界面的方法
    private void cleanConfig() {
        GameUtil.mItemBeans.clear();//清空此时的顺序
        mTimer.cancel();
        mTimerTask.cancel();//停止计时
        COUNT_INDEX = 0;
        TIMER_INDEX = 0;//步数时间清零
    }

    //每次交换完位置都要把最新的图片顺序集合记录
    private void recreateData() {
        mBitmapItemLists.clear();
        for (ItemBean temp : GameUtil.mItemBeans) {
            mBitmapItemLists.add(temp.getBitmap());
        }
    }

    //得到随机碎片，并开始计时
    private void generateGame() {
        //将图片分成碎片，并存到对片对象集合中
        new ImagesUtil().createInitBitmaps(TYPE, mPicSelected, PuzzleMain.this);
        GameUtil.getPuzzleGenerator();//生成有解的随机顺序，并将顺序保存到碎片对象集合中
        for (ItemBean temp : GameUtil.mItemBeans) {
            mBitmapItemLists.add(temp.getBitmap());
        }//取得乱序的碎片
        mAdapter = new GridItemsAdapter(this, mBitmapItemLists);
        mGvPuzzleMainDetail.setAdapter(mAdapter);//并将乱序碎片集合加载到网格布局中
        mTimer = new Timer(true);
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                Message msg = new Message();
                msg.what = 1;
                handler.sendMessage(msg);
            }
        };
        mTimer.schedule(mTimerTask, 0, 1000);//每1s执行，延迟0s
    }

    //初始化
    private void initViews() {
        mBtnBack = (Button) findViewById(R.id.btn_puzzle_main_back);
        mBtnImage = (Button) findViewById(R.id.btn_puzzle_main_img);
        mBtnRestart = (Button) findViewById(R.id.btn_puzzle_main_restart);
        mGvPuzzleMainDetail = (GridView) findViewById(R.id.gv_puzzle_main_detail);
        mGvPuzzleMainDetail.setNumColumns(TYPE);//设置网格布局的显示格数
        LayoutParams gridParams = new LayoutParams(
                mPicSelected.getWidth(), mPicSelected.getHeight());//设置参数长宽
        gridParams.addRule(RelativeLayout.CENTER_HORIZONTAL);//设置水平居中
        gridParams.addRule(RelativeLayout.BELOW, R.id.ll_puzzle_main_spinner);
        mGvPuzzleMainDetail.setLayoutParams(gridParams);
        mGvPuzzleMainDetail.setHorizontalSpacing(0);
        mGvPuzzleMainDetail.setVerticalSpacing(0);//设置水平和垂直间隔为0，这样更像拼图
        mTvPuzzleMainCounts = (TextView) findViewById(R.id.tv_puzzle_main_counts);
        mTvPuzzleMainCounts.setText("" + COUNT_INDEX);//设置步数
        mTvTimer = (TextView) findViewById(R.id.tv_puzzle_main_time);
        mTvTimer.setText("" + TIMER_INDEX);//设置时间
        addImgView();
    }

    //添加显示原图的view
    private void addImgView() {
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(
                R.id.rl_puzzle_main_main_layout);
        mImageView = new ImageView(PuzzleMain.this);
        mImageView.setImageBitmap(mPicSelected);
        int x = (int) (mPicSelected.getWidth() * 0.9f);
        int y = (int) (mPicSelected.getHeight() * 0.9f);
        LayoutParams params = new LayoutParams(x, y);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);//居中显示
        mImageView.setLayoutParams(params);
        relativeLayout.addView(mImageView);
        mImageView.setVisibility(View.GONE);//一开始隐藏
    }

    //将原图缩放至适应屏幕大小，适应网格
    private void handlerImage(Bitmap bitmap) {
        if (bitmap != null) {
            int screenWidth = ScreenUtil.getScreenSize(this).widthPixels;
            int screenHeight = ScreenUtil.getScreenSize(this).heightPixels;
            mPicSelected = new ImagesUtil().resizeBitmap(
                    screenWidth * 0.8f, screenHeight * 0.6f, bitmap);
        } else {
            Toast.makeText(this, "获取图片失败！", Toast.LENGTH_SHORT).show();
        }
    }
}
