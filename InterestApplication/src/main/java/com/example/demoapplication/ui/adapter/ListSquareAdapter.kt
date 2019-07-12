package com.example.demoapplication.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ToastUtils
import com.example.demoapplication.R
import com.example.demoapplication.model.MatchBean
import kotlinx.android.synthetic.main.item_list_square_pic.view.*

/**
 *    author : ZFM
 *    date   : 2019/6/2616:27
 *    desc   : 列表的广场
 *    version: 1.0
 */
class ListSquareAdapter(var context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var data = mutableListOf<MatchBean>()
    fun setData(data: MutableList<MatchBean>) {
        if (data.isNotEmpty()) {
            this.data.addAll(data)
            notifyDataSetChanged()
        }
    }


    companion object {
        val TYPE_PIC = 1 //图片
        val TYPE_VIDEO = 2//视频
        val TYPE_AUDIO = 3//音频
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            TYPE_PIC ->
                return PicViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.item_list_square_pic,
                        parent,
                        false
                    )
                )
            TYPE_VIDEO ->
                return ViderViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.item_list_square_video,
                        parent,
                        false
                    )
                )
            else ->
                return AudioViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.item_list_square_audio,
                        parent,
                        false
                    )
                )

        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (data[position].type) {
            1 -> TYPE_PIC
            2 -> TYPE_VIDEO
            else -> TYPE_AUDIO
        }

    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is PicViewHolder -> {
                holder.itemView.squareUserPics1.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
                holder.itemView.squareUserPics1.adapter = ListSquareImgsAdapter(context, data[position].imgs)

                ToastUtils.showShort("图")
            }
            is ViderViewHolder -> {
                ToastUtils.showShort("视频")

            }
            is AudioViewHolder -> {
                ToastUtils.showShort("音频")

            }
        }


    }


    class PicViewHolder(view: View) : RecyclerView.ViewHolder(view)

    class AudioViewHolder(view: View) : RecyclerView.ViewHolder(view)

    class ViderViewHolder(view: View) : RecyclerView.ViewHolder(view)
}