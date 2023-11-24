package com.demo.opencv;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.opencv.confirmCodeUtil.InputCodeLayout;
import com.demo.opencv.models.Owns;
import com.demo.opencv.models.Want;

import org.litepal.LitePal;

import java.util.List;

public class ConfirmCodeActivity extends AppCompatActivity {

    private InputCodeLayout mInputCodeLayout;
    private TextView textViewHint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_code);

        Bundle myBundle = this.getIntent().getExtras();
        String uniqueCode = myBundle.getString("code");


        Want want = new Want();
        List<Want> wants = LitePal.findAll(Want.class);
        for (Want w:wants) {
            if (w.getTradeCode().equals(uniqueCode)) {
                want = w;
            }
        }

        SharedPreferences preferences = getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
        String username = preferences.getString("username", "");
        textViewHint = findViewById(R.id.confirm_hint);
        if (username.equals(want.getOwnerName())) {
            textViewHint.setText(want.getTradeCode());
        }

        //Find the Owns for this painting according to the code and change the permissions

        mInputCodeLayout = findViewById(R.id.inputCodeLayout);
        mInputCodeLayout.setOnInputCompleteListener(new InputCodeLayout.OnInputCompleteCallback() {
            @Override
            public void onInputCompleteListener(String code) {
                Toast.makeText(ConfirmCodeActivity.this, "Verification code entered is：" + code, Toast.LENGTH_SHORT).show();
                Want want = new Want();
                List<Want> wants = LitePal.findAll(Want.class);
                for (Want w:wants) {
                    if (w.getTradeCode().equals(uniqueCode)) {
                        want = w;
                    }
                }
                SharedPreferences preferences = getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
                String username = preferences.getString("username", "");

                if (username.equals(want.getOwnerName())) {
                    if (code.equals(uniqueCode)) {
                        //want status changed to WAIT_REQ
                        ContentValues values = new ContentValues();
                        values.put("statue", "WAIT_REQ");
                        LitePal.updateAll(Want.class, values, "tradeCode = ?", uniqueCode);
                        Toast.makeText(ConfirmCodeActivity.this, "Owner authentication is complete, waiting for the requester", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ConfirmCodeActivity.this, "Confirm code does not exist", Toast.LENGTH_SHORT).show();
                    }
                } else if (username.equals(want.getReqName())) {
                    if (code.equals(uniqueCode)) {
                        //want status changed to DEAL and ownership of Owns modified
                        ContentValues values = new ContentValues();
                        values.put("statue", "DEAL");
                        LitePal.updateAll(Want.class, values, "tradeCode = ?", uniqueCode);

                        //Change of ownership
                        byte[] image = want.getImage();

                        List<Owns> owns = LitePal.findAll(Owns.class);
                        for (Owns o:owns) {
                            Bitmap ownsBit = Bytes2Bimap(o.getImage());
                            Bitmap thisBit = Bytes2Bimap(image);
                            if (HashUtil.hashCompare(ownsBit, thisBit).equals("LIKE")) {
                                String title = o.getTitle();
                                ContentValues valuesO = new ContentValues();

                                valuesO.put("username", username);
                                LitePal.updateAll(Owns.class, valuesO, "title = ?", title);
                                Toast.makeText(ConfirmCodeActivity.this, "The ownership transfer is complete", Toast.LENGTH_SHORT).show();
                            }
                        }


                    } else {
                        Toast.makeText(ConfirmCodeActivity.this, "Confirm code does not exist", Toast.LENGTH_SHORT).show();
                    }
                } else {
//
                }
            }
        });
    }

    public void normal(View v) {
        mInputCodeLayout.setShowMode(InputCodeLayout.NORMAL);
    }

    public void password(View v) {
        mInputCodeLayout.setShowMode(InputCodeLayout.PASSWORD);
    }

    public void clear(View v) {
        mInputCodeLayout.clear();
    }

    public Bitmap Bytes2Bimap(byte[] b) {
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } else {
            return null;
        }
    }


}