package com.sdy.jitangapplication.ui.fragment
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.*
import com.google.gson.Gson
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseActivity.Companion.CONTENT
import com.kotlin.base.ui.activity.BaseActivity.Companion.EMPTY
import com.kotlin.base.ui.activity.BaseActivity.Companion.ERROR
import com.kotlin.base.ui.activity.BaseActivity.Companion.LOADING
import com.kotlin.base.ui.fragment.BaseMvpLazyLoadFragment
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.constant.RefreshState
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.baselibrary.utils.CustomClickListener
import com.sdy.baselibrary.utils.RandomUtils
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.*
import com.sdy.jitangapplication.model.FriendBean
import com.sdy.jitangapplication.model.LabelQualityBean
import com.sdy.jitangapplication.model.SquareBean
import com.sdy.jitangapplication.model.SquareListBean
import com.sdy.jitangapplication.player.IjkMediaPlayerUtil
import com.sdy.jitangapplication.player.OnPlayingListener
import com.sdy.jitangapplication.presenter.SquarePresenter
import com.sdy.jitangapplication.presenter.view.SquareView
import com.sdy.jitangapplication.switchplay.SwitchUtil
import com.sdy.jitangapplication.switchplay.SwitchVideo
import com.sdy.jitangapplication.ui.activity.PublishActivity
import com.sdy.jitangapplication.ui.activity.SquareCommentDetailActivity
import com.sdy.jitangapplication.ui.activity.SquarePlayListDetailActivity
import com.sdy.jitangapplication.ui.adapter.MultiListSquareAdapter
import com.sdy.jitangapplication.ui.adapter.SquareFriendsAdapter
import com.sdy.jitangapplication.ui.dialog.TranspondDialog
import com.sdy.jitangapplication.utils.ScrollCalculatorHelper
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.CommonAlertDialog
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack
import com.shuyu.gsyvideoplayer.utils.GSYVideoType
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView.CURRENT_STATE_ERROR
import kotlinx.android.synthetic.main.error_layout.*
import kotlinx.android.synthetic.main.fragment_square.*
import kotlinx.android.synthetic.main.headerview_guide_publish.view.*
import kotlinx.android.synthetic.main.headerview_label.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.startActivityForResult


/**
 * 广场列表
 */
class SquareFragment : BaseMvpLazyLoadFragment<SquarePresenter>(), SquareView, OnRefreshListener, OnLoadMoreListener,
    MultiListSquareAdapter.ResetAudioListener {

    override fun loadData() {
        initView()

    }

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
            "tagid" to UserManager.getGlobalLabelId()
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


    //引导发布标题
    val guideList by lazy { mutableListOf<LabelQualityBean>() }
    //当前引导发布标题的下标
    private var currentTitleIndex = -1

    //创建引导布局
    private fun initGuideSquareView(): View {
        val guideHeadView =
            LayoutInflater.from(activity!!).inflate(R.layout.headerview_guide_publish, squareDynamicRv, false)
        guideHeadView.guideReselect.onClick {
            if (currentTitleIndex < guideList.size - 1) {
                currentTitleIndex += 1
            } else {
                currentTitleIndex = 0
            }
            setGuidePublishTitle(guideList[currentTitleIndex])
        }

        guideHeadView.guidePublish.onClick(object : CustomClickListener() {
            override fun onSingleClick() {
                mPresenter.checkBlock()
                if (currentTitleIndex != -1) {
                    activity!!.intent.putExtra("titleBean", guideList[currentTitleIndex])
                }

            }

            override fun onFastClick() {

            }

        })
        return guideHeadView
    }

    /**
     * 设置当前的标题
     */
    private fun setGuidePublishTitle(labelQualityBean: LabelQualityBean) {
        GlideUtil.loadRoundImgCenterinside(
            activity!!,
            labelQualityBean.icon,
            adapter.headerLayout.guideIv, 0.0f,
            SizeUtils.dp2px(12F)
        )
        adapter.headerLayout.guideTv.text = labelQualityBean.content
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
            startActivityForResult<SquarePlayListDetailActivity>(
                200,
                ("target_accid" to (friendsAdapter.data[position].accid ?: 0)), "position" to position
            )
        }

        return friendsView
    }


    private var currPlayIndex = -1

    private fun initView() {
        EventBus.getDefault().register(this)
        mPresenter = SquarePresenter()
        mPresenter.mView = this
        mPresenter.context = activity!!
        refreshLayout.setOnRefreshListener(this)
        refreshLayout.setOnLoadMoreListener(this)
        squareEdit.onClick(object : CustomClickListener() {
            override fun onSingleClick() {
                mPresenter.checkBlock()
                activity!!.intent.removeExtra("titleBean")
            }

            override fun onFastClick() {
            }

        })


        retryBtn.onClick {
            setViewState(LOADING)
//            这个地方还要默认设置选中第一个标签来更新数据
            mPresenter.getSquareList(listParams, true, true)
            //mPresenter.getFrinedsList(friendsParams)
        }

        adapter.setHeaderAndEmpty(true)
        squareDynamicRv.layoutManager = layoutManager
        squareDynamicRv.adapter = adapter
        adapter.bindToRecyclerView(squareDynamicRv)
        adapter.addHeaderView(initGuideSquareView())
        adapter.addHeaderView(initFriendsView())
        adapter.setEmptyView(R.layout.empty_layout, squareDynamicRv)
        //取消动画，主要是闪烁
//        (squareDynamicRv.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        squareDynamicRv.itemAnimator?.changeDuration = 0
        //限定范围为屏幕一半的上下偏移180
        val playTop = ScreenUtils.getScreenHeight() / 2 - SizeUtils.dp2px(150F)
        val playBottom = ScreenUtils.getScreenHeight() / 2 + SizeUtils.dp2px(150F)
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
                R.id.squareZhuanfaBtn1 -> {
                    showTranspondDialog(adapter.data[position])
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
        val params = UserManager.getFilterConditions()
        params.forEach {
            listParams[it.key] = it.value
        }

        mPresenter.getSquareList(listParams, true, true)
//        mPresenter.getFrinedsList(friendsParams)

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
                CommonFunction.toast("音频播放出错")
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

        refreshLayout.setNoMoreData(false)
        mPresenter.getSquareList(listParams, true)
//        mPresenter.getFrinedsList(friendsParams)

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
            if (isRefresh) {
                if (data!!.friend_list.isNullOrEmpty()) {
                    adapter.headerLayout.friendTv.visibility = View.GONE
                    adapter.headerLayout.headRv.visibility = View.GONE
                } else {
                    adapter.headerLayout.friendTv.visibility = View.VISIBLE
                    adapter.headerLayout.headRv.visibility = View.VISIBLE
                    friendsAdapter.setNewData(data!!.friend_list)
                }

                currentTitleIndex = 0
                if (data!!.title_banner.isNullOrEmpty()) {
                    adapter.headerLayout.guidePublishCl.isVisible = false
                } else {
                    adapter.headerLayout.guidePublishCl.isVisible = true
                    guideList.clear()
                    guideList.addAll(data!!.title_banner!!)
                    setGuidePublishTitle(data!!.title_banner!![currentTitleIndex])
                }
            }


            if (data!!.list != null && data!!.list!!.size > 0) {
                for (tempData in 0 until data!!.list!!.size) {
                    data!!.list!![tempData].type = when {
                        !data!!.list!![tempData].video_json.isNullOrEmpty() -> SquareBean.VIDEO
                        !data!!.list!![tempData].audio_json.isNullOrEmpty() -> {
                            data!!.list!![tempData].audio_json?.get(0)?.leftTime =
                                data!!.list!![tempData].audio_json?.get(0)?.duration ?: 0
                            SquareBean.AUDIO
                        }
                        data!!.list!![tempData].category_type != 2 && (!data!!.list!![tempData].photo_json.isNullOrEmpty() || (data!!.list!![tempData].photo_json.isNullOrEmpty() && data!!.list!![tempData].audio_json.isNullOrEmpty() && data!!.list!![tempData].video_json.isNullOrEmpty())) -> SquareBean.PIC
                        else -> SquareBean.OFFICIAL_NOTICE
                    }
                    data!!.list!![tempData].originalLike = data!!.list!![tempData].isliked
                    data!!.list!![tempData].originalLikeCount = data!!.list!![tempData].like_cnt
                }
                adapter.addData(data!!.list!!)
            }

            refreshLayout.finishRefresh(result)
            refreshLayout.finishLoadMore(result)
            refreshLayout.setEnableLoadMoreWhenContentNotFull(false)
            if (fragmentManager?.let { FragmentUtils.getTopShow(it) } == this)
//            EventBus.getDefault().postSticky(EnableLabelEvent(true))
                refreshLayout.postDelayed({ EventBus.getDefault().postSticky(EnableLabelEvent(true)) }, 1000L)
        } else {
            setViewState(ERROR)
            errorMsg.text = if (mPresenter.checkNetWork()) {
                activity!!.getString(R.string.retry_load_error)
            } else {
                activity!!.getString(R.string.retry_net_error)
            }
            refreshLayout.finishRefresh(result)
            refreshLayout.finishLoadMore(result)
            refreshLayout.setEnableLoadMoreWhenContentNotFull(false)
            adapter.notifyDataSetChanged()
            if (fragmentManager?.let { FragmentUtils.getTopShow(it) } == this)
                EventBus.getDefault().postSticky(EnableLabelEvent(true))

        }


    }


    override fun onGetSquareLikeResult(position: Int, result: Boolean) {
        if (result) {
            adapter.data[position].originalLike = adapter.data[position].isliked
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
                adapter.refreshNotifyItemChanged(position)
            }
        }
    }

    override fun onGetSquareReport(baseResp: BaseResp<Any?>?, position: Int) {
        if (baseResp != null)
            CommonFunction.toast(baseResp.msg)

    }


    override fun showLoading() {
        setViewState(LOADING)
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

    /**
     * 根据选择的标签切换广场内容
     */
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onUpdateLabelEvent(event: UpdateLabelEvent) {
//        squareDynamicRv.scrollToPosition(0)
        Log.d("SquareFragment", event.label.title)
        listParams["tagid"] = event.label.id
        //这个地方还要默认设置选中第一个标签来更新数据
        if (refreshLayout.state == RefreshState.Refreshing) {
            refreshLayout.finishRefresh()
        }
        refreshLayout.autoRefresh()

//        refreshLayout.autoRefreshAnimationOnly()
//        page = 1
//        listParams["page"] = page
//
//        resetAudio()
//
//        friendsAdapter.data.clear()
//        friendsAdapter.notifyDataSetChanged()
//
//        refreshLayout.setNoMoreData(false)
//        mPresenter.getSquareList(listParams, true)
//        mPresenter.getFrinedsList(friendsParams)

    }

    /**
     * 全局筛选框筛选广场内容
     */
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onRefreshEvent(event: RefreshEvent) {
//        squareDynamicRv.scrollToPosition(0)
        if (listParams["audit_only"] != null)
            listParams.remove("audit_only")
        if (listParams["local_only"] != null)
            listParams.remove("local_only")
        //这个地方还要默认设置选中第一个标签来更新数据
        val params = UserManager.getFilterConditions()
        params.forEach {
            listParams[it.key] = it.value
        }
        refreshLayout.autoRefresh()
    }

    /**
     * 更新广场
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshSquareEvent(event: RefreshSquareEvent) {
//        squareDynamicRv.scrollToPosition(0)

        refreshLayout.autoRefresh()
    }

    /**
     * 无缝切换小屏和全屏
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNotifyEvent(event: NotifyEvent) {
        val pos = event.position
//        GSYVideoManager.releaseAllVideos()

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

            if (event.code == 402) { //内容违规重新去编辑
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
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRePublishEvent(event: RePublishEvent) {
        if (event.context == UserCenterFragment::class.java.simpleName) {
            from = 2
        } else {
            from = 1
        }
        if (UserManager.publishState == 1) {//正在发布中
            CommonFunction.toast("还有动态正在发布哦~请稍候")
            return
        } else if (UserManager.publishState == -2) {//发布失败
            CommonAlertDialog.Builder(activity!!)
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
                        if (!ActivityUtils.isActivityExistsInStack(PublishActivity::class.java))
                            if (event.context == UserCenterFragment::class.java.simpleName) {
                                startActivity<PublishActivity>("from" to 2)
                            } else {
                                startActivity<PublishActivity>()
                            }
                    }
                })
                .create()
                .show()
        } else if (UserManager.publishState == -1) { //400
            SPUtils.getInstance(Constants.SPNAME).put("draft", UserManager.publishParams["descr"] as String)
            UserManager.clearPublishParams()
            if (!ActivityUtils.isActivityExistsInStack(PublishActivity::class.java))
                if (event.context == UserCenterFragment::class.java.simpleName) {
                    startActivity<PublishActivity>("from" to 2)
                } else {
                    startActivity<PublishActivity>()
                }
            uploadFl.isVisible = false
            val params = adapter.headerLayout.friendTv.layoutParams as LinearLayout.LayoutParams
            params.topMargin = SizeUtils.dp2px(10F)
            adapter.headerLayout.friendTv.layoutParams = params
        } else if (UserManager.publishState == 0) {
            if (event.context == UserCenterFragment::class.java.simpleName) {
                startActivity<PublishActivity>("from" to 2)
            } else {
                activity!!.intent.setClass(activity!!, PublishActivity::class.java)
                startActivity(activity!!.intent)
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshCommentEvent(event: RefreshCommentEvent) {
        if (event.position != -1) {
            adapter.data[event.position].comment_cnt = event.commentNum
            adapter.refreshNotifyItemChanged(event.position)
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
                    UserManager.keyList.add(Gson().toJson(UserManager.mediaBeans[uploadCount]))
                    uploadCount++
                    if (uploadCount == UserManager.mediaBeans.size) {
                        publish()
                    } else {
                        uploadPictures()
                    }
                }
                2 -> {
                    UserManager.mediaBeans[uploadCount].url = key ?: ""
                    UserManager.keyList.add(Gson().toJson(UserManager.mediaBeans[0]))
                    publish()
                }
                3 -> {
                    UserManager.mediaBeans[uploadCount].url = key ?: ""
                    UserManager.keyList.add(Gson().toJson(UserManager.mediaBeans[0]))
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


    //验证用户是否被封禁结果
    override fun onCheckBlockResult(result: Boolean) {
        if (result) {
            onRePublishEvent(RePublishEvent(true, SquareFragment::class.java.simpleName))
        }
        squareEdit.isEnabled = true
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //查看好友的，出来请求
        if (requestCode == 200 && resultCode == Activity.RESULT_OK) {
            if (data != null)
                if (data.getIntExtra("position", -1) != -1) {
                    friendsAdapter.remove(data.getIntExtra("position", -1))
                }
//            mPresenter.getFrinedsList(friendsParams)
        }
    }




}

