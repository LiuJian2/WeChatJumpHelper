package com.liujian.wechatjumphelper.local.screen;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.os.Build;
import android.os.IBinder;
import android.view.IWindowManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by liujian on 2017/12/31.
 */

public class ScreenManager {

    public static void init() throws ClassNotFoundException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Method getServiceMethod = Class.forName("android.os.ServiceManager").getDeclaredMethod("getService", new Class[]{String.class});
        sIWindowManager = IWindowManager.Stub.asInterface((IBinder) getServiceMethod.invoke(null, new Object[]{"window"}));
    }

    private static IWindowManager sIWindowManager;

    public static Bitmap screenshot() throws Exception {
        String surfaceClassName;
        Point size = SurfaceControlVirtualDisplayFactory.getCurrentDisplaySize(false);
        Bitmap b = null;
        if (Build.VERSION.SDK_INT <= 17) {
            surfaceClassName = "android.view.Surface";
        } else {
            surfaceClassName = "android.view.SurfaceControl";
        }
        b = (Bitmap) Class.forName(surfaceClassName).getDeclaredMethod("screenshot", new Class[]{Integer.TYPE, Integer.TYPE}).invoke(null, new Object[]{Integer.valueOf(size.x), Integer.valueOf(size.y)});

        int rotation = sIWindowManager.getRotation();

        if (rotation == 0) {
            return b;
        }

        Matrix m = new Matrix();
        if (rotation == 1) {
            m.postRotate(-90.0f);
        } else if (rotation == 2) {
            m.postRotate(-180.0f);
        } else if (rotation == 3) {
            m.postRotate(-270.0f);
        }
        return Bitmap.createBitmap(b, 0, 0, size.x, size.y, m, false);
    }
}
