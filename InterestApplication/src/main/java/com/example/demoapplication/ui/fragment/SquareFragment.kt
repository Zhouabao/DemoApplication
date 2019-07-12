package com.example.demoapplication.ui.fragment


import android.app.Activity.RESULT_OK
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
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
import com.example.demoapplication.model.FriendBean
import com.example.demoapplication.model.LabelBean
import com.example.demoapplication.model.SquareBean
import com.example.demoapplication.model.SquareListBean
import com.example.demoapplication.presenter.SquarePresenter
import com.example.demoapplication.presenter.view.SquareView
import com.example.demoapplication.ui.activity.LabelsActivity
import com.example.demoapplication.ui.adapter.MatchLabelAdapter
import com.example.demoapplication.ui.adapter.MultiListSquareAdapter
import com.example.demoapplication.ui.adapter.SquareFriendsAdapter
import com.example.demoapplication.ui.dialog.MoreActionDialog
import com.example.demoapplication.ui.dialog.TranspondDialog
import com.example.demoapplication.utils.ScrollCalculatorHelper
import com.example.demoapplication.utils.SharedPreferenceUtil
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.fragment.BaseMvpFragment
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.shuyu.gsyvideoplayer.GSYVideoManager
import kotlinx.android.synthetic.main.dialog_more_action.*
import kotlinx.android.synthetic.main.fragment_square.*
import org.jetbrains.anko.support.v4.startActivityForResult
import org.jetbrains.anko.support.v4.toast
import tv.danmaku.ijk.media.player.IjkMediaPlayer


/**
 * 广场列表
 * //todo 发布列表接口接入
 */
class SquareFragment : BaseMvpFragment<SquarePresenter>(), SquareView, OnRefreshListener, OnLoadMoreListener {

    companion object {
        val REQUEST_LABEL_CODE = 2000
    }

    //广场列表内容适配器
    private val adapter by lazy { MultiListSquareAdapter(activity!!, mutableListOf()) }
    //标签适配器
    private val labelAdapter: MatchLabelAdapter by lazy { MatchLabelAdapter(context!!) }
    //广场好友适配器
    private val friendsAdapter: SquareFriendsAdapter by lazy { SquareFriendsAdapter(userList) }
    //好友信息用户数据源
    var userList: MutableList<FriendBean> = mutableListOf()
    //标签数据源
    var labelList: MutableList<LabelBean> = mutableListOf()

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_square, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()

        initData()
    }

    private fun initData() {
        labelList = getSpLabels()
        if (labelList.size > 0)
            labelList[0].checked = true
        labelAdapter.setData(labelList)
    }

    private fun getSpLabels(): MutableList<LabelBean> {
        val tempLabels = mutableListOf<LabelBean>()
        if (SPUtils.getInstance(Constants.SPNAME).getStringSet("checkedLabels").isNotEmpty()) {
            (SPUtils.getInstance(Constants.SPNAME).getStringSet("checkedLabels")).forEach {
                tempLabels.add(SharedPreferenceUtil.String2Object(it) as LabelBean)
            }
        }
        return tempLabels
    }


    private fun initView() {
        mPresenter = SquarePresenter()
        mPresenter.mView = this
        mPresenter.context = activity!!
        refreshLayout.setOnRefreshListener(this)
        refreshLayout.setOnLoadMoreListener(this)

        initHeadView()

        squareDynamicRv.layoutManager = layoutManager
        squareDynamicRv.adapter = adapter

        //限定范围为屏幕一半的上下偏移180
        val playTop = ScreenUtils.getScreenHeight() / 2 - SizeUtils.dp2px(252F)
        val playBottom = ScreenUtils.getScreenHeight() / 2 + SizeUtils.dp2px(252F)
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
                scrollCalculatorHelper.onScroll(
                    recyclerView,
                    firstVisibleItem,
                    lastVisibleItem,
                    lastVisibleItem - firstVisibleItem
                )

                //大于0说明有播放
                if (GSYVideoManager.instance().playPosition >= 0) {
                    //当前播放的位置
                    val position = GSYVideoManager.instance().playPosition
                    //对应的播放列表TAG
                    if (GSYVideoManager.instance().playTag.equals(MultiListSquareAdapter.TAG) && (position < firstVisibleItem || position > lastVisibleItem)) {
                        //如果滑出去了就是否
                        if (!GSYVideoManager.isFullState(activity)) {
                            GSYVideoManager.releaseAllVideos()
                            adapter.notifyDataSetChanged()
                        }
                    }
                }
            }
        })

        adapter.setOnItemClickListener { adapter, view, position ->
            //            startActivity<SquareDetailActivity>("matchbean" to adapter.data[position])
        }

        adapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.squareChatBtn1 -> {
                    toast("聊天呗$position")
                }
                R.id.squareCommentBtn1 -> {
                    toast("评论$position")
                }
                R.id.squareDianzanBtn1 -> {
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
                R.id.squareZhuanfaBtn1 -> {
                    showTranspondDialog()
                }
                R.id.squareMoreBtn1 -> {
                    showMoreDialog(position)
                }
                R.id.audioPlayBtn -> {
                    initAudio()
                    ijkMediaPlayer!!.setDataSource(context, Uri.parse("http://up.mcyt.net/down/47541.mp3"))
                    adapter.data[position].isPlayAudio = !adapter.data[position].isPlayAudio
                    for (index in 0 until adapter.data.size) {
                        if (index != position) {
                            adapter.data[index].isPlayAudio = false
                        }
                    }
                    if (adapter.data[position].isPlayAudio) {
                        ijkMediaPlayer!!.prepareAsync()
                        ijkMediaPlayer!!.start()
                    } else {
                        ijkMediaPlayer!!.pause()
                    }
                    adapter.notifyDataSetChanged()

                }
            }
        }


        //这个地方还要默认设置选中第一个标签来更新数据
        mPresenter.getSquareList(listParams, true)

        mPresenter.getFrinedsList(
            hashMapOf(
//                "accid" to Constants.ACCID,
//                "token" to Constants.TOKEN
                "accid" to SPUtils.getInstance(Constants.SPNAME).getString("accid"),
                "token" to SPUtils.getInstance(Constants.SPNAME).getString("token")
            )
        )

    }

    var ijkMediaPlayer: IjkMediaPlayer? = null
    fun initAudio() {
        ijkMediaPlayer?.release()
        ijkMediaPlayer = IjkMediaPlayer()
        ijkMediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)

    }


    /**
     * 好友列表和标签列表
     * 设置头部数据一直居于最顶端
     */
    private fun initHeadView() {
        val labelManager = LinearLayoutManager(activity?.applicationContext, LinearLayoutManager.HORIZONTAL, false)
        headRvLabels.layoutManager = labelManager
        headRvLabels.adapter = labelAdapter
        labelAdapter.dataList = labelList
        labelAdapter.setOnItemClickListener(object : MatchLabelAdapter.OnItemClickListener {
            override fun onItemClick(item: View, position: Int) {
                if (position == 0) {
                    startActivityForResult<LabelsActivity>(REQUEST_LABEL_CODE, "from" to "squarefragment")
                } else {
                    for (index in 0 until labelAdapter.dataList.size) {
                        labelAdapter.dataList[index].checked = index == position - 1
                    }
                    labelAdapter.notifyDataSetChanged()
                    listParams["tagid"] = labelList[position - 1].id
                    //这个地方还要默认设置选中第一个标签来更新数据
                    mPresenter.getSquareList(listParams, true)
                }
            }

        })


        val linearLayoutManager =
            LinearLayoutManager(activity?.applicationContext, LinearLayoutManager.HORIZONTAL, false)
        headRvFriends.layoutManager = linearLayoutManager
        headRvFriends.adapter = friendsAdapter
        friendsAdapter.setOnItemClickListener { adapter, view, position ->

            toast("${adapter.data[position]}")
        }
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
            //todo 发起举报请求
            toast("举报${adapter.data[position].id}")
        }
        moreActionDialog.cancel.onClick {
            moreActionDialog.dismiss()
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
        GSYVideoManager.releaseAllVideos()
        ijkMediaPlayer?.release()

    }


    override fun onRefresh(refreshLayout: RefreshLayout) {
        page = 1
        listParams["page"] = page
//        adapter.data.clear()
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
                    !data!!.data[tempData].photo_json.isNullOrEmpty() -> SquareBean.PIC
                    else -> SquareBean.VIDEO
                }
            }
//            if (refresh)
//                adapter.setNewData(data!!.data)
//            else
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
            adapter.notifyItemChanged(position)
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

    override fun onGetSquareJubaoResult(position: Int, result: Boolean) {
        toast("举报成功！")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_LABEL_CODE) {
                val list = getSpLabels()
                for (i in 0 until labelList.size) {
                    for (j in 0 until list.size) {
                        if (labelList[i].id == list[j].id) {
                            list[j].checked = labelList[i].checked
                        }
                    }
                }
                labelList = list
                labelAdapter.setData(labelList)
            }
        }
    }
}

