package com.sdy.jitangapplication.nim.viewholder;

import android.content.Intent;
import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.SpanUtils;
import com.netease.nim.uikit.business.session.viewholder.MsgViewHolderBase;
import com.netease.nim.uikit.business.uinfo.UserInfoHelper;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.sdy.jitangapplication.R;
import com.sdy.jitangapplication.nim.attachment.SendCustomTipAttachment;
import com.sdy.jitangapplication.ui.activity.CandyMallActivity;
import com.sdy.jitangapplication.ui.activity.IDVerifyActivity;
import com.sdy.jitangapplication.ui.activity.MatchDetailActivity;
import com.sdy.jitangapplication.ui.dialog.ChatSendGiftDialog;

/**
 * author : ZFM
 * date   : 2019/8/1015:35
 * desc   : 发送礼物系列自定义tip
 * version: 1.0
 */
public class MsgViewHolderSendCustomTip extends MsgViewHolderBase {

    private TextView notificationTextView;
    private SendCustomTipAttachment attachment;


    public MsgViewHolderSendCustomTip(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

    @Override
    protected int getContentResId() {
        return R.layout.nim_message_item_notification;
    }

    @Override
    protected void inflateContentView() {
        //初始化数据
        notificationTextView = view.findViewById(com.netease.nim.uikit.R.id.message_item_notification_label);
    }

    @Override
    protected void bindContentView() {
        attachment = (SendCustomTipAttachment) message.getAttachment();
        switch (attachment.getShowType()) {
            case SendCustomTipAttachment.CUSTOM_TIP_FIRST_SEND_MAN://男方发送第一条消息（仅第一次发送消息会显示，切换用户不重复发送） 1
            case SendCustomTipAttachment.CUSTOM_TIP_NO_MONEY_MAN://男方发送消息余额不足 2
            case SendCustomTipAttachment.CUSTOME_TIP_RECEIVE_NOT_HUMAN://发件方 收件方非真人 8
            case SendCustomTipAttachment.CUSTOME_TIP_NORMAL://常规的tip 9
                notificationTextView.setText(attachment.getContent());
                break;
            case SendCustomTipAttachment.CUSTOME_TIP_RECEIVE_VERIFY_WOMAN://认证后的女方收到第一条消息（仅显示一次，切换用户不重复发送） 3
                notificationTextView.setText(SpanUtils.with(notificationTextView)
                        .append("回复消息将获得一个对方赠送的糖果，糖果可以")
                        .setForegroundColor(Color.parseColor("#FFC5C6C8"))
                        .append("兑换礼物")
                        .setClickSpan(new ClickableSpan() {
                            @Override
                            public void updateDrawState(@NonNull TextPaint ds) {
                                ds.setColor(Color.parseColor("#FF6796FA"));
                                ds.setUnderlineText(false);
                            }

                            @Override
                            public void onClick(@NonNull View widget) {
                                Intent intent = new Intent(context, CandyMallActivity.class);
                                context.startActivity(intent);
                            }
                        })
                        .setForegroundColor(Color.parseColor("#FF6796FA"))
                        .append("哦")
                        .setForegroundColor(Color.parseColor("#FFC5C6C8"))
                        .create());
                break;
            case SendCustomTipAttachment.CUSTOME_TIP_RECEIVE_NOT_VERIFY_WOMAN://未认证的女方收到第一条消息（切换用户重复发送）4
                notificationTextView.setText(SpanUtils.with(notificationTextView)
                        .append("认证后每次回复能收到对方糖果，")
                        .setForegroundColor(Color.parseColor("#FFC5C6C8"))
                        .append("立即认证")
                        .setClickSpan(new ClickableSpan() {
                            @Override
                            public void updateDrawState(@NonNull TextPaint ds) {
                                ds.setColor(Color.parseColor("#FF6796FA"));
                                ds.setUnderlineText(false);
                            }
                            @Override
                            public void onClick(@NonNull View widget) {
                                Intent intent = new Intent(context, IDVerifyActivity.class);
                                context.startActivity(intent);
                            }
                        })
                        .setForegroundColor(Color.parseColor("#FF6796FA"))
                        .create());
                break;
            case SendCustomTipAttachment.CUSTOME_TIP_WOMAN_CHAT_COUNT://女方聊天条数到达设定条数时,没有心愿礼物 5
                notificationTextView.setText(SpanUtils.with(notificationTextView)
                        .append("你还没有心愿礼物，快去")
                        .setForegroundColor(Color.parseColor("#FFC5C6C8"))
                        .append("糖果商城")
                        .setClickSpan(new ClickableSpan() {
                            @Override
                            public void updateDrawState(@NonNull TextPaint ds) {
                                ds.setColor(Color.parseColor("#FF6796FA"));
                                ds.setUnderlineText(false);
                            }

                            @Override
                            public void onClick(@NonNull View widget) {
                                Intent intent = new Intent(context, CandyMallActivity.class);
                                context.startActivity(intent);
                            }
                        })
                        .setForegroundColor(Color.parseColor("#FF6796FA"))
                        .append("看看吧")
                        .setForegroundColor(Color.parseColor("#FFC5C6C8"))
                        .create());
                break;
            case SendCustomTipAttachment.CUSTOME_TIP_MAN_BIGGER_CHAT_COUNT_HAS_GIFT://男方 双方聊天数>设定聊天条数  女方有心愿礼物 6
                notificationTextView.setText(SpanUtils.with(notificationTextView)
                        .append("你们聊的还不错，")
                        .setForegroundColor(Color.parseColor("#FFC5C6C8"))
                        .append("看看她喜欢什么东西吧！")
                        .setClickSpan(new ClickableSpan() {
                            @Override
                            public void updateDrawState(@NonNull TextPaint ds) {
                                ds.setColor(Color.parseColor("#FF6796FA"));
                                ds.setUnderlineText(false);
                            }

                            @Override
                            public void onClick(@NonNull View widget) {
                                MatchDetailActivity.start(context, message.getFromAccount(), -1, -1);
                            }
                        })
                        .setForegroundColor(Color.parseColor("#FF6796FA"))
                        .create());
                break;
            case SendCustomTipAttachment.CUSTOME_TIP_MAN_BIGGER_CHAT_COUNT_NOT_HAS_GIFT://男方 双方聊天数>设定聊天条数  女方没有心愿礼物 7
                notificationTextView.setText(SpanUtils.with(notificationTextView)
                        .append("你们聊的好像还不错，要不")
                        .setForegroundColor(Color.parseColor("#FFC5C6C8"))
                        .append("送个礼物")
                        .setClickSpan(new ClickableSpan() {
                            @Override
                            public void updateDrawState(@NonNull TextPaint ds) {
                                ds.setColor(Color.parseColor("#FF6796FA"));
                                ds.setUnderlineText(false);
                            }

                            @Override
                            public void onClick(@NonNull View widget) {
                                new ChatSendGiftDialog(UserInfoHelper.getUserTitleName(message.getFromAccount(), SessionTypeEnum.P2P),
                                        UserInfoHelper.getAvatar(message.getFromAccount()), message.getFromAccount(), context).show();
                            }
                        })
                        .setForegroundColor(Color.parseColor("#FF6796FA"))
                        .append("给她吧？")
                        .setForegroundColor(Color.parseColor("#FFC5C6C8"))
                        .create());
                break;
            default:
                notificationTextView.setText(attachment.getContent());
                break;
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
        return false;
    }

    @Override
    protected boolean isMiddleItem() {
        return true;
    }

    @Override
    protected void onItemClick() {

    }
}
