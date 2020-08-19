package com.sdy.jitangapplication.ui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.viewpager.widget.PagerAdapter;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * author : ZFM
 * date   : 2020/8/1915:07
 * desc   :
 * version: 1.0
 */
public class HeadViewPagerAdapter extends PagerAdapter {

    private Context mContext;
    private List<CircleImageView> mList;

    public HeadViewPagerAdapter(Context context, List<CircleImageView> list) {
        this.mContext = context;
        this.mList = list;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        // TODO Auto-generated method stub
        return arg0 == arg1;
    }

    // 当缓存view的数量超过上限时，会销毁最先的一个
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // TODO Auto-generated method stub
        // Log.d("remove", mImageViews[position].hashCode() + "");
        container.removeView((View) object);
    }

    // 添加View
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        // TODO Auto-generated method stub
        int newPosition = position % mList.size();
        CircleImageView civ = mList.get(newPosition);
        container.addView(civ);
        return civ;
    }

}
