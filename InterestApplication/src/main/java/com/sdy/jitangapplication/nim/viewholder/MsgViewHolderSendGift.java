package com.sdy.jitangapplication.nim.viewholder;

import android.widget.RelativeLayout;
import android.widget.TextView;

import com.netease.nim.uikit.business.session.viewholder.MsgViewHolderBase;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.msg.MsgService;
import com.sdy.jitangapplication.R;
import com.sdy.jitangapplication.nim.attachment.SendGiftAttachment;
import com.sdy.jitangapplication.ui.dialog.ReceiveCandyGiftDialog;

import java.util.HashMap;

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
        if (message.getLocalExtension() != null) {
            int giftReceiveStatus = (int) message.getLocalExtension().get("giftReceiveStatus");
            if (isReceivedMessage()) {
                if (attachment.isIfVirtual()) {
                    if (giftReceiveStatus == SendGiftAttachment.GiftReceiveStatusNormal) {
                        giftTitle.setText("开启礼物");
                        giftType.setText("糖果礼物待开启");
                        giftStatusBg.setBackgroundResource(R.drawable.gradient_orange_0_18_18_0);
                    } else if (giftReceiveStatus == SendGiftAttachment.GiftReceiveStatusHasOpen) {
                        giftTitle.setText("礼物已开启");
                        giftType.setText("糖果礼物已被领取");
                        giftStatusBg.setBackgroundResource(R.drawable.gradient_light_orange_0_18_18_0);
                    } else if (giftReceiveStatus == SendGiftAttachment.GiftReceiveStatusHasReturned) {
                        giftTitle.setText("礼物已退回");
                        giftType.setText("糖果礼物已退回");
                        giftStatusBg.setBackgroundResource(R.drawable.gradient_light_orange_0_18_18_0);
                    }
                } else {
                    if (giftReceiveStatus == SendGiftAttachment.GiftReceiveStatusNormal) {
                        giftTitle.setText("开启礼物");
                        giftType.setText("心愿礼物待开启");
                        giftStatusBg.setBackgroundResource(R.drawable.gradient_orange_0_18_18_0);
                    } else if (giftReceiveStatus == SendGiftAttachment.GiftReceiveStatusHasOpen) {
                        giftTitle.setText("已接收礼物");
                        giftType.setText("已领取心愿礼物");
                        giftStatusBg.setBackgroundResource(R.drawable.gradient_light_orange_0_18_18_0);
                    } else if (giftReceiveStatus == SendGiftAttachment.GiftReceiveStatusHasReturned) {
                        giftTitle.setText("礼物已退回");
                        giftType.setText("心愿礼物已被退回");
                        giftStatusBg.setBackgroundResource(R.drawable.gradient_light_orange_0_18_18_0);
                    }
                }
            } else {
                if (attachment.isIfVirtual()) {
                    if (giftReceiveStatus == SendGiftAttachment.GiftReceiveStatusNormal) {
                        giftTitle.setText("待开启");
                        giftType.setText("糖果礼物待开启");
                        giftStatusBg.setBackgroundResource(R.drawable.gradient_orange_0_18_18_0);
                    } else if (giftReceiveStatus == SendGiftAttachment.GiftReceiveStatusHasOpen) {
                        giftTitle.setText("礼物已开启");
                        giftType.setText("糖果礼物已被领取");
                        giftStatusBg.setBackgroundResource(R.drawable.gradient_light_orange_0_18_18_0);
                    } else if (giftReceiveStatus == SendGiftAttachment.GiftReceiveStatusHasReturned) {
                        giftTitle.setText("超时已退回");
                        giftType.setText("糖果礼物已退回");
                        giftStatusBg.setBackgroundResource(R.drawable.gradient_light_orange_0_18_18_0);
                    }
                } else {
                    if (giftReceiveStatus == SendGiftAttachment.GiftReceiveStatusNormal) {
                        giftTitle.setText("待接收");
                        giftType.setText("心愿礼物待开启");
                        giftStatusBg.setBackgroundResource(R.drawable.gradient_orange_0_18_18_0);
                    } else if (giftReceiveStatus == SendGiftAttachment.GiftReceiveStatusHasOpen) {
                        giftTitle.setText("礼物已接收");
                        giftType.setText("心愿礼物已被领取");
                        giftStatusBg.setBackgroundResource(R.drawable.gradient_light_orange_0_18_18_0);
                    } else if (giftReceiveStatus == SendGiftAttachment.GiftReceiveStatusHasReturned) {
                        giftTitle.setText("礼物已退回");
                        giftType.setText("心愿礼物已被退回");
                        giftStatusBg.setBackgroundResource(R.drawable.gradient_light_orange_0_18_18_0);
                    }
                }
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
        attachment = (SendGiftAttachment) message.getAttachment();
        if (message.getLocalExtension() != null) {
            if ((int) message.getLocalExtension().get("giftReceiveStatus") == SendGiftAttachment.GiftReceiveStatusNormal) {
                new ReceiveCandyGiftDialog(context).show();

                HashMap<String, Object> params = new HashMap<>();
                params.put("giftReceiveStatus", SendGiftAttachment.GiftReceiveStatusHasOpen);
                message.setLocalExtension(params);
                NIMClient.getService(MsgService.class).updateIMMessage(message);
            }

            int giftReceiveStatus = (int) message.getLocalExtension().get("giftReceiveStatus");
            if (isReceivedMessage()) {
                if (attachment.isIfVirtual()) {
                    if (giftReceiveStatus == SendGiftAttachment.GiftReceiveStatusNormal) {
                        giftTitle.setText("开启礼物");
                        giftType.setText("糖果礼物待开启");
                        giftStatusBg.setBackgroundResource(R.drawable.gradient_orange_0_18_18_0);
                    } else if (giftReceiveStatus == SendGiftAttachment.GiftReceiveStatusHasOpen) {
                        giftTitle.setText("礼物已开启");
                        giftType.setText("糖果礼物已被领取");
                        giftStatusBg.setBackgroundResource(R.drawable.gradient_light_orange_0_18_18_0);
                    } else if (giftReceiveStatus == SendGiftAttachment.GiftReceiveStatusHasReturned) {
                        giftTitle.setText("礼物已退回");
                        giftType.setText("糖果礼物已退回");
                        giftStatusBg.setBackgroundResource(R.drawable.gradient_light_orange_0_18_18_0);
                    }
                } else {
                    if (giftReceiveStatus == SendGiftAttachment.GiftReceiveStatusNormal) {
                        giftTitle.setText("开启礼物");
                        giftType.setText("心愿礼物待开启");
                        giftStatusBg.setBackgroundResource(R.drawable.gradient_orange_0_18_18_0);
                    } else if (giftReceiveStatus == SendGiftAttachment.GiftReceiveStatusHasOpen) {
                        giftTitle.setText("已接收礼物");
                        giftType.setText("已领取心愿礼物");
                        giftStatusBg.setBackgroundResource(R.drawable.gradient_light_orange_0_18_18_0);
                    } else if (giftReceiveStatus == SendGiftAttachment.GiftReceiveStatusHasReturned) {
                        giftTitle.setText("礼物已退回");
                        giftType.setText("心愿礼物已被退回");
                        giftStatusBg.setBackgroundResource(R.drawable.gradient_light_orange_0_18_18_0);
                    }
                }
            } else {
                if (attachment.isIfVirtual()) {
                    if (giftReceiveStatus == SendGiftAttachment.GiftReceiveStatusNormal) {
                        giftTitle.setText("待开启");
                        giftType.setText("糖果礼物待开启");
                        giftStatusBg.setBackgroundResource(R.drawable.gradient_orange_0_18_18_0);
                    } else if (giftReceiveStatus == SendGiftAttachment.GiftReceiveStatusHasOpen) {
                        giftTitle.setText("礼物已开启");
                        giftType.setText("糖果礼物已被领取");
                        giftStatusBg.setBackgroundResource(R.drawable.gradient_light_orange_0_18_18_0);
                    } else if (giftReceiveStatus == SendGiftAttachment.GiftReceiveStatusHasReturned) {
                        giftTitle.setText("超时已退回");
                        giftType.setText("糖果礼物已退回");
                        giftStatusBg.setBackgroundResource(R.drawable.gradient_light_orange_0_18_18_0);
                    }
                } else {
                    if (giftReceiveStatus == SendGiftAttachment.GiftReceiveStatusNormal) {
                        giftTitle.setText("待接收");
                        giftType.setText("心愿礼物待开启");
                        giftStatusBg.setBackgroundResource(R.drawable.gradient_orange_0_18_18_0);
                    } else if (giftReceiveStatus == SendGiftAttachment.GiftReceiveStatusHasOpen) {
                        giftTitle.setText("礼物已接收");
                        giftType.setText("心愿礼物已被领取");
                        giftStatusBg.setBackgroundResource(R.drawable.gradient_light_orange_0_18_18_0);
                    } else if (giftReceiveStatus == SendGiftAttachment.GiftReceiveStatusHasReturned) {
                        giftTitle.setText("礼物已退回");
                        giftType.setText("心愿礼物已被退回");
                        giftStatusBg.setBackgroundResource(R.drawable.gradient_light_orange_0_18_18_0);
                    }
                }
            }
        }


    }
}
