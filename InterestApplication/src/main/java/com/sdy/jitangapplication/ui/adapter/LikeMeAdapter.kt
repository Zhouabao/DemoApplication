package com.sdy.jitangapplication.ui.adapter

import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.LikeMeBean
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
        itemView.likeMeNew.isVisible = item.hasread ?: false

        itemView.likeOneDayRv.layoutManager = LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
        val adapter = LikeMeOneDayAdapter()
        itemView.likeOneDayRv.adapter = adapter
        adapter.setNewData(item.list ?: mutableListOf())

    }
}