package com.music.classes;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by sahil-goel on 26/7/16.
 */
public class CheckPermission {
    /*public static boolean isPermissionGranted(Context context, String permission) {
        int status = ContextCompat.checkSelfPermission(context, permission);
        if (status == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }


    public void checkPermission() {
        if (CheckPermission.isPermissionGranted(FoldersListActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            mIsPermissionGranted = true;
            updateSonListUI();
        } else {
            mIsPermissionGranted = false;
            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mIsPermissionGranted = true;
                updateSonListUI();
            } else {
                mIsPermissionGranted = false;
                Log.d("Training", "Please provide permission");
            }
        }
    }*/
}
