package com.sdy.jitangapplication.nim.viewholder;

import android.widget.TextView;

import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum;
import com.sdy.jitangapplication.R;
import com.sdy.jitangapplication.nim.attachment.ContactCandyAttachment;
import com.sdy.jitangapplication.nim.uikit.business.session.viewholder.MsgViewHolderBase;
import com.sdy.jitangapplication.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;
import com.sdy.jitangapplication.ui.dialog.ContactCandyReceiveDialog;

/**
 * author : ZFM
 * date   : 2019/8/1015:35
 * desc   :
 * version: 1.0
 */
public class MsgViewHolderContactCandy extends MsgViewHolderBase {

    private TextView giftAmount; // 礼物的名字
    private ContactCandyAttachment attachment;

    public MsgViewHolderContactCandy(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

    @Override
    protected int getContentResId() {
        return R.layout.item_contact_candy;
    }

    @Override
    protected void inflateContentView() {
        // 初始化数据
        giftAmount = findViewById(R.id.giftAmount);
    }

    @Override
    protected void bindContentView() {
        attachment = (ContactCandyAttachment) message.getAttachment();
        giftAmount.setText(attachment.getContactCandy() + "糖果");
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
        if (message.getDirect() == MsgDirectionEnum.In)
            new ContactCandyReceiveDialog(attachment.getContactCandy(), context).show();
    }
}
