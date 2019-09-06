package com.sdy.jitangapplication.nim.view;

import androidx.annotation.LayoutRes;
import com.sdy.jitangapplication.R;
import com.sdy.jitangapplication.model.Tag;
import com.netease.nim.uikit.common.ui.recyclerview.holder.BaseViewHolder;

import java.util.List;

/**
 * author : ZFM
 * date   : 2019/8/179:33
 * desc   :
 * version: 1.0
 */
public class ChatHeadView {
    private String avator;
    private List<Tag> tags;

    public String getAvator() {
        return avator;
    }

    public void setAvator(String avator) {
        this.avator = avator;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public void convert(BaseViewHolder holder) {
    }

    /**
     * load more layout
     *
     * @return
     */
    public @LayoutRes int getLayoutId(){
        return R.layout.item_chat_head;
    }

}
