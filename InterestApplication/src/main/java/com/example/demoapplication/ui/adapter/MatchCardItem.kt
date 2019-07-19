package com.example.demoapplication.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.SizeUtils
import com.example.demoapplication.R
import com.example.demoapplication.model.MatchBean
import com.example.demoapplication.widgets.stackview.BaseCardItem
import kotlinx.android.synthetic.main.item_match_user1.view.*

/**
 *    author : ZFM
 *    date   : 2019/7/1716:00
 *    desc   :
 *    version: 1.0
 */
class MatchCardItem(val context: Context, val data: MatchBean) : BaseCardItem(context) {

    override fun getView(convertView: View?, parent: ViewGroup?): View {
        val view = LayoutInflater.from(context).inflate(R.layout.item_match_user1, null)
     /*   view.vpPhotos.adapter = MatchImgsAdapter1(mContext, data.imgs)
        view.vpPhotos.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)

                for (i in 0 until view.vpIndicator.size) {
                    (view.vpIndicator[i] as RadioButton).isChecked = i == position
                }
            }
        })*/

        if (data.imgs.size > 1 && view.vpIndicator.childCount == 0) {
            for (i in 0 until data.imgs.size) {
                val indicator = RadioButton(mContext)
                indicator.width = SizeUtils.dp2px(10F)
                indicator.height = SizeUtils.dp2px(10F)
                indicator.buttonDrawable = null
                indicator.background = mContext.resources.getDrawable(R.drawable.selector_circle_indicator)

                indicator.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                val layoutParams: LinearLayout.LayoutParams = indicator.layoutParams as LinearLayout.LayoutParams
                layoutParams.setMargins(0, 0, SizeUtils.dp2px(6f), 0)
                indicator.layoutParams = layoutParams

                indicator.isChecked = i == 0
                view.vpIndicator.addView(indicator)
            }
        }

        view.matchUserName.text = data.name
        view.matchUserAge.text = "${data.age}"
        val drawable1 = ContextCompat.getDrawable(context, if (data.sex == 1) R.drawable.icon_man_orange else R.drawable.icon_woman_orange)
        drawable1!!.setBounds(0, 0, drawable1.intrinsicWidth, drawable1.intrinsicHeight)    //需要设置图片的大小才能显示
        view.matchUserAge.setCompoundDrawables(drawable1, null, null, null)

        return view
    }
}