package com.techv.techvesigning;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;

import java.io.ByteArrayOutputStream;

public class SaveFaceFrames extends Activity implements Camera.PreviewCallback, Camera.FaceDetectionListener {

    boolean lock = false;

    public void onPreviewFrame(byte[] data, Camera camera) {
        if(lock) {
            Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = parameters.getPreviewSize();
            YuvImage image = new YuvImage(data, parameters.getPreviewFormat(), size.width, size.height, null);
            ByteArrayOutputStream outstr = new ByteArrayOutputStream();
            image.compressToJpeg(new Rect(0, 0, image.getWidth(), image.getHeight()), 100, outstr);
            Bitmap bmp = BitmapFactory.decodeByteArray(outstr.toByteArray(), 0, outstr.size());
            lock = false;
        }
    }

    public void onFaceDetection(Camera.Face[] faces, Camera camera) {
        if(!lock) {
            if(faces.length != 0)
                lock = true;
        }
    }
}