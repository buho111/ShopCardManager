package com.example.shopcardmanager.model.usecase;

import android.content.res.Resources;
import android.graphics.*;
import com.example.shopcardmanager.R;
import org.opencv.android.Utils;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import java.util.*;


public class ImageScanner {

    private List<Double> d = new ArrayList<Double>();

    public Bitmap onImageScan(Bitmap bmp) {
        Mat mat = new Mat();
        Utils.bitmapToMat(bmp, mat, true);

        /* ①：画像を二値化 */
        Mat mat2;
        mat2 = getThreshold(mat);

        /* ②：輪郭の座標を取得 */
        List<MatOfPoint> contours = getContour(mat2);

        /* ③：最も面積が大きい四角形を返却 */
        int maxidx = 0;
        double maxarea = 0;
        for(int i=0; i<contours.size(); i++){
            if(Imgproc.contourArea(contours.get(i)) > maxarea) {
                maxarea = Imgproc.contourArea(contours.get(i));
                maxidx = i;
            }
        }

        List<Point> dstPoints = contours.get(maxidx).toList();
        double minX = -1, minY = -1, maxX = 0, maxY = 0;
        Point p1,p2,p3,p4;

        // debug start
        for(int i=0; i<contours.size(); i++) {
            List<Point> points = contours.get(i).toList();
            for(Point p : points) {
                d.add(p.x);
                d.add(p.y);
            }
        }
        // debug end

        for(Point p : dstPoints) {
            if(p.x > maxX) {
                maxX = p.x;
            }
            if(p.y > maxY) {
                maxY = p.y;
            }
            if(minX == -1) {
                minX = p.x;
            }
            else if(p.x < minX) {
                minX = p.x;
            }
            if(minY == -1) {
                minY = p.y;
            }
            else if(p.y < minY) {
                minY = p.y;
            }
        }

        MatOfPoint2f srcPoint2fMat = new MatOfPoint2f();
        MatOfPoint2f dstPoint2fMat = new MatOfPoint2f();

        contours.get(maxidx).convertTo(srcPoint2fMat, CvType.CV_32F);
        new MatOfPoint(
                new Point(minX, minY),
                new Point(maxX, minY),
                new Point(maxX, minY),
                new Point(maxX, maxY)).convertTo(dstPoint2fMat, CvType.CV_32F);

        Mat srcPointMat = contours.get(maxidx);
        Mat dstPointMat = new MatOfPoint(
                new Point(minX, minY),
                new Point(maxX, minY),
                new Point(maxX, minY),
                new Point(maxX, maxY));

        Mat M = Imgproc.getPerspectiveTransform(srcPoint2fMat, dstPoint2fMat);
        Mat dst =  new Mat(mat.rows(), mat.cols(), mat.type());
        Imgproc.warpPerspective(mat, dst, M, new Size(maxX, maxY));

        //Bitmap ret = Bitmap.createBitmap(dst.width(), dst.height(), Bitmap.Config.ARGB_8888);
        //Utils.matToBitmap(dst, ret);
        Bitmap ret = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, ret);

        return bmp;
    }

    public List<Double> getPoints() {
        return d;
    }

    /* 画像の二値化 */
    private Mat getThreshold(Mat mat) {
        /* ②-1-1：RGB空間チャネルの取得 */
        Mat mat_rgb = mat.clone();
        List<Mat> channels_rgb = new ArrayList<>();
        Core.split(mat_rgb, channels_rgb);

        /* ②-1-2：RGB空間 → グレースケール変換 → 二値化 */
        Imgproc.cvtColor(mat_rgb, mat_rgb, Imgproc.COLOR_RGB2GRAY);
        Core.subtract(channels_rgb.get(0), mat_rgb, channels_rgb.get(0));
        Core.subtract(channels_rgb.get(1), mat_rgb, channels_rgb.get(1));
        Core.subtract(channels_rgb.get(2), mat_rgb, channels_rgb.get(2));
        Imgproc.threshold(channels_rgb.get(0), channels_rgb.get(0), 0.0, 255.0, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
        Imgproc.threshold(channels_rgb.get(1), channels_rgb.get(1), 0.0, 255.0, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
        Imgproc.threshold(channels_rgb.get(2), channels_rgb.get(2), 0.0, 255.0, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);

        /* ②-1-3：RGBの輪郭を取得 */
        List<MatOfPoint> contour_rgb0 = getContour(channels_rgb.get(0));
        List<MatOfPoint> contour_rgb1 = getContour(channels_rgb.get(1));
        List<MatOfPoint> contour_rgb2 = getContour(channels_rgb.get(2));


        /* ②-2-1：YUV空間チャネルの取得 */
        Mat mat_yuv = mat.clone();
        Imgproc.cvtColor(mat_yuv, mat_yuv, Imgproc.COLOR_BGR2YUV);
        List<Mat> channels_yuv = new ArrayList<>();
        Core.split(mat_yuv, channels_yuv);

        /* ②-2-2：YUV空間 → グレースケール変換 → 二値化 */
        Imgproc.cvtColor(mat_yuv, mat_yuv, Imgproc.COLOR_RGB2GRAY);
        Core.subtract(channels_yuv.get(0), mat_yuv, channels_yuv.get(0));
        Core.subtract(channels_yuv.get(1), mat_yuv, channels_yuv.get(1));
        Core.subtract(channels_yuv.get(2), mat_yuv, channels_yuv.get(2));
        Imgproc.threshold(channels_yuv.get(0), channels_yuv.get(0), 0.0, 255.0, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
        Imgproc.threshold(channels_yuv.get(1), channels_yuv.get(1), 0.0, 255.0, Imgproc.THRESH_BINARY_INV | Imgproc.THRESH_OTSU);
        Imgproc.threshold(channels_yuv.get(2), channels_yuv.get(2), 0.0, 255.0, Imgproc.THRESH_BINARY_INV | Imgproc.THRESH_OTSU);

        /* ②-2-3：YUVの輪郭を取得 */
        List<MatOfPoint> contour_yuv0 = getContour(channels_yuv.get(0));
        List<MatOfPoint> contour_yuv1 = getContour(channels_yuv.get(1));
        List<MatOfPoint> contour_yuv2 = getContour(channels_yuv.get(2));


        /* ②-3：マスク画像に輪郭を合成 */
        Mat mat_mask = new Mat(mat.size(), CvType.CV_8UC4, Scalar.all(255));
        Scalar color = new Scalar(0, 0, 0);
        Imgproc.drawContours(mat_mask, contour_rgb0, -1, color, -1);
        Imgproc.drawContours(mat_mask, contour_rgb1, -1, color, -1);
        Imgproc.drawContours(mat_mask, contour_rgb2, -1, color, -1);
        Imgproc.drawContours(mat_mask, contour_yuv0, -1, color, -1);
        Imgproc.drawContours(mat_mask, contour_yuv1, -1, color, -1);
        Imgproc.drawContours(mat_mask, contour_yuv2, -1, color, -1);

        Imgproc.cvtColor(mat_mask, mat_mask, Imgproc.COLOR_RGB2GRAY);
        Imgproc.threshold(mat_mask, mat_mask, 0.0, 255.0, Imgproc.THRESH_BINARY_INV | Imgproc.THRESH_OTSU);

        return mat_mask;
    }

    /* 輪郭の取得 */
    private List<MatOfPoint> getContour(Mat mat) {
        List<MatOfPoint> contour = new ArrayList<>();

        /* 二値画像中の輪郭を検出 */
        List<MatOfPoint> tmp_contours = new ArrayList<>();
        Mat hierarchy = Mat.zeros(new Size(5, 5), CvType.CV_8UC1);
        Imgproc.findContours(mat, tmp_contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_TC89_L1);
        for (int i = 0; i < tmp_contours.size(); i++) {
            if (Imgproc.contourArea(tmp_contours.get(i)) < mat.size().area() / (100)) {
                /* サイズが小さいエリアは無視 */
                continue;
            }

            MatOfPoint2f ptmat2 = new MatOfPoint2f(tmp_contours.get(i).toArray());
            MatOfPoint2f approx = new MatOfPoint2f();
            MatOfPoint approxf1 = new MatOfPoint();

            /* 輪郭線の周囲長を取得 */
            double arclen = Imgproc.arcLength(ptmat2, true);
            /* 直線近似 */
            Imgproc.approxPolyDP(ptmat2, approx, 0.02 * arclen, true);
            approx.convertTo(approxf1, CvType.CV_32S);
            if (approxf1.size().area() != 4) {
                /* 四角形以外は無視 */
                continue;
            }

            /* 輪郭情報を登録 */
            contour.add(approxf1);
        }

        return contour;
    }
}
