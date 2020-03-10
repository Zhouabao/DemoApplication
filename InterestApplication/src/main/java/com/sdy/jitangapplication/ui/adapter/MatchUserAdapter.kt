package com.sdy.jitangapplication.ui.adapter

import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.core.view.size
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.google.android.flexbox.*
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.MatchBean
import com.sdy.jitangapplication.model.Newtag
import com.sdy.jitangapplication.ui.activity.MatchDetailActivity
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.DividerItemDecoration
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
    var my_tags_quality: MutableList<Newtag> = mutableListOf()
    override fun convert(holder: BaseViewHolder, item: MatchBean) {
        //点击切换上一张图片
        holder.addOnClickListener(R.id.lastImgBtn)
        //点击切换下一张图片
        holder.addOnClickListener(R.id.nextImgBtn)
        holder.addOnClickListener(R.id.v1)
        holder.addOnClickListener(R.id.btnHi)
        holder.addOnClickListener(R.id.btnHiLottieView)
        holder.itemView.btnHiIv.alpha = 1F
        holder.itemView.btnHiLeftTime.alpha = 0F
        //为了防止indicator重复 每次先给他remove了
        holder.itemView.vpIndicator.removeAllViews()
        holder.itemView.vpPhotos.setScrollable(false)
        holder.itemView.vpPhotos.tag = holder.layoutPosition
        holder.itemView.vpPhotos.adapter = MatchImgsPagerAdapter(
            mContext,
            if (item.photos.isNullOrEmpty()) mutableListOf(item.avatar ?: "") else item.photos!!
        )


        holder.itemView.vpPhotos.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageSelected(position: Int) {
                for (i in 0 until holder.itemView.vpIndicator.size) {
                    (holder.itemView.vpIndicator[i] as RadioButton).isChecked = i == position
                }

                //首张内容  用户在该兴趣下的特质
                //次张内容  用户的「关于我」描述文本。如用用户填写过则呈现，没有则保留至最后一张显示状态
                //三张内容  用户在发布过的内容（所有兴趣），参考设计内容，如用户没发布过则跳转至第三张内容
                when (position) {
                    0 -> {  //0显示用户在该兴趣下的特质
                        if (!item.newtags.isNullOrEmpty() && item.newtags!![0].label_quality.isNotEmpty()) {
                            holder.itemView.matchUserLocalTagCl.visibility = View.VISIBLE
                            holder.itemView.matchUserLocalTagCharacter.isVisible = true
                            (holder.itemView.matchUserLocalTagCharacter.layoutParams as ConstraintLayout.LayoutParams).topMargin =
                                SizeUtils.dp2px(5F)
                            holder.itemView.matchUserLocalTagContent.isVisible = false
                            holder.itemView.matchUserDynamicThumbRv.isVisible = false
                        } else if (!item.sign.isNullOrBlank()) {
                            holder.itemView.matchUserLocalTagCl.visibility = View.VISIBLE
                            holder.itemView.matchUserLocalTagContent.isVisible = true
                            holder.itemView.matchUserLocalTagCharacter.isVisible = false
                            holder.itemView.matchUserDynamicThumbRv.isVisible = false
                        } else if (!item.square.isNullOrEmpty()) {
                            holder.itemView.matchUserLocalTagCl.visibility = View.VISIBLE

                            holder.itemView.matchUserDynamicThumbRv.isVisible = true
                            holder.itemView.matchUserLocalTagContent.isVisible = false
                            holder.itemView.matchUserLocalTagCharacter.isVisible = false
                        } else {
                            holder.itemView.matchUserLocalTagCl.visibility = View.INVISIBLE
                        }

                    }
                    1 -> {
                        if (!item.sign.isNullOrBlank()) {
                            holder.itemView.matchUserLocalTagCl.visibility = View.VISIBLE
                            holder.itemView.matchUserLocalTagContent.isVisible = true
                            holder.itemView.matchUserLocalTagCharacter.isVisible = false
                            holder.itemView.matchUserDynamicThumbRv.isVisible = false
                        } else if (!item.square.isNullOrEmpty()) {
                            holder.itemView.matchUserLocalTagCl.visibility = View.VISIBLE
                            holder.itemView.matchUserDynamicThumbRv.isVisible = true
                            holder.itemView.matchUserLocalTagContent.isVisible = false
                            holder.itemView.matchUserLocalTagCharacter.isVisible = false
                        } else if (!item.newtags.isNullOrEmpty() && !item.newtags!![0].label_quality.isEmpty()) {
                            holder.itemView.matchUserLocalTagCl.visibility = View.VISIBLE
                            holder.itemView.matchUserLocalTagCharacter.isVisible = true
                            holder.itemView.matchUserLocalTagContent.isVisible = false
                            holder.itemView.matchUserDynamicThumbRv.isVisible = false
                        } else {

                            holder.itemView.matchUserLocalTagCl.visibility = View.INVISIBLE
                        }
                    }
                    else -> {
                        if (!item.square.isNullOrEmpty()) {
                            holder.itemView.matchUserLocalTagCl.visibility = View.VISIBLE

                            holder.itemView.matchUserDynamicThumbRv.isVisible = true
                            holder.itemView.matchUserLocalTagContent.isVisible = false
                            holder.itemView.matchUserLocalTagCharacter.isVisible = false
                        } else if (!item.sign.isNullOrBlank()) {
                            holder.itemView.matchUserLocalTagCl.visibility = View.VISIBLE

                            holder.itemView.matchUserLocalTagContent.isVisible = true
                            holder.itemView.matchUserLocalTagCharacter.isVisible = false
                            holder.itemView.matchUserDynamicThumbRv.isVisible = false
                        } else if (!item.newtags.isNullOrEmpty() && !item.newtags!![0].label_quality.isEmpty()) {
                            holder.itemView.matchUserLocalTagCl.visibility = View.VISIBLE

                            holder.itemView.matchUserLocalTagCharacter.isVisible = true
                            holder.itemView.matchUserLocalTagContent.isVisible = false
                            holder.itemView.matchUserDynamicThumbRv.isVisible = false
                        } else {
                            holder.itemView.matchUserLocalTagCl.visibility = View.INVISIBLE
                        }
                    }
                }
            }

        })
        holder.itemView.vpPhotos.currentItem = 0

        /*生成indicator*/
        if ((item.photos ?: mutableListOf<MatchBean>()).size > 1) {
            holder.itemView.vpIndicator.isVisible = true
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
        } else {
            holder.itemView.vpIndicator.isVisible = false
        }

        /*设置封面图片recyclerview*/
        val itemDecoration = DividerItemDecoration(
            mContext,
            DividerItemDecoration.VERTICAL_LIST,
            SizeUtils.dp2px(3F),
            mContext.resources.getColor(R.color.colorTransparent)
        )
        if (!item.square.isNullOrEmpty()) {
            holder.itemView.matchUserDynamicThumbRv.layoutManager =
                LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
            for (decoration in 0 until holder.itemView.matchUserDynamicThumbRv.itemDecorationCount) {
                holder.itemView.matchUserDynamicThumbRv.removeItemDecorationAt(decoration)
            }
            holder.itemView.matchUserDynamicThumbRv.addItemDecoration(itemDecoration)

            val adapter = DetailThumbAdapter(dataSize = (item.square ?: mutableListOf()).size)
            if ((item.square ?: mutableListOf()).size > DetailThumbAdapter.MAX_MATCH_COUNT) {
                adapter.setNewData(item.square!!.subList(0, DetailThumbAdapter.MAX_MATCH_COUNT))
            } else {
                adapter.setNewData(item.square ?: mutableListOf())
            }
            holder.itemView.matchUserDynamicThumbRv.adapter = adapter
            adapter.setOnItemClickListener { adapter, view, position ->
                MatchDetailActivity.start(mContext, item.accid)
            }
        }


        holder.itemView.ivVip.isVisible = item.isvip == 1
        holder.itemView.ivVerify.isVisible = item.isfaced == 1
        holder.itemView.btnHi.isVisible = item.greet_switch
        holder.itemView.btnHiView.isVisible = item.greet_switch
        if (!item.newtags.isNullOrEmpty() && item.newtags!![0].label_quality.isNotEmpty()) {
            (holder.itemView.matchUserLocalTagCharacter.layoutParams as ConstraintLayout.LayoutParams).topMargin =
                SizeUtils.dp2px(5F)
            holder.itemView.matchUserLocalTagCl.isVisible = true
            holder.itemView.matchUserLocalTagCharacter.isVisible = true
            holder.itemView.matchUserLocalTagContent.isVisible = false
            holder.itemView.matchUserDynamicThumbRv.isVisible = false
        } else if (!item.sign.isNullOrBlank()) {
            holder.itemView.matchUserLocalTagCl.isVisible = true
            holder.itemView.matchUserLocalTagContent.isVisible = true
            holder.itemView.matchUserLocalTagCharacter.isVisible = false
            holder.itemView.matchUserDynamicThumbRv.isVisible = false
        } else if (!item.square.isNullOrEmpty()) {
            holder.itemView.matchUserLocalTagCl.isVisible = true
            holder.itemView.matchUserDynamicThumbRv.isVisible = true
            holder.itemView.matchUserLocalTagContent.isVisible = false
            holder.itemView.matchUserLocalTagCharacter.isVisible = false
        } else {
            holder.itemView.matchUserLocalTagCl.visibility = View.GONE
        }


        holder.itemView.btnHiLeftTime.text = "${UserManager.getLightingCount()}"
        GlideUtil.loadCircleImg(mContext, item.intention_icon, holder.itemView.matchAimIv)
        holder.itemView.matchUserLocalTagContent.text = item.sign ?: ""
        holder.itemView.matchBothIntersetLl.isVisible = !item.matching_content.isNullOrEmpty() //撮合兴趣
        holder.itemView.matchBothIntersetContent.text = "${item.matching_content}"
        GlideUtil.loadImg(mContext, item.matching_icon, holder.itemView.matchBothIntersetIv)


        if (!item.newtags.isNullOrEmpty()) {
            if (holder.itemView.matchUserLocalTagCharacter.layoutManager == null) {
                val manager = FlexboxLayoutManager(mContext, FlexDirection.ROW, FlexWrap.WRAP)
                manager.alignItems = AlignItems.STRETCH
                manager.justifyContent = JustifyContent.FLEX_START
                holder.itemView.matchUserLocalTagCharacter.layoutManager = manager
            }

            val adapter1 = MatchDetailLabelQualityAdapter()
            outFor@ for (quality in my_tags_quality) {
                for (quality1 in item.newtags ?: mutableListOf()) {
                    if (quality1.id == quality.id) {
                        adapter1.myTags = quality.label_quality
                        break@outFor
                    }
                }
            }
            adapter1.setNewData(item.newtags!![0].label_quality)
            holder.itemView.matchUserLocalTagCharacter.adapter = adapter1
            adapter1.setOnItemClickListener { adapter, view, position ->
                MatchDetailActivity.start(mContext, item.accid)
            }
        }

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