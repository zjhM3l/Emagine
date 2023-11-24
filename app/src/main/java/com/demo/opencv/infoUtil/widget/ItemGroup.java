package com.demo.opencv.infoUtil.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.demo.opencv.R;


public class ItemGroup extends FrameLayout{

    private LinearLayout itemGroupLayout; //Layout of combination controls
    private TextView titleTv; //Title
    private TextView contentEdt; //Input box
    private ImageView jtRightIv; //Arrow to the right

    public TextView getContentEdt() {
        return contentEdt;
    }

    public ItemGroup(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public ItemGroup(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
        initAttrs(context, attrs);
    }

    public ItemGroup(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
        initAttrs(context,attrs);
    }

    //Initialize View
    private void initView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_group_layout, null);
        itemGroupLayout = (LinearLayout) view.findViewById(R.id.item_group_layout);
        titleTv = (TextView) view.findViewById(R.id.title_tv);
        contentEdt = (TextView) view.findViewById(R.id.content_edt);
        jtRightIv = (ImageView) view.findViewById(R.id.jt_right_iv);
        addView(view); //Add the custom layout of this combination control to the current FramLayout
    }
//    Initialise related properties, introduce related properties

    private void initAttrs(Context context, AttributeSet attrs) {
        //Default font colour for headings
        int defaultTitleColor = context.getResources().getColor(R.color.gray3);
        //Default font colour for input boxes
        int defaultEdtColor = context.getResources().getColor(R.color.black0);
        //Font colour of the default prompt content of the input box
        int defaultHintColor = context.getResources().getColor(R.color.gray9);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ItemGroup);
        String title = typedArray.getString(R.styleable.ItemGroup_title);
        float paddingLeft = typedArray.getDimension(R.styleable.ItemGroup_paddingLeft, 15);
        float paddingRight = typedArray.getDimension(R.styleable.ItemGroup_paddingRight, 15);
        float paddingTop = typedArray.getDimension(R.styleable.ItemGroup_paddingTop, 5);
        float paddingBottom = typedArray.getDimension(R.styleable.ItemGroup_paddingTop, 5);
        float titleSize = typedArray.getDimension(R.styleable.ItemGroup_title_size, 15);
        int titleColor = typedArray.getColor(R.styleable.ItemGroup_title_color, defaultTitleColor);
        String content = typedArray.getString(R.styleable.ItemGroup_edt_content);
        float contentSize = typedArray.getDimension(R.styleable.ItemGroup_edt_text_size, 13);
        int contentColor = typedArray.getColor(R.styleable.ItemGroup_edt_text_color, defaultEdtColor);
        String hintContent = typedArray.getString(R.styleable.ItemGroup_edt_hint_content);
        int hintColor = typedArray.getColor(R.styleable.ItemGroup_edt_hint_text_color, defaultHintColor);
        //The default input box can be edited
        boolean isEditable = typedArray.getBoolean(R.styleable.ItemGroup_isEditable, true);
        //Whether the arrow icon to the right is visible or not, the default is visible
        boolean showJtIcon = typedArray.getBoolean(R.styleable.ItemGroup_jt_visible, true);
        typedArray.recycle();

        //Setting data
        //Set the inner margin of the item
        itemGroupLayout.setPadding((int) paddingLeft, (int) paddingTop, (int) paddingRight, (int) paddingBottom);
        titleTv.setText(title);
        titleTv.setTextSize(titleSize);
        titleTv.setTextColor(titleColor);

        contentEdt.setText(content);
        contentEdt.setTextSize(contentSize);
        contentEdt.setTextColor(contentColor);
        contentEdt.setHint(hintContent);
        contentEdt.setHintTextColor(hintColor);

        jtRightIv.setVisibility(showJtIcon ? View.VISIBLE : View.GONE);  //Set whether the rightward arrow icon is visible
    }
}