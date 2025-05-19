package com.xbh.annotation;

import android.app.Service;
import android.content.Intent;
import android.graphics.Rect;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.xbh.dispatch.InputDispatchController;

/**
 * 示例 批注
 */
public class AnnotationService extends Service {
    private static final String TAG = "AnnotationService";

    private static final String KEY_IS_SHOW = "isShowAnnotation";
    private AnnotationWindow window;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        initInkWindow();

    }

    private void initInkWindow() {
        window=new AnnotationWindow(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent==null) return START_STICKY;
        Log.d(TAG, "onStartCommand intent ="+intent );
        boolean isShow=intent.getBooleanExtra(KEY_IS_SHOW,true);
        if(isShow){
            showWindow();
        }else{
            hideWindow();
        }
        return START_STICKY;
    }

    private void hideWindow() {
        Log.d(TAG,"hideInkWindow ");
        if(window!=null && window.isViewVisible())
            window.hide();
        InputDispatchController.clear();

    }

    private void showWindow() {
        Log.d(TAG, "showInkWindow " + window + " ");
        if (window != null && !window.isViewVisible()) {
            window.show();
            Log.d(TAG, "showInkWindow window.show()");
        }
        InputDispatchController.setControlSurfaceName(Constants.DRAW_SURFACE_NAME);
        InputDispatchController.setControllerDispatchType(InputDispatchController.DispatchType.NORMAL_TOUCH);
        InputDispatchController.setAlwaysDispatchArea(new Rect(0,2000,3840,2160));
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
