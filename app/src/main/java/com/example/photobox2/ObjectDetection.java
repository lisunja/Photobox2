package com.example.photobox2;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.util.Log;


import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjectDetection {
    public Rect detectObject(Bitmap bitmap) {
        Mat src = new Mat();
        Utils.bitmapToMat(bitmap, src);

        Mat gray = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);

        Imgproc.GaussianBlur(gray, gray, new Size(5, 5), 0);

        Imgproc.adaptiveThreshold(gray, gray, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, 11, 2);

        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        Imgproc.morphologyEx(gray, gray, Imgproc.MORPH_CLOSE, kernel);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(gray, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        double maxArea = 0;
        Rect boundingRect = new Rect();

        for (MatOfPoint contour : contours) {
            double area = Imgproc.contourArea(contour);
            Rect rect = Imgproc.boundingRect(contour);

            if (area > 1000 && area < src.width() * src.height() * 0.9 &&
                    rect.width > 30 && rect.height > 30 && rect.width < src.width() * 0.9 && rect.height < src.height() * 0.9) {
                if (area > maxArea) {
                    maxArea = area;
                    boundingRect = rect;
                }
            }
        }

        if (boundingRect.width == 0 || boundingRect.height == 0) {
            boundingRect = new Rect(0, 0, src.width(), src.height());
        }

        Log.d("ObjectDetection", "Detected object - x: " + boundingRect.x + ", y: " + boundingRect.y +
                ", width: " + boundingRect.width + ", height: " + boundingRect.height);

        src.release();
        gray.release();
        hierarchy.release();
        for (MatOfPoint contour : contours) {
            contour.release();
        }

        return boundingRect;
    }
}
