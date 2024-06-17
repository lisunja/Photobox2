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
        final int scale = 10;

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / scale, bitmap.getHeight() / scale, true);
        Mat image = new Mat();
        Utils.bitmapToMat(resizedBitmap, image);


        Mat gray = new Mat();
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);
       // saveStep( gray);

        Imgproc.GaussianBlur(gray, gray, new Size(11, 11), 0);
      //  saveStep( gray);
        Mat edges = new Mat();
        Imgproc.Canny(gray, edges, 10, 10);
       // saveStep( edges);
        // Imgproc.adaptiveThreshold(gray, gray, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, 11, 2);

        Mat morphImage = new Mat();
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5));
      //  saveStep( kernel);
        Imgproc.morphologyEx(edges, morphImage, Imgproc.MORPH_CLOSE, kernel);
       // saveStep( morphImage);


        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(morphImage, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        //saveStep( hierarchy);
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


        boundingRect.x *= scale;
        boundingRect.y *= scale;
        boundingRect.width *= scale;
        boundingRect.height *= scale;

        image.release();
        gray.release();
        hierarchy.release();
        for (MatOfPoint contour : contours) {
            contour.release();
        }

        return boundingRect;
    }
//    private void saveStep(Mat mat) {
//        Bitmap bmp = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
//        Utils.matToBitmap(mat, bmp);
//
//    }
}