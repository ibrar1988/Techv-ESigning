package com.techv.techvesigning;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Context mContext;
    private EditText userName;
    private ImageView userProfile;
    private Button captureImage;
    private Button register;
    private AlertDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.mContext = this;
        userName = findViewById(R.id.et_user_name);
        userProfile = findViewById(R.id.img_profile);
        userProfile.setOnClickListener(this);
        captureImage = findViewById(R.id.btn_capture);
        captureImage.setOnClickListener(this);
        register = findViewById(R.id.btn_register);
        register.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_profile:
                break;
            case R.id.btn_capture:
                break;
            case R.id.btn_register:
                break;
        }
    }

    private void captureImage(){
        if(checkCameraHardware()) {

        } else {
            alertMessage("Sorry camera is not installed on this phone");
        }
    }

    private boolean checkCameraHardware(){
        if (mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    private void alertMessage(String message){
        if(mDialog!=null && mDialog.isShowing()) {
            mDialog.dismiss();
            mDialog = null;
        }

        mDialog = new AlertDialog.Builder(mContext)
                .setMessage(message)
                .setCancelable(false)
                .create();
        mDialog.show();
    }
}
