package com.usc.camera.views;

/**
 * Created by adarsh on 2/7/2018.
 */

import android.content.Context;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.usc.camera.util.CameraUtil;
import com.usc.camera.util.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.usc.camera.util.CameraUtil.getCameraInstance;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private static final String LOG_TAG = CameraPreview.class.getName();
    private int frontCameraId = -1;
    private int width = 0;
    private int height = 0;
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private List<Camera.Size> mSupportedPreviewSizes;
    private Handler mHandler = null;


    public CameraPreview(Context context, int frontCameraId, Handler handler) {
        super(context);
        this.frontCameraId = frontCameraId;
        mHandler = handler;
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.

        mCamera = getCameraInstance(frontCameraId);
        mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
        if (height != 0 && width != 0) {
            Camera.Size mPreviewSize = CameraUtil.getOptimalPreviewSize(mSupportedPreviewSizes,
                    width, height);
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            mCamera.setParameters(parameters);
        }

        mCamera.setDisplayOrientation(90);

        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
            Message msg = new Message();
            msg.what = Constants.HANDLER_TAKE_PICTURE;
            mHandler.sendMessage(msg);
        } catch (IOException e) {
            Log.d(LOG_TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void startPreview(){
        if(mCamera!=null){
            try {
                mCamera.startPreview();
            }catch (Exception e){
                //
            }
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Took care of releasing the Camera preview.
        mCamera.release();
        mCamera = null;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }
        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }
        // set preview size and make any resize, rotate or
        // reformatting changes here
        mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
        if (height != 0 && width != 0) {
            Camera.Size mPreviewSize = CameraUtil.getOptimalPreviewSize(mSupportedPreviewSizes,
                    width, height);
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            mCamera.setParameters(parameters);
        }
        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e) {
            Log.d(LOG_TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public Camera getCamera(){
        return mCamera;
    }

}
