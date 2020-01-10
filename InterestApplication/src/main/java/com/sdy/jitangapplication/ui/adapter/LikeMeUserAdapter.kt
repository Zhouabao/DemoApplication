package com.sdy.jitangapplication.ui.adapter

import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.core.view.size
import androidx.viewpager.widget.ViewPager
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.SpanUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.kotlin.base.ext.onClick
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.PositiveLikeBean
import kotlinx.android.synthetic.main.item_like_me_user.view.*

class LikeMeUserAdapter : BaseQuickAdapter<PositiveLikeBean, BaseViewHolder>(R.layout.item_like_me_user) {
    override fun convert(helper: BaseViewHolder, item: PositiveLikeBean) {
        helper.addOnClickListener(R.id.likeBtn)
        helper.itemView.matchUserName.text = item.nickname
        helper.itemView.matchUserConstellation.text = item.constellation
        helper.itemView.matchUserDistance.text = "${item.distance}"
        helper.itemView.ivVip.isVisible = item.isvip
        helper.itemView.ivVerify.isVisible = item.isfaced
        helper.itemView.matchUserAge.text = "${item.age}"
        val left = mContext.resources.getDrawable(
            if (item.gender == 1) {
                R.drawable.icon_gender_man_gray
            } else {
                R.drawable.icon_gender_woman_gray
            }
        )
        helper.itemView.matchUserAge.setCompoundDrawablesWithIntrinsicBounds(left, null, null, null)


        helper.itemView.likeTag.text = SpanUtils.with(helper.itemView.likeTag)
            .append(
                "${if (!item.title.isNullOrEmpty()) {
                    "${if (item.gender == 1) {
                        "他"
                    } else {
                        "她"
                    }}通过"
                } else {
                    ""
                }}"
            )
            .append(
                if (!item.title.isNullOrEmpty()) {
                    "「${item.title}」"
                } else {
                    ""
                }
            )
            .setForegroundColor(mContext.resources.getColor(R.color.colorOrange))
            .setBold()
            .append(
                "${if (!item.title.isNullOrEmpty()) {
                    "标签找到你\n\n"
                } else {
                    ""
                }}"
            )
            .setForegroundColor(mContext.resources.getColor(R.color.colorBlack19))
            .append(
                "${if (!item.sametag.isNullOrEmpty()) {
                    "${if (item.gender == 1) {
                        "他"
                    } else {
                        "她"
                    }}有你喜欢的"
                } else {
                    ""
                }}"
            )
            .append(
                if (!item.sametag.isNullOrEmpty()) {
                    var tags = ""
                    for (tag in item.sametag) {
                        tags = tags.plus("「${tag}」")
                    }
                    "$tags"
                } else {
                    ""
                }
            )
            .setForegroundColor(mContext.resources.getColor(R.color.colorOrange))
            .setBold()
            .create()

        helper.itemView.vpIndicator.removeAllViews()
        helper.itemView.vpPhotos.setScrollable(false)
        helper.itemView.vpPhotos.currentItem = 0
        helper.itemView.vpPhotos.tag = helper.layoutPosition
        helper.itemView.vpPhotos.adapter = MatchImgsPagerAdapter(mContext, item.photo)

        helper.itemView.vpPhotos.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageSelected(position: Int) {
                for (i in 0 until helper.itemView.vpIndicator.size) {
                    (helper.itemView.vpIndicator[i] as RadioButton).isChecked = i == position
                }
            }

        })
        /*生成indicator*/
        if (helper.itemView.vpPhotos.adapter!!.count > 1) {
            val size = helper.itemView.vpPhotos.adapter!!.count
            for (i in 0 until size) {
                val width = SizeUtils.dp2px(6F)
                val height = SizeUtils.dp2px(6F)
                val indicator = RadioButton(mContext)
                indicator.buttonDrawable = null
                indicator.background = mContext.resources.getDrawable(R.drawable.selector_round_indicator)

                indicator.layoutParams = LinearLayout.LayoutParams(width, height)
                val layoutParams: LinearLayout.LayoutParams = indicator.layoutParams as LinearLayout.LayoutParams
                layoutParams.setMargins(
                    if (i == 0) {
                        SizeUtils.dp2px(15F)
                    } else {
                        0
                    }, 0, if (i == size - 1) {
                        SizeUtils.dp2px(15F)
                    } else {
                        SizeUtils.dp2px(6F)
                    }, 0
                )
                indicator.layoutParams = layoutParams
                indicator.isEnabled = false
                indicator.isChecked = i == 0
                helper.itemView.vpIndicator.addView(indicator)
            }
        }

        //下一张
        helper.itemView.nextImgBtn.onClick {
            if (helper.itemView.vpPhotos.adapter!!.count > helper.itemView.vpPhotos.currentItem + 1)
                helper.itemView.vpPhotos.currentItem = helper.itemView.vpPhotos.currentItem + 1
        }

        //上一张
        helper.itemView.lastImgBtn.onClick {
            if (helper.itemView.vpPhotos.currentItem > 0)
                helper.itemView.vpPhotos.currentItem = helper.itemView.vpPhotos.currentItem - 1
        }
    }


}
