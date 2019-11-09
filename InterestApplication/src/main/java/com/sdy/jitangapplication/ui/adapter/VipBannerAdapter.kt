package com.sdy.jitangapplication.ui.adapter

import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.VipDescr
import com.sdy.jitangapplication.ui.dialog.ChargeVipDialog
import com.sdy.jitangapplication.widgets.TimeRunTextView
import kotlinx.android.synthetic.main.item_vip_banner.view.*

/**
 *    author : ZFM
 *    date   : 2019/7/3016:56
 *    desc   : 会员支付购买时间适配器
 *    version: 1.0
 */
class VipBannerAdapter(var type: Int = 1) : BaseQuickAdapter<VipDescr, BaseViewHolder>(R.layout.item_vip_banner) {
    override fun convert(holder: BaseViewHolder, data: VipDescr) {
        holder.itemView.banner_name.text = "${data.title}"
        holder.itemView.banner_content.text = "${data.rule}"
        val params = holder.itemView.banner_img.layoutParams
        params.height = SizeUtils.dp2px(130F)
        params.width = LinearLayout.LayoutParams.WRAP_CONTENT
        holder.itemView.banner_img.layoutParams = params
        GlideUtil.loadRoundImgCenterinside(mContext, data.url ?: "", holder.itemView.banner_img, 0.1F, 0)

        if (type == ChargeVipDialog.PURCHASE_GREET_COUNT) {
            if (data.countdown > 0) {
                holder.itemView.banner_name.text = "${data.title}"
                holder.itemView.banner_time.isVisible = true
                holder.itemView.banner_time.startTime(data.countdown.toLong(), "2", "后补充")
                holder.itemView.banner_time.setTimeViewListener(object : TimeRunTextView.OnTimeViewListener {
                    override fun onTimeStart() {

                    }

                    override fun onTimeEnd() {
                        data.countdown = 0
                        holder.itemView.banner_time.isVisible = false
                        holder.itemView.banner_name.text = "${data.title_pay}"
                    }
                })
            } else {
                holder.itemView.banner_name.text = "${data.title_pay}"
                holder.itemView.banner_time.isVisible = false
            }
        } else {
            holder.itemView.banner_time.isVisible = false
        }
    }
}