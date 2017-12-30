package com.liujian.wechatjumphelper.util;

import android.util.Log;

import com.liujian.wechatjumphelper.BuildConfig;

/**
 * Created by liujian on 2017/12/31.
 */

public class LogUtil {

    private static final boolean DEBUG = BuildConfig.DEBUG;

    public static final String TAG = "LJTAG";

    public static void v(String msg) {
        if (!DEBUG) return;

        Log.v(TAG, msg);
    }

    public static void d(String msg) {
        if (!DEBUG) return;

        Log.d(TAG, msg);
    }

    public static void e(String msg) {
        if (!DEBUG) return;

        Log.e(TAG, msg);
    }

    public static void print(String msg) {
        if (!DEBUG) return;

        System.out.println(msg);
    }
}
