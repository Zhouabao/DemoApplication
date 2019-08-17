package com.example.demoapplication.nim.adapter;

import androidx.recyclerview.widget.RecyclerView;
import com.netease.nim.uikit.business.session.module.Container;
import com.netease.nim.uikit.business.session.module.list.MsgAdapter;
import com.netease.nimlib.sdk.msg.model.IMMessage;

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
