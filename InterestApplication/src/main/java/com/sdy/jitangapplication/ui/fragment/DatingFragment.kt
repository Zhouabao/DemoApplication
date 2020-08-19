package com.sdy.jitangapplication.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.fragment.BaseMvpFragment
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.constant.RefreshState
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.DatingOnePlayEvent
import com.sdy.jitangapplication.event.DatingStopPlayEvent
import com.sdy.jitangapplication.model.CheckBean
import com.sdy.jitangapplication.model.DatingBean
import com.sdy.jitangapplication.presenter.DatingPresenter
import com.sdy.jitangapplication.presenter.view.DatingView
import com.sdy.jitangapplication.ui.activity.DatingDetailActivity
import com.sdy.jitangapplication.ui.adapter.DatingSquareAdapter
import com.sdy.jitangapplication.ui.adapter.TodayWantAdapter
import com.sdy.jitangapplication.ui.dialog.TouristDialog
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.empty_friend_layout.view.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.fragment_dating.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 约会
 */
class DatingFragment : BaseMvpFragment<DatingPresenter>(), DatingView,
    OnRefreshListener, OnLoadMoreListener {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadData()
    }

    fun loadData() {
        initView()
        mPresenter.getIntention()
    }


    //广场列表内容适配器
    private val adapter by lazy { DatingSquareAdapter() }
    val layoutManager by lazy { LinearLayoutManager(activity!!, RecyclerView.VERTICAL, false) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dating, container, false)
    }


    private fun initView() {
        EventBus.getDefault().register(this)
        mPresenter = DatingPresenter()
        mPresenter.mView = this
        mPresenter.context = activity!!

        refreshDatingSquare.setOnRefreshListener(this)
        refreshDatingSquare.setOnLoadMoreListener(this)

        stateDatingSquare.retryBtn.onClick {
            stateDatingSquare.viewState = MultiStateView.VIEW_STATE_LOADING
//            这个地方还要默认设置选中第一个兴趣来更新数据

        }

        initDatingHeaderView()
        rvDatingSquare.layoutManager = layoutManager
        rvDatingSquare.adapter = adapter
        adapter.setHeaderAndEmpty(false)
        adapter.setEmptyView(R.layout.empty_friend_layout, rvDatingSquare)
        adapter.emptyView.emptyFriendTitle.text = "什么也没有"
        adapter.emptyView.emptyFriendTip.text = "一会儿再回来看看吧"
        adapter.emptyView.emptyImg.setImageResource(R.drawable.icon_empty_friend)
        adapter.isUseEmpty(false)
        adapter.bindToRecyclerView(rvDatingSquare)

        adapter.setOnItemClickListener { _, view, position ->
            if (UserManager.touristMode) {
                TouristDialog(activity!!).show()
            } else {
                DatingDetailActivity.start2Detail(activity!!, adapter.data[position].id)
            }

        }

    }

    /**
     * 初始化顶部约会项目数据
     */
    private val datingTypeAdapter by lazy { TodayWantAdapter(true) }
    private fun initDatingHeaderView() {
        headDatingRv.layoutManager = LinearLayoutManager(activity!!, RecyclerView.HORIZONTAL, false)
        headDatingRv.adapter = datingTypeAdapter
        datingTypeAdapter.setOnItemClickListener { _, view, position ->
            checkPosi = position
            checkWantId = datingTypeAdapter.data[position].id
            for (data in datingTypeAdapter.data) {
                data.checked = data == datingTypeAdapter.data[position]
            }
            datingTypeAdapter.notifyDataSetChanged()

            refreshDatingSquare.autoRefresh()
        }
    }


    private var page = 1
    override fun onRefresh(refreshTagSquare: RefreshLayout) {
        adapter.resetMyAudioViews()
        refreshTagSquare.resetNoMoreData()
        page = 1
        mPresenter.getIntention()
    }


    override fun onLoadMore(refreshLayout: RefreshLayout) {
        page++
        mPresenter.getDatingList(page, checkWantId)
    }

    override fun onGetSquareDatingResult(data: MutableList<DatingBean>?, result: Boolean) {
        if (result) {
            stateDatingSquare.viewState = MultiStateView.VIEW_STATE_CONTENT
            if (refreshDatingSquare.state == RefreshState.Loading) {
                refreshDatingSquare.finishLoadMore(result)
            } else {
                adapter.data.clear()
                rvDatingSquare.scrollToPosition(0)
                if (data.isNullOrEmpty()) {
                    adapter.isUseEmpty(true)
                }
                refreshDatingSquare.finishRefresh(result)
            }
            adapter.addData(data ?: mutableListOf())
            adapter.notifyDataSetChanged()
            if (adapter.data.size < Constants.PAGESIZE * page)
                refreshDatingSquare.finishLoadMoreWithNoMoreData()
        } else {
            stateDatingSquare.viewState = MultiStateView.VIEW_STATE_ERROR
            stateDatingSquare.errorMsg.text = if (mPresenter.checkNetWork()) {
                activity!!.getString(R.string.retry_load_error)
            } else {
                activity!!.getString(R.string.retry_net_error)
            }
            adapter.notifyDataSetChanged()
        }

    }

    private var checkWantId = -1
    private var checkPosi = 0
    override fun onGetIntentionResult(result: MutableList<CheckBean>?) {
        if (!result.isNullOrEmpty()) {
            var hasCheck = false
            if (checkWantId != -1) {
                for (data in result!!.withIndex()) {
                    if (data.value.id == checkWantId) {
                        data.value.checked = true
                        checkPosi = data.index
                        checkWantId = data.value.id
                        hasCheck = true
                        break
                    }
                }
            }
            if (!hasCheck) {
                result[0].checked = true
                checkWantId = result[0].id
                checkPosi = 0
            }
            datingTypeAdapter.setNewData(result)
        } else {
            adapter.removeAllHeaderView()
        }

        mPresenter.getDatingList(page, checkWantId)
    }


    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }


    override fun onStop() {
        super.onStop()
        adapter.resetMyAudioViews()
    }

    override fun onPause() {
        super.onPause()
        adapter.resetMyAudioViews()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateFindByTagEvent(eve: DatingStopPlayEvent) {
        adapter.resetMyAudioViews()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDatingOnePlayEvent(eve: DatingOnePlayEvent) {
        adapter.notifySomeOneAudioView(eve.positionId)
    }

}
