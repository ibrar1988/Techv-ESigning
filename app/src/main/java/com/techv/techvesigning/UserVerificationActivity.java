package com.techv.techvesigning;

import android.Manifest;
import android.app.ActionBar;
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
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
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

import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.Objects;

public class UserVerificationActivity extends AppCompatActivity implements View.OnClickListener {

    int CAMERA_PERMISSION_CODE = 1;
    int CAMERA_REQUEST = 2;
    private Context mContext;
    private ImageView img_user_verify;
    Button btn_user_verify;
    private TransparentProgressDialog transparent_pd;
    private JSONObject requestObj;
    private FaceDetector faceDetector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_verification);
        this.mContext = this;
        init();
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

    private void init(){
        faceDetector = new FaceDetector.Builder(getApplicationContext()).setTrackingEnabled(false).build();
        img_user_verify = findViewById(R.id.img_user_verify);
        img_user_verify.setOnClickListener(this);
        btn_user_verify = findViewById(R.id.btn_user_verify);
        btn_user_verify.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.img_user_verify:
                captureImage();
                break;
            case R.id.btn_user_verify:
                VerifyUser();
                break;
        }
    }

    private void captureImage(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            startCamera();
        }
    }

    private void startCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
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

    private void VerifyUser(){
        if(requestObj==null || requestObj.length()==0) {
            Common.showAlertMessage(mContext,"Please capture your face picture");
        } else {
            transparent_pd = new TransparentProgressDialog(mContext, "Uploading...");
            transparent_pd.show();
            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, Constants.kFaceVerification_Url, requestObj,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            dismissTransaparenDialog();
                            SigningApplication.mInstance.showToast(response.toString(),Toast.LENGTH_LONG);
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
        }
    }

    private String encodeImageBase64(Bitmap bm) {
        if(bm!=null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] byteText = baos.toByteArray();
            return Base64.encodeToString(byteText, Base64.DEFAULT);
        } else {
            return "";
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
                        img_user_verify.setImageBitmap(myBitmap);
                        if(requestObj!=null) {
                            Iterator keys = requestObj.keys();
                            while(keys.hasNext())
                                requestObj.remove((String)requestObj.keys().next());
                        } else {
                            requestObj = new JSONObject();
                        }
                        try {
                            requestObj.put("img",encodeImageBase64(myBitmap));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Common.showAlertMessage(mContext, "Sorry Could not detect any face");
                    }
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            }
        }
    }
}
