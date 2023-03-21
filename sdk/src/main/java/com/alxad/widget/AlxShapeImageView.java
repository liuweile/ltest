package com.alxad.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.alxad.R;
import com.alxad.analytics.AlxAgent;

/**
 * 矩形ImageView<br/>
 * 支持圆形，圆角，带边框的功能，支持gif的动画效果
 *
 * @author liuweile
 * @date 2021-11-9
 */
@SuppressLint("AppCompatCustomView")
public class AlxShapeImageView extends ImageView {
    private static final String TAG = "AlxShapeImageView";

    private final int SHAPE_DEFAULT = 0;//默认不处理
    private final int SHAPE_CIRCULAR = 1;//圆
    private final int SHAPE_RECTANGLE = 2;//矩形

    private int mShape = SHAPE_DEFAULT;
    private int mBorderColor;
    private float mBorderSize;
    private float mRoundRectRadius; //矩形圆角大小

    private Context mContext;

    private Paint mBorderPaint = new Paint();

    private RectF destination;
    private Path path = new Path();
    private Path maskPath;

    public AlxShapeImageView(Context context) {
        super(context);
        init(context, null);
    }

    public AlxShapeImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public AlxShapeImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        if (attrs != null && context != null) {
            try {
                TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AlxShapeImageView);
                mShape = a.getInt(R.styleable.AlxShapeImageView_alx_siv_shape, SHAPE_DEFAULT);
                mBorderColor = a.getColor(R.styleable.AlxShapeImageView_alx_siv_border_color, 0);
                mBorderSize = a.getDimension(R.styleable.AlxShapeImageView_alx_siv_border_size, 0);
                mRoundRectRadius = a.getDimension(R.styleable.AlxShapeImageView_alx_siv_rect_radius, 0);
                a.recycle();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setColor(mBorderColor);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setStrokeWidth(mBorderSize);

        destination = new RectF();
        maskPath = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //添加异常主要是为了以防万一，后期尽量去掉
        try {
            if (mShape == SHAPE_CIRCULAR || mShape == SHAPE_RECTANGLE) {
                drawStroke(canvas);//画边框
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    canvas.clipPath(maskPath);//裁切圆形或圆角矩形
                } else {
                    canvas.clipPath(maskPath, Region.Op.INTERSECT);//裁切圆形或圆角矩形
                }
            }
            super.onDraw(canvas);
        } catch (Exception e) {
            e.printStackTrace();
            AlxAgent.onError(e);
        }
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        if (mShape == SHAPE_CIRCULAR || mShape == SHAPE_RECTANGLE) {
            updateShapeMask(width, height);
        }
    }

    private void updateShapeMask(int width, int height) {
        //添加异常主要是为了以防万一，后期尽量去掉
        try {
            destination.set(
                    getPaddingLeft() + mBorderSize,
                    getPaddingTop() + mBorderSize,
                    width - getPaddingRight() - mBorderSize,
                    height - getPaddingBottom() - mBorderSize);
            if (mShape == SHAPE_CIRCULAR) {
                calculateCircularPath(destination, path);
            } else if (mShape == SHAPE_RECTANGLE) {
                calculateRectPath(destination, path);
            }
            // Remove path from rect to draw with clear paint.
            maskPath.rewind();
            maskPath.addPath(path);
        } catch (Exception e) {
            e.printStackTrace();
            AlxAgent.onError(e);
        }
    }

    private void calculateCircularPath(RectF bounds, Path path) throws Exception {
        path.rewind();
        float width = bounds.right - bounds.left;
        float height = bounds.bottom - bounds.top;
        int radius = (int) (Math.min(width / 2, height / 2) - mBorderSize / 2);

        path.addCircle(width / 2, height / 2, radius, Path.Direction.CW);
        path.close();
    }

    private void calculateRectPath(RectF bounds, Path path) throws Exception {
        path.rewind();
        path.addRoundRect(bounds, mRoundRectRadius, mRoundRectRadius, Path.Direction.CW);
        path.close();
    }

    private void drawStroke(Canvas canvas) {
        //添加异常主要是为了以防万一，后期尽量去掉
        try {
            if (getDrawable() == null) {
                return;
            }
            if (mBorderColor == 0 || mBorderSize <= 0) {
                return;
            }
            canvas.drawPath(path, mBorderPaint);
        } catch (Exception e) {
            e.printStackTrace();
            AlxAgent.onError(e);
        }
    }

}