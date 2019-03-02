package com.blueduck.ride.qrcode.base;

import android.graphics.Rect;

public interface IViewFinder {

    /**
     * Obtain the scan code area (identification area)
     * 获得扫码区域(识别区域)
     */
    Rect getFramingRect();
}