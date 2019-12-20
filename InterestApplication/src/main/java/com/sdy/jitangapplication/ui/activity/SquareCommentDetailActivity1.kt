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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.SPUtils
import com.kennyc.view.MultiStateView
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.NotifyEvent
import com.sdy.jitangapplication.event.RefreshCommentEvent
import com.sdy.jitangapplication.event.RefreshSquareEvent
import com.sdy.jitangapplication.event.UserCenterEvent
import com.sdy.jitangapplication.model.AllCommentBean
import com.sdy.jitangapplication.model.CommentBean
import com.sdy.jitangapplication.model.SquareBean
import com.sdy.jitangapplication.player.IjkMediaPlayerUtil
import com.sdy.jitangapplication.player.OnPlayingListener
import com.sdy.jitangapplication.presenter.SquareDetailPresenter
import com.sdy.jitangapplication.presenter.view.SquareDetailView
import com.sdy.jitangapplication.switchplay.SwitchUtil
import com.sdy.jitangapplication.switchplay.SwitchVideo
import com.sdy.jitangapplication.ui.adapter.MultiListCommentAdapter
import com.sdy.jitangapplication.ui.adapter.MultiListSquareAdapter
import com.sdy.jitangapplication.ui.dialog.CommentActionDialog
import com.sdy.jitangapplication.ui.dialog.DeleteDialog
import com.sdy.jitangapplication.ui.dialog.MoreActionNewDialog
import com.sdy.jitangapplication.ui.dialog.TranspondDialog
import com.sdy.jitangapplication.ui.fragment.MySquareFragment
import com.sdy.jitangapplication.utils.UserManager
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack
import com.shuyu.gsyvideoplayer.utils.GSYVideoType
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView
import kotlinx.android.synthetic.main.activity_square_comment_detail1.*
import kotlinx.android.synthetic.main.delete_dialog_layout.*
import kotlinx.android.synthetic.main.dialog_comment_action.*
import kotlinx.android.synthetic.main.dialog_more_action_new.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivity


/**
 * 广场详情页 包含内容详情以及点赞评论信息
 */
class SquareCommentDetailActivity1 : BaseMvpActivity<SquareDetailPresenter>(), SquareDetailView, View.OnClickListener,
    OnRefreshListener, OnLoadMoreListener, MultiListSquareAdapter.ResetAudioListener {
    override fun resetAudioState() {
        resetAudio()
    }

    private val TAG = SquareCommentDetailActivity1::class.java.simpleName
    //评论数据
    private var commentDatas: MutableList<CommentBean> = mutableListOf()
    private val commentAdapter: MultiListCommentAdapter by lazy { MultiListCommentAdapter(this, commentDatas) }
    private var squareBean: SquareBean? = null
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
            context.startActivity<SquareCommentDetailActivity1>(
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

    private val squareAdapter by lazy {
        MultiListSquareAdapter(
            mutableListOf(),
            resetAudioListener = this,
            type = MySquareFragment.TYPE_SQUARE_COMMENT
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_square_comment_detail1)
        initView()
        if (intent.getSerializableExtra("squareBean") != null) {
            stateview.viewState = MultiStateView.VIEW_STATE_CONTENT
            squareBean = intent.getSerializableExtra("squareBean") as SquareBean
            squareAdapter.addData(squareBean!!)
            if (squareBean!!.type == SquareBean.AUDIO) {
                initAudio(0)
                mediaPlayer!!.setDataSource(squareBean!!.audio_json?.get(0)?.url ?: "").prepareMedia()
            }
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

    private fun initAudio(position: Int) {
        resetAudio()
        mediaPlayer = IjkMediaPlayerUtil(this!!, position, object : OnPlayingListener {

            override fun onPlay(position: Int) {
                squareAdapter.data[position].isPlayAudio = IjkMediaPlayerUtil.MEDIA_PLAY
                squareAdapter.refreshNotifyItemChanged(position)
            }

            override fun onPause(position: Int) {
                squareAdapter.data[position].isPlayAudio = IjkMediaPlayerUtil.MEDIA_PAUSE
                squareAdapter.refreshNotifyItemChanged(position)
            }

            override fun onStop(position: Int) {
                squareAdapter.data[position].isPlayAudio = IjkMediaPlayerUtil.MEDIA_STOP
                resetAudio()
                squareAdapter.refreshNotifyItemChanged(position)
            }

            override fun onError(position: Int) {
                CommonFunction.toast("音频播放出错")
                squareAdapter.data[position].isPlayAudio = IjkMediaPlayerUtil.MEDIA_ERROR
                resetAudio()
                squareAdapter.refreshNotifyItemChanged(position)
            }

            override fun onPrepared(position: Int) {
                mediaPlayer!!.startPlay()
            }

            override fun onPreparing(position: Int) {
                squareAdapter.data[position].isPlayAudio = IjkMediaPlayerUtil.MEDIA_PREPARE
                squareAdapter.refreshNotifyItemChanged(position)
            }

            override fun onRelease(position: Int) {
                squareAdapter.data[position].isPlayAudio = IjkMediaPlayerUtil.MEDIA_STOP
                squareAdapter.refreshNotifyItemChanged(position)

            }

        }).getInstance()
    }

    private fun resetAudio() {
        if (mediaPlayer != null) {
            mediaPlayer!!.resetMedia()
            mediaPlayer = null
        }
    }


    private fun initView() {
        EventBus.getDefault().register(this)

        mPresenter = SquareDetailPresenter()
        mPresenter.mView = this
        mPresenter.context = this

        stateview.retryBtn.onClick {
            stateview.viewState = MultiStateView.VIEW_STATE_LOADING
            if (intent.getSerializableExtra("squareBean") != null) {
                stateview.viewState = MultiStateView.VIEW_STATE_CONTENT
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

        commentSquareRv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        commentSquareRv.adapter = squareAdapter
        squareAdapter.bindToRecyclerView(commentSquareRv)
        squareAdapter.setOnItemChildClickListener { _, view, position ->
            val squareBean = squareAdapter.data[0]
            when (view.id) {
                //播放音频
                R.id.audioPlayBtn -> {
                    if (squareBean.isPlayAudio == IjkMediaPlayerUtil.MEDIA_PREPARE || squareBean.isPlayAudio == IjkMediaPlayerUtil.MEDIA_ERROR) {
                        mediaPlayer!!.startPlay()
                    } else if (squareBean.isPlayAudio == IjkMediaPlayerUtil.MEDIA_PAUSE) {
                        mediaPlayer!!.resumePlay()
                    } else if (squareBean.isPlayAudio == IjkMediaPlayerUtil.MEDIA_PLAY) {
                        mediaPlayer!!.pausePlay()
                    }
                    commentAdapter.notifyDataSetChanged()
                }
            }
        }


        commentList.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        commentList.adapter = commentAdapter
        commentAdapter.setEmptyView(R.layout.empty_layout_comment, commentList)

        btnBack.onClick {
            onBackPressed()
        }
        hotT1.text = "动态详情"


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


        commentAdapter.setOnItemLongClickListener { adapter, view, position ->
            showCommentDialog(position)
            true
        }

        commentAdapter.setOnItemClickListener { _, view, position ->
            reply = true
            reply_id = commentAdapter.data[position].id!!.toInt()
            showCommentEt.isFocusable = true
            showCommentEt.hint = "『回复\t${commentAdapter.data[position].nickname}：』"
            KeyboardUtils.showSoftInput(showCommentEt)
        }


        commentAdapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.commentUser -> {
                    if ((commentAdapter.data[position].member_accid ?: "") != UserManager.getAccid())
                        MatchDetailActivity.start(this, commentAdapter.data[position].member_accid ?: "")
                }
                R.id.llCommentDianzanBtn -> {
                    mPresenter.getCommentLike(
                        hashMapOf(
                            "token" to UserManager.getToken(),
                            "accid" to UserManager.getAccid(),
                            "reply_id" to commentAdapter.data[position].id!!,
                            "type" to if (commentAdapter.data[position].isliked == 0) {
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
                    reply_id = commentAdapter.data[position].id!!
                    showCommentEt.hint = "『回复${commentAdapter.data[position].replyed_nickname}：』"
                    KeyboardUtils.showSoftInput(showCommentEt)
                }
            }
        }
    }


    var mediaPlayer: IjkMediaPlayerUtil? = null
    override fun onLoadMore(refreshLayout: RefreshLayout) {
        page++
        commentParams["page"] = page
        mPresenter.getCommentList(commentParams, false)
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        page = 1
        commentAdapter.data.clear()
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
                !data.audio_json.isNullOrEmpty() -> {
                    initAudio(0)
                    mediaPlayer!!.setDataSource(data.audio_json?.get(0)?.url ?: "").prepareMedia()
                    SquareBean.AUDIO
                }
                !data.photo_json.isNullOrEmpty() ||
                        (data.photo_json.isNullOrEmpty() && data.audio_json.isNullOrEmpty() && data.video_json.isNullOrEmpty()) -> SquareBean.PIC
                else -> SquareBean.PIC
            }
            squareBean = data
            squareAdapter.addData(squareBean!!)
            if (squareBean!!.type == SquareBean.AUDIO) {
                initAudio(0)
                mediaPlayer!!.setDataSource(squareBean!!.audio_json?.get(0)?.url ?: "").prepareMedia()
            }
            commentParams["square_id"] = "${squareBean!!.id}"
            mPresenter.getCommentList(commentParams, true)
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
                    commentAdapter.addData(CommentBean(content = "热门评论", type = CommentBean.TITLE))
                    for (i in 0 until allCommentBean.hotlist!!.size) {
                        allCommentBean.hotlist!![i]!!.type = CommentBean.CONTENT
                    }
                    commentAdapter.addData(allCommentBean.hotlist!!)
                }
                if (allCommentBean.list != null && allCommentBean.list!!.size > 0) {
                    commentAdapter.addData(CommentBean(content = "所有评论", type = CommentBean.TITLE))
                    for (i in 0 until allCommentBean.list!!.size) {
                        allCommentBean.list!![i]!!.type = CommentBean.CONTENT
                    }
                    commentAdapter.addData(allCommentBean.list!!)
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
                    commentAdapter.addData(allCommentBean.hotlist!!)
                }
                if (allCommentBean.list != null && allCommentBean.list!!.size > 0) {
                    for (i in 0 until allCommentBean.list!!.size) {
                        allCommentBean.list!![i]!!.type = 1
                    }
                    commentAdapter.addData(allCommentBean.list!!)
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
    }

    override fun onGetSquareReport(data: BaseResp<Any?>?) {
        if (data != null)
            CommonFunction.toast(data.msg)
    }


    override fun onAddCommentResult(data: BaseResp<Any?>?, result: Boolean) {
        resetCommentEt()
        if (result) {
            page = 1
            commentAdapter.data.clear()
            commentParams["page"] = page
            mPresenter.getCommentList(commentParams, true)
            squareBean!!.comment_cnt = squareBean!!.comment_cnt.plus(1)
            EventBus.getDefault().post(RefreshCommentEvent(squareBean!!.comment_cnt, intent.getIntExtra("position", 0)))
            squareAdapter.data[0].comment_cnt = squareBean!!.comment_cnt
            squareAdapter.notifyItemChanged(0)
        }
    }

    override fun onLikeCommentResult(data: BaseResp<Any?>, position: Int) {
        if (data.code == 200) {
            commentAdapter.data[position].isliked = if (commentAdapter.data[position].isliked == 0) {
                commentAdapter.data[position].like_count = commentAdapter.data[position].like_count!!.plus(1)
                1
            } else {
                commentAdapter.data[position].like_count = commentAdapter.data[position].like_count!!.minus(1)
                0
            }
            commentAdapter.notifyItemChanged(position)
        } else {
            CommonFunction.toast(data.msg)
        }
    }

    override fun onDeleteCommentResult(data: BaseResp<Any?>, position: Int) {
        if (data.msg == "删除成功!") {
            commentAdapter.data.removeAt(position)
            commentAdapter.notifyItemRemoved(position)
            squareBean!!.comment_cnt = squareBean!!.comment_cnt.minus(1)
            EventBus.getDefault().post(RefreshCommentEvent(squareBean!!.comment_cnt, intent.getIntExtra("position", 0)))
            squareAdapter.data[0].comment_cnt = squareBean!!.comment_cnt
            squareAdapter.notifyItemChanged(0)
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
            dialog.tip.text = getString(R.string.report_square)
            dialog.title.text = "动态举报"
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
            EventBus.getDefault().postSticky(UserCenterEvent(true))
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
            if (commentAdapter.data[position].member_accid!! == UserManager.getAccid())
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
            reply_id = commentAdapter.data[position].id!!
            showCommentEt.hint = "『回复\t${commentAdapter.data[position].nickname}：』"
            showCommentEt.postDelayed({ KeyboardUtils.showSoftInput(showCommentEt) }, 100L)

            commentActionDialog!!.dismiss()
        }

        commentActionDialog!!.jubaoComment.onClick {
            // 举报
            mPresenter.commentReport(
                hashMapOf(
                    "token" to UserManager.getToken(),
                    "accid" to UserManager.getAccid(),
                    "id" to commentAdapter.data[position].id!!
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
                    "id" to commentAdapter.data[position].id!!
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
        val clipData = ClipData.newPlainText("label", "${commentAdapter.data[position].content}")
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
        if (mediaPlayer != null)
            mediaPlayer!!.resumePlay()
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

//        GSYVideoManager.releaseAllVideos()
        EventBus.getDefault().unregister(this)
    }


    override fun finish() {
        super.finish()
        if (mediaPlayer != null) {
            mediaPlayer!!.resetMedia()
            mediaPlayer = null
        }
        if (showCommentEt.isFocused)
            resetCommentEt()
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


    /**
     * 无缝切换小屏和全屏
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNotifyEvent(event: NotifyEvent) {
        val pos = event.position
        val switchVideo = squareAdapter.getViewByPosition(pos, R.id.squareUserVideo) as SwitchVideo
        SwitchUtil.clonePlayState(switchVideo)
        val state = switchVideo.currentState
        //延迟加2S
        switchVideo.seekOnStart = switchVideo.gsyVideoManager.currentPosition
        switchVideo.startPlayLogic()
        switchVideo.setVideoAllCallBack(object : GSYSampleCallBack() {
            override fun onStartPrepared(url: String?, vararg objects: Any?) {
                super.onStartPrepared(url, *objects)
                GSYVideoManager.instance().isNeedMute = false
            }

            override fun onPrepared(url: String?, vararg objects: Any?) {
                super.onPrepared(url, *objects)
                GSYVideoManager.instance().isNeedMute = false
                if (state == GSYVideoView.CURRENT_STATE_PAUSE) {
                    switchVideo.onVideoPause()
                } else if (state == GSYVideoView.CURRENT_STATE_AUTO_COMPLETE || state == GSYVideoView.CURRENT_STATE_ERROR) {
                    SwitchUtil.release()
                    GSYVideoManager.releaseAllVideos()
                }
            }

            override fun onClickResume(url: String?, vararg objects: Any?) {
                super.onClickResume(url, *objects)
                switchVideo.onVideoResume()
            }

            override fun onAutoComplete(url: String?, vararg objects: Any?) {
                super.onAutoComplete(url, *objects)
                SwitchUtil.release()
                GSYVideoManager.releaseAllVideos()
                squareAdapter.refreshNotifyItemChanged(pos)

            }
        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK)
            if (requestCode == SquarePlayDetailActivity.REQUEST_CODE) {
                onNotifyEvent(NotifyEvent(data!!.getIntExtra("position", -1)))
            }
    }

}
