package com.xbh.annotation;


import android.util.Log;

import java.lang.reflect.Method;

/**
 * 系统属性工具类
 * <p>
 * 声明：使用反射访问隐藏/私有Android API并不安全; 它通常不会在其他供应商的设备上运行，
 * 它可能会突然停止工作（如果API被移除）或崩溃（如果API行为发生变化，因为无法保证兼容性）。
 *
 * @author LANGO
 * @date 2018/11/22
 */
public class SystemPropertiesUtil {

    private static final String TAG = "SystemPropertiesUtil";

    public static void set(String key, String value) {
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method set = c.getMethod("set", String.class, String.class);
            set.invoke(c, key, value);
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

    public static String get(String key) {
        Class c = null;
        String getStr = null;
        try {
            c = Class.forName("android.os.SystemProperties");
            Method method = c.getMethod("get", new Class[]{String.class});
            if (method != null) {
                getStr = (String) method.invoke(c, new Object[]{key});
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "get: Exception:" + e.getMessage());
        }
        return getStr;
    }

    public static String get(String key, String def) {
        Class c = null;
        String getStr = def;
        try {
            c = Class.forName("android.os.SystemProperties");
            Method method = c.getMethod("get", new Class[]{String.class, String.class});
            if (method != null) {
                getStr = (String) method.invoke(c, new Object[]{key, def});
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "get: Exception:" + e.getMessage());
        }
        return getStr;
    }

    public static int getInt(String key, int def) {
        Class c = null;
        int isln = def;
        try {
            c = Class.forName("android.os.SystemProperties");
            Method method = c.getMethod("getInt", new Class[]{String.class, int.class});
            if (method != null) {
                isln = (int) method.invoke(c, new Object[]{key, def});
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "getInt: Exception:" + e.getMessage());
        }
        return isln;
    }

    public static long getLong(String key, long def) {
        Class c = null;
        long isln = def;
        try {
            c = Class.forName("android.os.SystemProperties");
            Method method = c.getMethod("getLong", new Class[]{String.class, long.class});
            if (method != null) {
                isln = (long) method.invoke(c, new Object[]{key, def});
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "getLong: Exception:" + e.getMessage());
        }
        return isln;
    }

    public static boolean getBoolean(String key, boolean def) {
        Class c = null;
        boolean isbn = def;
        try {
            c = Class.forName("android.os.SystemProperties");
            Method method = c.getMethod("getBoolean", new Class[]{String.class, boolean.class});
            if (method != null) {
                isbn = (boolean) method.invoke(c, new Object[]{key, def});
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "getBoolean: Exception:" + e.getMessage());
        }
        return isbn;
    }


}
