package com.sdy.jitangapplication.ui.adapter

import android.graphics.Color
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.AddressBean
import kotlinx.android.synthetic.main.item_address.view.*

/**
 *    author : ZFM
 *    date   : 2020/3/2611:39
 *    desc   :
 *    version: 1.0
 */
class AddressAdapter : BaseQuickAdapter<AddressBean, BaseViewHolder>(R.layout.item_address) {
    override fun convert(helper: BaseViewHolder, item: AddressBean) {

        val params = helper.itemView.layoutParams as RecyclerView.LayoutParams
        params.leftMargin = if (helper.layoutPosition == 0) {
            0
        } else {
            SizeUtils.dp2px(10f)
        }
        helper.itemView.addAddressIv.isVisible = helper.layoutPosition == 0
        helper.itemView.addressCl.isVisible = helper.layoutPosition != 0
        helper.addOnClickListener(R.id.addAddressIv)
        helper.addOnClickListener(R.id.addressCl)
        if (item.checked) {
            if (helper.layoutPosition != 0)
                helper.itemView.setBackgroundResource(R.drawable.shape_rectangle_orange_white_10dp)
            else
                helper.itemView.setBackgroundColor(Color.TRANSPARENT)
            helper.itemView.addressChecked.isVisible = true
            helper.itemView.addressName.setTextColor(mContext.resources.getColor(R.color.colorOrange))
            helper.itemView.addressPhone.setTextColor(mContext.resources.getColor(R.color.colorOrange))
            helper.itemView.addressContent.setTextColor(Color.parseColor("#FFFF864C"))
        } else {
            helper.itemView.setBackgroundResource(R.drawable.shape_rectangle_gray_10dp)
            helper.itemView.addressChecked.isVisible = false
            helper.itemView.addressName.setTextColor(Color.parseColor("#FF191919"))
            helper.itemView.addressPhone.setTextColor(Color.parseColor("#FF191919"))
            helper.itemView.addressContent.setTextColor(Color.parseColor("#FF696E73"))
        }

        helper.itemView.addressName.text = item.nickname
        helper.itemView.addressPhone.text = item.phone
        helper.itemView.addressContent.text =
            "${item.province_name}${item.city_name}${item.area_name}${item.full_address}"

    }
}