package com.sdy.jitangapplication.nim.uikit.business.session.viewholder;

import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.sdy.jitangapplication.R;
import com.sdy.jitangapplication.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;

/**
 * Created by zhoujianghua on 2015/8/6.
 */
public class MsgViewHolderUnknown extends MsgViewHolderBase {

    public MsgViewHolderUnknown(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

    @Override
    protected int getContentResId() {
        return R.layout.nim_message_item_unknown;
    }

    @Override
    protected boolean isShowHeadImage() {
        if (message.getSessionType() == SessionTypeEnum.ChatRoom) {
            return false;
        }
        return true;
    }

    @Override
    protected void inflateContentView() {
    }

    @Override
    protected void bindContentView() {
    }
}
