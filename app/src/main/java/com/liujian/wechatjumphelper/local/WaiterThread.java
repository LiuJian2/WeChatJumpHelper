package com.liujian.wechatjumphelper.local;

import android.graphics.Bitmap;
import android.graphics.Point;

import com.liujian.wechatjumphelper.local.inject.InjectManager;
import com.liujian.wechatjumphelper.local.screen.ScreenManager;
import com.liujian.wechatjumphelper.util.Constants;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by liujian on 2017/12/31.
 */

public class WaiterThread extends Thread {

    private Socket mClientSocket;

    private BufferedOutputStream mBufferedOutputStream;
    private BufferedReader mBufferedReader;


    public WaiterThread(Socket socket) {
        mClientSocket = socket;
        try {
            mBufferedOutputStream = new BufferedOutputStream(mClientSocket.getOutputStream());
            mBufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        if (mClientSocket == null || mBufferedOutputStream == null || mBufferedReader == null) {
            return;
        }

        while (true) {
            String commond;
            try {
                commond = mBufferedReader.readLine();
                if (commond == null) {
                    return;
                }
                System.out.println("read commond " + commond);
                if (commond.startsWith(Constants.ACTION_DOWN)) {
                    hanlerDown(commond.substring(Constants.ACTION_DOWN.length()));
                } else if (commond.startsWith(Constants.ACTION_MOVE)) {
                    hanlerMove(commond.substring(Constants.ACTION_MOVE.length()));
                } else if (commond.startsWith(Constants.ACTION_UP)) {
                    handlerUp(commond.substring(Constants.ACTION_UP.length()));
                } else if (commond.startsWith(Constants.TAKE_PIC)) {
                    Bitmap bitmap = ScreenManager.screenshot();
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                    mBufferedOutputStream.write(2);
                    writeInt(mBufferedOutputStream, byteArrayOutputStream.size());
                    mBufferedOutputStream.write(byteArrayOutputStream.toByteArray());
                    mBufferedOutputStream.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void hanlerDown(String nums) {
        Point point = getCommondPoint(nums);
        if (point == null) return;

        InjectManager.touchDown(point.x, point.y);
    }

    private void hanlerMove(String nums) {
        Point point = getCommondPoint(nums);
        if (point == null) return;

        InjectManager.touchMove(point.x, point.y);
    }

    private void handlerUp(String nums) {
        Point point = getCommondPoint(nums);
        if (point == null) return;

        InjectManager.touchUp(point.x, point.y);
    }

    private static Point getCommondPoint(String nums) {
        try {
            Point point = new Point();
            String[] s = nums.split("#");
            point.x = Integer.valueOf(s[0]);
            point.y = Integer.valueOf(s[1]);
            return point;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void writeInt(OutputStream outputStream, int v) throws IOException {
        outputStream.write(v >> 24);
        outputStream.write(v >> 16);
        outputStream.write(v >> 8);
        outputStream.write(v);
    }
}
