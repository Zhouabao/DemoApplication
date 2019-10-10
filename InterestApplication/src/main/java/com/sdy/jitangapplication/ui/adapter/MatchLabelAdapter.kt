package com.sdy.jitangapplication.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.LabelBean
import kotlinx.android.synthetic.main.item_label_match.view.*

/**
 *    author : ZFM
 *    date   : 2019/6/249:54
 *    desc   :匹配页面标签的adapter  标签点击更改状态并且要实时更新用户
 *    version: 1.0
 */
class MatchLabelAdapter(var context: Context, var enable: Boolean = true) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
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
    var dataList: MutableList<LabelBean> = mutableListOf()

    /*
        设置数据
        Presenter处理过为null的情况，所以为不会为Null
     */
    fun setData(sources: MutableList<LabelBean>) {
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
        if (holder is ViewHolder) {
            holder.itemView.labelTv.text = dataList[position - 1].title
            if (dataList[position - 1].checked) {
                holder.itemView.labelTv.setTextColor(context.resources.getColor(R.color.colorWhite))
                holder.itemView.labelTv.setBackgroundResource(R.drawable.cb_label_checked_orange)
            } else {
                holder.itemView.labelTv.setTextColor(context.resources.getColor(R.color.colorBlackTitle))
                holder.itemView.labelTv.setBackgroundResource(R.drawable.cb_label_unchecked)
            }

//            holder.itemView.labelTv.isChecked = dataList[position - 1].checked
            holder.itemView.setOnClickListener {
                //                if (enable)
                mItemClickListener?.onItemClick(holder.itemView, position)
            }
        } else {
            holder.itemView.setOnClickListener {
                mItemClickListener?.onItemClick(holder.itemView, position)
            }
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