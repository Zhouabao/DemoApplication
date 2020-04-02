package com.sdy.jitangapplication.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kotlin.base.ui.fragment.BaseMvpLazyLoadFragment
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.constant.RefreshState
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.model.WantFriendBean
import com.sdy.jitangapplication.presenter.WantProductListPresenter
import com.sdy.jitangapplication.presenter.view.WantProductListView
import com.sdy.jitangapplication.ui.adapter.WantProductAdapter
import kotlinx.android.synthetic.main.empty_layout_comment.view.*
import kotlinx.android.synthetic.main.fragment_want_product_list.*

/**
 * 想要
 */
class WantProductListFragment(val goods_id: Int) :
    BaseMvpLazyLoadFragment<WantProductListPresenter>(),
    WantProductListView,
    OnRefreshListener, OnLoadMoreListener {

    private var page = 1
    val params by lazy {
        hashMapOf<String, Any>(
            "page" to page,
            "pagesize" to Constants.PAGESIZE,
            "goods_id" to goods_id
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_want_product_list, container, false)
    }

    private val wantProductAdapter = WantProductAdapter()


    override fun loadData() {
        mPresenter = WantProductListPresenter()
        mPresenter.mView = this
        mPresenter.context = activity!!

        refreshWant.setOnRefreshListener(this)
        refreshWant.setOnLoadMoreListener(this)
        rvWant.layoutManager = LinearLayoutManager(activity!!, RecyclerView.VERTICAL, false)
        rvWant.adapter = wantProductAdapter

        wantProductAdapter.setEmptyView(R.layout.empty_layout_comment, rvWant)
        wantProductAdapter.emptyView.tv1.isVisible = false
        wantProductAdapter.emptyView.emptyImg.setImageResource(R.drawable.icon_empty_message)
        wantProductAdapter.emptyView.emptyTip.text = "暂时还没有人留言"

        mPresenter.goodsWishList(params)
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        page = 1
        params["page"] = page
        mPresenter.goodsWishList(params)
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        page++
        params["page"] = page
        mPresenter.goodsWishList(params)
    }

    override fun onGoodsWishList(success: Boolean, data: MutableList<WantFriendBean>?) {
        if (refreshWant.state == RefreshState.Refreshing) {
            wantProductAdapter.data.clear()
            wantProductAdapter.notifyDataSetChanged()
            refreshWant.finishRefresh(data != null)
            refreshWant.resetNoMoreData()
            if (data.isNullOrEmpty()) {
                wantProductAdapter.isUseEmpty(true)
            }
        }
        if (refreshWant.state == RefreshState.Loading) {
            if ((data ?: mutableListOf<WantFriendBean>()).size < Constants.PAGESIZE)
                refreshWant.finishRefreshWithNoMoreData()
            else
                refreshWant.finishLoadMore(true)

        }
        if (data != null) {
            wantProductAdapter.addData(data)
        }

    }

}