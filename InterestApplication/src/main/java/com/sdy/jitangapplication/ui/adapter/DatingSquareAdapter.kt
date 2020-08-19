package com.sdy.jitangapplication.ui.adapter

import android.graphics.Color
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.SpanUtils
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.model.DatingBean
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.MyDatingAudioView
import kotlinx.android.synthetic.main.item_layout_dating_square_man.view.*
import kotlinx.android.synthetic.main.item_layout_dating_square_woman.view.*


/**
 * 约会适配器
 */
class DatingSquareAdapter : BaseMultiItemQuickAdapter<DatingBean, BaseViewHolder>(mutableListOf()) {

    val myAudioView by lazy { mutableListOf<MyDatingAudioView?>() }

    init {
        addItemType(DatingBean.TYPE_WOMAN, R.layout.item_layout_dating_square_woman)
        addItemType(DatingBean.TYPE_MAN, R.layout.item_layout_dating_square_man)
    }

    override fun convert(helper: BaseViewHolder, item: DatingBean) {
        when (helper.itemViewType) {
            DatingBean.TYPE_WOMAN -> {
                val itemview = helper.itemView
                val params = itemview.layoutParams as RecyclerView.LayoutParams
                params.width = ScreenUtils.getScreenWidth() - SizeUtils.dp2px(15 * 2F)
                params.height = params.width
                itemview.layoutParams = params
                GlideUtil.loadRoundImgCenterCrop(
                    mContext,
                    item.avatar,
                    itemview.datingAvatorWoman,
                    SizeUtils.dp2px(10F)
                )


                //content_type 1 文本 2 语音
                if (item.content_type == 1) {
                    itemview.datingContentAudioWoman.isVisible = false
                    itemview.datingContentTextWoman.isVisible = true
                    itemview.datingContentTextWoman.text = item.content
                    myAudioView.add(null)
                } else {
                    itemview.datingContentAudioWoman.isVisible = true
                    itemview.datingContentTextWoman.isVisible = false
                    itemview.datingContentAudioWoman.setUi(
                        R.drawable.shape_rectangle_gray4df_10dp,
                        audioTip = "点击播放活动语音描述"
                    )
                    itemview.datingContentAudioWoman.prepareAudio(
                        item.content,
                        item.duration,
                        helper.layoutPosition
                    )
                    myAudioView.add(itemview.datingContentAudioWoman)
                }
                itemview.datingNameWoman.text = item.nickname
                itemview.datingVipWoman.isVisible = item.isplatinumvip
                itemview.datingOnlineDistanceWoman.text = "${item.online_time}\t${item.distance}"
                SpanUtils.with(itemview.datingProjectWoman)
                    .append(
                        "${item.title}${if (item!!.dating_title.isNotEmpty()) {
                            "·"
                        } else {
                            ""
                        }}"
                    )
                    .append(item.dating_title)
                    .setForegroundColor(Color.parseColor("#FFFF6318"))
                    .create()
                itemview.datingPlaceWoman.text = if (item.dating_distance.isNullOrEmpty()) {
                    "无明确要求"
                } else {
                    item.dating_distance
                }
                itemview.datingObjectWoman.text =
                    "${if (item.private_chat_state.isNotEmpty()) {
                        "${item.private_chat_state} | "
                    } else {
                        ""
                    }}${item.dating_target} | ${item.cost_type} | ${item.cost_money}"
                itemview.datingPlanWoman.text = item.follow_up
                if (item.accid != UserManager.getAccid()) {
                    itemview.datingApplyForWomanBtn.isVisible = true
                } else {
                    itemview.datingApplyForWomanBtn.visibility = View.INVISIBLE
                }
                itemview.datingApplyForWomanBtn.clickWithTrigger {
                    CommonFunction.checkApplyForDating(mContext, item)
                }

            }
            DatingBean.TYPE_MAN -> {
                val itemview = helper.itemView
                GlideUtil.loadCircleImg(mContext, item.avatar, itemview.datingAvator)
                //content_type 1 文本 2 语音
                if (item.content_type == 1) {
                    itemview.datingContentAudio.isVisible = false
                    itemview.datingContentText.isVisible = true
                    itemview.datingContentText.text = item.content
                    myAudioView.add(null)
                } else {
                    itemview.datingContentAudio.isVisible = true
                    itemview.datingContentText.isVisible = false
                    itemview.datingContentAudio.setUi(
                        R.drawable.shape_rectangle_gray4df_10dp,
                        audioTip = "点击播放活动语音描述"
                    )
                    itemview.datingContentAudio.prepareAudio(
                        item.content,
                        item.duration,
                        item.id
                    )
                    myAudioView.add(itemview.datingContentAudio)

                }
                itemview.datingName.text = item.nickname
                itemview.datingVip.isVisible = item.isplatinumvip
                itemview.datingOnlineDistance.text = "${item.online_time}\t${item.distance}"
                SpanUtils.with(itemview.datingProject)
                    .append(
                        "${item.title}${if (item!!.dating_title.isNotEmpty()) {
                            "·"
                        } else {
                            ""
                        }}"
                    )
                    .append(item.dating_title)
                    .setForegroundColor(Color.parseColor("#FFFF6318"))
                    .create()
                itemview.datingPlace.text = if (item.dating_distance.isNullOrEmpty()) {
                    "无明确要求"
                } else {
                    item.dating_distance
                }
                itemview.datingObject.text =
                    "${if (item.private_chat_state.isNotEmpty()) {
                        "${item.private_chat_state} | "
                    } else {
                        ""
                    }}${item.dating_target} | ${item.cost_type} | ${item.cost_money}"
                itemview.datingPlan.text = item.follow_up
                if (item.accid != UserManager.getAccid()) {
                    itemview.datingApplyForBtn.isVisible = true
                } else {
                    itemview.datingApplyForBtn.visibility = View.INVISIBLE
                }
                itemview.datingApplyForBtn.clickWithTrigger {
                    CommonFunction.checkApplyForDating(mContext, item)
                }
            }
        }

    }

    fun resetMyAudioViews() {
        for (myaudio in myAudioView) {
            myaudio?.releaseAudio()
        }
    }

    fun notifySomeOneAudioView(positionId: Int) {
        for (myaudio in myAudioView.withIndex()) {
            if (myaudio?.value?.positionId != positionId) {
                myaudio?.value?.releaseAudio()
            }
        }
    }


}