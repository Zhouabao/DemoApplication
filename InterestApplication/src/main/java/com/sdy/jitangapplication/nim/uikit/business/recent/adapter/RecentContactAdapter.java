package com.sdy.jitangapplication.nim.uikit.business.recent.adapter;


import androidx.recyclerview.widget.RecyclerView;

import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.RecentContact;
import com.sdy.jitangapplication.R;
import com.sdy.jitangapplication.nim.uikit.business.recent.RecentContactsCallback;
import com.sdy.jitangapplication.nim.uikit.business.recent.holder.CommonRecentViewHolder;
import com.sdy.jitangapplication.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemQuickAdapter;
import com.sdy.jitangapplication.nim.uikit.common.ui.recyclerview.holder.BaseViewHolder;

import java.util.List;

/**
 * Created by huangjun on 2016/12/11.
 */

public class RecentContactAdapter extends BaseMultiItemQuickAdapter<RecentContact, BaseViewHolder> {

    interface ViewType {
        int VIEW_TYPE_COMMON = 1;
        int VIEW_TYPE_TEAM = 2;
        int VIEW_TYPE_SUPER_TEAM = 3;
    }

    private RecentContactsCallback callback;

    public RecentContactAdapter(RecyclerView recyclerView, List<RecentContact> data) {
        super(recyclerView, data);
        addItemType(ViewType.VIEW_TYPE_COMMON, R.layout.nim_recent_contact_list_item, CommonRecentViewHolder.class);
    }

    @Override
    protected int getViewType(RecentContact item) {
        return item.getSessionType() == SessionTypeEnum.Team ? ViewType.VIEW_TYPE_TEAM : ViewType.VIEW_TYPE_COMMON;
    }

    @Override
    protected String getItemKey(RecentContact item) {
        StringBuilder sb = new StringBuilder();
        sb.append(item.getSessionType().getValue()).append("_").append(item.getContactId());

        return sb.toString();
    }

    public RecentContactsCallback getCallback() {
        return callback;
    }

    public void setCallback(RecentContactsCallback callback) {
        this.callback = callback;
    }
}
