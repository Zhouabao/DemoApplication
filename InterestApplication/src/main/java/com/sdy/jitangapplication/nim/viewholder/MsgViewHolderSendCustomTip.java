package com.sdy.jitangapplication.nim.viewholder;

import android.content.Intent;
import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.SpanUtils;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.sdy.jitangapplication.R;
import com.sdy.jitangapplication.common.CommonFunction;
import com.sdy.jitangapplication.nim.attachment.SendCustomTipAttachment;
import com.sdy.jitangapplication.nim.uikit.business.session.viewholder.MsgViewHolderBase;
import com.sdy.jitangapplication.nim.uikit.business.uinfo.UserInfoHelper;
import com.sdy.jitangapplication.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;
import com.sdy.jitangapplication.ui.activity.IDVerifyActivity;
import com.sdy.jitangapplication.ui.activity.MyCandyActivity;
import com.sdy.jitangapplication.ui.dialog.ChatSendGiftDialog;

import java.util.StringTokenizer;

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
        // 初始化数据
        notificationTextView = view.findViewById(R.id.message_item_notification_label);
    }

    @Override
    protected void bindContentView() {
        attachment = (SendCustomTipAttachment) message.getAttachment();
        switch (attachment.getShowType()) {
        case SendCustomTipAttachment.CUSTOME_TIP_NORMAL:// 常规的tip 11
            notificationTextView.setText(attachment.getContent());
            break;
        case SendCustomTipAttachment.CUSTOM_TIP_NO_MONEY_MAN:// 男方发送消息余额不足 1
            notificationTextView.setText(SpanUtils.with(notificationTextView).append("糖果余额不足消息未能发送，请")
                    .setForegroundColor(Color.parseColor("#FFC5C6C8")).append("充值糖果").setClickSpan(new ClickableSpan() {
                        @Override
                        public void updateDrawState(@NonNull TextPaint ds) {
                            ds.setColor(Color.parseColor("#FF6796FA"));
                            ds.setUnderlineText(false);
                        }

                        @Override
                        public void onClick(@NonNull View widget) {
                            CommonFunction.INSTANCE.gotoCandyRecharge(context);
                        }
                    }).setForegroundColor(Color.parseColor("#FF6796FA")).append("后重试")
                    .setForegroundColor(Color.parseColor("#FFC5C6C8")).create());
            break;
        case SendCustomTipAttachment.CUSTOME_TIP_RECEIVE_VERIFY_WOMAN:// 认证后的女方收到第一条消息（仅显示一次，切换用户不重复发送） 2
            notificationTextView.setText(SpanUtils.with(notificationTextView).append("回复消息将获得一个对方赠送的糖果，糖果可以")
                    .setForegroundColor(Color.parseColor("#FFC5C6C8")).append("提现").setClickSpan(new ClickableSpan() {
                        @Override
                        public void updateDrawState(@NonNull TextPaint ds) {
                            ds.setColor(Color.parseColor("#FF6796FA"));
                            ds.setUnderlineText(false);
                        }

                        @Override
                        public void onClick(@NonNull View widget) {
                            Intent intent = new Intent(context, MyCandyActivity.class);
                            context.startActivity(intent);
                        }
                    }).setForegroundColor(Color.parseColor("#FF6796FA")).append("哦")
                    .setForegroundColor(Color.parseColor("#FFC5C6C8")).create());
            break;
        case SendCustomTipAttachment.CUSTOME_TIP_RECEIVE_NOT_VERIFY_WOMAN:// 未认证的女方收到第一条消息（切换用户重复发送）3
            notificationTextView.setText(SpanUtils.with(notificationTextView).append("认证后每次回复能收到对方糖果，")
                    .setForegroundColor(Color.parseColor("#FFC5C6C8")).append("立即认证").setClickSpan(new ClickableSpan() {
                        @Override
                        public void updateDrawState(@NonNull TextPaint ds) {
                            ds.setColor(Color.parseColor("#FF6796FA"));
                            ds.setUnderlineText(false);
                        }

                        @Override
                        public void onClick(@NonNull View widget) {
                            CommonFunction.INSTANCE.startToFace(context, IDVerifyActivity.TYPE_ACCOUNT_NORMAL, -1);
                        }
                    }).setForegroundColor(Color.parseColor("#FF6796FA")).create());
            break;
        case SendCustomTipAttachment.CUSTOME_TIP_MAN_BIGGER_CHAT_COUNT_HAS_GIFT:// 男方 双方聊天数>设定聊天条数 女方有心愿礼物 5
        case SendCustomTipAttachment.CUSTOME_TIP_MAN_BIGGER_CHAT_COUNT_NOT_HAS_GIFT:// 男方 双方聊天数>设定聊天条数 女方没有心愿礼物 6
            notificationTextView.setText(SpanUtils.with(notificationTextView).append("你们聊的好像还不错，要不")
                    .setForegroundColor(Color.parseColor("#FFC5C6C8")).append("送个礼物").setClickSpan(new ClickableSpan() {
                        @Override
                        public void updateDrawState(@NonNull TextPaint ds) {
                            ds.setColor(Color.parseColor("#FF6796FA"));
                            ds.setUnderlineText(false);
                        }

                        @Override
                        public void onClick(@NonNull View widget) {
                            new ChatSendGiftDialog(
                                    UserInfoHelper.getUserTitleName(message.getFromAccount(), SessionTypeEnum.P2P),
                                    UserInfoHelper.getAvatar(message.getFromAccount()), message.getFromAccount(),
                                    context).show();
                        }
                    }).setForegroundColor(Color.parseColor("#FF6796FA")).append("给她吧？")
                    .setForegroundColor(Color.parseColor("#FFC5C6C8")).create());
            break;
        case SendCustomTipAttachment.CUSTOME_TIP_RECEIVED_GIFT:// 已领取对方赠送的糖果礼物，可直接用于兑换商品和提现 7
            notificationTextView.setText(SpanUtils.with(notificationTextView).append("已领取对方赠送的糖果礼物，可直接用于")
                    .setForegroundColor(Color.parseColor("#FFC5C6C8")).append("提现").setClickSpan(new ClickableSpan() {
                        @Override
                        public void updateDrawState(@NonNull TextPaint ds) {
                            ds.setColor(Color.parseColor("#FF6796FA"));
                            ds.setUnderlineText(false);
                        }

                        @Override
                        public void onClick(@NonNull View widget) {
                            Intent intent = new Intent(context, MyCandyActivity.class);
                            context.startActivity(intent);
                        }
                    }).setForegroundColor(Color.parseColor("#FF6796FA")).create());
            break;
        case SendCustomTipAttachment.CUSTOME_TIP_EXCHANGE_PRODUCT:// 已满足兑换所需糖果，立即兑换 8
            break;
        case SendCustomTipAttachment.CUSTOME_TIP_EXCHANGE_FOR_ASSISTANT:// 小助手发聊天糖果退回警告 9
            StringTokenizer tokenizer = new StringTokenizer(attachment.getContent(), "立即认证");
            try {
                notificationTextView.setText(SpanUtils.with(notificationTextView).append(tokenizer.nextToken())
                        .setForegroundColor(Color.parseColor("#FFC5C6C8")).append("立即认证")
                        .setClickSpan(new ClickableSpan() {
                            @Override
                            public void updateDrawState(@NonNull TextPaint ds) {
                                ds.setColor(Color.parseColor("#FF6796FA"));
                                ds.setUnderlineText(false);
                            }

                            @Override
                            public void onClick(@NonNull View widget) {
                                CommonFunction.INSTANCE.startToFace(context, IDVerifyActivity.TYPE_ACCOUNT_NORMAL, -1);
                            }
                        }).setForegroundColor(Color.parseColor("#FF6796FA")).append(tokenizer.nextToken())
                        .setForegroundColor(Color.parseColor("#FFC5C6C8")).create());
            } catch (Exception e) {
                notificationTextView.setText(attachment.getContent());
                e.printStackTrace();
            }
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
