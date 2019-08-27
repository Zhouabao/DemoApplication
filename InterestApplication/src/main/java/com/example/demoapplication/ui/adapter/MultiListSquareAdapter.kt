package com.example.demoapplication.ui.adapter

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieComposition
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.baselibrary.glide.GlideUtil
import com.example.demoapplication.R
import com.example.demoapplication.api.Api
import com.example.demoapplication.event.UpdateHiCountEvent
import com.example.demoapplication.model.GreetBean
import com.example.demoapplication.model.SquareBean
import com.example.demoapplication.model.StatusBean
import com.example.demoapplication.nim.activity.ChatActivity
import com.example.demoapplication.nim.attachment.ChatHiAttachment
import com.example.demoapplication.player.IjkMediaPlayerUtil
import com.example.demoapplication.player.UpdateVoiceTimeThread
import com.example.demoapplication.switchplay.SwitchUtil
import com.example.demoapplication.ui.activity.MatchDetailActivity
import com.example.demoapplication.ui.activity.SquarePlayDetailActivity
import com.example.demoapplication.ui.activity.SquarePlayListDetailActivity
import com.example.demoapplication.ui.dialog.ChargeVipDialog
import com.example.demoapplication.ui.dialog.TickDialog
import com.example.demoapplication.utils.UriUtils
import com.example.demoapplication.utils.UserManager
import com.kotlin.base.common.BaseApplication.Companion.context
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
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack
import kotlinx.android.synthetic.main.item_list_square_pic.view.*
import kotlinx.android.synthetic.main.item_list_square_video.view.*
import kotlinx.android.synthetic.main.layout_record_audio.view.*

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
class MultiListSquareAdapter(data: MutableList<SquareBean>, var playState: Int = -1, var playPosition: Int = 0) :
    BaseMultiItemQuickAdapter<SquareBean, BaseViewHolder>(data), ModuleProxy {

    private val composition: LottieComposition? = null


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
            clickPos = holder.layoutPosition
            greetState(UserManager.getToken(), UserManager.getAccid(), item.accid)
        }

        if (item.descr.isNullOrEmpty()) {
            holder.itemView.squareContent1.visibility = View.GONE
        } else {
            holder.itemView.squareContent1.visibility = View.VISIBLE
            holder.itemView.squareContent1.setContent(item.descr)
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
        holder.itemView.squareLocationAndTime1.text = (item.city_name ?: "").plus(
            if (item.city_name.isNullOrEmpty()) {
                ""
            } else {
                "\t\t"
            }
        ).plus(item.out_time)
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
                holder.itemView.voicePlayView.setAnimation("yinzhu.json")

                if (item.isPlayAudio == IjkMediaPlayerUtil.MEDIA_PLAY) { //播放中
                    if (!holder.itemView.voicePlayView.isAnimating) {
                        holder.itemView.voicePlayView.loop(true)
                        holder.itemView.voicePlayView.playAnimation()
                    } else {
                        holder.itemView.voicePlayView.resumeAnimation()
                    }

                    UpdateVoiceTimeThread.getInstance(
                        item.audio_json?.get(0)?.duration?.let { UriUtils.getShowTime(it) },
                        holder.itemView.audioTime
                    ).start()
                    holder.itemView.audioPlayBtn.setImageResource(R.drawable.icon_pause_audio)
                } else if (item.isPlayAudio == IjkMediaPlayerUtil.MEDIA_PAUSE) {//暂停中
                    holder.itemView.voicePlayView.pauseAnimation()
                    UpdateVoiceTimeThread.getInstance(
                        item.audio_json?.get(0)?.duration?.let { UriUtils.getShowTime(it) },
                        holder.itemView.audioTime
                    ).pause()
                    holder.itemView.audioPlayBtn.setImageResource(R.drawable.icon_play_audio)
                } else if (item.isPlayAudio == IjkMediaPlayerUtil.MEDIA_STOP || item.isPlayAudio == IjkMediaPlayerUtil.MEDIA_ERROR) {//停止中
                    holder.itemView.voicePlayView.loop(false)
                    holder.itemView.voicePlayView.cancelAnimation()

                    UpdateVoiceTimeThread.getInstance(
                        item.audio_json?.get(0)?.duration?.let { UriUtils.getShowTime(it) },
                        holder.itemView.audioTime
                    ).stop()
                    holder.itemView.audioPlayBtn.setImageResource(R.drawable.icon_play_audio)
                } else if (item.isPlayAudio == IjkMediaPlayerUtil.MEDIA_PREPARE) {
                    holder.itemView.voicePlayView.loop(false)
                    holder.itemView.voicePlayView.cancelAnimation()

                    UpdateVoiceTimeThread.getInstance(
                        item.audio_json?.get(0)?.duration?.let { UriUtils.getShowTime(it) },
                        holder.itemView.audioTime
                    ).stop()
                    holder.itemView.audioPlayBtn.setImageResource(R.drawable.icon_play_audio)
                }
                holder.itemView.audioRecordLl.onClick {
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
    fun greetState(token: String, accid: String, target_accid: String) {
        if (!NetworkUtils.isConnected()) {
            ToastUtils.showShort("请连接网络！")
            return
        }
        RetrofitFactory.instance.create(Api::class.java)
            .greetState(token, accid, target_accid)
            .excute(object : BaseSubscriber<BaseResp<GreetBean?>>(null) {
                override fun onNext(t: BaseResp<GreetBean?>) {
                    if (t.code == 200) {
                        val greetBean = t.data
                        if (greetBean != null) {
                            if (greetBean.isfriend || greetBean.isgreet) {
                                ChatActivity.start(mContext as Activity, target_accid ?: "")
                            } else {
                                UserManager.saveLightingCount(greetBean.lightningcnt)
                                if (greetBean.lightningcnt > 0) {
                                    greet(UserManager.getToken(), UserManager.getAccid(), (target_accid ?: ""))
                                } else {
                                    if (UserManager.isUserVip()) {
                                        //TODO 会员充值
                                        ToastUtils.showShort("次数用尽，请充值。")
                                    } else {
                                        ChargeVipDialog(mContext).show()
                                    }
                                }
                            }
                        } else {
                            ToastUtils.showShort(t.msg)
                        }
                    } else {
                        ToastUtils.showShort(t.msg)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(mContext).show()
                    }
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
            ToastUtils.showShort("请连接网络！")
            return
        }
        RetrofitFactory.instance.create(Api::class.java)
            .greet(token, accid, target_accid, UserManager.getGlobalLabelId())
            .excute(object : BaseSubscriber<BaseResp<StatusBean?>>(null) {
                override fun onNext(t: BaseResp<StatusBean?>) {
                    if (t.code == 200) {
                        onGreetSResult(true)
                    } else if (t.code == 403) {
                        UserManager.startToLogin(context as Activity)
                    } else {
                        onGreetSResult(false)
                    }
                }

                override fun onError(e: Throwable?) {
                    ToastUtils.showShort(mContext.getString(R.string.service_error))
                }
            })
    }

    /**
     * 打招呼结果（先请求服务器）
     */
    fun onGreetSResult(greetBean: Boolean) {
        if (greetBean) {
            sendChatHiMessage(mData[clickPos])
        } else {
            ToastUtils.showShort("打招呼失败，重新试一次吧")
        }
    }

    /*--------------------------消息代理------------------------*/

    private fun sendChatHiMessage(squareBean: SquareBean) {
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
                EventBus.getDefault().postSticky(UpdateHiCountEvent())
            }

            override fun onFailed(code: Int) {
                ToastUtils.showShort("$code")
            }

            override fun onException(exception: Throwable) {
                ToastUtils.showShort(exception.message ?: "")
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

}
