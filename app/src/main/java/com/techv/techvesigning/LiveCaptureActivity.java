package com.techv.techvesigning;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

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

public class LiveCaptureActivity extends AppCompatActivity implements View.OnClickListener {

    int CAMERA_PERMISSION_CODE = 1;
    private Context mContext;
    private Camera mCamera;
    private CameraPreview mPreview;
    private ImageButton btn_switch_camera;
    private int mCurrentCameraId;
    private FrameLayout mPreviewLayout;
    private Paint myRectPaint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_capture);
        this.mContext = this;
        btn_switch_camera = (ImageButton)findViewById(R.id.btn_switch_camera);
        btn_switch_camera.setOnClickListener(this);
        mCurrentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        myRectPaint = new Paint();
        myRectPaint.setStrokeWidth(5);
        myRectPaint.setColor(Color.RED);
        myRectPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(checkCameraHardware()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
            } else {
                initializeCamera();
            }
        } else {
            Common.showAlertMessage(mContext,"Sorry camera is not installed on this phone", "");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mCamera!=null) {
            mCamera.stopFaceDetection();
            mCamera.stopPreview();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mCamera!=null) {
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_switch_camera:
                switchCamera();
                break;
            default:
                break;
        }
    }

    private boolean checkCameraHardware(){
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    private void initializeCamera(){
        // Create an instance of Camera
        mCamera = getCameraInstance(mCurrentCameraId);
        if(mCamera!=null) {
            //setCameraDisplayOrientation();
            // Create our Preview view and set it as the content of our activity.
            mPreview = new CameraPreview(mContext, mCamera);
            mPreviewLayout = (FrameLayout) findViewById(R.id.camera_preview);
            mPreviewLayout.addView(mPreview);
            MyFaceDetectionListener fDetectionListener = new MyFaceDetectionListener();
            mCamera.setFaceDetectionListener(fDetectionListener);
            mCamera.startFaceDetection();
            mCamera.setPreviewCallback(mPreviewCallback);
        } else {
            Common.showAlertMessage(mContext, "Sorry unable to open camera.", "finish");
        }
    }

    public Camera getCameraInstance(int cameraId){
        Camera c = null;
        try {
            c = Camera.open(cameraId); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    private void switchCamera() {
        if(mCamera!=null) {
            mPreview.surfaceDestroyed(mPreview.getHolder());
            mPreview.getHolder().removeCallback(mPreview);
            mPreview.destroyDrawingCache();
            mPreviewLayout.removeView(mPreview);
            mPreviewLayout.invalidate();
            mCamera.stopFaceDetection();
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            try {
                mCamera.setPreviewDisplay(null);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mCamera.release();
            if(mCurrentCameraId==Camera.CameraInfo.CAMERA_FACING_BACK) {
                mCurrentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
            } else {
                mCurrentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
            }
            initializeCamera();
            //mCamera.startPreview();
        }
    }

    public void setCameraDisplayOrientation() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (cameraInfo.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (cameraInfo.orientation - degrees + 360) % 360;
        }
        if(mCamera!=null) {
            mCamera.setDisplayOrientation(result);
        }
    }

    private class MyFaceDetectionListener implements Camera.FaceDetectionListener {

        @Override
        public void onFaceDetection(Camera.Face[] faces, Camera camera) {

            if (faces.length == 0) {
                Log.i("", "No faces detected");
            } else if (faces.length > 0) {
                Log.i("", "Faces Detected = " + String.valueOf(faces.length));

                List<RectF> faceRects = new ArrayList<RectF>();

                for (Camera.Face face : faces) {
                    int left = face.rect.left;
                    int right = face.rect.right;
                    int top = face.rect.top;
                    int bottom = face.rect.bottom;
                    RectF uRect = new RectF(left, top, right, bottom);
                    //faceRects.add(uRect);
                    Canvas tempCanvas = new Canvas();
                    tempCanvas.drawRect(uRect, myRectPaint);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                initializeCamera();
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }
}
