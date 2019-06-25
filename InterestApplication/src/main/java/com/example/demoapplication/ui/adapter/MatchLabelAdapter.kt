package com.example.demoapplication.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.demoapplication.R
import com.example.demoapplication.model.Label
import kotlinx.android.synthetic.main.item_label.view.*

/**
 *    author : ZFM
 *    date   : 2019/6/249:54
 *    desc   :匹配页面标签的adapter  标签点击更改状态并且要实时更新用户
 *    version: 1.0
 */
class MatchLabelAdapter(var context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        val TYPE_INDEX = 0 //首个添加
        val TYPE_CONTENT = 1 //标签内容
    }

    //ItemClick事件
    var mItemClickListener: OnItemClickListener? = null

    /*
       ItemClick事件声明
    */
    interface OnItemClickListener {
        fun onItemClick(item: View, position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mItemClickListener = listener
    }

    //数据集合
    var dataList: MutableList<Label> = mutableListOf()

    /*
        设置数据
        Presenter处理过为null的情况，所以为不会为Null
     */
    fun setData(sources: MutableList<Label>) {
        dataList = sources
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return dataList.size + 1
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            TYPE_INDEX -> {
                val view = LayoutInflater.from(context).inflate(R.layout.item_label_index, parent, false)
                return IndexViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(context).inflate(R.layout.item_label_match, parent, false)
                return ViewHolder(view)
            }
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder.itemView.setOnClickListener {
            mItemClickListener?.onItemClick(holder.itemView, position)
        }
        if (holder is ViewHolder) {
            holder.itemView.labelTv.text = dataList[position - 1].name
            holder.itemView.labelTv.isChecked = dataList[position - 1].checked
        }

    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> TYPE_INDEX
            else -> TYPE_CONTENT
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)


    class IndexViewHolder(view: View) : RecyclerView.ViewHolder(view)


}