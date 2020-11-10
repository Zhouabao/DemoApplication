package com.sdy.jitangapplication.ui.adapter

import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.SomeOneGetGiftBean
import kotlinx.android.synthetic.main.item_receive_gift.view.*

/**
 *    author : ZFM
 *    date   : 2020/4/210:16
 *    desc   :
 *    version: 1.0
 */
class SomeOneGetGiftAdapter :
    BaseQuickAdapter<SomeOneGetGiftBean, BaseViewHolder>(R.layout.item_receive_gift) {
    override fun convert(helper: BaseViewHolder, item: SomeOneGetGiftBean) {

        if (helper.layoutPosition == 0) {
            helper.itemView.iconScore.setImageResource(R.drawable.icon_num1)
            helper.itemView.iconScore.isVisible = true
        } else if (helper.layoutPosition == 1) {
            helper.itemView.iconScore.setImageResource(R.drawable.icon_num2)
            helper.itemView.iconScore.isVisible = true
        } else if (helper.layoutPosition == 2) {
            helper.itemView.iconScore.setImageResource(R.drawable.icon_num3)
            helper.itemView.iconScore.isVisible = true
        } else {
            helper.itemView.iconScore.isVisible = false
        }

        GlideUtil.loadCircleImg(mContext, item.avatar, helper.itemView.donateAvator)
        helper.addOnClickListener(R.id.donateAvator)
        helper.itemView.donateNickname.text = item.nickname
        helper.itemView.donateNum.text =
            mContext.getString(R.string.has_send_left, item.gif_cnt)
        helper.itemView.donateCandyAmount.text = "${item.all_amount}"
        helper.itemView.donateGiftRv.layoutManager =
            GridLayoutManager(mContext, 5, RecyclerView.VERTICAL, false)
        val adapter = ReceivetGiftAdapter()
        helper.itemView.donateGiftRv.adapter = adapter
        adapter.setNewData(item.list)
    }

}