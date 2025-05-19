package com.xbh.simplewhiteboarddemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;

/**
 * Author: Wen Luo
 * Date: 2020/2/1211:04
 * Email: Wen.Luo@lango-tech.cn
 * 手势擦除模式，擦除面积随接触面积变化
 */

public class GestureEraser extends AEraser {
    private int preEraserWidth;
    private int preEraserHeight;
    private Bitmap eraserBitmap;


    public GestureEraser(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void updateEraserIcon() {
        if(preEraserWidth == eraserWidth && preEraserHeight == eraserHeight) {
            return;
        }

        if(eraserBitmap == null) {
            eraserBitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.eraser_bg);
        }

        releaseResource(eraserIcon);
        eraserIcon = Bitmap.createScaledBitmap(eraserBitmap, eraserWidth, eraserHeight, false);

        if(preEraserWidth != eraserWidth) {
            preEraserWidth = eraserWidth;
        }
        if(preEraserHeight != eraserHeight) {
            preEraserHeight = eraserHeight;
        }
    }

    private void releaseResource(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
        System.gc();
    }
}
