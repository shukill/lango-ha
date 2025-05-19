package com.xbh.simplewhiteboarddemo;

import android.graphics.Rect;

/**
 * Author: Wen Luo
 * Date: 2020/2/1211:04
 * Email: Wen.Luo@lango-tech.cn
 */
public interface IDrawer {
    Rect getCurrRect();
    void touchDown(float x, float y);
    boolean touchMove(float x, float y);
    boolean touchUp(float x, float y,DrawSurfaceView drawSurfaceView);
    void draw(DrawSurfaceView drawSurfaceView);
}
