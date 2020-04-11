package com.sdy.jitangapplication.nim.panel;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
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

        if (position != getCount() - 1) {
            if (viewHolder.isEnable()) {
                if (viewHolder.isCheck()) {
                    ((LottieAnimationView) itemlayout.findViewById(R.id.imageView)).setImageResource(viewHolder.getIconResIdChoose());
                } else {
                    ((LottieAnimationView) itemlayout.findViewById(R.id.imageView)).setImageResource(viewHolder.getIconResIdChoose());
                }
            } else {
                ((LottieAnimationView) itemlayout.findViewById(R.id.imageView)).setImageResource(viewHolder.getIconResId());
            }
        } else {
            ((LottieAnimationView) itemlayout.findViewById(R.id.imageView)).setImageAssetsFolder("images_gift");
            ((LottieAnimationView) itemlayout.findViewById(R.id.imageView)).setRepeatCount(-1);
            ((LottieAnimationView) itemlayout.findViewById(R.id.imageView)).setAnimation("data_gift.json");
            ((LottieAnimationView) itemlayout.findViewById(R.id.imageView)).playAnimation();
        }
        ((TextView) itemlayout.findViewById(R.id.textView)).setText(context.getString(viewHolder.getTitleId()));

        if (position == getCount() - 1) {
            itemlayout.setEnabled(true);
        } else {
            itemlayout.setEnabled(viewHolder.isEnable());
        }
        return itemlayout;
    }
}

