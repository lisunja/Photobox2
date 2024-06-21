package com.example.photobox2;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class ObjectDetection {
    public Rect detectObject(Bitmap bitmap) {

        Mat image = new Mat();
        Utils.bitmapToMat(bitmap, image);


        Mat gray = new Mat();
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);

        Imgproc.GaussianBlur(gray, gray, new Size(11, 11), 0);
        Mat edges = new Mat();
        Imgproc.Canny(gray, edges, 10, 10);

        Mat morphImage = new Mat();
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5));
        Imgproc.morphologyEx(edges, morphImage, Imgproc.MORPH_CLOSE, kernel);


        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(morphImage, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        double maxArea = 0;
        Rect boundingRect = new Rect();

        for (MatOfPoint contour : contours) {

            double area = Imgproc.contourArea(contour);
            Rect rect = Imgproc.boundingRect(contour);

            if (area > 1000 &&
                    rect.width > 30 && rect.height > 30 &&
                    rect.width < image.width() * 0.9 && rect.height < image.height() * 0.9) {
                if (area > maxArea) {
                    maxArea = area;
                    boundingRect = rect;
                }
            }
        }

        if (boundingRect.width == 0 || boundingRect.height == 0) {
            boundingRect = new Rect(0, 0, image.width(), image.height());
        }

        image.release();
        gray.release();
        hierarchy.release();
        for (MatOfPoint contour : contours) {
            contour.release();
        }

        return boundingRect;
    }
}