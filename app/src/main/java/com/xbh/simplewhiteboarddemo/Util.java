package com.xbh.simplewhiteboarddemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.IntDef;

import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


public class Util {
    public static final float TOUCH_TOLERANCE = 1;

    //560为1080P，其他为4K
    public static int SCREEN_WIDTH  = 3840;//1920;
    public static int SCREEN_HEIGHT = 2160;//1080;


    /**
     * point status
     * NONE 0 ; do nothing
     * DOWN 1
     * MOVE 2
     * UP 3
     * ALL UP 4
     */
    public static final int NONE = 0 ; // do nothing
    public static final int DOWN = 1;
    public static final int MOVE = 2;
    public static final int UP = 3;
    public static final int ALL_UP = 4;

    public static int OVERRIDE_SCREEN_WIDTH  = 1920;
    public static int OVERRIDE_SCREEN_HEIGHT = 1080;


    //模式类型
    public static final int PEN = 0;
    public static final int ERASER = 1;
    public static final int GESTURE = 2;

    public static final int ERASER_WIDTH = 28;   //暂时跟黑板擦的宽度相差一半
    public static final int ERASER_HEIGHT = 42;   //暂时跟黑板擦的高度相差一半
    public static float ERASER_VIEW_TOUCH_WIDTH = 8;
    public static float ERASER_VIEW_TOUCH_HEIGHT = 8;
    public static float ERASER_VIEW_AMP_FACTOR = 4.0F;
    public static final int ERASER_MAX_WIDTH = 120;

    @IntDef({PEN, ERASER, GESTURE})
    @Retention(RetentionPolicy.SOURCE)
    public static @interface MODE {
    }

    public static int currentMode;
    public static void setMode(@MODE int mode) {
        currentMode = mode;
    }

    @MODE
    public static int getMode() {
        return currentMode;
    }

    public static boolean INPUTEVENT_DRAW = false;

    /**
     * 根据选中的背景图的位置，返回背景图的bitmap对象
     *
     * @param context  上下文
     * @param options  编码器
     * @return
     */
    public static Bitmap returnBgBitmap(Context context, @IdRes int id, BitmapFactory.Options options) {
        if (options == null) {
            options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.outWidth  = Util.SCREEN_WIDTH;
            options.outHeight = Util.SCREEN_HEIGHT;
        }
        //获取资源图片
        InputStream is = context.getResources().openRawResource(id);
        return BitmapFactory.decodeStream(is, null, options);
    }

    /**
     * @param reqWidth
     * @param reqHeight
     * @return Bitmap
     * @description 从SD卡上加载图片
     * @author zhouyang
     */
    public static Bitmap returnBgBitmap(Context context, @DrawableRes int resId, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), resId);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight, true);
        options.inJustDecodeBounds = false;
        Bitmap src = BitmapFactory.decodeResource(context.getResources(), resId);
        return createScaleBitmap(src, reqWidth, reqHeight, options.inSampleSize);
    }

    /**
     * @param src
     * @param dstWidth
     * @param dstHeight
     * @return
     * @description 通过传入的bitmap，进行压缩，得到符合标准的bitmap
     */
    private static Bitmap createScaleBitmap(Bitmap src, int dstWidth, int dstHeight, int inSampleSize) {
        // 如果是放大图片，filter决定是否平滑，如果是缩小图片，filter无影响，我们这里是缩小图片，所以直接设置为false
        Bitmap dst = Bitmap.createScaledBitmap(src, dstWidth, dstHeight, true);
        if (src != dst) { // 如果没有缩放，那么不回收
            src.recycle(); // 释放Bitmap的native像素数组
        }
        return dst;
    }

    /**
     * 计算宽高比
     *
     * @param options   配置
     * @param reqWidth  需要得到的宽
     * @param reqHeight 需要得到的高
     * @return 宽高比
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight
            , boolean isLargeRate) {
        // 源图片的高度和宽度
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            // 计算出实际宽高和目标宽高的比率
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            if (isLargeRate) {
                // 选择宽和高中最大的比率作为inSampleSize的值
                inSampleSize = heightRatio > widthRatio ? heightRatio : widthRatio;
            } else {
                // 选择宽和高中最小的比率作为inSampleSize的值
                inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
            }
        }
        return inSampleSize;
    }
}
