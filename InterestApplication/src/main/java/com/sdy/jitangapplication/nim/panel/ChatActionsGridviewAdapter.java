package com.sdy.jitangapplication.nim.panel;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.sdy.jitangapplication.R;
import com.sdy.jitangapplication.nim.session.ChatBaseAction;

import java.util.List;

public class ChatActionsGridviewAdapter extends BaseAdapter {

    private Context context;

    private List<ChatBaseAction> baseActions;

    public ChatActionsGridviewAdapter(Context context, List<ChatBaseAction> baseActions) {
        this.context = context;
        this.baseActions = baseActions;
    }

    @Override
    public int getCount() {
        return baseActions.size();
    }

    @Override
    public Object getItem(int position) {
        return baseActions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View itemlayout;
        ChatBaseAction viewHolder = baseActions.get(position);
        if (convertView == null) {
            itemlayout = LayoutInflater.from(context).inflate(R.layout.chat_nim_actions_item_layout, null);
        } else {
            itemlayout = convertView;
        }

        itemlayout.setEnabled(viewHolder.isEnable());
        if (viewHolder.isEnable()) {
            if (viewHolder.isCheck()) {
                ((ImageView) itemlayout.findViewById(R.id.imageView)).setImageResource(viewHolder.getIconResIdChoose());
            } else {
                ((ImageView) itemlayout.findViewById(R.id.imageView)).setImageResource(viewHolder.getIconResId());
            }
        } else {
            ((ImageView) itemlayout.findViewById(R.id.imageView)).setImageResource(viewHolder.getIconResIdDisable());
        }
        return itemlayout;
    }
}

