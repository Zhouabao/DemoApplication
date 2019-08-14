package com.example.demoapplication.ui.adapter

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.demoapplication.R
import com.example.demoapplication.model.LikeMeBean
import kotlinx.android.synthetic.main.item_like_me.view.*

/**
 *    author : ZFM
 *    date   : 2019/8/516:02
 *    desc   : 喜欢我的
 *    version: 1.0
 */
class LikeMeAdapter : BaseQuickAdapter<LikeMeBean, BaseViewHolder>(R.layout.item_like_me) {
    override fun convert(holder: BaseViewHolder, item: LikeMeBean) {
        holder.addOnClickListener(R.id.likeMeCount)
        val itemView = holder.itemView
        itemView.likeMeDate.text = item.date ?: ""
        itemView.likeMeCount.text = "${item.count} 人对你感兴趣"
        itemView.likeMeNew.visibility = if (item.count == null || item.count == 0) {
            View.GONE
        } else {
            View.VISIBLE
        }

        itemView.likeOneDayRv.layoutManager = LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
        val adapter = LikeMeOneDayAdapter()
        itemView.likeOneDayRv.adapter = adapter
        adapter.setNewData(item.list ?: mutableListOf())

    }
}