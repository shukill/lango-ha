package com.xbh.annotation;

import android.graphics.Path;
import android.graphics.PointF;
import android.os.SystemClock;

public class ActPath {
        public Path path;
        public long time;
        public PointF startPoint;
        public PointF endPoint;

        public ActPath(Path path, PointF startPoint, PointF endPoint) {
            this.path = path;
            this.time = SystemClock.uptimeMillis();
            this.startPoint = startPoint;
            this.endPoint = endPoint;
        }
    }