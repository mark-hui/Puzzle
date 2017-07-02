package com.android.puzzle;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.puzzle.adapter.GridPicListAdapter;
import com.android.puzzle.util.ScreenUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    PopupWindow mPopupWindow;//选择难度的弹窗
    View mPopupView;//弹窗之中的控件
    GridView mGvPicList;//网格控件的对象
    int[] mResPicId;//图片资源的id数组
    List<Bitmap> mPicList;//图片的对象的集合

    private static final int RESULT_IMAGE = 1;//本地图库请求码
    private static final String IMAGE_TYPE = "image/*";//MIME的type类型，格式为xxx/xxx
    private static final int RESULT_CAMERA = 2;//相机请求码

    private int mType = 2;//游戏难度

    private String[] mCustomItems = new String[]{"本地图册", "相机拍照"};//点最后一张图片出现的选项

    private TextView mTvPuzzleMainTypeSelected;//选择难度的文字控件

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPicList = new ArrayList<>();
        initViews();
        //设置网格布局的匹配器
        mGvPicList.setAdapter(new GridPicListAdapter(MainActivity.this, mPicList));
        //设置网格布局的点击事件
        mGvPicList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == mResPicId.length - 1) {//若点最后一张图片
                    showDialogCustom();//则打开选择本地或拍照取图功能
                } else {//若不是则是选择默认提供的图片
                    Intent intent = new Intent(MainActivity.this, PuzzleMain.class);
                    intent.putExtra("picSelectedID", mResPicId[position]);//运送选择的默认图资源id
                    intent.putExtra("mType", mType);//运送选择的难度
                    startActivity(intent);//启动意图intent
                }
            }
        });
        mTvPuzzleMainTypeSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupShow(v);
            }
        });
    }

    private void showDialogCustom() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("选择：");
        builder.setItems(mCustomItems, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        takeSDCardPhoto();//取本地相册的图片
                        break;
                    case 1:
                        takeCameraPhoto();//拍照得到图片
                        break;
                    default:
                        break;
                }
            }
        });
        builder.create().show();
    }

    //取本地相册方法，获取运行时权限
    private void takeSDCardPhoto() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.
                PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new
                    String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE }, 1);
        } else {
            openAlbum();//已经获取了权限则运行打开相册方法
        }
    }

    //打开相册方法
    private void openAlbum() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(IMAGE_TYPE);
        startActivityForResult(intent, RESULT_IMAGE);
    }

    //判断是否允许了权限方法
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.
                        PERMISSION_GRANTED) {
                    openAlbum();
                } else {
                    Toast.makeText(this, "你没有允许权限！", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    //拍照的方法
    private void takeCameraPhoto() {
        File photoImage = new File(getExternalCacheDir(), "temp.jpg");
        Uri photoUri;//相机拍的照片的地址URI
        try {
            if (photoImage.exists()) {
                photoImage.delete();
            }
            photoImage.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT >= 24) {
            photoUri = FileProvider.getUriForFile(MainActivity.this,
                    "com.android.puzzle.fileprovider", photoImage);
        } else {
            photoUri = Uri.fromFile(photoImage);
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        startActivityForResult(intent, RESULT_CAMERA);
    }

    //回调从相册或者拍照得到的图片
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RESULT_CAMERA:
                if (resultCode == RESULT_OK) {
                    Intent intent = new Intent(MainActivity.this, PuzzleMain.class);
                    intent.putExtra("mPicPath", getExternalCacheDir() + "/temp.jpg");
                    intent.putExtra("mType", mType);
                    startActivity(intent);
                }
                break;
            case RESULT_IMAGE:
                if (resultCode == RESULT_OK) {
                    if (Build.VERSION.SDK_INT >= 19) {
                        handleImageOnKitKat(data);//手机版本至少为4.4运行这方法
                    } else {
                        handleImageBeforeKitKat(data);//低于4.4运行这方法
                    }
                }
                break;
            default:
                break;
        }
    }

    //要api大于19即android版本高于4.4才能识别此方法，获取图片地址uri方法
    @TargetApi(19)
    private void handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            String id = docId.split(":")[1];
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.
                        parse("content://downloads/public_downloads"), Long.valueOf(id));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            imagePath = uri.getPath();
        }
        displayImage(imagePath);
    }

    //api小于19即android版本小于4.4则执行此方法，获取图片地址uri方法
    private void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        displayImage(imagePath);
    }

    //将每个方式得到的地址uri转变成String格式的路径
    private String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    //将String格式的路径传到拼图界面使用
    private void displayImage(String imagePath) {
        if (imagePath != null) {
            Intent intent = new Intent(MainActivity.this, PuzzleMain.class);
            intent.putExtra("mPicPath", imagePath);
            intent.putExtra("mType", mType);
            startActivity(intent);
        } else {
            Toast.makeText(this, "获取图片失败", Toast.LENGTH_SHORT).show();
        }
    }

    //弹出选择难度窗口方法
    private void popupShow(View view) {
        int density = (int) ScreenUtil.getDeviceDensity(this);//获取设备密度
        //设置弹窗的内容，大小
        mPopupWindow = new PopupWindow(mPopupView, 200 * density, 50 * density);
        mPopupWindow.setFocusable(true);//可获取焦点
        mPopupWindow.setOutsideTouchable(true);//点击外部可退出弹窗（只有设置了下面两句这句才会生效）
        Drawable transpent = new ColorDrawable(Color.TRANSPARENT);//设置图片样式
        mPopupWindow.setBackgroundDrawable(transpent);//设置弹窗背景为透明
        int[] location = new int[2];
        //记录控件在屏幕上的坐标，坐标是以弹窗为父布局得出的，返回为void，但传入数组中已存控件的XY坐标
        view.getLocationOnScreen(location);
        mPopupWindow.showAtLocation(
                view,//弹窗内容控件
                Gravity.NO_GRAVITY,//弹窗对齐方式，无对齐方式即为在左上
                location[0] - 40 * density,//X坐标偏移量
                location[1] + 30 * density);//Y坐标偏移量
    }

    //初始化控件
    private void initViews() {
        mGvPicList = (GridView) findViewById(R.id.gv_puzzle_main_pic_list);//与控件关联
        mResPicId = new int[]{
                R.drawable.pic1, R.drawable.pic2, R.drawable.pic3,
                R.drawable.pic4, R.drawable.pic5, R.drawable.pic6,
                R.drawable.pic7, R.drawable.pic8, R.drawable.pic9,
                R.drawable.pic10, R.drawable.pic11, R.drawable.pic12,
                R.drawable.pic13, R.drawable.pic14, R.drawable.pic15,R.drawable.plus
        };
        Bitmap[] bitmaps = new Bitmap[mResPicId.length];//创建一个跟图片id数组一样大小的图片对象数组
        for (int i = 0; i < bitmaps.length; i++) {
            //产生一个图片对象数组装有id所对应的图片对象
            bitmaps[i] = BitmapFactory.decodeResource(getResources(), mResPicId[i]);
            mPicList.add(bitmaps[i]);//将图片对象数组中对象对应装到集合中
        }
        mTvPuzzleMainTypeSelected = (TextView) findViewById(R.id.tv_puzzle_main_type_selected);
        mPopupView = LayoutInflater.from(this).
                inflate(R.layout.puzzle_main_type_selected, null);
        TextView mTvType2 = (TextView) mPopupView.findViewById(R.id.tv_main_type_2);
        mTvType2.setOnClickListener(this);
        TextView mTvType3 = (TextView) mPopupView.findViewById(R.id.tv_main_type_3);
        mTvType3.setOnClickListener(this);
        TextView mTvType4 = (TextView) mPopupView.findViewById(R.id.tv_main_type_4);
        mTvType4.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_main_type_2:
                mType = 2;
                mTvPuzzleMainTypeSelected.setText("2 × 2");
                break;
            case R.id.tv_main_type_3:
                mType = 3;
                mTvPuzzleMainTypeSelected.setText("3 × 3");
                break;
            case R.id.tv_main_type_4:
                mType = 4;
                mTvPuzzleMainTypeSelected.setText("4 × 4");
                break;
            default:
                break;
        }
        mPopupWindow.dismiss();
    }
}
