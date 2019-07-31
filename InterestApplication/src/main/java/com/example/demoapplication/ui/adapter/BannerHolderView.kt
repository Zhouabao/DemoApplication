package com.example.demoapplication.ui.adapter

import android.content.Context
import android.view.View
import com.bigkoo.convenientbanner.holder.Holder
import com.example.baselibrary.glide.GlideUtil
import com.example.demoapplication.model.VipDescr
import kotlinx.android.synthetic.main.item_vip_banner.view.*

/**
 *    author : ZFM
 *    date   : 2019/6/2810:48
 *    desc   :
 *    version: 1.0
 */
class BannerHolderView(itemView: View, var context: Context) : Holder<VipDescr>(itemView) {
    override fun updateUI(data: VipDescr) {
        itemView.banner_name.text = "${data.title}"
        itemView.banner_content.text = "${data.rule}"
        GlideUtil.loadImg(context, data.url ?: "", itemView.banner_img)
    }

    override fun initView(itemView: View?) {

    }

}