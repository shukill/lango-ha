/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xbh.annotation;

import static android.view.WindowManager.LayoutParams;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.xbh.dispatch.InputDispatchController;
import com.xbh.simplewhiteboarddemo.DrawSurfaceView;
import com.xbh.simplewhiteboarddemo.R;

final class AnnotationWindow {

    private static final String TAG = "AnnotationWindow";
    private final WindowManager mWindowManager;
    private final Context context;
    private final View decoreView;
    private final View llTool;
    private LayoutParams params;
    private boolean mIsViewAdded;

    public AnnotationWindow(Context context) {
        this.context=context;
        decoreView = LayoutInflater.from(context).inflate(R.layout.activity_annotation, null);

        DrawSurfaceView surfaceView=decoreView.findViewById(R.id.drawSv);
        surfaceView.setBgBitmap(null);
        decoreView.setVisibility(View.GONE);

        if (params == null) {
            params = new LayoutParams();
            params.setTitle(Constants.DRAW_SURFACE_NAME);

            params.type = LayoutParams.TYPE_APPLICATION_OVERLAY;
            params.format = PixelFormat.RGBA_8888;
            params.width = LayoutParams.MATCH_PARENT;
            params.height = LayoutParams.MATCH_PARENT;

            params.flags = LayoutParams.FLAG_FULLSCREEN | LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE;

        }

        mWindowManager = context.getSystemService(WindowManager.class);

        llTool=decoreView.findViewById(R.id.ll_tool);
        decoreView.findViewById(R.id.bt_pen).setOnClickListener((v)->{
            InputDispatchController.setControllerDispatchType(InputDispatchController.DispatchType.NORMAL_TOUCH);
        });
        decoreView.findViewById(R.id.bt_control).setOnClickListener((v)->{
            Rect r=new Rect();
            llTool.getGlobalVisibleRect(r);
            InputDispatchController.setAlwaysDispatchArea(r);//set alway rect
            InputDispatchController.setControllerDispatchType(InputDispatchController.DispatchType.NO_TOUCH);
        });
        decoreView.findViewById(R.id.bt_stylus_hand).setOnClickListener((v)->{

            Rect r=new Rect();
            llTool.getGlobalVisibleRect(r);
            InputDispatchController.setAlwaysDispatchArea(r);//set alway rect
            InputDispatchController.setControllerDispatchType(InputDispatchController.DispatchType.ONLY_STYLUS);
        });
        decoreView.findViewById(R.id.bt_close).setOnClickListener((v)->{
            hide();
        });
    }

    public void show() {
        if (getDecorView() == null) {
            Log.i(TAG, "DecorView is not set for show() failed.");
            return;
        }
        getDecorView().setVisibility(View.VISIBLE);
        if (!mIsViewAdded) {
            mWindowManager.addView(getDecorView(), params);
            mIsViewAdded = true;

        }
        Log.i(TAG, "DecorView show title="+params.getTitle());

    }


    private View getDecorView() {
        return decoreView;
    }

    /**
     * Method to hide InkWindow from screen.
     * Emulates internal behavior similar to Dialog.hide().
     *
     */
    void hide() {
        if (getDecorView() != null) {
            mWindowManager.removeView(getDecorView());
            mIsViewAdded=false;
        }
    }

    boolean isViewVisible() {
        return getDecorView().getVisibility() == View.VISIBLE && getDecorView().isAttachedToWindow();
    }

}
