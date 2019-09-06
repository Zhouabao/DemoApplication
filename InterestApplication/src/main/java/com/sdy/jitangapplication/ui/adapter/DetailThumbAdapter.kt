package com.sdy.jitangapplication.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.Square
import com.kotlin.base.ui.adapter.BaseRecyclerViewAdapter
import kotlinx.android.synthetic.main.item_match_detail_thumb.view.*

/**
 *    author : ZFM
 *    date   : 2019/6/2611:22
 *    desc   :广场动态封面adapter
 *    version: 1.0
 */
class DetailThumbAdapter(context: Context) : BaseRecyclerViewAdapter<Square, DetailThumbAdapter.ViewHolder>(context) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_match_detail_thumb, parent, false))
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val item: Square = dataList[position]
//        GlideUtil.loadImg(mContext, if (item.photo_json.isNullOrEmpty()) { item.video_json } else { item.photo_json }, holder.itemView.ivThumb)
        GlideUtil.loadRoundImgCenterCrop(mContext, item.cover_url, holder.itemView.ivThumb, SizeUtils.dp2px(5F))

    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
}