package com.sdy.jitangapplication.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ScreenUtils
import com.kennyc.view.MultiStateView
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.fragment.BaseMvpLazyLoadFragment
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.*
import com.sdy.jitangapplication.model.SquareBean
import com.sdy.jitangapplication.model.SquareListBean
import com.sdy.jitangapplication.player.IjkMediaPlayerUtil
import com.sdy.jitangapplication.player.OnPlayingListener
import com.sdy.jitangapplication.presenter.SquarePresenter
import com.sdy.jitangapplication.presenter.view.SquareView
import com.sdy.jitangapplication.switchplay.SwitchUtil
import com.sdy.jitangapplication.switchplay.SwitchVideo
import com.sdy.jitangapplication.ui.activity.SquareCommentDetailActivity
import com.sdy.jitangapplication.ui.adapter.MultiListSquareAdapter
import com.sdy.jitangapplication.ui.dialog.TouristDialog
import com.sdy.jitangapplication.utils.ScrollCalculatorHelper
import com.sdy.jitangapplication.utils.UserManager
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack
import com.shuyu.gsyvideoplayer.utils.GSYVideoType
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView
import kotlinx.android.synthetic.main.empty_friend_layout.view.*
import kotlinx.android.synthetic.main.error_layout.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.fragment_square.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


/**
 * 好友广场列表
 */
class FriendSquareFragment : BaseMvpLazyLoadFragment<SquarePresenter>(), SquareView,
    OnRefreshListener,
    OnLoadMoreListener,
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
    private val adapter by lazy {
        MultiListSquareAdapter(
            mutableListOf(),
            resetAudioListener = this
        )
    }

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
            "gender" to SPUtils.getInstance(Constants.SPNAME).getInt("filter_square_gender", 3)
        )
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_square, container, false)
    }

    private var currPlayIndex = -1


    private fun initView() {


        EventBus.getDefault().register(this)
        mPresenter = SquarePresenter()
        mPresenter.mView = this
        mPresenter.context = activity!!
        refreshLayout.setOnRefreshListener(this)
        refreshLayout.setOnLoadMoreListener(this)


        retryBtn.onClick {
            stateSquare.viewState = MultiStateView.VIEW_STATE_LOADING
//            这个地方还要默认设置选中第一个兴趣来更新数据
            mPresenter.squareNewestLists(listParams, true, true)
            //mPresenter.getFrinedsList(friendsParams)
        }

        squareDynamicRv.layoutManager = layoutManager
        squareDynamicRv.adapter = adapter
        adapter.setHeaderAndEmpty(false)
        adapter.setEmptyView(R.layout.empty_friend_layout, squareDynamicRv)
        adapter.isUseEmpty(false)
        adapter.bindToRecyclerView(squareDynamicRv)
        //取消动画，主要是闪烁
//        (squareDynamicRv.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        squareDynamicRv.itemAnimator?.changeDuration = 0
        //限定范围为屏幕一半的上下偏移180
        val playTop = ScreenUtils.getScreenHeight() / 2 - ScreenUtils.getScreenHeight() / 4
        val playBottom = ScreenUtils.getScreenHeight() / 2 + ScreenUtils.getScreenHeight() / 4
        scrollCalculatorHelper =
            ScrollCalculatorHelper(R.id.llVideo, R.id.squareUserVideo, playTop, playBottom)
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
            if (UserManager.touristMode) {
                TouristDialog(activity!!).show()
            } else {
                view.isEnabled = false
                resetAudio()
                SquareCommentDetailActivity.start(
                    activity!!,
                    adapter.data[position],
                    position = position
                )
                view.postDelayed({ view.isEnabled = true }, 1000L)
            }
        }

        adapter.setOnItemChildClickListener { _, view, position ->
            val squareBean = adapter.data[position]
            when (view.id) {
                //播放音频
                R.id.audioPlayBtn -> {
                    if (currPlayIndex != position && squareBean.isPlayAudio != IjkMediaPlayerUtil.MEDIA_PLAY) {
                        initAudio(position)
                        mediaPlayer!!.setDataSource(squareBean.audio_json?.get(0)?.url ?: "")
                            .prepareMedia()
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


        mPresenter.squareNewestLists(listParams, true, true)

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


    override fun onRemoveMySquareResult(result: Boolean, position: Int) {
    }


    override fun onPause() {
        super.onPause()
        Log.d("FriendSquareFragment", "onPause")
//        GSYVideoManager.onPause()
    }


    override fun onStop() {
        super.onStop()
        resetAudio()
        Log.d("FriendSquareFragment", "onStop")
//        GSYVideoManager.onPause()
    }

    override fun onResume() {
        super.onResume()
        GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_DEFAULT)
        GSYVideoManager.onResume(false)
        Log.d("FriendSquareFragment", "onResume")
    }


    override fun onDestroy() {
        super.onDestroy()
        GSYVideoManager.releaseAllVideos()
        resetAudio()
        Log.d("FriendSquareFragment", "onDestroy")

        //反注册eventbus
        EventBus.getDefault().unregister(this)
    }


    override fun onRefresh(refreshLayout: RefreshLayout) {
        page = 1
        listParams["page"] = page
        listParams["gender"] =
            SPUtils.getInstance(Constants.SPNAME).getInt("filter_square_gender", 3)

        resetAudio()

        refreshLayout.setNoMoreData(false)
        mPresenter.squareNewestLists(listParams, true)

    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        if (adapter.data.size < Constants.PAGESIZE * page) {
            refreshLayout.finishLoadMoreWithNoMoreData()
        } else {
            page++
            listParams["page"] = page
            mPresenter.squareNewestLists(listParams, false)
        }
    }


    override fun onGetSquareListResult(data: SquareListBean?, result: Boolean, isRefresh: Boolean) {
        if (result) {
            if (isRefresh) {
                adapter.data.clear()
                adapter.notifyDataSetChanged()
                squareDynamicRv.scrollToPosition(0)
            }
            stateSquare.viewState = MultiStateView.VIEW_STATE_CONTENT
            //更新标题显示内容数据
            if (data != null)
                if (data!!.list != null && data!!.list!!.size > 0) {
                    adapter.isUseEmpty(false)
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
                } else {
                    adapter.isUseEmpty(true)
                    if (adapter.headerLayout != null)
                        adapter.headerLayout.isVisible = false

                    adapter.emptyView.emptyImg.setImageResource(R.drawable.icon_empty_friend)
                    adapter.emptyView.emptyFriendTitle.text = "暂时没有动态"
                    adapter.emptyView.emptyFriendTip.text = "主动打招呼去添加更多好友\n好友已发布的动态可在此直接查看"
                    adapter.emptyView.emptyFriendGoBtn.isVisible = false
                }

            refreshLayout.finishRefresh(result)
            refreshLayout.finishLoadMore(result)
            refreshLayout.setEnableLoadMoreWhenContentNotFull(false)
        } else {
            stateSquare.viewState = MultiStateView.VIEW_STATE_ERROR
            stateSquare.errorMsg.text = if (mPresenter.checkNetWork()) {
                activity!!.getString(R.string.retry_load_error)
            } else {
                activity!!.getString(R.string.retry_net_error)
            }
            refreshLayout.finishRefresh(result)
            refreshLayout.finishLoadMore(result)
            refreshLayout.setEnableLoadMoreWhenContentNotFull(false)
            adapter.notifyDataSetChanged()


        }


    }


    override fun onGetSquareLikeResult(position: Int, result: Boolean) {
    }

    override fun onGetSquareCollectResult(position: Int, data: BaseResp<Any?>?) {
    }

    override fun onGetSquareReport(baseResp: BaseResp<Any?>?, position: Int) {
    }

    override fun showLoading() {
        stateSquare.viewState = MultiStateView.VIEW_STATE_LOADING
    }


    /***************************事件总线******************************/
    /**
     * 更新广场
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshSquareEvent(event: RefreshSquareEvent) {
//        squareDynamicRv.scrollToPosition(0)
        refreshLayout.autoRefresh()
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshSquareByGenderEvent(event: RefreshSquareByGenderEvent) {
        refreshLayout.autoRefresh()
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshLikeEvent(event: RefreshLikeEvent) {
        if (event.position != -1 && event.squareId == adapter.data[event.position].id) {
            adapter.data[event.position].originalLike = event.isLike
            adapter.data[event.position].isliked = event.isLike
            adapter.data[event.position].like_cnt =
                if (event.likeCount >= 0) {
                    event.likeCount
                } else {
                    if (event.isLike == 1) {
                        adapter.data[event.position].like_cnt + 1
                    } else {
                        adapter.data[event.position].like_cnt - 1
                    }
                }

            adapter.refreshNotifyItemChanged(event.position)
        }
    }


    /**
     * 无缝切换小屏和全屏
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNotifyEvent(event: NotifyEvent) {
        val pos = event.position
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
                adapter.refreshNotifyItemChanged(pos)

            }
        })

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshCommentEvent(event: RefreshCommentEvent) {
        if (event.position != -1) {
            adapter.data[event.position].comment_cnt = event.commentNum
            adapter.refreshNotifyItemChanged(event.position)
        }
    }


}

