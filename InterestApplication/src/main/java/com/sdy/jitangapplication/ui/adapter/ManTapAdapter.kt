package com.sdy.jitangapplication.ui.adapter

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.event.UpdateManTapBtnEvent
import com.sdy.jitangapplication.model.MyTapsBean
import kotlinx.android.synthetic.main.item_man_tap.view.*
import org.greenrobot.eventbus.EventBus

/**
 *    author : ZFM
 *    date   : 2020/5/714:45
 *    desc   :
 *    version: 1.0
 */
class ManTapAdapter : BaseQuickAdapter<MyTapsBean, BaseViewHolder>(R.layout.item_man_tap) {
    override fun convert(helper: BaseViewHolder, item: MyTapsBean) {
        helper.itemView.manTapTitle.text = item.title
        helper.itemView.manTapRv.layoutManager =
            LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
        val adapter = ManTapDetailAdapter()
        helper.itemView.manTapRv.adapter = adapter
        adapter.setNewData(item.child)

        adapter.setOnItemClickListener { adapter, view, position ->
            for (tData in data[helper.adapterPosition].child) {
                tData.checked = tData == data[helper.adapterPosition].child[position]
            }
            adapter.notifyDataSetChanged()
            notifyDataSetChanged()
            EventBus.getDefault().post(UpdateManTapBtnEvent())
        }
    }
}