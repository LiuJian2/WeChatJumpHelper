package com.liujian.wechatjumphelper.local;

import com.liujian.wechatjumphelper.local.inject.InjectManager;
import com.liujian.wechatjumphelper.local.screen.ScreenManager;
import com.liujian.wechatjumphelper.util.Constants;
import com.liujian.wechatjumphelper.util.LogUtil;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by liujian on 2017/12/31.
 * 通过Root权限 或者 Adb运行
 * 在手机中起一个本地Socket服务器
 * 同时负责Hook 触摸事件 和 获取屏幕截图
 */

public class JumpLocalService {

    public static void main(String[] args) {
        JumpLocalService jumpLocalService = new JumpLocalService();
        jumpLocalService.startService();
    }

    ServerSocket mServerSocket;

    public void startService() {
        try {
            // 初始化输入 和 截屏Manager
            InjectManager.init();
            ScreenManager.init();
            mServerSocket = new ServerSocket(Constants.SOCKET_PORT);
            LogUtil.print("Start JumpLocalService success, waiting client...");
            waitClient();
        } catch (Exception e) {
            LogUtil.print("Start JumpLocalService Failed, caused " + e.getMessage());
        }
    }


    public void waitClient() {
        while (true) {
            try {
                Socket clientSocket = mServerSocket.accept();
                LogUtil.print("Find a client, read to server...");
                new WaiterThread(clientSocket).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
