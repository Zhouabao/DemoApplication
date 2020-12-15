package com.sdy.jitangapplication.nim.viewholder;

import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.ActivityUtils;
import com.sdy.jitangapplication.R;
import com.sdy.jitangapplication.nim.attachment.ChatUpAttachment;
import com.sdy.jitangapplication.nim.uikit.business.session.viewholder.MsgViewHolderBase;
import com.sdy.jitangapplication.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;
import com.sdy.jitangapplication.nim.uikit.impl.NimUIKitImpl;
import com.sdy.jitangapplication.ui.activity.CustomChatUpContentActivity;

/**
 * author : ZFM
 * date   : 2019/8/1015:35
 * desc   :
 * version: 1.0
 */
public class MsgViewHolderChatUp extends MsgViewHolderBase {

    private LinearLayout contentView;
    private TextView chatUpContent,
            customChatUpBtn;//自定义搭讪语btn

    public MsgViewHolderChatUp(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

    @Override
    protected int getContentResId() {
        return R.layout.item_chat_up;
    }

    @Override
    protected void inflateContentView() {
        //初始化数据
        contentView = findViewById(R.id.contentView);
        chatUpContent = findViewById(R.id.chatUpContentTv);
        customChatUpBtn = findViewById(R.id.chatUpCustomBtn);
    }

    @Override
    protected void bindContentView() {
        ChatUpAttachment attachment = (ChatUpAttachment) message.getAttachment();
        if (isReceivedMessage()) {
//            setGravity(contentView,Gravity.LEFT);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) chatUpContent.getLayoutParams();
            params.gravity = Gravity.LEFT;
            chatUpContent.setTextColor(Color.BLACK);
            chatUpContent.setBackgroundResource(NimUIKitImpl.getOptions().messageLeftBackground);
            customChatUpBtn.setVisibility(View.GONE);
        } else {
//            setGravity(contentView,Gravity.RIGHT);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) chatUpContent.getLayoutParams();
            params.gravity = Gravity.RIGHT;
            chatUpContent.setTextColor(Color.WHITE);
            chatUpContent.setBackgroundResource(NimUIKitImpl.getOptions().messageRightBackground);
            customChatUpBtn.setVisibility(View.VISIBLE);

        }
        chatUpContent.setText(attachment.getChatUpContent());
        customChatUpBtn.setOnClickListener(v -> ActivityUtils.startActivity(CustomChatUpContentActivity.class));
    }

    @Override
    protected int leftBackground() {
        return R.color.transparent;
    }

    @Override
    protected int rightBackground() {
        return R.color.transparent;
    }

    @Override
    protected boolean isShowBubble() {
        return true;
    }
}
