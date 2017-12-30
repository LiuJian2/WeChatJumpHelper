package com.liujian.wechatjumphelper.local.inject;

import android.hardware.input.InputManager;
import android.os.SystemClock;
import android.support.v4.view.InputDeviceCompat;
import android.view.InputEvent;
import android.view.MotionEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by liujian on 2017/12/30.
 */
public class InjectManager {

    private static InputManager im;
    private static Method injectInputEventMethod;

    private static long downTime;

    public static void init() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        im = (InputManager) InputManager.class.getDeclaredMethod("getInstance", new Class[0]).invoke(null, new Object[0]);
        MotionEvent.class.getDeclaredMethod("obtain", new Class[0]).setAccessible(true);
        injectInputEventMethod = InputManager.class.getMethod("injectInputEvent", new Class[]{InputEvent.class, Integer.TYPE});
    }

    public static void injectMotionEvent(InputManager im, Method injectInputEventMethod, int inputSource, int action, long downTime, long eventTime, float x, float y, float pressure) throws InvocationTargetException, IllegalAccessException {
        MotionEvent event = MotionEvent.obtain(downTime, eventTime, action, x, y, pressure, 1.0f, 0, 1.0f, 1.0f, 0, 0);
        event.setSource(inputSource);
        injectInputEventMethod.invoke(im, new Object[]{event, Integer.valueOf(0)});
    }

    public static void touchUp(float clientX, float clientY) {
        System.out.println("touchUp " + clientX + " " + clientY);
        try {
            injectMotionEvent(im, injectInputEventMethod, InputDeviceCompat.SOURCE_TOUCHSCREEN, MotionEvent.ACTION_UP, downTime, SystemClock.uptimeMillis(), clientX, clientY, 1.0f);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void touchMove(float clientX, float clientY) {
        System.out.println("touchMove " + clientX + " " + clientY);
        try {
            injectMotionEvent(im, injectInputEventMethod, InputDeviceCompat.SOURCE_TOUCHSCREEN, MotionEvent.ACTION_MOVE, downTime, SystemClock.uptimeMillis(), clientX, clientY, 1.0f);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void touchDown(float clientX, float clientY) {
        try {
            System.out.println("touchDown " + clientX + " " + clientY);
            downTime = SystemClock.uptimeMillis();
            injectMotionEvent(im, injectInputEventMethod, InputDeviceCompat.SOURCE_TOUCHSCREEN, 0, downTime, downTime, clientX, clientY, 1.0f);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
