package com.demo.opencv;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.opencv.models.ArtData;
import com.demo.opencv.models.Owns;
import com.demo.opencv.models.Want;

import org.litepal.LitePal;
import org.opencv.android.OpenCVLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ArtInformationActivity extends AppCompatActivity {

    private TextView mTvArt, mTvPrice;
    private ImageView mIvArt;
    private Button btnOwner, btnWant;
    private String oo;

    private RecyclerView mRecyclerView;
    private ArtInformationActivity.GalleryAdapter mAdapter;
    private List<Bitmap> mShows;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OpenCVLoader.initDebug();
        //Screenshots are prohibited
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_art_information);

        mTvArt = findViewById(R.id.art_info_name);
        mIvArt = findViewById(R.id.art_info_image);
        mTvPrice = findViewById(R.id.price_info);
        Bundle myBundle = this.getIntent().getExtras();
        int position = myBundle.getInt("pos");
        List<ArtData> arts = LitePal.findAll(ArtData.class);
        byte[] image = arts.get(position).getImage();
        String name = arts.get(position).getName();
        String price = arts.get(position).getPrice();
        Bitmap bp = Bytes2Bimap(arts.get(position).getImage());
        mTvArt.setText(name);
        mIvArt.setImageBitmap(bp);
        mTvPrice.setText(price);

        Owns ownShip = LitePal.where("title = ?", name).findFirst(Owns.class);
        String ownerName = ownShip.getUsername();
        oo = ownerName;

        btnOwner = findViewById(R.id.btn_owner);
        btnWant = findViewById(R.id.btn_want);

        btnOwner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString("owner", ownerName);
                intent.putExtras(bundle);
                intent.setClass(ArtInformationActivity.this, UserOtherInfo.class);
                startActivity(intent);
            }
        });

        btnWant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences preferences = getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
                String username = preferences.getString("username", "");

                if (username.equals(ownerName)) {
                    Toast.makeText(ArtInformationActivity.this, "You own this!", Toast.LENGTH_SHORT).show();
                } else {
                    String check = "NOT EXIST";
                    List<Want> wants = LitePal.findAll(Want.class);
                    for (Want w:wants) {
                        Bitmap thisPic = Bytes2Bimap(w.getImage());
                        Bitmap existPic = Bytes2Bimap(image);
                        if (HashUtil.hashCompare(thisPic, existPic).equals("LIKE")) {
                            check = "EXIST";
                        }
                    }
                    if (check.equals("NOT EXIST")) {
                        Want trade = new Want();
                        trade.setTimeStamp();
                        trade.setExceptCost(price);
                        trade.setImage(image);
                        trade.setOwnerName(ownerName);
                        trade.setReqName(username);
                        trade.setStatue("WAIT_BOTH");

                        String tradeCode = getRandomString(5);
                        trade.setTradeCode(tradeCode);
                        trade.save();
                        Toast.makeText(ArtInformationActivity.this, "Request Sent", Toast.LENGTH_SHORT).show();
                    } else {
                        List<Want> wantList = LitePal.findAll(Want.class);
                        List<Want> alreadyWantList = new ArrayList<>();
                        for (Want w:wantList) {
                            Bitmap thisPic = Bytes2Bimap(w.getImage());
                            Bitmap existPic = Bytes2Bimap(image);
                            if (HashUtil.hashCompare(thisPic, existPic).equals("LIKE")) {
                                alreadyWantList.add(w);
                            }
                        }
                        String checkAgain = "NOT EXIST";
                        for (Want want:alreadyWantList) {
                            if (want.getReqName().equals(username)) {
                                checkAgain = "EXIST";
                            }
                        }
                        if (checkAgain.equals("EXIST")) {
                            Toast.makeText(ArtInformationActivity.this, "You already want it!", Toast.LENGTH_SHORT).show();
                        } else {
                            Want trade = new Want();
                            trade.setTimeStamp();
                            trade.setExceptCost(price);
                            trade.setImage(image);
                            trade.setOwnerName(ownerName);
                            trade.setReqName(username);
                            trade.setStatue("WAIT_BOTH");

                            String tradeCode = getRandomString(5);
                            trade.setTradeCode(tradeCode);
                            trade.save();
                            Toast.makeText(ArtInformationActivity.this, "Request Sent", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });

        initDatas();
        //Get controls
        mRecyclerView = (RecyclerView) findViewById(R.id.id_recyclerview_horizontal_other);
        //Setting up the layout manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        //Setting up the adapter
        mAdapter = new ArtInformationActivity.GalleryAdapter(this, mShows);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void initDatas()
    {
        mShows = new ArrayList<>();
        //Find the image that needs to be shown in the user owns
        List<Owns> ownsList = LitePal.findAll(Owns.class);
        for (Owns o:ownsList) {
            if (o.getUsername().equals(oo)) {
                byte[] target = o.getImage();
                Bitmap oldPic = Bytes2Bimap(target);

                List<ArtData> artDatas = LitePal.findAll(ArtData.class);
                for (ArtData artData:artDatas) {
                    byte[] targetByte = artData.getImage();
                    Bitmap newPic = Bytes2Bimap(targetByte);
                    if (HashUtil.hashCompare(newPic, oldPic).equals("LIKE")) {
                        if (artData.getShow()) {
                            mShows.add(Bytes2Bimap(artData.getImage()));
                        }
                    }
                }

            }
        }
    }

    public class GalleryAdapter extends RecyclerView.Adapter<ArtInformationActivity.GalleryAdapter.ViewHolder> {

        private LayoutInflater mInflater;
        private List<Bitmap> mShows;

        public GalleryAdapter(Context context, List<Bitmap> datats)
        {
            mInflater = LayoutInflater.from(context);
            mShows = datats;
        }

        public class ViewHolder extends RecyclerView.ViewHolder
        {
            public ViewHolder(View arg0)
            {
                super(arg0);
            }

            ImageView mImg;
            TextView mTxt;
        }

        @NonNull
        @Override
        public ArtInformationActivity.GalleryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = mInflater.inflate(R.layout.item_show_image,
                    viewGroup, false);
            ArtInformationActivity.GalleryAdapter.ViewHolder viewHolder = new ArtInformationActivity.GalleryAdapter.ViewHolder(view);

            viewHolder.mImg = (ImageView) view
                    .findViewById(R.id.id_index_gallery_item_image);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull ArtInformationActivity.GalleryAdapter.ViewHolder holder, int position) {
            holder.mImg.setImageBitmap(mShows.get(position));
        }

        @Override
        public int getItemCount() {
            return mShows.size();
        }
    }

    public Bitmap Bytes2Bimap(byte[] b) {
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } else {
            return null;
        }
    }

    //lengthThe length of the string requested by the user
    public static String getRandomString(int length){
        String str="0123456789";
        Random random=new Random();
        StringBuffer sb=new StringBuffer();
        for(int i=0;i<length;i++){
            int number=random.nextInt(10);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

}