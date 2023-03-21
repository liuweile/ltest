package com.alxad.widget;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.alxad.R;

/**
 * 广告logo 布局标记
 *
 * @author lwl
 * @date 2021-8-4
 */
public class AlxLogoView extends LinearLayout {

    private final String TEXT_COLOR = "#FFFFFF";
    private final int TEXT_SIZE = 10;//字体大小 sp
    private final int TEXT_MARGIN_LEFT = 2;//dp

    private final int IMAGE_WIDTH = 10;//dp
    private final int IMAGE_HEIGHT = 10;//dp

    private final int PADDING_LEFT = 3;//dp
    private final int PADDING_RIGHT = 3;//dp

    private Context mContext;
    private TextView mTvTitle;
    private ImageView mIvImage;

    private int mTextMarginLeft;

    private int mImageWidth;
    private int mImageHeight;

    private int mPaddingLeft;
    private int mPaddingRight;

    public AlxLogoView(Context context) {
        super(context);
        initView(context);
    }

    public AlxLogoView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public AlxLogoView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        mContext = context;
        init();

        setOrientation(HORIZONTAL);
        setBackgroundResource(R.drawable.alx_logo_bg);
        setPadding(mPaddingLeft, 0, mPaddingRight, 0);
        setGravity(Gravity.CENTER_VERTICAL);

        addImageView();
        addTextView();
        setText(R.string.alx_ad_log_ad);
        setImage(R.drawable.alx_ad_logo);
    }

    private void init() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        mPaddingLeft = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, PADDING_LEFT, displayMetrics);
        mPaddingRight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, PADDING_RIGHT, displayMetrics);

        mTextMarginLeft = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, TEXT_MARGIN_LEFT, displayMetrics);

        mImageWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, IMAGE_WIDTH, displayMetrics);
        mImageHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, IMAGE_HEIGHT, displayMetrics);
    }

    private void addTextView() {
        mTvTitle = new TextView(mContext);
        mTvTitle.setTextColor(Color.parseColor(TEXT_COLOR));
        mTvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE);

        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            params.setMarginStart(mTextMarginLeft);
        } else {
            params.leftMargin = mTextMarginLeft;
        }
        mTvTitle.setLayoutParams(params);

        addView(mTvTitle);
    }

    private void addImageView() {
        mIvImage = new ImageView(mContext);
        LayoutParams params = new LayoutParams(mImageWidth, mImageHeight);
        mIvImage.setLayoutParams(params);

        addView(mIvImage);
    }

    public void setText(int resId) {
        mTvTitle.setText(resId);
    }

    public void setImage(int resId) {
        mIvImage.setImageResource(resId);
    }

}