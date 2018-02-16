package com.usc.camera.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.usc.camera.R;
import com.usc.camera.util.CameraUtil;
import com.usc.camera.util.Constants;
import com.usc.camera.views.CameraPreview;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    public static final String LOG_TAG = MainActivity.class.getName();

    volatile CameraPreview mPreview;

    Camera mCamera;

    FrameLayout mFrameLayout;

    int frontCameraId = -1;

    Handler mHandler = null;

    Handler mResultHandler = null;

    volatile List<String> mPicturesList;


    @Override
    protected void onResume() {
        super.onResume();
        checkAndPutPictures();
    }

    private void checkAndPutPictures() {
        //If pictures already present in the pictures directory, then we need not put the
        // pictures again
        mPicturesList = new ArrayList<>();
        mPicturesList.add("Picture1");
        mPicturesList.add("Picture2");
        mPicturesList.add("Picture3");
        mPicturesList.add("Picture4");
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFrameLayout = (FrameLayout) findViewById(R.id.preview_layout);
        //If we have the camera feature, then ask for permission, else go to the flow without camera
        frontCameraId = CameraUtil.hasFrontCameraFeature();
        if (CameraUtil.checkCameraHardware(this) && frontCameraId != -1) {
            RequestPermissionForCamera();
        } else {
            Toast.makeText(this, R.string.requires_camera, Toast.LENGTH_SHORT).show();
            Handler mHandler = new Handler();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 3000);
        }
    }

    private void showTheCameraView() {
        // Create our Preview view and set it as the content of our activity.
        if (mHandler == null) {
            initializeHandler();
        }
        mPreview = new CameraPreview(this, frontCameraId, mHandler);
        mFrameLayout.addView(mPreview);
    }

    private void initializeHandler() {

        mResultHandler = new Handler(getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case Constants.FULL_PICTURE_TAKEN:
                        mPicturesList.remove(String.valueOf(msg.obj));
                        Message msgTakePicture = new Message();
                        msgTakePicture.what = Constants.HANDLER_TAKE_PICTURE;
                        mHandler.sendMessage(msgTakePicture);
                        break;
                    default:
                        break;
                }
            }
        };


        mHandler = new Handler(getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case Constants.HANDLER_TAKE_PICTURE:
                        //take the first photo from the list
                        if (mPreview != null) {
                            mPreview.startPreview();
                        }
                        if (mPicturesList.size() == 0) {
                            Toast.makeText(getApplicationContext(), "All images taken " +
                                    "successfully", Toast.LENGTH_SHORT).show();
                            break;
                        }
                        Handler mHandler = new Handler();
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                CameraUtil.takePicture(mPicturesList.get(0), mPreview,
                                        mResultHandler);
                            }
                        }, 6000);
                        break;
                }
            }
        };
    }


    private void RequestPermissionForCamera() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission
                    .CAMERA)) {
                //TODO
                //Please allow camera permission else you cannot proceed with this feature
                //Make sure not to block this thread
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                        Constants.CAMERA_PERMISSION);
            }
        } else {
            showTheCameraView();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.CAMERA_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showTheCameraView();
                } else {
                    Toast.makeText(this, R.string.requires_camera, Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                Log.i(LOG_TAG, "onRequestPermissionsResult : Permission not handled" + requestCode);
        }
    }

}
