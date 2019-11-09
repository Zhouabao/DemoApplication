package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.SizeUtils
import com.kennyc.view.MultiStateView
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.netease.nim.uikit.business.session.module.Container
import com.netease.nim.uikit.business.session.module.ModuleProxy
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.msg.MessageBuilder
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.RefreshCommentEvent
import com.sdy.jitangapplication.event.RefreshLikeEvent
import com.sdy.jitangapplication.event.RefreshSquareEvent
import com.sdy.jitangapplication.event.UpdateHiCountEvent
import com.sdy.jitangapplication.model.AllCommentBean
import com.sdy.jitangapplication.model.CommentBean
import com.sdy.jitangapplication.model.GreetBean
import com.sdy.jitangapplication.model.SquareBean
import com.sdy.jitangapplication.nim.activity.ChatActivity
import com.sdy.jitangapplication.nim.attachment.ChatHiAttachment
import com.sdy.jitangapplication.player.IjkMediaPlayerUtil
import com.sdy.jitangapplication.player.OnPlayingListener
import com.sdy.jitangapplication.player.UpdateVoiceTimeThread
import com.sdy.jitangapplication.presenter.SquareDetailPresenter
import com.sdy.jitangapplication.presenter.view.SquareDetailView
import com.sdy.jitangapplication.switchplay.SwitchUtil
import com.sdy.jitangapplication.ui.adapter.ListSquareImgsAdapter
import com.sdy.jitangapplication.ui.adapter.MultiListCommentAdapter
import com.sdy.jitangapplication.ui.adapter.SquareTagAdapter
import com.sdy.jitangapplication.ui.dialog.*
import com.sdy.jitangapplication.utils.UriUtils
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.DividerItemDecoration
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack
import com.shuyu.gsyvideoplayer.utils.GSYVideoType
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView
import com.umeng.socialize.UMShareAPI
import kotlinx.android.synthetic.main.activity_square_comment_detail.*
import kotlinx.android.synthetic.main.delete_dialog_layout.*
import kotlinx.android.synthetic.main.dialog_comment_action.*
import kotlinx.android.synthetic.main.dialog_more_action_new.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import kotlinx.android.synthetic.main.layout_record_audio.*
import kotlinx.android.synthetic.main.layout_square_list_bottom.*
import kotlinx.android.synthetic.main.layout_square_list_top.*
import kotlinx.android.synthetic.main.switch_video.view.*
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.startActivity


/**
 * 广场详情页 包含内容详情以及点赞评论信息
 */
class SquareCommentDetailActivity : BaseMvpActivity<SquareDetailPresenter>(), SquareDetailView, View.OnClickListener,
    OnRefreshListener, OnLoadMoreListener, ModuleProxy {
    private val TAG = SquareCommentDetailActivity::class.java.simpleName

    //评论数据
    private var commentDatas: MutableList<CommentBean> = mutableListOf()
    private val adapter: MultiListCommentAdapter by lazy { MultiListCommentAdapter(this, commentDatas) }

    private var squareBean: SquareBean? = null

    //是否改变了点赞的状态
    private var isChangeLike = false

    //通过标志进入的入口来决定是否弹起键盘
    private val enterPosition: String? by lazy { intent.getStringExtra("enterPosition") }

    private var page = 1

    private val commentParams = hashMapOf(
        "token" to UserManager.getToken(),
        "accid" to UserManager.getAccid(),
        "square_id" to "",
        "page" to page,
        "pagesize" to Constants.PAGESIZE
    )

    companion object {
        fun start(
            context: Context,
            squareBean: SquareBean? = null,
            squareId: Int? = null,
            enterPosition: String? = null,
            position: Int? = 0
        ) {
            context.startActivity<SquareCommentDetailActivity>(
                if (squareBean != null) {
                    "squareBean" to squareBean
                } else {
                    "" to ""
                },
                if (squareId != null) {
                    "square_id" to squareId
                } else {
                    "" to ""
                },
                if (enterPosition != null) {
                    "enterPosition" to enterPosition
                } else {
                    "" to ""
                },
                "position" to position
            )
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_square_comment_detail)
        initView()
        if (intent.getSerializableExtra("squareBean") != null) {
            stateview.viewState = MultiStateView.VIEW_STATE_CONTENT
            squareBean = intent.getSerializableExtra("squareBean") as SquareBean
            initData()
            commentParams["square_id"] = "${squareBean!!.id}"
            mPresenter.getCommentList(commentParams, true)
        } else {
            mPresenter.getSquareInfo(
                hashMapOf(
                    "token" to UserManager.getToken(),
                    "accid" to UserManager.getAccid(),
                    "square_id" to intent.getIntExtra("square_id", 0)
                )
            )
        }

    }

    private fun initData() {

        when {
            squareBean!!.type == 1 -> {
                squareUserPics.visibility = View.VISIBLE
                initPics()
            }
            squareBean!!.type == 2 -> {
                squareUserVideo.visibility = View.VISIBLE
                initVideo()
            }
            else -> {
                audioRecordLl.isVisible = true
//                initAudio()
                initAudio(0)
                mediaPlayer!!.setDataSource(squareBean!!.audio_json?.get(0)?.url ?: "").prepareMedia()
            }
        }


        GlideUtil.loadAvatorImg(this, squareBean!!.avatar ?: "", squareUserIv1)


        squareDianzanBtn1.setCompoundDrawablesWithIntrinsicBounds(
            resources.getDrawable(if (squareBean!!.isliked == 1) R.drawable.icon_dianzan_red else R.drawable.icon_dianzan),
            null,
            null,
            null
        )

        squareDianzanBtn1.text = "${if (squareBean!!.like_cnt < 0) {
            0
        } else {
            squareBean!!.like_cnt
        }}"
        squareCommentBtn1.text = "${squareBean!!.comment_cnt}"
        squareContent1.setContent("${squareBean!!.descr}")
        squareZhuanfaBtn1.text = "${squareBean!!.share_cnt}"
        squareUserName1.text = "${squareBean!!.nickname}"
        squareUserVipIv1.isVisible = squareBean!!.isvip == 1

        if (squareBean!!.isfriend)
            squareChatBtn1.isVisible = true
        else
            squareChatBtn1.isVisible =
                !(UserManager.getAccid() == squareBean!!.accid || !squareBean!!.greet_switch)
        squareChatBtn1.onClick {
            squareChatBtn1.isEnabled = false
            mPresenter.greetState(UserManager.getToken(), UserManager.getAccid(), squareBean?.accid ?: "")
        }
        squareUserIv1.onClick {
            if ((squareBean?.accid ?: "") != UserManager.getAccid())
                MatchDetailActivity.start(this, squareBean?.accid ?: "")
        }

        squareLocationAndTime1.text =
            "${squareBean!!.province_name}${if (!squareBean!!.province_name.isNullOrEmpty() && squareBean!!.city_name.isNotEmpty() && squareBean!!.city_name != squareBean!!.province_name) {
                "\t${squareBean!!.city_name}"
            } else {
                ""
            }}".plus("\t\t${squareBean!!.out_time}")


        squareTime.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        squareTime.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL_LIST,
                SizeUtils.dp2px(6F),
                resources.getColor(R.color.colorWhite)
            )
        )
        val squareAdapter = SquareTagAdapter()
        squareTime.adapter = squareAdapter
        squareAdapter.setNewData(squareBean!!.tags ?: mutableListOf())
//        squareTime.text = "\t\t${squareBean!!.out_time}"

    }

    private fun initView() {
        mPresenter = SquareDetailPresenter()
        mPresenter.mView = this
        mPresenter.context = this

        stateview.retryBtn.onClick {
            stateview.viewState = MultiStateView.VIEW_STATE_LOADING
            if (intent.getSerializableExtra("squareBean") != null) {
                stateview.viewState = MultiStateView.VIEW_STATE_CONTENT
                squareBean = intent.getSerializableExtra("squareBean") as SquareBean
                initData()
                commentParams["square_id"] = "${squareBean!!.id}"
                mPresenter.getCommentList(commentParams, true)
            } else {
                mPresenter.getSquareInfo(
                    hashMapOf(
                        "token" to UserManager.getToken(),
                        "accid" to UserManager.getAccid(),
                        "square_id" to intent.getIntExtra("square_id", 0)
                    )
                )
            }
        }


        refreshLayout.setOnRefreshListener(this)
        refreshLayout.setOnLoadMoreListener(this)

        commentList.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        commentList.adapter = adapter
        adapter.setEmptyView(R.layout.empty_layout_comment, commentList)

        btnBack.onClick {
            onBackPressed()
        }
        hotT1.text = "动态详情"


        squareZhuanfaBtn1.setOnClickListener(this)
        squareDianzanBtn1.setOnClickListener(this)
        squareCommentBtn1.setOnClickListener(this)
        squareMoreBtn1.setOnClickListener(this)
        sendCommentBtn.setOnClickListener(this)

        showCommentEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(charSequence: Editable?) {
                if (charSequence.toString().isNotEmpty()) {
                    sendCommentBtn.isEnabled = true
                    sendCommentBtn.setBackgroundResource(R.drawable.icon_send_enable)
                } else {
                    sendCommentBtn.isEnabled = false
                    sendCommentBtn.setBackgroundResource(R.drawable.icon_send_unable)
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
        })


        adapter.setOnItemLongClickListener { adapter, view, position ->
            showCommentDialog(position)
            true
        }

        adapter.setOnItemClickListener { _, view, position ->
            reply = true
            reply_id = adapter.data[position].id!!.toInt()
            showCommentEt.isFocusable = true
            showCommentEt.hint = "『回复\t${adapter.data[position].nickname}：』"
            KeyboardUtils.showSoftInput(showCommentEt)
        }


        adapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.commentUser -> {
                    if ((adapter.data[position].member_accid ?: "") != UserManager.getAccid())
                        MatchDetailActivity.start(this, adapter.data[position].member_accid ?: "")
//                    reply = true
//                    reply_id = adapter.data[position].reply_id!!
//                    showCommentEt.isFocusable = true
//                    showCommentEt.hint = "『回复\t${adapter.data[position].replyed_nickname}：』"
//                    KeyboardUtils.showSoftInput(showCommentEt)
                }
                R.id.llCommentDianzanBtn -> {
                    mPresenter.getCommentLike(
                        hashMapOf(
                            "token" to UserManager.getToken(),
                            "accid" to UserManager.getAccid(),
                            "reply_id" to adapter.data[position].id!!,
                            "type" to if (adapter.data[position].isliked == 0) {
                                1
                            } else {
                                2
                            }
                        )
                        , position
                    )

                }
                R.id.commentReplyBtn -> {
                    reply = true
                    reply_id = adapter.data[position].id!!
                    showCommentEt.hint = "『回复${adapter.data[position].replyed_nickname}：』"
                    KeyboardUtils.showSoftInput(showCommentEt)
                }
            }
        }
    }


    var mediaPlayer: IjkMediaPlayerUtil? = null

    private fun initAudio(position: Int) {
        if (mediaPlayer != null) {
            mediaPlayer!!.resetMedia()
            mediaPlayer = null
        }
        mediaPlayer = IjkMediaPlayerUtil(this, position, object : OnPlayingListener {
            override fun onPlay(position: Int) {
                squareBean!!.isPlayAudio = IjkMediaPlayerUtil.MEDIA_PLAY
                voicePlayView.playAnimation()
                UpdateVoiceTimeThread.getInstance(
                    squareBean!!.audio_json?.get(0)?.duration?.let { UriUtils.getShowTime(it) },
                    audioTime
                ).start()
                audioPlayBtn.setImageResource(R.drawable.icon_pause_audio)
            }

            override fun onPause(position: Int) {
                squareBean!!.isPlayAudio = IjkMediaPlayerUtil.MEDIA_PAUSE
                voicePlayView.cancelAnimation()
                UpdateVoiceTimeThread.getInstance(
                    squareBean!!.audio_json?.get(0)?.duration?.let { UriUtils.getShowTime(it) },
                    audioTime
                ).pause()
                audioPlayBtn.setImageResource(R.drawable.icon_play_audio)
            }

            override fun onStop(position: Int) {
                squareBean!!.isPlayAudio = IjkMediaPlayerUtil.MEDIA_STOP
                voicePlayView.cancelAnimation()
                UpdateVoiceTimeThread.getInstance(
                    squareBean!!.audio_json?.get(0)?.duration?.let { UriUtils.getShowTime(it) },
                    audioTime
                ).stop()
                audioPlayBtn.setImageResource(R.drawable.icon_play_audio)

            }

            override fun onError(position: Int) {
                CommonFunction.toast("音频播放出错")
                squareBean!!.isPlayAudio = IjkMediaPlayerUtil.MEDIA_ERROR
                voicePlayView.cancelAnimation()
                UpdateVoiceTimeThread.getInstance(
                    squareBean!!.audio_json?.get(0)?.duration?.let { UriUtils.getShowTime(it) },
                    audioTime
                ).stop()
                audioPlayBtn.setImageResource(R.drawable.icon_play_audio)
                mediaPlayer!!.resetMedia()
            }

            override fun onPrepared(position: Int) {
                mediaPlayer!!.startPlay()
            }

            override fun onPreparing(position: Int) {
                voicePlayView.cancelAnimation()
                UpdateVoiceTimeThread.getInstance(
                    squareBean!!.audio_json?.get(0)?.duration?.let { UriUtils.getShowTime(it) },
                    audioTime
                ).stop()
                audioPlayBtn.setImageResource(R.drawable.icon_play_audio)
            }

            override fun onRelease(position: Int) {
//                squareBean!!.isPlayAudio = IjkMediaPlayerUtil.MEDIA_STOP
//                voicePlayView.stop()
//                UpdateVoiceTimeThread.getInstance("03:40", audioTime).stop()
//                audioPlayBtn.setImageResource(R.drawable.icon_play_audio)
//                mediaPlayer!!.resetMedia()
//                mediaPlayer = null
            }

        }).getInstance()

        audioPlayBtn.setOnClickListener {
            when (squareBean!!.isPlayAudio) {
                IjkMediaPlayerUtil.MEDIA_ERROR -> {
                    initAudio(0)
                    mediaPlayer!!.setDataSource(squareBean!!.audio_json?.get(0)?.url ?: "").prepareMedia()
                }
                IjkMediaPlayerUtil.MEDIA_PREPARE -> {//准备中
                    mediaPlayer!!.prepareMedia()
                }
                IjkMediaPlayerUtil.MEDIA_STOP -> {//停止就重新准备
                    initAudio(0)
                    mediaPlayer!!.setDataSource(squareBean!!.audio_json?.get(0)?.url ?: "").prepareMedia()
                }
                IjkMediaPlayerUtil.MEDIA_PLAY -> {//播放点击就暂停
                    mediaPlayer!!.pausePlay()
                }
                IjkMediaPlayerUtil.MEDIA_PAUSE -> {//暂停再次点击就播放
                    mediaPlayer!!.resumePlay()
                }
            }
        }
    }


    /**
     * 初始化播放视频
     */
    private fun initVideo() {
        squareUserVideo.detail_btn.onClick {
            if (squareUserVideo.isInPlayingState) {
                SwitchUtil.savePlayState(squareUserVideo)
                squareUserVideo.gsyVideoManager.setLastListener(squareUserVideo)
                SquarePlayDetailActivity.startActivity(this, squareUserVideo, squareBean!!, 0)
            }
        }

        SwitchUtil.optionPlayer(squareUserVideo, squareBean!!.video_json?.get(0)?.url ?: "", true)
        squareUserVideo.setUp(squareBean!!.video_json?.get(0)?.url ?: "", false, null, null, "")
//        squareUserVideo.setSwitchUrl(squareBean!!.video_json?.get(0)?.url ?: "")
//        squareUserVideo.setSwitchCache(false)
//        squareUserVideo.setUp(squareBean!!.video_json?.get(0)?.url ?: "", false, "")
        squareUserVideo.startPlayLogic()
    }


    /**
     * 初始化图片列表
     */
    private val imgsAdapter by lazy { ListSquareImgsAdapter(this, squareBean!!.photo_json ?: mutableListOf()) }

    private fun initPics() {
        if (squareBean!!.photo_json != null && squareBean!!.photo_json!!.size > 0) {
            squareUserPics.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
            squareUserPics.adapter = imgsAdapter
            imgsAdapter.setOnItemClickListener { _, view, position ->
                startActivity<BigImageActivity>(
                    BigImageActivity.IMG_KEY to squareBean!!,
                    BigImageActivity.IMG_POSITION to position
                )
            }
        }
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        page++
        commentParams["page"] = page
        mPresenter.getCommentList(commentParams, false)
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        page = 1
        adapter.data.clear()
        commentParams["page"] = page
        mPresenter.getCommentList(commentParams, true)

    }


    override fun onGetSquareInfoResults(data: SquareBean?) {
        stateview.viewState = MultiStateView.VIEW_STATE_CONTENT
        if (data != null) {
            data.type = when {
                !data.video_json.isNullOrEmpty() -> SquareBean.VIDEO
                !data.audio_json.isNullOrEmpty() -> SquareBean.AUDIO
                !data.photo_json.isNullOrEmpty() ||
                        (data.photo_json.isNullOrEmpty() && data.audio_json.isNullOrEmpty() && data.video_json.isNullOrEmpty()) -> SquareBean.PIC
                else -> SquareBean.PIC
            }
            squareBean = data
            initData()
            commentParams["square_id"] = "${squareBean!!.id}"
            mPresenter.getCommentList(commentParams, true)
        } else {
            CommonFunction.toast("该动态已经被删除了")
            finish()
        }
    }

    override fun onGetCommentListResult(allCommentBean: AllCommentBean?, refresh: Boolean) {
        if (refresh) {
            refreshLayout.setNoMoreData(false)
            if (allCommentBean != null) {
                if (allCommentBean.hotlist != null && allCommentBean.hotlist!!.size > 0) {
                    adapter.addData(CommentBean(content = "热门评论", type = 0))
                    for (i in 0 until allCommentBean.hotlist!!.size) {
                        allCommentBean.hotlist!![i]!!.type = 1
                    }
                    adapter.addData(allCommentBean.hotlist!!)
                }
                if (allCommentBean.list != null && allCommentBean.list!!.size > 0) {
                    adapter.addData(CommentBean(content = "所有评论", type = 0))
                    for (i in 0 until allCommentBean.list!!.size) {
                        allCommentBean.list!![i]!!.type = 1
                    }
                    adapter.addData(allCommentBean.list!!)
                }
            }
            refreshLayout.finishRefresh(true)
        } else {
            if (allCommentBean != null) {
                if ((allCommentBean.hotlist == null || allCommentBean.hotlist!!.size == 0) && (allCommentBean.list == null || allCommentBean.list!!.size == 0)) {
                    refreshLayout.finishLoadMoreWithNoMoreData()
                    return
                }

                if (allCommentBean.hotlist != null && allCommentBean.hotlist!!.size > 0) {
                    for (i in 0 until allCommentBean.hotlist!!.size) {
                        allCommentBean.hotlist!![i]!!.type = 1
                    }
                    adapter.addData(allCommentBean.hotlist!!)
                }
                if (allCommentBean.list != null && allCommentBean.list!!.size > 0) {
                    for (i in 0 until allCommentBean.list!!.size) {
                        allCommentBean.list!![i]!!.type = 1
                    }
                    adapter.addData(allCommentBean.list!!)
                }
            }
            refreshLayout.finishLoadMore(true)
        }
    }

    override fun onGetSquareCollectResult(data: BaseResp<Any?>?) {
        if (data != null) {
            CommonFunction.toast(data.msg)
            if (data.code == 200) {
                squareBean!!.iscollected = if (squareBean!!.iscollected == 1) {
                    0
                } else {
                    1
                }
                EventBus.getDefault().post(RefreshSquareEvent(true, TAG))
            }
        }
    }

    override fun onGetSquareLikeResult(result: Boolean) {
        if (result) {
            squareBean!!.isliked = if (squareBean!!.isliked == 0) {
                squareBean!!.like_cnt = squareBean!!.like_cnt?.plus(1)
                1
            } else {
                squareBean!!.like_cnt = squareBean!!.like_cnt?.minus(1)
                0
            }
            squareDianzanBtn1.text = "${squareBean!!.like_cnt}"
            squareDianzanBtn1.setCompoundDrawablesWithIntrinsicBounds(
                resources.getDrawable(if (squareBean!!.isliked == 1) R.drawable.icon_dianzan_red else R.drawable.icon_dianzan),
                null,
                null,
                null
            )

            EventBus.getDefault()
                .post(RefreshLikeEvent(squareBean?.isliked ?: 0, intent.getIntExtra("position", -1)))
//            EventBus.getDefault().post(RefreshSquareEvent(true, TAG))
        }
    }

    override fun onGetSquareReport(data: BaseResp<Any?>?) {
        if (data != null)
            CommonFunction.toast(data.msg)
    }


    override fun onAddCommentResult(data: BaseResp<Any?>?, result: Boolean) {
        resetCommentEt()
        if (result) {
            page = 1
            adapter.data.clear()
            commentParams["page"] = page
            mPresenter.getCommentList(commentParams, true)
            squareBean!!.comment_cnt = squareBean!!.comment_cnt.plus(1)
            EventBus.getDefault().post(RefreshCommentEvent(squareBean!!.comment_cnt, intent.getIntExtra("position", 0)))
            squareCommentBtn1.text = "${squareBean!!.comment_cnt}"
        }
    }

    override fun onLikeCommentResult(data: BaseResp<Any?>, position: Int) {
        if (data.code == 200) {
            adapter.data[position].isliked = if (adapter.data[position].isliked == 0) {
                adapter.data[position].like_count = adapter.data[position].like_count!!.plus(1)
                1
            } else {
                adapter.data[position].like_count = adapter.data[position].like_count!!.minus(1)
                0
            }
            adapter.notifyItemChanged(position)
        } else {
            CommonFunction.toast(data.msg)
        }
    }

    override fun onDeleteCommentResult(data: BaseResp<Any?>, position: Int) {
        if (data.msg == "删除成功!") {
            adapter.data.removeAt(position)
            adapter.notifyItemRemoved(position)
            squareBean!!.comment_cnt = squareBean!!.comment_cnt.minus(1)
            EventBus.getDefault().post(RefreshCommentEvent(squareBean!!.comment_cnt, intent.getIntExtra("position", 0)))
            squareCommentBtn1.text = "${squareBean!!.comment_cnt}"
        }
    }

    override fun onReportCommentResult(data: BaseResp<Any?>, position: Int) {
        CommonFunction.toast(data.msg)
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.squareZhuanfaBtn1 -> {
                showTranspondDialog()
            }
            R.id.squareDianzanBtn1 -> {
                val params = hashMapOf(
                    "token" to SPUtils.getInstance(Constants.SPNAME).getString("token"),
                    "accid" to SPUtils.getInstance(Constants.SPNAME).getString("accid"),
                    "type" to if (squareBean!!.isliked == 1) {
                        2
                    } else {
                        1
                    },
                    "square_id" to squareBean!!.id!!,
                    "_timestamp" to System.currentTimeMillis()
                )
                mPresenter.getSquareLike(params)
            }
            R.id.squareCommentBtn1 -> {
                squareScrollView.smoothScrollTo(commentList.left, commentList.top)
            }
            R.id.squareMoreBtn1 -> {
                showMoreDialog()
            }
            R.id.sendCommentBtn -> {
                mPresenter.addComment(
                    hashMapOf(
                        "accid" to UserManager.getAccid(),
                        "token" to UserManager.getToken(),
                        "square_id" to squareBean!!.id!!,
                        "content" to showCommentEt.text.toString(),
                        "reply_id" to reply_id
                    )
                )
                sendCommentBtn.isEnabled = false

            }
        }
    }


    /**
     * 展示转发动态对话框
     */
    private fun showTranspondDialog() {
        val transpondDialog = TranspondDialog(this, squareBean!!)
        transpondDialog.show()
    }


//    var moreActionDialog: MoreActionDialog? = null
//    /**
//     * 展示更多操作对话框
//     */
//    private fun showMoreDialog() {
//        if (moreActionDialog == null)
//            moreActionDialog = MoreActionDialog(this!!, "square")
//        moreActionDialog!!.show()
//
//        if (squareBean!!.iscollected == 0) {
//            moreActionDialog!!.collect.text = "收藏"
//            moreActionDialog!!.collectBtn.setImageResource(R.drawable.icon_collect_no)
//        } else {
//            moreActionDialog!!.collect.text = "取消收藏"
//            moreActionDialog!!.collectBtn.setImageResource(R.drawable.icon_collectt)
//        }
//
//        if (squareBean!!.accid == UserManager.getAccid()) {
//            moreActionDialog!!.llDelete.visibility = View.VISIBLE
//            moreActionDialog!!.llJubao.visibility = View.GONE
//            moreActionDialog!!.llCollect.visibility = View.GONE
//        } else {
//            moreActionDialog!!.llDelete.visibility = View.GONE
//            moreActionDialog!!.llJubao.visibility = View.VISIBLE
//            moreActionDialog!!.llCollect.visibility = View.VISIBLE
//        }
//        moreActionDialog!!.llDelete.onClick {
//            val params = hashMapOf(
//                "accid" to SPUtils.getInstance(Constants.SPNAME).getString("accid"),
//                "token" to SPUtils.getInstance(Constants.SPNAME).getString("token"),
//                "square_id" to squareBean!!.id!!
//            )
//            mPresenter.removeMySquare(params)
//            moreActionDialog!!.dismiss()
//
//        }
//
//        moreActionDialog!!.llCollect.onClick {
//            //发起收藏请求
//            val params = hashMapOf(
//                "accid" to UserManager.getAccid(),
//                "token" to UserManager.getToken(),
//                "type" to if (squareBean!!.iscollected == 0) {
//                    1
//                } else {
//                    2
//                },
//                "square_id" to squareBean!!.id!!,
//                "_timestamp" to System.currentTimeMillis()
//            )
//            mPresenter.getSquareCollect(params)
//            moreActionDialog!!.dismiss()
//        }
//        moreActionDialog!!.llJubao.onClick {
//            //发起举报请求
//            mPresenter.getSquareReport(
//                hashMapOf(
//                    "accid" to UserManager.getAccid(),
//                    "token" to UserManager.getToken(),
//                    "square_id" to squareBean!!.id!!,
//                    "_timestamp" to System.currentTimeMillis()
//                )
//            )
//            moreActionDialog!!.dismiss()
//
//        }
//        moreActionDialog!!.cancel.onClick {
//            moreActionDialog!!.dismiss()
//        }
//        moreActionDialog!!.setOnDismissListener {
//            moreActionDialog = null
//        }
//
//    }


    lateinit var moreActionDialog: MoreActionNewDialog
    /**
     * 展示更多操作对话框
     */
    private fun showMoreDialog() {
        moreActionDialog = MoreActionNewDialog(this, squareBean)
        moreActionDialog.show()

        if (squareBean!!.iscollected == 0) {
            moreActionDialog.collect.text = "收藏"
            val top = resources.getDrawable(R.drawable.icon_collect1)
            moreActionDialog.collect.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null)
        } else {
            moreActionDialog.collect.text = "取消收藏"
            val top = resources.getDrawable(R.drawable.icon_collected1)
            moreActionDialog.collect.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null)
        }
        if (squareBean!!.accid == UserManager.getAccid()) {
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
                "square_id" to squareBean!!.id!!
            )
            mPresenter.removeMySquare(params)
            moreActionDialog.dismiss()

        }


        moreActionDialog.collect.onClick {
            //发起收藏请求
            val params = hashMapOf(
                "accid" to SPUtils.getInstance(Constants.SPNAME).getString("accid"),
                "token" to SPUtils.getInstance(Constants.SPNAME).getString("token"),
                "type" to if (squareBean!!.iscollected == 0) {
                    1
                } else {
                    2
                },
                "square_id" to squareBean!!.id!!,
                "_timestamp" to System.currentTimeMillis()
            )
            mPresenter.getSquareCollect(params)
            moreActionDialog.dismiss()
        }
        moreActionDialog.report.onClick {
            val dialog = DeleteDialog(this)
            dialog.show()
            dialog.tip.text = "确认举报该条动态？"
            dialog.confirm.text = "举报"
            dialog.cancel.onClick { dialog.dismiss() }
            dialog.confirm.onClick {
                dialog.dismiss()
                //发起举报请求
                val params = hashMapOf(
                    "accid" to SPUtils.getInstance(Constants.SPNAME).getString("accid"),
                    "token" to SPUtils.getInstance(Constants.SPNAME).getString("token"),
                    "type" to if (squareBean!!.iscollected == 0) {
                        1
                    } else {
                        2
                    },
                    "square_id" to squareBean!!.id!!,
                    "_timestamp" to System.currentTimeMillis()
                )
                mPresenter.getSquareReport(params)
            }
            moreActionDialog.dismiss()
        }
    }

    override fun onRemoveMySquareResult(result: Boolean) {
        if (result) {
            CommonFunction.toast("动态删除成功!")
            EventBus.getDefault().post(RefreshSquareEvent(true, TAG))
            finish()
        } else {
            CommonFunction.toast("动态删除失败！")
        }
    }

    var commentActionDialog: CommentActionDialog? = null
    //判断当前是添加评论还是回复评论
    private var reply = false
    private var reply_id = 0
    private fun showCommentDialog(position: Int) {
        if (commentActionDialog == null) {
            //判断该条评论是不是自己发的
            if (adapter.data[position].member_accid!! == UserManager.getAccid())
                commentActionDialog = CommentActionDialog(this, "self")
            else
                commentActionDialog = CommentActionDialog(this, "others")

        }
        commentActionDialog!!.show()

        commentActionDialog!!.copyComment.onClick {
            copyText(position)
            commentActionDialog!!.dismiss()
        }

        commentActionDialog!!.replyComment.onClick {
            reply = true
            reply_id = adapter.data[position].id!!
            showCommentEt.hint = "『回复\t${adapter.data[position].nickname}：』"
            showCommentEt.postDelayed({ KeyboardUtils.showSoftInput(showCommentEt) }, 100L)

            commentActionDialog!!.dismiss()
        }

        commentActionDialog!!.jubaoComment.onClick {
            //todo 举报
            mPresenter.commentReport(
                hashMapOf(
                    "token" to UserManager.getToken(),
                    "accid" to UserManager.getAccid(),
                    "id" to adapter.data[position].id!!
                )
                , position
            )
            commentActionDialog!!.dismiss()

        }

        commentActionDialog!!.deleteComment.onClick {
            mPresenter.deleteComment(
                hashMapOf(
                    "token" to UserManager.getToken(),
                    "accid" to UserManager.getAccid(),
                    "id" to adapter.data[position].id!!
                )
                , position
            )
            commentActionDialog!!.dismiss()
        }


        commentActionDialog!!.setOnDismissListener {
            commentActionDialog = null
        }
    }

    private fun copyText(position: Int) {
        //获取剪贴板管理器
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        //创建普通字符串clipData
        val clipData = ClipData.newPlainText("label", "${adapter.data[position].content}")
        //将clipdata内容放到系统剪贴板里
        cm.setPrimaryClip(clipData)
        CommonFunction.toast("已复制内容到剪贴板")
    }

    override fun onError(text: String) {
        stateview.viewState = MultiStateView.VIEW_STATE_ERROR
        stateview.errorMsg.text = if (!mPresenter.checkNetWork()) {
            getString(R.string.retry_net_error)
        } else {
            getString(R.string.retry_load_error)
        }
        refreshLayout.finishRefresh(false)
        refreshLayout.finishLoadMore(false)
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG1, "super.onPause()")
//        squareUserVideo.onVideoPause()
        if (mediaPlayer != null)
            mediaPlayer!!.pausePlay()
//        squareUserVideo.onVideoPause()
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG1, "super.onStart()")
        GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_DEFAULT)
//        if (!enterPosition.isNullOrEmpty()) {
//            showCommentEt.isFocusable = true
//            showCommentEt.postDelayed({ KeyboardUtils.showSoftInput(showCommentEt) }, 500L)
//        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG1, "super.onResume()")
        squareUserVideo.onVideoResume(false)
        if (mediaPlayer != null)
            mediaPlayer!!.resumePlay()
//        squareUserVideo.onVideoResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG1, "super.onDestroy()")
        if (mediaPlayer != null) {
            mediaPlayer!!.resetMedia()
            mediaPlayer = null
        }
        if (showCommentEt.isFocused)
            resetCommentEt()
    }


    override fun finish() {
        super.finish()
        if (mediaPlayer != null) {
            mediaPlayer!!.resetMedia()
            mediaPlayer = null
        }
        if (showCommentEt.isFocused)
            resetCommentEt()
        //释放所有
        squareUserVideo.gsyVideoManager.setListener(squareUserVideo.gsyVideoManager.lastListener())
        squareUserVideo.gsyVideoManager.setLastListener(null)
        squareUserVideo.release()
        GSYVideoManager.releaseAllVideos()
        SwitchUtil.release()
    }

    override fun onBackPressed() {
        if (showCommentEt.isFocused) {
            resetCommentEt()
        }
        if (mediaPlayer != null) {
            mediaPlayer!!.resetMedia()
            mediaPlayer = null
        }

        //释放所有
        squareUserVideo.gsyVideoManager.setListener(squareUserVideo.gsyVideoManager.lastListener())
        squareUserVideo.gsyVideoManager.setLastListener(null)
        squareUserVideo.release()
        GSYVideoManager.releaseAllVideos()
        SwitchUtil.release()
        super.onBackPressed()


    }

    /**
     * 重置输入框，清除焦点，隐藏键盘
     */
    private fun resetCommentEt() {
        reply = false
        reply_id = 0
        showCommentEt.clearFocus()
        showCommentEt.text.clear()
        showCommentEt.hint = "有什么感受说来听听"
        KeyboardUtils.hideSoftInput(showCommentEt)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SquarePlayDetailActivity.REQUEST_CODE) {
                GSYVideoManager.releaseAllVideos()
                SwitchUtil.clonePlayState(squareUserVideo)
                val state = squareUserVideo.currentState
                //延迟加2S
                squareUserVideo.seekOnStart = squareUserVideo.gsyVideoManager.currentPosition + 1000
                squareUserVideo.startPlayLogic()
                squareUserVideo.setVideoAllCallBack(object : GSYSampleCallBack() {
                    override fun onStartPrepared(url: String?, vararg objects: Any?) {
                        super.onStartPrepared(url, *objects)
                        GSYVideoManager.instance().isNeedMute = true
                    }

                    override fun onPrepared(url: String?, vararg objects: Any?) {
                        super.onPrepared(url, *objects)
                        GSYVideoManager.instance().isNeedMute = true
                        if (state == GSYVideoView.CURRENT_STATE_PAUSE) {
                            squareUserVideo.onVideoPause()
                        } else if (state == GSYVideoView.CURRENT_STATE_AUTO_COMPLETE || state == GSYVideoView.CURRENT_STATE_ERROR) {
                            SwitchUtil.release()
                            GSYVideoManager.releaseAllVideos()
                        }
                    }

                    override fun onClickResume(url: String?, vararg objects: Any?) {
                        super.onClickResume(url, *objects)
                        squareUserVideo.onVideoResume()
                    }

                    override fun onAutoComplete(url: String?, vararg objects: Any?) {
                        super.onAutoComplete(url, *objects)
                        SwitchUtil.release()
                        GSYVideoManager.releaseAllVideos()
                    }
                })
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_UP) {
            val view = currentFocus

            //如果不是落在edittext区域,就关闭输入法
            if (view != null && view is EditText) {
                val location = intArrayOf(0, 0)
                view.getLocationInWindow(location)

                //获取现在拥有焦点控件的位置
                val left = location[0] //控件的左
                val top = location[1] //控件的上
                val bottom = top + view.height
                val right = left + view.width

                if (!(ev.x > left && ev.x < right && ev.y > top && ev.y < bottom)) {
                    KeyboardUtils.hideSoftInput(view)
                }
            }
        }

        return super.dispatchTouchEvent(ev)
    }
/*--------------------------消息代理------------------------*/

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

    override fun onGreetSResult(b: Boolean) {
        if (b) {
            sendChatHiMessage()
        }
    }


//todo  这里要判断是不是VIP用户 如果是VIP 直接进入聊天界面
//1.首先判断是否有次数，
// 若有 就打招呼
// 若无 就弹充值
    /**
     * 判断当前能否打招呼
     */
    override fun onGreetStateResult(greetBean: GreetBean?) {
        if (greetBean != null && greetBean.lightningcnt != -1) {
            if (greetBean.isfriend || greetBean.isgreet) {
                ChatActivity.start(this, squareBean?.accid ?: "")
            } else {
                UserManager.saveLightingCount(greetBean.lightningcnt)
                UserManager.saveCountDownTime(greetBean.countdown)
                if (greetBean.lightningcnt > 0) {
                    mPresenter.greet(
                        UserManager.getToken(),
                        UserManager.getAccid(),
                        (squareBean?.accid ?: ""),
                        UserManager.getGlobalLabelId()
                    )
                } else {
                    ChargeVipDialog(
                        ChargeVipDialog.DOUBLE_HI, this, if (UserManager.isUserVip()) {
                            ChargeVipDialog.PURCHASE_GREET_COUNT
                        } else {
                            ChargeVipDialog.PURCHASE_VIP
                        }
                    ).show()
                }
            }
        }
        squareChatBtn1.isEnabled = true

    }


    private fun sendChatHiMessage() {
        val container = Container(this, squareBean?.accid, SessionTypeEnum.P2P, this, true)
        val chatHiAttachment = ChatHiAttachment(
            UserManager.getGlobalLabelName(),
            ChatHiAttachment.CHATHI_HI
        )
        val message = MessageBuilder.createCustomMessage(
            squareBean?.accid,
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
                ChatActivity.start(this@SquareCommentDetailActivity, squareBean?.accid ?: "")
                //发送通知修改招呼次数
                UserManager.saveLightingCount(UserManager.getLightingCount() - 1)
                EventBus.getDefault().postSticky(UpdateHiCountEvent())
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

}
