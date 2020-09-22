package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.blankj.utilcode.util.*
import com.google.android.flexbox.*
import com.kennyc.view.MultiStateView
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.leochuan.ScaleLayoutManager
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.common.OnLazyClickListener
import com.sdy.jitangapplication.event.RefreshCommentEvent
import com.sdy.jitangapplication.event.RefreshDeleteSquareEvent
import com.sdy.jitangapplication.event.RefreshLikeEvent
import com.sdy.jitangapplication.event.RefreshSquareEvent
import com.sdy.jitangapplication.model.AllCommentBean
import com.sdy.jitangapplication.model.CommentBean
import com.sdy.jitangapplication.model.SquareBean
import com.sdy.jitangapplication.player.IjkMediaPlayerUtil
import com.sdy.jitangapplication.player.OnPlayingListener
import com.sdy.jitangapplication.player.UpdateVoiceTimeThread
import com.sdy.jitangapplication.presenter.SquareDetailPresenter
import com.sdy.jitangapplication.presenter.view.SquareDetailView
import com.sdy.jitangapplication.switchplay.SwitchUtil
import com.sdy.jitangapplication.ui.adapter.ListSquareImgsAdapter
import com.sdy.jitangapplication.ui.adapter.MultiListCommentAdapter
import com.sdy.jitangapplication.ui.adapter.SquareTitleAdapter
import com.sdy.jitangapplication.ui.dialog.CommentActionDialog
import com.sdy.jitangapplication.ui.dialog.DeleteDialog
import com.sdy.jitangapplication.ui.dialog.MoreActionNewDialog
import com.sdy.jitangapplication.ui.dialog.TranspondDialog
import com.sdy.jitangapplication.utils.UriUtils
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.GalleryOnScrollListener
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack
import com.shuyu.gsyvideoplayer.utils.GSYVideoType
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView
import com.umeng.socialize.UMShareAPI
import kotlinx.android.synthetic.main.activity_square_comment_detail.*
import kotlinx.android.synthetic.main.activity_square_comment_detail.headSquareView
import kotlinx.android.synthetic.main.delete_dialog_layout.*
import kotlinx.android.synthetic.main.delete_dialog_layout.view
import kotlinx.android.synthetic.main.dialog_comment_action.*
import kotlinx.android.synthetic.main.dialog_more_action_new.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import kotlinx.android.synthetic.main.layout_comment_head.view.*
import kotlinx.android.synthetic.main.layout_record_audio.*
import kotlinx.android.synthetic.main.layout_square_list_bottom.*
import kotlinx.android.synthetic.main.layout_square_list_top.*
import kotlinx.android.synthetic.main.switch_video.view.*
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.startActivity


/**
 * 广场详情页 包含内容详情以及点赞评论信息
 */
class SquareCommentDetailActivity : BaseMvpActivity<SquareDetailPresenter>(), SquareDetailView,
    OnLazyClickListener,
    OnRefreshListener, OnLoadMoreListener {
    private val TAG = SquareCommentDetailActivity::class.java.simpleName


    //评论数据
    private var commentDatas: MutableList<CommentBean> = mutableListOf()
    private val adapter: MultiListCommentAdapter by lazy {
        MultiListCommentAdapter(
            this,
            commentDatas
        )
    }

    private var squareBean: SquareBean? = null

    private var page = 1

    private val commentParams = hashMapOf<String,Any>(
        "token" to UserManager.getToken(),
        "accid" to UserManager.getAccid(),
        "square_id" to "",
        "page" to page,
        "pagesize" to Constants.PAGESIZE
    )
    private val type by lazy { intent.getIntExtra("type", TYPE_SQUARE) }

    companion object {
        const val TYPE_SQUARE = 1
        const val TYPE_SWEET = 2
        fun start(
            context: Context,
            squareBean: SquareBean? = null,
            squareId: Int? = null,
            position: Int? = 0,
            type: Int = TYPE_SQUARE, gender: Int = 0
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
                "position" to position,
                "type" to type,
                "gender" to gender
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
            stateview.postDelayed({
                mPresenter.getSquareInfo(
                    hashMapOf(
                        "token" to UserManager.getToken(),
                        "accid" to UserManager.getAccid(),
                        "square_id" to intent.getIntExtra("square_id", 0)
                    )
                )
            }, 700L)
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
                mediaPlayer!!.setDataSource(squareBean!!.audio_json?.get(0)?.url ?: "")
                    .prepareMedia()
            }
        }


        GlideUtil.loadAvatorImg(this, squareBean!!.avatar ?: "", squareUserIv1)
        GlideUtil.loadAvatorImg(this, squareBean!!.avatar ?: "", headSquareUserIv1)
        if (!squareBean!!.tags.isNullOrEmpty()) {
            squareTagName.text = squareBean!!.tags ?: ""
            squareTagLl.isVisible = true
        } else {
            squareTagLl.isVisible = false
        }


        //标题跳转
        val manager = FlexboxLayoutManager(this, FlexDirection.ROW, FlexWrap.WRAP)
        manager.alignItems = AlignItems.STRETCH
        manager.justifyContent = JustifyContent.FLEX_START
        squareTitleRv.layoutManager = manager
        val adapter = SquareTitleAdapter()
        adapter.addData(squareBean!!.title_list ?: mutableListOf())
        squareTitleRv.adapter = adapter
        adapter.setOnItemClickListener { _, view, position ->
            startActivity<TagDetailCategoryActivity>(
                "id" to adapter.data[position].id,
                "type" to TagDetailCategoryActivity.TYPE_TOPIC
            )
        }
        squareTitleRv.isVisible = !squareBean!!.title_list.isNullOrEmpty()
        setLikeStatus(
            squareBean!!.isliked,
            squareBean!!.like_cnt,
            squareDianzanBtn1,
            squareDianzanAni,
            false
        )

        if (intent.getIntExtra("position", -1) != -1)
            EventBus.getDefault().post(
                RefreshLikeEvent(
                    squareBean?.id ?: 0,
                    squareBean?.isliked ?: 0,
                    intent.getIntExtra("position", -1),
                    if (squareBean!!.like_cnt < 0) {
                        0
                    } else {
                        squareBean!!.like_cnt
                    }
                )
            )

        squareCommentBtn1.text = "${squareBean!!.comment_cnt}"
        squareContent1.isVisible = !squareBean!!.descr.isNullOrEmpty()
        if (!squareBean!!.descr.isNullOrEmpty()) {
            squareContent1.setContent("${squareBean!!.descr}")
        }

        //todo sweet heart
        if (squareBean!!.approve_type != 0) {
            squareTagLl.isVisible = false
            squareLocationAndTime1Ll.isVisible = false
            squareTagName.isVisible = false
            squareUserSweetLogo.isVisible = true
            squareSweetVerifyContent.isVisible = true
            val params =
                squareSweetVerifyContent.layoutParams as ConstraintLayout.LayoutParams
            params.width = ScreenUtils.getScreenWidth() - SizeUtils.dp2px(15 * 2F)
            params.height = (params.width * (177 / 1035F)).toInt()

            //// 0普通 1资产认证 2豪车认证 3 身材认证 4 职业认证
            if (squareBean!!.approve_type == 1 || squareBean!!.approve_type == 2) {
                squareContent1.setTextColor(Color.parseColor("#FFFFCD52"))
                squareUserSweetLogo.imageAssetsFolder = "images_sweet_logo_man"
                squareUserSweetLogo.setAnimation("data_sweet_logo_man.json")
                squareUserSweetLogo.playAnimation()
            } else {
                squareContent1.setTextColor(Color.parseColor("#FFFF7CA8"))
                squareUserSweetLogo.imageAssetsFolder =
                    "images_sweet_logo_woman"
                squareUserSweetLogo.setAnimation("data_sweet_logo_woman.json")
                squareUserSweetLogo.playAnimation()
            }

            squareSweetVerifyContent.setImageResource(
                when (squareBean!!.approve_type) {
                    1 -> {
                        R.drawable.icon_sweet_type_wealth
                    }

                    2 -> {
                        R.drawable.icon_sweet_type_car
                    }
                    3 -> {
                        R.drawable.icon_sweet_type_figure
                    }
                    else -> {
                        R.drawable.icon_sweet_type_profession
                    }
                }
            )
        } else {
            squareUserSweetLogo.isVisible = false
            squareSweetVerifyContent.isVisible = false
            squareContent1.setTextColor(Color.parseColor("#FF191919"))

            if (squareBean!!.puber_address.isNullOrEmpty()) {
                squareLocationAndTime1Ll.visibility = View.INVISIBLE
            } else {
                squareLocationAndTime1Ll.isVisible = true
            }

            if (!squareBean!!.tags.isNullOrEmpty()) {
                squareTagName.text = squareBean!!.tags
                squareTagLl.isVisible = true
            } else {
                squareTagLl.isVisible = false
            }

        }



        squareZhuanfaBtn1.text = "${squareBean!!.share_cnt}"
        squareUserName1.text = "${squareBean!!.nickname}"
        headSquareUserName1.text = "${squareBean!!.nickname}"
        squareUserVipIv1.isVisible = squareBean!!.isplatinumvip || squareBean!!.isdirectvip
        headSquareUserVipIv1.isVisible = squareUserVipIv1.isVisible
        if (squareBean!!.isplatinumvip) {
            squareUserVipIv1.setImageResource(R.drawable.icon_vip)
            headSquareUserVipIv1.setImageResource(R.drawable.icon_vip)
        } else if (squareBean!!.isdirectvip) {
            squareUserVipIv1.setImageResource(R.drawable.icon_direct_vip)
            headSquareUserVipIv1.setImageResource(R.drawable.icon_direct_vip)
        }

        if (squareBean!!.isfriend) {
            squareChatBtn1.isVisible = true
            headSquareChatBtn1.isVisible = true
        } else {
            squareChatBtn1.visibility =
                if (!(UserManager.getAccid() == squareBean!!.accid || !squareBean!!.greet_switch)) {
                    View.VISIBLE
                } else {
                    View.INVISIBLE
                }
            headSquareChatBtn1.visibility =
                if (!(UserManager.getAccid() == squareBean!!.accid || !squareBean!!.greet_switch)) {
                    View.VISIBLE
                } else {
                    View.INVISIBLE
                }
        }

        squareTime.text = "${squareBean!!.out_time}"
        headSquareTime.text = "${squareBean!!.out_time}"
        squareLocation.text = "${squareBean!!.puber_address}"
    }

    private fun setLikeStatus(
        isliked: Int,
        likeCount: Int,
        likeView: TextView,
        likeAni: LottieAnimationView,
        animated: Boolean = true
    ) {
        if (isliked == 1) {
            if (animated) {
                likeAni.playAnimation()
                VibrateUtils.vibrate(50L)
            } else {
                likeAni.progress = 1F
            }
        } else {
            likeAni.progress = 0F
        }

        likeView.text = "${if (likeCount < 0) {
            0
        } else {
            likeCount
        }}"
    }

    private val gender by lazy { intent.getIntExtra("gender", 1) }
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

        if (type == TYPE_SWEET) {
            hotT1.text = "${if (gender == 1) {
                "他"
            } else {
                "她"
            }}的认证资料"
            refreshLayout.setEnableRefresh(false)
            refreshLayout.setEnableLoadMore(false)
            commentList.isVisible = false
            showCommentLl.isVisible = false
        } else {
            hotT1.text = "动态详情"
            refreshLayout.setOnRefreshListener(this)
            refreshLayout.setOnLoadMoreListener(this)

            commentList.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
            commentList.adapter = adapter
//        commentList.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.HORIZONTAL_LIST,SizeUtils.dp2px(15F),Color.WHITE))
            adapter.setEmptyView(R.layout.empty_layout_comment, commentList)
            adapter.emptyView.allT2.isVisible = false


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
                            MatchDetailActivity.start(
                                this,
                                adapter.data[position].member_accid ?: ""
                            )
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
        }




        btnBack.setOnClickListener(this)
        headBtnBack.setOnClickListener(this)
        squareZhuanfaBtn1.setOnClickListener(this)
        squareDianzanAni.setOnClickListener(this)
        squareCommentBtn1.setOnClickListener(this)
        squareChatBtn1.setOnClickListener(this)
        headSquareChatBtn1.setOnClickListener(this)
        squareUserIv1.setOnClickListener(this)
        headSquareUserIv1.setOnClickListener(this)
        squareMoreBtn1.setOnClickListener(this)
        sendCommentBtn.setOnClickListener(this)
        view.isVisible = false





        squareScrollView.setOnScrollChangeListener { v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            if (scrollY >= SizeUtils.dp2px(56F)) {
                headSquareView.isVisible = true
                topLayout.visibility = View.INVISIBLE
                llTitle.visibility = View.INVISIBLE
            } else {
                topLayout.isVisible = true
                llTitle.isVisible = true
                headSquareView.isVisible = false
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
                    mediaPlayer!!.setDataSource(squareBean!!.audio_json?.get(0)?.url ?: "")
                        .prepareMedia()
                }
                IjkMediaPlayerUtil.MEDIA_PREPARE -> {//准备中
                    mediaPlayer!!.prepareMedia()
                }
                IjkMediaPlayerUtil.MEDIA_STOP -> {//停止就重新准备
                    initAudio(0)
                    mediaPlayer!!.setDataSource(squareBean!!.audio_json?.get(0)?.url ?: "")
                        .prepareMedia()
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
    private val imgsAdapter by lazy {
        ListSquareImgsAdapter(
            this,
            squareBean!!.photo_json ?: mutableListOf()
        )
    }

    private fun initPics() {
        if (squareBean!!.photo_json != null && squareBean!!.photo_json!!.size > 0) {
            squareUserPics.layoutManager = ScaleLayoutManager(this, 0)
            squareUserPics.adapter = imgsAdapter

            //分页滑动效果
            squareUserPics.onFlingListener = null
            PagerSnapHelper().attachToRecyclerView(squareUserPics)
            //滑动动画
            squareUserPics.addOnScrollListener(GalleryOnScrollListener())
            imgsAdapter.setOnItemClickListener { _, view, position ->
                if (squareBean!!.isliked != 1) {
                    val params = hashMapOf<String, Any>(
                        "type" to if (squareBean!!.isliked == 1) {
                            2
                        } else {
                            1
                        },
                        "square_id" to squareBean!!.id!!
                    )
                    mPresenter.getSquareLike(params, true)
                }

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
        if (data != null) {
            if (data.id == null) {
                CommonFunction.toast("该动态已被删除")
                finish()
                return
            }


            stateview.viewState = MultiStateView.VIEW_STATE_CONTENT
            data.type = when {
                !data.video_json.isNullOrEmpty() -> SquareBean.VIDEO
                !data.audio_json.isNullOrEmpty() -> SquareBean.AUDIO
                !data.photo_json.isNullOrEmpty() ||
                        (data.photo_json.isNullOrEmpty() && data.audio_json.isNullOrEmpty() && data.video_json.isNullOrEmpty()) -> SquareBean.PIC
                else -> SquareBean.PIC
            }
            squareBean = data
            initData()
            if (type == TYPE_SQUARE) {
                commentParams["square_id"] = "${squareBean!!.id}"
                mPresenter.getCommentList(commentParams, true)
            }
        } else {
            CommonFunction.toast("该动态已被删除")
            finish()
        }
    }

    override fun onGetCommentListResult(allCommentBean: AllCommentBean?, refresh: Boolean) {
        if (refresh) {
            refreshLayout.setNoMoreData(false)
            if (allCommentBean != null) {
                if (allCommentBean.hotlist != null && allCommentBean.hotlist!!.size > 0) {
                    adapter.addData(CommentBean(content = "热门评论", type = CommentBean.TITLE))
                    for (i in 0 until allCommentBean.hotlist!!.size) {
                        allCommentBean.hotlist!![i]!!.type = CommentBean.CONTENT
                    }
                    adapter.addData(allCommentBean.hotlist!!)
                }
                if (allCommentBean.list != null && allCommentBean.list!!.size > 0) {
                    adapter.addData(CommentBean(content = "所有评论", type = CommentBean.TITLE))
                    for (i in 0 until allCommentBean.list!!.size) {
                        allCommentBean.list!![i]!!.type = CommentBean.CONTENT
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


            setLikeStatus(
                squareBean!!.isliked,
                squareBean!!.like_cnt,
                squareDianzanBtn1,
                squareDianzanAni,
                true
            )

            if (intent.getIntExtra("position", -1) != -1)
                EventBus.getDefault().post(
                    RefreshLikeEvent(
                        squareBean?.id ?: 0,
                        squareBean?.isliked ?: 0,
                        intent.getIntExtra("position", -1)
                    )
                )
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
            EventBus.getDefault().post(
                RefreshCommentEvent(
                    squareBean!!.comment_cnt,
                    intent.getIntExtra("position", 0)
                )
            )
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
            EventBus.getDefault().post(
                RefreshCommentEvent(
                    squareBean!!.comment_cnt,
                    intent.getIntExtra("position", 0)
                )
            )
            squareCommentBtn1.text = "${squareBean!!.comment_cnt}"
        }
    }

    override fun onReportCommentResult(data: BaseResp<Any?>, position: Int) {
        CommonFunction.toast(data.msg)
    }


    override fun onLazyClick(view: View) {
        when (view.id) {
            R.id.squareZhuanfaBtn1 -> {
                showTranspondDialog()
            }
            R.id.btnBack, R.id.headBtnBack -> {
                onBackPressed()
            }
            R.id.squareDianzanAni -> {
                val params = hashMapOf<String,Any>(
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
            R.id.squareChatBtn1, R.id.headSquareChatBtn1 -> {
                CommonFunction.checkChat(this, squareBean?.accid ?: "")
            }
            R.id.squareUserIv1, R.id.headSquareUserIv1 -> {
                if ((squareBean?.accid ?: "") != UserManager.getAccid())
                    MatchDetailActivity.start(this, squareBean?.accid ?: "")
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
            val params = hashMapOf<String,Any>(
                "accid" to SPUtils.getInstance(Constants.SPNAME).getString("accid"),
                "token" to SPUtils.getInstance(Constants.SPNAME).getString("token"),
                "square_id" to squareBean!!.id!!
            )
            mPresenter.removeMySquare(params)
            moreActionDialog.dismiss()

        }


        moreActionDialog.collect.onClick {
            //发起收藏请求
            val params = hashMapOf<String,Any>(
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
            dialog.tip.text = getString(R.string.report_square)
            dialog.title.text = "动态举报"
            dialog.confirm.text = "举报"
            dialog.cancel.onClick { dialog.dismiss() }
            dialog.confirm.onClick {
                dialog.dismiss()
                //发起举报请求
                val params = hashMapOf<String,Any>(
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
            EventBus.getDefault().post(
                RefreshDeleteSquareEvent(
                    if (squareBean != null) {
                        squareBean!!.id ?: 0
                    } else {
                        intent.getIntExtra("square_id", 0)
                    }
                )
            )
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
            //举报
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


}
