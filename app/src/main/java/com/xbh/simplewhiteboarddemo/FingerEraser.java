package com.xbh.simplewhiteboarddemo;

import android.content.Context;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;

/**
 * Author: Wen Luo
 * Date: 2020/2/1211:04
 * Email: Wen.Luo@lango-tech.cn
 *
 * 手指擦除模式，擦除面积不变
 */

public class FingerEraser extends AEraser {
    public FingerEraser(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void updateEraserIcon() {
        if(eraserIcon == null) {
            eraserIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.eraser_bg);
        }
    }
}
