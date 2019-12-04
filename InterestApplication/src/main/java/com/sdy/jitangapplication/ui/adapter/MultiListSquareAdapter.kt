package com.sdy.jitangapplication.ui.adapter

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.ext.onClick
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.model.SquareBean
import com.sdy.jitangapplication.player.IjkMediaPlayerUtil
import com.sdy.jitangapplication.switchplay.SwitchUtil
import com.sdy.jitangapplication.ui.activity.MatchDetailActivity
import com.sdy.jitangapplication.ui.activity.SquareCommentDetailActivity
import com.sdy.jitangapplication.ui.activity.SquarePlayDetailActivity
import com.sdy.jitangapplication.ui.activity.SquarePlayListDetailActivity
import com.sdy.jitangapplication.ui.dialog.DeleteDialog
import com.sdy.jitangapplication.ui.dialog.MoreActionNewDialog
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.ui.dialog.TranspondDialog
import com.sdy.jitangapplication.ui.fragment.MySquareFragment
import com.sdy.jitangapplication.utils.UriUtils
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.DividerItemDecoration
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack
import kotlinx.android.synthetic.main.delete_dialog_layout.*
import kotlinx.android.synthetic.main.dialog_more_action_new.*
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
    var playPosition: Int = 0,
    var resetAudioListener: ResetAudioListener? = null,
    var type: Int = MySquareFragment.TYPE_SQUARE
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
        holder.itemView.view.isVisible = holder.layoutPosition - headerLayoutCount != 0

        //设置点赞状态
        setLikeStatus(item.isliked, item.like_cnt, holder.itemView.squareDianzanBtn1)
        //为自己，不能聊天（用户详情界面），未开启招呼，非好友   聊天按钮不可见
        if (item.isfriend)
            holder.itemView.squareChatBtn1.visibility = View.VISIBLE
        else
            if (UserManager.getAccid() == item.accid || !item.greet_switch || !chat) {
                holder.itemView.squareChatBtn1.visibility = View.INVISIBLE
            } else {
                holder.itemView.squareChatBtn1.visibility = View.VISIBLE
            }

        if (item.descr.isEmpty()) {
            holder.itemView.squareContent1.visibility = View.GONE
        } else {
            holder.itemView.squareContent1.visibility = View.VISIBLE
            holder.itemView.squareContent1.setContent(item.descr)
        }

        holder.itemView.squareUserName1.text = item.nickname ?: ""

        holder.itemView.squareCommentBtn1.text = "${item.comment_cnt}"
        holder.itemView.squareUserVipIv1.visibility = if (item.isvip == 1) {
            View.VISIBLE
        } else {
            View.GONE
        }
        GlideUtil.loadAvatorImg(mContext, item.avatar, holder.itemView.squareUserIv1)
        holder.itemView.squareLocationAndTime1.text = item.province_name.plus(
            if (item.city_name.isEmpty() || item.city_name == item.province_name || item.province_name.isNullOrEmpty()) {
                ""
            } else {
                "\t${item.city_name}"
            }
        ).plus("\t\t${item.out_time}")
//        holder.itemView.squareTime.text = "${item.out_time}"

        holder.itemView.squareTime.layoutManager = LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
        if (holder.itemView.squareTime.itemDecorationCount == 0) {
            holder.itemView.squareTime.addItemDecoration(
                DividerItemDecoration(
                    mContext,
                    DividerItemDecoration.VERTICAL_LIST,
                    SizeUtils.dp2px(6F),
                    mContext.resources.getColor(R.color.colorWhite)
                )
            )
        }
        val squareAdapter = SquareTagAdapter()
        holder.itemView.squareTime.adapter = squareAdapter
        squareAdapter.setNewData(item.tags ?: mutableListOf())

        //点击跳转评论详情
        holder.itemView.squareCommentBtn1.onClick {
            if (resetAudioListener != null) {
                resetAudioListener!!.resetAudioState()
            }
            SquareCommentDetailActivity.start(
                mContext!!,
                data[holder.layoutPosition - headerLayoutCount],
                enterPosition = "comment",
                position = holder.layoutPosition - headerLayoutCount
            )
        }
        //更多弹窗
        holder.itemView.squareMoreBtn1.onClick {
            showMoreDialog(holder.layoutPosition - headerLayoutCount)
        }
        //点击转发
        holder.itemView.squareZhuanfaBtn1.onClick {
            showTranspondDialog(item)
        }

        //进入聊天界面
        holder.itemView.squareChatBtn1.onClick {
            if (resetAudioListener != null) {
                resetAudioListener!!.resetAudioState()
            }
            CommonFunction.commonGreet(
                mContext,
                item.isfriend,
                item.greet_switch,
                item.greet_state,
                item.accid,
                item.nickname ?: "",
                item.isgreeted,
                holder.itemView.squareChatBtn1
            )

        }

        //点赞
        holder.itemView.squareDianzanBtn1.onClick {
            clickZan(holder.itemView.squareDianzanBtn1, holder.layoutPosition - headerLayoutCount)
        }



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
                        if (resetAudioListener != null) {
                            resetAudioListener!!.resetAudioState()
                        }
                        mContext.startActivity<SquarePlayListDetailActivity>(
                            "item" to item,
                            "picPosition" to position
                        )
                    }
                } else {
                    holder.itemView.squareUserPics1.visibility = View.GONE
                }

            }
            SquareBean.VIDEO -> {
                //增加封面
                val imageview = ImageView(mContext)
                GlideUtil.loadRoundImgCenterCrop(mContext, item.cover_url ?: "", imageview, SizeUtils.dp2px(15F))
                if (imageview.parent != null) {
                    val vg = imageview.parent as ViewGroup
                    vg.removeView(imageview)
                }
                holder.itemView.squareUserVideo.thumbImageView = imageview
                holder.itemView.squareUserVideo.detail_btn.setOnClickListener {
                    if (holder.itemView.squareUserVideo.isInPlayingState) {
                        SwitchUtil.savePlayState(holder.itemView.squareUserVideo)
                        holder.itemView.squareUserVideo.gsyVideoManager.setLastListener(holder.itemView.squareUserVideo)
                        SquarePlayDetailActivity.startActivity(
                            mContext as Activity,
                            holder.itemView.squareUserVideo,
                            item,
                            holder.layoutPosition,
                            type
                        )
                    }
                }
                holder.itemView.squareUserVideo.playTag = TAG
                holder.itemView.squareUserVideo.playPosition = holder.layoutPosition
                holder.itemView.squareUserVideo.setVideoAllCallBack(object : GSYSampleCallBack() {
                    override fun onStartPrepared(url: String?, vararg objects: Any?) {
                        super.onStartPrepared(url, *objects)
                        if (resetAudioListener != null) {
                            resetAudioListener!!.resetAudioState()
                        }
                    }

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
                val audioTimeView = holder.itemView.audioTime

                if (item.isPlayAudio == IjkMediaPlayerUtil.MEDIA_PLAY) { //播放中
                    holder.itemView.voicePlayView.playAnimation()

                    audioTimeView.startTime((item.audio_json?.get(0)?.leftTime ?: 0).toLong(), "3")
                    holder.itemView.audioPlayBtn.setImageResource(R.drawable.icon_pause_audio)
                } else if (item.isPlayAudio == IjkMediaPlayerUtil.MEDIA_PAUSE) {//暂停中
                    holder.itemView.voicePlayView.pauseAnimation()
                    audioTimeView.stopTime()
                    item.audio_json?.get(0)?.leftTime = UriUtils.stringToTimeInt(audioTimeView.text.toString())
                    holder.itemView.audioPlayBtn.setImageResource(R.drawable.icon_play_audio)
                } else if (item.isPlayAudio == IjkMediaPlayerUtil.MEDIA_STOP || item.isPlayAudio == IjkMediaPlayerUtil.MEDIA_ERROR) {//停止中
                    audioTimeView.stopTime()
                    item.audio_json?.get(0)?.leftTime = item.audio_json?.get(0)?.duration ?: 0
                    audioTimeView.text = UriUtils.getShowTime(item.audio_json?.get(0)?.leftTime ?: 0)

                    holder.itemView.voicePlayView.pauseAnimation()
                    holder.itemView.voicePlayView.cancelAnimation()
                    holder.itemView.audioPlayBtn.setImageResource(R.drawable.icon_play_audio)
                } else if (item.isPlayAudio == IjkMediaPlayerUtil.MEDIA_PREPARE) {
                    audioTimeView.stopTime()
                    item.audio_json?.get(0)?.leftTime = item.audio_json?.get(0)?.duration ?: 0
                    audioTimeView.text = UriUtils.getShowTime(item.audio_json?.get(0)?.leftTime ?: 0)

                    holder.itemView.voicePlayView.pauseAnimation()
                    holder.itemView.voicePlayView.cancelAnimation()

                    holder.itemView.audioPlayBtn.setImageResource(R.drawable.icon_play_audio)
                }
                holder.itemView.audioRecordLl.onClick {
                    if (resetAudioListener != null) {
                        resetAudioListener!!.resetAudioState()
                    }
                    mContext.startActivity<SquarePlayListDetailActivity>("item" to item, "from" to "squareFragment")
                }
            }
        }

    }


    /**
     * 设置点赞状态
     */
    private fun setLikeStatus(isliked: Int, likeCount: Int, likeView: TextView) {
        val drawable1 =
            mContext.resources.getDrawable(if (isliked == 1) R.drawable.icon_dianzan_red else R.drawable.icon_dianzan)
        drawable1!!.setBounds(0, 0, drawable1.intrinsicWidth, drawable1.intrinsicHeight)    //需要设置图片的大小才能显示
        likeView.setCompoundDrawables(drawable1, null, null, null)
        likeView.text = "${if (likeCount < 0) {
            0
        } else {
            likeCount
        }}"
    }

    /**
     * 点赞按钮
     */
    private fun clickZan(likeBtn: TextView, position: Int) {
        if (data[position].isliked == 1) {
            data[position].isliked = 0
            data[position].like_cnt = data[position].like_cnt!!.minus(1)
        } else {
            data[position].isliked = 1
            data[position].like_cnt = data[position].like_cnt!!.plus(1)
        }
        setLikeStatus(data[position].isliked, data[position].like_cnt, likeBtn)

        likeBtn.postDelayed({
            if (data[position].originalLike == data[position].isliked) {
                return@postDelayed
            }
            val params = hashMapOf(
                "token" to SPUtils.getInstance(Constants.SPNAME).getString("token"),
                "accid" to SPUtils.getInstance(Constants.SPNAME).getString("accid"),
                "type" to if (data[position].isliked == 0) {
                    2
                } else {
                    1
                },
                "square_id" to data[position].id!!,
                "_timestamp" to System.currentTimeMillis()
            )
            getSquareLike(params, position)
        }, 2000L)

    }


    /**
     * 点赞 取消点赞
     * 1 点赞 2取消点赞
     */
    fun getSquareLike(params: HashMap<String, Any>, position: Int) {
        RetrofitFactory.instance.create(Api::class.java)
            .getSquareLike(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(null) {
                override fun onNext(t: BaseResp<Any?>) {
                    super.onNext(t)
                    if (t.code == 200) {
                        onGetSquareLikeResult(position, true)
                    } else if (t.code == 403) {
                        TickDialog(mContext).show()
                    } else {
                        CommonFunction.toast(t.msg)
                        onGetSquareLikeResult(position, false)
                    }

                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(mContext).show()
                    } else {
                        CommonFunction.toast(mContext.getString(R.string.service_error))
                    }
                }
            })
    }

    /**
     * 点赞结果
     */
    private fun onGetSquareLikeResult(position: Int, success: Boolean) {
        if (success) {
            data[position].originalLike = data[position].isliked
        } else {
            data[position].isliked = data[position].originalLike
            data[position].like_cnt = data[position].originalLikeCount
//            refreshNotifyItemChanged(position)

        }
    }


    /**
     * 展示转发动态对话框
     */
    private fun showTranspondDialog(squareBean: SquareBean) {
        val transpondDialog = TranspondDialog(mContext, squareBean)
        transpondDialog.show()
//        transpondDialog.squareBean = squareBean
    }


    lateinit var moreActionDialog: MoreActionNewDialog
    /**
     * 展示更多操作对话框
     */
    private fun showMoreDialog(position: Int) {
        moreActionDialog = MoreActionNewDialog(mContext, data[position])
        moreActionDialog.show()

        if (data[position]?.iscollected == 0) {
            moreActionDialog.collect.text = "收藏"
            val top = mContext.resources.getDrawable(R.drawable.icon_collect1)
            moreActionDialog.collect.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null)
        } else {
            moreActionDialog.collect.text = "取消收藏"
            val top = mContext.resources.getDrawable(R.drawable.icon_collected1)
            moreActionDialog.collect.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null)
        }
        if (data[position].accid == UserManager.getAccid()) {
            moreActionDialog.delete.visibility = View.VISIBLE
            moreActionDialog.report.visibility = View.GONE
            moreActionDialog.collect.visibility = View.GONE
        } else {
            moreActionDialog.delete.visibility = View.GONE
            moreActionDialog.report.visibility = View.VISIBLE
            moreActionDialog.collect.visibility = View.VISIBLE
        }
        moreActionDialog.delete.onClick {
            val params = hashMapOf(
                "accid" to SPUtils.getInstance(Constants.SPNAME).getString("accid"),
                "token" to SPUtils.getInstance(Constants.SPNAME).getString("token"),
                "square_id" to data[position].id!!
            )
            removeMySquare(params, position)
            moreActionDialog.dismiss()

        }


        moreActionDialog.collect.onClick {

            //发起收藏请求
            val params = hashMapOf(
                "accid" to SPUtils.getInstance(Constants.SPNAME).getString("accid"),
                "token" to SPUtils.getInstance(Constants.SPNAME).getString("token"),
                "type" to if (data[position].iscollected == 0) {
                    1
                } else {
                    2
                },
                "square_id" to data[position].id!!,
                "_timestamp" to System.currentTimeMillis()
            )
            getSquareCollect(params, position)
        }
        moreActionDialog.report.onClick {
            val dialog = DeleteDialog(mContext)
            dialog.show()
            dialog.title.text = "动态举报"
            dialog.tip.text = mContext.getString(R.string.report_square)
            dialog.confirm.text = "举报"
            dialog.cancel.onClick { dialog.dismiss() }
            dialog.confirm.onClick {
                dialog.dismiss()
                //发起举报请求
                val params = hashMapOf(
                    "accid" to SPUtils.getInstance(Constants.SPNAME).getString("accid"),
                    "token" to SPUtils.getInstance(Constants.SPNAME).getString("token"),
                    "type" to if (data[position].iscollected == 0) {
                        1
                    } else {
                        2
                    },
                    "square_id" to data[position].id!!,
                    "_timestamp" to System.currentTimeMillis()
                )
                getSquareReport(params, position)
            }
            moreActionDialog.dismiss()


        }
//        moreActionDialog.cancel.onClick {
//            moreActionDialog.dismiss()
//        }

    }


    /**
     * 广场删除
     */
    fun removeMySquare(params: HashMap<String, Any>, position: Int) {

        RetrofitFactory.instance.create(Api::class.java)
            .removeMySquare(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(null) {
                override fun onNext(t: BaseResp<Any?>) {
                    super.onNext(t)
                    if (t.code == 200)
                        onRemoveMySquareResult(true, position)
                    else if (t.code == 403) {
                        UserManager.startToLogin(mContext as Activity)
                    } else {
                        CommonFunction.toast(t.msg)
                        onRemoveMySquareResult(false, position)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(mContext).show()
                    } else {
                        CommonFunction.toast(mContext.getString(R.string.service_error))
                        onRemoveMySquareResult(false, position)
                    }
                }
            })
    }


    /**
     * 收藏
     * 1 收藏 2取消收藏
     */
    fun getSquareCollect(params: HashMap<String, Any>, position: Int) {
        RetrofitFactory.instance.create(Api::class.java)
            .getSquareCollect(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(null) {
                override fun onNext(t: BaseResp<Any?>) {
                    super.onNext(t)
                    if (t.code == 200)
                        onGetSquareCollectResult(position, t)
                    else if (t.code == 403) {
                        TickDialog(mContext).show()
                    } else {
                        onGetSquareCollectResult(position, t)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(mContext).show()
                    } else {
                        CommonFunction.toast(mContext.getString(R.string.service_error))
                        onGetSquareCollectResult(position, null)
                    }
                }
            })
    }


    /**
     * 广场举报
     */
    fun getSquareReport(params: HashMap<String, Any>, position: Int) {
        RetrofitFactory.instance.create(Api::class.java)
            .getSquareReport(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(null) {
                override fun onNext(t: BaseResp<Any?>) {
                    super.onNext(t)
                    if (t.code == 200)
                        onGetSquareReport(t, position)
                    else if (t.code == 403) {
                        TickDialog(mContext).show()
                    } else {
                        onGetSquareReport(t, position)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(mContext).show()
                    } else {
                        CommonFunction.toast(mContext.getString(R.string.service_error))
                        onGetSquareReport(null, position)
                    }
                }
            })
    }

    fun onRemoveMySquareResult(result: Boolean, position: Int) {
        if (result) {
            if (data[position].type == SquareBean.AUDIO && resetAudioListener != null) {
                resetAudioListener!!.resetAudioState()
            } else if (data[position].type == SquareBean.VIDEO) {
                GSYVideoManager.releaseAllVideos()
            }
            data.removeAt(position)
            notifyItemRemoved(position + headerLayoutCount)
        }
    }


    fun onGetSquareCollectResult(position: Int, data1: BaseResp<Any?>?) {
        if (data1 != null) {
            CommonFunction.toast(data1.msg)
            if (data1.code == 200) {
                if (data[position].iscollected == 1) {
                    data[position].iscollected = 0
                } else {
                    data[position].iscollected = 1
                }
//                refreshNotifyItemChanged(position)
            }
        }
        if (moreActionDialog != null && moreActionDialog.isShowing) {
            moreActionDialog.dismiss()
        }
    }

    fun onGetSquareReport(baseResp: BaseResp<Any?>?, position: Int) {
        if (baseResp != null)
            CommonFunction.toast(baseResp.msg)
        if (moreActionDialog != null && moreActionDialog.isShowing) {
            moreActionDialog.dismiss()
        }
    }

    interface ResetAudioListener {
        fun resetAudioState()
    }


}
