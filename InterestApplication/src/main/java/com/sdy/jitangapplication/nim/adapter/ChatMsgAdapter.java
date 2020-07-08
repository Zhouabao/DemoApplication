package com.sdy.jitangapplication.nim.adapter;

import androidx.recyclerview.widget.RecyclerView;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.sdy.jitangapplication.nim.uikit.business.session.module.Container;
import com.sdy.jitangapplication.nim.uikit.business.session.module.list.MsgAdapter;

import java.util.List;

/**
 * Created by huangjun on 2016/12/21.
 */
public class ChatMsgAdapter extends MsgAdapter {
    public ChatMsgAdapter(RecyclerView recyclerView, List<IMMessage> data, Container container) {
        super(recyclerView, data, container);
//        registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
//            @Override
//            public void onChanged() {
//                Log.e("TAG","AdapterData Change....");
//            }
//        });
    }



}
