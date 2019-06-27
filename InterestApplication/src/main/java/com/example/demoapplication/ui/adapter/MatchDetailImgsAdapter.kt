package com.example.demoapplication.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.baselibrary.glide.GlideUtil
import com.example.demoapplication.R
import kotlinx.android.synthetic.main.item_match_detail_img.view.*

/**
 *    author : ZFM
 *    date   : 2019/6/2418:04
 *    desc   : Y用户详情加载用户多张图片的adapter
 *    version: 1.0
 */
class MatchDetailImgsAdapter(var context: Context, private var datas: MutableList<Int>) :
    RecyclerView.Adapter<MatchDetailImgsAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(context).inflate(R.layout.item_match_detail_img, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        GlideUtil.loadImg(context, datas[position], holder.itemView.ivUser)
    }


    override fun getItemCount(): Int {
        return datas.size
    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
}