package com.techv.techvesigning;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PreviewDemoActivity extends AppCompatActivity {

    int CAMERA_PERMISSION_CODE = 1;
    Context mContext;
    private SurfaceView preview=null;
    private SurfaceView transparentView=null;
    private SurfaceHolder previewHolder=null;
    private SurfaceHolder holderTransparent=null;
    private Camera camera=null;
    private boolean inPreview=false;
    private boolean cameraConfigured=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_demo);
        mContext = this;
        preview=(SurfaceView)findViewById(R.id.preview);
        preview.setOnTouchListener(onTouchListner);
        transparentView=(SurfaceView)findViewById(R.id.TransparentView);
        holderTransparent = transparentView.getHolder();
        holderTransparent.setFormat(PixelFormat.TRANSPARENT);
        //holderTransparent.addCallback(surfaceCallback);
        holderTransparent.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        previewHolder=preview.getHolder();
        previewHolder.addCallback(surfaceCallback);
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        myRectPaint = new Paint();
        myRectPaint.setStrokeWidth(5);
        myRectPaint.setColor(Color.RED);
        myRectPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(checkCameraHardware()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
            } else {
                camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
                startPreview();
            }
        }
    }

    @Override
    public void onPause() {
        if (inPreview && camera!=null) {
            camera.stopPreview();
            camera.stopFaceDetection();
            camera.stopPreview();
            try {
                camera.setPreviewDisplay(null);
            } catch (IOException e) {
                e.printStackTrace();
            }
            camera.release();
            camera=null;
            inPreview=false;
        }
        super.onPause();
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

    private Camera.Size getBestPreviewSize(int width, int height,
                                           Camera.Parameters parameters) {
        Camera.Size result=null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width<=width && size.height<=height) {
                if (result==null) {
                    result=size;
                }
                else {
                    int resultArea=result.width*result.height;
                    int newArea=size.width*size.height;

                    if (newArea>resultArea) {
                        result=size;
                    }
                }
            }
        }

        return(result);
    }

    private void initPreview(int width, int height) {
        if (camera!=null && previewHolder.getSurface()!=null) {
            try {
                camera.setPreviewDisplay(previewHolder);
                camera.setDisplayOrientation(90);
            }
            catch (Throwable t) {
                Log.e("PreviewDemo-Callback","Exception in setPreviewDisplay()", t);
                Toast
                        .makeText(PreviewDemoActivity.this, t.getMessage(), Toast.LENGTH_LONG)
                        .show();
            }

            if (!cameraConfigured) {
                Camera.Parameters parameters=camera.getParameters();
                Camera.Size size=getBestPreviewSize(width, height,
                        parameters);

                if (size!=null) {
                    parameters.setPreviewSize(size.width, size.height);
                    camera.setParameters(parameters);
                    cameraConfigured=true;
                }
            }
        }
    }

    private void startPreview() {
        if (cameraConfigured && camera!=null) {
            camera.startPreview();
            MyFaceDetectionListener fDetectionListener = new MyFaceDetectionListener();
            camera.setFaceDetectionListener(fDetectionListener);
            camera.startFaceDetection();
            camera.setPreviewCallback(mPreviewCallback);
            inPreview=true;
        }
    }

    SurfaceHolder.Callback surfaceCallback=new SurfaceHolder.Callback() {
        public void surfaceCreated(SurfaceHolder holder) {
            // no-op -- wait until surfaceChanged()
        }

        public void surfaceChanged(SurfaceHolder holder,
                                   int format, int width,
                                   int height) {
            initPreview(width, height);
            startPreview();
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // no-op
        }
    };

    private Paint myRectPaint;

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
                    Canvas tempCanvas = previewHolder.lockCanvas();// new Canvas();
                    if(tempCanvas!=null) {
                        tempCanvas.drawRect(uRect, myRectPaint);
                        previewHolder.unlockCanvasAndPost(tempCanvas);
                    }
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

    private void DrawFocusRect(float RectLeft, float RectTop, float RectRight, float RectBottom, int color)
    {

        Canvas canvas = holderTransparent.lockCanvas();
        canvas.drawColor(0,PorterDuff.Mode.CLEAR);
        //border's properties
        myRectPaint = new Paint();
        myRectPaint.setStyle(Paint.Style.STROKE);
        myRectPaint.setColor(color);
        myRectPaint.setStrokeWidth(3);
        canvas.drawRect(RectLeft, RectTop, RectRight, RectBottom, myRectPaint);


        holderTransparent.unlockCanvasAndPost(canvas);
    }

    View.OnTouchListener onTouchListner = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            float RectLeft = event.getX() - 100;
            float RectTop = event.getY() - 100 ;
            float RectRight = event.getX() + 100;
            float RectBottom = event.getY() + 100;
            DrawFocusRect(RectLeft , RectTop , RectRight , RectBottom , Color.BLUE);
            return false;
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
                startPreview();
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }
}