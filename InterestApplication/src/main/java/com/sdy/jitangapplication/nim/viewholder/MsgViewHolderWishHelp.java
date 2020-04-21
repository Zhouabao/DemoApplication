package com.sdy.jitangapplication.nim.viewholder;

import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.netease.nim.uikit.business.session.viewholder.MsgViewHolderBase;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;
import com.sdy.baselibrary.glide.GlideUtil;
import com.sdy.jitangapplication.R;
import com.sdy.jitangapplication.nim.attachment.WishHelpAttachment;

/**
 * author : ZFM
 * date   : 2019/8/1015:35
 * desc   : 心愿助力
 * version: 1.0
 */
public class MsgViewHolderWishHelp extends MsgViewHolderBase {

    private TextView giftTitle; //礼物的名字
    private ImageView wishHelpImg; //礼物的名字
    private TextView wishHelpAmount; //赠送的糖果额度
    private RelativeLayout giftStatusBg;//礼物背景状态
    private WishHelpAttachment attachment;


    public MsgViewHolderWishHelp(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

    @Override
    protected int getContentResId() {
        return R.layout.item_chat_wish_help;
    }

    @Override
    protected void inflateContentView() {
        //初始化数据
        giftTitle = findViewById(R.id.wishHelpTitle);
        wishHelpAmount = findViewById(R.id.wishHelpAmount);
        wishHelpImg = findViewById(R.id.wishHelpImg);
        giftStatusBg = findViewById(R.id.wishHelpStatusBg);
    }

    @Override
    protected void bindContentView() {
        attachment = (WishHelpAttachment) message.getAttachment();
        int giftReceiveStatus = attachment.getWishHelpStatus();
        if (giftReceiveStatus == WishHelpAttachment.WISH_HELP_STATUS_NORMAL || giftReceiveStatus == WishHelpAttachment.WISH_HELP_STATUS_HAS_OPEN) {
            giftStatusBg.setBackgroundResource(R.drawable.gradient_white_0_18_18_0);
        } else if (giftReceiveStatus == WishHelpAttachment.WISH_HELP_STATUS_HAS_RETURNED) {
            giftStatusBg.setBackgroundResource(R.drawable.gradient_gray_0_18_18_0);
        }
        wishHelpAmount.setText("助力额度" + attachment.getAmount() + "糖果");
        GlideUtil.loadImgCenterCrop(context, attachment.getIcon(), wishHelpImg);
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


    }
}
