package com.sdy.jitangapplication.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.fragment.BaseMvpLazyLoadFragment
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.NotifyEvent
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
import com.sdy.jitangapplication.ui.activity.MyCollectionEtcActivity
import com.sdy.jitangapplication.ui.activity.SquareCommentDetailActivity
import com.sdy.jitangapplication.ui.adapter.MultiListSquareAdapter
import com.sdy.jitangapplication.utils.ScrollCalculatorHelper
import com.sdy.jitangapplication.utils.UserManager
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack
import com.shuyu.gsyvideoplayer.utils.GSYVideoType
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.fragment_my_square.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 我的点赞、我的收藏
 * //todo 解决视频无缝衔接播放问题
 */
class MySquareFragment(val type: Int) : BaseMvpLazyLoadFragment<MyCollectionPresenter>(), MyCollectionView,
    OnRefreshListener,
    OnLoadMoreListener, MultiListSquareAdapter.ResetAudioListener {
    companion object {
        const val TYPE_COLLECT = 3
        const val TYPE_LIKE = 2
        const val TYPE_MINE = 1
        const val TYPE_SQUARE = 0
    }

    override fun loadData() {
        if (type != TYPE_MINE) {
            initView()
            //refreshLayout.autoRefresh()
            mPresenter.getMySquare(params)
        }

    }

    private val TAG = MyCollectionEtcActivity::class.java.simpleName

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

    //广场列表内容适配器
    private val adapter by lazy { MultiListSquareAdapter(mutableListOf(), resetAudioListener = this) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_my_square, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (type == TYPE_MINE) {
            initView()
            //refreshLayout.autoRefresh()
            mPresenter.getMySquare(params)
        }
    }

    override fun resetAudioState() {
        //                adapter.notifyItemChanged(position)
        if (mediaPlayer != null) {
            mediaPlayer!!.resetMedia()
            mediaPlayer = null
        }
        currPlayIndex = -1
    }

    private lateinit var scrollCalculatorHelper: ScrollCalculatorHelper


    val layoutManager by lazy { LinearLayoutManager(activity!!, RecyclerView.VERTICAL, false) }


    private var currPlayIndex = -1

    private fun initView() {
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
        mPresenter = MyCollectionPresenter()
        mPresenter.mView = this
        mPresenter.context = activity!!
        refreshLayout.setOnRefreshListener(this)
        refreshLayout.setOnLoadMoreListener(this)
        refreshLayout.setEnableAutoLoadMore(true)



        stateview.retryBtn.onClick {
            stateview.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.getMySquare(params)
        }




        collectionRv.layoutManager = layoutManager
        collectionRv.adapter = adapter
        adapter.type = type
        adapter.setEmptyView(R.layout.empty_layout, collectionRv)
        adapter.setHeaderAndEmpty(true)
        adapter.bindToRecyclerView(collectionRv)
        //取消动画，主要是闪烁
//        (collectionRv.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        collectionRv.itemAnimator?.changeDuration = 0

        //限定范围为屏幕一半的上下偏移180 56+32=88
        val playTop = ScreenUtils.getScreenHeight() / 2 - SizeUtils.dp2px(200F)
        val playBottom = ScreenUtils.getScreenHeight() / 2 + SizeUtils.dp2px(200F)
//        val playTop = ScreenUtils.getScreenHeight() / 2 - SizeUtils.dp2px(126F)
//        val playBottom = ScreenUtils.getScreenHeight() / 2 + SizeUtils.dp2px(126F)
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
            SquareCommentDetailActivity.start(activity!!, adapter.data[position], position = position)
        }

        adapter.setOnItemChildClickListener { _, view, position ->
            val squareBean = adapter.data[position]
            when (view.id) {
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

    var mediaPlayer: IjkMediaPlayerUtil? = null

    private fun initAudio(position: Int) {
        resetAudio()
        mediaPlayer = IjkMediaPlayerUtil(activity!!, position, object : OnPlayingListener {

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
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this)

        resetAudio()
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

    /**
     * 无缝切换小屏和全屏
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNotifyEvent(event: NotifyEvent) {
        if (event.type == type) {
            val pos = event.position
//            GSYVideoManager.releaseAllVideos()
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
    }

}
