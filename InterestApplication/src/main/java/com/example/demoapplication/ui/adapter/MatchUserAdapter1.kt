package com.example.demoapplication.ui.adapter

import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.core.view.get
import androidx.core.view.size
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.demoapplication.R
import com.example.demoapplication.model.MatchBean1
import com.kotlin.base.ext.onClick
import kotlinx.android.synthetic.main.item_match_user1.view.*

/**
 *    author : ZFM
 *    date   : 2019/6/2415:13
 *    desc   :匹配用户适配器
 *    version: 1.0
 *
 */
class MatchUserAdapter1(data: MutableList<MatchBean1>) :
    BaseQuickAdapter<MatchBean1, BaseViewHolder>(R.layout.item_match_user1, data) {
    override fun convert(holder: BaseViewHolder, item: MatchBean1) {
        //为了防止indicator重复 每次先给他remove了
        holder.itemView.vpIndicator.removeAllViews()

        holder.itemView.vpPhotos.adapter = MatchImgsAdapter(
            mContext,
            if (item.photos.isNullOrEmpty()) mutableListOf(item.avatar ?: "") else item.photos!!
        )
        holder.itemView.vpPhotos.currentItem = 0
        holder.itemView.vpPhotos.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                for (i in 0 until holder.itemView.vpIndicator.size) {
                    (holder.itemView.vpIndicator[i] as RadioButton).isChecked = i == position
                }

                when (position) {
                    0 -> {
                        holder.itemView.matchUserInfoCl.visibility = View.VISIBLE
                        holder.itemView.matchUserIntroduce.visibility = View.GONE
                        holder.itemView.matchUserDynamicLl.visibility = View.GONE
                    }
                    1 -> {
                        holder.itemView.matchUserInfoCl.visibility = View.GONE
                        holder.itemView.matchUserIntroduce.visibility = View.VISIBLE
                        holder.itemView.matchUserDynamicLl.visibility = View.GONE
                    }
                    2 -> {
                        holder.itemView.matchUserInfoCl.visibility = View.GONE
                        holder.itemView.matchUserIntroduce.visibility = View.GONE
                        holder.itemView.matchUserDynamicLl.visibility = View.VISIBLE
                    }
                }
            }
        })

        /*生成indicator*/
        if ((item.photos ?: mutableListOf<MatchBean1>()).size > 1) {
            val size = (item.photos ?: mutableListOf<MatchBean1>()).size
            for (i in 0 until size) {
                val indicator = RadioButton(mContext)
                indicator.width =((ScreenUtils.getScreenWidth()
                        - SizeUtils.applyDimension(15F, TypedValue.COMPLEX_UNIT_DIP)*2
                        - (SizeUtils.applyDimension(6F, TypedValue.COMPLEX_UNIT_DIP)*(size-1)))/size).toInt()
                indicator.height = SizeUtils.dp2px(5F)
                indicator.buttonDrawable = null
                indicator.background = mContext.resources.getDrawable(R.drawable.selector_round_indicator)

                indicator.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                val layoutParams: LinearLayout.LayoutParams = indicator.layoutParams as LinearLayout.LayoutParams
                layoutParams.setMargins(if(i==0){SizeUtils.dp2px(15F)}else{0}, 0,if(i==size-1){SizeUtils.dp2px(15F)}else{ SizeUtils.dp2px(6f)}, 0)
                indicator.layoutParams = layoutParams
                indicator.isEnabled = false
                indicator.isChecked = i == 0
                holder.itemView.vpIndicator.addView(indicator)
            }
        }

        /*设置封面图片recyclerview*/
        holder.itemView.matchUserDynamicThumbRv.layoutManager =
            LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
        val adapter = DetailThumbAdapter(mContext)
        adapter.setData(item.square?: mutableListOf())
        holder.itemView.matchUserDynamicThumbRv.adapter = adapter

        //点击切换上一张图片
        holder.itemView.lastImgBtn.onClick {
            if (holder.itemView.vpPhotos.currentItem > 0) {
                val index = holder.itemView.vpPhotos.currentItem
                holder.itemView.vpPhotos.setCurrentItem(index - 1, false)
            }
        }

        //点击切换下一张图片
        holder.itemView.nextImgBtn.onClick {
            if (holder.itemView.vpPhotos.currentItem < (item.photos ?: mutableListOf<MatchBean1>()).size - 1) {
                val index = holder.itemView.vpPhotos.currentItem
                holder.itemView.vpPhotos.setCurrentItem(index + 1, false)
            }
        }

        holder.itemView.ivVip.visibility = if (item.isvip == 1) {
            View.VISIBLE
        } else {
            View.GONE
        }
        holder.itemView.matchUserIntroduce.text = item.sign ?: ""
        holder.itemView.matchUserLightCount.text = "${item.square_count}"
        holder.itemView.matchUserLabelsLikeCount.text = "${item.tagcount}"
        holder.itemView.matchUserName.text = item.nickname ?: ""
        holder.itemView.matchUserJob.text = item.job ?: ""
        holder.itemView.matchUserAge.text = "${item.age}\t" +
                "/\t${if (item.gender == 1) { "男" } else { "女" }}\t" +
                "/\t${item.job ?: ""}\t" +
                "/\t${item.distance ?: ""}"
    }

}