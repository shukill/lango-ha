package com.xbh.whiteboard;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.view.Surface;


/**
 *  @author LANGO
 */
public class AccelerateDraw {
    public static final String TAG = AccelerateDraw.class.getSimpleName();

    private static final AccelerateDraw mfbd = new AccelerateDraw();

    //防止通过单例直接调用到加速库接口
    private AccelerateDraw() {
    }

    public static AccelerateDraw getInstance() {
        return mfbd;
    }

    static {
        System.loadLibrary("AccelerateDraw");
    }

    public native String getVersion();

    //加速库初始化
    public native void accelerateInit(int w, int h);

    //加速库注销，释放资源
    public native void accelerateDeInit();

    /**
     * 开始加速，由底层加速库刷新界面
     */
    public native void startAccelerateDraw();

    /**
     * 局部刷新
     * src=(x,y,x+w,y+h);
     * dst=(x,y,x+w,y+h)
     */
    @Deprecated
    public native void refreshAccelerateDraw(int x, int y, int w, int h, Bitmap bitmap);

    /**
     * @param x       区域左上角坐标x
     * @param y       区域左上角坐标y
     * @param w       区域宽
     * @param h       区域高
     * @param bitmap  贴图的bitmap
     * @param bitmapX bitmap 区域的左上角 x
     * @param bitmapY bitmap 区域的左上角 y
     * @param isNotFilter 是否不过滤透明度为0的像素，默认为false
     *
     * 把bitmap的 指定区域 (src) 贴到屏幕的指定区域(dst)
     * src=(bitmapX,bitmapY,bitmapX+w,bitmapY+h);
     * dst=(x,y,x+w,y+h)
     */
    public native void refreshAccelerateDrawV2(int x, int y, int w, int h, Bitmap bitmap, int bitmapX, int bitmapY,boolean isNotFilter);


    //一般是 停止加速后，再延时100ms,清空加速层数据
    public void stopAndClearAccelerate(){
        stopAccelerateDraw();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        clearAccLayer();
    }

    /**
     * 停止加速，将刷新权限交回给android系统
     */
    public native boolean stopAccelerateDraw();

    /**
     * 清空 加速层 数据，置为0
     *
     */
    public native void clearAccLayer();

    /**
     * 清空 加速层 指定区域 像素，置为0
     */
    public void clearAccLayerRect(Rect rect){
        clearAccLayerRect(rect.left,rect.top,rect.width(),rect.height());
    }

    /**
     * 清空 加速层 指定区域 像素，置为0
     * @param x
     * @param y
     * @param w
     * @param h
     */
    public native void clearAccLayerRect(int x, int y, int w, int h);


    //---------  以下为过期方法，不推荐使用------
    @Deprecated
    public native void showTrackBall(boolean flag);

    /**
     * 不推荐使用，使用下面方法替代。定制性更强
     *  Canvas canvas = mSurfaceHolder.lockHardwareCanvas();
     *  canvas.drawBitmap(mCacheBitmap, null, mScreenRect, null);
     *  mSurfaceHolder.unlockHardwareCanvasAndPost(canvas);
     */
    @Deprecated
    public native void refreshSurface(Surface surface, Bitmap bitmap);

    @Deprecated
    public native void getActivityBitmap(Bitmap bitmap);

    @Deprecated
    public native void setStartEventPull(boolean flag);

    @Deprecated
    public void callback(int id, float x, float y, float width, float height, float stroke, int status, int type){
    }

}