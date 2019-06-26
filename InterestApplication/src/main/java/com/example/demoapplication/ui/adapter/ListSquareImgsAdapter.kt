package com.example.demoapplication.ui.adapter

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.example.baselibrary.glide.GlideUtil
import com.example.demoapplication.R
import kotlinx.android.synthetic.main.item_match_detail_img.view.*

/**
 *    author : ZFM
 *    date   : 2019/6/2418:04
 *    desc   : 加载用户多张图片的adapter
 *    version: 1.0
 */
class ListSquareImgsAdapter(var context: Context, private var datas: MutableList<Int>) :
    RecyclerView.Adapter<ListSquareImgsAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_match_detail_img, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var layoutParams = holder.itemView.ivUser.layoutParams
        if (datas.size == 1) {
            layoutParams.width = ScreenUtils.getScreenWidth()
            layoutParams.height = SizeUtils.applyDimension(252F, TypedValue.COMPLEX_UNIT_DIP).toInt()
            holder.itemView.ivUser.layoutParams = layoutParams
        } else {
            layoutParams.width = SizeUtils.applyDimension(270F, TypedValue.COMPLEX_UNIT_DIP).toInt()
            layoutParams.height = SizeUtils.applyDimension(252F, TypedValue.COMPLEX_UNIT_DIP).toInt()
            holder.itemView.ivUser.layoutParams = layoutParams
        }

        GlideUtil.loadImg(context, datas[position], holder.itemView.ivUser)
    }


    override fun getItemCount(): Int {
        return datas.size
    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
}