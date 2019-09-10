package com.sdy.jitangapplication.ui.activity

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.ToastUtils
import com.kennyc.view.MultiStateView
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.demoap.MultiListDetailPlayAdapter
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.RefreshSquareEvent
import com.sdy.jitangapplication.model.SquareBean
import com.sdy.jitangapplication.player.IjkMediaPlayerUtil
import com.sdy.jitangapplication.player.OnPlayingListener
import com.sdy.jitangapplication.presenter.SquarePlayDetaiPresenter
import com.sdy.jitangapplication.presenter.view.SquarePlayDetailView
import com.sdy.jitangapplication.ui.dialog.MoreActionDialog
import com.sdy.jitangapplication.ui.dialog.TranspondDialog
import com.sdy.jitangapplication.utils.UserManager
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.utils.GSYVideoType
import kotlinx.android.synthetic.main.activity_square_play_detail.btnBack
import kotlinx.android.synthetic.main.activity_square_play_list_detail.*
import kotlinx.android.synthetic.main.dialog_more_action.*
import kotlinx.android.synthetic.main.error_layout.view.*
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

/**
 * 点击图片、视频、录音进入详情页面，并且支持点击左右切换好友动态
 *    from 确定内容的来源地  1好友列表 2广场列表 3聊天跳转
 */
public class SquarePlayListDetailActivity : BaseMvpActivity<SquarePlayDetaiPresenter>(), SquarePlayDetailView,
    View.OnClickListener {

    //确定内容的来源地  1好友列表 2广场列表 3聊天跳转
    private var from = -1
    private val TAG = SquarePlayListDetailActivity::class.java.simpleName

    //广场列表内容适配器
    private val adapter by lazy { MultiListDetailPlayAdapter(this, mutableListOf()) }
    private val squareBean: SquareBean by lazy { intent.getSerializableExtra("item") as SquareBean }

    companion object {
        public fun start(context: Context, id: Int) {
            context.startActivity<SquarePlayListDetailActivity>("id" to id)
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
//                adapter.notifyItemChanged(position)
                adapter.notifyDataSetChanged()
                mediaPlayer!!.resetMedia()
                mediaPlayer = null
            }

            override fun onError(position: Int) {
                toast("音频播放出错")
                adapter.data[position].isPlayAudio = IjkMediaPlayerUtil.MEDIA_ERROR
//                adapter.notifyItemChanged(position)
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
            }

            override fun onRelease(position: Int) {
                adapter.data[position].isPlayAudio = IjkMediaPlayerUtil.MEDIA_STOP
                adapter.notifyDataSetChanged()
            }

        }).getInstance()
    }


    private val fromChatParams by lazy {
        hashMapOf(
            "accid" to SPUtils.getInstance(Constants.SPNAME).getString("accid"),
            "token" to SPUtils.getInstance(Constants.SPNAME).getString("token"),
            "square_id" to intent.getIntExtra("id", -1)
        )
    }

    private val fromFriendsParams by lazy {
        hashMapOf<String, Any>(
            "accid" to SPUtils.getInstance(Constants.SPNAME).getString("accid"),
            "token" to SPUtils.getInstance(Constants.SPNAME).getString("token"),
            "target_accid" to intent.getStringExtra("target_accid")
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_square_play_list_detail)
        initView()
        if (intent.getSerializableExtra("item") != null) {//广场列表
            from = 2
            rvLast.visibility = View.GONE
            rvNext.visibility = View.GONE
            adapter.addData(squareBean)
            stateview.viewState = MultiStateView.VIEW_STATE_CONTENT
        } else if (intent.getIntExtra("id", -1) != -1) {//好友聊天
            from = 3
            rvLast.visibility = View.GONE
            rvNext.visibility = View.GONE
            mPresenter.getSquareInfo(fromChatParams)
        } else {//好友列表
            from = 1
            rvLast.visibility = View.VISIBLE
            rvNext.visibility = View.VISIBLE
            mPresenter.getRencentlySquares(fromFriendsParams)
        }
    }

    val layoutmanager by lazy {
        object : LinearLayoutManager(this, RecyclerView.HORIZONTAL, false) {
            override fun canScrollHorizontally(): Boolean {
                return false
            }
        }
    }
    //音频当前播放位置
    private var currPlayIndex = -1

    private fun initView() {
        ScreenUtils.setFullScreen(this)
        btnBack.onClick { finish() }
        GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_DEFAULT)
        mPresenter = SquarePlayDetaiPresenter()
        mPresenter.mView = this
        mPresenter.context = this

        stateview.retryBtn.onClick {
            stateview.viewState = MultiStateView.VIEW_STATE_LOADING
            if (from == 1) {
                mPresenter.getRencentlySquares(fromFriendsParams)
            } else if (from == 3) {
                mPresenter.getSquareInfo(fromChatParams)
            }
        }


        friendSquareList.layoutManager = layoutmanager
        friendSquareList.adapter = adapter
        adapter.bindToRecyclerView(friendSquareList)
        //取消动画，主要是闪烁
        (friendSquareList.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        //获取用户输入的评论
        adapter.onTextChangeListener = object : MultiListDetailPlayAdapter.OnTextChangeListener {
            override fun afterTextChanged(text: String, position: Int) {
                adapter.data[position].comment = text
            }
        }
        adapter.setOnItemChildClickListener { _, view, position ->
            val squareBean = adapter.data[position]
            when (view.id) {
                //播放
                R.id.detailPlayBtn -> {
                    if (mediaPlayer == null || currPlayIndex != position && squareBean.isPlayAudio != IjkMediaPlayerUtil.MEDIA_PLAY) {
                        initAudio(position)
                        //todo  还原播放器
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
                //评论
                R.id.detailPlayCommentSend -> {
                    if (!adapter.data[position].comment.isNullOrEmpty())
                        mPresenter.addComment(
                            hashMapOf(
                                "accid" to SPUtils.getInstance(Constants.SPNAME).getString("accid"),
                                "token" to SPUtils.getInstance(Constants.SPNAME).getString("token"),
                                "square_id" to adapter.data[position].id!!,
                                "content" to (adapter.data[position].comment ?: "")
                            ), position
                        )
                    else
                        toast("说点什么吧")
                }
                //更多操作
                R.id.detailPlayMoreActions -> {
                    showMoreDialog(position)
                }
                //点赞
                R.id.detailPlaydianzan -> {
                    val params = hashMapOf(
                        "token" to SPUtils.getInstance(Constants.SPNAME).getString("token"),
                        "accid" to SPUtils.getInstance(Constants.SPNAME).getString("accid"),
                        "type" to if (adapter.data[position].isliked == 1) {
                            2
                        } else {
                            1
                        },
                        "square_id" to adapter.data[position].id!!,
                        "_timestamp" to System.currentTimeMillis()
                    )
                    mPresenter.getSquareLike(params, position)
                }
                //点击内容跳转到评论详情页面
                R.id.detailPlayContent, R.id.detailPlayCommentBtn -> {
                    SquareCommentDetailActivity.start(this, adapter.data[position])
                }
                R.id.audioFl -> {
                    hideCover(position, SquareBean.AUDIO)
                }

            }
        }


        //上一个
        rvLast.setOnClickListener(this)
        //下一个
        rvNext.setOnClickListener(this)

    }

    //type :
    public fun hideCover(position: Int, type: Int) {
        when (type) {
            SquareBean.AUDIO, SquareBean.PIC -> {
                val coverLayout = adapter.getViewByPosition(position, R.id.coverLayout)
                if (coverLayout != null)
                    if (coverLayout.isVisible) {
                        coverLayout.isVisible = false
                        btnBack.isVisible = false
                    } else {
                        coverLayout.isVisible = true
                        btnBack.isVisible = true
                    }
            }

            SquareBean.VIDEO -> {
                val coverLayout = adapter.getViewByPosition(position, R.id.videoCover)
                if (coverLayout != null)
                    if (coverLayout.isVisible) {
                        coverLayout.isVisible = false
                        btnBack.isVisible = false
                    } else {
                        coverLayout.isVisible = true
                        btnBack.isVisible = true
                    }
            }
        }
    }


    private var currentIndex = 0
    private fun moveToPosition(manager: LinearLayoutManager, mRecyclerView: RecyclerView, currentIndex: Int) {
        val firstItem = manager.findFirstVisibleItemPosition()
        val lastItem = manager.findLastVisibleItemPosition()
        if (currentIndex <= firstItem) {
            mRecyclerView.scrollToPosition(currentIndex)
        } else if (currentIndex <= lastItem) {
            val top = mRecyclerView.getChildAt(currentIndex - firstItem).getTop()
            mRecyclerView.scrollBy(0, top)
        } else {
            mRecyclerView.scrollToPosition(currentIndex)
        }
    }

    lateinit var moreActionDialog: MoreActionDialog

    /**
     * 展示更多操作对话框
     */
    private fun showMoreDialog(position: Int) {
        moreActionDialog = MoreActionDialog(this, "square_detail")
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


        moreActionDialog.llShare.onClick {
            showTranspondDialog(adapter.data[position])
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
            AlertDialog.Builder(this)
                .setNegativeButton("取消举报") { p0, p1 -> p0.cancel() }
                .setPositiveButton("确认举报") { p0, p1 ->
                    mPresenter.getSquareReport(
                        hashMapOf(
                            "accid" to UserManager.getAccid(),
                            "token" to UserManager.getToken(),
                            "square_id" to adapter.data[position].id!!,
                            "_timestamp" to System.currentTimeMillis()
                        )
                    )
                }
                .setTitle("举报")
                .setMessage("是否确认举报该动态？")
                .show()
        }
        moreActionDialog.cancel.onClick {
            moreActionDialog.dismiss()
        }

    }

    override fun onGetSquareReport(t: Boolean) {
        if (t) {
            ToastUtils.showShort("举报成功！")
        } else {
            ToastUtils.showShort("举报失败！")
        }

    }

    override fun onRemoveMySquareResult(result: Boolean, position: Int) {

        if (result) {
            toast("删除动态成功！")
            if (adapter.data.size == 1) {
                finish()
            } else {
                adapter.remove(position)
            }
            EventBus.getDefault().post(RefreshSquareEvent(true, TAG))

        } else {
            toast("删除动态失败！")
        }
    }


    /**
     * 展示转发动态对话框
     */
    private fun showTranspondDialog(squareBean: SquareBean) {
        val transpondDialog = TranspondDialog(this, squareBean)
        if (transpondDialog != null && !transpondDialog.isShowing)
            transpondDialog.show()
    }

    override fun onGetSquareInfoResults(mutableList: SquareBean?) {
        stateview.viewState = MultiStateView.VIEW_STATE_CONTENT
        if (mutableList != null) {
            mutableList.type = when {
                !mutableList.video_json.isNullOrEmpty() -> SquareBean.VIDEO
                !mutableList.audio_json.isNullOrEmpty() -> SquareBean.AUDIO
                !mutableList.photo_json.isNullOrEmpty() ||
                        (mutableList.photo_json.isNullOrEmpty() && mutableList.audio_json.isNullOrEmpty() && mutableList.video_json.isNullOrEmpty()) -> SquareBean.PIC
                else -> SquareBean.PIC
            }
            adapter.addData(mutableList)
        } else {
            ToastUtils.showLong("该动态已经被删除了")
            finish()
        }
    }

    override fun onGetRecentlySquaresResults(data: MutableList<SquareBean?>) {
        stateview.viewState = MultiStateView.VIEW_STATE_CONTENT
//        data.addAll(data)
        for (tempData in 0 until data.size) {

            data[tempData]!!.type = when {
                !data!![tempData]!!.video_json.isNullOrEmpty() -> SquareBean.VIDEO
                !data!![tempData]!!.audio_json.isNullOrEmpty() -> SquareBean.AUDIO
                !data!![tempData]!!.photo_json.isNullOrEmpty() ||
                        (data!![tempData]!!.photo_json.isNullOrEmpty() && data!![tempData]!!.audio_json.isNullOrEmpty() && data!![tempData]!!.video_json.isNullOrEmpty()) -> SquareBean.PIC
                else -> SquareBean.PIC
            }
        }

        if (data.size <= 1) {
            rvLast.visibility = View.GONE
            rvNext.visibility = View.GONE
        } else {
            rvLast.visibility = View.GONE
            rvNext.visibility = View.VISIBLE
        }
        adapter.addData(data)

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
            adapter.notifyItemChanged(position, "hahah")
            EventBus.getDefault().post(RefreshSquareEvent(true, TAG))
        } else {
            ToastUtils.showShort("点赞失败！")
        }
    }

    override fun onGetSquareCollectResult(position: Int, data: BaseResp<Any?>) {
        toast(data.msg)
        if (adapter.data[position].iscollected == 1) {
            adapter.data[position].iscollected = 0
        } else {
            adapter.data[position].iscollected = 1
        }
        if (moreActionDialog != null && moreActionDialog.isShowing) {
            moreActionDialog.dismiss()
        }
        EventBus.getDefault().post(RefreshSquareEvent(true, TAG))
    }

    override fun onAddCommentResult(position: Int, data: BaseResp<Any?>) {
        toast(data.msg)
        if (data.code == 200) {
            adapter.data[position].comment = ""
            adapter.notifyItemChanged(position)
            EventBus.getDefault().post(RefreshSquareEvent(true, TAG))
        }
    }


    override fun onPause() {
        super.onPause()
        GSYVideoManager.onPause()
    }

    override fun onResume() {
        super.onResume()
        GSYVideoManager.onResume(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        //释放所有
        GSYVideoManager.releaseAllVideos()
        if (mediaPlayer != null) {
            mediaPlayer!!.resetMedia()
            mediaPlayer = null
        }
    }


    override fun finish() {
        super.finish()
        //释放所有
        GSYVideoManager.releaseAllVideos()
        if (mediaPlayer != null) {
            mediaPlayer!!.resetMedia()
            mediaPlayer = null
        }
    }

    override fun onError(text: String) {
        stateview.viewState = MultiStateView.VIEW_STATE_ERROR
        stateview.errorMsg.text = if (mPresenter.checkNetWork()) {
            getString(R.string.retry_load_error)
        } else {
            getString(R.string.retry_net_error)
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            //上一个
            R.id.rvLast -> {
                if (currentIndex > 0) {
                    currentIndex--
                    Log.i("squareplaydetail", "$currentIndex")
                }
                if (currentIndex >= 0) {
                    moveToPosition(layoutmanager, friendSquareList, currentIndex)
                }

                if (currentIndex == 0) {
                    rvLast.visibility = View.GONE
                } else {
                    rvLast.visibility = View.VISIBLE
                }
                if (adapter.data.size > 1) {
                    rvNext.visibility = View.VISIBLE
                }
                GSYVideoManager.releaseAllVideos()
                if (mediaPlayer != null) {
                    mediaPlayer!!.resetMedia()
                    mediaPlayer = null
                }
            }
            R.id.rvNext -> {
                if (currentIndex < adapter.data.size) {
                    currentIndex++
                    Log.i("squareplaydetail", "$currentIndex")
                }
                if (currentIndex < adapter.data.size) {
                    moveToPosition(layoutmanager, friendSquareList, currentIndex)
                }
                if (currentIndex + 1 == adapter.data.size) {
                    rvNext.visibility = View.GONE
                } else {
                    rvNext.visibility = View.VISIBLE
                }
                if (adapter.data.size > 1) {
                    rvLast.visibility = View.VISIBLE
                }
                GSYVideoManager.releaseAllVideos()
                if (mediaPlayer != null) {
                    mediaPlayer!!.resetMedia()
                    mediaPlayer = null
                }
            }
        }
    }
}