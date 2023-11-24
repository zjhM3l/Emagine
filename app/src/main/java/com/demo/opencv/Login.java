package com.demo.opencv;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.opencv.models.LoginUser;
import com.demo.opencv.models.Owns;
import com.demo.opencv.models.UserData;

import org.litepal.LitePal;
import org.litepal.tablemanager.Connector;

import java.util.List;

public class Login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //Create a database
        Connector.getDatabase();

        //Initialize the login state store
        EditText username = findViewById(R.id.lg_username);
        EditText password = findViewById(R.id.lg_password);
        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        username.setText(sharedPreferences.getString("username", ""));


        TextView register = findViewById(R.id.lg_register);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(Login.this, Register.class), 1);
            }
        });

        //login
        Button loginButton = findViewById(R.id.lg_login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<UserData> users = LitePal.findAll(UserData.class);
                //Account password matching
                if (users.size() == 0) {
                    Toast.makeText(Login.this,"The account or password is incorrect！",Toast.LENGTH_SHORT).show();
                }

                String check = "NOEXIST";
                for (UserData user : users) {
                    if (user.getUsername().equals(username.getText().toString()) && user.getPassword().equals(password.getText().toString())) {
                        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("username", username.getText().toString());
                        editor.putString("password", password.getText().toString());
                        editor.commit();
                        check = "EXIST";
                        Intent intent = new Intent(Login.this, MainActivity.class);

                        startActivity(intent);
                        finish();
                    }
                    if (check.equals("NOEXIST")) {
                        Toast.makeText(Login.this,"The account or password is incorrect！",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //Accept the account that is sent back
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    final EditText loginUsername = findViewById(R.id.lg_username);
                    String returnUsername = data.getStringExtra("username");
                    loginUsername.setText(returnUsername);
                    loginUsername.setSelection(returnUsername.length());
                }
                break;
            default:
        }
    }
}