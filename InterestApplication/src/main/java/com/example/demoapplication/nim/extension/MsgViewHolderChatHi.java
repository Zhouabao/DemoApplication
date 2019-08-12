package com.example.demoapplication.nim.extension;

import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import com.example.baselibrary.glide.GlideUtil;
import com.example.demoapplication.R;
import com.example.demoapplication.ui.adapter.MatchDetailLabelAdapter;
import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.netease.nim.uikit.business.session.viewholder.MsgViewHolderBase;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * author : ZFM
 * date   : 2019/8/1015:35
 * desc   :
 * version: 1.0
 */
public class MsgViewHolderChatHi extends MsgViewHolderBase {

    private TextView chatHiMatch; //匹配的标签
    private CircleImageView chatHiAvator;//头像
    private RecyclerView chatHiTags;//对方所有的标签
    private ConstraintLayout targetCl;//对方所有的标签

    public MsgViewHolderChatHi(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

    @Override
    protected int getContentResId() {
        return R.layout.item_chat_head;
    }

    @Override
    protected void inflateContentView() {
        //初始化数据
        chatHiMatch = findViewById(R.id.targetMatchWay);
        chatHiAvator = findViewById(R.id.targetAvator);
        chatHiTags = findViewById(R.id.targetLabels);
        targetCl = findViewById(R.id.targetCl);
        setLayoutParams(ScreenUtil.screenWidth, FrameLayout.LayoutParams.WRAP_CONTENT, targetCl);
        setGravity(targetCl, Gravity.CENTER);
    }

    @Override
    protected void bindContentView() {
        ChatHiAttachment attachment = (ChatHiAttachment) message.getAttachment();
        //匹配的标签
        chatHiMatch.setText("通过「" + attachment.getMatchTag() + "」匹配");
        //头像
        GlideUtil.loadAvatorImg(context, attachment.getAvator(), chatHiAvator);
        //用户标签
        FlexboxLayoutManager manager = new FlexboxLayoutManager(context, FlexDirection.ROW, FlexWrap.WRAP);
        manager.setAlignItems(AlignItems.STRETCH);
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
