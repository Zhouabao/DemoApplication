package com.example.demoapplication.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.baselibrary.glide.GlideUtil
import com.example.demoapplication.R
import com.kotlin.base.ui.adapter.BaseRecyclerViewAdapter
import kotlinx.android.synthetic.main.item_block_square.view.*

/**
 *    author : ZFM
 *    date   : 2019/6/2615:22
 *    desc   :九宫格形式的广场内容适配器
 *    version: 1.0
 */
class BlockAdapter(context: Context) : BaseRecyclerViewAdapter<Int, BlockAdapter.ViewHolder>(context) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_block_square, parent, false))
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        GlideUtil.loadRoundImg(mContext, dataList[position], holder.itemView.ivSquare, 60)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
}