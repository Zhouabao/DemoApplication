package com.sdy.jitangapplication.nim.viewholder;

import android.content.Intent;
import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.SpanUtils;
import com.sdy.jitangapplication.R;
import com.sdy.jitangapplication.common.CommonFunction;
import com.sdy.jitangapplication.nim.attachment.SendCustomTipAttachment;
import com.sdy.jitangapplication.nim.uikit.business.session.viewholder.MsgViewHolderBase;
import com.sdy.jitangapplication.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;
import com.sdy.jitangapplication.ui.activity.IDVerifyActivity;
import com.sdy.jitangapplication.ui.activity.SettingsActivity;
import com.sdy.jitangapplication.ui.activity.VipPowerActivity;

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
            case SendCustomTipAttachment.CUSTOME_TIP_EXCHANGE_FOR_ASSISTANT:// 小助手发聊天糖果退回警告 9
                StringTokenizer tokenizer = new StringTokenizer(attachment.getContent(), context.getString(R.string.verify_now));
                try {
                    notificationTextView.setText(SpanUtils.with(notificationTextView).append(tokenizer.nextToken())
                            .setForegroundColor(Color.parseColor("#FFC5C6C8")).append(context.getString(R.string.verify_now))
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
            case SendCustomTipAttachment.CUSTOME_TIP_PRIVICY_SETTINGS:// 消息太多？你可以设置私聊权限仅黄金会员过滤消息
                notificationTextView.setText(SpanUtils.with(notificationTextView)
                        .append(context.getString(R.string.too_many_message_1))
                        .setForegroundColor(Color.parseColor("#FFC5C6C8"))
                        .append(context.getString(R.string.too_many_message_2))
                        .setClickSpan(new ClickableSpan() {
                            @Override
                            public void updateDrawState(@NonNull TextPaint ds) {
                                ds.setColor(Color.parseColor("#FF6796FA"));
                                ds.setUnderlineText(false);
                            }

                            @Override
                            public void onClick(@NonNull View widget) {
                                Intent intent = new Intent(context, SettingsActivity.class);
                                context.startActivity(intent);
                            }
                        })
                        .setForegroundColor(Color.parseColor("#FF6796FA"))
                        .append(context.getString(R.string.too_many_message_3))
                        .setForegroundColor(Color.parseColor("#FFC5C6C8"))
                        .create());
                break;
            case SendCustomTipAttachment.CUSTOME_TIP_CHARGE_PT_VIP:// 免费消息会被归于对方搭讪列表，可能回复率偏低， 充值黄金会员可提升消息回复
                notificationTextView.setText(SpanUtils.with(notificationTextView)
                        .append(context.getString(R.string.free_chat_message1))
                        .setForegroundColor(Color.parseColor("#FFC5C6C8"))
                        .append(context.getString(R.string.free_chat_message2))
                        .setClickSpan(new ClickableSpan() {
                            @Override
                            public void updateDrawState(@NonNull TextPaint ds) {
                                ds.setColor(Color.parseColor("#FF6796FA"));
                                ds.setUnderlineText(false);
                            }

                            @Override
                            public void onClick(@NonNull View widget) {
                                CommonFunction.INSTANCE.startToVip(context, VipPowerActivity.SOURCE_FREE_CHAT, 0);
                            }
                        })
                        .setForegroundColor(Color.parseColor("#FF6796FA"))
                        .append(context.getString(R.string.free_chat_message3))
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
