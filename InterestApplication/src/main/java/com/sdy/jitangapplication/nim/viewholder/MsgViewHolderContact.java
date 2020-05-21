package com.sdy.jitangapplication.nim.viewholder;

import android.graphics.Color;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.SpanUtils;
import com.netease.nim.uikit.business.session.viewholder.MsgViewHolderBase;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;
import com.netease.nim.uikit.common.util.sys.ClipboardUtil;
import com.sdy.jitangapplication.R;
import com.sdy.jitangapplication.common.CommonFunction;
import com.sdy.jitangapplication.nim.attachment.ContactAttachment;

/**
 * author : ZFM
 * date   : 2019/8/1015:35
 * desc   : 心愿助力
 * version: 1.0
 */
public class MsgViewHolderContact extends MsgViewHolderBase {

    private ContactAttachment attachment;
    private TextView contactContent;
    private ImageView contactImg;


    public MsgViewHolderContact(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

    @Override
    protected int getContentResId() {
        return R.layout.item_chat_contact;
    }

    @Override
    protected void inflateContentView() {
        //初始化数据
        contactContent = view.findViewById(R.id.contactContent);
        contactImg = view.findViewById(R.id.contactImg);
    }


    //	0没有留下联系方式 1 电话 2 微信 3 qq 99隐藏
    @Override
    protected void bindContentView() {
        attachment = (ContactAttachment) message.getAttachment();
        if (attachment.getContactWay() == 1) {
            contactImg.setImageResource(R.drawable.icon_phone_circle);
        } else if (attachment.getContactWay() == 2) {
            contactImg.setImageResource(R.drawable.icon_wechat_circle);
        } else if (attachment.getContactWay() == 3) {
            contactImg.setImageResource(R.drawable.icon_qq_circle);
        }

        SpanUtils.with(contactContent)
                .append(attachment.getContactContent())
                .setClickSpan(new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        ClipboardUtil.clipboardCopyText(context, attachment.getContactContent());
                        CommonFunction.INSTANCE.toast("已复制联系方式");
                    }
                })
                .setForegroundColor(Color.parseColor("#FF6318"))
                .create();
//        contactContent.setText(attachment.getContactContent());
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
