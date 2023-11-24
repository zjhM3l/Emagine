package com.demo.opencv.confirmCodeUtil;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import androidx.annotation.IntDef;

import com.demo.opencv.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Reference from airsaid
 */
public class InputCodeLayout extends RelativeLayout implements TextWatcher, View.OnKeyListener {

    @IntDef({NORMAL, PASSWORD})
    @Retention(RetentionPolicy.SOURCE)
    @interface ShowMode {}

    public static final int NORMAL = 0;
    public static final int PASSWORD = 1;

    private final Context mContext;

    /** Number of input boxes */
    private int mNumber;
    /** Input box width */
    private int mWidth;
    /** Input box height */
    private int mHeight;
    /** Width of dividing line between input boxes */
    private int mDivideWidth;
    /** Enter text colour */
    private int mTextColor;
    /** Enter text size */
    private int mTextSize;
    /** Input box background with focus */
    private int mFocusBackground;
    /** Input box background without focus */
    private int mUnFocusBackground;
    /** Display mode */
    private int mShowMode;

    private LinearLayout mContainer;
    private TextView[] mTextViews;
    private EditText mEdtCode;

    public InputCodeLayout(Context context) {
        this(context, null);
    }

    public InputCodeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InputCodeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initViews();
        initAttrs(attrs);
        initListener();
    }

    private void initAttrs(AttributeSet attrs) {
        TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.InputCodeLayout);
        mNumber = a.getInt(R.styleable.InputCodeLayout_icl_number, -1);
        mWidth = a.getDimensionPixelSize(R.styleable.InputCodeLayout_icl_width, -1);
        mHeight = a.getDimensionPixelSize(R.styleable.InputCodeLayout_icl_height, -1);
        int divideWidth = a.getDimensionPixelSize(R.styleable.InputCodeLayout_icl_divideWidth, -1);
        if(divideWidth != -1) setDivideWidth(divideWidth);
        mTextColor = a.getColor(R.styleable.InputCodeLayout_icl_textColor, -1);
        mTextSize = a.getDimensionPixelSize(R.styleable.InputCodeLayout_icl_textSize, 14);
        mFocusBackground = a.getResourceId(R.styleable.InputCodeLayout_icl_focusBackground, -1);
        mUnFocusBackground = a.getResourceId(R.styleable.InputCodeLayout_icl_unFocusBackground, -1);
        mShowMode = a.getInt(R.styleable.InputCodeLayout_icl_showMode, NORMAL);
        int gravity = a.getInt(R.styleable.InputCodeLayout_android_gravity, -1);
        if(gravity != -1) setGravity(gravity);
        a.recycle();
    }

    private void initViews() {
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        mContainer = new LinearLayout(mContext);
        mContainer.setLayoutParams(params);
        mContainer.setOrientation(LinearLayout.HORIZONTAL);
        mContainer.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        addView(mContainer);

        mEdtCode = new EditText(mContext);
        mEdtCode.setLayoutParams(params);
        mEdtCode.setCursorVisible(false);
        mEdtCode.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
        mEdtCode.setBackgroundResource(android.R.color.transparent);
        addView(mEdtCode);
    }

    private void initListener() {
        mEdtCode.addTextChangedListener(this);
        mEdtCode.setOnKeyListener(this);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        setCode(s.toString());
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
            deleteCode();
            return true;
        }
        return false;
    }

    /**
     * Set the verification code.
     *
     * @param code Verification Code
     */
    private void setCode(String code) {
        if (TextUtils.isEmpty(code)) return;

        for (int i = 0; i < mTextViews.length; i++) {
            TextView textView = mTextViews[i];
            // If no text is set then set
            if (TextUtils.isEmpty(textView.getText().toString())) {
                textView.setText(code);
                // Cancel focus
                textView.setBackgroundResource(mUnFocusBackground);
                // Set next input box focus if next input box is available
                if (i < mTextViews.length - 1)
                    mTextViews[i + 1].setBackgroundResource(mFocusBackground);
                // Setting the input completion callback
                if (i == mTextViews.length - 1 && mOnInputCompleteCallback != null)
                    mOnInputCompleteCallback.onInputCompleteListener(getCode());
                // Jump out
                break;
            }
        }
        mEdtCode.setText("");
    }

    /**
     * Delete the captcha.
     */
    private void deleteCode() {
        for (int i = mTextViews.length - 1; i >= 0; i--) {
            TextView textView = mTextViews[i];
            // Delete if there is setting text
            if (!TextUtils.isEmpty(textView.getText().toString())) {
                textView.setText("");
                // Setting the focus
                textView.setBackgroundResource(mFocusBackground);
                // Unfocus the next input box if there is a next input box
                if (i < mTextViews.length - 1)
                    mTextViews[i + 1].setBackgroundResource(mUnFocusBackground);
                // Jump out
                break;
            }
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mContainer.post(new Runnable() {
            @Override
            public void run() {
                initTextView();
            }
        });
    }

    private void initTextView() {
        if(mNumber <= 0) return;

        int measuredWidth = mContainer.getMeasuredWidth();
        // Calculate the height of the input box based on the width of the control in the case of equal division
        int height = (measuredWidth - (mDivideWidth * (mNumber - 1))) / mNumber;

        mTextViews = new TextView[mNumber];
        mContainer.removeAllViews();
        for (int i = 0; i < mNumber; i++) {
            final TextView textView = new TextView(mContext);

            // Determines that if the width and height are not set, they are divided equally according to the width of the control
            if (mWidth != -1 && mHeight != -1) {
                textView.setWidth(mWidth);
                textView.setHeight(mHeight);
            } else {
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT + mDivideWidth, height, 1);
                textView.setLayoutParams(lp);
            }

            if (mTextSize != -1)
                textView.getPaint().setTextSize(mTextSize);
            if (mTextColor != -1)
                textView.setTextColor(mTextColor);
            if (mFocusBackground != -1 && mUnFocusBackground != -1)
                textView.setBackgroundResource(i != 0 ? mUnFocusBackground : mFocusBackground);

            textView.setGravity(Gravity.CENTER);
            textView.setFocusable(false);
            setShowMode(textView);
            mTextViews[i] = textView;
            mContainer.addView(textView);
        }

        mContainer.post(new Runnable() {
            @Override
            public void run() {
                mEdtCode.setHeight(mContainer.getMeasuredHeight());
            }
        });
    }

    /**
     * Set the number of input boxes.
     *
     * @param number Number of input boxes
     */
    public void setNumber(int number){
        if(mNumber != number){
            mNumber = number;
            mEdtCode.setFilters(new InputFilter[]{new InputFilter.LengthFilter(mNumber)});
            onFinishInflate();
        }
    }

    /**
     * Set the split line width.
     *
     * @param width Width of dividing line
     */
    public void setDivideWidth(int width){
        if(width != mDivideWidth){
            mDivideWidth = width;
            mContainer.setDividerDrawable(createDivideShape(mDivideWidth));
        }
    }

    private Drawable createDivideShape(int width) {
        GradientDrawable shape = new GradientDrawable();
        shape.setSize(width, 0);
        return shape;
    }

    /**
     * Set the width of the input box. (If width == -1, the size of the input box is divided equally according to the width of the control)
     *
     * @param width Input box width
     */
    public void setWidth(int width){
        if(mWidth != width){
            mWidth = width;
            onFinishInflate();
        }
    }

    /**
     * Set the height of the input box. (If height == -1, then the input box size is divided evenly by the width of the control)
     *
     * @param height Input box height
     */
    public void setHeight(int height){
        if(mHeight != height){
            mHeight = height;
            onFinishInflate();
        }
    }

    /**
     * Set the display mode.
     *
     * @param showMode Set via {@link #NORMAL} or {@link #PASSWORD} Default is {@link #NORMAL}
     */
    public void setShowMode(@ShowMode int showMode) {
        if (mShowMode != showMode) {
            mShowMode = showMode;
            for (TextView textView : mTextViews) {
                setShowMode(textView);
            }
        }
    }

    private void setShowMode(TextView textView){
        if (mShowMode == NORMAL)
            textView.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        else
            textView.setTransformationMethod(PasswordTransformationMethod.getInstance());
    }

    /**
     * Set the placement of the input box.
     *
     * @param gravity See {@link Gravity}
     */
    public void setGravity(int gravity) {
        if(mContainer != null)
            mContainer.setGravity(gravity);
    }

    /**
     * Get the verification code that has been entered.
     *
     * @return Verification Code
     */
    public String getCode() {
        StringBuilder sb = new StringBuilder();
        for (TextView textView : mTextViews) {
            sb.append(textView.getText().toString());
        }
        return sb.toString();
    }

    /**
     * Clear the input box.
     */
    public void clear() {
        for (int i = 0; i < mTextViews.length; i++) {
            TextView textView = mTextViews[i];
            textView.setText("");
            textView.setBackgroundResource(i != 0 ? mUnFocusBackground : mFocusBackground);
        }
    }

    private OnInputCompleteCallback mOnInputCompleteCallback;

    public interface OnInputCompleteCallback {
        /**
         * Input to complete listening.
         *
         * @param code Verification code entered
         */
        void onInputCompleteListener(String code);
    }

    /**
     * Set the input to complete listening.
     *
     * @param callback Callbacks
     */
    public void setOnInputCompleteListener(OnInputCompleteCallback callback) {
        this.mOnInputCompleteCallback = callback;
    }
}
