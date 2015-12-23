package com.example.lanusiv.myapplication;

import android.util.Log;

/**
 * Created by Leray on 2015/8/10.
 */
public class MathUtils {

    /**
     * calculate the length between two points
     * @param cx
     * @param cy
     * @param ax
     * @param ay
     * @return
     */
    public static double sideLength(double cx, double cy, double ax, double ay) {
        return Math.sqrt(Math.pow(ax - cx, 2) + Math.pow(ay - cy, 2));
    }

    /**
     * get the angle of a and b, center is c
     * @param startPoint
     * @param endPoint
     * @param center
     * @return
     */
    public static double getAngle(double[] startPoint, double[] endPoint, double[] center) {
        double angle = 0;
        double radians = 0;

        // 边长
        double la = sideLength(center[0], center[1], endPoint[0], endPoint[1]);
        double lb = sideLength(center[0], center[1], startPoint[0], startPoint[1]);
        double lc = sideLength(startPoint[0], startPoint[1], endPoint[0], endPoint[1]);

        System.out.println("la = " + la + ", lb = " + lb + ", lc = " + lc);

        double cosAngle = (Math.pow(la, 2) + Math.pow(lb, 2) - Math.pow(lc, 2)) / (2 * la * lb);
        radians = Math.acos(cosAngle);
        angle = Math.toDegrees(radians);

        System.out.println("cosAngle = " + cosAngle + ", radians = " + radians + ", angle = " + angle);

        angle *= getDirection(startPoint, endPoint, center);

        return angle;
    }

    /**
     * return the direction of either clockwise or anticlockwise of the gesture
     *
     * first we need to convert the android coordinate to Cartesian coordinate system,
     * in order to calculate the tan of the two points
     * @param startPoint
     * @param endPoint
     * @param center
     * @return
     */
    public static int getDirection(double[] startPoint, double[] endPoint, double[] center) {
        double startX = startPoint[0] - center[0];
        double startY = center[1] - startPoint[1];
        double endX = endPoint[0] - center[0];
        double endY = center[1] - endPoint[1];

        double kStart = startY / startX;
        double kEnd = endY / endX;
        Log.d("hello", "kEnd = " + kEnd);
        if (kEnd > 90) {
            kEnd = kStart + 0.1;
        }
//        if (kEnd < 0.0001) {
//            kEnd = kStart
//        }

        if (kStart < kEnd) {
            return DIRECTION_ANTICLOCKWISE;
        } else if (kStart > kEnd){
            return DIRECTION_CLOCKWISE;
        } else  {
            return startX > endX ? DIRECTION_ANTICLOCKWISE : DIRECTION_CLOCKWISE;
        }
    }

    public static final int DIRECTION_ANTICLOCKWISE = -1;
    public static final int DIRECTION_CLOCKWISE = 1;
}
