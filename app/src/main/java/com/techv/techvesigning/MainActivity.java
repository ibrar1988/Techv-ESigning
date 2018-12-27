package com.techv.techvesigning;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import java.util.Objects;

public class MainActivity extends AppCompatActivity /*implements View.OnClickListener*/ {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).setTitle("E-Signing");
        setContentView(R.layout.activity_main);
        findViewById(R.id.tv_user_registration).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, UserRegistrationActivity.class));
            }
        });

        findViewById(R.id.tv_user_verification).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, UserVerificationActivity.class));
            }
        });

        findViewById(R.id.tv_live_capture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PreviewDemoActivity.class));
            }
        });
    }

    /*int CAMERA_REQUEST = 1;
    int MY_CAMERA_PERMISSION_CODE = 2;
    public static final int MEDIA_TYPE_IMAGE = 3;
    public static final int MEDIA_TYPE_VIDEO = 4;
    private Camera mCamera;
    private CameraPreview mPreview;
    boolean faceDetectionRunning = false;
    int imagePosition = -1;
    private Context mContext;
    private EditText userName;
    private Button register, btn_athenticate_face;
    private AlertDialog mDialog;
    //private String base46image = null;
    public TransparentProgressDialog transparent_pd;
    JSONObject requestObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.mContext = this;
        userName = findViewById(R.id.et_user_name);
        findViewById(R.id.imageOne).setOnClickListener(this);
        findViewById(R.id.imageTwo).setOnClickListener(this);
        findViewById(R.id.imageThree).setOnClickListener(this);
        findViewById(R.id.imageFour).setOnClickListener(this);
        findViewById(R.id.imageFive).setOnClickListener(this);
        findViewById(R.id.imageSix).setOnClickListener(this);
        register = findViewById(R.id.btn_register);
        register.setOnClickListener(this);
        btn_athenticate_face = findViewById(R.id.btn_athenticate_face);
        btn_athenticate_face.setOnClickListener(this);
        requestObj = new JSONObject();
        initializeCamera();
    }

    @Override
    public void onDestroy(){
        if (transparent_pd!=null && transparent_pd.isShowing()) {
            transparent_pd.dismiss();
            transparent_pd = null;
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageOne:
                captureImage(1);
                break;
            case R.id.imageTwo:
                captureImage(2);
                break;
            case R.id.imageThree:
                captureImage(3);
                break;
            case R.id.imageFour:
                captureImage(4);
                break;
            case R.id.imageFive:
                captureImage(5);
                break;
            case R.id.imageSix:
                captureImage(6);
                break;
            case R.id.btn_register:
                doRegister();
                break;
            case R.id.btn_athenticate_face:
                authenticateUser();
                break;
        }
    }

    private void checkPermission(){


    }

    private void initializeCamera(){
        // Create an instance of Camera
        mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        MyFaceDetectionListener fDetectionListener = new MyFaceDetectionListener();
        mCamera.setFaceDetectionListener(fDetectionListener);
        mCamera.startFaceDetection();
        faceDetectionRunning = true;
        mCamera.setPreviewCallback(previewCallback);
    }

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

    *//** Create a file Uri for saving an image or video *//*
    private Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    *//** Create a File for saving an image or video *//*
    private File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyCameraApp");
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

    private void captureImage(int postion){
        if(checkCameraHardware()) {
            imagePosition = postion;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                } else {
                    startCamera();
                }
            } else {
                startCamera();
            }
        } else {
            alertMessage("Sorry camera is not installed on this phone");
        }
    }

    Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {

        }
    };

    private boolean checkCameraHardware(){
        if (mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    private void startCamera(){
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    private void alertMessage(String message){
        if(mDialog!=null && mDialog.isShowing()) {
            mDialog.dismiss();
            mDialog = null;
        }

        mDialog = new AlertDialog.Builder(mContext)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        mDialog.show();
    }

    public int stopFaceDetection() {
        if (faceDetectionRunning) {
            mCamera.stopFaceDetection();
            faceDetectionRunning = false;
            return 1;
        }
        return 0;
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

                mCamera.takePicture(null, null, mPicture);

                // add function to draw rects on view/surface/canvas
            }
        }
    }

    private void doRegister(){
        if(userName.getText().toString().trim().isEmpty()){
            alertMessage("Please enter user name");
        } else if(requestObj.length()<6) {
            alertMessage("Please capture all six images");
        } else {
            sendImage(Constants.kRegistration_Url, requestObj);
        }
    }

    private void authenticateUser(){
        if(requestObj.length()<0) {
            alertMessage("Please capture at least one image");
        } else {
            ArrayList<String> arr = new ArrayList<>();
            for(Iterator<String> iter = requestObj.keys();iter.hasNext();) {
                arr.add(iter.next());
            }

            if(arr.size()>0) {
                JSONObject reqObj = new JSONObject();
                try {
                    reqObj.put("img", arr.get(0));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                sendImage(Constants.kFaceVerification_Url, reqObj);
            } else {
                alertMessage("Please capture at least one image");
            }
        }
    }

    private void sendImage(String url, JSONObject reqObj){
        transparent_pd = new TransparentProgressDialog(mContext,"Uploading...");
        transparent_pd.show();
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, url, reqObj,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        dismissTransaparenDialog();
                        SigningApplication.mInstance.showToast(response.toString());
                        if(response.has("msg")) {
                            alertMessage(response.optString("msg"));
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                SigningApplication.mInstance.showToast(error.toString());
                dismissTransaparenDialog();
            }
        });

        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(60000, 5, 1f));

        SigningApplication.mInstance.addToRequestQueue(jsonObjReq, Constants.kRegistration_Url);
    }

    private class TransparentProgressDialog extends Dialog {
        private ImageView imgRotator;
        private TextView tvTitle;

        private TransparentProgressDialog(Context context, String title) {
            super(context, R.style.TransparentProgressDialog);

            try {
                setCancelable(false);
                setOnCancelListener(null);
                View spinnerView = View.inflate(mContext, R.layout.signing_transaparent, null);
                tvTitle = (TextView) spinnerView.findViewById(R.id.title);
                if(title!=null && !title.equalsIgnoreCase("")){
                    tvTitle.setText(title);
                }
                imgRotator = (ImageView) spinnerView.findViewById(R.id.signin_rotator);
                DisplayMetrics dm = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(dm);
                int width = dm.widthPixels;
                int height = dm.heightPixels;
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
                addContentView(spinnerView, params);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void show() {
            super.show();
            try {
                RotateAnimation anim = new RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF, .5f);
                anim.setInterpolator(new LinearInterpolator());
                anim.setRepeatCount(Animation.INFINITE);
                anim.setDuration(1000);
                imgRotator.setAnimation(anim);
                imgRotator.startAnimation(anim);
            } catch(Exception ex){
                ex.printStackTrace();
            }
        }
    }
    public void dismissTransaparenDialog() {
        if (transparent_pd!=null && transparent_pd.isShowing()) {
            transparent_pd.dismiss();
        }
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isFailover()) {
            return false;
        } else if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    private void updateImage(Bitmap btImage, int position){
        try {
        switch (position) {
            case 1:
                ((ImageView)findViewById(R.id.imageOne)).setImageBitmap(btImage);
                requestObj.put("image_0", encodeImage(btImage));
                break;
            case 2:
                ((ImageView)findViewById(R.id.imageTwo)).setImageBitmap(btImage);
                requestObj.put("image_1", encodeImage(btImage));
                break;
            case 3:
                ((ImageView)findViewById(R.id.imageThree)).setImageBitmap(btImage);
                requestObj.put("image_2", encodeImage(btImage));
                break;
            case 4:
                ((ImageView)findViewById(R.id.imageFour)).setImageBitmap(btImage);
                requestObj.put("image_3", encodeImage(btImage));
                break;
            case 5:
                ((ImageView)findViewById(R.id.imageFive)).setImageBitmap(btImage);
                requestObj.put("image_4", encodeImage(btImage));
                break;
            case 6:
                ((ImageView)findViewById(R.id.imageSix)).setImageBitmap(btImage);
                requestObj.put("image_5", encodeImage(btImage));
                break;
        }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String encodeImage(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG,100,baos);
        byte[] byteText = baos.toByteArray();
        return Base64.encodeToString(byteText, Base64.DEFAULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            assert data != null;
            Bitmap mphoto = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
            updateImage(mphoto, imagePosition);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                startCamera();
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }else if(requestCode==3) {
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                init();
            }
        } else if(requestCode==4 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            mCamera.takePicture(null, null, mPicture);
        }
    }*/
}
