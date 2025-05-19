package com.xbh.simplewhiteboarddemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.SurfaceControl;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

import com.xbh.annotation.Constants;
import com.xbh.whiteboard.AccelerateDraw;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author LANGO
 */
public class DrawSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private final String TAG = DrawSurfaceView.class.getSimpleName();
    private final Handler handler = new Handler();
    private ViewTreeObserver.OnGlobalLayoutListener mPreDrawListener;

    private SurfaceHolder mSurfaceHolder = null;
    private Paint mPaint = null;
    private Rect mScreenRect = null;
    private Bitmap mBgBitmap = null;
    private Bitmap mCacheBitmap = null;//成熟区，保存的是已经绘制的笔迹
    private Bitmap mDrawBitmap = null;//刷新区，保存的是书写过程传给加速库的笔迹或板擦
    private Canvas mCacheCanvas = null;
    private Canvas mDrawCanvas = null;//书写画布

    private AEraser mFingerEraser;
    private AEraser mGestureEraser;

    private int preEraserPointId = -1;
    private SparseArray<IDrawer> mPencilList = new SparseArray<>();
    private static final Object PEN_LOCKER = new Object();
    private AccelerateDraw mAcd = AccelerateDraw.getInstance();

    private boolean isGetEventDraw = false;

    protected Rect drawSkipBoundsRect;
    protected List<Path> drawSkipPathList;
    private Rect mViewRect = new Rect();


    public DrawSurfaceView(Context context) {
        super(context);
    }

    public DrawSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.d(TAG, "DrawSurfaceView");
        mSurfaceHolder = this.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        mScreenRect = new Rect(0, 0, Util.SCREEN_WIDTH, Util.SCREEN_HEIGHT);

        //创建背景图
        mBgBitmap = Util.returnBgBitmap(this.getContext(), R.mipmap.canvas_bg_0, Util.SCREEN_WIDTH, Util.SCREEN_HEIGHT);
        //创建成熟区画布
        mCacheBitmap = Bitmap.createBitmap(Util.SCREEN_WIDTH, Util.SCREEN_HEIGHT, Bitmap.Config.ARGB_8888);
        mCacheBitmap.eraseColor(Color.TRANSPARENT);
        mCacheCanvas = new Canvas(mCacheBitmap);

        //创建书写区画布
        mDrawBitmap = Bitmap.createBitmap(Util.SCREEN_WIDTH, Util.SCREEN_HEIGHT, Bitmap.Config.ARGB_8888);
        mDrawBitmap.eraseColor(Color.TRANSPARENT);
        mDrawCanvas = new Canvas(mDrawBitmap);

        drawSkipBoundsRect = new Rect();


        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                onPreDraw();
            }
        };

        // PreDraw监听，用于窗口位置大小改变后刷新画布参数
        mPreDrawListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                handler.removeCallbacks(runnable);
                handler.postDelayed(runnable, 100);
            }
        };
    }

    private void onPreDraw() {
        boolean update = updateViewRect();
        if (update) {
            mAcd.stopAndClearAccelerate();
            requestCacheDraw();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalLayoutListener(mPreDrawListener);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeOnGlobalLayoutListener(mPreDrawListener);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated");
        //主进程判断，关闭加速，防止异常时未关闭加速

        AccelerateDraw.getInstance().accelerateDeInit();
        AccelerateDraw.getInstance().accelerateInit(Util.SCREEN_WIDTH, Util.SCREEN_HEIGHT);
        AccelerateDraw.getInstance().stopAndClearAccelerate();

        /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            SurfaceControl.Transaction transaction = new SurfaceControl.Transaction();
            SurfaceControl  surfaceControl = getSurfaceControl();
            Reflect.on(transaction).call("setTrustedOverlay",surfaceControl, true).call("apply");

//            SurfaceControl transaction = new SurfaceControl.Builder().setName(Constants.DRAW_SURFACE_NAME).build();
        }*/
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged width = " + width + " height=" + height);

        //适配多窗
        Util.OVERRIDE_SCREEN_WIDTH = width;
        Util.OVERRIDE_SCREEN_HEIGHT = height;
        updateViewRect();

        requestCacheDraw();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed");
        mAcd.stopAndClearAccelerate();
        mAcd.accelerateDeInit();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int toolType=event.getToolType(0);
        boolean isStylus=toolType==MotionEvent.TOOL_TYPE_STYLUS;

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                MotionEventTouchDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                MotionEventTouchMove(event);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                MotionEventTouchUp(event, false);
                break;
            case MotionEvent.ACTION_UP:
                MotionEventTouchUp(event, true);
                break;
            default:
                break;
        }
        return true;
    }

    private void MotionEventTouchDown(MotionEvent event) {
        int curPointerIndex = event.getActionIndex();
        int curPointerId = event.getPointerId(curPointerIndex);
        float x = event.getX(curPointerIndex);
        float y = event.getY(curPointerIndex);
        float width = event.getTouchMajor(curPointerIndex);

        if (Util.getMode() == Util.PEN) {
            touchDown(curPointerId, x, y);
        } else {
            if (event.getPointerCount() == 1) {
                touchDown(curPointerId, x, y);
                IDrawer drawer = toCreateDrawer(curPointerId);
                drawer.touchDown(x, y);
            }
            udateEraserIcon(curPointerId, width);
        }
    }

    //开始加速
    private void touchDown(int curPointerId, float x, float y) {
        IDrawer drawer = toCreateDrawer(curPointerId);
        mAcd.startAccelerateDraw();
        drawer.touchDown(x, y);
    }

    //更新擦除板擦UI大小
    private void udateEraserIcon(int curPointerId, float width) {
        if (Util.getMode() == Util.PEN) {
            return;
        }
        AEraser eraser = (AEraser) getDrawer(curPointerId);
        if (width > eraser.getEraserWidth()) {
            preEraserPointId = curPointerId;
            eraser.setEraserWidthHeight(width);
        }
        eraser.updateEraserIcon();
    }

    private void MotionEventTouchMove(MotionEvent event) {
        int pointerCount = event.getPointerCount();
        int curPointerIndex = 0;
        for (int i = 0; i < pointerCount; i++) {
            if (Util.getMode() == Util.PEN || preEraserPointId == event.getPointerId(i)) {
                curPointerIndex = i;
            } else {
                curPointerIndex = 0;
            }
            paintDraw(event.getPointerId(curPointerIndex), event.getX(curPointerIndex), event.getY(curPointerIndex));
        }
    }

    private void touchMove(int curPointerId, float x, float y) {
        paintDraw(curPointerId, x, y);
    }

    private void paintDraw(int curPointerId, float x, float y) {
        IDrawer drawer = getDrawer(curPointerId);
        if (drawer != null && drawer.touchMove(x, y)) {
            drawer.draw(this);
        }
    }

    private void MotionEventTouchUp(MotionEvent event, boolean releaseAll) {
        int curPointerIndex = 0;
        if (Util.getMode() == Util.PEN || preEraserPointId == event.getPointerId(event.getActionIndex())) {
            curPointerIndex = event.getActionIndex();
        } else {
            curPointerIndex = 0;
        }
        touchUp(event.getPointerId(curPointerIndex), event.getX(curPointerIndex), event.getY(curPointerIndex), releaseAll);
    }

    private void touchUp(int curPointerId, float x, float y, boolean releaseAll) {
        synchronized (PEN_LOCKER) {
            paintUp(curPointerId, x, y);
            //刷新总个界面
            if (releaseAll) {
                preEraserPointId = -1;
                clearAllDrawer();
                requestCacheDraw();
                mAcd.stopAndClearAccelerate();
            }
        }
    }

    private void paintUp(int curPointerId, float x, float y) {
        IDrawer drawer = getDrawer(curPointerId);
        if (drawer != null) {
            if (drawer.touchUp(x, y, this)) {
                drawer.draw(this);
            }
            if (Util.getMode() == Util.PEN) {
                mPencilList.remove(curPointerId);
            }
        }
    }

    /**
     * 更新View可见区域
     */
    public boolean updateViewRect() {
        int[] position = new int[2];
        getLocationOnScreen(position);
        int left = position[0];
        int top = position[1];
        int right = position[0] + getWidth();
        int bottom = position[1] + getHeight();
        if (left == mViewRect.left && top == mViewRect.top
                && right == mViewRect.right && bottom == mViewRect.bottom) {
            return false;
        }
        mViewRect.set(left, top, right, bottom);
        Log.d(TAG, "updateViewRect =" + mViewRect);
        return true;
    }

    //刷新书写内容
    public void refreshCache(Rect rect) {
        if (rect != null) {
            /*针对多窗适配*/
            int left = mViewRect.left + rect.left;
            int top = mViewRect.top + rect.top;

            //把 书写图层 传入加速库
            mAcd.refreshAccelerateDrawV2(left, top, rect.width(), rect.height(),
                    mDrawBitmap, rect.left, rect.top, false);
        }
    }


    //刷新总个屏幕
    public void requestCacheDraw() {
        synchronized (mSurfaceHolder) {

            //绘制之前，将书写图层叠加到 成熟图层
            mCacheCanvas.drawBitmap(mDrawBitmap, null, mScreenRect, null);
            mDrawBitmap.eraseColor(Color.TRANSPARENT);

            Canvas canvas = mSurfaceHolder.lockHardwareCanvas();
            if (canvas == null) return;
            if(mBgBitmap!=null)
                canvas.drawBitmap(mBgBitmap, null, mScreenRect, null);
            canvas.drawBitmap(mCacheBitmap, null, mScreenRect, null);

            mSurfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    //刷新书写画布
    public void cleanDrawCanvas() {
        Log.d(TAG, "cleanDrawCanvas");
        mDrawBitmap.eraseColor(Color.TRANSPARENT);
    }

    //清除总个界面
    public void cleanSurfaceView() {
        Log.d(TAG, "cleanSurfaceView");
        cleanDrawCanvas();
        mCacheBitmap.eraseColor(Color.TRANSPARENT);
        requestCacheDraw();
    }

    public Canvas getCacheCanvas() {
        return mCacheCanvas;
    }

    public Canvas getDrawCanvas() {
        return mDrawCanvas;
    }

    public Bitmap getBgBitmap() {
        return mBgBitmap;
    }

    public void setBgBitmap(Bitmap mBgBitmap) {
        this.mBgBitmap = mBgBitmap;
    }

    public Bitmap getSkipBitmap() {
        return mDrawBitmap;
    }

    public Bitmap getCacheBitmap() {
        return mCacheBitmap;
    }

    private IDrawer getDrawer(int pointId) {
        if (Util.getMode() == Util.ERASER) {
            return mFingerEraser;
        } else if (Util.getMode() == Util.GESTURE) {
            return mGestureEraser;
        } else {
            return mPencilList.get(pointId);
        }
    }

    private void clearAllDrawer() {
        mFingerEraser = null;
        mGestureEraser = null;
        mPencilList.clear();
    }

    private IDrawer toCreateDrawer(int pointId) {
        if (mPaint == null) {
            toCreatePaint();
        }

        IDrawer drawer = null;
        if (Util.getMode() == Util.ERASER) {
            if (mFingerEraser == null) {
                mFingerEraser = new FingerEraser(getContext());
            }
            drawer = mFingerEraser;
        } else if (Util.getMode() == Util.GESTURE) {
            if (mGestureEraser == null) {
                mGestureEraser = new GestureEraser(getContext());
            }
            drawer = mGestureEraser;
        } else {
            drawer = new Pencil(mPaint);
            mPencilList.append(pointId, drawer);
        }
        return drawer;
    }

    private void toCreatePaint() {
        mPaint = new Paint();
        float size = 5.0f;
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setColor(Color.GREEN);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(size);
    }

    public void upateDrawSkipPathRect(List<Path> drawSkipPathRect) {
        if (drawSkipPathRect == null) {
            return;
        }
        drawSkipBoundsRect.setEmpty();
        this.drawSkipPathList = drawSkipPathRect;
        for (Path skipPath : drawSkipPathList) {
            RectF rectF = new RectF();
            skipPath.computeBounds(rectF, true);
            drawSkipBoundsRect.union((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom);
        }
    }

    public Rect getDrawSkipBoundsRect() {
        return drawSkipBoundsRect;
    }


    public boolean isIntersectWithSkipBounds(int x, int y) {
        return drawSkipBoundsRect != null && drawSkipBoundsRect.contains(x, y);
    }

    public boolean isIntersectWithSkipBounds(Rect rect) {
        //不要用intersect，会修改drawSkipBoundsRect，导致区域异常;
        return drawSkipBoundsRect != null && rect != null && Rect.intersects(drawSkipBoundsRect, rect);
    }

}
