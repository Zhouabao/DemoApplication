package com.sdy.jitangapplication.nim.viewholder;

import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.sdy.jitangapplication.R;
import com.sdy.jitangapplication.nim.attachment.ChatHiAttachment;
import com.sdy.jitangapplication.nim.uikit.business.session.viewholder.MsgViewHolderBase;
import com.sdy.jitangapplication.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;
import com.sdy.jitangapplication.nim.uikit.common.util.sys.ScreenUtil;
import com.sdy.jitangapplication.utils.UserManager;

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
            chatHiMatch.setText(R.string.match_succeed);
            targetMatchContent.setText(R.string.find_same_talk_him);
            targetMatchIv.setImageResource(R.drawable.icon_like);
        } else if (attachment.getShowType() == ChatHiAttachment.CHATHI_HI) {
            targetMatchIv.setImageResource(R.drawable.icon_flash_small);
            if (message.getFromAccount().equals(UserManager.INSTANCE.getAccid())) {
                chatHiMatch.setText(R.string.you_say_hi);
                targetMatchContent.setText(R.string.try_best_to_be_friend);
            } else {
                chatHiMatch.setText(R.string.he_say_hi);
                targetMatchContent.setText(R.string.hi_might_be_ok);
            }
        } else if (attachment.getShowType() == ChatHiAttachment.CHATHI_RFIEND) {
            targetMatchIv.setImageResource(R.drawable.icon_like);
            if (message.getFromAccount().equals(UserManager.INSTANCE.getAccid())) {
                chatHiMatch.setText(R.string.you_add_him_friend);
            } else {
                chatHiMatch.setText(R.string.he_add_you_friend);
            }
            targetMatchContent.setText(R.string.talk_him_more);
        } else if (attachment.getShowType() == ChatHiAttachment.CHATHI_OUTTIME) {
            targetMatchIv.setImageResource(R.drawable.icon_chat_hit_outtime);
            chatHiMatch.setText(R.string.msg_time_out);
            targetMatchContent.setText(R.string.emmm_bye);
        } else if (attachment.getShowType() == ChatHiAttachment.CHATHI_WISH_HELP) {
            targetMatchIv.setImageResource(R.drawable.icon_like);
            chatHiMatch.setText(R.string.match_succeed);
            targetMatchContent.setText(R.string.you_have_been_friend_by_wish_hlpe);
        } else if (attachment.getShowType() == ChatHiAttachment.CHATHI_WANT_MATCH) {
            targetMatchIv.setImageResource(R.drawable.icon_match_want);
            chatHiMatch.setText(R.string.intention_match);
            targetMatchContent.setText(R.string.talk_what_you_want_to_do);
        } else if (attachment.getShowType() == ChatHiAttachment.CHATHI_CHATUP_FRIEND) {
            targetMatchIv.setImageResource(R.drawable.icon_match_chat_up);
            chatHiMatch.setText(R.string.unlocked_chat);
            targetMatchContent.setText(R.string.talk_freely);
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
