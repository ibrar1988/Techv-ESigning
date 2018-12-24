package com.techv.techvesigning;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class Common {

    private static AlertDialog mDialog;

    public static void showAlertMessage(Context mContext, String message){
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

    public static String encodeImageBase64(Bitmap bm) {
        if(bm!=null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] byteText = baos.toByteArray();
            return Base64.encodeToString(byteText, Base64.DEFAULT);
        } else {
            return "";
        }
    }
}
