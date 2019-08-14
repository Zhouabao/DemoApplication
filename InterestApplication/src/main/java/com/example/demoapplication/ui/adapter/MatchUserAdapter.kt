package com.example.demoapplication.ui.adapter

import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.core.view.get
import androidx.core.view.size
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.demoapplication.R
import com.example.demoapplication.model.MatchBean
import com.kotlin.base.ext.onClick
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
        //为了防止indicator重复 每次先给他remove了
        holder.addOnClickListener(R.id.v1)
        holder.itemView.vpIndicator.removeAllViews()
        holder.itemView.vpPhotos.setScrollable(false)
        holder.itemView.vpPhotos.adapter = MatchImgsPagerAdapter(
            mContext,
            if (item.photos.isNullOrEmpty()) mutableListOf(item.avatar ?: "") else item.photos!!
        )
        holder.itemView.vpPhotos.currentItem = 0
        holder.itemView.vpPhotos.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageSelected(position: Int) {
                for (i in 0 until holder.itemView.vpIndicator.size) {
                    (holder.itemView.vpIndicator[i] as RadioButton).isChecked = i == position
                }
                when (position) {
                    0 -> {
                        holder.itemView.matchUserInfoCl.visibility = View.VISIBLE
                        holder.itemView.matchUserIntroduce.visibility = View.INVISIBLE
                        holder.itemView.matchUserDynamicLl.visibility = View.INVISIBLE
                    }
                    1 -> {
                        holder.itemView.matchUserInfoCl.visibility = View.INVISIBLE
                        holder.itemView.matchUserIntroduce.visibility = if (item.sign.isNullOrEmpty()) {
                            View.INVISIBLE
                        } else {
                            View.VISIBLE
                        }
                        holder.itemView.matchUserDynamicLl.visibility = View.INVISIBLE
                    }
                    2 -> {
                        holder.itemView.matchUserInfoCl.visibility = View.INVISIBLE
                        holder.itemView.matchUserIntroduce.visibility = View.INVISIBLE
                        holder.itemView.matchUserDynamicLl.visibility = View.VISIBLE
                    }
                }
            }

        })

        /*生成indicator*/
        if ((item.photos ?: mutableListOf<MatchBean>()).size > 1) {
            val size = (item.photos ?: mutableListOf<MatchBean>()).size
            for (i in 0 until size) {
                val indicator = RadioButton(mContext)
                indicator.width = ((ScreenUtils.getScreenWidth()
                        - SizeUtils.dp2px(15F) * 4
                        - (SizeUtils.dp2px(6F) * (size - 1))) * 1F / size).toInt()
                indicator.height = SizeUtils.dp2px(5F)
                indicator.buttonDrawable = null
                indicator.background = mContext.resources.getDrawable(R.drawable.selector_round_indicator)

                indicator.layoutParams =
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
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
                holder.itemView.vpIndicator.addView(indicator)
            }
        }

        /*设置封面图片recyclerview*/
        if (!item.square.isNullOrEmpty()) {
            holder.itemView.matchUserDynamicThumbRv.visibility = View.VISIBLE
            holder.itemView.matchUserDynamicThumbRv.layoutManager =
                LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
            val adapter = DetailThumbAdapter(mContext)
            adapter.setData(item.square ?: mutableListOf())
            holder.itemView.matchUserDynamicThumbRv.adapter = adapter
        } else {
            holder.itemView.matchUserDynamicThumbRv.visibility = View.GONE
            holder.itemView.matchUserDynamicThumbTitle.visibility = View.GONE
        }


        //点击切换上一张图片
        holder.itemView.lastImgBtn.onClick {
            if (holder.itemView.vpPhotos.currentItem > 0) {
                val index = holder.itemView.vpPhotos.currentItem
                holder.itemView.vpPhotos.setCurrentItem(index - 1, false)
            }
        }

        //点击切换下一张图片
        holder.itemView.nextImgBtn.onClick {
            if (holder.itemView.vpPhotos.currentItem < (item.photos ?: mutableListOf<MatchBean>()).size - 1) {
                val index = holder.itemView.vpPhotos.currentItem
                holder.itemView.vpPhotos.setCurrentItem(index + 1, false)
            }
        }

        holder.itemView.ivVip.visibility = if (item.isvip == 1) {
            View.VISIBLE
        } else {
            View.GONE
        }

        holder.itemView.ivVerify.visibility = if (item.isverify == 1) {
            View.VISIBLE
        } else {
            View.GONE
        }
        holder.itemView.matchUserIntroduce.text = item.sign ?: ""


        holder.itemView.matchUserLabelsLikeCount.visibility = if (item.tagcount == null || item.tagcount == 0) {
            View.INVISIBLE
        } else {
            holder.itemView.matchUserLabelsLikeCount.text = "${item.tagcount}个标签重合"
            View.VISIBLE
        }
        holder.itemView.matchUserJob.visibility = if (item.job.isNullOrEmpty()) {
            View.INVISIBLE
        } else {
            holder.itemView.matchUserJob.text = item.job ?: ""
            View.VISIBLE
        }
        holder.itemView.matchUserName.text = item.nickname ?: ""
        holder.itemView.matchUserAge.text = "${item.age}\t" + "/\t${if (item.gender == 1) {
            "男"
        } else {
            "女"
        }}\t" + "/\t${item.job ?: ""}\t" + "/\t${item.distance ?: ""}"

    }

}