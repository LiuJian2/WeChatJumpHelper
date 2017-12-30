# WeChatJumpHelper
微信 跳一跳 辅助工具 Android 安卓 版本

### 使用方式

对于mac 笔记本用户：
* Android手机开启开发者选项
* 用数据线连接Android手机和mac

对于所有用户：
* Android手机开启开发者选项
* 用数据线连接Android手机和PC
* 安装app-debug.apk 或者直接用Android Studio Build进手机
* 执行adb shell命令 `adb shell`
* 执行命令 `pm path com.liujian.wechatjumphelper`
* 复制"package:" 之后的路径, 通常是一个 /data/app开头的一个apk路径
* 执行命令 `export CLASSPATH={替换为上面的apk路径}`
* 执行命令 `exec app_process /system/bin com.liujian.wechatjumphelper.local.JumpLocalService '$@'`
* 正常的话, 上面操作完之后 会出现 Start JumpLocalService success, waiting client... 表示启动成功
* 打开APP, 点击开始跳一跳, 然后进入微信 开始跳一跳游戏 即可


完整命令如下
```html

liujiandeMacBook-Pro:~ liujian$ adb shell
shell@shamu:/ $ pm path com.liujian.wechatjumphelper
package:/data/app/com.liujian.wechatjumphelper-1/base.apk
shell@shamu:/ $ export CLASSPATH=/data/app/com.liujian.wechatjumphelper-1/base.apk
shell@shamu:/ $ exec app_process /system/bin com.liujian.wechatjumphelper.local.JumpLocalService '$@'
Start JumpLocalService success, waiting client...

```

先看个效果图, 原理下次再写..
![Sample](https://github.com/LiuJian2/WeChatJumpHelper/blob/master/img/sample.png =500)

