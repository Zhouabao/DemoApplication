package com.sdy.jitangapplication.ui.fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SPUtils
import com.kennyc.view.MultiStateView
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.fragment.BaseMvpLazyLoadFragment
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
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
import com.sdy.jitangapplication.ui.activity.SquareCommentDetailActivity
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
import kotlinx.android.synthetic.main.delete_dialog_layout.*
import kotlinx.android.synthetic.main.dialog_more_action_new.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.fragment_list_square.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


/**
 * 列表形式的广场列表
 */
class ListSquareFragment(var targetAccid: String = "") : BaseMvpLazyLoadFragment<SquarePresenter>(), SquareView,
    OnLoadMoreListener, MultiListSquareAdapter.ResetAudioListener {
    private var page = 1
    private val params by lazy {
        hashMapOf(
            "token" to UserManager.getToken(),
            "accid" to UserManager.getAccid(),
            "page" to page,
            "target_accid" to targetAccid
        )
    }

    override fun onCheckBlockResult(result: Boolean) {

    }

    private val TAG = ListSquareFragment::class.java.simpleName
    private lateinit var scrollCalculatorHelper: ScrollCalculatorHelper
    //音频当前播放位置
    private var currPlayIndex = -1

    private val adapter by lazy {
        MultiListSquareAdapter(mutableListOf(), resetAudioListener = this).apply {
            chat = false
        }
    }

    override fun resetAudioState() {
        if (mediaPlayer != null) {
            mediaPlayer!!.resetMedia()
            mediaPlayer = null
        }
        currPlayIndex = -1

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list_square, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }


    private fun initView() {
        mPresenter = SquarePresenter()
        mPresenter.mView = this
        mPresenter.context = activity!!
        listRefresh.setOnLoadMoreListener(this)
        EventBus.getDefault().register(this)

        val linearLayoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        listSquareRv.layoutManager = linearLayoutManager
        listSquareRv.adapter = adapter
        adapter.type = MySquareFragment.TYPE_OTHER_DETAIL
        adapter.bindToRecyclerView(listSquareRv)
        stateList.retryBtn.onClick {
            stateList.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.getSomeoneSquare(params)
        }

        //取消动画，主要是闪烁
        listSquareRv.itemAnimator?.changeDuration = 0

        adapter.setOnItemClickListener { _, view, position ->
            if (mediaPlayer != null) {
                mediaPlayer!!.resetMedia()
                mediaPlayer = null
            }
            SquareCommentDetailActivity.start(activity!!, adapter.data[position], position = position)
        }

        adapter.setOnItemChildClickListener { _, view, position ->
            val squareBean = adapter.data[position]
            when (view.id) {
                R.id.squareChatBtn1 -> {
                    if (mediaPlayer != null) {
                        mediaPlayer!!.resetMedia()
                        mediaPlayer = null
                    }
                    ChatActivity.start(activity!!, adapter.data[position].accid ?: "")
                }
                R.id.squareCommentBtn1 -> {
                    if (mediaPlayer != null) {
                        mediaPlayer!!.resetMedia()
                        mediaPlayer = null
                    }
                    SquareCommentDetailActivity.start(
                        activity!!,
                        adapter.data[position],
                        enterPosition = "comment",
                        position = position
                    )
                }
                R.id.squareDianzanBtn1 -> {
                    //  clickZan(position)
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
    }

    override fun loadData() {
        initView()
        mPresenter.getSomeoneSquare(params)
    }


    override fun onGetFriendsListResult(friends: MutableList<FriendBean?>) {

    }

    override fun onSquareAnnounceResult(type: Int, b: Boolean, code: Int) {
    }

    override fun onQnUploadResult(b: Boolean, type: Int, key: String?) {
    }


    override fun onGetSquareListResult(data: SquareListBean?, result: Boolean, isRefresh: Boolean) {
        if (result) {
            stateList.viewState = MultiStateView.VIEW_STATE_CONTENT
            if (data == null || data.list == null || data!!.list!!.size == 0) {
                if (adapter.data.isNullOrEmpty()) {
                    stateList.viewState = MultiStateView.VIEW_STATE_EMPTY
                }
                listRefresh.finishLoadMoreWithNoMoreData()
            } else {
                for (tempData in 0 until data!!.list!!.size) {
                    data.list!![tempData].type = when {
                        !data.list!![tempData].video_json.isNullOrEmpty() -> SquareBean.VIDEO
                        !data.list!![tempData].audio_json.isNullOrEmpty() -> SquareBean.AUDIO
                        !data.list!![tempData].photo_json.isNullOrEmpty() || (data.list!![tempData].photo_json.isNullOrEmpty() && data.list!![tempData].audio_json.isNullOrEmpty() && data.list!![tempData].video_json.isNullOrEmpty()) -> SquareBean.PIC
                        else -> SquareBean.PIC
                    }
                    data.list!![tempData].originalLike = data.list!![tempData].isliked
                    data.list!![tempData].originalLikeCount = data.list!![tempData].like_cnt
                }
                adapter.addData(data!!.list!!)
                listRefresh.finishLoadMore(true)

            }
        } else {
            if (listRefresh != null && page > 1)
                listRefresh.finishLoadMore(false)
            else
                stateList.viewState = MultiStateView.VIEW_STATE_ERROR
        }
        EventBus.getDefault().post(UserDetailViewStateEvent(result))

    }

    override fun onGetSquareLikeResult(position: Int, result: Boolean) {
        if (result) {
            adapter.data[position].originalLike = adapter.data[position].isliked
            EventBus.getDefault().post(RefreshSquareEvent(refresh = true, from = TAG))
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
                adapter.notifyItemChanged(position)
                EventBus.getDefault().post(RefreshSquareEvent(refresh = true, from = TAG))
            }
        }
        if (moreActionDialog != null && moreActionDialog.isShowing) {
            moreActionDialog.dismiss()
        }
    }

    override fun onGetSquareReport(baseResp: BaseResp<Any?>?, position: Int) {
        if (baseResp != null)
            CommonFunction.toast(baseResp.msg)
        if (moreActionDialog != null && moreActionDialog.isShowing) {
            moreActionDialog.dismiss()
        }
    }


    override fun onLoadMore(refreshLayout: RefreshLayout) {
        page++
        params["page"] = page
        mPresenter.getSomeoneSquare(params)
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
                CommonFunction.toast("音频播放出错")
                adapter.data[position].isPlayAudio = IjkMediaPlayerUtil.MEDIA_ERROR
                currPlayIndex = -1
//                adapter.notifyItemChanged(position)
                adapter.notifyDataSetChanged()
                mediaPlayer!!.resetMedia()
                mediaPlayer = null
            }

            override fun onPrepared(position: Int) {
                //todo  异步准备 准备好了才会实现播放。
//                adapter.data[position].isPlayAudio = IjkMediaPlayerUtil.MEDIA_PLAY
//                adapter.notifyItemChanged(position)
//                adapter.notifyDataSetChanged()
                mediaPlayer!!.startPlay()
            }

            override fun onPreparing(position: Int) {
            }

            override fun onRelease(position: Int) {
                adapter.data[position].isPlayAudio = IjkMediaPlayerUtil.MEDIA_STOP
                adapter.notifyItemChanged(position)
//                adapter.notifyDataSetChanged()
            }

        }).getInstance()
    }

    /**
     * 展示转发动态对话框
     */
    private fun showTranspondDialog(squareBean: SquareBean) {
        val transpondDialog = TranspondDialog(activity!!, squareBean)
        transpondDialog.show()
    }


    lateinit var moreActionDialog: MoreActionNewDialog
    /**
     * 展示更多操作对话框
     */
    private fun showMoreDialog(position: Int) {
        moreActionDialog = MoreActionNewDialog(activity!!, adapter.data[position])
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
            val dialog = DeleteDialog(activity!!)
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


    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onListDataEvent(event: ListDataEvent) {
        targetAccid = event.targetAccid
        params["target_accid"] = targetAccid
        if (event.refresh) {
            adapter.data.clear()
            listRefresh.setNoMoreData(false)
            page = 1
            params["page"] = page
            mPresenter.getSomeoneSquare(params)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshSquareEvent(event: RefreshSquareEvent) {
        if (event.from != TAG) {
            adapter.data.clear()
            listRefresh.setNoMoreData(false)
            page = 1
            params["page"] = page
            mPresenter.getSomeoneSquare(params)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshCommentEvent(event: RefreshCommentEvent) {
        if (event.position != -1) {
            adapter.data[event.position].comment_cnt = event.commentNum
            adapter.refreshNotifyItemChanged(event.position)
        }
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
