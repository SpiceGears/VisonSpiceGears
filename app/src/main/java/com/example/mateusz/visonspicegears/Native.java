package com.example.mateusz.visonspicegears;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import static android.opengl.GLES10.GL_RGBA;
import static android.opengl.GLES10.glReadPixels;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_UNSIGNED_BYTE;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glTexSubImage2D;

import static org.opencv.core.CvType.CV_8UC4;

public class Native {

    public static long processImage(int texOut,
                                           int w,
                                           int h,
                                           Target targetInfo) {


        Mat input = new Mat();

        input.create(h, w, CV_8UC4);

        ByteBuffer buffer = ByteBuffer.allocate(input.rows() * input.cols() * input.channels());
        glReadPixels(0, 0, w, h, GL_RGBA, GL_UNSIGNED_BYTE,buffer);
        Core.flip(input,input, 1);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texOut);
        glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, w, h, GL_RGBA, GL_UNSIGNED_BYTE,buffer);

        return (long) Vision.findTarget(input).botX;
    }

    public static byte[] processJpg(long ptr){
        Mat input = new Mat(ptr);
        ArrayList jpgBuffer = new ArrayList<String>();
        Imgproc.cvtColor(input, input, Imgproc.COLOR_BGR2RGB);
        byte jpg[]  = new byte[ jpgBuffer.size()];
        input.release();
        input = null;
        return jpg;
    }
    public static void releaseImage(long ptr) {
        Mat  realPtr = new Mat(ptr);
        realPtr.release();
        realPtr = null;
    }


    public static class Target {
        public long imagePtr;

        public boolean found;

        public double topCentroidX;
        public double topCentroidY;
        public double topWidth;
        public double topHeight;

        public double bottomCentroidX;
        public double bottomCentroidY;
        public double bottomWidth;
        public double bottomHeight;
    }
}