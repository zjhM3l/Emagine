package com.demo.opencv;

import android.app.Activity;
import android.content.Context;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

public class CreateUserPopWin extends PopupWindow {
    private Context mContext;

    private View view;

    private Button btn_save_pop;

    public EditText text_name, text_price;


    public CreateUserPopWin(Activity mContext, View.OnClickListener itemsOnClick) {

        this.mContext = mContext;

        this.view = LayoutInflater.from(mContext).inflate(R.layout.create_user_dialog, null);

        text_name = (EditText) view.findViewById(R.id.text_name);

        text_price = (EditText) view.findViewById(R.id.text_price);

        btn_save_pop =  (Button) view.findViewById(R.id.btn_save_pop);

        // Set button monitoring
        btn_save_pop.setOnClickListener(itemsOnClick);

        // Settings externally clickable
        this.setOutsideTouchable(true);


        /* Set pop-up characteristics */
        // Set the view
        this.setContentView(this.view);

        // Sets the width and height of the pop-up form
        Window dialogWindow = mContext.getWindow();

        WindowManager m = mContext.getWindowManager();
        Display d = m.getDefaultDisplay(); // Get the screen width and height for use
        WindowManager.LayoutParams p = dialogWindow.getAttributes(); // Gets the current parameter value of the dialog box

        this.setHeight(RelativeLayout.LayoutParams.WRAP_CONTENT);
        this.setWidth((int) (d.getWidth() * 0.8));

        // The settings pop-up form is clickable
        this.setFocusable(true);
    }
}
