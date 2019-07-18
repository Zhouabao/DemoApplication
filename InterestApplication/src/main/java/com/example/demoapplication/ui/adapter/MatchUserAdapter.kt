package com.example.demoapplication.ui.adapter

import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.core.view.size
import androidx.viewpager2.widget.ViewPager2
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.demoapplication.R
import com.example.demoapplication.model.MatchBean
import kotlinx.android.synthetic.main.item_match_user.view.*

/**
 *    author : ZFM
 *    date   : 2019/6/2415:13
 *    desc   :匹配用户适配器
 *    version: 1.0
 *
 */
class MatchUserAdapter(data: MutableList<MatchBean>) :
    BaseQuickAdapter<MatchBean, BaseViewHolder>(R.layout.item_match_user, data) {
    override fun convert(holder: BaseViewHolder, item: MatchBean) {
        holder.itemView.vpPhotos.adapter = MatchImgsAdapter(mContext, item.imgs)
        holder.itemView.vpPhotos.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)

                for (i in 0 until holder.itemView.vpIndicator.size) {
                    (holder.itemView.vpIndicator[i] as RadioButton).isChecked = i == position
                }
            }
        })

        if (item.imgs.size > 1 && holder.itemView.vpIndicator.childCount == 0) {
            for (i in 0 until item.imgs.size) {
                val indicator = RadioButton(mContext)
                indicator.width = SizeUtils.dp2px(10F)
                indicator.height = SizeUtils.dp2px(10F)
                indicator.buttonDrawable = null
                indicator.background = mContext.resources.getDrawable(R.drawable.selector_circle_indicator)

                indicator.layoutParams =
                    LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                val layoutParams: LinearLayout.LayoutParams = indicator.layoutParams as LinearLayout.LayoutParams
                layoutParams.setMargins(0, 0, SizeUtils.dp2px(6f), 0)
                indicator.layoutParams = layoutParams

                indicator.isChecked = i == 0
                holder.itemView.vpIndicator.addView(indicator)
            }
        }

        holder.itemView.matchUserName.text = item.name
        holder.itemView.matchUserAge.text = "${item.age}"
        val drawable1 = ContextCompat.getDrawable(
            mContext,
            if (item.sex == 1) R.drawable.icon_man_orange else R.drawable.icon_woman_orange
        )
        drawable1!!.setBounds(0, 0, drawable1.intrinsicWidth, drawable1.intrinsicHeight)    //需要设置图片的大小才能显示
        holder.itemView.matchUserAge.setCompoundDrawables(drawable1, null, null, null)
//        holder.itemView.tvLocation.text = model.
//        holder.addOnClickListener(R.id.v1)
        holder.addOnClickListener(R.id.btnLike)
        holder.addOnClickListener(R.id.btnDislike)


    }

}