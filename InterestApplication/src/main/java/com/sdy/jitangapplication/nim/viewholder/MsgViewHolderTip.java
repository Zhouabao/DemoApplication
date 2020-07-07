package com.sdy.jitangapplication.nim.viewholder;

import android.content.Intent;
import android.graphics.Color;
import android.text.Html;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.SpanUtils;
import com.netease.nim.uikit.business.session.viewholder.MsgViewHolderBase;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;
import com.sdy.jitangapplication.common.CommonFunction;
import com.sdy.jitangapplication.ui.activity.IDVerifyActivity;
import com.sdy.jitangapplication.ui.activity.MatchDetailActivity;
import com.sdy.jitangapplication.ui.activity.MyCandyActivity;

import java.util.Map;

/**
 * Created by huangjun on 2015/11/25.
 * Tip类型消息ViewHolder
 */
public class MsgViewHolderTip extends MsgViewHolderBase {

    protected TextView notificationTextView;

    public MsgViewHolderTip(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

    @Override
    protected int getContentResId() {
        return com.netease.nim.uikit.R.layout.nim_message_item_notification;
    }

    @Override
    protected void inflateContentView() {
        notificationTextView = (TextView) view.findViewById(com.netease.nim.uikit.R.id.message_item_notification_label);
    }

    @Override
    protected void bindContentView() {
        String text = "未知通知提醒";
        Map<String, Object> content = message.getRemoteExtension();
//        Map<String, Object> content = message.getLocalExtension();
        if (content != null && !content.isEmpty()) {
            text = (String) content.get("content");
            notificationTextView.setText(SpanUtils.with(notificationTextView)
                    .append((String) content.get("head"))
                    .setForegroundColor(Color.parseColor("#FFC5C6C8"))
                    .append((String) content.get("footer"))
                    .setClickSpan(new ClickableSpan() {
                        @Override
                        public void updateDrawState(@NonNull TextPaint ds) {
                            ds.setColor(Color.parseColor("#FF6796FA"));
                            ds.setUnderlineText(false);
                        }

                        @Override
                        public void onClick(@NonNull View widget) {
                            if (content.get("type") != null) {
                                int type = (int) content.get("type");
                                if (type == 1) {
                                    Intent intent = new Intent(context, MyCandyActivity.class);
                                    context.startActivity(intent);
                                } else if (type == 2) {
                                    CommonFunction.INSTANCE.startToFace(context, IDVerifyActivity.TYPE_ACCOUNT_NORMAL,-1);
                                }  else if (type == 4) {
                                    MatchDetailActivity.start(context, message.getFromAccount(), -1, -1);
                                }
                            }
                        }
                    })
                    .setForegroundColor(Color.parseColor("#FF6796FA"))
                    .create());
        } else {
            text = message.getContent();
            notificationTextView.setText(Html.fromHtml(text));
        }

//        handleTextNotification(text);
    }

    private void handleTextNotification(String text) {
        notificationTextView.setText(Html.fromHtml(text));
//        MoonUtil.identifyFaceExpressionAndATags(context, notificationTextView, text, ImageSpan.ALIGN_BOTTOM);
//        notificationTextView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    protected boolean shouldDisplayReceipt() {
        return false;
    }

    @Override
    protected boolean isMiddleItem() {
        return true;
    }


}
