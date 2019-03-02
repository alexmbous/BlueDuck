package com.blueduck.ride.qrcode.base;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.FrameLayout;

import com.blueduck.ride.utils.LogUtils;

import java.util.ArrayList;

/**
 * Basic scan view, including CameraPreview and ViewFinderView (scan code frame, shadow mask, etc.)
 * 基本扫码视图，包含CameraPreview（相机预览）和ViewFinderView（扫码框、阴影遮罩等）
 */
public abstract class BarcodeScannerView extends FrameLayout implements Camera.PreviewCallback {
    private static final String TAG = "BarcodeScannerView";

    protected CameraHandlerThread cameraHandlerThread;//当相机被释放时会被置为null / Will be set to null when the camera is released
    protected CameraWrapper cameraWrapper;//当相机被释放时会被置为null / Will be set to null when the camera is released

    private IViewFinder viewFinderView;
    private CameraPreview cameraPreview;
    private Rect scaledRect, rotatedRect;
    private ArrayList<Camera.Area> focusAreas;
    private boolean shouldAdjustFocusArea = false;//是否需要自动调整对焦区域 / Do you need to adjust the focus area automatically?

    public BarcodeScannerView(@NonNull Context context, @NonNull IViewFinder viewFinderView) {
        super(context);

        if (viewFinderView instanceof View) {
            this.viewFinderView = viewFinderView;
        } else {
            throw new IllegalArgumentException("viewFinderView必须是View对象");
        }
    }

    /**
     * Turn on the system camera and perform basic initialization
     * 打开系统相机，并进行基本的初始化
     */
    public void startCamera() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            if (cameraHandlerThread == null) {
                cameraHandlerThread = new CameraHandlerThread(this);
            }
            cameraHandlerThread.startCamera(CameraUtils.getDefaultCameraId());
        } else {//没有相机权限 / No camera permissions
            throw new RuntimeException("没有Camera权限");
        }
    }

    /**
     * Basic initialization
     * 基本初始化
     */
    public void setupCameraPreview(final CameraWrapper cameraWrapper) {
        this.cameraWrapper = cameraWrapper;

        if (this.cameraWrapper != null) {
            removeAllViews();
            cameraPreview = new CameraPreview(getContext(), cameraWrapper, this, new CameraPreview.FocusAreaSetter() {
                @Override
                public void setAutoFocusArea() {
                    setupFocusAreas();//设置对焦区域 / Set the focus area
                }
            });
            addView(cameraPreview);
            addView(((View) viewFinderView));
        } else {
            LogUtils.e(TAG, "相机打开失败");
        }
    }

    /**
     * Set the focus area
     * 设置对焦区域
     */
    private void setupFocusAreas() {
        if (!shouldAdjustFocusArea) return;

        if (cameraWrapper == null) return;

        Camera.Parameters parameters = cameraWrapper.camera.getParameters();
        if (parameters.getMaxNumFocusAreas() <= 0) {
            LogUtils.e(TAG, "不支持设置对焦区域");
            return;
        }

        if (focusAreas == null) {
            int width = 2000, height = 2000;
            Rect framingRect = viewFinderView.getFramingRect();//获得扫码框区域 / Get the scan code area
            if (framingRect == null) return;
            int viewFinderViewWidth = ((View) viewFinderView).getWidth();
            int viewFinderViewHeight = ((View) viewFinderView).getHeight();

            //Scale the focus area according to the size ratio of ViewFinderView and 2000*2000
            //1.根据ViewFinderView和2000*2000的尺寸之比，缩放对焦区域
            Rect scaledRect = new Rect(framingRect);
            scaledRect.left = scaledRect.left * width / viewFinderViewWidth;
            scaledRect.right = scaledRect.right * width / viewFinderViewWidth;
            scaledRect.top = scaledRect.top * height / viewFinderViewHeight;
            scaledRect.bottom = scaledRect.bottom * height / viewFinderViewHeight;

            //2.旋转对焦区域 / Rotate the focus area
            Rect rotatedRect = new Rect(scaledRect);
            int rotationCount = getRotationCount();
            if (rotationCount == 1) {//若相机图像需要顺时针旋转90度，则将扫码框逆时针旋转90度
                //If the camera image needs to be rotated 90 degrees clockwise, rotate the scan frame 90 degrees counterclockwise
                rotatedRect.left = scaledRect.top;
                rotatedRect.top = 2000 - scaledRect.right;
                rotatedRect.right = scaledRect.bottom;
                rotatedRect.bottom = 2000 - scaledRect.left;
            } else if (rotationCount == 2) {//若相机图像需要顺时针旋转180度,则将扫码框逆时针旋转180度
                //If the camera image needs to be rotated 180 degrees clockwise, rotate the scan frame 180 degrees counterclockwise
                rotatedRect.left = 2000 - scaledRect.right;
                rotatedRect.top = 2000 - scaledRect.bottom;
                rotatedRect.right = 2000 - scaledRect.left;
                rotatedRect.bottom = 2000 - scaledRect.top;
            } else if (rotationCount == 3) {//若相机图像需要顺时针旋转270度，则将扫码框逆时针旋转270度
                //If the camera image needs to be rotated 270 degrees clockwise, rotate the scan frame counterclockwise by 270 degrees
                rotatedRect.left = 2000 - scaledRect.bottom;
                rotatedRect.top = scaledRect.left;
                rotatedRect.right = 2000 - scaledRect.top;
                rotatedRect.bottom = scaledRect.right;
            }

            //3.坐标系平移 / Coordinate system translation
            Rect rect = new Rect(rotatedRect.left - 1000, rotatedRect.top - 1000, rotatedRect.right - 1000, rotatedRect.bottom - 1000);

            Camera.Area area = new Camera.Area(rect, 1000);
            focusAreas = new ArrayList<>();
            focusAreas.add(area);
        }

        parameters.setFocusAreas(focusAreas);
        cameraWrapper.camera.setParameters(parameters);
    }

    /**
     * Free resources such as camera resources
     * 释放相机资源等各种资源
     */
    public void stopCamera() {
        if (cameraHandlerThread != null) {
            cameraHandlerThread.quit();
            cameraHandlerThread = null;
        }

        if (cameraWrapper != null) {
            cameraPreview.stopCameraPreview();//停止相机预览并置空各种回调 / Stop camera preview and blank various callbacks
            cameraPreview.setCamera(null, null);

            cameraWrapper.camera.release();//释放资源 Release resources
            cameraWrapper = null;
        }
    }

    /**
     * Scale the scan area according to the size ratio of ViewFinderView and preview
     * 根据ViewFinderView和preview的尺寸之比，缩放扫码区域
     */
    public Rect getScaledRect(int previewWidth, int previewHeight) {
        if (scaledRect == null) {
            Rect framingRect = viewFinderView.getFramingRect();//获得扫码框区域 / Get the scan code area
            int viewFinderViewWidth = ((View) viewFinderView).getWidth();
            int viewFinderViewHeight = ((View) viewFinderView).getHeight();

            int width, height;
            if (DisplayUtils.getScreenOrientation(getContext()) == Configuration.ORIENTATION_PORTRAIT//竖屏使用 / Vertical screen use
                    && previewHeight < previewWidth) {
                width = previewHeight;
                height = previewWidth;
            } else if (DisplayUtils.getScreenOrientation(getContext()) == Configuration.ORIENTATION_LANDSCAPE//横屏使用 / Horizontal screen use
                    && previewHeight > previewWidth) {
                width = previewHeight;
                height = previewWidth;
            } else {
                width = previewWidth;
                height = previewHeight;
            }

            scaledRect = new Rect(framingRect);
            scaledRect.left = scaledRect.left * width / viewFinderViewWidth;
            scaledRect.right = scaledRect.right * width / viewFinderViewWidth;
            scaledRect.top = scaledRect.top * height / viewFinderViewHeight;
            scaledRect.bottom = scaledRect.bottom * height / viewFinderViewHeight;
        }

        return scaledRect;
    }

    public Rect getRotatedRect(int previewWidth, int previewHeight, Rect rect) {
        if (rotatedRect == null) {
            int rotationCount = getRotationCount();
            rotatedRect = new Rect(rect);

            if (rotationCount == 1) {//若相机图像需要顺时针旋转90度，则将扫码框逆时针旋转90度
                //If the camera image needs to be rotated 90 degrees clockwise, rotate the scan frame 90 degrees counterclockwise
                rotatedRect.left = rect.top;
                rotatedRect.top = previewHeight - rect.right;
                rotatedRect.right = rect.bottom;
                rotatedRect.bottom = previewHeight - rect.left;
            } else if (rotationCount == 2) {//若相机图像需要顺时针旋转180度,则将扫码框逆时针旋转180度
                //If the camera image needs to be rotated 180 degrees clockwise, rotate the scan frame 180 degrees counterclockwise
                rotatedRect.left = previewWidth - rect.right;
                rotatedRect.top = previewHeight - rect.bottom;
                rotatedRect.right = previewWidth - rect.left;
                rotatedRect.bottom = previewHeight - rect.top;
            } else if (rotationCount == 3) {//若相机图像需要顺时针旋转270度，则将扫码框逆时针旋转270度
                //If the camera image needs to be rotated 270 degrees clockwise, rotate the scan frame counterclockwise by 270 degrees
                rotatedRect.left = previewWidth - rect.bottom;
                rotatedRect.top = rect.left;
                rotatedRect.right = previewWidth - rect.top;
                rotatedRect.bottom = rect.right;
            }
        }

        return rotatedRect;
    }

    /**
     * Rotate data
     * 旋转data
     */
    public byte[] rotateData(byte[] data, Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        int width = parameters.getPreviewSize().width;
        int height = parameters.getPreviewSize().height;

        int rotationCount = getRotationCount();
        for (int i = 0; i < rotationCount; i++) {
            byte[] rotatedData = new byte[data.length];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++)
                    rotatedData[x * height + height - y - 1] = data[x + y * width];
            }
            data = rotatedData;
            int tmp = width;
            width = height;
            height = tmp;
        }

        return data;
    }

    /**
     * Get (rotation angle / 90)
     * 获取（旋转角度/90）
     */
    private int getRotationCount() {
        int displayOrientation = cameraPreview.getDisplayOrientation();
        return displayOrientation / 90;
    }

//-------------------------------------------------------------------------

    /**
     * Turn the flash on/off
     * 开启/关闭闪光灯
     */
    public void setFlash(boolean flag) {
        if (cameraWrapper != null && CameraUtils.isFlashSupported(cameraWrapper.camera)) {
            Camera.Parameters parameters = cameraWrapper.camera.getParameters();
            if (flag) {
                if (parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH)) {
                    return;
                }
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            } else {
                if (parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_OFF)) {
                    return;
                }
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            }
            cameraWrapper.camera.setParameters(parameters);
        }
    }

    /**
     * Switch the lighting state of the flash
     * 切换闪光灯的点亮状态
     */
    public void toggleFlash() {
        if (cameraWrapper != null && CameraUtils.isFlashSupported(cameraWrapper.camera)) {
            Camera.Parameters parameters = cameraWrapper.camera.getParameters();
            if (parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            } else {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            }
            cameraWrapper.camera.setParameters(parameters);
        }
    }

    /**
     * Is the flash lit?
     * 闪光灯是否被点亮
     */
    public boolean isFlashOn() {
        if (cameraWrapper != null && CameraUtils.isFlashSupported(cameraWrapper.camera)) {
            Camera.Parameters parameters = cameraWrapper.camera.getParameters();
            if (parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH)) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    /**
     * Set whether to adjust the position of the focus area according to the position of the scan code frame
     * The default value is false, that is, no adjustment, the system default configuration will be used,
     * then the focus area will be in the center of the preview screen.
     * (Tested, this function is invalid for a few models, to be optimized)
     * 设置是否要根据扫码框的位置去调整对焦区域的位置<br/>
     * 默认值为false，即不调整，会使用系统默认的配置，那么对焦区域会位于预览画面的中央<br/>
     * <br/>
     * (经测试，此功能功能对少数机型无效，待优化)
     */
    public void setShouldAdjustFocusArea(boolean shouldAdjustFocusArea) {
        this.shouldAdjustFocusArea = shouldAdjustFocusArea;
    }
}