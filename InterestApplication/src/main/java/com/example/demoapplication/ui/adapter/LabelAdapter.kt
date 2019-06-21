package com.example.demoapplication.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.demoapplication.R
import com.example.demoapplication.model.Label
import com.kotlin.base.ui.adapter.BaseRecyclerViewAdapter
import kotlinx.android.synthetic.main.item_label.view.*

/**
 *    author : ZFM
 *    date   : 2019/6/2020:48
 *    desc   : 此处存在二级标签和三级标签的选择和反选
 *    version: 1.0
 */
class LabelAdapter(context: Context) : BaseRecyclerViewAdapter<Label, LabelAdapter.ViewHolder>(context) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext)
            .inflate(R.layout.item_label, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val model = dataList[position]

        holder.itemView.labelTv.text = model.name
        holder.itemView.labelTv.isChecked = model.checked
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)


}