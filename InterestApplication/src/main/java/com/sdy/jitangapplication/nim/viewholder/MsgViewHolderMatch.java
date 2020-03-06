package com.sdy.jitangapplication.nim.viewholder;

import android.view.Gravity;
import android.widget.FrameLayout;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.flexbox.*;
import com.netease.nim.uikit.business.session.viewholder.MsgViewHolderBase;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;
import com.sdy.baselibrary.glide.GlideUtil;
import com.sdy.jitangapplication.R;
import com.sdy.jitangapplication.nim.attachment.ChatMatchAttachment;
import com.sdy.jitangapplication.ui.adapter.MatchDetailLabelAdapter;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * author : ZFM
 * date   : 2019/8/1015:35
 * desc   :
 * version: 1.0
 */
public class MsgViewHolderMatch extends MsgViewHolderBase {

    private CircleImageView chatHiAvator;//头像
    private RecyclerView chatHiTags;//对方所有的兴趣
    private ConstraintLayout targetCl;//对方所有的兴趣

    public MsgViewHolderMatch(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

    @Override
    protected int getContentResId() {
        return R.layout.item_chat_head;
    }

    @Override
    protected void inflateContentView() {
        //初始化数据
        chatHiAvator = findViewById(R.id.targetAvator);
        chatHiTags = findViewById(R.id.targetSquare);
        targetCl = findViewById(R.id.targetCl);
        setLayoutParams(ScreenUtil.screenWidth, FrameLayout.LayoutParams.WRAP_CONTENT, targetCl);
        setGravity(targetCl, Gravity.CENTER);
    }

    @Override
    protected void bindContentView() {
        ChatMatchAttachment attachment = (ChatMatchAttachment) message.getAttachment();
        //头像
        GlideUtil.loadAvatorImg(context, attachment.getAvator(), chatHiAvator);
        //用户兴趣
        FlexboxLayoutManager manager = new FlexboxLayoutManager(context, FlexDirection.ROW, FlexWrap.WRAP);
        manager.setAlignItems(AlignItems.STRETCH);
        manager.setJustifyContent(JustifyContent.CENTER);
        chatHiTags.setLayoutManager(manager);
        MatchDetailLabelAdapter adapter = new MatchDetailLabelAdapter(context);
        chatHiTags.setAdapter(adapter);
        if (attachment.getTags() != null && attachment.getTags().size() > 0) {
            adapter.setData(attachment.getTags());
        }
    }

    @Override
    protected int leftBackground() {
        return R.color.transparent;
    }

    @Override
    protected int rightBackground() {
        return R.color.transparent;
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
    protected boolean isShowHeadImage() {

        return false;
    }

    @Override
    protected boolean isShowBubble() {

        return false;
    }
}
