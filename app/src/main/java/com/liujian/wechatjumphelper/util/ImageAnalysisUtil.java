package com.liujian.wechatjumphelper.util;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by liujian on 2017/12/31.
 * 分析一帧图片中 棋子的位置 和下一个要跳跃的块的中心点
 */
public class ImageAnalysisUtil {

    /**
     * 距离->时间转换参数
     * TODO 根据前两次跳跃调整距离时间参数
     */
    public static double DISTANCE_TO_TIME = 1000 * 0.85 / 622.74;

    private static final float START_SCAN_RATIO = 0.33f; // 从屏幕的40%出开始向下扫描

    private static final int CHESS_COLOR = -13092766;

    private static final int CHESS_WIDTH = 104;

    private static final int MARK_WIDTH = 5;

    public static long findNextPressTime(int index, Bitmap bitmap) {
        if (bitmap == null) return 0;

        Point nextJumpPoint = findNextJumpPoint(bitmap, null);
        Point currnetPoint = findCurrentChessPoint(bitmap);

        if (nextJumpPoint == null || currnetPoint == null) {
            return 0;
        }

        if (Math.abs(nextJumpPoint.x - currnetPoint.x) < 30) {
            // 下一个块的起始位置y 比棋子的y小, 排除棋子区域重新扫描
            nextJumpPoint = findNextJumpPoint(bitmap, currnetPoint);
            if (nextJumpPoint == null) {
                return 0;
            }
        }
        int deltaX = nextJumpPoint.x - currnetPoint.x;
        int deltaY = nextJumpPoint.y - currnetPoint.y;
        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        if (Constants.SAVE_MARK_IMG) {
            markBitmapAndSave(bitmap, index, nextJumpPoint, currnetPoint, MARK_WIDTH);
        }

        return (long) (distance * DISTANCE_TO_TIME);
    }

    private static Point findNextJumpPoint(Bitmap bitmap, Point chessPoint) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int startY = (int) (height * START_SCAN_RATIO);

        Point topPoint = findNextBlockTopPoint(bitmap, width, height, startY, chessPoint);
        if (topPoint == null) {
            return null;
        }
        Point rightPoint = findNextBlockRightPoint(bitmap, width, height, topPoint, chessPoint);
        if (rightPoint == null) {
            return null;
        }
        return new Point(topPoint.x, rightPoint.y);
    }

    private static Point findNextBlockTopPoint(Bitmap bitmap, int width, int height, int startY, Point chessPoint) {
        int lastPixel;
        for (int y = startY; y < height; y++) {
            lastPixel = bitmap.getPixel(0, y - 1);
            for (int x = 1; x < width; x++) {
                if (inChessRect(chessPoint, x)) {
                    continue;
                }
                int pixel = bitmap.getPixel(x, y);
                if (distanceOfColor(pixel, lastPixel) > 30) {
                    // 找到第一个颜色突变点, 可视为方块顶点
                    // TODO 对圆形取中心点
                    return new Point(x, y);
                }
                lastPixel = pixel;
            }
        }
        return null;
    }

    private static void markBitmapAndSave(Bitmap bitmap, int index, Point nextJumpPoint, Point currnetPoint, int radius) {
        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        markBitmap(mutableBitmap, nextJumpPoint, radius);
        markBitmap(mutableBitmap, currnetPoint, radius);
        try {
            saveMarkBitmapToFile(mutableBitmap, index);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void markBitmap(Bitmap bitmap, Point point, int radius) {
        for (int x = point.x - radius; x < point.x + radius; x++) {
            for (int y = point.y - radius; y < point.y + radius; y++) {
                bitmap.setPixel(x, y, Color.RED);
            }
        }
    }

    private static void saveMarkBitmapToFile(Bitmap bitmap, int index) throws IOException {
        if (!Constants.SAVE_MARK_IMG || bitmap == null) {
            return;
        }
        File file = new File("/sdcard/mark_" + index + ".png");
        FileOutputStream out = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        out.flush();
        out.close();
    }

    private static boolean inChessRect(Point chessPoint, int x) {
        return chessPoint != null
                && ((chessPoint.x - CHESS_WIDTH / 2) <= x)
                && ((chessPoint.x + CHESS_WIDTH / 2) >= x);
    }

    // 从找到的块顶点作为左上角 为画一个矩形, 在矩形中扫描最右的一个与顶点颜色相同的点
    private static Point findNextBlockRightPoint(Bitmap bitmap, int width, int height, Point top, Point chessPoint) {
        int topPixel = bitmap.getPixel(top.x, top.y);
        int startX = top.x;
        int startY = top.y;
        for (int x = Math.min(startX + 320, width - 1); x > startX; x--) {
            if (inChessRect(chessPoint, x)) {
                continue;
            }
            for (int y = startY + 1; y < Math.min(startY + 220, height); y++) {
                int pixel = bitmap.getPixel(x, y);
                if (distanceOfColor(pixel, topPixel) < 30) {
                    return new Point(x, y);
                }
            }
        }
        return null;
    }

    private static Point findCurrentChessPoint(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int startY = (int) (height * START_SCAN_RATIO);
        return findCurrentChessPoint(bitmap, 0, startY, width, height);
    }

    private static Point findCurrentChessPoint(Bitmap bitmap, int startX, int startY, int width, int height) {
        boolean findLeft = false;
        int leftX = 0, leftY = 0;
        for (int x = startX; x < width; x++) {
            for (int y = height - 1; y > startY; y--) {
                int pixel = bitmap.getPixel(x, y);
                if (pixel == CHESS_COLOR) {
                    findLeft = true;
                    leftX = x;
                    leftY = y;
                    break;
                }
            }
            if (findLeft) {
                break;
            }
        }
        for (int x = width - 1; x > leftX; x--) {
            int pixel = bitmap.getPixel(x, leftY);
            if (pixel == CHESS_COLOR) {
                return new Point((x + leftX) / 2, leftY);
            }
        }
        if (findLeft) {
            return new Point(leftX + 8, leftY);
        }
        return null;
    }

    public static int distanceOfColor(int pixelColor1, int pixelColor2) {
        int rgb1[] = getColorRgb(pixelColor1);
        int rgb2[] = getColorRgb(pixelColor2);
        rgb2[0] = rgb1[0] - rgb2[0];
        rgb2[1] = rgb1[1] - rgb2[1];
        rgb2[2] = rgb1[2] - rgb2[2];
        return (int) Math.sqrt(rgb2[0] * rgb2[0] + rgb2[1] * rgb2[1] + rgb2[2] * rgb2[2]);
    }

    public static int[] getColorRgb(int color) {
        int rgb[] = new int[3];
        rgb[0] = (color & 0xff0000) >> 16;
        rgb[1] = (color & 0xff00) >> 8;
        rgb[2] = (color & 0xff);
        return rgb;
    }
}
