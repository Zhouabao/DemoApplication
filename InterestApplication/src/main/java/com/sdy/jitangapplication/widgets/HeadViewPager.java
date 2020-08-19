package com.sdy.jitangapplication.widgets;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

import com.sdy.jitangapplication.R;
import com.sdy.jitangapplication.ui.adapter.HeadViewPagerAdapter;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * author : ZFM
 * date   : 2020/8/1915:08
 * desc   :
 * version: 1.0
 */
public class HeadViewPager extends FrameLayout {
    private Context mContext;
    private NoScrollViewPager mViewPager;
    private List<Integer> mImageIds;
    private List<CircleImageView> mImageViews;
    private List<ImageView> tips;

    public HeadViewPager(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        creatView(context);
    }

    public HeadViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        creatView(context);
    }

    public HeadViewPager(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        creatView(context);
    }

    public HeadViewPager(Context context, List<CircleImageView> imgageList) {
        super(context);
        // TODO Auto-generated constructor stub
        creatView(context, imgageList);
    }

    public void creatView(Context context) {
        this.mContext = context;
        LayoutInflater.from(context).inflate(R.layout.head_view_pager, this);
        mViewPager = (NoScrollViewPager) findViewById(R.id.viewpager);

        mImageViews = new ArrayList<CircleImageView>();
        mImageIds = new ArrayList<Integer>();
        tips = new ArrayList<ImageView>();

        build();
    }

    public void creatView(Context context, List<CircleImageView> imgageList) {
        this.mContext = context;
        LayoutInflater.from(context).inflate(R.layout.head_view_pager, this);
        mViewPager = (NoScrollViewPager) findViewById(R.id.viewpager);
        mImageViews = imgageList;
        mImageIds = new ArrayList<Integer>();
        tips = new ArrayList<ImageView>();

        build();
    }

    public void build() {
        mViewPager.setScrollable(false);
        mViewPager.setAdapter(new HeadViewPagerAdapter(mContext, mImageViews));
        // 设置默认显示页面为第0页
        mViewPager.setCurrentItem(0);
        // 设置选择页面时的动画
        mViewPager.setPageTransformer(true, new HeadViewPagerTransformer());
        // 设置缓存View的个数，默认是3个，这表示缓存了5个
        // mViewPager.setOffscreenPageLimit(mImageViews.size());
    }

    public Context getmContext() {
        return mContext;
    }

    public void setmContext(Context mContext) {
        this.mContext = mContext;
    }

    public ViewPager getmViewPager() {
        return mViewPager;
    }

    public void setmViewPager(NoScrollViewPager mViewPager) {
        this.mViewPager = mViewPager;
    }

    public List<CircleImageView> getmImageViews() {
        return mImageViews;
    }

    // 改变图片队列时，要更新整个viewPager
    public void setmImageViews(List<CircleImageView> mImageViews) {
        this.mImageViews = mImageViews;
        this.mViewPager.notify();
        this.mViewPager.setCurrentItem(1);

    }

    class MyHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            // int nextItem = (mViewPager.getCurrentItem() + 1) % mImageViews.size();
            //
            // if (nextItem == 0) {
            // // if (mImageViews.size() > 1)
            // // mViewPager.setCurrentItem(nextItem + 1, false);
            // // else
            // mViewPager.setCurrentItem(nextItem, false);
            // } else if (nextItem == mImageViews.size() - 1) {
            // mViewPager.setCurrentItem(1, false);
            // } else {
            // mViewPager.setCurrentItem(nextItem);
            // }

            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
            handler.sendMessageDelayed(Message.obtain(), delayTime);
        }
    }

    private MyHandler handler;
    private long delayTime = 1500L;

    public void startLoop() {
        if (handler == null) {
            handler = new MyHandler();
        }
        handler.removeCallbacksAndMessages(null);
        handler.sendMessageDelayed(Message.obtain(), delayTime);
    }

    public void stopLoop() {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (handler != null)
            handler.removeCallbacksAndMessages(null);
        handler = null;

    }
}
