package com.sdy.jitangapplication.ui.fragment


import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.ToastUtils
import com.google.gson.Gson
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.fragment.BaseMvpFragment
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.sdy.baselibrary.utils.RandomUtils
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.*
import com.sdy.jitangapplication.model.FriendBean
import com.sdy.jitangapplication.model.SquareBean
import com.sdy.jitangapplication.model.SquareListBean
import com.sdy.jitangapplication.nim.activity.ChatActivity
import com.sdy.jitangapplication.player.IjkMediaPlayerUtil
import com.sdy.jitangapplication.player.OnPlayingListener
import com.sdy.jitangapplication.presenter.SquarePresenter
import com.sdy.jitangapplication.presenter.view.SquareView
import com.sdy.jitangapplication.switchplay.SwitchUtil
import com.sdy.jitangapplication.switchplay.SwitchVideo
import com.sdy.jitangapplication.ui.activity.PublishActivity
import com.sdy.jitangapplication.ui.activity.SquareCommentDetailActivity
import com.sdy.jitangapplication.ui.activity.SquarePlayListDetailActivity
import com.sdy.jitangapplication.ui.activity.UserCenterActivity
import com.sdy.jitangapplication.ui.adapter.MultiListSquareAdapter
import com.sdy.jitangapplication.ui.adapter.SquareFriendsAdapter
import com.sdy.jitangapplication.ui.dialog.MoreActionDialog
import com.sdy.jitangapplication.ui.dialog.TranspondDialog
import com.sdy.jitangapplication.utils.ScrollCalculatorHelper
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.CommonAlertDialog
import com.sdy.jitangapplication.widgets.CommonItemDecoration
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack
import com.shuyu.gsyvideoplayer.utils.GSYVideoType
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView.CURRENT_STATE_ERROR
import kotlinx.android.synthetic.main.dialog_more_action.*
import kotlinx.android.synthetic.main.error_layout.*
import kotlinx.android.synthetic.main.fragment_square.*
import kotlinx.android.synthetic.main.headerview_label.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast


/**
 * 广场列表
 */
class SquareFragment : BaseMvpFragment<SquarePresenter>(), SquareView, OnRefreshListener, OnLoadMoreListener,
    View.OnClickListener, MultiListSquareAdapter.ResetAudioListener {


    override fun resetAudioState() {
        if (mediaPlayer != null) {
            mediaPlayer!!.resetMedia()
            mediaPlayer = null
            currPlayIndex = -1
        }
    }

    //广场列表内容适配器
    private val adapter by lazy { MultiListSquareAdapter(mutableListOf(), resetAudioListener = this) }

    //广场好友适配器
    private val friendsAdapter: SquareFriendsAdapter by lazy { SquareFriendsAdapter(userList) }
    //好友信息用户数据源
    var userList: MutableList<FriendBean> = mutableListOf()


    private lateinit var scrollCalculatorHelper: ScrollCalculatorHelper


    val layoutManager by lazy { LinearLayoutManager(activity!!, RecyclerView.VERTICAL, false) }

    //当前请求页
    var page = 1
    //请求广场的参数 TODO要更新tagid
    private val listParams by lazy {
        hashMapOf(
            "accid" to UserManager.getAccid(),
            "token" to UserManager.getToken(),
            "page" to page,
            "pagesize" to Constants.PAGESIZE,
            "_timestamp" to System.currentTimeMillis(),
            "tagid" to SPUtils.getInstance(Constants.SPNAME).getInt("globalLabelId")
        )
    }

    //好友的params
    private val friendsParams = hashMapOf(
        "accid" to SPUtils.getInstance(Constants.SPNAME).getString("accid"),
        "token" to SPUtils.getInstance(Constants.SPNAME).getString("token")
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_square, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()

    }


    //创建好友布局
    private fun initFriendsView(): View {
        val friendsView = LayoutInflater.from(activity!!).inflate(R.layout.headerview_label, squareDynamicRv, false)
        val linearLayoutManager =
            LinearLayoutManager(activity?.applicationContext, LinearLayoutManager.HORIZONTAL, false)
        friendsView.headRv.layoutManager = linearLayoutManager
        friendsView.headRv.adapter = friendsAdapter
        friendsAdapter.addData(userList)
        friendsAdapter.setOnItemClickListener { adapter, view, position ->
            resetAudio()
            startActivity<SquarePlayListDetailActivity>("target_accid" to (friendsAdapter.data[position].accid ?: 0))
        }

        return friendsView
    }


    private var currPlayIndex = -1

    private fun initView() {
//        GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_4_3)
        //注册eventbus
        EventBus.getDefault().register(this)
        mPresenter = SquarePresenter()
        mPresenter.mView = this
        mPresenter.context = activity!!
        refreshLayout.setOnRefreshListener(this)
        refreshLayout.setOnLoadMoreListener(this)
        squareEdit.setOnClickListener(this)


        retryBtn.onClick {
            setViewState(LOADING)
//            这个地方还要默认设置选中第一个标签来更新数据
            mPresenter.getSquareList(listParams, true, true)
            mPresenter.getFrinedsList(friendsParams)
        }


        val itemdecoration = CommonItemDecoration(activity!!, DividerItemDecoration.VERTICAL)
        itemdecoration.setDrawable(activity!!.resources.getDrawable(R.drawable.recycler_divider))
        squareDynamicRv.addItemDecoration(itemdecoration)

        adapter.setHeaderAndEmpty(true)
        squareDynamicRv.layoutManager = layoutManager
        squareDynamicRv.adapter = adapter
        adapter.bindToRecyclerView(squareDynamicRv)
        adapter.addHeaderView(initFriendsView())
        adapter.setEmptyView(R.layout.empty_layout, squareDynamicRv)
        //取消动画，主要是闪烁
//        (squareDynamicRv.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        squareDynamicRv.itemAnimator?.changeDuration = 0

        //限定范围为屏幕一半的上下偏移180
        val playTop = ScreenUtils.getScreenHeight() / 2 - SizeUtils.dp2px(126F)
        val playBottom = ScreenUtils.getScreenHeight() / 2 + SizeUtils.dp2px(126F)
        scrollCalculatorHelper = ScrollCalculatorHelper(R.id.llVideo, R.id.squareUserVideo, playTop, playBottom)
        squareDynamicRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
//                if (lastVisibleItem >= adapter.itemCount - 5) {
//                    refreshLayout.autoLoadMore()
//                }
            }
        })

        adapter.setOnItemClickListener { _, view, position ->
            resetAudio()
            SquareCommentDetailActivity.start(activity!!, adapter.data[position], position = position)
        }

        adapter.setOnItemChildClickListener { _, view, position ->
            val squareBean = adapter.data[position]
            when (view.id) {
                R.id.squareChatBtn1 -> {
                    resetAudio()
                    ChatActivity.start(activity!!, adapter.data[position].accid ?: "")
                }
                R.id.squareCommentBtn1 -> {
                    resetAudio()
                    SquareCommentDetailActivity.start(
                        activity!!,
                        adapter.data[position],
                        enterPosition = "comment",
                        position = position
                    )
                }
                R.id.squareDianzanBtn1 -> {
                    clickZan(position)
                }
                R.id.squareZhuanfaBtn1 -> {
                    showTranspondDialog(adapter.data[position])
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
                        if (index != currPlayIndex && adapter.data[index].type == 3) {
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


        //这个地方还要默认设置选中第一个标签来更新数据
        mPresenter.getSquareList(listParams, true, true)
        mPresenter.getFrinedsList(friendsParams)

    }


    /**
     * 点赞按钮
     */
    private fun clickZan(position: Int) {
        if (adapter.data[position].isliked == 1) {
            adapter.data[position].isliked = 0
            adapter.data[position].like_cnt = adapter.data[position].like_cnt!!.minus(1)
        } else {
            adapter.data[position].isliked = 1
            adapter.data[position].like_cnt = adapter.data[position].like_cnt!!.plus(1)
        }
//        adapter.refreshNotifyItemChanged(position)
        adapter.notifyDataSetChanged()
        Handler().postDelayed({
            if (adapter.data[position].originalLike == adapter.data[position].isliked) {
                return@postDelayed
            }
            val params = hashMapOf(
                "token" to SPUtils.getInstance(Constants.SPNAME).getString("token"),
                "accid" to SPUtils.getInstance(Constants.SPNAME).getString("accid"),
                "type" to if (adapter.data[position].isliked == 0) {
                    2
                } else {
                    1
                },
                "square_id" to adapter.data[position].id!!,
                "_timestamp" to System.currentTimeMillis()
            )
            mPresenter.getSquareLike(params, position)
        }, 2000L)

    }

    var mediaPlayer: IjkMediaPlayerUtil? = null

    private fun initAudio(position: Int) {
        resetAudio()
        mediaPlayer = IjkMediaPlayerUtil(activity!!, position, object : OnPlayingListener {

            override fun onPlay(position: Int) {
                adapter.data[position].isPlayAudio = IjkMediaPlayerUtil.MEDIA_PLAY
//                adapter.notifyItemChanged(position)
                adapter.refreshNotifyItemChanged(position)

            }

            override fun onPause(position: Int) {
                adapter.data[position].isPlayAudio = IjkMediaPlayerUtil.MEDIA_PAUSE
                adapter.refreshNotifyItemChanged(position)
            }

            override fun onStop(position: Int) {
                adapter.data[position].isPlayAudio = IjkMediaPlayerUtil.MEDIA_STOP
                resetAudio()
                adapter.refreshNotifyItemChanged(position)
            }

            override fun onError(position: Int) {
                toast("音频播放出错")
                adapter.data[position].isPlayAudio = IjkMediaPlayerUtil.MEDIA_ERROR
                resetAudio()
                adapter.refreshNotifyItemChanged(position)
            }

            override fun onPrepared(position: Int) {
                mediaPlayer!!.startPlay()
            }

            override fun onPreparing(position: Int) {
                adapter.data[position].isPlayAudio = IjkMediaPlayerUtil.MEDIA_PREPARE
                adapter.refreshNotifyItemChanged(position)
            }

            override fun onRelease(position: Int) {
                adapter.data[position].isPlayAudio = IjkMediaPlayerUtil.MEDIA_STOP
                adapter.refreshNotifyItemChanged(position)

            }

        }).getInstance()
    }

    private fun resetAudio() {
        currPlayIndex = -1
        if (mediaPlayer != null) {
            mediaPlayer!!.resetMedia()
            mediaPlayer = null
        }
    }


    /**
     * 展示转发动态对话框
     */
    private fun showTranspondDialog(squareBean: SquareBean) {
        val transpondDialog = TranspondDialog(activity!!, squareBean)
        transpondDialog.show()
//        transpondDialog.squareBean = squareBean
    }


    lateinit var moreActionDialog: MoreActionDialog
    /**
     * 展示更多操作对话框
     */
    private fun showMoreDialog(position: Int) {
        moreActionDialog = MoreActionDialog(activity!!, "square")
        moreActionDialog.show()

        if (adapter.data[position]?.iscollected == 0) {
            moreActionDialog.collect.text = "收藏"
            moreActionDialog.collectBtn.setImageResource(R.drawable.icon_collect_no)
        } else {
            moreActionDialog.collect.text = "取消收藏"
            moreActionDialog.collectBtn.setImageResource(R.drawable.icon_collectt)
        }


        if (adapter.data[position].accid == UserManager.getAccid()) {
            moreActionDialog.llDelete.visibility = View.VISIBLE
            moreActionDialog.llJubao.visibility = View.GONE
            moreActionDialog.llCollect.visibility = View.GONE
        } else {
            moreActionDialog.llDelete.visibility = View.GONE
            moreActionDialog.llJubao.visibility = View.VISIBLE
            moreActionDialog.llCollect.visibility = View.VISIBLE
        }
        moreActionDialog.llDelete.onClick {
            val params = hashMapOf(
                "accid" to SPUtils.getInstance(Constants.SPNAME).getString("accid"),
                "token" to SPUtils.getInstance(Constants.SPNAME).getString("token"),
                "square_id" to adapter.data[position].id!!
            )
            mPresenter.removeMySquare(params, position)
            moreActionDialog.dismiss()

        }


        moreActionDialog.llCollect.onClick {

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
        moreActionDialog.llJubao.onClick {
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
        moreActionDialog.cancel.onClick {
            moreActionDialog.dismiss()
        }

    }

    override fun onRemoveMySquareResult(result: Boolean, position: Int) {
        if (result) {
            if (adapter.data[position].type == SquareBean.AUDIO) {
                resetAudio()
            } else if (adapter.data[position].type == SquareBean.VIDEO) {
                GSYVideoManager.releaseAllVideos()
            }
            adapter.data.removeAt(position)
            adapter.notifyItemRemoved(position + adapter.headerLayoutCount)
        }
    }


    override fun onPause() {
        super.onPause()
        Log.d("SquareFragment", "onPause")
//        GSYVideoManager.onPause()
    }

    override fun onResume() {
        super.onResume()
        GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_DEFAULT)
        GSYVideoManager.onResume(false)
        Log.d("SquareFragment", "onResume")

    }


    override fun onDestroy() {
        super.onDestroy()
        GSYVideoManager.releaseAllVideos()
        resetAudio()
        Log.d("SquareFragment", "onDestroy")

        //反注册eventbus
        EventBus.getDefault().unregister(this)
    }


    override fun onRefresh(refreshLayout: RefreshLayout) {
        page = 1
        listParams["page"] = page

        resetAudio()

        friendsAdapter.data.clear()
        friendsAdapter.notifyDataSetChanged()

        refreshLayout.setNoMoreData(false)
        mPresenter.getSquareList(listParams, true)
        mPresenter.getFrinedsList(friendsParams)

    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        if (adapter.data.size < Constants.PAGESIZE * page) {
            refreshLayout.finishLoadMoreWithNoMoreData()
        } else {
            page++
            listParams["page"] = page
            mPresenter.getSquareList(listParams, false)
        }
    }


    override fun onGetFriendsListResult(friends: MutableList<FriendBean?>) {
        if (friends.size == 0) {
//            adapter.headerLayout.setVisible(false)
            adapter.headerLayout.friendTv.visibility = View.GONE
            adapter.headerLayout.headRv.visibility = View.GONE
        } else {
//            adapter.headerLayout.setVisible(true)
            adapter.headerLayout.friendTv.visibility = View.VISIBLE
            adapter.headerLayout.headRv.visibility = View.VISIBLE
            friendsAdapter.setNewData(friends)

        }
    }

    override fun onGetSquareListResult(data: SquareListBean?, result: Boolean, isRefresh: Boolean) {
        if (result) {
            if (isRefresh) {
                adapter.data.clear()
                adapter.notifyDataSetChanged()
                squareDynamicRv.scrollToPosition(0)
            }

            setViewState(CONTENT)
            if (data!!.list != null && data!!.list!!.size > 0) {
                for (tempData in 0 until data!!.list!!.size) {
                    data!!.list!![tempData].type = when {
                        !data!!.list!![tempData].video_json.isNullOrEmpty() -> SquareBean.VIDEO
                        !data!!.list!![tempData].audio_json.isNullOrEmpty() -> {
                            data!!.list!![tempData].audio_json?.get(0)?.leftTime =
                                data!!.list!![tempData].audio_json?.get(0)?.duration ?: 0
                            SquareBean.AUDIO
                        }
                        !data!!.list!![tempData].photo_json.isNullOrEmpty() || (data!!.list!![tempData].photo_json.isNullOrEmpty() && data!!.list!![tempData].audio_json.isNullOrEmpty() && data!!.list!![tempData].video_json.isNullOrEmpty()) -> SquareBean.PIC
                        else -> SquareBean.PIC
                    }
                    data!!.list!![tempData].originalLike = data!!.list!![tempData].isliked
                }
                adapter.addData(data!!.list!!)
            }
        } else {
            setViewState(ERROR)
            errorMsg.text = if (mPresenter.checkNetWork()) {
                activity!!.getString(R.string.retry_load_error)
            } else {
                activity!!.getString(R.string.retry_net_error)
            }
        }
        adapter.notifyDataSetChanged()
        refreshLayout.finishRefresh(result)
        refreshLayout.finishLoadMore(result)
        refreshLayout.setEnableLoadMoreWhenContentNotFull(false)
    }

    override fun onGetSquareLikeResult(position: Int, result: Boolean) {
        if (result) {
            adapter.data[position].originalLike = adapter.data[position].isliked
        }
    }

    override fun onGetSquareCollectResult(position: Int, data: BaseResp<Any?>?) {
        if (data != null)
            toast(data.msg)
        if (adapter.data[position].iscollected == 1) {
            adapter.data[position].iscollected = 0
        } else {
            adapter.data[position].iscollected = 1
        }
        adapter.notifyDataSetChanged()
        if (moreActionDialog != null && moreActionDialog.isShowing) {
            moreActionDialog.dismiss()
        }
    }

    override fun onGetSquareReport(baseResp: BaseResp<Any?>?, position: Int) {
        if (baseResp != null)
            toast(baseResp.msg)
        if (moreActionDialog != null && moreActionDialog.isShowing) {
            moreActionDialog.dismiss()
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.squareEdit -> {
                onRePublishEvent(RePublishEvent(true, activity!!))
            }
        }
    }

    override fun showLoading() {
        setViewState(LOADING)
    }


    companion object {
        private const val LOADING = 0
        private const val CONTENT = 1
        private const val ERROR = 2
        private const val EMPTY = 3
    }

    private fun setViewState(state: Int) {
        when (state) {
            LOADING -> {
                loadingLayout.isVisible = true
                contentLayout.isVisible = false
                errorLayout.isVisible = false
                emptyLayout.isVisible = false
            }
            CONTENT -> {
                contentLayout.isVisible = true
                loadingLayout.isVisible = false
                errorLayout.isVisible = false
                emptyLayout.isVisible = false
            }
            ERROR -> {
                errorLayout.isVisible = true
                contentLayout.isVisible = false
                loadingLayout.isVisible = false
                emptyLayout.isVisible = false
            }
            EMPTY -> {
                emptyLayout.isVisible = true
                contentLayout.isVisible = false
                errorLayout.isVisible = false
                loadingLayout.isVisible = false
            }
        }

    }


    /***************************事件总线******************************/

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateLabelEvent(event: UpdateLabelEvent) {
//        squareDynamicRv.scrollToPosition(0)

        listParams["tagid"] = event.label.id
        //这个地方还要默认设置选中第一个标签来更新数据
        refreshLayout.autoRefresh()

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshEvent(event: RefreshEvent) {
//        squareDynamicRv.scrollToPosition(0)
        //这个地方还要默认设置选中第一个标签来更新数据
        val params = UserManager.getFilterConditions()
        params.forEach {
            listParams[it.key] = it.value
        }
        refreshLayout.autoRefresh()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshSquareEvent(event: RefreshSquareEvent) {
//        squareDynamicRv.scrollToPosition(0)

        refreshLayout.autoRefresh()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNotifyEvent(event: NotifyEvent) {
        val pos = event.position
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
                } else if (state == GSYVideoView.CURRENT_STATE_AUTO_COMPLETE || state == CURRENT_STATE_ERROR) {
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
                adapter.refreshNotifyItemChanged(pos)

            }
        })

    }


    private var changeMarTop = false
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onProgressEvent(event: UploadEvent) {
        if (event.from == 1)
            if (event.qnSuccess) {
                llRetry.isVisible = false
                btnClose.isVisible = false
                uploadProgressBar.progress =
                    (((event.currentFileIndex - 1) * 1.0F / event.totalFileCount + (1.0F / event.totalFileCount * event.progress)) * 100).toInt()
                uploadProgressTv.text = "正在发布    ${uploadProgressBar.progress}%"
                uploadFl.isVisible = true
                if (!changeMarTop) {
                    val params = adapter.headerLayout.friendTv.layoutParams as LinearLayout.LayoutParams
                    params.topMargin = SizeUtils.dp2px(45F)
                    adapter.headerLayout.friendTv.layoutParams = params
                    changeMarTop = true
                }
            } else {
                UserManager.cancelUpload = true
                UserManager.publishState = -2
            }

    }


    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onAnnounceEvent(event: AnnounceEvent) {
        if (event.serverSuccess) {
            UserManager.clearPublishParams()
            uploadProgressTv.text = "动态发布成功!"
            uploadFl.postDelayed({
                uploadFl.isVisible = false
                val params = adapter.headerLayout.friendTv.layoutParams as LinearLayout.LayoutParams
                params.topMargin = SizeUtils.dp2px(10F)
                adapter.headerLayout.friendTv.layoutParams = params
            }, 500)
        } else {
            UserManager.cancelUpload = true
            uploadFl.isVisible = true
            val params = adapter.headerLayout.friendTv.layoutParams as LinearLayout.LayoutParams
            params.topMargin = SizeUtils.dp2px(45F)
            adapter.headerLayout.friendTv.layoutParams = params
            uploadProgressBar.progress = 0
            llRetry.isVisible = true
            btnClose.isVisible = true

            if (event.code == 400) { //内容违规重新去编辑
                UserManager.publishState = -1
                uploadProgressTv.text = "内容违规请重新编辑"
                iconRetry.setImageResource(R.drawable.icon_edit_retry)
                editRetry.text = "编辑"
                llRetry.onClick {
                    SPUtils.getInstance(Constants.SPNAME).put("draft", UserManager.publishParams["descr"] as String)
                    UserManager.clearPublishParams()
                    startActivity<PublishActivity>()
                    UserManager.publishState = 0
                    uploadFl.isVisible = false
                    val params = adapter.headerLayout.friendTv.layoutParams as LinearLayout.LayoutParams
                    params.topMargin = SizeUtils.dp2px(10F)
                    adapter.headerLayout.friendTv.layoutParams = params

                }
            } else { //发布失败重新发布
                UserManager.publishState = -2
                uploadProgressTv.text = "发布失败"
                iconRetry.setImageResource(R.drawable.icon_retry)
                editRetry.text = "重试"
                llRetry.onClick {
                    retryPublish()
                }
            }
            //TODO 取消重新发布，清除本地所存下的发布的数据
            btnClose.onClick {
                uploadFl.isVisible = false
                val params = adapter.headerLayout.friendTv.layoutParams as LinearLayout.LayoutParams
                params.topMargin = SizeUtils.dp2px(10F)
                adapter.headerLayout.friendTv.layoutParams = params
                UserManager.clearPublishParams()
            }
        }
    }


    private var from = 1
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onRePublishEvent(event: RePublishEvent) {
        if (event.context is UserCenterActivity) {
            from = 2
        } else {
            from = 1
        }
        if (UserManager.publishState == 1) {//正在发布中
            ToastUtils.showShort("还有动态正在发布哦~请稍候")
            return
        } else if (UserManager.publishState == -2) {//发布失败
            CommonAlertDialog.Builder(event.context)
                .setTitle("发布提示")
                .setContent("您有一条内容未成功发布，是否重新发布？")
                .setConfirmText("重新上传")
                .setOnConfirmListener(object : CommonAlertDialog.OnConfirmListener {
                    override fun onClick(dialog: Dialog) {
                        dialog.cancel()
                        retryPublish()
                    }
                })
                .setCancelText("发布新内容")
                .setOnCancelListener(object : CommonAlertDialog.OnCancelListener {
                    override fun onClick(dialog: Dialog) {
                        dialog.cancel()
                        uploadFl.isVisible = false
                        val params = adapter.headerLayout.friendTv.layoutParams as LinearLayout.LayoutParams
                        params.topMargin = SizeUtils.dp2px(10F)
                        adapter.headerLayout.friendTv.layoutParams = params
                        UserManager.clearPublishParams()
                        if (event.context is UserCenterActivity) {
                            event.context.startActivity<PublishActivity>("from" to 2)
                        } else {
                            event.context.startActivity<PublishActivity>()
                        }
                    }
                })
                .create()
                .show()
        } else if (UserManager.publishState == -1) { //400
            SPUtils.getInstance(Constants.SPNAME).put("draft", UserManager.publishParams["descr"] as String)
            UserManager.clearPublishParams()
            if (event.context is UserCenterActivity) {
                event.context.startActivity<PublishActivity>("from" to 2)
            } else {
                event.context.startActivity<PublishActivity>()
            }
            uploadFl.isVisible = false
            val params = adapter.headerLayout.friendTv.layoutParams as LinearLayout.LayoutParams
            params.topMargin = SizeUtils.dp2px(10F)
            adapter.headerLayout.friendTv.layoutParams = params
        } else if (UserManager.publishState == 0) {
            if (event.context is UserCenterActivity) {
                event.context.startActivity<PublishActivity>("from" to 2)
            } else {
                event.context.startActivity<PublishActivity>()
            }
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshLikeEvent(event: RefreshLikeEvent) {
        if (event.position != -1) {
            if (adapter.data[event.position].isliked != event.isLike) {
                if (event.isLike == 1) {
                    adapter.data[event.position].like_cnt++
                } else {
                    adapter.data[event.position].like_cnt--
                }
                adapter.data[event.position].isliked = event.isLike
                adapter.refreshNotifyItemChanged(event.position)
            }
        }
    }


    /*-------------------------------------- 重新上传-----------------------------*/
    private var uploadCount = 0

    private fun retryPublish() {
        if (!mPresenter.checkNetWork()) {
            uploadProgressTv.text = "网络不可用,请检查网络设置"
            return
        } else {
            uploadProgressTv.text = ""
        }
        uploadCount = 0
        llRetry.isVisible = false
        btnClose.isVisible = false
        //发布消息的类型0,纯文本的 1，照片 2，视频 3，声音
        UserManager.publishState = 1
        when {
            UserManager.publishParams["type"] == 0 -> publish()
            UserManager.publishParams["type"] == 1 -> {
                UserManager.cancelUpload = false
                uploadPictures()
            }
            UserManager.publishParams["type"] == 2 -> {
                UserManager.cancelUpload = false
                //TODO上传视频
                val videoQnPath =
                    "${Constants.FILE_NAME_INDEX}${Constants.PUBLISH}${SPUtils.getInstance(Constants.SPNAME).getString(
                        "accid"
                    )}/${System.currentTimeMillis()}/${RandomUtils.getRandomString(
                        16
                    )}"
                mPresenter.uploadFile(1, 1, UserManager.mediaBeans[0].url, videoQnPath, 2)
            }
            UserManager.publishParams["type"] == 3 -> {
                UserManager.cancelUpload = false
                //TODO上传音频
                val audioQnPath =
                    "${Constants.FILE_NAME_INDEX}${Constants.PUBLISH}${SPUtils.getInstance(Constants.SPNAME).getString(
                        "accid"
                    )}/${System.currentTimeMillis()}/${RandomUtils.getRandomString(
                        16
                    )}"
                mPresenter.uploadFile(1, 1, UserManager.mediaBeans[0].url, audioQnPath, 3)
            }
        }
    }


    //发布消息的类型0,纯文本的 1，照片 2，视频 3，声音
    override fun onQnUploadResult(success: Boolean, type: Int, key: String?) {
        if (success) {
            when (type) {
                0 -> {
                    publish()
                }
                1 -> {
                    UserManager.mediaBeans[uploadCount].url = key ?: ""
                    UserManager.keyList?.set(uploadCount, Gson().toJson(UserManager.mediaBeans[uploadCount]))
                    uploadCount++
                    if (uploadCount == UserManager.mediaBeans.size) {
                        publish()
                    } else {
                        uploadPictures()
                    }
                }
                2 -> {
                    UserManager.mediaBeans[uploadCount].url = key ?: ""
                    UserManager.keyList?.set(uploadCount, Gson().toJson(UserManager.mediaBeans[0]))
                    publish()
                }
                3 -> {
                    UserManager.mediaBeans[uploadCount].url = key ?: ""
                    UserManager.keyList?.set(uploadCount, Gson().toJson(UserManager.mediaBeans[0]))
                    publish()
                }
            }
        } else {
            onProgressEvent(UploadEvent(qnSuccess = false))
        }
    }

    override fun onSquareAnnounceResult(type: Int, success: Boolean, code: Int) {
        onAnnounceEvent(AnnounceEvent(success, code))
        if (from == 2) {
            EventBus.getDefault().postSticky(UploadEvent(1, 1, 1.0, from = 2))
        } else {
            refreshLayout.autoRefresh()
        }

        from = 1
    }

    private fun uploadPictures() {
        //上传图片
        val imagePath =
            "${Constants.FILE_NAME_INDEX}${Constants.PUBLISH}${SPUtils.getInstance(Constants.SPNAME).getString(
                "accid"
            )}/${System.currentTimeMillis()}/${RandomUtils.getRandomString(
                16
            )}"
        mPresenter.uploadFile(
            UserManager.mediaBeans.size,
            uploadCount + 1,
            UserManager.mediaBeans[uploadCount].url,
            imagePath,
            1
        )
    }

    private fun publish() {
        mPresenter.publishContent(
            UserManager.publishParams["type"] as Int,
            UserManager.publishParams,
            UserManager.checkIds,
            UserManager.keyList
        )
    }


}

