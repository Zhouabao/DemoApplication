package com.sdy.jitangapplication.ui.adapter

import androidx.core.view.isVisible
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.AddressBean
import kotlinx.android.synthetic.main.item_address.view.*

/**
 *    author : ZFM
 *    date   : 2020/3/2611:39
 *    desc   :地址
 *    version: 1.0
 */
class AddressAdapter : BaseQuickAdapter<AddressBean, BaseViewHolder>(R.layout.item_address) {
    override fun convert(helper: BaseViewHolder, item: AddressBean) {
        helper.addOnClickListener(R.id.addressEdit)
        helper.addOnClickListener(R.id.menuDefault)
        helper.addOnClickListener(R.id.menuDelete)
        helper.addOnClickListener(R.id.content)
        if (item.checked) {
            helper.itemView.addressChecked.setImageResource(R.drawable.icon_checked_address)
        } else {
            helper.itemView.addressChecked.setImageResource(R.drawable.icon_location_address)
        }

        helper.itemView.addressName.text = "${item.nickname}\t\t${item.phone}"
        helper.itemView.addressDefault.isVisible = item.is_default
        helper.itemView.addressContent.text =
            "${item.province_name}${item.city_name}${item.area_name}${item.full_address}"

    }
}