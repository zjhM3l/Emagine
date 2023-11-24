package com.demo.opencv;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.demo.opencv.models.Want;
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

import java.util.ArrayList;
import java.util.List;

public class TradeMSGActivity extends AppCompatActivity implements TopicAdapter.OnItemClickListener{

    Boolean change;
    Button mBtnChange;

    private ArrayList<TopicBean> mTopicData = new ArrayList<>();
    private ArrayList<TopicBean> mTopicData1 = new ArrayList<>();
    ListView listView;

    List<Want> wantList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trade_msgactivity);

        change = true;
        mBtnChange = findViewById(R.id.btn_change);

        SharedPreferences preferences = getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
        String username = preferences.getString("username", "");

        listView = (ListView) findViewById(R.id.list_view);
        if (change == true) {
            mBtnChange.setText("Want");
            wantList = LitePal.where("ownerName = ?", username).find(Want.class);
            TradeAdapter tradeAdapter = new TradeAdapter(TradeMSGActivity.this, R.layout.layout_trade_item, wantList);
            listView.setAdapter(tradeAdapter);
        } else {
            mBtnChange.setText("Own");
            wantList = LitePal.where("reqName = ?", username).find(Want.class);
            TradeAdapterReq tradeAdapter = new TradeAdapterReq(TradeMSGActivity.this, R.layout.layout_trade_item, wantList);
            listView.setAdapter(tradeAdapter);
        }

//        List<Want> wantList = LitePal.findAll(Want.class);
//        List<Want> wantList = LitePal.where("ownerName = ? or reqName = ?", username, username).find(Want.class);

//        Owns ownShip = LitePal.where("title = ?", name).findFirst(Owns.class);


        //Tap to open the other person's message
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Want want = wantList.get(i);
                String ownerName = want.getOwnerName();
                String reqName = want.getReqName();
                SharedPreferences preferences = getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
                String username = preferences.getString("username", "");
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                if (username.equals(ownerName)) {
                    bundle.putString("owner", reqName);
                } else {
                    bundle.putString("owner", ownerName);
                }
                intent.putExtras(bundle);
                intent.setClass(TradeMSGActivity.this, UserOtherInfo.class);
                startActivity(intent);
            }
        });
        //Long press to enter the verification code
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Want want = wantList.get(i);
                String uniqueCode = want.getTradeCode();
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString("code", uniqueCode);
                intent.putExtras(bundle);
                intent.setClass(TradeMSGActivity.this, ConfirmCodeActivity.class);
                startActivity(intent);
                return true;
            }
        });

        mBtnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (change == true) {
                    mBtnChange.setText("Own");
                    change = false;
                    wantList = LitePal.where("reqName = ?", username).find(Want.class);
                    TradeAdapterReq tradeAdapter = new TradeAdapterReq(TradeMSGActivity.this, R.layout.layout_trade_item, wantList);
                    listView.setAdapter(tradeAdapter);
                } else {
                    mBtnChange.setText("Want");
                    change = true;
                    wantList = LitePal.where("ownerName = ?", username).find(Want.class);
                    TradeAdapter tradeAdapter = new TradeAdapter(TradeMSGActivity.this, R.layout.layout_trade_item, wantList);
                    listView.setAdapter(tradeAdapter);
                }
            }
        });

        initTopicData();
        initTypeViewPager(2, 5);
        initTopicData1();
        initTypeViewPager1(1, 3);
    }

    private void initTopicData1() {
        mTopicData1.clear();
        mTopicData1.add(new TopicBean(R.mipmap.icon_home_trade, "Trade"));
        mTopicData1.add(new TopicBean(R.mipmap.icon_home_info, "Info"));
        mTopicData1.add(new TopicBean(R.mipmap.icon_home_arts, "Arts"));
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
                indicator.setLineHeight(UIUtil.dip2px(context, 3));//is the height of the indicator
                indicator.setLineWidth(UIUtil.dip2px(context, 66 / finalPageNum));//is the width of the indicator, and then scored by the number of pages
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
            startActivity(new Intent(TradeMSGActivity.this, MainActivity.class));
        } else if (position.getTitle().equals("Camera")) {
            startActivity(new Intent(TradeMSGActivity.this, CameraOpenCVActivity.class));
        } else if (position.getTitle().equals("CV Process")) {
            startActivity(new Intent(TradeMSGActivity.this, OpenCvImageProcessActivity.class));
        } else if (position.getTitle().equals("Basic Process")) {
            startActivity(new Intent(TradeMSGActivity.this, JavaImageProcessActivity.class));
        } else if (position.getTitle().equals("Info")) {
            startActivity(new Intent(TradeMSGActivity.this, UserSelfInfo.class));
        } else if (position.getTitle().equals("Trade")) {
            Toast.makeText(TradeMSGActivity.this,"Already on the current page！",Toast.LENGTH_SHORT).show();
        } else if (position.getTitle().equals("Arts")) {
            startActivity(new Intent(TradeMSGActivity.this, CardArtActivity.class));
        }
    }


    public static int dp2px(Context context, float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, context.getResources().getDisplayMetrics());
    }

}