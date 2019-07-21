package com.example.demoapplication.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.demoapplication.R
import com.example.demoapplication.model.Tag
import com.kotlin.base.ui.adapter.BaseRecyclerViewAdapter
import kotlinx.android.synthetic.main.item_match_detail_label.view.*


/**
 *    author : ZFM
 *    date   : 2019/6/2020:48
 *    desc   :
 *    version: 1.0
 */
class MatchDetailLabelAdapter(context: Context) :
    BaseRecyclerViewAdapter<Tag, MatchDetailLabelAdapter.ViewHolder>(context) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_match_detail_label, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val model = dataList[position]
        holder.itemView.isEnabled = false
        holder.itemView.labelName.text = model.title
        holder.itemView.labelName.isChecked = model.sameLabel ?: false

    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)


}