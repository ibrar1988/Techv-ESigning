package com.techv.techvesigning;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

public class LiveCaptureActivity extends AppCompatActivity {

    private Context mContext;
    private Camera mCamera;
    private CameraPreview mPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_capture);
        this.mContext = this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        initializeCamera();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mCamera!=null) {
            mCamera.stopFaceDetection();
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void initializeCamera(){
        // Create an instance of Camera
        mCamera = getCameraInstance();
        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(mContext, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        MyFaceDetectionListener fDetectionListener = new MyFaceDetectionListener();
        mCamera.setFaceDetectionListener(fDetectionListener);
        mCamera.startFaceDetection();
        mCamera.setPreviewCallback(mPreviewCallback);
    }

    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    private class MyFaceDetectionListener implements Camera.FaceDetectionListener {

        @Override
        public void onFaceDetection(Camera.Face[] faces, Camera camera) {

            if (faces.length == 0) {
                Log.i("", "No faces detected");
            } else if (faces.length > 0) {
                Log.i("", "Faces Detected = " + String.valueOf(faces.length));

                List<Rect> faceRects = new ArrayList<Rect>();

                for (int i=0; i<faces.length; i++) {
                    int left = faces[i].rect.left;
                    int right = faces[i].rect.right;
                    int top = faces[i].rect.top;
                    int bottom = faces[i].rect.bottom;
                    Rect uRect = new Rect(left, top, right, bottom);
                    faceRects.add(uRect);
                }

                //mCamera.takePicture(null, null, mPicture);

                // add function to draw rects on view/surface/canvas
            }
        }
    }

    Camera.PreviewCallback mPreviewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {

        }
    };

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null){
                Log.d("MainActivity", "Error creating media file, check storage permissions");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d("MainActivity", "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d("MainActivity", "Error accessing file: " + e.getMessage());
            }
        }
    };

    //** Create a file Uri for saving an image or video *//*
    private Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    //** Create a File for saving an image or video *//*
    private File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyFaceDetectorApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }
}
