package com.demo.opencv;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.demo.opencv.models.ArtData;

import java.util.List;

public class ArtFrontPageAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private List<ArtData> mArtData;

    private ArtFrontPageAdapter.OnItemClickListener mListener;

    public ArtFrontPageAdapter(Context context, List<ArtData> data) {
        mContext = context;
        mArtData = data;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_art_front_page_image, null);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MyViewHolder holder2 = (MyViewHolder) holder;
        ArtData artCard = mArtData.get(position);
        Bitmap bp = Bytes2Bimap(artCard.getImage());
        holder2.artImage.setImageBitmap(bp);
        holder2.artImage.getLayoutParams().height = artCard.getImgHeight(); //Gets the height of the picture from the data source and dynamically sets it to the control
        holder2.artName.setText(artCard.getName());

        holder2.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onItemClick(holder.getAdapterPosition());
            }
        });
    }

    public void setOnItemClickListener(ArtFrontPageAdapter.OnItemClickListener listener) {
        this.mListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    @Override
    public int getItemCount() {
        if (mArtData != null) {
            return mArtData.size();
        }
        return 0;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView artImage;
        public TextView artName;

        public MyViewHolder(View itemView) {
            super(itemView);
            artImage = itemView.findViewById(R.id.art_image);
            artName = (TextView) itemView.findViewById(R.id.art_name);
        }
    }

    public Bitmap Bytes2Bimap(byte[] b) {
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } else {
            return null;
        }
    }
}
