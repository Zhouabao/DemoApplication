package com.example.demoapplication.ui.adapter

import android.content.Context
import android.util.TypedValue
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.baselibrary.glide.GlideUtil
import com.example.demoapplication.R
import com.example.demoapplication.model.MatchBean
import com.example.demoapplication.model.SquareBean
import com.example.demoapplication.player.UpdateVoiceTimeThread
import com.example.demoapplication.ui.activity.SquarePlayDetailActivity
import com.kotlin.base.ext.onClick
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack
import kotlinx.android.synthetic.main.item_list_square_audio.view.*
import kotlinx.android.synthetic.main.item_list_square_pic.view.*
import kotlinx.android.synthetic.main.item_list_square_video.view.*
import kotlinx.android.synthetic.main.layout_square_list_bottom.view.*
import kotlinx.android.synthetic.main.layout_square_list_top.view.*
import org.jetbrains.anko.startActivity


/**
 *    author : ZFM
 *    date   : 2019/6/2616:27
 *    desc   : 多状态的广场
 *    version: 1.0
 */
class MultiListSquareAdapter(var context: Context, data: MutableList<SquareBean>) :
    BaseMultiItemQuickAdapter<SquareBean, BaseViewHolder>(data) {
    companion object {
        val TAG = "RecyclerView2List"
    }

    private val gsyVideoOptionBuilder by lazy { GSYVideoOptionBuilder() }

    init {
        addItemType(SquareBean.PIC, R.layout.item_list_square_pic)
        addItemType(SquareBean.VIDEO, R.layout.item_list_square_video)
        addItemType(SquareBean.AUDIO, R.layout.item_list_square_audio)
    }

    override fun convert(holder: BaseViewHolder, item: SquareBean) {
        val drawable1 =
            context.resources.getDrawable(if (item.isliked == 1) R.drawable.icon_dianzan_red else R.drawable.icon_dianzan)
        drawable1!!.setBounds(0, 0, drawable1.intrinsicWidth, drawable1.intrinsicHeight)    //需要设置图片的大小才能显示
        holder.itemView.squareDianzanBtn1.setCompoundDrawables(drawable1, null, null, null)

        //todo 点赞
        holder.addOnClickListener(R.id.squareDianzanBtn1)
        //点击转发
        holder.addOnClickListener(R.id.squareZhuanfaBtn1)
        //todo 评论
        holder.addOnClickListener(R.id.squareCommentBtn1)
        //todo 展开举报啊啥的
        holder.addOnClickListener(R.id.squareMoreBtn1)
        //todo 跳转到聊天界面
        holder.addOnClickListener(R.id.squareChatBtn1)

        //todo 此处要点击展开所有内容
        holder.itemView.squareContent1.initWidth(
            (ScreenUtils.getScreenWidth() - SizeUtils.applyDimension(
                27F,
                TypedValue.COMPLEX_UNIT_DIP
            )).toInt()
        )
//        holder.itemView.squareContent1.setOriginalText(item.descr ?: "")

        holder.itemView.squareContent1.setOriginalText("在蚂蚁金服中后台产品在蚂蚁金服中后台产品在蚂蚁金服中后台产品在蚂蚁金服中后台产品在蚂蚁金服中后台产品在蚂蚁金服中后台产品在蚂蚁金服中后台产品在蚂蚁金服中后台产品在蚂蚁金服中后台产品在蚂蚁金服中后台产品在蚂蚁金服中后台产品在蚂蚁金服中后台产品在蚂蚁金服中后台产品在蚂蚁金服中后台产品在蚂蚁金服中后台产品在蚂蚁金服中后台产品在蚂蚁金服中后台产品在蚂蚁金服中后台产品在蚂蚁金服中后台产品在蚂蚁金服中后台产品")
        holder.itemView.squareContent1.setHasAnimation(true)

        holder.itemView.squareDianzanBtn1.text = item.like_cnt.toString()
        holder.itemView.squareCommentBtn1.text = item.comment_cnt.toString()
        holder.itemView.squareUserVipIv1.visibility = if (item.isvip == 1) {
            View.VISIBLE
        } else {
            View.GONE
        }
        GlideUtil.loadAvatorImg(mContext, item.avatar ?: "", holder.itemView.squareUserIv1)
        holder.itemView.squareLocationAndTime1.text =
            (item.city_name ?: "").plus("\t").plus(
                item.out_time
//                if (!item.create_time.isNullOrEmpty()) {
//                    TimeUtils.getFitTimeSpan(TimeUtils.getNowString(), item.create_time, 2)
//                } else ""
            )

        when (holder.itemViewType) {
            MatchBean.PIC -> {
                if (item.photo_json != null && item.photo_json!!.size > 0) {
                    holder.itemView.squareUserPics1.visibility = View.VISIBLE
                    holder.itemView.squareUserPics1.layoutManager =
                        LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
                    val adapter = ListSquareImgsAdapter(context, item.photo_json ?: mutableListOf())
                    holder.itemView.squareUserPics1.adapter = adapter
                    adapter.setOnItemClickListener(object : ListSquareImgsAdapter.OnItemClickListener {
                        override fun onItemClick(position: Int) {
                            context.startActivity<SquarePlayDetailActivity>("item" to item)
                        }
                    })
                } else {
                    holder.itemView.squareUserPics1.visibility = View.GONE
                    holder.itemView.squareContent1.onClick {
                        context.startActivity<SquarePlayDetailActivity>("item" to item)
                    }
                }

            }
            MatchBean.VIDEO -> {
//                holder.itemView.squareUserVideo.onClick {
//                    context.startActivity<SquarePlayDetailActivity>()
//                }
                gsyVideoOptionBuilder.setIsTouchWiget(false)
//                    .setThumbImageView()
                    .setUrl("http://9890.vod.myqcloud.com/9890_4e292f9a3dd011e6b4078980237cc3d3.f20.mp4")
                    .setCacheWithPlay(false)
                    .setRotateViewAuto(true)
                    .setLockLand(false)
                    .setPlayTag(TAG)
                    .setPlayPosition(holder.layoutPosition)
                    .setShowFullAnimation(true)
                    .setNeedLockFull(true)
                    .setVideoAllCallBack(object : GSYSampleCallBack() {
                        override fun onPrepared(url: String?, vararg objects: Any?) {
                            super.onPrepared(url, *objects)
                            if (!holder.itemView.squareUserVideo.isIfCurrentIsFullscreen) {
                                //静音
                                GSYVideoManager.instance().isNeedMute = true
                            }
                        }

                        override fun onQuitFullscreen(url: String?, vararg objects: Any?) {
                            super.onQuitFullscreen(url, *objects)
                            //全屏不静音
                            GSYVideoManager.instance().isNeedMute = true
                        }

                        override fun onEnterFullscreen(url: String?, vararg objects: Any?) {
                            super.onEnterFullscreen(url, *objects)
                            GSYVideoManager.instance().isNeedMute = false
                        }
                    })
                    .build(holder.itemView.squareUserVideo)
                holder.itemView.squareUserVideo.backButton.visibility = View.GONE
                holder.itemView.squareUserVideo.fullscreenButton.onClick {
                    holder.itemView.squareUserVideo.startWindowFullscreen(context, false, true)
                }


            }
            MatchBean.AUDIO -> {
                //点击播放
                holder.addOnClickListener(R.id.audioPlayBtn)
                if (item.isPlayAudio) {
                    holder.itemView.voicePlayView.start()
                    UpdateVoiceTimeThread.getInstance("03:40", holder.itemView.audioTime).start()
                    holder.itemView.audioPlayBtn.setImageResource(R.drawable.icon_pause_audio)
                } else {
                    holder.itemView.voicePlayView.stop()
                    UpdateVoiceTimeThread.getInstance("03:40", holder.itemView.audioTime).pause()
                    holder.itemView.audioPlayBtn.setImageResource(R.drawable.icon_play_audio)
                }
                holder.itemView.squareUserAudio.onClick {
                    context.startActivity<SquarePlayDetailActivity>("item" to item)
                }
            }
        }

    }

}
