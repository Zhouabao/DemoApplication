package com.sdy.jitangapplication.ui.fragment

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isNotEmpty
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
import com.sdy.baselibrary.utils.CustomClickListener
import com.sdy.baselibrary.utils.RandomUtils
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.*
import com.sdy.jitangapplication.model.*
import com.sdy.jitangapplication.player.IjkMediaPlayerUtil
import com.sdy.jitangapplication.player.OnPlayingListener
import com.sdy.jitangapplication.presenter.SquarePresenter
import com.sdy.jitangapplication.presenter.view.SquareView
import com.sdy.jitangapplication.switchplay.SwitchUtil
import com.sdy.jitangapplication.switchplay.SwitchVideo
import com.sdy.jitangapplication.ui.activity.*
import com.sdy.jitangapplication.ui.adapter.AllTitleAdapter
import com.sdy.jitangapplication.ui.adapter.MultiListSquareAdapter
import com.sdy.jitangapplication.ui.adapter.SquareSwitchAdapter
import com.sdy.jitangapplication.ui.adapter.TagAdapter
import com.sdy.jitangapplication.ui.dialog.ChargeLabelDialog
import com.sdy.jitangapplication.utils.ScrollCalculatorHelper
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.CommonAlertDialog
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack
import com.shuyu.gsyvideoplayer.utils.GSYVideoType
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView.CURRENT_STATE_ERROR
import kotlinx.android.synthetic.main.empty_friend_layout.view.*
import kotlinx.android.synthetic.main.error_layout.*
import kotlinx.android.synthetic.main.fragment_square.*
import kotlinx.android.synthetic.main.headerview_square_recommend_title.view.*
import kotlinx.android.synthetic.main.popupwindow_square_filter_gender.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.support.v4.startActivity


/**
 * 广场列表
 */
class SquareFragment : BaseMvpLazyLoadFragment<SquarePresenter>(), SquareView, OnRefreshListener, OnLoadMoreListener,
    MultiListSquareAdapter.ResetAudioListener {
    private var checkedTitleId = 0
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
            "gender" to sp.getInt("filter_square_gender", 3),
            "type" to when (checkedTitleId) {
                SQUARE_TAG -> {
                    1
                }
                else -> {
                    3
                }
            }
        )
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_square, container, false)
    }


    /**
     * 推荐标题帮助
     */
    private val helpPop by lazy {
        PopupWindow(activity!!).apply {
            contentView =
                LayoutInflater.from(activity!!).inflate(R.layout.popupwindow_square_recommend_help, null, false)
            width = ViewGroup.LayoutParams.WRAP_CONTENT
            height = ViewGroup.LayoutParams.WRAP_CONTENT
            setBackgroundDrawable(null)
            isOutsideTouchable = true
            setOnDismissListener {
                adapter.headerLayout.recommendHelpBtn.setImageResource(R.drawable.icon_square_recommend_help_normal)
            }

        }
    }

    //推荐话题适配器
    private val topicAdapter: AllTitleAdapter by lazy { AllTitleAdapter(3) }

    //创建推荐话题头布局
    private fun initRecommendTopicHeader(): View {
        val recommendTopicView =
            LayoutInflater.from(activity!!).inflate(R.layout.headerview_square_recommend_title, squareDynamicRv, false)
        val linearLayoutManager = LinearLayoutManager(activity!!, RecyclerView.HORIZONTAL, false)
        recommendTopicView.headRv.layoutManager = linearLayoutManager
        recommendTopicView.headRv.adapter = topicAdapter

        recommendTopicView.recommendMoreBtn.onClick(object : CustomClickListener() {
            override fun onSingleClick(view: View) {
                startActivity<AllTitleActivity>()
            }
        })

        recommendTopicView.recommendTitle.onClick {
            if (helpPop.isShowing) {
                helpPop.dismiss()
                recommendTopicView.recommendHelpBtn.setImageResource(R.drawable.icon_square_recommend_help_normal)
            } else {
                recommendTopicView.recommendHelpBtn.setImageResource(R.drawable.icon_square_recommend_help_choosed)
                helpPop.showAsDropDown(
                    adapter.headerLayout.recommendTitle,
                    SizeUtils.dp2px(15F),
                    SizeUtils.dp2px(-65F),
                    Gravity.TOP
                )
            }
        }
        recommendTopicView.recommendHelpBtn.onClick {
            if (helpPop.isShowing) {
                helpPop.dismiss()
                recommendTopicView.recommendHelpBtn.setImageResource(R.drawable.icon_square_recommend_help_normal)
            } else {
                recommendTopicView.recommendHelpBtn.setImageResource(R.drawable.icon_square_recommend_help_choosed)
                helpPop.showAsDropDown(
                    adapter.headerLayout.recommendTitle,
                    SizeUtils.dp2px(15F),
                    SizeUtils.dp2px(-65F),
                    Gravity.TOP
                )
            }
        }

        return recommendTopicView
    }


    private var currPlayIndex = -1

    private val filterPopupWindow by lazy {
        PopupWindow(activity!!).apply {
            contentView =
                LayoutInflater.from(activity!!).inflate(R.layout.popupwindow_square_filter_gender, null, false)
            width = ViewGroup.LayoutParams.WRAP_CONTENT
            height = ViewGroup.LayoutParams.WRAP_CONTENT
            setBackgroundDrawable(null)
            isOutsideTouchable = true

            contentView.genderAll.onClick {
                contentView.genderAll.setTextColor(activity!!.resources.getColor(R.color.colorOrange))
                contentView.genderMan.setTextColor(Color.parseColor("#191919"))
                contentView.genderWoman.setTextColor(Color.parseColor("#191919"))
                filterGenderBtn.setImageResource(R.drawable.icon_square_filter_gender)
                sp.put("filter_square_gender", 3)
                refreshLayout.autoRefresh()
                dismiss()
            }
            contentView.genderMan.onClick {
                contentView.genderMan.setTextColor(activity!!.resources.getColor(R.color.colorOrange))
                contentView.genderAll.setTextColor(Color.parseColor("#191919"))
                contentView.genderWoman.setTextColor(Color.parseColor("#191919"))
                filterGenderBtn.setImageResource(R.drawable.icon_square_filter_man)
                sp.put("filter_square_gender", 1)
                refreshLayout.autoRefresh()
                dismiss()

            }
            contentView.genderWoman.onClick {
                contentView.genderWoman.setTextColor(activity!!.resources.getColor(R.color.colorOrange))
                contentView.genderAll.setTextColor(Color.parseColor("#191919"))
                contentView.genderMan.setTextColor(Color.parseColor("#191919"))
                filterGenderBtn.setImageResource(R.drawable.icon_square_filter_woman)
                sp.put("filter_square_gender", 2)
                refreshLayout.autoRefresh()
                dismiss()
            }
        }
    }

    private val sp by lazy { SPUtils.getInstance(Constants.SPNAME) }


    private val tags by lazy { mutableListOf<TagBean>() }
    private val tagAdapter by lazy { TagAdapter() }

    private fun setTagData() {
        tags.clear()
        tags.addAll(UserManager.getSpLabels())
        //初始化选中的tag
        if (UserManager.getGlobalLabelSquareId() == 0) {
            if (tags.isNotEmpty()) {
                tags[0].cheked = true
                listParams["tag_id"] = tags[0].id
                UserManager.saveGlobalLabelSquareId(tags[0].id)
            }
        } else {
            var hasCheck = false
            for (tag in tags) {
                if (tag.id == UserManager.getGlobalLabelSquareId()) {
                    tag.cheked = true
                    listParams["tag_id"] = tag.id
                    hasCheck = true
                    break
                }
            }
            if (!hasCheck) {
                tags[0].cheked = true
                listParams["tag_id"] = tags[0].id
                UserManager.saveGlobalLabelSquareId(tags[0].id)
            }
        }
        tagAdapter.setNewData(tags)
    }

    private fun initTagsView() {
        addTagBtn.onClick(object : CustomClickListener() {
            override fun onSingleClick(view: View) {
                val intent = Intent()
                intent.putExtra("from", AddLabelActivity.FROM_ADD_NEW)
                intent.setClass(activity!!, AddLabelActivity::class.java)
                startActivity(intent)
            }

        })

        squareTagRv.layoutManager = LinearLayoutManager(activity!!, RecyclerView.HORIZONTAL, false)
        squareTagRv.adapter = tagAdapter
        setTagData()
        tagAdapter.setOnItemClickListener { _, view, position ->
            for (tag in tagAdapter.data) {
                tag.cheked = tag == tagAdapter.data[position]
            }
            tagAdapter.notifyDataSetChanged()
            UserManager.saveGlobalLabelSquareId(tagAdapter.data[position].id)
            listParams["tag_id"] = tagAdapter.data[position].id
            updateChooseTitle()
        }

    }


    companion object {
        const val SQUARE_TAG = 0
        const val SQUARE_FRIEND = 1
    }

    val titleAdapter by lazy { SquareSwitchAdapter() }
    private fun initHeadView() {
        initTagsView()

        rgSquare.layoutManager = LinearLayoutManager(activity!!, RecyclerView.HORIZONTAL, false)
        titleAdapter.addData(SquareTitleBean("广场", true))
        titleAdapter.addData(SquareTitleBean("好友", false))
        rgSquare.adapter = titleAdapter

        titleAdapter.setOnItemClickListener { _, view, position ->
            for (data in titleAdapter.data) {
                data.checked = data == titleAdapter.data[position]
            }
            titleAdapter.notifyDataSetChanged()

            checkedTitleId = if (position == 0) {
                SQUARE_TAG
            } else {
                SQUARE_FRIEND
            }
            squareTagRv.isVisible = checkedTitleId == SQUARE_TAG
            addTagBg.isVisible = checkedTitleId == SQUARE_TAG
            updateChooseTitle()
        }

        /**
         *性别筛选
         */
        if (sp.getInt("filter_square_gender", 3) == 3) {
            filterGenderBtn.setImageResource(R.drawable.icon_square_filter_gender)
        } else if (sp.getInt("filter_square_gender", 3) == 1) {
            filterGenderBtn.setImageResource(R.drawable.icon_square_filter_man)
        } else if (sp.getInt("filter_square_gender", 3) == 2) {
            filterGenderBtn.setImageResource(R.drawable.icon_square_filter_woman)
        }
        filterGenderBtn.onClick {
            if (filterPopupWindow.isShowing) {
                filterPopupWindow.dismiss()
            } else {
                filterPopupWindow.showAsDropDown(filterGenderBtn, 0, SizeUtils.dp2px(-15F))
                if (sp.getInt("filter_square_gender", 3) == 3) {
                    filterPopupWindow.contentView.genderAll.setTextColor(activity!!.resources.getColor(R.color.colorOrange))
                    filterPopupWindow.contentView.genderMan.setTextColor(Color.parseColor("#191919"))
                    filterPopupWindow.contentView.genderWoman.setTextColor(Color.parseColor("#191919"))
                    filterGenderBtn.setImageResource(R.drawable.icon_square_filter_gender)

                } else if (sp.getInt("filter_square_gender", 3) == 1) {
                    filterPopupWindow.contentView.genderMan.setTextColor(activity!!.resources.getColor(R.color.colorOrange))
                    filterPopupWindow.contentView.genderAll.setTextColor(Color.parseColor("#191919"))
                    filterPopupWindow.contentView.genderWoman.setTextColor(Color.parseColor("#191919"))
                    filterGenderBtn.setImageResource(R.drawable.icon_square_filter_man)

                } else if (sp.getInt("filter_square_gender", 3) == 2) {
                    filterPopupWindow.contentView.genderWoman.setTextColor(activity!!.resources.getColor(R.color.colorOrange))
                    filterPopupWindow.contentView.genderAll.setTextColor(Color.parseColor("#191919"))
                    filterPopupWindow.contentView.genderMan.setTextColor(Color.parseColor("#191919"))
                    filterGenderBtn.setImageResource(R.drawable.icon_square_filter_woman)

                }

            }
        }

    }

    /**
     * 更新选中的广场类型
     * 0-想认识 1-找同好 2-好友
     *
     */
    private fun updateChooseTitle() {
//        setViewState(LOADING)
        setTagData()
        listParams["type"] = when (checkedTitleId) {
            SQUARE_TAG -> {
                1
            }
            else -> {
                3
            }
        }
        refreshLayout.autoRefresh()
    }


    private fun initView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val param = customStatusBar.layoutParams as ConstraintLayout.LayoutParams
            param.height = BarUtils.getStatusBarHeight()
        } else {
            customStatusBar.isVisible = false
        }


        EventBus.getDefault().register(this)
        mPresenter = SquarePresenter()
        mPresenter.mView = this
        mPresenter.context = activity!!


        initHeadView()

        refreshLayout.setOnRefreshListener(this)
        refreshLayout.setOnLoadMoreListener(this)
        squareEdit.onClick(object : CustomClickListener() {
            override fun onSingleClick(view: View) {
                mPresenter.checkBlock()
            }
        })


        retryBtn.onClick {
            setViewState(LOADING)
//            这个地方还要默认设置选中第一个标签来更新数据
            mPresenter.getSquareList(listParams, true, true)
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
            view.isEnabled = false
            resetAudio()
            SquareCommentDetailActivity.start(activity!!, adapter.data[position], position = position)
            view.postDelayed({ view.isEnabled = true }, 1000L)
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


        mPresenter.getSquareList(listParams, true, true)

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


    override fun onStop() {
        super.onStop()
        resetAudio()
        Log.d("SquareFragment", "onStop")
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
        listParams["gender"] = sp.getInt("filter_square_gender", 3)

        resetAudio()

        refreshLayout.setNoMoreData(false)
        mPresenter.getSquareList(listParams, true)

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


    override fun onGetSquareListResult(data: SquareListBean?, result: Boolean, isRefresh: Boolean) {
        if (result) {
            adapter.removeAllHeaderView()
            if ((data?.banner_title ?: mutableListOf()).size > 0) {
                adapter.addHeaderView(initRecommendTopicHeader())
                topicAdapter.setNewData(data?.banner_title ?: mutableListOf<TopicBean>())
                adapter.headerLayout.isVisible = true
            } else {
                if (adapter.headerLayout != null)
                    adapter.headerLayout.isVisible = false
            }

            if (isRefresh) {
                adapter.data.clear()
                adapter.notifyDataSetChanged()
                squareDynamicRv.scrollToPosition(0)
            }
            setViewState(CONTENT)
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
                    if (checkedTitleId == SQUARE_TAG) {
                        adapter.emptyView.emptyFriendTitle.text = "暂时没有人了"
                        adapter.emptyView.emptyFriendTip.text = "一会儿再回来看看吧"
                        adapter.emptyView.emptyImg.setImageResource(R.drawable.icon_empty_friend)
                        adapter.emptyView.emptyFriendGoBtn.isVisible = false
                    } else {
                        adapter.emptyView.emptyImg.setImageResource(R.drawable.icon_empty_friend)
                        adapter.emptyView.emptyFriendTitle.text = "暂时没有动态"
                        adapter.emptyView.emptyFriendTip.text = "通过右滑匹配或主动打招呼去添加更多好友\n好友已发布的动态可在此直接查看"
                        adapter.emptyView.emptyFriendGoBtn.isVisible = false
                    }
                }

            refreshLayout.finishRefresh(result)
            refreshLayout.finishLoadMore(result)
            refreshLayout.setEnableLoadMoreWhenContentNotFull(false)
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun turnToLastLabelEvent(event: TurnToLastLabelEvent) {
        if (event.from == ChargeLabelDialog.FROM_SQUARE) {
            UserManager.saveGlobalLabelSquareId(0)
            onRefreshEvent(RefreshEvent(true))
        }
    }


    /**
     * 根据选择的标签切换广场内容
     */
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onUpdateLabelEvent(event: UpdateLabelEvent) {
        //这个地方还要默认设置选中第一个标签来更新数据
        if (refreshLayout.state == RefreshState.Refreshing) {
            refreshLayout.finishRefresh()
        }
        refreshLayout.autoRefresh()
    }

    /**
     * 全局筛选框筛选广场内容
     */
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onRefreshEvent(event: RefreshEvent) {
        squareDynamicRv.scrollToPosition(0)
        updateChooseTitle()
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
                if (!changeMarTop && adapter.headerLayout != null && adapter.headerLayout.isNotEmpty()) {
                    val params = adapter.headerLayout.recommendTitle.layoutParams as ConstraintLayout.LayoutParams
                    params.topMargin = SizeUtils.dp2px(45F)
                    adapter.headerLayout.recommendTitle.layoutParams = params
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
                if (adapter.headerLayout != null && adapter.headerLayout.isNotEmpty()) {
                    val params = adapter.headerLayout.recommendTitle.layoutParams as ConstraintLayout.LayoutParams
                    params.topMargin = SizeUtils.dp2px(10F)
                    adapter.headerLayout.recommendTitle.layoutParams = params
                }
            }, 500)
        } else {
            UserManager.cancelUpload = true
            uploadFl.isVisible = true
            if (adapter.headerLayout != null && adapter.headerLayout.isNotEmpty()) {
                val params = adapter.headerLayout.recommendTitle.layoutParams as ConstraintLayout.LayoutParams
                params.topMargin = SizeUtils.dp2px(45F)
                adapter.headerLayout.recommendTitle.layoutParams = params
            }
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

                    startActivity<ChooseLabelActivity>()
                    UserManager.publishState = 0
                    uploadFl.isVisible = false
                    if (adapter.headerLayout != null && adapter.headerLayout.isNotEmpty()) {
                        val params = adapter.headerLayout.recommendTitle.layoutParams as ConstraintLayout.LayoutParams
                        params.topMargin = SizeUtils.dp2px(10F)
                        adapter.headerLayout.recommendTitle.layoutParams = params
                    }
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
            //取消重新发布，清除本地所存下的发布的数据
            btnClose.onClick {
                uploadFl.isVisible = false
                if (adapter.headerLayout != null && adapter.headerLayout.isNotEmpty()) {
                    val params = adapter.headerLayout.recommendTitle.layoutParams as ConstraintLayout.LayoutParams
                    params.topMargin = SizeUtils.dp2px(10F)
                    adapter.headerLayout.recommendTitle.layoutParams = params
                }
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
                        if (adapter.headerLayout != null && adapter.headerLayout.isNotEmpty()) {
                            val params =
                                adapter.headerLayout.recommendTitle.layoutParams as ConstraintLayout.LayoutParams
                            params.topMargin = SizeUtils.dp2px(10F)
                            adapter.headerLayout.recommendTitle.layoutParams = params
                        }
                        UserManager.clearPublishParams()
                        if (!ActivityUtils.isActivityExistsInStack(PublishActivity::class.java))
                            if (event.context == UserCenterFragment::class.java.simpleName) {
                                startActivity<ChooseLabelActivity>("from" to 2)
                            } else {
                                startActivity<ChooseLabelActivity>()
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
                    startActivity<ChooseLabelActivity>("from" to 2)
                } else {
                    startActivity<ChooseLabelActivity>()
                }
            uploadFl.isVisible = false
            if (adapter.headerLayout != null && adapter.headerLayout.isNotEmpty()) {
                val params = adapter.headerLayout.recommendTitle.layoutParams as ConstraintLayout.LayoutParams
                params.topMargin = SizeUtils.dp2px(10F)
                adapter.headerLayout.recommendTitle.layoutParams = params
            }
        } else if (UserManager.publishState == 0) {
            if (event.context == UserCenterFragment::class.java.simpleName) {
                startActivity<ChooseLabelActivity>("from" to 2)
            } else {
                activity!!.intent.setClass(activity!!, ChooseLabelActivity::class.java)
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
                    topicAdapter.remove(data.getIntExtra("position", -1))
                }
//            mPresenter.getFrinedsList(friendsParams)
        }
    }


}

