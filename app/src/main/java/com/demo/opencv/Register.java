package com.demo.opencv;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.demo.opencv.models.LoginUser;
import com.demo.opencv.models.UserData;

import org.litepal.LitePal;

import java.util.List;

public class Register extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Button re_register = findViewById(R.id.re_register);
        re_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText username = findViewById(R.id.re_username);
                EditText password = findViewById(R.id.re_password);
                EditText passwordAffirm = findViewById(R.id.re_affirm);
                String inputUsername = username.getText().toString();
                String inputPassword = password.getText().toString();
                String inputAffirm = passwordAffirm.getText().toString();
                UserData user = new UserData();
                LoginUser userInfo = new LoginUser();
                List<UserData> userDataList = LitePal.findAll(UserData.class);
                String check = "DIFFERENT";
                for (UserData userData:userDataList) {
                    if (userData.getUsername().equals(username.getText().toString())) {
                        check = "SAME";
                    }
                }
                if (check.equals("SAME")) {
                    Toast.makeText(Register.this,"The user name already exists", Toast.LENGTH_SHORT).show();
                } else {
                    if (inputPassword.isEmpty()) {
                        Toast.makeText(Register.this,"Please enter a password", Toast.LENGTH_SHORT).show();
                    } else {
                        if (inputAffirm.equals(inputPassword)) {
                            //Store the account password
                            user.setUsername(inputUsername);
                            user.setPassword(inputPassword);
                            user.save();
                            //store the account Info
                            userInfo.setName(inputUsername);
                            userInfo.save();
                            //Pass back the account
                            Intent intent = new Intent();
                            intent.putExtra("username", inputUsername);
                            setResult(RESULT_OK, intent);
                            finish();
                        } else {
                            Toast.makeText(Register.this,"The passwords do not match twice", Toast.LENGTH_SHORT).show();
                        }
                    }

                }
            }
        });
    }
}