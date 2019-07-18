package com.example.demoapplication.ui.fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.example.demoapplication.R
import com.example.demoapplication.common.Constants
import com.example.demoapplication.event.UpdateLabelEvent
import com.example.demoapplication.model.FriendBean
import com.example.demoapplication.model.SquareBean
import com.example.demoapplication.model.SquareListBean
import com.example.demoapplication.player.IjkMediaPlayerUtil
import com.example.demoapplication.player.OnPlayingListener
import com.example.demoapplication.presenter.SquarePresenter
import com.example.demoapplication.presenter.view.SquareView
import com.example.demoapplication.ui.activity.SquareCommentDetailActivity
import com.example.demoapplication.ui.activity.SquarePlayListDetailActivity
import com.example.demoapplication.ui.adapter.MultiListSquareAdapter
import com.example.demoapplication.ui.adapter.SquareFriendsAdapter
import com.example.demoapplication.ui.dialog.MoreActionDialog
import com.example.demoapplication.ui.dialog.TranspondDialog
import com.example.demoapplication.utils.ScrollCalculatorHelper
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.fragment.BaseMvpFragment
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.shuyu.gsyvideoplayer.utils.GSYVideoType
import kotlinx.android.synthetic.main.dialog_more_action.*
import kotlinx.android.synthetic.main.fragment_square.*
import kotlinx.android.synthetic.main.headerview_label.*
import kotlinx.android.synthetic.main.headerview_label.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast


/**
 * 广场列表
 * //todo 发布列表接口接入
 */
class SquareFragment : BaseMvpFragment<SquarePresenter>(), SquareView, OnRefreshListener, OnLoadMoreListener {


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
//            "accid" to Constants.ACCID,
//            "token" to Constants.TOKEN,
            "accid" to SPUtils.getInstance(Constants.SPNAME).getString("accid"),
            "token" to SPUtils.getInstance(Constants.SPNAME).getString("token"),
            "page" to page,
            "pagesize" to Constants.PAGESIZE,
            "_timestamp" to System.currentTimeMillis(),
            "tagid" to 1
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

        squareDynamicRv.layoutManager = layoutManager
        squareDynamicRv.adapter = adapter
        adapter.addHeaderView(initFriendsView())
        //取消动画，主要是闪烁
//        (squareDynamicRv.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        squareDynamicRv.itemAnimator?.changeDuration = 0

        //限定范围为屏幕一半的上下偏移180
        val playTop = ScreenUtils.getScreenHeight() / 2 - SizeUtils.dp2px(126F)
        val playBottom = ScreenUtils.getScreenHeight() / 2 + SizeUtils.dp2px(126F)
        scrollCalculatorHelper = ScrollCalculatorHelper(R.id.squareUserVideo, playTop, playBottom)
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

        adapter.setOnItemClickListener { adapter, view, position ->
            startActivity<SquareCommentDetailActivity>("squareBean" to adapter.data[position])
            if (mediaPlayer != null) {
                mediaPlayer!!.resetMedia()
                mediaPlayer = null
            }
        }

        adapter.setOnItemChildClickListener { _, view, position ->
            val squareBean = adapter.data[position]
            when (view.id) {
                R.id.squareChatBtn1 -> {
                    toast("聊天呗$position")
                }
                R.id.squareCommentBtn1 -> {
                    startActivity<SquareCommentDetailActivity>(
                        "squareBean" to squareBean,
                        "enterPosition" to "comment"
                    )
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
                    showTranspondDialog()
                }
                R.id.squareMoreBtn1 -> {
                    showMoreDialog(position)
                }
                //播放音频
                R.id.audioPlayBtn -> {
                    if (currPlayIndex != position && squareBean.isPlayAudio != IjkMediaPlayerUtil.MEDIA_PLAY) {
                        initAudio(position)
                        mediaPlayer!!.setDataSource(squareBean.audio_json?.get(0) ?: "").prepareMedia()
                        currPlayIndex = position
                    }
                    for (index in 0 until adapter.data.size) {
                        if (index != position && adapter.data[index].type == 3) {
                            adapter.data[index].isPlayAudio = IjkMediaPlayerUtil.MEDIA_STOP
                        }
                    }
                    if (squareBean.isPlayAudio == IjkMediaPlayerUtil.MEDIA_PREPARE) {
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
        mPresenter.getSquareList(listParams, true)
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
//                adapter.notifyItemChanged(position)
                adapter.notifyDataSetChanged()
                mediaPlayer!!.resetMedia()
                mediaPlayer = null
            }

            override fun onError(position: Int) {
                toast("音频播放出错")
                adapter.data[position].isPlayAudio = IjkMediaPlayerUtil.MEDIA_STOP
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
//                adapter.notifyItemChanged(position)
                adapter.notifyDataSetChanged()
            }

        }).getInstance()
    }


    private val transpondDialog by lazy { TranspondDialog(activity!!) }
    /**
     * 展示转发动态对话框
     */
    private fun showTranspondDialog() {
        if (transpondDialog != null && !transpondDialog.isShowing)
            transpondDialog.show()
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
//        GSYVideoManager.releaseAllVideos()
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
            friendTv.visibility = View.GONE
        } else {
            friendsAdapter.setNewData(friends)
            friendTv.visibility = View.VISIBLE
        }
    }

    override fun onGetSquareListResult(data: SquareListBean?, result: Boolean, refresh: Boolean) {
        if (result) {
            for (tempData in 0 until data!!.data.size) {
                data!!.data[tempData].type = when {
                    !data!!.data[tempData].video_json.isNullOrEmpty() -> SquareBean.VIDEO
                    !data!!.data[tempData].audio_json.isNullOrEmpty() -> SquareBean.AUDIO
                    !data!!.data[tempData].photo_json.isNullOrEmpty() || (data!!.data[tempData].photo_json.isNullOrEmpty() && data!!.data[tempData].audio_json.isNullOrEmpty() && data!!.data[tempData].video_json.isNullOrEmpty()) -> SquareBean.PIC
                    else -> SquareBean.PIC
                }
            }
            if (refresh)
                adapter.setNewData(data!!.data)
            else
                adapter.addData(data!!.data)
//            adapter.notifyDataSetChanged()
        }
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

    override fun onGetSquareCollectResult(position: Int, data: BaseResp<Any?>) {
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

    override fun onGetSquareReport(baseResp: BaseResp<Any?>, position: Int) {
        toast(baseResp.msg)
        if (moreActionDialog != null && moreActionDialog.isShowing) {
            moreActionDialog.dismiss()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateLabelEvent(event: UpdateLabelEvent) {
        listParams["tagid"] = event.label.id
        //这个地方还要默认设置选中第一个标签来更新数据
        mPresenter.getSquareList(listParams, true)
    }


}

