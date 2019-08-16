package com.example.demoapplication.ui.fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.example.demoapplication.R
import com.example.demoapplication.common.Constants
import com.example.demoapplication.event.NotifyEvent
import com.example.demoapplication.event.RefreshEvent
import com.example.demoapplication.event.UpdateLabelEvent
import com.example.demoapplication.model.FriendBean
import com.example.demoapplication.model.SquareBean
import com.example.demoapplication.model.SquareListBean
import com.example.demoapplication.nim.activity.ChatActivity
import com.example.demoapplication.player.IjkMediaPlayerUtil
import com.example.demoapplication.player.OnPlayingListener
import com.example.demoapplication.presenter.SquarePresenter
import com.example.demoapplication.presenter.view.SquareView
import com.example.demoapplication.switchplay.SwitchUtil
import com.example.demoapplication.switchplay.SwitchVideo
import com.example.demoapplication.ui.activity.PublishActivity
import com.example.demoapplication.ui.activity.SquareCommentDetailActivity
import com.example.demoapplication.ui.activity.SquarePlayListDetailActivity
import com.example.demoapplication.ui.adapter.MultiListSquareAdapter
import com.example.demoapplication.ui.adapter.SquareFriendsAdapter
import com.example.demoapplication.ui.dialog.MoreActionDialog
import com.example.demoapplication.ui.dialog.TranspondDialog
import com.example.demoapplication.utils.ScrollCalculatorHelper
import com.example.demoapplication.utils.UserManager
import com.example.demoapplication.widgets.CommonItemDecoration
import com.kennyc.view.MultiStateView
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.fragment.BaseMvpFragment
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack
import com.shuyu.gsyvideoplayer.utils.GSYVideoType
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView.CURRENT_STATE_ERROR
import kotlinx.android.synthetic.main.dialog_more_action.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.fragment_square.*
import kotlinx.android.synthetic.main.headerview_label.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast


/**
 * 广场列表
 */
class SquareFragment : BaseMvpFragment<SquarePresenter>(), SquareView, OnRefreshListener, OnLoadMoreListener,
    View.OnClickListener {

    //广场列表内容适配器
    private val adapter by lazy { MultiListSquareAdapter(mutableListOf()) }

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


        stateview.retryBtn.onClick {
            stateview.viewState = MultiStateView.VIEW_STATE_LOADING
            //这个地方还要默认设置选中第一个标签来更新数据
            mPresenter.getSquareList(listParams, true, true)
            mPresenter.getFrinedsList(friendsParams)
        }


        val itemdecoration = CommonItemDecoration(activity!!, DividerItemDecoration.VERTICAL)
        itemdecoration.setDrawable(activity!!.resources.getDrawable(R.drawable.recycler_divider))
        squareDynamicRv.addItemDecoration(itemdecoration)

        squareDynamicRv.layoutManager = layoutManager
        squareDynamicRv.adapter = adapter
        adapter.bindToRecyclerView(squareDynamicRv)
        adapter.addHeaderView(initFriendsView())
        adapter.setEmptyView(R.layout.empty_layout, squareDynamicRv)
        adapter.setHeaderAndEmpty(false)
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
            }
        })

        adapter.setOnItemClickListener { _, view, position ->
            SquareCommentDetailActivity.start(activity!!, adapter.data[position])
            if (mediaPlayer != null) {
                mediaPlayer!!.resetMedia()
                mediaPlayer = null
            }
        }

        adapter.setOnItemChildClickListener { _, view, position ->
            val squareBean = adapter.data[position]
            when (view.id) {
                R.id.squareChatBtn1 -> {
                    ChatActivity.start(activity!!, adapter.data[position].accid ?: "")
                }
                R.id.squareCommentBtn1 -> {
                    SquareCommentDetailActivity.start(activity!!, adapter.data[position],enterPosition = "comment")
                    if (mediaPlayer != null) {
                        mediaPlayer!!.resetMedia()
                        mediaPlayer = null
                    }
                }
                R.id.squareDianzanBtn1 -> {
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
                    mPresenter.getSquareLike(params, position)
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


        //这个地方还要默认设置选中第一个标签来更新数据
        mPresenter.getSquareList(listParams, true, true)
        mPresenter.getFrinedsList(friendsParams)

    }

    var mediaPlayer: IjkMediaPlayerUtil? = null

    private fun initAudio(position: Int) {
        if (mediaPlayer != null) {
            mediaPlayer!!.resetMedia()
            mediaPlayer = null
        }
        mediaPlayer = IjkMediaPlayerUtil(activity!!, position, object : OnPlayingListener {

            override fun onPlay(position: Int) {
                adapter.data[position].isPlayAudio = IjkMediaPlayerUtil.MEDIA_PLAY
//                adapter.notifyItemChanged(position)
                adapter.notifyDataSetChanged()

            }

            override fun onPause(position: Int) {
                adapter.data[position].isPlayAudio = IjkMediaPlayerUtil.MEDIA_PAUSE
//                adapter.notifyItemChanged(position)
                adapter.notifyDataSetChanged()
            }

            override fun onStop(position: Int) {
                adapter.data[position].isPlayAudio = IjkMediaPlayerUtil.MEDIA_STOP
                currPlayIndex = -1
//                adapter.notifyItemChanged(position)
                adapter.notifyDataSetChanged()
                mediaPlayer!!.resetMedia()
                mediaPlayer = null
            }

            override fun onError(position: Int) {
                toast("音频播放出错")
                adapter.data[position].isPlayAudio = IjkMediaPlayerUtil.MEDIA_ERROR
//                adapter.notifyItemChanged(position)
                currPlayIndex = -1
                adapter.notifyDataSetChanged()
                mediaPlayer!!.resetMedia()
                mediaPlayer = null
            }

            override fun onPrepared(position: Int) {
//                adapter.data[position].isPlayAudio = IjkMediaPlayerUtil.MEDIA_PLAY
//                adapter.notifyItemChanged(position)
//                adapter.notifyDataSetChanged()
                mediaPlayer!!.startPlay()
            }

            override fun onPreparing(position: Int) {
                adapter.data[position].isPlayAudio = IjkMediaPlayerUtil.MEDIA_PREPARE
                adapter.notifyDataSetChanged()

            }

            override fun onRelease(position: Int) {
                adapter.data[position].isPlayAudio = IjkMediaPlayerUtil.MEDIA_STOP
//                adapter.notifyItemChanged(position)
                adapter.notifyDataSetChanged()
            }

        }).getInstance()
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
            adapter.data.removeAt(position)
            adapter.notifyItemRemoved(position)
        }
    }


    override fun onPause() {
        super.onPause()
//        GSYVideoManager.onPause()
    }

    override fun onResume() {
        super.onResume()
        GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_DEFAULT)
        GSYVideoManager.onResume(false)
    }


    override fun onDestroy() {
        super.onDestroy()
        GSYVideoManager.releaseAllVideos()
        if (mediaPlayer != null) {
            mediaPlayer!!.resetMedia()
            mediaPlayer = null
        }
        //反注册eventbus
        EventBus.getDefault().unregister(this)
    }


    override fun onRefresh(refreshLayout: RefreshLayout) {
        page = 1
        listParams["page"] = page
        adapter.data.clear()
        mPresenter.getSquareList(listParams, true)
        friendsAdapter.data.clear()
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
            adapter.headerLayout.friendTv.visibility = View.GONE
        } else {
            friendsAdapter.setNewData(friends)
            adapter.headerLayout.friendTv.visibility = View.VISIBLE
        }
    }

    override fun onGetSquareListResult(data: SquareListBean?, result: Boolean, refresh: Boolean?) {
        if (refresh == true) {
            adapter.data.clear()
        }
        if (result) {
            stateview.viewState = MultiStateView.VIEW_STATE_CONTENT
            if (data!!.list != null && data!!.list!!.size > 0) {
                for (tempData in 0 until data!!.list!!.size) {
                    data!!.list!![tempData].type = when {
                        !data!!.list!![tempData].video_json.isNullOrEmpty() -> SquareBean.VIDEO
                        !data!!.list!![tempData].audio_json.isNullOrEmpty() -> SquareBean.AUDIO
                        !data!!.list!![tempData].photo_json.isNullOrEmpty() || (data!!.list!![tempData].photo_json.isNullOrEmpty() && data!!.list!![tempData].audio_json.isNullOrEmpty() && data!!.list!![tempData].video_json.isNullOrEmpty()) -> SquareBean.PIC
                        else -> SquareBean.PIC
                    }
                }
                adapter.addData(data!!.list!!)
            }
        } else {
            stateview.viewState = MultiStateView.VIEW_STATE_ERROR
            stateview.errorMsg.text = if (mPresenter.checkNetWork()) {
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
            if (adapter.data[position].isliked == 1) {
                adapter.data[position].isliked = 0
                adapter.data[position].like_cnt = adapter.data[position].like_cnt!!.minus(1)
            } else {
                adapter.data[position].isliked = 1
                adapter.data[position].like_cnt = adapter.data[position].like_cnt!!.plus(1)
            }
//            adapter.notifyItemChanged(position)
            adapter.notifyDataSetChanged()
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateLabelEvent(event: UpdateLabelEvent) {
        listParams["tagid"] = event.label.id
        //这个地方还要默认设置选中第一个标签来更新数据
        mPresenter.getSquareList(listParams, true, true)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshEvent(event: RefreshEvent) {
        //这个地方还要默认设置选中第一个标签来更新数据
        val params = UserManager.getFilterConditions()
        params.forEach {
            listParams[it.key] = it.value
        }
        mPresenter.getSquareList(listParams, true, false)
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
                adapter.notifyItemChanged(pos)

            }
        })

    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.squareEdit -> {
                startActivity<PublishActivity>()
            }
        }
    }

    override fun showLoading() {
        stateview.viewState = MultiStateView.VIEW_STATE_LOADING
    }


}

