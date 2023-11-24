package com.demo.opencv;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.demo.opencv.models.ArtData;
import com.demo.opencv.models.Owns;
import com.demo.opencv.topicview.adapter.HomeTopicPagerAdapter;
import com.demo.opencv.topicview.adapter.TopicAdapter;
import com.demo.opencv.topicview.bean.TopicBean;

//Reference from lucode hackware to beautify topic bar
import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.ViewPagerHelper;
import net.lucode.hackware.magicindicator.buildins.UIUtil;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.DummyPagerTitleView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class JavaImageProcessActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener, TopicAdapter.OnItemClickListener{

    private double max_size = 1024;
    private int PICK_IMAGE_REQUEST = 1;
    private final int REQUEST_GPS = 1;
    private Bitmap selectbp, selectbp2;
    CreateUserPopWin createUserPopWin;
    String name, price;

    private ImageView mImageView;
    private AppCompatSeekBar mRotateSeekBar;
    private AppCompatSeekBar mSaturationSeekBar;
    private AppCompatSeekBar mScaleSeekBar;

    private float mRotate;
    private float mSaturation;
    private float mScale;

    private static int MIN_PROGRESS = 128;
    private static int MAX_PROGRESS = 255;

//    The filter is displayed using Util
    private RecyclerView mRecyclerView;
    private BeautyAdapter mBeautyAdapter;
    private List<float[]> mColorMatrixList;

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,//Write permissions
    };

    private ArrayList<TopicBean> mTopicData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Screenshots are prohibited
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_java_image_process);
        initTopicData();
        initTypeViewPager(2, 5);

        mImageView = findViewById(R.id.imageView);
        mRotateSeekBar = findViewById(R.id.seekBar_rotate);
        mSaturationSeekBar = findViewById(R.id.seekBar_saturation);
        mScaleSeekBar = findViewById(R.id.seekBar_scale);

        mRotateSeekBar.setOnSeekBarChangeListener(this);
        mSaturationSeekBar.setOnSeekBarChangeListener(this);
        mScaleSeekBar.setOnSeekBarChangeListener(this);

        mRotateSeekBar.setMax(MAX_PROGRESS);
        mSaturationSeekBar.setMax(MAX_PROGRESS);
        mScaleSeekBar.setMax(MAX_PROGRESS);

        mRotateSeekBar.setProgress(MIN_PROGRESS);
        mSaturationSeekBar.setProgress(MIN_PROGRESS);
        mScaleSeekBar.setProgress(MIN_PROGRESS);

        Button selectImageBtn = (Button)findViewById(R.id.btn_select);
        selectImageBtn.setOnClickListener(new View.OnClickListener() {//Defining the listener for selecting images
            @Override
            public void onClick(View v) {
                // makeText(MainActivity.this.getApplicationContext(), "start to browser image", Toast.LENGTH_SHORT).show();
                selectImage();//Call the function that selects the picture
            }

            //Select a picture from your phone's album
            private void selectImage() {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);//Allows users to select a special kind of data and return it (special kind of data: take a photo or record a sound)
                startActivityForResult(Intent.createChooser(intent,"choose picture..."), PICK_IMAGE_REQUEST);//Start another activity
            }
        });

        Button saveImageBtn = findViewById(R.id.btn_java_save);
        saveImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditPopWin(view);
            }
        });

//        The filter is displayed using Util
        mColorMatrixList = new ArrayList<float[]>();
        mColorMatrixList.add(BeautyUtil.colormatrix_huguang);
        mColorMatrixList.add(BeautyUtil.colormatrix_hepian);
        mColorMatrixList.add(BeautyUtil.colormatrix_landiao);
        mColorMatrixList.add(BeautyUtil.colormatrix_qingning);
        mColorMatrixList.add(BeautyUtil.colormatrix_yese);
        mColorMatrixList.add(BeautyUtil.colormatrix_fugu);
        mColorMatrixList.add(BeautyUtil.colormatrix_huan_huang);
        mColorMatrixList.add(BeautyUtil.colormatrix_jiuhong);
        mColorMatrixList.add(BeautyUtil.colormatrix_chuan_tong);
        mColorMatrixList.add(BeautyUtil.colormatrix_ruise);
        mColorMatrixList.add(BeautyUtil.colormatrix_gete);
        mColorMatrixList.add(BeautyUtil.colormatrix_menghuan);
        mColorMatrixList.add(BeautyUtil.colormatrix_langman);
        mColorMatrixList.add(BeautyUtil.colormatrix_danya);
        mColorMatrixList.add(BeautyUtil.colormatrix_jiao_pian);
        mColorMatrixList.add(BeautyUtil.colormatrix_guangyun);
        mColorMatrixList.add(BeautyUtil.colormatrix_heibai);
        mColorMatrixList.add(BeautyUtil.colormatrix_huaijiu);
        mColorMatrixList.add(BeautyUtil.colormatrix_fanse);

        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mRecyclerView.addItemDecoration(new ItemDecoration());
        mBeautyAdapter = new BeautyAdapter();
        mRecyclerView.setAdapter(mBeautyAdapter);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        switch (seekBar.getId()) {
            case R.id.seekBar_rotate:
                mRotate = (mRotateSeekBar.getProgress() - 128f) * 1.0f / 128f * 180;
                break;
            case R.id.seekBar_saturation:
                mSaturation = mSaturationSeekBar.getProgress() / 128f;
                break;
            case R.id.seekBar_scale:
                mScale = mScaleSeekBar.getProgress() / 128f;
                break;
        }

        if (selectbp != null) {
            Bitmap bitmap = BeautyUtil.beautyImage(selectbp, mRotate, mSaturation, mScale);
            mImageView.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    //        Filters use Util to display auxiliary classes
    class BeautyAdapter extends RecyclerView.Adapter<BeautyAdapter.BeautyViewHolder> {

        @NonNull
        @Override
        public BeautyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(JavaImageProcessActivity.this).inflate(R.layout.item_recyclerview_image, null, false);
            ImageView iv = view.findViewById(R.id.imageView);
            iv.setImageBitmap(selectbp);
            BeautyViewHolder beautyViewHolder = new BeautyViewHolder(view);
            return beautyViewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull BeautyViewHolder holder, int position) {
            ImageView imageView = holder.imageView;
            if (imageView != null) {
                imageView.setImageBitmap(selectbp);
                ColorMatrix colorMatrix = new ColorMatrix();
                colorMatrix.set(mColorMatrixList.get(position));
                imageView.setColorFilter(new ColorMatrixColorFilter(colorMatrix));

                holder.imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mImageView.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
                        Mat src = new Mat();
                        Utils.bitmapToMat(selectbp, src);
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return mColorMatrixList == null ? 0 : mColorMatrixList.size();
        }

        class BeautyViewHolder extends RecyclerView.ViewHolder {

            private ImageView imageView;

            public BeautyViewHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.imageView);
            }
        }
    }

    //        Filters use Util to display auxiliary classes
    class ItemDecoration extends RecyclerView.ItemDecoration {
        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            super.onDraw(c, parent, state);
        }

        @Override
        public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
            super.onDrawOver(c, parent, state);
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.set(3, 3, 3, 3);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //requestCode is originally provided to startActivityForResult() as an integer request code that allows you to identify the source of this result.
        //The integer requestCode is used to compare the value of the requestCode in startActivityForResult in order to confirm which activity the returned data was returned.
        //The integer result code returned by the resultCode subactivity through its setResult(). Applies to the value returned by multiple activities when data is returned.
        //data: An intent object with the returned data. You can do this via data.getXxxExtra( ); method to get data of the specified data type,
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {//当为选择image这个意图时，进入下面代码。选择图片以位图的形式显示出来
            Uri uri = data.getData();
            try {
                Log.d("image-tag", "start to decode selected image now...");
                InputStream input = getContentResolver().openInputStream(uri);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(input, null, options);
                int raw_width = options.outWidth;
                int raw_height = options.outHeight;
                int max = Math.max(raw_width, raw_height);
                int newWidth = raw_width;
                int newHeight = raw_height;
                int inSampleSize = 1;
                if(max > max_size) {
                    newWidth = raw_width / 2;
                    newHeight = raw_height / 2;
                    while((newWidth/inSampleSize) > max_size || (newHeight/inSampleSize) > max_size) {
                        inSampleSize *=2;
                    }
                }

                options.inSampleSize = inSampleSize;
                options.inJustDecodeBounds = false;
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                selectbp = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, options);

                mImageView.setImageBitmap(selectbp);//Displays the selected bitmap

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void startRequestPermission(){
        //321 is the request code
        ActivityCompat.requestPermissions(this,PERMISSIONS_STORAGE,321);
    }
    //The callback method for dynamic applications
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(MainActivity.this, "Permission GET", Toast.LENGTH_SHORT).show();
            //How to save pictures, customize
            saveBitmap();
        } else {
            //Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //Save the picture
    public void saveBitmap() {
        //Log.e("开始保存", "保存图片");
        Toast.makeText(JavaImageProcessActivity.this, "Start saving", Toast.LENGTH_SHORT).show();
        //Get the root path of the SD card
//        File sd = Environment.getExternalStorageDirectory();
        //Get if the SD card is accessible
//        boolean can_write = sd.canWrite();
        //Log.e("是否被访问", can_write + "");
        //Create a file with the suffix .jpg under the SD card path
//        File f = new File(Environment.getExternalStorageDirectory() + "/" + name + ".jpg");
        //If the file exists, delete the original file.
//        if (f.exists()) {
//            f.delete();
//        }
        try {
            //File output stream
//            FileOutputStream out = new FileOutputStream(f);
            //bm is a variable of type private Bitmap. private Bitmap bm;
            //Call BitMap's compress method
            /*
             *Bitmap.CompressFormat format the compression format of the image;
             *int quality Image compression ratio, 0-100. 0 compresses 100%, 100 means no compression;
             *OutputStream stream Write the output stream of compressed data;
             * */
//            if (selectbp == null) {
//                selectbp.compress(Bitmap.CompressFormat.PNG, 90, out);
//            } else {
//                selectbp.compress(Bitmap.CompressFormat.PNG, 90, out);
//            }
            //flushed
//            out.flush();
            //Close the output stream
//            out.close();
            //Log.e("保存", "已经保存");
            //After saving the picture, a broadcast notification is sent to update the database
//            Uri uri = Uri.fromFile(f);
//            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));

            //Save it into the Litepal database
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            selectbp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageBytes = baos.toByteArray();

            ArtData artData = new ArtData();
            artData.setName(name);
            artData.setPrice(price);
            artData.setImage(imageBytes);
            artData.save();
            //Inquire
//            Product product = DataSupport.find(Product.class, id);
//            String name = product.getName();
//            double price = product.getPrice();
//            byte[] imageBytes = product.getImage();
            //Multi-cascade check
//            Product product = DataSupport.select("name", "price").where("id = ?", id).find(Product.class);
            SharedPreferences preferences = getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
            String username = preferences.getString("username", "");
            Owns ownShip = new Owns();
            ownShip.setTitle(name);
            ownShip.setPrice(price);
            ownShip.setImage(imageBytes);
            ownShip.setUsername(username);
            ownShip.setTimeStamp();
            ownShip.save();
            Toast.makeText(JavaImageProcessActivity.this, "Save successful", Toast.LENGTH_SHORT).show();
//        } catch (FileNotFoundException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showEditPopWin(View view) {
        createUserPopWin = new CreateUserPopWin(this, onClickListener);
        createUserPopWin.showAtLocation(findViewById(R.id.main_java_view), Gravity.CENTER, 0, 0);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_save_pop:
                    name = createUserPopWin.text_name.getText().toString().trim();
                    price = createUserPopWin.text_price.getText().toString();
                    createUserPopWin.dismiss();
                    ActivityCompat.requestPermissions(JavaImageProcessActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS}, REQUEST_GPS);
                    break;
            }
        }
    };

    private void initTopicData() {
        mTopicData.clear();
        mTopicData.add(new TopicBean(R.mipmap.icon_home_front, "Front Page"));
        mTopicData.add(new TopicBean(R.mipmap.icon_home_camera, "Camera"));
        mTopicData.add(new TopicBean(R.mipmap.icon_home_cv, "CV Process"));
        mTopicData.add(new TopicBean(R.mipmap.icon_home_basic, "Basic Process"));
        mTopicData.add(new TopicBean(R.mipmap.icon_home_info, "Info"));
    }

    private void initTypeViewPager(int rowNum, int columnNum) {
        final ViewPager topicViewPager = findViewById(R.id.topicViewPager);
        final MagicIndicator topicIndicator = findViewById(R.id.topicIndicator);
        //1.Pagination according to the amount of data, and the data per page is rw
        int singlePageDatasNum = rowNum * columnNum; //The amount of data contained in each single page: 2*4=8;
        int pageNum = mTopicData.size() / singlePageDatasNum;//Figure out how many pages of menu there are: 20% 8 = 3;
        if (mTopicData.size() % singlePageDatasNum > 0) pageNum++;//If the modulus is greater than 0, one more page will come out and the remaining dissatisfaction items will be placed
        ArrayList<RecyclerView> mList = new ArrayList<>();
        for (int i = 0; i < pageNum; i++) {
            RecyclerView recyclerView = new RecyclerView(getApplicationContext());
            GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), columnNum);
            recyclerView.setLayoutManager(gridLayoutManager);
            int fromIndex = i * singlePageDatasNum;
            int toIndex = (i + 1) * singlePageDatasNum;
            if (toIndex > mTopicData.size()) toIndex = mTopicData.size();
            //a.Screenshot each page contains data
            ArrayList<TopicBean> menuItems = new ArrayList<TopicBean>(mTopicData.subList(fromIndex, toIndex));
            //b.Set adapter data for each page
            TopicAdapter menuAdapter = new TopicAdapter(getApplicationContext(), menuItems);
            menuAdapter.setOnItemClickListener(this);
            //c.Bind the adapter and add it to the list
            recyclerView.setAdapter(menuAdapter);
            mList.add(recyclerView);
        }
        //2.ViewPager's adapter
        HomeTopicPagerAdapter menuViewPagerAdapter = new HomeTopicPagerAdapter(mList);
        topicViewPager.setAdapter(menuViewPagerAdapter);
        //3.Dynamically sets the height of the ViewPager and loads all pages
        int height = dp2px(getApplicationContext(), 76.0f);//Here 80 is the height of the layout file in the MainMenuAdapter
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, mTopicData.size() <= columnNum ? height : height * rowNum);
        topicViewPager.setLayoutParams(layoutParams);
        topicViewPager.setOffscreenPageLimit(pageNum - 1);

        //4.Create an indicator
        CommonNavigator commonNavigator = new CommonNavigator(getApplicationContext());
        commonNavigator.setAdjustMode(true);
        final int finalPageNum = pageNum;

        commonNavigator.setAdapter(new CommonNavigatorAdapter() {
            @Override
            public int getCount() {
                return finalPageNum;
            }

            @Override
            public IPagerTitleView getTitleView(Context context, int index) {
                return new DummyPagerTitleView(context);
            }

            @Override
            public IPagerIndicator getIndicator(Context context) {
                LinePagerIndicator indicator = new LinePagerIndicator(context);
                indicator.setMode(LinePagerIndicator.MODE_EXACTLY);
                indicator.setLineHeight(UIUtil.dip2px(context, 3));//is the indicator's high
                indicator.setLineWidth(UIUtil.dip2px(context, 66 / finalPageNum));//It's the width of the indicator, which is then scored by the number of pages
                indicator.setRoundRadius(UIUtil.dip2px(context, 3));
                indicator.setStartInterpolator(new AccelerateInterpolator());
                indicator.setEndInterpolator(new DecelerateInterpolator(3));
                indicator.setColors(ContextCompat.getColor(context, R.color.colorAccent));
                return indicator;
            }
        });
        //5.Configure the indicator and bind it to ViewPager
        topicIndicator.setNavigator(commonNavigator);
        ViewPagerHelper.bind(topicIndicator, topicViewPager);
    }

    @Override
    public void onTopicItemClick(TopicBean position) {
        if (position.getTitle().equals("Front Page")) {
            startActivity(new Intent(JavaImageProcessActivity.this, MainActivity.class));
        } else if (position.getTitle().equals("Camera")) {
            startActivity(new Intent(JavaImageProcessActivity.this, CameraOpenCVActivity.class));
        } else if (position.getTitle().equals("CV Process")) {
            startActivity(new Intent(JavaImageProcessActivity.this, OpenCvImageProcessActivity.class));
        } else if (position.getTitle().equals("Basic Process")) {
            Toast.makeText(JavaImageProcessActivity.this,"Already on the current page！",Toast.LENGTH_SHORT).show();
        } else if (position.getTitle().equals("Info")) {
            startActivity(new Intent(JavaImageProcessActivity.this, UserSelfInfo.class));
        }
    }

    public static int dp2px(Context context, float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, context.getResources().getDisplayMetrics());
    }
}