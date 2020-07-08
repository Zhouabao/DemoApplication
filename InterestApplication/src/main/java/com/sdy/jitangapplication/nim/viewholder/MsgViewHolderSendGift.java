package com.sdy.jitangapplication.nim.viewholder;

import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sdy.jitangapplication.R;
import com.sdy.jitangapplication.common.CommonFunction;
import com.sdy.jitangapplication.nim.attachment.SendGiftAttachment;
import com.sdy.jitangapplication.nim.uikit.business.session.viewholder.MsgViewHolderBase;
import com.sdy.jitangapplication.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;

/**
 * author : ZFM
 * date   : 2019/8/1015:35
 * desc   :
 * version: 1.0
 */
public class MsgViewHolderSendGift extends MsgViewHolderBase {

    private TextView giftTitle; //礼物的名字
    private TextView giftType; //礼物状态类型
    private RelativeLayout giftStatusBg;//礼物背景状态
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
        giftType = findViewById(R.id.giftType);
        giftStatusBg = findViewById(R.id.giftStatusBg);
    }

    @Override
    protected void bindContentView() {
        attachment = (SendGiftAttachment) message.getAttachment();
        int giftReceiveStatus = attachment.getGiftStatus();
        if (isReceivedMessage()) {
            if (giftReceiveStatus == SendGiftAttachment.GIFT_RECEIVE_STATUS_NORMAL) {
                giftTitle.setText("开启礼物");
                giftType.setText("糖果礼物待开启");
                giftStatusBg.setBackgroundResource(R.drawable.gradient_orange_0_18_18_0);
            } else if (giftReceiveStatus == SendGiftAttachment.GIFT_RECEIVE_STATUS_HAS_OPEN) {
                giftTitle.setText("礼物已开启");
                giftType.setText("已领取糖果礼物");
                giftStatusBg.setBackgroundResource(R.drawable.gradient_light_orange_0_18_18_0);
            } else if (giftReceiveStatus == SendGiftAttachment.GIFT_RECEIVE_STATUS_HAS_RETURNED) {
                giftTitle.setText("礼物已退回");
                giftType.setText("糖果礼物已退回");
                giftStatusBg.setBackgroundResource(R.drawable.gradient_light_orange_0_18_18_0);
            }

        } else {
            if (giftReceiveStatus == SendGiftAttachment.GIFT_RECEIVE_STATUS_NORMAL) {
                giftTitle.setText("待开启");
                giftType.setText("糖果礼物待开启");
                giftStatusBg.setBackgroundResource(R.drawable.gradient_orange_0_18_18_0);
            } else if (giftReceiveStatus == SendGiftAttachment.GIFT_RECEIVE_STATUS_HAS_OPEN) {
                giftTitle.setText("礼物已开启");
                giftType.setText("糖果礼物已被领取");
                giftStatusBg.setBackgroundResource(R.drawable.gradient_light_orange_0_18_18_0);
            } else if (giftReceiveStatus == SendGiftAttachment.GIFT_RECEIVE_STATUS_HAS_RETURNED) {
                giftTitle.setText("超时已退回");
                giftType.setText("糖果礼物已退回");
                giftStatusBg.setBackgroundResource(R.drawable.gradient_light_orange_0_18_18_0);
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
        attachment = (SendGiftAttachment) message.getAttachment();
        CommonFunction.INSTANCE.openGiftLetter(isReceivedMessage(), attachment.getGiftStatus(), attachment.getId(), context, message.getSessionId());
        contentContainer.postDelayed(() -> contentContainer.setEnabled(true), 2000L);
    }
}
