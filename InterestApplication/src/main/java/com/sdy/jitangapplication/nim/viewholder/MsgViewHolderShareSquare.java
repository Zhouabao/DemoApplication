package com.sdy.jitangapplication.nim.viewholder;

import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.blankj.utilcode.util.SizeUtils;
import com.netease.nim.uikit.business.session.viewholder.MsgViewHolderBase;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;
import com.sdy.baselibrary.glide.GlideUtil;
import com.sdy.jitangapplication.R;
import com.sdy.jitangapplication.model.SquareBean;
import com.sdy.jitangapplication.nim.attachment.ShareSquareAttachment;
import com.sdy.jitangapplication.ui.activity.SquarePlayListDetailActivity;

/**
 * author : ZFM
 * date   : 2019/8/1015:35
 * desc   :
 * version: 1.0
 */
public class MsgViewHolderShareSquare extends MsgViewHolderBase {

    private TextView shareDesc; //分享描述文本
    private TextView shareContent; //分享文字内容
    private ImageView shareImg;//分享的图片
    private ImageView shareType;//分享的类型
    private ShareSquareAttachment attachment;

    public MsgViewHolderShareSquare(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

    @Override
    protected int getContentResId() {
        return R.layout.item_share_square;
    }

    @Override
    protected void inflateContentView() {
        //初始化数据
        shareDesc = findViewById(R.id.shareDesc);
        shareContent = findViewById(R.id.shareContent);
        shareImg = findViewById(R.id.shareImg);
        shareType = findViewById(R.id.shareType);
    }

    @Override
    protected void bindContentView() {
        attachment = (ShareSquareAttachment) message.getAttachment();
        if (attachment.getContent() != null && !attachment.getContent().isEmpty()) {
            shareDesc.setVisibility(View.VISIBLE);
            shareDesc.setText(attachment.getContent());
        } else {
            shareDesc.setVisibility(View.GONE);
        }
        shareContent.setText(attachment.getDesc());
        GlideUtil.loadRoundImgCenterCrop(context, attachment.getImg(), shareImg, SizeUtils.dp2px(5F));
        if (attachment.getShareType() == SquareBean.VIDEO) {
            shareType.setVisibility(View.VISIBLE);
        } else {
            shareType.setVisibility(View.GONE);
        }
        if (isReceivedMessage()) {
            shareDesc.setTextColor(Color.BLACK);
        } else {
            shareDesc.setTextColor(Color.WHITE);
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
        return true;
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
        SquarePlayListDetailActivity.Companion.start(context, attachment.getId());
    }
}
