package com.demo.opencv;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.demo.opencv.models.LoginUser;
import com.demo.opencv.models.Want;

import org.litepal.LitePal;

import java.util.List;

public class TradeAdapterReq extends ArrayAdapter {
    public TradeAdapterReq(@NonNull Context context, int resource, @NonNull List<Want> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Want want = (Want) getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_trade_item, parent,false);
        ImageView artimage =view.findViewById(R.id.want_art_image);
        TextView traderole =view.findViewById(R.id.want_art_role);
        TextView stage = view.findViewById(R.id.want_stage);
        ImageView ownerimage =view.findViewById(R.id.want_other_image);

        String username = want.getOwnerName();
        LoginUser userData = LitePal.where("name = ?", username).findFirst(LoginUser.class);

        if (userData.getPortrait() != null) {
            ownerimage.setImageBitmap(Bytes2Bimap(userData.getPortrait()));
        } else {
            ownerimage.setImageResource(R.drawable.user_image);
        }



        artimage.setImageBitmap(Bytes2Bimap(want.getImage()));
        traderole.setText("OWNS");
        stage.setText(want.getStatue());
        return view;
    }


    public Bitmap Bytes2Bimap(byte[] b) {
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } else {
            return null;
        }
    }
}
