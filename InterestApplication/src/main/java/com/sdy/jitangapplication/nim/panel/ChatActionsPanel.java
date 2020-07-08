package com.sdy.jitangapplication.nim.panel;

import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import com.sdy.jitangapplication.R;
import com.sdy.jitangapplication.nim.session.ChatBaseAction;

import java.util.List;

/**
 * 更多操作模块
 * Created by hzxuwen on 2015/6/17.
 */
public class ChatActionsPanel {
    // 初始化更多布局adapter
    public static void init(View view, final List<ChatBaseAction> actions) {
        GridView gridView = view.findViewById(R.id.viewPager);
        final ChatActionsGridviewAdapter  adapter = new ChatActionsGridviewAdapter(view.getContext(), actions);
        gridView.setAdapter(adapter);
        gridView.setNumColumns(5);
        gridView.setSelector(R.color.transparent);
        gridView.setHorizontalSpacing(0);
        gridView.setVerticalSpacing(0);
        gridView.setGravity(Gravity.CENTER);
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
    }

}
