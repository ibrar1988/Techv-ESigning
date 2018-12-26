package com.techv.techvesigning;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class UserRegistrationActivity extends AppCompatActivity implements View.OnClickListener {

    int CAMERA_PERMISSION_CODE = 1;
    int CAMERA_REQUEST = 2;
    private Context mContext;
    private EditText et_user_name;
    private TransparentProgressDialog transparent_pd;
    int imagePosition = -1;
    private JSONObject requestObj;
    private FaceDetector faceDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        mContext = this;
        et_user_name = findViewById(R.id.et_user_name);
        findViewById(R.id.btn_register_user).setOnClickListener(this);
        findViewById(R.id.imageOne).setOnClickListener(this);
        findViewById(R.id.imageTwo).setOnClickListener(this);
        findViewById(R.id.imageThree).setOnClickListener(this);
        findViewById(R.id.imageFour).setOnClickListener(this);
        findViewById(R.id.imageFive).setOnClickListener(this);
        findViewById(R.id.imageSix).setOnClickListener(this);
        faceDetector = new FaceDetector.Builder(getApplicationContext()).setTrackingEnabled(false).build();
        requestObj = new JSONObject();
    }

    @Override
    public void onBackPressed() {
        finish();
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
            case R.id.btn_register_user:
                doRegister();
                break;
        }
    }

    private void captureImage(int postion){
        if(checkCameraHardware()) {
            imagePosition = postion;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
            } else {
                startCamera();
            }
        } else {
            Common.showAlertMessage(mContext,"Sorry camera is not installed on this phone");
        }
    }

    private void startCamera(){
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
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

    private void doRegister(){
        if(et_user_name.getText().toString().trim().isEmpty()){
            Common.showAlertMessage(mContext,"Please enter user name");
        } else if(requestObj.length()<6) {
            Common.showAlertMessage(mContext,"Please capture all six images");
        } else {
            registerUser();
        }
    }

    private void updateImage(Bitmap btImage, int position){
        try {
            switch (position) {
                case 1:
                    ((ImageView)findViewById(R.id.imageOne)).setImageBitmap(btImage);
                    requestObj.put("image_0", Common.encodeImageBase64(btImage));
                    break;
                case 2:
                    ((ImageView)findViewById(R.id.imageTwo)).setImageBitmap(btImage);
                    requestObj.put("image_1", Common.encodeImageBase64(btImage));
                    break;
                case 3:
                    ((ImageView)findViewById(R.id.imageThree)).setImageBitmap(btImage);
                    requestObj.put("image_2", Common.encodeImageBase64(btImage));
                    break;
                case 4:
                    ((ImageView)findViewById(R.id.imageFour)).setImageBitmap(btImage);
                    requestObj.put("image_3", Common.encodeImageBase64(btImage));
                    break;
                case 5:
                    ((ImageView)findViewById(R.id.imageFive)).setImageBitmap(btImage);
                    requestObj.put("image_4", Common.encodeImageBase64(btImage));
                    break;
                case 6:
                    ((ImageView)findViewById(R.id.imageSix)).setImageBitmap(btImage);
                    requestObj.put("image_5", Common.encodeImageBase64(btImage));
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
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

    private void registerUser() {
        try {
            requestObj.put("name", et_user_name.getText().toString().trim());
            transparent_pd = new TransparentProgressDialog(mContext, "Uploading...");
            transparent_pd.show();
            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, Constants.kRegistration_Url, requestObj,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            dismissTransaparenDialog();
                            SigningApplication.mInstance.showToast(response.toString(), Toast.LENGTH_LONG);
                            if (response.has("msg")) {
                                Common.showAlertMessage(mContext, response.optString("msg"));
                            }
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    SigningApplication.mInstance.showToast(error.toString(), Toast.LENGTH_LONG);
                    dismissTransaparenDialog();
                }
            });

            jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(60000, 5, 1f));

            SigningApplication.mInstance.addToRequestQueue(jsonObjReq, Constants.kRegistration_Url);
        } catch (JSONException ex){
            ex.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            assert data != null;
            Bitmap myBitmap = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
            if(myBitmap!=null) {
                if (!faceDetector.isOperational()) {
                    Common.showAlertMessage(mContext, "Could not set up the face detector!");
                } else {
                    Frame frame = new Frame.Builder().setBitmap(myBitmap).build();
                    SparseArray<Face> faces = faceDetector.detect(frame);
                    if(faces.size()>0) {
                        updateImage(myBitmap, imagePosition);
                    } else {
                        Common.showAlertMessage(mContext, "Sorry Could not detect any face");
                    }
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                startCamera();
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }
}
