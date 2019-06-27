package com.example.demoapplication.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.baselibrary.glide.GlideUtil
import com.example.demoapplication.R
import com.kotlin.base.ui.adapter.BaseRecyclerViewAdapter
import kotlinx.android.synthetic.main.item_match_detail_thumb.view.*

/**
 *    author : ZFM
 *    date   : 2019/6/2611:22
 *    desc   :广场动态封面adapter
 *    version: 1.0
 */
class DetailThumbAdapter(context: Context) : BaseRecyclerViewAdapter<Int, DetailThumbAdapter.ViewHolder>(context) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_match_detail_thumb, parent, false))
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        GlideUtil.loadImg(mContext, dataList[position], holder.itemView.ivThumb)
    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
}