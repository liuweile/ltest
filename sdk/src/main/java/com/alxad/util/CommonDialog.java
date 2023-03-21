package com.alxad.util;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.alxad.R;


public class CommonDialog extends Dialog implements View.OnClickListener {
    private TextView contentTxt;
    private TextView titleTxt;
    private TextView submitTxt;
    private TextView cancelTxt;

    private Context mContext;
    private String content;
    private OnCloseListener listener;
    private String positiveName;
    private String negativeName;
    private String title;

    public CommonDialog(Context context) {
        super(context);
        this.mContext = context;
    }

    public CommonDialog(Context context, int themeResId, String content) {
        super(context, themeResId);
        this.mContext = context;
        this.content = content;
    }

    public CommonDialog(Context context, int themeResId, String content, OnCloseListener listener) {
        super(context, themeResId);
        this.mContext = context;
        this.content = content;
        this.listener = listener;
    }

    protected CommonDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        this.mContext = context;
    }

    public CommonDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public CommonDialog setPositiveButton(String name) {
        this.positiveName = name;
        return this;
    }

    public CommonDialog setNegativeButton(String name) {
        this.negativeName = name;
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alx_dialog_ok_cancel);
        setCanceledOnTouchOutside(false);
        initView();
    }

    private void initView() {
        contentTxt = (TextView) findViewById(R.id.alx_content);
        titleTxt = (TextView) findViewById(R.id.alx_title);
        submitTxt = (TextView) findViewById(R.id.alx_dialog_ok);
        submitTxt.setOnClickListener(this);
        cancelTxt = (TextView) findViewById(R.id.alx_dialog_cancel);
        cancelTxt.setOnClickListener(this);

        contentTxt.setText(content);
        if (!TextUtils.isEmpty(positiveName)) {
            submitTxt.setText(positiveName);
        }

        if (!TextUtils.isEmpty(negativeName)) {
            cancelTxt.setText(negativeName);
        }

        if (!TextUtils.isEmpty(title)) {
            titleTxt.setText(title);
        }

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.alx_dialog_cancel) {
            this.dismiss();
            if (listener != null) {
                listener.onClick(this, false);
            }
        } else if (id == R.id.alx_dialog_ok) {
            if (listener != null) {
                this.dismiss();
                listener.onClick(this, true);
            }
        }
    }

    public interface OnCloseListener {
        void onClick(Dialog dialog, boolean confirm);
    }
}
