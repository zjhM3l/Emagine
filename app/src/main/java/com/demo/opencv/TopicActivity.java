package com.demo.opencv;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

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

import java.util.ArrayList;

public class TopicActivity extends AppCompatActivity implements TopicAdapter.OnItemClickListener {

    private ArrayList<TopicBean> mTopicData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);
        initTopicData();
        initTypeViewPager(2, 5);
    }

    private void initTopicData() {
        mTopicData.clear();
        mTopicData.add(new TopicBean(R.mipmap.icon_home_front, "Front Page"));
        mTopicData.add(new TopicBean(R.mipmap.icon_home_camera, "Camera"));
        mTopicData.add(new TopicBean(R.mipmap.icon_home_cv, "CV Process"));
        mTopicData.add(new TopicBean(R.mipmap.icon_home_basic, "Basic Process"));
        mTopicData.add(new TopicBean(R.mipmap.icon_home_arts, "My Arts"));
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

    @Override
    public void onTopicItemClick(TopicBean position) {
        if (position.getTitle().equals("Front Page")) {
            startActivity(new Intent(TopicActivity.this, MainActivity.class));
        } else if (position.getTitle().equals("Camera")) {
            startActivity(new Intent(TopicActivity.this, CameraOpenCVActivity.class));
        } else if (position.getTitle().equals("CV Process")) {
            startActivity(new Intent(TopicActivity.this, OpenCvImageProcessActivity.class));
        } else if (position.getTitle().equals("Basic Process")) {
            startActivity(new Intent(TopicActivity.this, JavaImageProcessActivity.class));
        } else if (position.getTitle().equals("My Arts")) {
            startActivity(new Intent(TopicActivity.this, CardArtActivity.class));
        }
    }

    public static int dp2px(Context context, float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, context.getResources().getDisplayMetrics());
    }
}