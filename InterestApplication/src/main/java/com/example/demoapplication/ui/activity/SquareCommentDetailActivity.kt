package com.example.demoapplication.ui.activity

import android.app.Activity
import android.app.ActivityOptions
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.transition.Explode
import android.view.View
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.app.SharedElementCallback
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.SPUtils
import com.example.baselibrary.glide.GlideUtil
import com.example.demoapplication.R
import com.example.demoapplication.common.Constants
import com.example.demoapplication.model.AllCommentBean
import com.example.demoapplication.model.CommentBean
import com.example.demoapplication.model.SquareBean
import com.example.demoapplication.player.IjkMediaPlayerUtil
import com.example.demoapplication.player.OnPlayingListener
import com.example.demoapplication.player.UpdateVoiceTimeThread
import com.example.demoapplication.presenter.SquareDetailPresenter
import com.example.demoapplication.presenter.view.SquareDetailView
import com.example.demoapplication.switchplay.SwitchUtil
import com.example.demoapplication.ui.adapter.ListSquareImgsAdapter
import com.example.demoapplication.ui.adapter.MultiListCommentAdapter
import com.example.demoapplication.ui.dialog.CommentActionDialog
import com.example.demoapplication.ui.dialog.MoreActionDialog
import com.example.demoapplication.ui.dialog.TranspondDialog
import com.example.demoapplication.utils.UserManager
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.shuyu.gsyvideoplayer.utils.GSYVideoType
import kotlinx.android.synthetic.main.activity_square_comment_detail.*
import kotlinx.android.synthetic.main.dialog_comment_action.*
import kotlinx.android.synthetic.main.dialog_more_action.*
import kotlinx.android.synthetic.main.switch_video.view.*
import org.jetbrains.anko.toast
import java.util.*


/**
 * 广场详情页 包含内容详情以及点赞评论信息
 */
class SquareCommentDetailActivity : BaseMvpActivity<SquareDetailPresenter>(), SquareDetailView, View.OnClickListener,
    OnRefreshListener, OnLoadMoreListener {

    //评论数据
    private var commentDatas: MutableList<CommentBean> = mutableListOf()
    private val adapter: MultiListCommentAdapter by lazy { MultiListCommentAdapter(this, commentDatas) }

    private val squareBean: SquareBean  by lazy { intent.getSerializableExtra("squareBean") as SquareBean }

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

    override fun onCreate(savedInstanceState: Bundle?) {
        // 设置一个exit transition
        window?.enterTransition = Explode()
        window?.exitTransition = Explode()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_square_comment_detail)
        initView()
        initData()


        commentParams["square_id"] = "${squareBean.id}"
        mPresenter.getCommentList(commentParams, true)
    }

    private fun initData() {
        GlideUtil.loadAvatorImg(this, squareBean.avatar ?: "", squareUserIv)
        val drawable1 =
            resources.getDrawable(if (squareBean.isliked == 1) R.drawable.icon_dianzan_red else R.drawable.icon_dianzan)
        drawable1!!.setBounds(0, 0, drawable1.intrinsicWidth, drawable1.intrinsicHeight)    //需要设置图片的大小才能显示
        squareDianzanBtn.setCompoundDrawables(drawable1, null, null, null)
        squareDianzanBtn.text = "${squareBean.like_cnt}"
        squareCommentBtn.text = "${squareBean.comment_cnt}"
        squareContent.text = "${squareBean.descr}"
        squareZhuanfaBtn.text = "${squareBean.share_cnt}"
        detailPlayUserName.text = "${squareBean.nickname}"
        detailPlayUserLocationAndTime.text =
            "${squareBean.province_name}省${squareBean.city_name}市\t${squareBean.out_time}"

    }

    private fun initView() {
        mPresenter = SquareDetailPresenter()
        mPresenter.mView = this
        mPresenter.context = this

        refreshLayout.setOnRefreshListener(this)
        refreshLayout.setOnLoadMoreListener(this)
        refreshLayout.setEnableLoadMoreWhenContentNotFull(false)

        commentList.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        commentList.adapter = adapter

        btnBack.onClick {
            finish()
        }

        when {
            squareBean.type == 1 -> {
                squareUserPics.visibility = View.VISIBLE
                initPics()
            }
            squareBean.type == 2 -> {
                squareUserVideo.visibility = View.VISIBLE
                initVideo()
            }
            else -> {
                squareUserAudio.visibility = View.VISIBLE
//                initAudio()
                initAudio(0)
                mediaPlayer!!.setDataSource(squareBean.audio_json?.get(0) ?: "").prepareMedia()
            }
        }

        squareZhuanfaBtn.setOnClickListener(this)
        squareDianzanBtn.setOnClickListener(this)
        squareCommentBtn.setOnClickListener(this)
        squareMoreBtn.setOnClickListener(this)
        showCommentBtn.setOnClickListener(this)

        showCommentEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(charSequence: Editable?) {
                if (charSequence.toString().isNotEmpty()) {
                    showCommentBtn.isEnabled = true
                    showCommentBtn.setBackgroundResource(R.drawable.icon_send_enable)
                } else {
                    showCommentBtn.isEnabled = false
                    showCommentBtn.setBackgroundResource(R.drawable.icon_send_unable)
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
                R.id.childView -> {
//                    reply = true
//                    reply_id = adapter.data[position].reply_id!!
//                    showCommentEt.isFocusable = true
//                    showCommentEt.hint = "『回复\t${adapter.data[position].replyed_nickname}：』"
//                    KeyboardUtils.showSoftInput(showCommentEt)
                }
                R.id.commentDianzanBtn -> {
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
                    showCommentEt.isFocusable = true
                    showCommentEt.hint = "『回复${adapter.data[position].replyed_nickname}：』"
                    KeyboardUtils.showSoftInput(showCommentEt)
                }
            }
        }
    }

    /**
     * 初始化播放音频
     */
    // var ijkMediaPlayer: IjkMediaPlayer? = null

//    private fun initAudio() {
//        ijkMediaPlayer?.release()
//        ijkMediaPlayer = IjkMediaPlayer()
//        ijkMediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
//        ijkMediaPlayer!!.setDataSource(this, Uri.parse(squareBean.audio_json?.get(0) ?: ""))
//
//
//
//    }


    var mediaPlayer: IjkMediaPlayerUtil? = null

    private fun initAudio(position: Int) {
        if (mediaPlayer != null) {
            mediaPlayer!!.resetMedia()
            mediaPlayer = null
        }
        mediaPlayer = IjkMediaPlayerUtil(this, position, object : OnPlayingListener {
            override fun onPlay(position: Int) {
                squareBean.isPlayAudio = IjkMediaPlayerUtil.MEDIA_PLAY
                voicePlayView.start()
                UpdateVoiceTimeThread.getInstance("03:40", audioTime).start()
                audioPlayBtn.setImageResource(R.drawable.icon_pause_audio)
            }

            override fun onPause(position: Int) {
                squareBean.isPlayAudio = IjkMediaPlayerUtil.MEDIA_PAUSE
                voicePlayView.stop()
                UpdateVoiceTimeThread.getInstance("03:40", audioTime).pause()
                audioPlayBtn.setImageResource(R.drawable.icon_play_audio)
            }

            override fun onStop(position: Int) {
                squareBean.isPlayAudio = IjkMediaPlayerUtil.MEDIA_STOP
                voicePlayView.stop()
                UpdateVoiceTimeThread.getInstance("03:40", audioTime).stop()
                audioPlayBtn.setImageResource(R.drawable.icon_play_audio)
                mediaPlayer!!.resetMedia()
                mediaPlayer = null
            }

            override fun onError(position: Int) {
                toast("音频播放出错")
                squareBean.isPlayAudio = IjkMediaPlayerUtil.MEDIA_STOP
//                adapter.notifyItemChanged(position)
                adapter.notifyDataSetChanged()
                mediaPlayer!!.resetMedia()
                mediaPlayer = null
            }

            override fun onPrepared(position: Int) {
                //todo  异步准备 准备好了才会实现播放。
                mediaPlayer!!.startPlay()
            }

            override fun onPreparing(position: Int) {
                voicePlayView.stop()
                UpdateVoiceTimeThread.getInstance("03:40", audioTime).stop()
                audioPlayBtn.setImageResource(R.drawable.icon_play_audio)
            }

            override fun onRelease(position: Int) {
//                squareBean.isPlayAudio = IjkMediaPlayerUtil.MEDIA_STOP
//                voicePlayView.stop()
//                UpdateVoiceTimeThread.getInstance("03:40", audioTime).stop()
//                audioPlayBtn.setImageResource(R.drawable.icon_play_audio)
//                mediaPlayer!!.resetMedia()
//                mediaPlayer = null
            }

        }).getInstance()

        audioPlayBtn.setOnClickListener {
            when (squareBean.isPlayAudio) {
                IjkMediaPlayerUtil.MEDIA_PREPARE -> {
                    mediaPlayer!!.prepareMedia()
                }
                IjkMediaPlayerUtil.MEDIA_STOP -> {
                    mediaPlayer!!.prepareMedia()
                }
                IjkMediaPlayerUtil.MEDIA_PLAY -> {
                    mediaPlayer!!.pausePlay()
                }
                IjkMediaPlayerUtil.MEDIA_PAUSE -> {
                    mediaPlayer!!.resumePlay()
                }
            }
        }
    }


    /**
     * 初始化播放视频
     */
    private fun initVideo() {
        GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_DEFAULT)
        squareUserVideo.fullscreenButton.visibility = View.GONE//设置全屏按钮
        squareUserVideo.startButton.visibility = View.GONE
        squareUserVideo.backButton.visibility = View.GONE//设置返回键
        squareUserVideo.titleTextView.visibility = View.GONE//增加title
        squareUserVideo.setIsTouchWiget(true)//可以滑动调整
        squareUserVideo.detail_btn.onClick {
            if (squareUserVideo.isInPlayingState) {
                SwitchUtil.savePlayState(squareUserVideo)
                squareUserVideo.gsyVideoManager.setLastListener(squareUserVideo)
                //fixme 页面跳转是，元素共享，效果会有一个中间中间控件的存在
                //fixme 这时候中间控件 CURRENT_STATE_PLAYING，会触发 startProgressTimer
                //FIXME 但是没有cancel
                SquarePlayDetailActivity.startActivity(this, squareUserVideo, squareBean, 0)
            }
        }
        squareUserVideo.setSwitchUrl(squareBean.video_json?.get(0) ?: "")
        squareUserVideo.setSwitchCache(false)
        squareUserVideo.setUp(squareBean.video_json?.get(0) ?: "", false, "")
        squareUserVideo.startPlayLogic()
    }


    /**
     * 初始化图片列表
     */
    private val imgsAdapter by lazy { ListSquareImgsAdapter(this, squareBean.photo_json ?: mutableListOf()) }

    private fun initPics() {
        if (squareBean.photo_json != null && squareBean.photo_json!!.size > 0) {
            squareUserPics.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
            squareUserPics.adapter = imgsAdapter
            imgsAdapter.setOnItemClickListener { _, view, position ->
                val intent = Intent(this@SquareCommentDetailActivity, BigImageActivity::class.java)
                intent.putExtra(BigImageActivity.IMG_KEY, squareBean)
                intent.putExtra(BigImageActivity.IMG_POSITION, position)
                val bundle = ActivityOptions.makeSceneTransitionAnimation(
                    this@SquareCommentDetailActivity,
                    view.findViewById(R.id.ivUser) as ImageView,
                    "share"
                ).toBundle()
                startActivity(intent, bundle)
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

    override fun onGetCommentListResult(allCommentBean: AllCommentBean, refresh: Boolean) {
        if (refresh) {
            refreshLayout.setNoMoreData(false)
            if (allCommentBean.hotlist != null && allCommentBean.hotlist!!.size > 0) {
                adapter.addData(CommentBean(content = "热门评论", type = 0))
                for (i in 0 until allCommentBean.hotlist!!.size) {
                    allCommentBean.hotlist!![i]!!.type = 1
                }
                adapter.addData(allCommentBean.hotlist!!)
            }
            adapter.addData(CommentBean(content = "所有评论", type = 0))
            if (allCommentBean.list != null && allCommentBean.list!!.size > 0) {
                for (i in 0 until allCommentBean.list!!.size) {
                    allCommentBean.list!![i]!!.type = 1
                }
                adapter.addData(allCommentBean.list!!)
            }
            refreshLayout.finishRefresh(true)
        } else {
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
            refreshLayout.finishLoadMore(true)
        }
    }

    override fun onGetSquareCollectResult(data: BaseResp<Any?>) {
        toast(data.msg)
        if (data.code == 200) {
            squareBean.iscollected = if (squareBean.iscollected == 1) {
                0
            } else {
                1
            }
        }
    }

    override fun onGetSquareLikeResult(result: Boolean) {
        if (result) {
            squareBean.isliked = if (squareBean.isliked == 0) {
                toast("点赞成功")
                squareBean.like_cnt = squareBean.like_cnt?.plus(1)
                1
            } else {
                toast("取消点赞成功")
                squareBean.like_cnt = squareBean.like_cnt?.minus(1)
                0
            }
            squareDianzanBtn.text = "${squareBean.like_cnt}"
            val drawable1 =
                resources.getDrawable(if (squareBean.isliked == 1) R.drawable.icon_dianzan_red else R.drawable.icon_dianzan)
            drawable1!!.setBounds(0, 0, drawable1.intrinsicWidth, drawable1.intrinsicHeight)    //需要设置图片的大小才能显示
            squareDianzanBtn.setCompoundDrawables(drawable1, null, null, null)
        } else {
            toast("点赞失败，请重试")
        }
    }

    override fun onGetSquareReport(data: BaseResp<Any?>) {
        toast(data.msg)
    }


    override fun onAddCommentResult(data: BaseResp<Any?>, result: Boolean) {
        toast(data.msg)
        if (result) {
            resetCommentEt()
            refreshLayout.autoRefresh()
        }
    }

    override fun onLikeCommentResult(data: BaseResp<Any?>, position: Int) {
        toast(data.msg)
        adapter.data[position].isliked = if (adapter.data[position].isliked == 0) {
            adapter.data[position].like_count = adapter.data[position].like_count!!.plus(1)
            1
        } else {
            adapter.data[position].like_count = adapter.data[position].like_count!!.minus(1)
            0
        }
        adapter.notifyItemChanged(position)
    }

    override fun onDeleteCommentResult(data: BaseResp<Any?>, position: Int) {
        toast(data.msg)
        adapter.notifyItemChanged(position)
    }

    override fun onReportCommentResult(data: BaseResp<Any?>, position: Int) {
        toast(data.msg)
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.squareZhuanfaBtn -> {
                showTranspondDialog()
            }
            //todo  取消点赞后台有问题
            R.id.squareDianzanBtn -> {
                val params = hashMapOf(
                    "token" to SPUtils.getInstance(Constants.SPNAME).getString("token"),
                    "accid" to SPUtils.getInstance(Constants.SPNAME).getString("accid"),
                    "type" to if (squareBean.isliked == 1) {
                        2
                    } else {
                        1
                    },
                    "square_id" to squareBean.id!!,
                    "_timestamp" to System.currentTimeMillis()
                )
                mPresenter.getSquareLike(params)
            }
            R.id.squareCommentBtn -> {
                squareScrollView.smoothScrollTo(commentList.left, commentList.top)
            }
            R.id.squareMoreBtn -> {
                showMoreDialog()
            }
            R.id.showCommentBtn -> {
                mPresenter.addComment(
                    hashMapOf(
                        "accid" to UserManager.getAccid(),
                        "token" to UserManager.getToken(),
                        "square_id" to squareBean.id!!,
                        "content" to showCommentEt.text.toString(),
                        "reply_id" to reply_id
                    )
                )

            }
        }
    }


    private val transpondDialog by lazy { TranspondDialog(this) }
    /**
     * 展示转发动态对话框
     */
    private fun showTranspondDialog() {
        if (transpondDialog != null && !transpondDialog.isShowing)
            transpondDialog.show()
    }


    var moreActionDialog: MoreActionDialog? = null
    /**
     * 展示更多操作对话框
     */
    private fun showMoreDialog() {
        if (moreActionDialog == null)
            moreActionDialog = MoreActionDialog(this!!, "square")
        moreActionDialog!!.show()

        if (squareBean.iscollected == 0) {
            moreActionDialog!!.collect.text = "收藏"
            moreActionDialog!!.collectBtn.setImageResource(R.drawable.icon_collect_no)
        } else {
            moreActionDialog!!.collect.text = "取消收藏"
            moreActionDialog!!.collectBtn.setImageResource(R.drawable.icon_collectt)
        }
        moreActionDialog!!.llCollect.onClick {
            //发起收藏请求
            val params = hashMapOf(
                "accid" to UserManager.getAccid(),
                "token" to UserManager.getToken(),
                "type" to if (squareBean.iscollected == 0) {
                    1
                } else {
                    2
                },
                "square_id" to squareBean.id!!,
                "_timestamp" to System.currentTimeMillis()
            )
            mPresenter.getSquareCollect(params)
            moreActionDialog!!.dismiss()
        }
        moreActionDialog!!.llJubao.onClick {
            //发起举报请求
            mPresenter.getSquareReport(
                hashMapOf(
                    "accid" to UserManager.getAccid(),
                    "token" to UserManager.getToken(),
                    "type" to if (squareBean.iscollected == 0) {
                        1
                    } else {
                        2
                    },
                    "square_id" to squareBean.id!!,
                    "_timestamp" to System.currentTimeMillis()
                )
            )
            moreActionDialog!!.dismiss()

        }
        moreActionDialog!!.cancel.onClick {
            moreActionDialog!!.dismiss()
        }
        moreActionDialog!!.setOnDismissListener {
            moreActionDialog = null
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
            showCommentEt.isFocusable = true
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
        toast("已复制内容到剪贴板")
    }

    override fun onError(text: String) {
        toast(text)
        refreshLayout.finishRefresh(false)
        refreshLayout.finishLoadMore(false)
    }

    override fun onPause() {
        super.onPause()
        squareUserVideo.onVideoPause()
        if (mediaPlayer != null)
            mediaPlayer!!.pausePlay()
//        squareUserVideo.onVideoPause()
    }

    override fun onStart() {
        super.onStart()
        if (!enterPosition.isNullOrEmpty()) {
            showCommentEt.isFocusable = true
            showCommentEt.postDelayed({ KeyboardUtils.showSoftInput(showCommentEt) }, 500L)
        }
    }

    override fun onResume() {
        super.onResume()
        squareUserVideo.onVideoResume(false)
        if (mediaPlayer != null)
            mediaPlayer!!.resumePlay()
//        squareUserVideo.onVideoResume()
    }

    override fun onDestroy() {
        super.onDestroy()
//        squareUserVideo.release()
        if (mediaPlayer != null) {
            mediaPlayer!!.resetMedia()
            mediaPlayer = null
        }
        if (showCommentEt.isFocused)
            resetCommentEt()
//        GSYVideoManager.releaseAllVideos()
    }

    override fun onBackPressed() {
        //释放所有
        squareUserVideo.setVideoAllCallBack(null)
        if (showCommentEt.isFocused) {
            resetCommentEt()
        } else
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

    override fun onActivityReenter(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            val exitPos = data.getIntExtra(BigImageActivity.IMG_CURRENT_POSITION, -1)
            if (exitPos != -1) {
                var exitView = imgsAdapter.getViewByPosition(squareUserPics, exitPos, R.id.ivUser)
                if (exitView != null) {
                    ActivityCompat.setExitSharedElementCallback(this,
                        object : SharedElementCallback() {
                            override fun onMapSharedElements(
                                names: MutableList<String>?,
                                sharedElements: MutableMap<String, View>?
                            ) {
                                names?.clear()
                                sharedElements?.clear()
                                names?.add(ViewCompat.getTransitionName(exitView)!!)
                                sharedElements?.put(
                                    Objects.requireNonNull(ViewCompat.getTransitionName(exitView)!!),
                                    exitView
                                )
                                //清空回调，避免下次进入时共享元素混乱
                                setExitSharedElementCallback(object : SharedElementCallback() {})
                            }
                        })
                }
            }
        }
    }
}
