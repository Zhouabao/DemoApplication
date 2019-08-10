package com.example.demoapplication.nim.panel;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.blankj.utilcode.util.SizeUtils;
import com.example.demoapplication.nim.session.ChatBaseAction;
import com.netease.nim.uikit.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hzxuwen on 2015/3/10.
 */
public class ChatActionsPagerAdapter extends PagerAdapter {

    private final int ITEM_COUNT_PER_GRID_VIEW = 8;

    private final Context context;

    private final List<ChatBaseAction> actions;
    private final ViewPager viewPager;
    private final int gridViewCount = 1;
    private ChatActionsGridviewAdapter adapter;

    public ChatActionsPagerAdapter(ViewPager viewPager, List<ChatBaseAction> actions) {
        this.context = viewPager.getContext();
        this.actions = new ArrayList<>(actions);
        this.viewPager = viewPager;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        GridView gridView = new GridView(context);
        adapter = new ChatActionsGridviewAdapter(context, actions);
        gridView.setAdapter(adapter);
        gridView.setNumColumns(5);

        container.post(new Runnable() {
            @Override
            public void run() {
                ViewGroup.LayoutParams layoutParams = viewPager.getLayoutParams();
                layoutParams.height = SizeUtils.dp2px(24F);
                viewPager.setLayoutParams(layoutParams);
            }
        });
        gridView.setSelector(R.color.transparent);
        gridView.setHorizontalSpacing(0);
        gridView.setVerticalSpacing(0);
        gridView.setGravity(Gravity.CENTER);
        gridView.setTag(Integer.valueOf(position));
        gridView.setOnItemClickListener(new GridView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                actions.get(position).onClick();
                for (int i = 0; i < actions.size(); i++) {
                    if (i == position) {
                        actions.get(i).setCheck(!actions.get(i).isCheck());
                    } else {
                        actions.get(i).setCheck(false);
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });

        container.addView(gridView);
        return gridView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public int getCount() {
        return gridViewCount;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}
