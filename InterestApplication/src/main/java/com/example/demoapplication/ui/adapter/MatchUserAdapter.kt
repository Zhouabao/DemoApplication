package com.example.demoapplication.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.core.view.size
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.ToastUtils
import com.example.demoapplication.R
import com.example.demoapplication.model.MatchBean
import com.kotlin.base.common.BaseApplication
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.adapter.BaseRecyclerViewAdapter
import kotlinx.android.synthetic.main.item_match_user.view.*

/**
 *    author : ZFM
 *    date   : 2019/6/2415:13
 *    desc   :匹配用户适配器
 *    version: 1.0
 *
 *    //todo("图片高度要按照屏幕比例来压缩进行适配")
 */
class MatchUserAdapter(context: Context) : BaseRecyclerViewAdapter<MatchBean, MatchUserAdapter.ViewHolder>(context) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_match_user, parent, false)
        return ViewHolder(view)
    }


    //todo("填充用户数据")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        //todo("设置rv中的viewpgaer2竖直滑动 并且添加图片指示器")
//        holder.itemView.vpPhotos.setImageResource(dataList[position])
        val model = dataList[position]
        holder.itemView.vpPhotos.adapter = MatchImgsAdapter(mContext, model.imgs)
        holder.itemView.vpPhotos.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)

                for (i in 0 until holder.itemView.vpIndicator.size) {
                    (holder.itemView.vpIndicator[i] as RadioButton).isChecked = i == position
                }
            }
        })

        if (dataList[position].imgs.size > 1) {
            for (i in 0 until dataList[position].imgs.size) {
                val indicator = RadioButton(mContext)
                indicator.width = SizeUtils.dp2px(10F)
                indicator.height = SizeUtils.dp2px(10F)
                indicator.buttonDrawable = null
                indicator.background = mContext.resources.getDrawable(R.drawable.selector_circle_indicator)

                indicator.layoutParams =
                    LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                val layoutParams: LinearLayout.LayoutParams = indicator.layoutParams as LinearLayout.LayoutParams
                layoutParams.setMargins(0, SizeUtils.dp2px(6f), 0, 0)
                indicator.layoutParams = layoutParams

                indicator.isChecked = i == 0
                holder.itemView.vpIndicator.addView(indicator)
            }
        }

        holder.itemView.tvUserName.text = model.name
        holder.itemView.tvUserAge.text = "${model.age}"
        val drawable1 = ContextCompat.getDrawable(
            BaseApplication.context,
            if (model.sex == 1) R.drawable.icon_man_orange else R.drawable.icon_woman_orange
        )
        drawable1!!.setBounds(0, 0, drawable1.intrinsicWidth, drawable1.intrinsicHeight)    //需要设置图片的大小才能显示
        holder.itemView.tvUserAge.setCompoundDrawables(drawable1, null, null, null)
//        holder.itemView.tvLocation.text = model.


        holder.itemView.btnLike.onClick {
            ToastUtils.showShort("like  $position")
        }
        holder.itemView.btnLike.onClick {
            ToastUtils.showShort("like  $position")
        }
    }

    private fun createIndicator() {

    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
}