package com.sdy.jitangapplication.ui.adapter

import android.app.Activity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.ext.onClick
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.netease.nim.uikit.business.session.module.Container
import com.netease.nim.uikit.business.session.module.ModuleProxy
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.msg.MessageBuilder
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.event.UpdateHiCountEvent
import com.sdy.jitangapplication.model.GreetBean
import com.sdy.jitangapplication.model.SquareBean
import com.sdy.jitangapplication.model.StatusBean
import com.sdy.jitangapplication.nim.activity.ChatActivity
import com.sdy.jitangapplication.nim.attachment.ChatHiAttachment
import com.sdy.jitangapplication.player.IjkMediaPlayerUtil
import com.sdy.jitangapplication.switchplay.SwitchUtil
import com.sdy.jitangapplication.ui.activity.MatchDetailActivity
import com.sdy.jitangapplication.ui.activity.SquarePlayDetailActivity
import com.sdy.jitangapplication.ui.activity.SquarePlayListDetailActivity
import com.sdy.jitangapplication.ui.dialog.ChargeVipDialog
import com.sdy.jitangapplication.ui.dialog.CountDownChatHiDialog
import com.sdy.jitangapplication.ui.dialog.HarassmentDialog
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UriUtils
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.DividerItemDecoration
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack
import kotlinx.android.synthetic.main.item_list_square_audio.view.*
import kotlinx.android.synthetic.main.item_list_square_pic.view.*
import kotlinx.android.synthetic.main.item_list_square_video.view.*
import kotlinx.android.synthetic.main.layout_square_list_bottom.view.*
import kotlinx.android.synthetic.main.layout_square_list_top.view.*
import kotlinx.android.synthetic.main.switch_video.view.*
import org.greenrobot.eventbus.EventBus
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
    var resetAudioListener: ResetAudioListener? = null
) :
    BaseMultiItemQuickAdapter<SquareBean, BaseViewHolder>(data), ModuleProxy {
    companion object {
        val TAG = "RecyclerView2List"
    }

    var chat: Boolean = true

    init {
        addItemType(SquareBean.PIC, R.layout.item_list_square_pic)
        addItemType(SquareBean.VIDEO, R.layout.item_list_square_video)
        addItemType(SquareBean.AUDIO, R.layout.item_list_square_audio)
    }

    private var clickPos = -1

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

        holder.addOnClickListener(R.id.squareDianzanBtn1)
        //点击转发
        holder.addOnClickListener(R.id.squareZhuanfaBtn1)
        holder.addOnClickListener(R.id.squareCommentBtn1)
        holder.addOnClickListener(R.id.squareMoreBtn1)
//        holder.addOnClickListener(R.id.squareChatBtn1)


        //todo 进入聊天界面
        holder.itemView.squareChatBtn1.onClick {
            if (resetAudioListener != null) {
                resetAudioListener!!.resetAudioState()
            }
            clickPos = holder.layoutPosition - headerLayoutCount
            greetState(UserManager.getToken(), UserManager.getAccid(), item.accid, holder.itemView.squareChatBtn1)
        }

        if (item.descr.isEmpty()) {
            holder.itemView.squareContent1.visibility = View.GONE
        } else {
            holder.itemView.squareContent1.visibility = View.VISIBLE
            holder.itemView.squareContent1.setContent(item.descr)
        }

        holder.itemView.squareUserName1.text = item.nickname ?: ""

        holder.itemView.squareDianzanBtn1.text = "${if (item.like_cnt < 0) {
            0
        } else {
            item.like_cnt
        }}"
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
                GlideUtil.loadRoundImgCenterCrop(mContext, item.cover_url ?: "", imageview, 0)
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
                            holder.layoutPosition
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


    /*----------------------------打招呼请求逻辑--------------------------------*/
//todo  这里要判断是不是VIP用户 如果是VIP 直接进入聊天界面
    //1.首先判断是否有次数，
    // 若有 就打招呼
    // 若无 就弹充值
    /**
     * 判断当前能否打招呼
     */
    fun greetState(token: String, accid: String, target_accid: String, btn: ImageView) {
        if (!NetworkUtils.isConnected()) {
            CommonFunction.toast("请连接网络！")
            return
        }
        RetrofitFactory.instance.create(Api::class.java)
            .greetState(token, accid, target_accid)
            .excute(object : BaseSubscriber<BaseResp<GreetBean?>>(null) {
                override fun onStart() {

                    btn.isEnabled = false
                }

                override fun onNext(t: BaseResp<GreetBean?>) {
                    if (t.code == 200) {
                        val greetBean = t.data
                        if (greetBean != null && greetBean.lightningcnt != -1) {
                            if (greetBean.isfriend || greetBean.isgreet) {
                                ChatActivity.start(mContext as Activity, target_accid ?: "")
                                clickPos = -1
                            } else {
                                UserManager.saveLightingCount(greetBean.lightningcnt)
                                UserManager.saveCountDownTime(greetBean.countdown)
                                if (greetBean.lightningcnt > 0) {
                                    greet(UserManager.getToken(), UserManager.getAccid(), (target_accid ?: ""))
                                } else {
                                    if (UserManager.isUserVip()) {
                                        CountDownChatHiDialog(mContext).show()
                                    } else {
                                        ChargeVipDialog(ChargeVipDialog.DOUBLE_HI, mContext).show()
                                    }
                                }
                            }
                        } else {
                            CommonFunction.toast(t.msg)
                        }
                    } else {
                        CommonFunction.toast(t.msg)
                    }
                    btn.isEnabled = true
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(mContext).show()
                    }
                    btn.isEnabled = true
                }
            })
    }

    /** todo
     *  点击聊天
     *  1. 好友 直接聊天 已经匹配过了 ×
     *
     *  2. 不是好友 判断是否打过招呼
     *
     *     2.1 打过招呼 且没有过期  直接直接聊天
     *
     *     2.2 未打过招呼 判断招呼剩余次数
     *
     *         2.2.1 有次数 直接打招呼
     *
     *         2.2.2 无次数 其他操作--如:请求充值会员
     */

    /**
     * 打招呼
     */
    fun greet(token: String, accid: String, target_accid: String) {
        if (!NetworkUtils.isConnected()) {
            CommonFunction.toast("请连接网络！")
            return
        }
        RetrofitFactory.instance.create(Api::class.java)
            .greet(token, accid, target_accid, UserManager.getGlobalLabelId())
            .excute(object : BaseSubscriber<BaseResp<StatusBean?>>(null) {
                override fun onNext(t: BaseResp<StatusBean?>) {
                    if (t.code == 200) {
                        onGreetSResult(true)
                    } else if (t.code == 403) {
                        UserManager.startToLogin(mContext as Activity)
                    } else if (t.code == 401) {
                        HarassmentDialog(mContext, HarassmentDialog.CHATHI).show()
                    } else {
                        CommonFunction.toast(t.msg)
                    }
                }

                override fun onError(e: Throwable?) {
                    CommonFunction.toast(mContext.getString(R.string.service_error))
                }
            })
    }

    /**
     * 打招呼结果（先请求服务器）
     */
    fun onGreetSResult(greetBean: Boolean) {
        if (greetBean) {
            sendChatHiMessage(mData[clickPos])
        }
    }

    /*--------------------------消息代理------------------------*/

    private fun sendChatHiMessage(squareBean: SquareBean) {
        Log.d("OkHttp", "${squareBean.accid}====================")
        val container = Container(mContext as Activity, squareBean?.accid, SessionTypeEnum.P2P, this, true)
        val chatHiAttachment = ChatHiAttachment(
            UserManager.getGlobalLabelName(),
            ChatHiAttachment.CHATHI_HI
        )
        val message = MessageBuilder.createCustomMessage(
            squareBean.accid,
            SessionTypeEnum.P2P,
            "",
            chatHiAttachment,
            CustomMessageConfig()
        )
        container.proxy.sendMessage(message)
    }

    override fun sendMessage(msg: IMMessage): Boolean {
        NIMClient.getService(MsgService::class.java).sendMessage(msg, false).setCallback(object :
            RequestCallback<Void?> {
            override fun onSuccess(param: Void?) {
                ChatActivity.start(mContext as Activity, mData[clickPos].accid ?: "")
                //发送通知修改招呼次数
                UserManager.saveLightingCount(UserManager.getLightingCount() - 1)
                EventBus.getDefault().postSticky(UpdateHiCountEvent())
                clickPos = -1
            }

            override fun onFailed(code: Int) {

            }

            override fun onException(exception: Throwable) {

            }
        })
        return true
    }

    override fun onInputPanelExpand() {

    }

    override fun shouldCollapseInputPanel() {

    }

    override fun isLongClickEnabled(): Boolean {
        return false
    }

    override fun onItemFooterClick(message: IMMessage?) {

    }


    interface ResetAudioListener {
        fun resetAudioState()
    }
}
