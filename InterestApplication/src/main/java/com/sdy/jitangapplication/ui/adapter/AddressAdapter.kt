package com.sdy.jitangapplication.ui.adapter

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import kotlinx.android.synthetic.main.item_address.view.*

/**
 *    author : ZFM
 *    date   : 2020/3/2611:39
 *    desc   :
 *    version: 1.0
 */
class AddressAdapter : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_address) {
    override fun convert(helper: BaseViewHolder, item: String) {

        val params = helper.itemView.layoutParams as RecyclerView.LayoutParams
        params.rightMargin = if (helper.layoutPosition == mData.size - 1) {
            0
        } else {
            SizeUtils.dp2px(10f)
        }
        helper.itemView.addAddressIv.isVisible = helper.layoutPosition == 0
        helper.itemView.addressCl.isVisible = helper.layoutPosition != 0
        helper.addOnClickListener(R.id.addAddressIv)
        helper.addOnClickListener(R.id.addressCl)

    }
}