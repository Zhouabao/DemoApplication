package com.example.demoapplication.ui.adapter

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.demoapplication.R
import com.example.demoapplication.model.MatchBean
import kotlinx.android.synthetic.main.item_list_square_pic.view.*


/**
 *    author : ZFM
 *    date   : 2019/6/2616:27
 *    desc   :
 *    version: 1.0
 */
class MultiListSquareAdapter(var context: Context, data: MutableList<MatchBean>) :
    BaseMultiItemQuickAdapter<MatchBean, BaseViewHolder>(data) {

    init {
        addItemType(MatchBean.PIC, R.layout.item_list_square_pic)
        addItemType(MatchBean.VIDEO, R.layout.item_list_square_video)
        addItemType(MatchBean.AUDIO, R.layout.item_list_square_audio)
    }

    override fun convert(holder: BaseViewHolder, item: MatchBean) {
        when (holder.itemViewType) {
            MatchBean.PIC -> {
                holder.itemView.squareUserPics.layoutManager =
                    LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
                holder.itemView.squareUserPics.adapter = ListSquareImgsAdapter(context, item.imgs)

            }
            MatchBean.VIDEO -> {

            }
            MatchBean.AUDIO -> {

            }
        }

    }
}
