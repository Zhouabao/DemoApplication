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
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.GreetedListBean
import com.sdy.jitangapplication.player.IjkMediaPlayerUtil
import com.sdy.jitangapplication.player.OnPlayingListener
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
        helper.itemView.matchAimTv.text = item.intention_title
        GlideUtil.loadCircleImg(mContext, item.intention_icon, helper.itemView.matchAimIv)

        helper.itemView.matchBothIntersetLl.isVisible = !item.matching_content.isNullOrEmpty()
        helper.itemView.matchBothIntersetContent.text = item.matching_content


        helper.itemView.rvChatContent.layoutManager = LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
        val adapter = ChatContentAdapter()
        adapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.audioPlayBtn -> {
                    val squareBean = adapter.data[position]
                    if (currPlayIndex != position && squareBean.isPlayAudio != IjkMediaPlayerUtil.MEDIA_PLAY) {
                        initAudio(helper.layoutPosition, position)
                        mediaPlayer!!.setDataSource(squareBean.content).prepareMedia()
                        currPlayIndex = position
                    }
                    for (index in 0 until adapter.data.size) {
                        if (index != currPlayIndex && adapter.data[index].type == 3) {
                            adapter.data[index].isPlayAudio = IjkMediaPlayerUtil.MEDIA_STOP
                        }
                    }
                    if (squareBean.isPlayAudio == IjkMediaPlayerUtil.MEDIA_PREPARE || squareBean.isPlayAudio == IjkMediaPlayerUtil.MEDIA_ERROR) {
                        mediaPlayer!!.startPlay()
                    } else if (squareBean.isPlayAudio == IjkMediaPlayerUtil.MEDIA_PAUSE) {
                        mediaPlayer!!.resumePlay()
                    } else if (squareBean.isPlayAudio == IjkMediaPlayerUtil.MEDIA_PLAY) {
                        mediaPlayer!!.pausePlay()
                    }
                    adapter.notifyDataSetChanged()
                }
            }
        }


        helper.itemView.rvChatContent.onFlingListener = null
        PagerSnapHelper().attachToRecyclerView(helper.itemView.rvChatContent)
        adapter.setNewData(item.send_msg)
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


    var mediaPlayer: IjkMediaPlayerUtil? = null
    fun initAudio(parent: Int, position: Int) {
        resetAudio()
        mediaPlayer = IjkMediaPlayerUtil(mContext!!, position, object : OnPlayingListener {

            override fun onPlay(position: Int) {
                mData[parent].send_msg[position].isPlayAudio = IjkMediaPlayerUtil.MEDIA_PLAY
                refreshNotifyItemChanged(parent)

            }

            override fun onPause(position: Int) {
                mData[parent].send_msg[position].isPlayAudio = IjkMediaPlayerUtil.MEDIA_PAUSE
                refreshNotifyItemChanged(parent)
            }

            override fun onStop(position: Int) {
                mData[parent].send_msg[position].isPlayAudio = IjkMediaPlayerUtil.MEDIA_STOP
                refreshNotifyItemChanged(parent)
                resetAudio()
            }

            override fun onError(position: Int) {
                mData[parent].send_msg[position].isPlayAudio = IjkMediaPlayerUtil.MEDIA_ERROR
                refreshNotifyItemChanged(parent)
                CommonFunction.toast("音频播放出错")
                resetAudio()
            }

            override fun onPrepared(position: Int) {
                mediaPlayer!!.startPlay()
            }

            override fun onPreparing(position: Int) {
                mData[parent].send_msg[position].isPlayAudio = IjkMediaPlayerUtil.MEDIA_PREPARE
                refreshNotifyItemChanged(parent)
            }

            override fun onRelease(position: Int) {
                mData[parent].send_msg[position].isPlayAudio = IjkMediaPlayerUtil.MEDIA_STOP
                refreshNotifyItemChanged(parent)

            }

        }).getInstance()
    }

    private var currPlayIndex = -1
    fun resetAudio() {
        currPlayIndex = -1
        if (mediaPlayer != null) {
            mediaPlayer!!.resetMedia()
            mediaPlayer = null
        }
    }

}
