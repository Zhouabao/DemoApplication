package com.sdy.jitangapplication.ui.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.kennyc.view.MultiStateView
import com.kotlin.base.ui.fragment.BaseMvpLazyLoadFragment
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.constant.RefreshState
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.event.UpdateNearPeopleParamsEvent
import com.sdy.jitangapplication.model.NearPersonBean
import com.sdy.jitangapplication.presenter.PeopleNearbyPresenter
import com.sdy.jitangapplication.presenter.view.PeopleNearbyView
import com.sdy.jitangapplication.ui.activity.MatchDetailActivity
import com.sdy.jitangapplication.ui.adapter.PeopleNearbyAdapter
import com.sdy.jitangapplication.ui.dialog.NearPeopleFilterDialog
import kotlinx.android.synthetic.main.empty_friend_layout.view.*
import kotlinx.android.synthetic.main.fragment_people_nearby.*
import kotlinx.android.synthetic.main.header_item_near_by.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.textColor
import kotlin.math.abs

/**
 * 附近的人
 */
class PeopleNearbyFragment : BaseMvpLazyLoadFragment<PeopleNearbyPresenter>(), PeopleNearbyView,
    OnRefreshListener, OnLoadMoreListener {

    private val adapter by lazy { PeopleNearbyAdapter() }

    private var page = 1
    private val params by lazy {
        hashMapOf<String, Any>(
            "page" to page,
            "pagesize" to Constants.PAGESIZE
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_people_nearby, container, false)
    }

    private val linearLayoutManager by lazy {
        LinearLayoutManager(
            activity!!,
            RecyclerView.VERTICAL,
            false
        )
    }

    override fun loadData() {
        EventBus.getDefault().register(this)

        mPresenter = PeopleNearbyPresenter()
        mPresenter.mView = this
        mPresenter.context = activity!!

        refreshPeopleNearby.setOnRefreshListener(this)
        refreshPeopleNearby.setOnLoadMoreListener(this)


        nearFilterLl.clickWithTrigger {
            NearPeopleFilterDialog(activity!!).show()
        }

        rvPeopleNearby.layoutManager = linearLayoutManager
        rvPeopleNearby.adapter = adapter
        adapter.addHeaderView(initHeadView())
        adapter.setEmptyView(R.layout.empty_friend_layout, rvPeopleNearby)
        adapter.emptyView.emptyFriendTitle.text = "这里暂时没有人"
        adapter.emptyView.emptyFriendTip.text = "过会儿再来看看吧"
        adapter.emptyView.emptyImg.setImageResource(R.drawable.icon_empty_friend)
        adapter.setHeaderAndEmpty(true)
        adapter.setOnItemClickListener { _, view, position ->
            MatchDetailActivity.start(activity!!, adapter.data[position].accid)
        }

        rvPeopleNearby.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                nearFilterLl.isVisible =
                    abs(recyclerView.computeVerticalScrollOffset()) >= SizeUtils.dp2px(72f)
            }
        })

        mPresenter.nearlyIndex(params)
    }

    //附近的人筛选条件
    fun initHeadView(): View {
        val headNearBy = layoutInflater.inflate(R.layout.header_item_near_by, rvPeopleNearby, false)
        headNearBy.clickWithTrigger {
            NearPeopleFilterDialog(activity!!).show()
        }
        return headNearBy
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        page = 1
        params["page"] = page
        refreshPeopleNearby.resetNoMoreData()
        mPresenter.nearlyIndex(params)
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        page += 1
        params["page"] = page
        mPresenter.nearlyIndex(params)
    }

    override fun nearlyIndexResult(success: Boolean, mutableList: MutableList<NearPersonBean>?) {
        if (success) {
            statePeopleNearby.viewState = MultiStateView.VIEW_STATE_CONTENT
            if (refreshPeopleNearby.state == RefreshState.Refreshing) {
                adapter.data.clear()
                adapter.notifyDataSetChanged()
                rvPeopleNearby.scrollToPosition(0)
                refreshPeopleNearby.finishRefresh(success)
            }
            if (mutableList?.size ?: 0 < Constants.PAGESIZE) {
                refreshPeopleNearby.finishLoadMoreWithNoMoreData()
            } else {
                refreshPeopleNearby.finishLoadMore(true)
            }
            adapter.addData(mutableList ?: mutableListOf())

            if (adapter.data.isNullOrEmpty()) {
                adapter.isUseEmpty(true)
            }
        } else {
            statePeopleNearby.viewState = MultiStateView.VIEW_STATE_ERROR
        }

    }


    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateNearPeopleParamsEvent(event: UpdateNearPeopleParamsEvent) {
        if (event.params.isEmpty()) {
            nearFilterContent.text = "同城筛选"
            nearFilterContent.textColor = Color.parseColor("#191919")
            adapter.headerLayout.headFilterContent.setCompoundDrawablesWithIntrinsicBounds(
                activity!!.resources.getDrawable(
                    R.drawable.icon_filter_small
                ), null, null, null
            )
            adapter.headerLayout.headFilterContent.text = "同城筛选"
            adapter.headerLayout.headFilterContent.textColor = Color.parseColor("#191919")
            nearFilterContent.setCompoundDrawablesWithIntrinsicBounds(
                activity!!.resources.getDrawable(
                    R.drawable.icon_filter_small
                ), null, null, null
            )
            params.clear()
            params["page"] = 1
            params["pagesize"] = Constants.PAGESIZE
        } else {
//            1智能 2距离 3在线
            adapter.headerLayout.headFilterContent.text =
                "已筛选\t${when (event.params["rank_type_nearly"]) {
                    1 -> "智能推荐"
                    2 -> "距离优先"
                    else -> "在线优先"
                }}\t${when (event.params["gender_nearly"]) {
                    1 -> "男"
                    2 -> "女"
                    else -> "性别不限"
                }}\t${event.params["limit_age_low_nearly"]}-${event.params["limit_age_high_nearly"]}岁"
            adapter.headerLayout.headFilterContent.textColor = Color.parseColor("#FF6318")
            adapter.headerLayout.headFilterContent.setCompoundDrawablesWithIntrinsicBounds(
                activity!!.resources.getDrawable(
                    R.drawable.icon_filter_orange_small
                ), null, null, null
            )
            nearFilterContent.text = adapter.headerLayout.headFilterContent.text
            nearFilterContent.textColor = Color.parseColor("#FF6318")
            nearFilterContent.setCompoundDrawablesWithIntrinsicBounds(
                activity!!.resources.getDrawable(
                    R.drawable.icon_filter_orange_small
                ), null, null, null
            )
            params.putAll(event.params)
        }
        refreshPeopleNearby.autoRefresh()
    }
}
