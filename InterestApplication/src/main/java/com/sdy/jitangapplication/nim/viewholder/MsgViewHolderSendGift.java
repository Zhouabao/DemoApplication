package com.sdy.jitangapplication.nim.viewholder;

import android.widget.ImageView;
import android.widget.TextView;

import com.netease.nim.uikit.business.session.viewholder.MsgViewHolderBase;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;
import com.sdy.baselibrary.glide.GlideUtil;
import com.sdy.jitangapplication.R;
import com.sdy.jitangapplication.nim.attachment.SendGiftAttachment;
import com.sdy.jitangapplication.ui.activity.SquarePlayListDetailActivity;

/**
 * author : ZFM
 * date   : 2019/8/1015:35
 * desc   :
 * version: 1.0
 */
public class MsgViewHolderSendGift extends MsgViewHolderBase {

    private TextView giftTitle; //分享描述文本
    private TextView giftReceiver; //分享描述文本
    private ImageView giftImg;//分享的图片
    private SendGiftAttachment attachment;


    public MsgViewHolderSendGift(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

    @Override
    protected int getContentResId() {
        return R.layout.item_chat_send_gift;
    }

    @Override
    protected void inflateContentView() {
        //初始化数据
        giftTitle = findViewById(R.id.giftTitle);
        giftReceiver = findViewById(R.id.giftReceiver);
        giftImg = findViewById(R.id.giftImg);

    }

    @Override
    protected void bindContentView() {
        attachment = (SendGiftAttachment) message.getAttachment();
        giftTitle.setText(attachment.getTitle());
        GlideUtil.loadImgCenterCrop(context, attachment.getImg(), giftImg);
        if (isReceivedMessage()) {
            giftReceiver.setText("Ta送了礼物给你");
        } else {
            giftReceiver.setText("送礼物给Ta");
        }
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
        SquarePlayListDetailActivity.Companion.start(context, attachment.getId());
    }
}
