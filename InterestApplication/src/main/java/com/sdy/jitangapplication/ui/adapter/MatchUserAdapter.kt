package com.sdy.jitangapplication.ui.adapter

import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.core.view.size
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.MatchBean
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
        holder.itemView.vpPhotos.tag = holder.layoutPosition
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
                    0 -> {  //0显示广场
                        holder.itemView.matchUserLocalTagCl.isVisible = true
                        holder.itemView.matchUserDynamicLl.isVisible = false
                        holder.itemView.matchUserIntroduceCl.isVisible = false
                    }
                    1 -> {//如果广场动态有，就显示广场
                        //如果广场动态没有,就判断有没有签名, 如果有签名显示签名 ,如果没有就显示个人信息
                        // 如果有个人信息显示个人信息
                        if (item.square.isNullOrEmpty()) {
                            holder.itemView.matchUserDynamicLl.isVisible = false
                            holder.itemView.matchUserIntroduceCl.isVisible = !item.sign.isNullOrBlank()
                            holder.itemView.matchUserLocalTagCl.isVisible = item.sign.isNullOrBlank()
                        } else {
                            holder.itemView.matchUserDynamicLl.isVisible = true
                            holder.itemView.matchUserIntroduceCl.isVisible = false
                            holder.itemView.matchUserLocalTagCl.isVisible = false
                        }
                    }
                    else -> {
                        if (item.sign.isNullOrBlank()) {
                            holder.itemView.matchUserIntroduceCl.isVisible = false
                            holder.itemView.matchUserLocalTagCl.isVisible = item.square.isNullOrEmpty()
                            holder.itemView.matchUserDynamicLl.isVisible = !item.square.isNullOrEmpty()
                        } else {
                            holder.itemView.matchUserIntroduceCl.isVisible = true
                            holder.itemView.matchUserDynamicLl.isVisible = false
                            holder.itemView.matchUserLocalTagCl.isVisible = false
                        }

                    }
                }
            }

        })

        /*生成indicator*/
        if ((item.photos ?: mutableListOf<MatchBean>()).size > 1) {
            val size = (item.photos ?: mutableListOf<MatchBean>()).size
            for (i in 0 until size) {
//                val width = ((ScreenUtils.getScreenWidth()
//                        - SizeUtils.dp2px(15F) * 4
//                        - (SizeUtils.dp2px(6F) * (size - 1))) * 1F / size).toInt()
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
                holder.itemView.vpIndicator.addView(indicator)
            }
        }

        /*设置封面图片recyclerview*/
        if (item.square.isNullOrEmpty()) {
            holder.itemView.matchUserDynamicLl.visibility = View.GONE
            holder.itemView.matchUserIntroduceCl.visibility = View.GONE
            holder.itemView.matchUserLocalTagCl.visibility = View.VISIBLE
        } else {
            holder.itemView.matchUserDynamicLl.visibility = View.VISIBLE
            holder.itemView.matchUserDynamicThumbRv.layoutManager =
                LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
            val adapter = DetailThumbAdapter(mContext)
            adapter.setData(item.square ?: mutableListOf())
            holder.itemView.matchUserDynamicThumbRv.adapter = adapter
            holder.itemView.matchUserIntroduceCl.visibility = View.GONE
            holder.itemView.matchUserLocalTagCl.visibility = View.GONE
        }

        //点击切换上一张图片
        holder.addOnClickListener(R.id.lastImgBtn)
        //点击切换下一张图片
        holder.addOnClickListener(R.id.nextImgBtn)


        holder.itemView.ivVip.visibility = if (item.isvip == 1) {
            View.VISIBLE
        } else {
            View.GONE
        }

        holder.itemView.ivVerify.visibility = if (item.isfaced == 1) {
            View.VISIBLE
        } else {
            View.GONE
        }


        holder.itemView.matchUserIntroduce.text = item.sign ?: ""

//        holder.itemView.matchUserLocalTagContent.text = item.sign ?: ""  //标签下的描述
        holder.itemView.matchUserLocalTagCharacter.layoutManager =
            LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)//标签下的特质标签
        val adapter1 = MatchDetailLabelAdapter(mContext)
        adapter1.setData(item.tags ?: mutableListOf())
        holder.itemView.matchUserLocalTagCharacter.adapter = adapter1

//        holder.itemView.matchUserLabelsLikeCount.visibility = if (item.tagcount == null || item.tagcount == 0) {
//            View.GONE
//        } else {
//            holder.itemView.matchUserLabelsLikeCount.text = "${item.tagcount}个标签重合"
//            View.VISIBLE
//        }
//        holder.itemView.matchUserJob.visibility = if (item.job.isNullOrEmpty()) {
//            View.GONE
//        } else {
//            holder.itemView.matchUserJob.text = item.job ?: ""
//            View.VISIBLE
//        }
        holder.itemView.matchUserName.text = item.nickname ?: ""
        holder.itemView.matchUserAge.text = "${item.age}"
        val left = mContext.resources.getDrawable(
            if (item.gender == 1) {
                R.drawable.icon_gender_man_gray
            } else {
                R.drawable.icon_gender_woman_gray
            }
        )
        holder.itemView.matchUserAge.setCompoundDrawablesWithIntrinsicBounds(left, null, null, null)

        holder.itemView.matchUserConstellation.text = "${item.constellation}"
        holder.itemView.matchUserDistance.text = "${item.distance}"
    }

}