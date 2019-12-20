package com.sdy.jitangapplication.ui.adapter

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
import com.google.android.flexbox.*
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.MatchBean
import com.sdy.jitangapplication.model.Newtag
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
        //为了防止indicator重复 每次先给他remove了
        holder.addOnClickListener(R.id.v1)
        holder.itemView.vpIndicator.removeAllViews()
        holder.itemView.vpPhotos.setScrollable(false)
        holder.itemView.vpPhotos.currentItem = 0
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

                //首张内容  用户在该标签下的描述、标签内容
                //次张内容  用户在发布过的内容（所有标签），参考设计内容，如用户没发布过则跳转至第三张内容
                //三张内容  用户的「关于我」描述文本。如用用户填写过则呈现，没有则保留至最后一张显示状态
                when (position) {
                    0 -> {  //0显示个人信息
                        holder.itemView.matchUserLocalTagCl.isVisible = true
                        holder.itemView.matchUserDynamicLl.isVisible = false
                    }
                    else -> {//如果广场动态有，就显示广场
                        if (item.square.isNullOrEmpty()) {
                            holder.itemView.matchUserLocalTagCl.isVisible = true
                            holder.itemView.matchUserDynamicLl.isVisible = false
                        } else {
                            holder.itemView.matchUserDynamicLl.isVisible = true
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
        }

        //点击切换上一张图片
        holder.addOnClickListener(R.id.lastImgBtn)
        //点击切换下一张图片
        holder.addOnClickListener(R.id.nextImgBtn)


        holder.itemView.ivVip.isVisible = item.isvip == 1
        holder.itemView.ivVerify.isVisible = item.isfaced == 1


//        holder.itemView.matchUserIntroduce.text = item.sign ?: "" //关于自己
        holder.itemView.matchAim.isVisible = item.intention.isNotEmpty()//标签意向
        holder.itemView.matchAim.text = item.intention

        holder.itemView.matchUserLocalTagContent.isVisible = !item.sign.isNullOrEmpty()//标签介绍
        holder.itemView.matchUserLocalTagContent.text = item.sign ?: ""


        holder.itemView.matchBothIntersetLl.isVisible = !item.matching_content.isNullOrEmpty() //撮合标签
        holder.itemView.matchBothIntersetContent.text = "${item.matching_content}"
        GlideUtil.loadImg(mContext, item.matching_icon, holder.itemView.matchBothIntersetIv)

        if (!item.newtags.isNullOrEmpty()) {
            val manager = FlexboxLayoutManager(mContext, FlexDirection.ROW, FlexWrap.WRAP)
            manager.alignItems = AlignItems.STRETCH
            manager.justifyContent = JustifyContent.FLEX_START
            holder.itemView.matchUserLocalTagCharacter.layoutManager =
                LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)//标签下的特质标签
            val adapter1 = MatchDetailLabelQualityAdapter()
            outFor@ for (quality in my_tags_quality) {
                for (quality1 in item.newtags ?: mutableListOf()) {
                    if (quality1.id == quality.id) {
                        adapter1.myTags = quality.label_quality
                        break@outFor
                    }
                }
            }
            holder.itemView.matchUserLocalTagCharacterLl.isVisible = true
            holder.itemView.matchUserLocalTagName.text = item.newtags!![0].title
            adapter1.setNewData(item.newtags!![0].label_quality)
            holder.itemView.matchUserLocalTagCharacter.adapter = adapter1


            holder.itemView.matchUserLocalTagCharacter.setOnTouchListener { v, event ->
                v.parent.requestDisallowInterceptTouchEvent(true)
                false
            }
        } else {
            holder.itemView.matchUserLocalTagCharacterLl.isVisible = false
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