package com.demo.opencv;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.demo.opencv.infoUtil.ActivityCollector;
import com.demo.opencv.infoUtil.widget.TitleLayout;
import com.demo.opencv.models.LoginUser;

import org.litepal.LitePal;

import java.util.List;

public class EditName extends AppCompatActivity {

    private LoginUser loginUser;
    private TitleLayout tl_title;
    private EditText edit_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);
        setContentView(R.layout.activity_edit_name);

        SharedPreferences preferences = getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
        String username = preferences.getString("username", "");
        List<LoginUser> loginUsers = LitePal.select("name").where("name = ?", username).find(LoginUser.class);
        loginUser = loginUsers.get(0);

        LoginUser loginUser1 = new LoginUser();
        List<LoginUser> logins = LitePal.findAll(LoginUser.class);
        for (LoginUser lo : logins) {
            if (lo.getName().equals(username)) {
                loginUser1 = lo;
            }
        }

        tl_title = (TitleLayout) findViewById(R.id.tl_title);
        edit_name = (EditText) findViewById(R.id.et_edit_name);
        edit_name.setText(loginUser1.getRegion());

        //Setting up a listener
        //If you click Finish, update the loginUser and destroy
        tl_title.getTextView_forward().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues values = new ContentValues();
                values.put("region", edit_name.getText().toString());
                LitePal.updateAll(LoginUser.class, values, "name = ?", loginUser.getName());
//                loginUser.setName(edit_name.getText().toString());
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
}