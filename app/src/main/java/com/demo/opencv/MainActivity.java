package com.demo.opencv;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.demo.opencv.models.ArtData;
import com.demo.opencv.models.LoginUser;
import com.demo.opencv.models.OwnHistory;
import com.demo.opencv.models.Owns;
import com.demo.opencv.models.UserData;
import com.demo.opencv.models.Want;
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

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements TopicAdapter.OnItemClickListener{
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArtFrontPageAdapter mAdapter;
    private ArrayList<TopicBean> mTopicData = new ArrayList<>();
    private Button mBtnClean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Screenshots are prohibited
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_main);
        init();
        initTopicData();
        initTypeViewPager(2, 5);
        clickListener();

        mBtnClean = findViewById(R.id.btn_clean);
        mBtnClean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LitePal.deleteAll(ArtData.class);
                LitePal.deleteAll(LoginUser.class);
                LitePal.deleteAll(Owns.class);
                LitePal.deleteAll(UserData.class);
                LitePal.deleteAll(Want.class);
                LitePal.deleteAll(OwnHistory.class);
            }
        });
    }

    private void init() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView_frontPage);
        //Set the layout manager to 2 columns, portrait
        mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mAdapter = new ArtFrontPageAdapter(this, buildData());

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }

    private List<ArtData> buildData() {
        List<ArtData> arts = LitePal.findAll(ArtData.class);
        return arts;
    }

    private void clickListener() {
        mAdapter.setOnItemClickListener(new ArtFrontPageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                List<ArtData> arts = LitePal.findAll(ArtData.class);
                String name = arts.get(position).getName();
                bundle.putInt("pos", position);
                intent.putExtras(bundle);
                intent.setClass(MainActivity.this, ArtInformationActivity.class);
                startActivity(intent);
            }
        });
    }
    @Override
    public void onTopicItemClick(TopicBean position) {
        if (position.getTitle().equals("Front Page")) {
            Toast.makeText(MainActivity.this,"Already on the current page！",Toast.LENGTH_SHORT).show();
        } else if (position.getTitle().equals("Camera")) {
            startActivity(new Intent(MainActivity.this, CameraOpenCVActivity.class));
        } else if (position.getTitle().equals("CV Process")) {
            startActivity(new Intent(MainActivity.this, OpenCvImageProcessActivity.class));
        } else if (position.getTitle().equals("Basic Process")) {
            startActivity(new Intent(MainActivity.this, JavaImageProcessActivity.class));
        } else if (position.getTitle().equals("Info")) {
            startActivity(new Intent(MainActivity.this, UserSelfInfo.class));
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



    public static int dp2px(Context context, float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, context.getResources().getDisplayMetrics());
    }

}