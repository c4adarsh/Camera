package com.usc.camera.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.usc.camera.views.CameraPreview;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
import static com.usc.camera.util.Constants.DIRECTORY_NAME;


/**
 * Created by adarsh on 2/7/2018.
 */

public class CameraUtil {

    public static final String LOG_TAG = CameraUtil.class.getName();

    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance(int frontCameraId) {
        Camera c = null;
        try {
            c = Camera.open(frontCameraId); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    /**
     * Check if this device has a camera
     */
    public static boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    public static int hasFrontCameraFeature() {
        int numCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (CAMERA_FACING_FRONT == info.facing) {
                return i;
            }
        }
        return -1;
    }

    public static Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {

        if (sizes==null) return null;

        Camera.Size optimalSize = null;
        double ratio = (double)h/w;
        double minDiff = Double.MAX_VALUE;
        double newDiff;
        for (Camera.Size size : sizes) {
            newDiff = Math.abs((double)size.width/size.height - ratio);
            if (newDiff < minDiff) {
                optimalSize = size;
                minDiff = newDiff;
            }
        }
        return optimalSize;
    }

    public static Camera.PictureCallback getPictureCallback(final String pictureName, final Handler mHandler){
        Camera.PictureCallback mPicture = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {

                File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE,pictureName);
                if (pictureFile == null){
                    Log.d(LOG_TAG, "Error creating media file, check storage permissions");
                    return;
                }

                try {
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(data);
                    fos.close();
                    Message msg = new Message();
                    msg.what = Constants.FULL_PICTURE_TAKEN;
                    msg.obj = pictureName;
                    mHandler.sendMessage(msg);
                } catch (FileNotFoundException e) {
                    Log.d(LOG_TAG, "File not found: " + e.getMessage());
                } catch (IOException e) {
                    Log.d(LOG_TAG, "Error accessing file: " + e.getMessage());
                }
            }
        };

        return mPicture;
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type, String imageName){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), DIRECTORY_NAME);
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    imageName + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    imageName + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    public static void takePicture(String name, CameraPreview mPreview, Handler mHandler){
        if(mPreview!=null){
            Camera camera = mPreview.getCamera();
            if(camera!=null){
                try{
                    camera.takePicture(null,null, getPictureCallback(name, mHandler));
                }catch(Exception e){
                    Log.i(LOG_TAG,"TakePicture " + e.getMessage());
                }
            }
        }
    }

}
