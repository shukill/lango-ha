package com.xbh.simplewhiteboarddemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Region;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;

/**
 * Author: Wen Luo
 * Date: 2020/2/12 11:04
 * Email: Wen.Luo@lango-tech.cn
 */

public abstract class AEraser implements IDrawer {
    public static final String TAG = AEraser.class.getSimpleName();

    protected float mX = 0, mY = 0;

    protected int preRectIndex = 0;
    protected int eraserX = 0;
    protected int eraserY = 0;
    protected int eraserWidth = Util.ERASER_WIDTH;
    protected int eraserHeight = Util.ERASER_HEIGHT;

    protected Region eraserRegion;        //擦除的区域region
    protected Bitmap eraserIcon;
    protected Context context;
    protected ArrayList<Rect> mLastRectList;        //每次移动生成的rect用于绘制

    protected abstract void updateEraserIcon();

    Paint paint;

    public AEraser(@NonNull Context context) {
        this.context = context;

        mLastRectList = new ArrayList<>();
        eraserRegion = new Region();
        paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    @Override
    public void draw(DrawSurfaceView drawSurfaceView) {
        if (drawSurfaceView == null && mLastRectList != null && !mLastRectList.isEmpty()) {
            return;
        }

        long start = System.currentTimeMillis();
        Canvas drawCanvas = drawSurfaceView.getDrawCanvas();

        if (drawCanvas == null) {
            return;
        }
        Rect union = new Rect();
        for (int i = preRectIndex; i < mLastRectList.size(); i++) {
            Rect r = mLastRectList.get(i);
            union.union(r);
            drawCanvas.drawBitmap(drawSurfaceView.getBgBitmap(), r, r, null);
            //显示板叉的最后一桢，即橡皮擦
            if (i == mLastRectList.size() - 1) {
                if (eraserIcon != null && !eraserIcon.isRecycled()) {
                    drawCanvas.drawBitmap(eraserIcon, null, r, null);
                }
                preRectIndex = i;
            }
        }
        drawSurfaceView.refreshCache(union);

        Log.d(TAG, "eraser draw= " + (System.currentTimeMillis() - start)+" refresh="+union);
    }


    public void setEraserWidthHeight(float width) {
        if (width > Util.ERASER_VIEW_TOUCH_WIDTH) {
            eraserWidth = (int) (width * Util.ERASER_VIEW_AMP_FACTOR);
            if (eraserWidth > Util.ERASER_MAX_WIDTH) {
                eraserWidth = Util.ERASER_MAX_WIDTH;
            }
            eraserHeight = eraserWidth / 2 * 3;
        } else {
            eraserWidth = Util.ERASER_WIDTH;
            eraserHeight = Util.ERASER_HEIGHT;
        }
    }

    public void setEraserWidthHeight(int eraserWidth, int eraserHeight) {
        this.eraserWidth = eraserWidth;
        this.eraserHeight = eraserHeight;
    }

    public int getEraserWidth() {
        return eraserWidth;
    }

    public int getEraserHeight() {
        return eraserHeight;
    }


    @Override
    public void touchDown(float x, float y) {
        clear();
        Rect r = new Rect();
        eraserX = (int) x - eraserWidth / 2;
        eraserY = (int) y - eraserHeight / 2;
        createRect(eraserX, eraserY, eraserWidth, eraserHeight, r);
        mLastRectList.add(r);
        eraserRegion.union(r);
        mX = x;
        mY = y;
    }

    @Override
    public boolean touchMove(float x, float y) {
        collectRects(x, y);
        mX = x;
        mY = y;
        return true;
    }

    @Override
    public boolean touchUp(float x, float y, DrawSurfaceView drawSurfaceView) {
        Rect r = getCurrRect();
        if (r != null && drawSurfaceView != null) {
            //清除板擦
            drawSurfaceView.getDrawCanvas().drawRect(r, paint);
        }
        return false;
    }

    public ArrayList<Rect> getEraserRectList() {
        return mLastRectList;
    }

    @Override
    public Rect getCurrRect() {
        return mLastRectList != null && !mLastRectList.isEmpty() ? mLastRectList.get(mLastRectList.size() - 1) : null;
    }

    public Region getEraserRegion() {
        return eraserRegion;
    }

    public void clear() {
        preRectIndex = 0;
        mLastRectList.clear();
        eraserRegion.setEmpty();
    }

    protected void collectRects(float x, float y) {
        //mLastRectFList.clear();
        Rect curRect;
        double disX = (double) (x - mX);
        double disY = (double) (y - mY);
        //double distance1 = Math.abs(disX);//Math.sqrt((disX * disX) + (disY * disY));
        double distance = 0;
        double distance1 = Math.abs(disX);//sqrt((disX * disX) + (disY * disY));
        double distance2 = Math.abs(disY);

        int pointCount = 0;
        if (distance1 > eraserWidth / 2) {
            distance = distance1;
            pointCount = ((int) ((distance - eraserWidth / 2) / (eraserWidth / 4))) + 1;
        } else if (distance2 > eraserHeight / 2) {
            distance = distance2;
            pointCount = ((int) ((distance - eraserHeight / 2) / (eraserHeight / 4))) + 1;
        }

        if (distance > 0) {
            //int pointCount = ((int)((distance - eraserWidth/2)/(eraserWidth/4))) + 1;
            int deltaX = (int) disX / pointCount;
            int deltaY = (int) disY / pointCount;
            int tempX = (int) mX + deltaX;
            int tempY = (int) mY + deltaY;
            for (int i = 0; i < pointCount; i++) {
                eraserX = tempX - eraserWidth / 2;
                eraserY = tempY - eraserHeight / 2;
                curRect = new Rect();
                createRect(eraserX, eraserY, eraserWidth, eraserHeight, curRect);
                mLastRectList.add(curRect);
                eraserRegion.union(curRect);
                tempX += deltaX;
                tempY += deltaY;
            }
        }
        curRect = new Rect();
        eraserX = (int) x - eraserWidth / 2;
        eraserY = (int) y - eraserHeight / 2;
        createRect(eraserX, eraserY, eraserWidth, eraserHeight, curRect);
        mLastRectList.add(curRect);
        eraserRegion.union(curRect);
    }

    protected void createRect(int x, int y, int width, int height, Rect rect) {
        // 临界条件的处理
        if (x + width <= width) {
            x = 0;
        }

        if (y + height <= height) {
            y = 0;
        }

        if (x + width >= Util.SCREEN_WIDTH) {
            x = Util.SCREEN_WIDTH - width;
        }

        if (y + height >= Util.SCREEN_HEIGHT) {
            y = Util.SCREEN_HEIGHT - height;
        }

        rect.set(x, y, x + width, y + height);
    }
}
