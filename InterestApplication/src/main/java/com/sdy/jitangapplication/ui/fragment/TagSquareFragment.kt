package com.sdy.jitangapplication.ui.fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SPUtils
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.fragment.BaseMvpLazyLoadFragment
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.constant.RefreshState
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.RefreshSquareEvent
import com.sdy.jitangapplication.event.UpdateLabelEvent
import com.sdy.jitangapplication.model.SquareTagBean
import com.sdy.jitangapplication.presenter.TagSquarePresenter
import com.sdy.jitangapplication.presenter.view.TagSquareView
import com.sdy.jitangapplication.ui.activity.TagDetailCategoryActivity
import com.sdy.jitangapplication.ui.adapter.TagSquareAdapter
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.fragment_tag_square.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.support.v4.startActivity

/**
 * 兴趣广场
 */
class TagSquareFragment : BaseMvpLazyLoadFragment<TagSquarePresenter>(), TagSquareView, OnRefreshListener {
    override fun loadData() {
        initView()

    }

    //广场列表内容适配器
    private val adapter by lazy { TagSquareAdapter() }

    val layoutManager by lazy { LinearLayoutManager(activity!!, RecyclerView.VERTICAL, false) }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tag_square, container, false)
    }


    private val sp by lazy { SPUtils.getInstance(Constants.SPNAME) }
    private fun initView() {
        EventBus.getDefault().register(this)
        mPresenter = TagSquarePresenter()
        mPresenter.mView = this
        mPresenter.context = activity!!

        refreshTagSquare.setOnRefreshListener(this)

        stateTagSquare.retryBtn.onClick {
            stateTagSquare.viewState = MultiStateView.VIEW_STATE_LOADING
//            这个地方还要默认设置选中第一个兴趣来更新数据
            mPresenter.getSquareList()
        }

        rvTagSquare.layoutManager = layoutManager
        rvTagSquare.adapter = adapter
        adapter.setHeaderAndEmpty(false)
        adapter.setEmptyView(R.layout.empty_friend_layout, rvTagSquare)
        adapter.isUseEmpty(false)
        adapter.bindToRecyclerView(rvTagSquare)

        adapter.setOnItemClickListener { _, view, position ->
            startActivity<TagDetailCategoryActivity>("id" to adapter.data[position].id, "type" to 1)
        }

        adapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.btnTagMore -> {

                }
            }
        }

        mPresenter.getSquareList()

    }


    override fun onRefresh(refreshTagSquare: RefreshLayout) {
        refreshTagSquare.setNoMoreData(false)
        mPresenter.getSquareList()

    }


    override fun onGetSquareTagResult(data: MutableList<SquareTagBean>?, result: Boolean) {
        if (result) {
            if (refreshTagSquare.state == RefreshState.Refreshing) {
                adapter.data.clear()
                adapter.notifyDataSetChanged()
            }
            stateTagSquare.viewState = MultiStateView.VIEW_STATE_CONTENT
            adapter.addData(data ?: mutableListOf())

        } else {
            stateTagSquare.viewState = MultiStateView.VIEW_STATE_ERROR
            stateTagSquare.errorMsg.text = if (mPresenter.checkNetWork()) {
                activity!!.getString(R.string.retry_load_error)
            } else {
                activity!!.getString(R.string.retry_net_error)
            }
            adapter.notifyDataSetChanged()
        }
        refreshTagSquare.finishRefresh(result)
    }


    override fun showLoading() {
        stateTagSquare.viewState = MultiStateView.VIEW_STATE_LOADING
    }


    /***************************事件总线******************************/
    /**
     * 根据选择的兴趣切换广场内容
     */
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onUpdateLabelEvent(event: UpdateLabelEvent) {
        //这个地方还要默认设置选中第一个兴趣来更新数据
        if (refreshTagSquare.state == RefreshState.Refreshing) {
            refreshTagSquare.finishRefresh()
        }
        refreshTagSquare.autoRefresh()
    }


    /**
     * 更新广场
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshSquareEvent(event: RefreshSquareEvent) {
        refreshTagSquare.autoRefresh()
    }


    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

}
