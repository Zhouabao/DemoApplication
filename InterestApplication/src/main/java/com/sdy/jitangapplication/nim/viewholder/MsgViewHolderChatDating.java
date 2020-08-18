package com.sdy.jitangapplication.nim.viewholder;

import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.SizeUtils;
import com.sdy.baselibrary.glide.GlideUtil;
import com.sdy.jitangapplication.R;
import com.sdy.jitangapplication.nim.attachment.ChatDatingAttachment;
import com.sdy.jitangapplication.nim.uikit.business.session.viewholder.MsgViewHolderBase;
import com.sdy.jitangapplication.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;
import com.sdy.jitangapplication.ui.activity.DatingDetailActivity;

/**
 * author : ZFM
 * date   : 2019/8/1015:35
 * desc   :
 * version: 1.0
 */
public class MsgViewHolderChatDating extends MsgViewHolderBase {

    private TextView datingContent; // 分享文字内容
    private TextView datingName; // 分享文字内容
    private ImageView datingImg;// 分享的图片
    private ChatDatingAttachment attachment;

    public MsgViewHolderChatDating(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

    @Override
    protected int getContentResId() {
        return R.layout.item_chat_dating;
    }

    @Override
    protected void inflateContentView() {
        // 初始化数据
        datingName = findViewById(R.id.datingName);
        datingContent = findViewById(R.id.datingContent);
        datingImg = findViewById(R.id.datingImg);
    }

    @Override
    protected void bindContentView() {
        attachment = (ChatDatingAttachment) message.getAttachment();
        datingContent.setText(attachment.getContent());
        GlideUtil.loadRoundImgCenterCrop(context, attachment.getImg(), datingImg, SizeUtils.dp2px(10F));
    }

    @Override
    protected int leftBackground() {
        return R.drawable.shape_rectangle_share_square_bg_left;
    }

    @Override
    protected int rightBackground() {
        return R.drawable.shape_rectangle_share_square_bg;
    }

    @Override
    protected boolean shouldDisplayReceipt() {
        return false;
    }

    @Override
    protected boolean shouldDisplayNick() {
        return false;
    }

    @Override
    protected boolean isShowBubble() {

        return true;
    }

    @Override
    protected void onItemClick() {
        DatingDetailActivity.Companion.start2Detail(context, attachment.getId());
    }
}
