package com.djtiyu.m.djtiyu.util;

/**
 * Created by jov on 2015/1/26.
 */

import android.app.ProgressDialog;
import android.content.Context;
import android.view.Gravity;
import android.widget.TextView;

import com.djtiyu.m.djtiyu.R;

public class CustomProgressDialog extends ProgressDialog {
    private TextView tvMsg;
    public CustomProgressDialog(Context context, String strMessage, boolean cancelable) {
        this(context, R.style.CustomProgressDialog, strMessage, cancelable);
    }

    public CustomProgressDialog(Context context, int theme, String strMessage, boolean cancelable) {
        super(context, theme);
        this.show();
        this.setContentView(R.layout.progress_dialog);
        this.getWindow().getAttributes().gravity = Gravity.CENTER;
        tvMsg = (TextView) this.findViewById(R.id.loadingmsg);
        this.setCancelable(cancelable);
        setCanceledOnTouchOutside(false);
        if (tvMsg != null) {
            tvMsg.setText(strMessage);
        }
    }

    public static CustomProgressDialog show(Context context, String strMessage, boolean cancelable) {
        CustomProgressDialog dialog = new CustomProgressDialog(context, R.style.CustomProgressDialog, strMessage, cancelable);
        return dialog;
    }
    public void setMsg(String msg){
        if (tvMsg != null) {
            tvMsg.setText(msg);
        }
    }
}