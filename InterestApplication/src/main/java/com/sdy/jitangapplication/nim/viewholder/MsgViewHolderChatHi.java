package com.sdy.jitangapplication.nim.viewholder;

import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.sdy.jitangapplication.R;
import com.sdy.jitangapplication.nim.attachment.ChatHiAttachment;
import com.sdy.jitangapplication.utils.UserManager;
import com.netease.nim.uikit.business.session.viewholder.MsgViewHolderBase;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;

/**
 * author : ZFM
 * date   : 2019/8/1015:35
 * desc   :
 * version: 1.0
 */
public class MsgViewHolderChatHi extends MsgViewHolderBase {

    private TextView chatHiMatch,//匹配的兴趣
            targetMatchContent;
    private ImageView targetMatchIv;
    private ConstraintLayout targetCl;

    public MsgViewHolderChatHi(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

    @Override
    protected int getContentResId() {
        return R.layout.item_chat_hi;
    }

    @Override
    protected void inflateContentView() {
        //初始化数据
        chatHiMatch = findViewById(R.id.targetMatchWay);
        targetMatchContent = findViewById(R.id.targetMatchContent);
        targetMatchIv = findViewById(R.id.targetMatchIv);
        targetCl = findViewById(R.id.targetCl);
        setLayoutParams(ScreenUtil.screenWidth, FrameLayout.LayoutParams.WRAP_CONTENT, targetCl);
        setGravity(targetCl, Gravity.CENTER);
    }

    @Override
    protected void bindContentView() {
        ChatHiAttachment attachment = (ChatHiAttachment) message.getAttachment();
        //匹配的兴趣 1.匹配  2.招呼 3.好友
        if (attachment.getShowType() == ChatHiAttachment.CHATHI_MATCH) {
//            chatHiMatch.setText("通过『" + attachment.getTag() + "』匹配");
            chatHiMatch.setText("匹配成功");
            targetMatchContent.setText("找到一样的人了就和他聊聊天吧");
            targetMatchIv.setImageResource(R.drawable.icon_like);
        } else if (attachment.getShowType() == ChatHiAttachment.CHATHI_HI) {
            targetMatchIv.setImageResource(R.drawable.icon_flash_small);
            if (message.getFromAccount().equals(UserManager.INSTANCE.getAccid())) {
                chatHiMatch.setText("你向对方打了个招呼");
                targetMatchContent.setText("好好表现争取赢个好友位吧");
            } else {
                chatHiMatch.setText("对方向你打了个招呼");
                targetMatchContent.setText("和他接触下吧说不定不错呢");
            }
        } else if (attachment.getShowType() == ChatHiAttachment.CHATHI_RFIEND) {
            targetMatchIv.setImageResource(R.drawable.icon_like);
            if (message.getFromAccount().equals(UserManager.INSTANCE.getAccid())) {
                chatHiMatch.setText("你已添加对方为好友");
            } else {
                chatHiMatch.setText("对方已添加你为好友");
            }
            targetMatchContent.setText("快多和他聊聊天吧");
        } else if (attachment.getShowType() == ChatHiAttachment.CHATHI_OUTTIME) {
            targetMatchIv.setImageResource(R.drawable.icon_chat_hit_outtime);
            chatHiMatch.setText("消息已过期");
            targetMatchContent.setText("Emmmm有缘下次聊吧");
        } else if (attachment.getShowType() == ChatHiAttachment.CHATHI_WISH_HELP) {
            targetMatchIv.setImageResource(R.drawable.icon_like);
            chatHiMatch.setText("匹配成功");
            targetMatchContent.setText("你们已通过心愿助力成为好友");
        } else if (attachment.getShowType() == ChatHiAttachment.CHATHI_WANT_MATCH) {
            targetMatchIv.setImageResource(R.drawable.icon_match_want);
            chatHiMatch.setText("意向匹配");
            targetMatchContent.setText("聊聊你们想做的事吧");
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
