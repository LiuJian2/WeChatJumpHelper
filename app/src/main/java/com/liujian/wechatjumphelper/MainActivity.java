package com.liujian.wechatjumphelper;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.liujian.wechatjumphelper.util.Constants;
import com.liujian.wechatjumphelper.util.ImageAnalysisUtil;
import com.liujian.wechatjumphelper.util.LogUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class MainActivity extends Activity {

    JumpThread mJumpThread;

    TextView mTvwStartJump;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 10086:
                    Toast.makeText(MainActivity.this, "连接成功, 打开微信跳一跳\n开始游戏即可", Toast.LENGTH_SHORT).show();
                    break;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTvwStartJump = findViewById(R.id.tvw_start_jump);
        findViewById(R.id.tvw_stop_jump).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mJumpThread != null) {
                    mJumpThread.mStoped = true;
                }
            }
        });
        mTvwStartJump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryStartJump();
            }
        });
    }

    private void tryStartJump() {
        if (mJumpThread != null) {
            mJumpThread.mStoped = true;
        }
        mJumpThread = new JumpThread();
        mJumpThread.start();
    }

    /**
     * 跳一跳线程
     * 运行过程:
     * 1. 拉取一帧图片
     * 2. 分析图片, 获取棋子中心和下一个出现的方块中心, 得到距离计算下一次按压时长
     * 3. 模拟按压
     * 4. 重复步骤一
     */
    class JumpThread extends Thread {
        boolean mStoped = false;
        Socket mSocket;

        BufferedInputStream mBufferedInputStream;
        BufferedOutputStream mBufferedOutputStream;
        BufferedWriter mBufferedWriter;

        @Override
        public void run() {
            try {
                mSocket = new Socket("127.0.0.1", Constants.SOCKET_PORT);
                mBufferedInputStream = new BufferedInputStream(mSocket.getInputStream());
                mBufferedOutputStream = new BufferedOutputStream(mSocket.getOutputStream());
                mBufferedWriter = new BufferedWriter(new OutputStreamWriter(mBufferedOutputStream));
                LogUtil.d("Connect 127.0.0.1:" + Constants.SOCKET_PORT + " Success!");
                mHandler.sendEmptyMessage(10086);
            } catch (Exception e) {
                LogUtil.d("Connect 127.0.0.1:" + Constants.SOCKET_PORT + " failed with excption: " + e.getMessage());
                e.printStackTrace();
                return;
            }
            int index = 0;
            while (!mStoped) {
                try {
                    Bitmap bitmap = pollImageFrame();
                    long pressTime = ImageAnalysisUtil.findNextPressTime(index, bitmap);
                    LogUtil.e("Jump press time : " + pressTime);
                    if (pressTime <= 0) {
                        Thread.sleep(1000);
                        continue;
                    }
                    index++;
                    sendPress((long) pressTime);
                    Thread.sleep(1800);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private Bitmap pollImageFrame() {
            try {
                mBufferedWriter.write(Constants.TAKE_PIC);
                mBufferedWriter.newLine();
                mBufferedWriter.flush();
                return readBitmap();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        private Bitmap readBitmap() throws IOException {
            byte[] bytes = null;
            int version = mBufferedInputStream.read();
            if (version == -1) {
                return null;
            }

            int length = readInt(mBufferedInputStream);
            if (bytes == null) {
                bytes = new byte[length];
            }
            if (bytes.length < length) {
                bytes = new byte[length];
            }

            int read = 0;
            while ((read < length)) {
                read += mBufferedInputStream.read(bytes, read, length - read);
            }
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            return bitmap;
        }

        private void sendPress(long time) throws IOException, InterruptedException {
            mBufferedWriter.write("DOWN500#500");
            mBufferedWriter.newLine();
            mBufferedWriter.flush();
            mBufferedWriter.write("MOVE500#500");
            mBufferedWriter.newLine();
            mBufferedWriter.flush();
            Thread.sleep(time);
            mBufferedWriter.write("UP500#500");
            mBufferedWriter.newLine();
            mBufferedWriter.flush();
        }
    }

    private static int readInt(InputStream inputStream) throws IOException {
        int b1 = inputStream.read();
        int b2 = inputStream.read();
        int b3 = inputStream.read();
        int b4 = inputStream.read();

        return (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
    }
}
