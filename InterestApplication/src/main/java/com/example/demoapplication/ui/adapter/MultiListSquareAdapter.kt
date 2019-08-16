package com.example.demoapplication.ui.adapter

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.baselibrary.glide.GlideUtil
import com.example.demoapplication.R
import com.example.demoapplication.model.SquareBean
import com.example.demoapplication.player.IjkMediaPlayerUtil
import com.example.demoapplication.player.UpdateVoiceTimeThread
import com.example.demoapplication.switchplay.SwitchUtil
import com.example.demoapplication.ui.activity.MatchDetailActivity
import com.example.demoapplication.ui.activity.SquarePlayDetailActivity
import com.example.demoapplication.ui.activity.SquarePlayListDetailActivity
import com.example.demoapplication.utils.UriUtils
import com.example.demoapplication.utils.UserManager
import com.kotlin.base.ext.onClick
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack
import kotlinx.android.synthetic.main.item_list_square_audio.view.*
import kotlinx.android.synthetic.main.item_list_square_pic.view.*
import kotlinx.android.synthetic.main.item_list_square_video.view.*
import kotlinx.android.synthetic.main.layout_square_list_bottom.view.*
import kotlinx.android.synthetic.main.layout_square_list_top.view.*
import kotlinx.android.synthetic.main.switch_video.view.*
import org.jetbrains.anko.startActivity


/**
 *    author : ZFM
 *    date   : 2019/6/2616:27
 *    desc   : 多状态的广场
 *    version: 1.0
 *     playState:Int = -1  //0停止  1播放中  2暂停
 *     playPosition播放的进度
 */
class MultiListSquareAdapter(
    data: MutableList<SquareBean>,
    var playState: Int = -1,
    var playPosition: Int = 0
) :
    BaseMultiItemQuickAdapter<SquareBean, BaseViewHolder>(data) {
    companion object {
        val TAG = "RecyclerView2List"
    }

    var chat: Boolean = true

    init {
        addItemType(SquareBean.PIC, R.layout.item_list_square_pic)
        addItemType(SquareBean.VIDEO, R.layout.item_list_square_video)
        addItemType(SquareBean.AUDIO, R.layout.item_list_square_audio)
    }

    override fun convert(holder: BaseViewHolder, item: SquareBean) {
        val drawable1 =
            mContext.resources.getDrawable(if (item.isliked == 1) R.drawable.icon_dianzan_red else R.drawable.icon_dianzan)
        drawable1!!.setBounds(0, 0, drawable1.intrinsicWidth, drawable1.intrinsicHeight)    //需要设置图片的大小才能显示
        holder.itemView.squareDianzanBtn1.setCompoundDrawables(drawable1, null, null, null)

        if (UserManager.getAccid() == item.accid || !chat) {
            holder.itemView.squareChatBtn1.visibility = View.INVISIBLE
        } else {
            holder.itemView.squareChatBtn1.visibility = View.VISIBLE
        }

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
        if (item.descr.isNullOrEmpty()) {
            holder.itemView.squareContent1.visibility = View.GONE
        } else {
            holder.itemView.squareContent1.visibility = View.VISIBLE
            holder.itemView.squareContent1.setContent(item.descr ?: "")
        }

        holder.itemView.squareUserName1.text = item.nickname ?: ""

        holder.itemView.squareDianzanBtn1.text = "${item.like_cnt}"
        holder.itemView.squareCommentBtn1.text = "${item.comment_cnt}"
        holder.itemView.squareUserVipIv1.visibility = if (item.isvip == 1) {
            View.VISIBLE
        } else {
            View.GONE
        }
        GlideUtil.loadAvatorImg(mContext, item.avatar ?: "", holder.itemView.squareUserIv1)
        holder.itemView.squareLocationAndTime1.text = (item.city_name ?: "").plus("\t\t").plus(item.out_time)
        holder.itemView.squareUserIv1.onClick {
            if (!(UserManager.getAccid() == item.accid || !chat)) {
                MatchDetailActivity.start(mContext, item.accid)

            }
        }

        when (holder.itemViewType) {
            SquareBean.PIC -> {
                if (item.photo_json != null && item.photo_json!!.size > 0) {
                    holder.itemView.squareUserPics1.visibility = View.VISIBLE
                    holder.itemView.squareUserPics1.layoutManager =
                        LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
                    val adapter = ListSquareImgsAdapter(mContext, item.photo_json ?: mutableListOf())
                    holder.itemView.squareUserPics1.adapter = adapter
                    adapter.setOnItemClickListener { adapter, view, position ->
                        mContext.startActivity<SquarePlayListDetailActivity>("item" to item)
                    }
                } else {
                    holder.itemView.squareUserPics1.visibility = View.GONE
                    holder.itemView.onClick {
                        mContext.startActivity<SquarePlayListDetailActivity>("item" to item)
                    }
                }

            }
            SquareBean.VIDEO -> {
                //增加封面
                val imageview = ImageView(mContext)
                imageview.scaleType = ImageView.ScaleType.CENTER_INSIDE
                GlideUtil.loadImg(mContext, item.cover_url ?: "", imageview)
                if (imageview.parent != null) {
                    val vg = imageview.parent as ViewGroup
                    vg.removeView(imageview)
                }
                holder.itemView.squareUserVideo.thumbImageView = imageview

                holder.itemView.squareUserVideo.detail_btn.setOnClickListener {
                    SwitchUtil.savePlayState(holder.itemView.squareUserVideo)
                    holder.itemView.squareUserVideo.gsyVideoManager.setLastListener(holder.itemView.squareUserVideo)
                    SquarePlayDetailActivity.startActivity(
                        mContext as Activity,
                        holder.itemView.squareUserVideo,
                        item,
                        holder.layoutPosition
                    )
                }
                holder.itemView.squareUserVideo.playTag = TAG
                holder.itemView.squareUserVideo.playPosition = holder.layoutPosition
                holder.itemView.squareUserVideo.setVideoAllCallBack(object : GSYSampleCallBack() {
                    override fun onPrepared(url: String?, vararg objects: Any?) {
                        if (!holder.itemView.squareUserVideo.isIfCurrentIsFullscreen) {
                            //静音
                            GSYVideoManager.instance().isNeedMute = true
                        }
                    }

                    override fun onQuitFullscreen(url: String?, vararg objects: Any?) {
                        super.onQuitFullscreen(url, *objects)
                        //退出全屏静音
                        GSYVideoManager.instance().isNeedMute = true
                    }

                    override fun onEnterFullscreen(url: String?, vararg objects: Any?) {
                        super.onEnterFullscreen(url, *objects)
                        GSYVideoManager.instance().isNeedMute = false
                    }

                })

                SwitchUtil.optionPlayer(
                    holder.itemView.squareUserVideo,
                    item.video_json?.get(0)?.url ?: "",
                    true
                )
                holder.itemView.squareUserVideo.setUp(
                    item.video_json?.get(0)?.url ?: "",
                    false,
                    null,
                    null,
                    ""
                )
            }

            SquareBean.AUDIO -> {
                //点击播放
                holder.addOnClickListener(R.id.audioPlayBtn)
                if (item.isPlayAudio == IjkMediaPlayerUtil.MEDIA_PLAY) { //播放中
                    holder.itemView.voicePlayView.start()
                    UpdateVoiceTimeThread.getInstance(
                        item.audio_json?.get(0)?.duration?.let { UriUtils.getShowTime(it) },
                        holder.itemView.audioTime
                    ).start()
                    holder.itemView.audioPlayBtn.setImageResource(R.drawable.icon_pause_audio)
                } else if (item.isPlayAudio == IjkMediaPlayerUtil.MEDIA_PAUSE) {//暂停中
                    holder.itemView.voicePlayView.stop()
                    UpdateVoiceTimeThread.getInstance(
                        item.audio_json?.get(0)?.duration?.let { UriUtils.getShowTime(it) },
                        holder.itemView.audioTime
                    ).pause()
                    holder.itemView.audioPlayBtn.setImageResource(R.drawable.icon_play_audio)
                } else if (item.isPlayAudio == IjkMediaPlayerUtil.MEDIA_STOP || item.isPlayAudio == IjkMediaPlayerUtil.MEDIA_ERROR) {//停止中
                    holder.itemView.voicePlayView.stop()
                    UpdateVoiceTimeThread.getInstance(
                        item.audio_json?.get(0)?.duration?.let { UriUtils.getShowTime(it) },
                        holder.itemView.audioTime
                    ).stop()
                    holder.itemView.audioPlayBtn.setImageResource(R.drawable.icon_play_audio)
                } else if (item.isPlayAudio == IjkMediaPlayerUtil.MEDIA_PREPARE) {
                    holder.itemView.voicePlayView.stop()
                    UpdateVoiceTimeThread.getInstance(
                        item.audio_json?.get(0)?.duration?.let { UriUtils.getShowTime(it) },
                        holder.itemView.audioTime
                    ).stop()
                    holder.itemView.audioPlayBtn.setImageResource(R.drawable.icon_play_audio)
                }
                holder.itemView.squareUserAudio.onClick {
                    mContext.startActivity<SquarePlayListDetailActivity>("item" to item, "from" to "squareFragment")
                }
            }
        }

    }

}
