package com.xbh.simplewhiteboarddemo;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.graphics.Rect;
import android.os.Handler;

import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.xbh.whiteboard.AccelerateDraw;

import java.lang.reflect.Method;


/**
 * @author LANGO
 */
public class MainActivity extends AppCompatActivity {
    private static String TAG = "AAA-MainActivity";
    private AccelerateDraw mAcd = AccelerateDraw.getInstance();
    private DrawSurfaceView drawSurfaceView;
    private Button mBt0;
    private Button mBt1;
    private Button mBt2;
    private TextView tv;


    public static MainActivity mainActivity;

    public static MainActivity getMainActivity() {
        return mainActivity;
    }

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);
        mainActivity = this;

        Log.d(TAG, "onCreate");
        drawSurfaceView = findViewById(R.id.drawSv);

        tv = findViewById(R.id.tv_version);
        tv.setText(mAcd.getVersion());

        mBt0 = findViewById(R.id.bt_pen);
        mBt0.setOnClickListener(new BtCheckListener());

        mBt1 = findViewById(R.id.bt_eraser);
        mBt1.setOnClickListener(new BtCheckListener());

        mBt2 = findViewById(R.id.bt_clean);
        mBt2.setOnClickListener(new BtCheckListener());

    }

    private class BtCheckListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.bt_pen:
                    Log.d(TAG, "pencil is click");
                    Util.setMode(Util.PEN);
                    break;
                case R.id.bt_eraser:
                    Log.d(TAG, "eraser is click");
                    Util.setMode(Util.ERASER);
                    break;
                case R.id.bt_clean:
                    Log.d(TAG, "clean is click");
                    drawSurfaceView.cleanSurfaceView();
                    break;
                default:
                    break;
            }
        }
    }

    public static boolean startAppByPackageName(Activity context, String packageName, boolean isFreedom) {
        if (context == null || TextUtils.isEmpty(packageName)) {
            return false;
        }
        boolean result = false;
        try {
            PackageManager packageManager = context.getPackageManager();
            Intent intent = packageManager.getLaunchIntentForPackage(packageName);
            Log.d(TAG, "startAppByPackageName packageName=" + packageName + ",intent=" + intent);
            if (intent != null) {
                int WINDOWING_MODE_FREEFORM = 5;
//获取屏幕高宽
                DisplayMetrics metric = new DisplayMetrics();
                context.getWindowManager().getDefaultDisplay().getMetrics(metric);
                int screenWidth = metric.widthPixels;
                int screenHeight = metric.heightPixels;

                intent.setFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT | Intent.FLAG_ACTIVITY_NEW_TASK);
                ActivityOptions activityOptions = ActivityOptions.makeBasic();
//设置为freeform模式
                try {
                    Method method = ActivityOptions.class.getMethod("setLaunchWindowingMode", int.class);
                    method.invoke(activityOptions, WINDOWING_MODE_FREEFORM);
                } catch (Exception e) {
                    e.printStackTrace();
                }
//freeform模式下自由窗口的大小
                int freeformWidth = 1000;
                int freeformHeight = 800;
//居中显示
                int left = screenWidth / 2 - freeformWidth / 2;
                int top = screenHeight / 2 - freeformHeight / 2;
                int right = screenWidth / 2 + freeformWidth / 2;
                int bottom = screenHeight / 2 + freeformHeight / 2;
                activityOptions.setLaunchBounds(new Rect(left,top,right,bottom));
                Bundle bundle = activityOptions.toBundle();
                context.startActivity(intent,bundle);

                result = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public void releaseResource(Bitmap bitmap) {
        Bitmap tempBitmap = bitmap;
        if (tempBitmap != null && !tempBitmap.isRecycled()) {
            tempBitmap.recycle();
            tempBitmap = null;
        }
    }


}
