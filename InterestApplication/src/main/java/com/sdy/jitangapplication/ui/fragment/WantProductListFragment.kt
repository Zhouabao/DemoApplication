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
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.presenter.WantProductListPresenter
import com.sdy.jitangapplication.presenter.view.WantProductListView
import com.sdy.jitangapplication.ui.adapter.WantProductAdapter
import kotlinx.android.synthetic.main.empty_layout_comment.view.*
import kotlinx.android.synthetic.main.fragment_want_product_list.*

/**
 * 想要
 */
class WantProductListFragment : BaseMvpLazyLoadFragment<WantProductListPresenter>(),
    WantProductListView,
    OnRefreshListener, OnLoadMoreListener {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_want_product_list, container, false)
    }

    private val wantProductAdapter = WantProductAdapter()


    override fun loadData() {
        refreshWant.setOnRefreshListener(this)
        refreshWant.setOnLoadMoreListener(this)
        rvWant.layoutManager = LinearLayoutManager(activity!!, RecyclerView.VERTICAL, false)
        rvWant.adapter = wantProductAdapter


        wantProductAdapter.setEmptyView(R.layout.empty_layout_comment, rvWant)
        wantProductAdapter.emptyView.tv1.isVisible = false
        wantProductAdapter.emptyView.emptyImg.setImageResource(R.drawable.icon_empty_message)
        wantProductAdapter.emptyView.emptyTip.text = "暂时还没有人留言"

        wantProductAdapter.isUseEmpty(true)

//        for (i in 0 until 10) {
//            wantProductAdapter.addData("")
//        }
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        wantProductAdapter.data.clear()
        wantProductAdapter.notifyDataSetChanged()
        for (i in 0 until 10) {
            wantProductAdapter.addData("")
        }
        refreshLayout.finishRefresh()
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        for (i in 0 until 10) {
            wantProductAdapter.addData("")
        }
        refreshLayout.finishLoadMore()
    }

}
