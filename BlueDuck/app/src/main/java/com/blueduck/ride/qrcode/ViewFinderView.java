package com.blueduck.ride.qrcode;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.widget.RelativeLayout;

import com.blueduck.ride.R;
import com.blueduck.ride.qrcode.base.IViewFinder;


/**
 * A view overlaid on the camera preview, including a scan code frame, a scan line, a shadow mask around the scan code frame, etc.
 * 覆盖在相机预览上的view，包含扫码框、扫描线、扫码框周围的阴影遮罩等
 */
public class ViewFinderView extends RelativeLayout implements IViewFinder {
    private Rect framingRect;//扫码框所占区域 Sweep the area occupied by the code frame
//    private float widthRatio = 0.86f;//扫码框宽度占view总宽度的比例
    private float widthRatio = 0.6f;//扫码框宽度占view总宽度的比例 The width of the scan code frame as a percentage of the total width of the view
    private float heightWidthRatio = 0.6f;//扫码框的高宽比 Sweep the aspect ratio of the code frame
    //Scan the code frame relative to the left offset, if it is negative, the scan code frame will be horizontally centered
    private int leftOffset = -1;//扫码框相对于左边的偏移量，若为负值，则扫码框会水平居中
//    private int topOffset = (int) (150*getContext().getResources().getDisplayMetrics().density);//扫码框相对于顶部的偏移量，若为负值，则扫码框会竖直居中
    //The offset of the scan code frame relative to the top. If it is negative, the scan code frame will be vertically centered.
    private int topOffset = -1;//扫码框相对于顶部的偏移量，若为负值，则扫码框会竖直居中

    private boolean isLaserEnabled = true;//是否显示扫描线 Whether to display the scan line
    private static final int[] laserAlpha = {0, 64, 128, 192, 255, 192, 128, 64};
    private int laserAlphaIndex;
    private static final long animationDelay = 10l;

    private int laserColor;//扫描线颜色 Scan line color
    private int maskColor;//阴影遮盖颜色 Shadow cover color
    private int borderColor;//扫描框颜色 Scan box color
    private final int borderStrokeWidth = 20;
    protected int borderLineLength = 80;//四个角线长度 Four corner lengths
    protected int borderLineBackGauge = 20;//四个角离边框距离 Four corners away from the border
    //新增部分 New section
    public int scannerStart = 0;
    public int scannerEnd = 0;
    private static final int SCANNER_LINE_HEIGHT = 10;  //扫描线宽度 Scan line width
    private static final int SCANNER_LINE_MOVE_DISTANCE = 5;  //扫描线移动距离 Scan line moving distance

    protected Paint laserPaint;
    protected Paint maskPaint;
    protected Paint borderPaint;

    private boolean isLicense = false;//是否扫描驾照 / Scan your driver's license

    public ViewFinderView(Context context,boolean isLicense) {
        super(context);
        this.isLicense = isLicense;
        initDraw();
//        initLayout();
    }

    private void initDraw() {
        setWillNotDraw(false);//需要进行绘制 Need to draw

        Resources resources = getResources();
        laserColor = resources.getColor(R.color.white);
        borderColor = resources.getColor(R.color.white);
        maskColor = resources.getColor(R.color.viewfinder_mask);

        //扫描线画笔 Scan line brush
        laserPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        laserPaint.setColor(laserColor);
//        laserPaint.setStyle(Paint.Style.FILL);

        //阴影遮罩画笔 Shadow mask brush
        maskPaint = new Paint();
        maskPaint.setColor(maskColor);

        //边框画笔 Border brush
        borderPaint = new Paint();
        borderPaint.setColor(borderColor);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(borderStrokeWidth);
        borderPaint.setAntiAlias(true);
    }

    /*private void initLayout() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_view_finder, this, true);
    }*/

    @Override
    public void onDraw(Canvas canvas) {
        if (getFramingRect() == null) {
            return;
        }

        drawViewFinderMask(canvas);
        drawViewFinderBorder(canvas);

        if(scannerStart == 0 || scannerEnd == 0) {
            scannerStart = framingRect.top;
            scannerEnd = framingRect.bottom;
        }

        if (isLaserEnabled) {
            //绘制扫描线闪烁 Draw scan line blinking
//            drawLaser(canvas);
//            绘制扫描线线性渐变 Draw a linear gradient of the scan line
            drawLaserScanner(canvas);
        }
    }

    /**
     * Draw a shadow mask around the scan code frame
     * 绘制扫码框四周的阴影遮罩
     */
    public void drawViewFinderMask(Canvas canvas) {
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        Rect framingRect = getFramingRect();

        canvas.drawRect(0, 0, width, framingRect.top, maskPaint);//扫码框顶部阴影 Sweep the top shadow of the code frame
        canvas.drawRect(0, framingRect.top, framingRect.left, framingRect.bottom, maskPaint);//扫码框左边阴影 Scan the left side of the code frame
        canvas.drawRect(framingRect.right, framingRect.top, width, framingRect.bottom, maskPaint);//扫码框右边阴影 Scan the right side of the code box
        canvas.drawRect(0, framingRect.bottom, width, height, maskPaint);//扫码框底部阴影 Scan the bottom of the code frame
    }

    /**
     * Draw the border of the scan code frame
     * 绘制扫码框的边框
     */
    public void drawViewFinderBorder(Canvas canvas) {
        Rect framingRect = getFramingRect();

        // 边框 border
        Path path = new Path();

        // Top-left corner 左上角
        path.moveTo(framingRect.left - borderLineBackGauge, framingRect.top + borderLineLength);
        path.lineTo(framingRect.left - borderLineBackGauge, framingRect.top - borderLineBackGauge);
        path.lineTo(framingRect.left + borderLineLength, framingRect.top - borderLineBackGauge);
        canvas.drawPath(path, borderPaint);

        // Top-right corner 右上角
        path.moveTo(framingRect.right + borderLineBackGauge, framingRect.top + borderLineLength);
        path.lineTo(framingRect.right + borderLineBackGauge, framingRect.top - borderLineBackGauge);
        path.lineTo(framingRect.right - borderLineLength, framingRect.top - borderLineBackGauge);
        canvas.drawPath(path, borderPaint);

        // Bottom-right corner 右下角
        path.moveTo(framingRect.right + borderLineBackGauge, framingRect.bottom - borderLineLength);
        path.lineTo(framingRect.right + borderLineBackGauge, framingRect.bottom + borderLineBackGauge);
        path.lineTo(framingRect.right - borderLineLength, framingRect.bottom + borderLineBackGauge);
        canvas.drawPath(path, borderPaint);

        // Bottom-left corner 左下角
        path.moveTo(framingRect.left - borderLineBackGauge, framingRect.bottom - borderLineLength);
        path.lineTo(framingRect.left - borderLineBackGauge, framingRect.bottom + borderLineBackGauge);
        path.lineTo(framingRect.left + borderLineLength, framingRect.bottom + borderLineBackGauge);
        canvas.drawPath(path, borderPaint);
    }

    //绘制扫描线 Draw scan lines
    private void drawLaserScanner(Canvas canvas) {
        Rect frame = getFramingRect();
        //线性渐变 Linear gradient
        LinearGradient linearGradient = new LinearGradient(
                frame.left, scannerStart,
                frame.left, scannerStart + SCANNER_LINE_HEIGHT,
                shadeColor(laserColor),
                laserColor,
                Shader.TileMode.MIRROR);

        RadialGradient radialGradient = new RadialGradient(
                (float)(frame.left + frame.width() / 2),
                (float)(scannerStart + SCANNER_LINE_HEIGHT / 2),
                360f,
                laserColor,
                shadeColor(laserColor),
                Shader.TileMode.MIRROR);
        SweepGradient sweepGradient = new SweepGradient(
                (float)(frame.left + frame.width() / 2),
                (float)(scannerStart + SCANNER_LINE_HEIGHT),
                shadeColor(laserColor),
                laserColor);
        ComposeShader composeShader = new ComposeShader(radialGradient, linearGradient, PorterDuff.Mode.ADD);
        laserPaint.setShader(radialGradient);
        if(scannerStart < scannerEnd) {
            //椭圆 oval
            RectF rectF = new RectF(frame.left + 2 * SCANNER_LINE_HEIGHT, scannerStart, frame.right - 2 * SCANNER_LINE_HEIGHT, scannerStart + SCANNER_LINE_HEIGHT);
            canvas.drawOval(rectF, laserPaint);
            scannerStart += SCANNER_LINE_MOVE_DISTANCE;
        } else {
            scannerStart = frame.top;
        }
        laserPaint.setShader(null);

        //区域刷新 Area refresh
        refreshScanArea();
    }

    //处理颜色模糊 Handling color blur
    public int shadeColor(int color) {
        String hax = Integer.toHexString(color);
        String result = "20"+hax.substring(2);
        return Integer.valueOf(result, 16);
    }

    /**
     * Draw scan lines
     * 绘制扫描线
     */
    public void drawLaser(Canvas canvas) {
        Rect framingRect = getFramingRect();

        laserPaint.setAlpha(laserAlpha[laserAlphaIndex]);
        laserAlphaIndex = (laserAlphaIndex + 1) % laserAlpha.length;
        int middle = framingRect.height() / 2 + framingRect.top;
        canvas.drawRect(framingRect.left + 1, middle - 1, framingRect.right - 1, middle + 1, laserPaint);

        //区域刷新 Area refresh
        refreshScanArea();
    }

    /**
     * Refresh the scan box area
     * 刷新扫描框区域
     */
    private void refreshScanArea(){
        postInvalidateDelayed(animationDelay,
                framingRect.left,
                framingRect.top,
                framingRect.right,
                framingRect.bottom);
    }

    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
        updateFramingRect();
    }

    /**
     * Set the value of framingRect (the area occupied by the scan code frame)
     * 设置framingRect的值（扫码框所占的区域）
     */
    public synchronized void updateFramingRect() {
        Point viewSize = new Point(getWidth(), getHeight());
        int width, height;
        if (isLicense){
            width = (int) (getWidth() * 0.86f);
            height = (int) (heightWidthRatio * width);
        }else{
            width = (int) (getWidth() * 0.6f);
            height = width;
        }

        int left, top;
        if (leftOffset < 0) {
            left = (viewSize.x - width) / 2;//水平居中 Horizontally centered
        } else {
            left = leftOffset;
        }
        if (topOffset < 0) {
            top = (viewSize.y - height) / 2;//竖直居中 Vertically centered
//            top = viewSize.y / 2 - height / 4;//竖直偏下 Vertically lower
        } else {
            top = topOffset;
        }
        framingRect = new Rect(left, top, left + width, top + height);
    }

    public Rect getFramingRect() {
        return framingRect;
    }
}