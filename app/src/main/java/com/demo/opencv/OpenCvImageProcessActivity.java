package com.demo.opencv;

import static org.opencv.imgproc.Imgproc.MORPH_RECT;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.demo.opencv.models.ArtData;
import com.demo.opencv.models.Owns;
import com.demo.opencv.topicview.adapter.HomeTopicPagerAdapter;
import com.demo.opencv.topicview.adapter.TopicAdapter;
import com.demo.opencv.topicview.bean.TopicBean;

//Reference from lucode hackware to beautify topic bar
//http://hackware.lucode.net
import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.ViewPagerHelper;
import net.lucode.hackware.magicindicator.buildins.UIUtil;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.DummyPagerTitleView;

import org.litepal.LitePal;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class OpenCvImageProcessActivity extends AppCompatActivity implements TopicAdapter.OnItemClickListener{

    private double max_size = 1024;
    private int PICK_IMAGE_REQUEST = 1;
    private final int REQUEST_GPS = 1;
    private Bitmap selectbp;
    private Bitmap selectbp2;
    CreateUserPopWin createUserPopWin;
    String name, price;

    CascadeClassifier classifier;

    private ImageView iv;

    private Button mBtnSave;

    private ArrayList<TopicBean> mTopicData = new ArrayList<>();
    private ArrayList<TopicBean> mTopicData1 = new ArrayList<>();


    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,//Write permissions
            Manifest.permission.CAMERA//Photo rights
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        OpenCVLoader.initDebug();
        super.onCreate(savedInstanceState);
        //Screenshots are prohibited
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_open_cv_image_process);

        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
            //Check the permissions
            int i = ContextCompat.checkSelfPermission(this,PERMISSIONS_STORAGE[0]);
            //If the permission request fails, reapply the permission
            if(i!= PackageManager.PERMISSION_GRANTED){
                //Reapply permission function
                startRequestPermission();
            }
        }

        initTopicData();
        initTypeViewPager(2, 5);
        initTopicData1();
        initTypeViewPager1(1, 5);

        iv = findViewById(R.id.opencv_iv);
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);

        Button selectImageBtn = (Button)findViewById(R.id.btn_select);//Define a button to select a picture
        selectImageBtn.setOnClickListener(new View.OnClickListener() {//Defines the listener that selects the picture
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
                startActivityForResult(Intent.createChooser(intent,"Select the image..."), PICK_IMAGE_REQUEST);//Start another activity
            }
        });
        mBtnSave = findViewById(R.id.btn_save);

        mBtnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap newPic = selectbp2;
                List<ArtData> artDatas = LitePal.findAll(ArtData.class);
                String check = "UNLIKE";
                for (ArtData artData:artDatas) {
                    byte[] targetByte = artData.getImage();
                    Bitmap oldPic = Bytes2Bimap(targetByte);
                    if (HashUtil.hashCompare(newPic, oldPic).equals("LIKE")) {
                        check = "LIKE";
                    }
                }
                if (check.equals("UNLIKE")) {
                    showEditPopWin(view);
                } else {
                    Toast.makeText(OpenCvImageProcessActivity.this, "A similar image asset already exists", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public Bitmap face() {
        initClassifier();
        Mat mat = new Mat();
        Mat matdst = new Mat();
        Utils.bitmapToMat(selectbp, mat);
        //Give a copy of the current data to matdst
        mat.copyTo(matdst);

        //1.Turn the image to grayscale BGR2GRAY, note that it is BGR
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY);

        //2.Defines MatOfRect for receiving face positions
        MatOfRect faces = new MatOfRect();

        //3.Start face detection and store the detected face data in faces
        classifier.detectMultiScale(mat, faces, 1.05, 3, 0, new Size(30, 30), new Size());
        List<Rect> faceList = faces.toList();

        //4.Determine if there is a human face
        if (faceList.size() > 0) {
            for (Rect rect : faceList) {
                //5.Draw a rectangular box based on the position of the resulting face
                //rect.tl() Upper left corner
                //rect.br() Bottom right corner
                Imgproc.rectangle(matdst, rect.tl(), rect.br(), new Scalar(255, 0, 0,255), 4);
            }
        }

        Bitmap resultBitmap = Bitmap.createBitmap(matdst.width(), matdst.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(matdst, resultBitmap);
        mat.release();
        matdst.release();
        return resultBitmap;
    }


    private void BinarizationProcess() {
        //Binarization
        Mat src = new Mat();
        Mat temp = new Mat();
        Mat dst = new Mat();
        Utils.bitmapToMat(selectbp, src);//Convert bitmaps to Mat data. For bitmaps, they consist of A, R, G, and B channels
        Imgproc.cvtColor(src, temp, Imgproc.COLOR_BGRA2BGR);//Conversion to BGR (data storage mode in opencv)
        Imgproc.cvtColor(temp, temp, Imgproc.COLOR_BGR2GRAY);//Grayscale.

        Imgproc.threshold(temp,dst,50,255,Imgproc.THRESH_BINARY);
        selectbp2 = Bitmap.createBitmap(src.width(), src.height(), Bitmap.Config.ARGB_8888) ;
        Utils.matToBitmap(dst, selectbp2);//Then convert the mat to a bitmap
        iv.setImageBitmap(selectbp2);//Displays a bitmap
        src.release();
        dst.release();
    }

    private void Togray(){
        //Grayscale processing
        Mat src = new Mat();
        Mat temp = new Mat();
        Mat dst = new Mat();

        Utils.bitmapToMat(selectbp,src);
        Imgproc.cvtColor(src,dst,Imgproc.COLOR_BGR2GRAY);
        selectbp2 = Bitmap.createBitmap(src.width(), src.height(), Bitmap.Config.ARGB_8888) ;
        Utils.matToBitmap(dst,selectbp2);

        iv.setImageBitmap(selectbp2);
        src.release();
        dst.release();
    }

    private void Tocorrosion(){
        //corrode
        Mat src = new Mat();
        Mat temp = new Mat();
        Mat dst = new Mat();
        Mat element = Imgproc.getStructuringElement(MORPH_RECT, new Size(10,10));;

        Utils.bitmapToMat(selectbp,src);
        //    src：Source image
        //    dst：Output image
        //    element：This is the kernel that we will use to perform the operation. If we don't specify, the default is a simple 3x3 matrix. Otherwise, we can specify its shape. To do this, we need to use the function cv :: getStructuringElement:

        Imgproc.erode(src, dst, element);
        selectbp2 = Bitmap.createBitmap(src.width(), src.height(), Bitmap.Config.ARGB_8888) ;
        Utils.matToBitmap(dst,selectbp2);

        iv.setImageBitmap(selectbp2);
        src.release();
        dst.release();
    }

    private void Todilate(){
        //Dilate

        //    src：Source image
        //    dst：Output image
        //    element：This is the kernel that we will use to perform the operation. If we don't specify, the default is a simple 3x3 matrix. Otherwise, we can specify its shape.
        //    To do this, we need to use the function cv :: getStructuringElement:
        Mat src = new Mat();
        Mat temp = new Mat();
        Mat dst = new Mat();
        Mat element = Imgproc.getStructuringElement(MORPH_RECT, new Size(10,10));;

        Utils.bitmapToMat(selectbp,src);
        Imgproc.dilate(src, dst, element);
        selectbp2 = Bitmap.createBitmap(src.width(), src.height(), Bitmap.Config.ARGB_8888) ;
        Utils.matToBitmap(dst,selectbp2);

        iv.setImageBitmap(selectbp2);
        src.release();
        dst.release();
    }

    private void TomedianBlur(){
        //Median filtering
        Mat src = new Mat();
        Mat temp = new Mat();
        Mat dst = new Mat();

        Utils.bitmapToMat(selectbp, src);
        // InputArray src: Input image, image is 1, 3, 4 channel image, when the template size is 3 or 5, the image depth can only be one of CV_8U, CV_16U, CV_32F,
        // such as for pictures with larger aperture size, the image depth can only be CV_8U.
        // OutputArray dst: The output image, size and type are consistent with the input image, and you can use Mat::Clone to initialize the output image dst with the original image as a template
        // int ksize: The size of the filter template must be an odd number greater than 1, such as 3, 5, 7...
        Imgproc.medianBlur(src, dst, 77);
        selectbp2 = Bitmap.createBitmap(src.width(), src.height(), Bitmap.Config.ARGB_8888) ;
        Utils.matToBitmap(dst,selectbp2);

        iv.setImageBitmap(selectbp2);
        src.release();
        dst.release();
    }

    private void ToGaussian(){
        //Gaussian blur
        Mat src = new Mat();
        Mat temp = new Mat();
        Mat dst = new Mat();

        Utils.bitmapToMat(selectbp, src);
        // src，Enter the image, that is, the source image, and fill in the object of the Mat class.
        // It can be a separate image with any number of channels, but it should be noted that the image depth should be one of CV_8U, CV_16U, CV_16S, CV_32F, and CV_64F.
        // dst，That is, the target image needs to have the same size and type as the source image.
        // For example, you can use Mat::Clone to initialize the target map such as a fake package using the source image as a template.
        // ksize，The size of the Gaussian kernel. Ksize.width and ksize.height can be different, but they must both be positive and odd (not understandable).
        // Alternatively, they can be zero, and they are all calculated from sigma.
        // sigmaX，Represents the standard deviation of the Gaussian kernel function in the X direction.
        // sigmaY，Represents the standard deviation of the Gaussian kernel function in the Y direction. If sigmaY is zero, it is set to sigmaX,
        // and if both sigmaX and sigmaY are 0, then ksize.width and ksize.height are calculated.
        Imgproc.GaussianBlur(src,dst,new Size(77,77),5, 5);
        selectbp2 = Bitmap.createBitmap(src.width(), src.height(), Bitmap.Config.ARGB_8888) ;
        Utils.matToBitmap(dst,selectbp2);

        iv.setImageBitmap(selectbp2);
        src.release();
        dst.release();
    }

    private void CannyScan(){
        //Edge detection
        Mat src = new Mat();
        Utils.bitmapToMat(selectbp, src);

        Mat gray = new Mat();
        Imgproc.cvtColor(src,gray,Imgproc.COLOR_BGR2GRAY);//灰度处理
        Mat ret = src.clone();
        // image  The input image must be a single-channel or three-channel image of the CV_8U.
        // edges  Output image, a single-channel image with the same dimensions as the input image and a CV_8U data type.
        // threshold1  The first lag threshold.
        // threshold2  The second lag threshold.
        Imgproc.Canny(src, ret, 75, 200);
        selectbp2 = Bitmap.createBitmap(src.width(), src.height(), Bitmap.Config.ARGB_8888) ;
        Utils.matToBitmap(ret,selectbp2);

        iv.setImageBitmap(selectbp2);
        src.release();
        gray.release();
        ret.release();
    }

    private void getRoI() {
        Mat src = new Mat();
        Mat temp = new Mat();
        Mat dst = new Mat();
        Utils.bitmapToMat(selectbp, src);//Convert bitmaps to Mat data. For bitmaps, they consist of A, R, G, and B channels
        Imgproc.cvtColor(src, src, Imgproc.COLOR_BGRA2BGR);//Conversion to BGR (data storage mode in opencv)

        Rect rect = new Rect(30, 120, 340, 350); // Set the position of the rectangular ROI
        Mat imgRectROI= new Mat(src, rect);      // Take a screenshot from the original image

        selectbp2 = Bitmap.createBitmap(imgRectROI.width(), imgRectROI.height(), Bitmap.Config.ARGB_8888) ;
        Utils.matToBitmap(imgRectROI, selectbp2);//Then convert the mat to a bitmap
        iv.setImageBitmap(selectbp2);//Displays a bitmap
    }


    //Installation-free Opencv manager
    //The onResume() method is called when the activity is ready to interact with the user.
    // The activity at this time must be at the top of the return stack and running.
    //So call before the activity starts, check if there is an OpenCV library, if not, download it
    @Override
    protected void onResume() {
        super.onResume();
        //Installation-free OpenCV Manager
        if (!OpenCVLoader.initDebug()) {
            System.out.println("Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
        } else {
            System.out.println("OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    // The callback function after the OpenCV library is loaded and initialized successfully
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    System.out.println("OpenCV loaded successfully");
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };


    //In one main interface (main activity) through the intention (startActivityForResult) jump to multiple different sub-activities,
    // When the code of the submodule is executed, return to the main page again, and display the data obtained in the sub-activity on the main interface/the completed data to the main activity for processing.
    // This intent jump with data requires the use of the activity's onActivityResult() method
    //note:After clicking the Select Image button, you should enter the options here
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //requestCode is originally provided to startActivityForResult() as an integer request code that allows you to identify the source of this result.
        //The integer requestCode is used to compare the value of the requestCode in startActivityForResult in order to confirm which activity the returned data was returned.
        //The integer result code returned by the resultCode subactivity through its setResult(). Applies to the value returned by multiple activities when data is returned.
        //data。 An intent object with the returned data. You can do this via data.getXxxExtra( ); method to get data of the specified data type,
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

                iv.setImageBitmap(selectbp);//Displays the selected bitmap

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void startRequestPermission(){
        //321 is the request code
        ActivityCompat.requestPermissions(this,PERMISSIONS_STORAGE,321);
    }

    public void initClassifier() {
        try {
            //Read files stored in RAW
            InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface_improved);
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            File cascadeFile = new File(cascadeDir,"lbpcascade_frontalface_improved.xml");
            FileOutputStream os = new FileOutputStream(cascadeFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while((bytesRead = is.read(buffer))!=-1){
                os.write(buffer,0,bytesRead);
            }
            is.close();
            os.close();
            //Face detection is operated through classifiers, and a CascadeClassifier classifier is defined externally for global variable use
            classifier = new CascadeClassifier(cascadeFile.getAbsolutePath());
            cascadeFile.delete();
            cascadeDir.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        Toast.makeText(OpenCvImageProcessActivity.this, "Start saving", Toast.LENGTH_SHORT).show();
        //Get the root path of the SD card
//        File sd = Environment.getExternalStorageDirectory();
        //Get if the SD card is accessible
//        boolean can_write = sd.canWrite();
//        Log.d("是否被访问", can_write + "");
        //Create a file with the suffix .jpg under the SD card path
//        File f = new File(Environment.getExternalStorageDirectory() + "/" + name + ".jpg");
//        //If the file exists, delete the original file.
//        if (f.exists()) {
//            f.delete();
//        }
        try {
            //File output stream
//            FileOutputStream out = new FileOutputStream(f);
            //bm is a variable of type private Bitmap. private Bitmap bm;
            //Call BitMap's compress method
            /*
             *Bitmap.CompressFormat format The compressed format of the image；
             *int quality Image compression rate，0-100。 0 Compress 100%, 100 means no compression；
             *OutputStream stream Writes the output stream of compressed data；
             * */
//            if (selectbp2 == null) {
//                selectbp2.compress(Bitmap.CompressFormat.PNG, 90, out);
//            } else {
//                selectbp2.compress(Bitmap.CompressFormat.PNG, 90, out);
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
            selectbp2.compress(Bitmap.CompressFormat.JPEG, 100, baos);
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
            Toast.makeText(OpenCvImageProcessActivity.this, "Save successful", Toast.LENGTH_SHORT).show();

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
        createUserPopWin.showAtLocation(findViewById(R.id.main_view), Gravity.CENTER, 0, 0);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_save_pop:
                    name = createUserPopWin.text_name.getText().toString().trim();
                    price = createUserPopWin.text_price.getText().toString();
                    createUserPopWin.dismiss();
                    ActivityCompat.requestPermissions(OpenCvImageProcessActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS}, REQUEST_GPS);
                    break;
            }
        }
    };
    public Bitmap Bytes2Bimap(byte[] b) {
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } else {
            return null;
        }
    }

    private void initTopicData() {
        mTopicData.clear();
        mTopicData.add(new TopicBean(R.mipmap.icon_home_front, "Front Page"));
        mTopicData.add(new TopicBean(R.mipmap.icon_home_camera, "Camera"));
        mTopicData.add(new TopicBean(R.mipmap.icon_home_cv, "CV Process"));
        mTopicData.add(new TopicBean(R.mipmap.icon_home_basic, "Basic Process"));
        mTopicData.add(new TopicBean(R.mipmap.icon_home_info, "Info"));
    }

    private void initTopicData1() {
        mTopicData1.clear();
        mTopicData1.add(new TopicBean(R.mipmap.icon_cv_gray, "Gray"));
        mTopicData1.add(new TopicBean(R.mipmap.icon_cv_corrosion, "Corrosion"));
        mTopicData1.add(new TopicBean(R.mipmap.icon_cv_dilate, "Dilate"));
        mTopicData1.add(new TopicBean(R.mipmap.icon_cv_blur, "MedianBlur"));
        mTopicData1.add(new TopicBean(R.mipmap.icon_cv_gaussian, "Gaussian"));
        mTopicData1.add(new TopicBean(R.mipmap.icon_cv_scan, "CannyScan"));
        mTopicData1.add(new TopicBean(R.mipmap.icon_cv_binary, "Binary"));
        mTopicData1.add(new TopicBean(R.mipmap.icon_cv_roi, "ROI"));
        mTopicData1.add(new TopicBean(R.mipmap.icon_cv_face, "Face"));
        mTopicData1.add(new TopicBean(R.mipmap.icon_cv_detection, "Detection"));
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
        int height = dp2px(getApplicationContext(), 76.0f);//这里的80为MainMenuAdapter中布局文件高度
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

    private void initTypeViewPager1(int rowNum, int columnNum) {
        final ViewPager topicViewPager = findViewById(R.id.topicViewPager1);
        final MagicIndicator topicIndicator = findViewById(R.id.topicIndicator1);
        //1.Pagination according to the amount of data, and the data per page is rw
        int singlePageDatasNum = rowNum * columnNum; //The amount of data contained in each single page: 2*4=8;
        int pageNum = mTopicData1.size() / singlePageDatasNum;//Figure out how many pages of menu there are: 20% 8 = 3;
        if (mTopicData1.size() % singlePageDatasNum > 0) pageNum++;//If the modulus is greater than 0, one more page will come out and the remaining dissatisfaction items will be placed
        ArrayList<RecyclerView> mList = new ArrayList<>();
        for (int i = 0; i < pageNum; i++) {
            RecyclerView recyclerView = new RecyclerView(getApplicationContext());
            GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), columnNum);
            recyclerView.setLayoutManager(gridLayoutManager);
            int fromIndex = i * singlePageDatasNum;
            int toIndex = (i + 1) * singlePageDatasNum;
            if (toIndex > mTopicData1.size()) toIndex = mTopicData1.size();
            //a.Screenshot each page contains data
            ArrayList<TopicBean> menuItems = new ArrayList<TopicBean>(mTopicData1.subList(fromIndex, toIndex));
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
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, mTopicData1.size() <= columnNum ? height : height * rowNum);
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
                indicator.setLineHeight(UIUtil.dip2px(context, 3));//就是指示器的高
                indicator.setLineWidth(UIUtil.dip2px(context, 66 / finalPageNum));//就是指示器的宽度，然后通过页数来评分
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
            startActivity(new Intent(OpenCvImageProcessActivity.this, MainActivity.class));
        } else if (position.getTitle().equals("Camera")) {
            startActivity(new Intent(OpenCvImageProcessActivity.this, CameraOpenCVActivity.class));
        } else if (position.getTitle().equals("CV Process")) {
            Toast.makeText(OpenCvImageProcessActivity.this,"Already on the current page！",Toast.LENGTH_SHORT).show();
        } else if (position.getTitle().equals("Basic Process")) {
            startActivity(new Intent(OpenCvImageProcessActivity.this, JavaImageProcessActivity.class));
        } else if (position.getTitle().equals("Info")) {
            startActivity(new Intent(OpenCvImageProcessActivity.this, UserSelfInfo.class));
        } else if (position.getTitle().equals("Detection")){
            startActivity(new Intent(OpenCvImageProcessActivity.this, ObjectDetectionActivity.class));
        } else if (position.getTitle().equals("Gray")) {
            Togray();
        } else if (position.getTitle().equals("Corrosion")) {
            Tocorrosion();
        } else if (position.getTitle().equals("Dilate")) {
            Todilate();
        } else if (position.getTitle().equals("MedianBlur")) {
            TomedianBlur();
        } else if (position.getTitle().equals("Gaussian")) {
            ToGaussian();
        } else if (position.getTitle().equals("CannyScan")) {
            CannyScan();
        } else if (position.getTitle().equals("Binary")) {
            BinarizationProcess();
        } else if (position.getTitle().equals("ROI")) {
            getRoI();
        } else if (position.getTitle().equals("Face")) {
            iv.setImageBitmap(face());
        }
    }

    public static int dp2px(Context context, float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, context.getResources().getDisplayMetrics());
    }
}