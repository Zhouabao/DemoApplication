package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
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
import com.sdy.jitangapplication.event.RefreshCommentEvent
import com.sdy.jitangapplication.event.RefreshSquareEvent
import com.sdy.jitangapplication.model.SquareBean
import com.sdy.jitangapplication.model.SquareListBean
import com.sdy.jitangapplication.player.IjkMediaPlayerUtil
import com.sdy.jitangapplication.player.OnPlayingListener
import com.sdy.jitangapplication.presenter.MyCollectionPresenter
import com.sdy.jitangapplication.presenter.view.MyCollectionView
import com.sdy.jitangapplication.switchplay.SwitchUtil
import com.sdy.jitangapplication.switchplay.SwitchVideo
import com.sdy.jitangapplication.ui.adapter.MultiListSquareAdapter
import com.sdy.jitangapplication.ui.dialog.DeleteDialog
import com.sdy.jitangapplication.ui.dialog.MoreActionNewDialog
import com.sdy.jitangapplication.ui.dialog.TranspondDialog
import com.sdy.jitangapplication.utils.ScrollCalculatorHelper
import com.sdy.jitangapplication.utils.UserManager
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack
import com.shuyu.gsyvideoplayer.utils.GSYVideoType
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView
import com.umeng.socialize.UMShareAPI
import kotlinx.android.synthetic.main.activity_my_collection_etc.*
import kotlinx.android.synthetic.main.delete_dialog_layout.*
import kotlinx.android.synthetic.main.dialog_more_action_new.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 我的收藏、我的点赞、我的动态
 *  1,我的所有动态 2我点过赞的 3 我收藏的
 */
class MyCollectionEtcActivity : BaseMvpActivity<MyCollectionPresenter>(), MyCollectionView, OnRefreshListener,
    OnLoadMoreListener, MultiListSquareAdapter.ResetAudioListener {
    private val TAG = MyCollectionEtcActivity::class.java.simpleName

    private val type by lazy { intent.getIntExtra("type", 0) }

    //当前请求页
    var page = 1
    //请求广场的参数 TODO要更新tagid
    private val params by lazy {
        hashMapOf(
            "accid" to UserManager.getAccid(),
            "token" to UserManager.getToken(),
            "type" to type,
            "page" to page,
            "pagesize" to Constants.PAGESIZE
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_collection_etc)
        initView()
        refreshLayout.autoRefresh()

    }


    //广场列表内容适配器
    private val adapter by lazy { MultiListSquareAdapter(mutableListOf(), resetAudioListener = this) }

    override fun resetAudioState() {
        //                adapter.notifyItemChanged(position)
        if (mediaPlayer != null) {
            mediaPlayer!!.resetMedia()
            mediaPlayer = null
        }
        currPlayIndex = -1
    }

    private lateinit var scrollCalculatorHelper: ScrollCalculatorHelper


    val layoutManager by lazy { LinearLayoutManager(this, RecyclerView.VERTICAL, false) }


    private var currPlayIndex = -1

    private fun initView() {
        EventBus.getDefault().register(this)

        mPresenter = MyCollectionPresenter()
        mPresenter.mView = this
        mPresenter.context = this
        refreshLayout.setOnRefreshListener(this)
        refreshLayout.setOnLoadMoreListener(this)
        refreshLayout.setEnableAutoLoadMore(true)

        btnBack.onClick {
            finish()
        }
        hotT1.text = when (type) {
            1 -> {
                "我的动态"
            }
            2 -> {
                "我点赞过的"
            }
            3 -> {
                "我收藏的"
            }
            else -> {
                ""
            }
        }


        stateview.retryBtn.onClick {
            stateview.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.getMySquare(params)
        }

        collectionRv.addItemDecoration(
            com.sdy.jitangapplication.widgets.DividerItemDecoration(
                this,
                com.sdy.jitangapplication.widgets.DividerItemDecoration.HORIZONTAL_LIST,
                SizeUtils.dp2px(8F),
                Color.parseColor("#FFF1F2F6")
            )
        )



        collectionRv.layoutManager = layoutManager
        collectionRv.adapter = adapter
        adapter.setEmptyView(R.layout.empty_layout, collectionRv)
        adapter.setHeaderAndEmpty(true)
        adapter.bindToRecyclerView(collectionRv)
        //取消动画，主要是闪烁
//        (collectionRv.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        collectionRv.itemAnimator?.changeDuration = 0

        //限定范围为屏幕一半的上下偏移180
        val playTop = ScreenUtils.getScreenHeight() / 2 - SizeUtils.dp2px(126F)
        val playBottom = ScreenUtils.getScreenHeight() / 2 + SizeUtils.dp2px(126F)
        scrollCalculatorHelper = ScrollCalculatorHelper(R.id.llVideo, R.id.squareUserVideo, playTop, playBottom)
        collectionRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var firstVisibleItem = 0
            var lastVisibleItem = 0
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                scrollCalculatorHelper.onScrollStateChanged(recyclerView, newState)
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                firstVisibleItem = layoutManager.findFirstVisibleItemPosition()
                lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                //滑动自动播放
                scrollCalculatorHelper.onScroll(
                    recyclerView,
                    firstVisibleItem,
                    lastVisibleItem,
                    lastVisibleItem - firstVisibleItem
                )
            }
        })

        adapter.setOnItemClickListener { _, view, position ->
            resetAudio()
            SquareCommentDetailActivity.start(this, adapter.data[position], position = position)
        }

        adapter.setOnItemChildClickListener { _, view, position ->
            val squareBean = adapter.data[position]
            when (view.id) {
                R.id.squareChatBtn1 -> {
                }
                R.id.squareCommentBtn1 -> {
                    resetAudio()
                    SquareCommentDetailActivity.start(
                        this,
                        adapter.data[position],
                        enterPosition = "comment",
                        position = position
                    )
                }
                R.id.squareDianzanBtn1 -> {
                    //clickZan(position)
                }
                R.id.squareZhuanfaBtn1 -> {
                    showTranspondDialog(squareBean)
                }
                R.id.squareMoreBtn1 -> {
                    showMoreDialog(position)
                }
                //播放音频
                R.id.audioPlayBtn -> {
                    if (currPlayIndex != position && squareBean.isPlayAudio != IjkMediaPlayerUtil.MEDIA_PLAY) {
                        initAudio(position)
                        mediaPlayer!!.setDataSource(squareBean.audio_json?.get(0)?.url ?: "").prepareMedia()
                        currPlayIndex = position
                    }
                    for (index in 0 until adapter.data.size) {
                        if (index != position && adapter.data[index].type == 3) {
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

    }


    /**
     * 点赞按钮
     */
    private fun clickZan(position: Int) {
        val squareBean = adapter.data[position]
        if (adapter.data[position].isliked == 1) {
            adapter.data[position].isliked = 0
            adapter.data[position].like_cnt = adapter.data[position].like_cnt!!.minus(1)
        } else {
            adapter.data[position].isliked = 1
            adapter.data[position].like_cnt = adapter.data[position].like_cnt!!.plus(1)
        }
//        adapter.notifyItemChanged(position + adapter.headerLayoutCount)
        adapter.notifyDataSetChanged()
        Handler().postDelayed({
            if (squareBean.originalLike == squareBean.isliked) {
                return@postDelayed
            }
            val params = hashMapOf(
                "token" to SPUtils.getInstance(Constants.SPNAME).getString("token"),
                "accid" to SPUtils.getInstance(Constants.SPNAME).getString("accid"),
                "type" to if (squareBean.isliked == 0) {
                    2
                } else {
                    1
                },
                "square_id" to squareBean.id!!,
                "_timestamp" to System.currentTimeMillis()
            )
            mPresenter.getSquareLike(params, position)
        }, 2000L)

    }


    var mediaPlayer: IjkMediaPlayerUtil? = null

    private fun initAudio(position: Int) {
        resetAudio()
        mediaPlayer = IjkMediaPlayerUtil(this, position, object : OnPlayingListener {

            override fun onPlay(position: Int) {
                adapter.data[position].isPlayAudio = IjkMediaPlayerUtil.MEDIA_PLAY
                adapter.notifyItemChanged(position)
//                adapter.notifyDataSetChanged()

            }

            override fun onPause(position: Int) {
                adapter.data[position].isPlayAudio = IjkMediaPlayerUtil.MEDIA_PAUSE
                adapter.notifyItemChanged(position)
//                adapter.notifyDataSetChanged()
            }

            override fun onStop(position: Int) {
                adapter.data[position].isPlayAudio = IjkMediaPlayerUtil.MEDIA_STOP
                resetAudio()
//                adapter.notifyDataSetChanged()
                adapter.notifyItemChanged(position)
            }

            override fun onError(position: Int) {
                CommonFunction.toast("音频播放出错")
                adapter.data[position].isPlayAudio = IjkMediaPlayerUtil.MEDIA_ERROR
                resetAudio()
//                adapter.notifyDataSetChanged()
                adapter.notifyItemChanged(position)
            }

            override fun onPrepared(position: Int) {
//                adapter.data[position].isPlayAudio = IjkMediaPlayerUtil.MEDIA_PLAY
//                adapter.notifyItemChanged(position)
//                adapter.notifyDataSetChanged()
                mediaPlayer!!.startPlay()
            }

            override fun onPreparing(position: Int) {
                adapter.data[position].isPlayAudio = IjkMediaPlayerUtil.MEDIA_PREPARE
                adapter.notifyItemChanged(position)

            }

            override fun onRelease(position: Int) {
                adapter.data[position].isPlayAudio = IjkMediaPlayerUtil.MEDIA_STOP
                adapter.notifyItemChanged(position)
//                adapter.notifyDataSetChanged()
            }

        }).getInstance()
    }


    private fun resetAudio() {
        currPlayIndex = -1
        //                adapter.notifyItemChanged(position)
        if (mediaPlayer != null) {
            mediaPlayer!!.resetMedia()
            mediaPlayer = null
        }
    }


    /**
     * 展示转发动态对话框
     */
    private fun showTranspondDialog(squareBean: SquareBean) {
        val transpondDialog = TranspondDialog(this, squareBean)
        transpondDialog.show()
    }


//    lateinit var moreActionDialog: MoreActionDialog
//    /**
//     * 展示更多操作对话框
//     */
//    private fun showMoreDialog(position: Int) {
//        moreActionDialog = MoreActionDialog(this, "square")
//        moreActionDialog.show()
//
//        if (adapter.data[position]?.iscollected == 0) {
//            moreActionDialog.collect.text = "收藏"
//            moreActionDialog.collectBtn.setImageResource(R.drawable.icon_collect_no)
//        } else {
//            moreActionDialog.collect.text = "取消收藏"
//            moreActionDialog.collectBtn.setImageResource(R.drawable.icon_collectt)
//        }
//
//        if (adapter.data[position].accid == UserManager.getAccid()) {
//            moreActionDialog.llDelete.visibility = View.VISIBLE
//            moreActionDialog.llJubao.visibility = View.GONE
//            moreActionDialog.llCollect.visibility = View.GONE
//        } else {
//            moreActionDialog.llDelete.visibility = View.GONE
//            moreActionDialog.llJubao.visibility = View.VISIBLE
//            moreActionDialog.llCollect.visibility = View.VISIBLE
//        }
//        moreActionDialog.llDelete.onClick {
//            val params = hashMapOf(
//                "accid" to SPUtils.getInstance(Constants.SPNAME).getString("accid"),
//                "token" to SPUtils.getInstance(Constants.SPNAME).getString("token"),
//                "square_id" to adapter.data[position].id!!
//            )
//            mPresenter.removeMySquare(params, position)
//            moreActionDialog.dismiss()
//
//        }
//
//
//
//        moreActionDialog.llCollect.onClick {
//            //发起收藏请求
//            val params = hashMapOf(
//                "accid" to SPUtils.getInstance(Constants.SPNAME).getString("accid"),
//                "token" to SPUtils.getInstance(Constants.SPNAME).getString("token"),
//                "type" to if (adapter.data[position].iscollected == 0) {
//                    1
//                } else {
//                    2
//                },
//                "square_id" to adapter.data[position].id!!,
//                "_timestamp" to System.currentTimeMillis()
//            )
//            mPresenter.getSquareCollect(params, position)
//        }
//        moreActionDialog.llJubao.onClick {
//            //发起举报请求
//            val params = hashMapOf(
//                "accid" to SPUtils.getInstance(Constants.SPNAME).getString("accid"),
//                "token" to SPUtils.getInstance(Constants.SPNAME).getString("token"),
//                "type" to if (adapter.data[position].iscollected == 0) {
//                    1
//                } else {
//                    2
//                },
//                "square_id" to adapter.data[position].id!!,
//                "_timestamp" to System.currentTimeMillis()
//            )
//            mPresenter.getSquareReport(params, position)
//        }
//        moreActionDialog.cancel.onClick {
//            moreActionDialog.dismiss()
//        }
//
//    }


    lateinit var moreActionDialog: MoreActionNewDialog
    /**
     * 展示更多操作对话框
     */
    private fun showMoreDialog(position: Int) {
        moreActionDialog = MoreActionNewDialog(this, adapter.data[position])
        moreActionDialog.show()

        if (adapter.data[position]?.iscollected == 0) {
            moreActionDialog.collect.text = "收藏"
            val top = resources.getDrawable(R.drawable.icon_collect1)
            moreActionDialog.collect.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null)
        } else {
            moreActionDialog.collect.text = "取消收藏"
            val top = resources.getDrawable(R.drawable.icon_collected1)
            moreActionDialog.collect.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null)
        }
        if (adapter.data[position].accid == UserManager.getAccid()) {
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
                "square_id" to adapter.data[position].id!!
            )
            mPresenter.removeMySquare(params, position)
            moreActionDialog.dismiss()

        }


        moreActionDialog.collect.onClick {

            //发起收藏请求
            val params = hashMapOf(
                "accid" to SPUtils.getInstance(Constants.SPNAME).getString("accid"),
                "token" to SPUtils.getInstance(Constants.SPNAME).getString("token"),
                "type" to if (adapter.data[position].iscollected == 0) {
                    1
                } else {
                    2
                },
                "square_id" to adapter.data[position].id!!,
                "_timestamp" to System.currentTimeMillis()
            )
            mPresenter.getSquareCollect(params, position)
        }
        moreActionDialog.report.onClick {
            val dialog = DeleteDialog(this)
            dialog.show()
            dialog.tip.text = getString(R.string.report_square)
            dialog.confirm.text = "举报"
            dialog.cancel.onClick { dialog.dismiss() }
            dialog.confirm.onClick {
                dialog.dismiss()
                //发起举报请求
                val params = hashMapOf(
                    "accid" to SPUtils.getInstance(Constants.SPNAME).getString("accid"),
                    "token" to SPUtils.getInstance(Constants.SPNAME).getString("token"),
                    "type" to if (adapter.data[position].iscollected == 0) {
                        1
                    } else {
                        2
                    },
                    "square_id" to adapter.data[position].id!!,
                    "_timestamp" to System.currentTimeMillis()
                )
                mPresenter.getSquareReport(params, position)

            }

        }

    }

    private var deleteTag = false
    override fun onRemoveMySquareResult(result: Boolean, position: Int) {
        if (result) {
            if (adapter.data[position].type == SquareBean.AUDIO) {
                resetAudio()
            } else if (adapter.data[position].type == SquareBean.VIDEO) {
                GSYVideoManager.releaseAllVideos()
            }
            adapter.data.removeAt(position)
            adapter.notifyItemRemoved(position + adapter.headerLayoutCount)

            EventBus.getDefault().post(RefreshSquareEvent(true, TAG))
            deleteTag = true
        }
    }


    override fun onPause() {
        super.onPause()
//        GSYVideoManager.onPause()
    }

    override fun onResume() {
        super.onResume()
        GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_DEFAULT)
//        GSYVideoManager.onResume(false)
    }


    override fun onDestroy() {
        super.onDestroy()
        GSYVideoManager.releaseAllVideos()
        EventBus.getDefault().unregister(this)

        resetAudio()
    }

    override fun finish() {
        if (deleteTag)
            setResult(Activity.RESULT_OK, intent)
        super.finish()
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        refreshLayout.setNoMoreData(false)
        resetAudio()
        page = 1
        params["page"] = page
        adapter.data.clear()
        mPresenter.getMySquare(params)
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        if (adapter.data.size < Constants.PAGESIZE * page) {
            refreshLayout.finishLoadMoreWithNoMoreData()
        } else {
            page++
            params["page"] = page
            mPresenter.getMySquare(params)
        }
    }


    override fun onGetSquareListResult(data: SquareListBean?) {
        stateview.viewState = MultiStateView.VIEW_STATE_CONTENT
        if (data?.list != null && data.list!!.size > 0) {
            for (tempData in 0 until data.list!!.size) {
                data.list!![tempData].type = when {
                    !data.list!![tempData].video_json.isNullOrEmpty() -> SquareBean.VIDEO
                    !data.list!![tempData].audio_json.isNullOrEmpty() -> SquareBean.AUDIO
                    !data.list!![tempData].photo_json.isNullOrEmpty() || (data.list!![tempData].photo_json.isNullOrEmpty() && data.list!![tempData].audio_json.isNullOrEmpty() && data.list!![tempData].video_json.isNullOrEmpty()) -> SquareBean.PIC
                    else -> SquareBean.PIC
                }
                data.list!![tempData].originalLike = data.list!![tempData].isliked
                data.list!![tempData].originalLikeCount = data.list!![tempData].like_cnt
            }
            adapter.addData(data.list!!)
        }
        adapter.notifyDataSetChanged()
        refreshLayout.finishRefresh(true)
        refreshLayout.finishLoadMore(true)
        refreshLayout.setEnableLoadMoreWhenContentNotFull(false)
    }

    override fun onGetSquareLikeResult(position: Int, result: Boolean) {
        if (result) {
            adapter.data[position].originalLike = adapter.data[position].isliked
            EventBus.getDefault().post(RefreshSquareEvent(true, TAG))
        } else {
            adapter.data[position].isliked = adapter.data[position].originalLike
            adapter.data[position].like_cnt = adapter.data[position].originalLikeCount
            adapter.refreshNotifyItemChanged(position)
        }
    }

    override fun onGetSquareCollectResult(position: Int, data: BaseResp<Any?>?) {
        if (data != null) {
            CommonFunction.toast(data.msg)
            if (data.code == 200) {
                if (adapter.data[position].iscollected == 1) {
                    adapter.data[position].iscollected = 0
                } else {
                    adapter.data[position].iscollected = 1
                }
                adapter.notifyDataSetChanged()
                if (moreActionDialog != null && moreActionDialog.isShowing) {
                    moreActionDialog.dismiss()
                }
                EventBus.getDefault().post(RefreshSquareEvent(true))
            }
        }
    }

    override fun onGetSquareReport(baseResp: BaseResp<Any?>?, position: Int) {
        if (baseResp != null)
            CommonFunction.toast(baseResp.msg)
        if (moreActionDialog != null && moreActionDialog.isShowing) {
            moreActionDialog.dismiss()
        }
    }


    override fun showLoading() {
        stateview.viewState = MultiStateView.VIEW_STATE_LOADING
    }

    override fun onError(text: String) {
        adapter.notifyDataSetChanged()
        refreshLayout.finishRefresh(false)
        refreshLayout.finishLoadMore(false)
        stateview.viewState = MultiStateView.VIEW_STATE_ERROR
        stateview.errorMsg.text = if (mPresenter.checkNetWork()) {
            this.getString(R.string.retry_load_error)
        } else {
            this.getString(R.string.retry_net_error)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SquarePlayDetailActivity.REQUEST_CODE) {
                val pos = data!!.getIntExtra("position", -1)
                GSYVideoManager.releaseAllVideos()

//        adapter.notifyDataSetChanged()
                //静音
                val switchVideo = adapter.getViewByPosition(pos, R.id.squareUserVideo) as SwitchVideo
                SwitchUtil.clonePlayState(switchVideo)
                val state = switchVideo.currentState
//        switchVideo.isStartAfterPrepared = false
                //延迟加2S
                switchVideo.seekOnStart = switchVideo.gsyVideoManager.currentPosition
                switchVideo.startPlayLogic()
                switchVideo.setVideoAllCallBack(object : GSYSampleCallBack() {
                    override fun onStartPrepared(url: String?, vararg objects: Any?) {
                        super.onStartPrepared(url, *objects)
                        GSYVideoManager.instance().isNeedMute = true
                    }

                    override fun onPrepared(url: String?, vararg objects: Any?) {
                        super.onPrepared(url, *objects)
                        GSYVideoManager.instance().isNeedMute = true
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
                        adapter.notifyItemChanged(pos)

                    }
                })
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshSquareEvent(event: RefreshSquareEvent) {
        if (event.from != TAG)
            refreshLayout.autoRefresh()
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshCommentEvent(event: RefreshCommentEvent) {
        if (event.position != -1) {
            adapter.data[event.position].comment_cnt = event.commentNum
            adapter.refreshNotifyItemChanged(event.position)
        }
    }
}
