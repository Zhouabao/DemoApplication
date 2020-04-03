package com.sdy.jitangapplication.nim.panel;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

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

        if (viewHolder.isEnable()) {
            if (viewHolder.isCheck()) {
                ((ImageView) itemlayout.findViewById(R.id.imageView)).setImageResource(viewHolder.getIconResIdChoose());
            } else {
                ((ImageView) itemlayout.findViewById(R.id.imageView)).setImageResource(viewHolder.getIconResIdChoose());
            }
        } else {
            ((ImageView) itemlayout.findViewById(R.id.imageView)).setImageResource(viewHolder.getIconResId());
        }
        ((TextView) itemlayout.findViewById(R.id.textView)).setText(context.getString(viewHolder.getTitleId()));

        if (position == getCount() - 1) {
            itemlayout.setEnabled(true);
            itemlayout.findViewById(R.id.imageView).startAnimation(AnimationUtils.loadAnimation(context, R.anim.anim_small_to_big_gift));
        } else {
            itemlayout.setEnabled(viewHolder.isEnable());
        }
        return itemlayout;
    }
}

