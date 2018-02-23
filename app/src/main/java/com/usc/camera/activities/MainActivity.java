package com.usc.camera.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.usc.camera.R;
import com.usc.camera.util.CameraUtil;
import com.usc.camera.util.Constants;
import com.usc.camera.views.CameraPreview;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity {

    public static final String LOG_TAG = MainActivity.class.getName();

    volatile CameraPreview mPreview;

    Camera mCamera;

    FrameLayout mFrameLayout;

    int frontCameraId = -1;

    Handler mHandler = null;

    Handler mResultHandler = null;

    volatile List<String> mPicturesList;

    int timeLapsed = 0;
    int time = 0;

    Runnable r;

    static final Integer WRITE_EXST = 0x3;
    static final Integer READ_EXST = 0x4;

    @BindView(R.id.text_view_camera)
    TextView textViewCamera;

    @BindView(R.id.image_view_timer)
    ImageView imageView;

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

        ButterKnife.bind(this);

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
        mPreview = new CameraPreview(getApplicationContext(), frontCameraId, mHandler);
        mFrameLayout.addView(mPreview);

//        final Handler handler = new Handler();
//        r = new Runnable() {
//            public void run() {
//                //Update and display
//                timeLapsed += 1;
//                switch (timeLapsed) {
//                    case 1:
//                        textViewCamera.setText("Now we will take photos");
//                        break;
//                    case 2:
//                        textViewCamera.setText("Get Ready");
//                        break;
//                    case 3:
//
//                        break;
//                    case 4: handler.removeCallbacks(r);
//                        break;
//                }
//                handler.postDelayed(this, 1000);
//
//            }
//        };

//        handler.postDelayed(r,1000);

//        time = 6;

//        final Handler temp = new Handler();
//        temp.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                time -= 1;
//                Log.e("rrbhbhbh", time+"");
//                switch (time){
//                    case 0:
//                        temp.removeCallbacks(this);
//                        textViewCamera.setText("Click");
//                        break;
//                    default: textViewCamera.setText(textViewCamera.getText().toString()+time);
//                        break;
//                }
//            }
//        }, 1000);

//        if (mHandler == null) {
//            initializeHandler();
//        }
//        mPreview = new CameraPreview(this, frontCameraId, mHandler);
//        mFrameLayout.addView(mPreview);
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
                        Toast.makeText(getApplicationContext(), "Taken", Toast.LENGTH_LONG).show();
                        break;
                    default:
                        break;
                }
            }
        };

        final Handler temp = new Handler();
        final Runnable rTemp = new Runnable() {
            @Override
            public void run() {
                time -= 1;
                switch (time){
                    case 5:
                        imageView.setImageResource(R.mipmap.five);
                        break;
                    case 4:imageView.setImageResource(R.mipmap.four);
                        break;
                    case 3:
                        imageView.setImageResource(R.mipmap.three);
                        break;
                    case 2:imageView.setImageResource(R.mipmap.two);
                        break;
                    case 1:imageView.setImageResource(R.mipmap.one);
                        break;
                    case 0: imageView.setImageDrawable(null);
                        break;
                }
                temp.postDelayed(this, 1000);
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
                            textViewCamera.setText("Done");
                            break;
                        }

                        time = 6;

                        temp.postDelayed(rTemp, 1000);

                        Handler mHandler = new Handler();
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                CameraUtil.takePicture(mPicturesList.get(0), mPreview,
                                        mResultHandler);
//                                textViewCamera.setText(""+mPicturesList.size());
                                temp.removeCallbacks(rTemp);

                            }
                        }, 7000);

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
                    askForPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,WRITE_EXST);
                    askForPermission(Manifest.permission.READ_EXTERNAL_STORAGE,READ_EXST);
                } else {
                    Toast.makeText(this, R.string.requires_camera, Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                Log.i(LOG_TAG, "onRequestPermissionsResult : Permission not handled" + requestCode);
        }
    }

    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permission)) {

                //This is called if user has denied the permission before
                //In this case I am just asking the permission again
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
            } else {

                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
            }
        } else {
            Toast.makeText(this, "" + permission + " is already granted.", Toast.LENGTH_SHORT).show();
        }
    }
}
