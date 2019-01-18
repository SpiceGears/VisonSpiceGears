package com.example.mateusz.visonspicegears;

import android.os.FileUriExposedException;
import android.provider.MediaStore;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;


import java.io.File;
import java.io.FileFilter;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;


import static android.opengl.GLES10.GL_RGBA;
import static android.opengl.GLES10.glReadPixels;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_UNSIGNED_BYTE;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glTexSubImage2D;
import static java.lang.StrictMath.abs;
import static org.opencv.core.CvType.CV_8UC4;
import static org.opencv.features2d.BRISK.create;
import static org.opencv.imgcodecs.Imgcodecs.CV_IMWRITE_JPEG_QUALITY;


public class Vision  {

    static final class TargetData {
        boolean found;
        double topWidth;
        double topHeight;

        double topX;
        double topY;

        double botWidth;
        double botHeight;
        double botX;
        double botY;
    }





    static TargetData findTarget(Mat frame)  {
        TargetData d = new TargetData();
        Mat binaries = new Mat();
        Imgproc.cvtColor(frame,binaries,Imgproc.COLOR_BGR2HSV);
        Core.inRange(frame, new Scalar(40, 100, 40), new Scalar(70, 255, 255), binaries);


        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Rect rect = null;
        double maxArea = 300;
        double min_area = 100;
        Imgproc.findContours(binaries, contours,hierarchy,Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
        ArrayList<Rect> arr = new ArrayList<Rect>();
        for (int i = 0; i < contours.size(); i++) {
            Mat contour = contours.get(i);
            double contourArea = Imgproc.contourArea(contour);
            if (contourArea > maxArea) {
                rect = Imgproc.boundingRect(contours.get(i));
                arr.add(rect);

                if (rect.y > 390)
                    continue;

                // Drop the target if it's smaller than 200 pixels
                if ( rect.area() < 200 )
                    continue;

                if ( rect.area() > 2400 )
                    continue;

                if ( rect.height > rect.width )
                    continue;
            }
            if (contourArea < min_area || contourArea > maxArea) {
                continue;
            }
            Imgproc.drawContours(binaries, contours, i, new Scalar(255, 255, 255), -1);
        }


        Vector<Rect> targets = null;

        hierarchy.push_back(rect);
        if (targets.size() < 2){
            d.found = false;
        }
        else {
            for (int a = 0; a < targets.size(); a++) {
                for (int b = 0; b < targets.size(); b++) {
                    if (a != b) {
                        Rect top = targets.get(a);
                        Rect bottom = targets.get(b);

                        // Make sure the top is above the bottom
                        // Skip it if top.y > bottom.y
                        // Note (0,0) is top left
                        if (top.y > bottom.y)
                            continue;

                        // Same X +/- 10 pixels
                        if (abs(top.x - bottom.x) > 20)
                            continue;

                        // Not farther than 100px apart
                        if ( abs(top.y - bottom.y) > 100 )
                            continue;

                        if (abs(top.width - bottom.width) > 20)
                            continue;

                        // assuming we have top and bottom correct...

                        // Box the selected target in green
                        Imgproc.rectangle(frame, top.tl(), bottom.br(), new Scalar(0, 255, 0), 1);

                        d.topWidth = top.width;
                        d.topHeight = top.height;

                        d.topX = top.x + (d.topWidth / 2);
                        d.topY = top.y;

                        // Draw top point (used for distance and angle calc)
                        Imgproc.rectangle(frame, new Point(d.topX, top.y), new Point(d.topX, top.y), new Scalar(0, 255, 0), 5);

                        d.botWidth = bottom.width;
                        d.botHeight = bottom.height;

                        d.botX = bottom.x + (d.botWidth / 2);
                        d.botY = bottom.y;

                        d.found = true;
                    }
                }
            }
        }
        return d;

    }

    void processImage(int texOut, int w, int h ) {


        Mat input = new Mat();

        input.create(h, w, CV_8UC4);

        ByteBuffer buffer = ByteBuffer.allocate(input.rows() * input.cols() * input.channels());
        glReadPixels(0, 0, w, h, GL_RGBA, GL_UNSIGNED_BYTE,buffer);
        Core.flip(input,input, 1);

        TargetData target = findTarget(input);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texOut);
        glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, w, h, GL_RGBA, GL_UNSIGNED_BYTE,buffer);
    }

    byte processJpg(long ptr) {
        Mat input = new Mat();
        ArrayList jpgBuffer = new ArrayList<String>();
        Imgproc.cvtColor(input, input, Imgproc.COLOR_BGR2RGB);
        byte jpg = (byte) jpgBuffer.size();
        input.release();
        input = null;
        return jpg;
    }
    /**
    Point getSettings(Mat image, double angle){
        double radians = Math.toRadians(angle);
        double sin = Math.abs(Math.sin(radians));
        double cos = Math.abs(Math.cos(radians));

        int newWidth = (int) (image.width() * cos + image.height() * sin);
        int newHeight = (int) (image.width() * sin + image.height() * cos);

        // rotating image
        Point center = new Point(newWidth / 2, newHeight / 2);
        Mat rotMatrix = Imgproc.getRotationMatrix2D(center, angle, 1.0); //1.0 means 100 % scale

        Size size = new Size(newWidth, newHeight);

        Imgproc.warpAffine(image, image, rotMatrix, image.size());
        return  center;

    }
     */
}
