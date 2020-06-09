package com.sdy.jitangapplication.nim.viewholder;

import android.widget.RelativeLayout;
import android.widget.TextView;

import com.netease.nim.uikit.business.session.viewholder.MsgViewHolderBase;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;
import com.sdy.jitangapplication.R;
import com.sdy.jitangapplication.nim.attachment.AccostGiftAttachment;
import com.sdy.jitangapplication.nim.attachment.SendGiftAttachment;
import com.sdy.jitangapplication.ui.dialog.ReceiveAccostGiftDialog;

/**
 * author : ZFM
 * date   : 2019/8/1015:35
 * desc   :
 * version: 1.0
 */
public class MsgViewHolderAccostGift extends MsgViewHolderBase {

    private TextView giftTitle; //礼物的名字
    private RelativeLayout giftStatusBg;//礼物背景状态
    private AccostGiftAttachment attachment;


    public MsgViewHolderAccostGift(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

    @Override
    protected int getContentResId() {
        return R.layout.item_chat_accost_gift;
    }

    @Override
    protected void inflateContentView() {
        //初始化数据
        giftTitle = findViewById(R.id.giftTitle);
        giftStatusBg = findViewById(R.id.giftStatusBg);
    }

    @Override
    protected void bindContentView() {
        attachment = (AccostGiftAttachment) message.getAttachment();
        int giftReceiveStatus = attachment.getGiftStatus();
        if (isReceivedMessage()) {
            if (giftReceiveStatus == SendGiftAttachment.GIFT_RECEIVE_STATUS_NORMAL) {
                giftTitle.setText("礼物名字");
                giftStatusBg.setBackgroundResource(R.drawable.gradient_white_0_18_18_0);
            } else if (giftReceiveStatus == SendGiftAttachment.GIFT_RECEIVE_STATUS_HAS_OPEN) {
                giftTitle.setText("礼物已开启");
                giftStatusBg.setBackgroundResource(R.drawable.gradient_gray_0_18_18_0);
            } else if (giftReceiveStatus == SendGiftAttachment.GIFT_RECEIVE_STATUS_HAS_RETURNED) {
                giftTitle.setText("礼物已退回");
                giftStatusBg.setBackgroundResource(R.drawable.gradient_gray_0_18_18_0);
            }

        } else {
            if (giftReceiveStatus == SendGiftAttachment.GIFT_RECEIVE_STATUS_NORMAL) {
                giftTitle.setText("礼物名字");
                giftStatusBg.setBackgroundResource(R.drawable.gradient_white_0_18_18_0);
            } else if (giftReceiveStatus == SendGiftAttachment.GIFT_RECEIVE_STATUS_HAS_OPEN) {
                giftTitle.setText("礼物已开启");
                giftStatusBg.setBackgroundResource(R.drawable.gradient_gray_0_18_18_0);
            } else if (giftReceiveStatus == SendGiftAttachment.GIFT_RECEIVE_STATUS_HAS_RETURNED) {
                giftTitle.setText("超时已退回");
                giftStatusBg.setBackgroundResource(R.drawable.gradient_gray_0_18_18_0);
            }

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
        contentContainer.setEnabled(false);

        new ReceiveAccostGiftDialog(context).show();

//        attachment = (AccostGiftAttachment) message.getAttachment();
//        CommonFunction.INSTANCE.openGiftLetter(isReceivedMessage(), attachment.getGiftStatus(), attachment.getId(), context, message.getSessionId());
        contentContainer.postDelayed(() -> contentContainer.setEnabled(true), 2000L);
    }
}
