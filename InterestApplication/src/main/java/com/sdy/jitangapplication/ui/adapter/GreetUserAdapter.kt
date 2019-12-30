package com.sdy.jitangapplication.ui.adapter

import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.core.view.size
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.kotlin.base.ext.onClick
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.GreetedListBean
import kotlinx.android.synthetic.main.item_greet_user.view.*

class GreetUserAdapter : BaseQuickAdapter<GreetedListBean, BaseViewHolder>(R.layout.item_greet_user) {
    override fun convert(helper: BaseViewHolder, item: GreetedListBean) {
        helper.itemView.matchUserName.text = item.nickname
        helper.itemView.matchUserConstellation.text = item.constellation
        helper.itemView.matchUserAge.text = "${item.age}"
        helper.itemView.matchUserDistance.text = "${item.distance}"
        helper.itemView.ivVip.isVisible = item.isvip == 1
        helper.itemView.ivVerify.isVisible = item.isfaced == 1
        helper.itemView.matchAim.isVisible = !item.intention_title.isNullOrEmpty()
        helper.itemView.matchAim.text = item.intention_title
        helper.itemView.matchBothIntersetLl.isVisible = !item.matching_content.isNullOrEmpty()
        helper.itemView.matchBothIntersetContent.text = item.matching_content


        helper.itemView.rvChatContent.layoutManager = LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
        val adapter = ChatContentAdapter()
        helper.itemView.rvChatContent.onFlingListener = null
        PagerSnapHelper().attachToRecyclerView(helper.itemView.rvChatContent)
        adapter.setNewData(item.msgs)
        helper.itemView.rvChatContent.adapter = adapter
        helper.itemView.rvChatContent.setOnTouchListener { v, event ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            false
        }

        helper.itemView.vpIndicator.removeAllViews()
        helper.itemView.vpPhotos.setScrollable(false)
        helper.itemView.vpPhotos.currentItem = 0
        helper.itemView.vpPhotos.tag = helper.layoutPosition
        helper.itemView.vpPhotos.adapter = MatchImgsPagerAdapter(mContext, item.photos)

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
