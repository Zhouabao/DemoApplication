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
import com.sdy.jitangapplication.model.ProductCommentBean
import com.sdy.jitangapplication.presenter.CommentPresenter
import com.sdy.jitangapplication.presenter.view.CommentView
import com.sdy.jitangapplication.ui.adapter.CommentProductAdapter
import kotlinx.android.synthetic.main.empty_layout_comment.view.*
import kotlinx.android.synthetic.main.fragment_comment.*

/**
 * 评论
 */
class CommentFragment(val goods_id: Int) : BaseMvpLazyLoadFragment<CommentPresenter>(), CommentView,
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_comment, container, false)
    }

    private val commentProductAdapter = CommentProductAdapter()

    override fun loadData() {
        mPresenter = CommentPresenter()
        mPresenter.mView = this
        mPresenter.context = activity!!

        refreshcomment.setOnRefreshListener(this)
        refreshcomment.setOnLoadMoreListener(this)
        rvComment.layoutManager = LinearLayoutManager(activity!!, RecyclerView.VERTICAL, false)
        rvComment.adapter = commentProductAdapter

        commentProductAdapter.setEmptyView(R.layout.empty_layout_comment, rvComment)
        commentProductAdapter.emptyView.tv1.isVisible = false
        commentProductAdapter.emptyView.emptyImg.setImageResource(R.drawable.icon_message_comment)
        commentProductAdapter.emptyView.emptyTip.text = "暂时还没有评价"
        commentProductAdapter.isUseEmpty(false)
        mPresenter.goodscommentsList(params)
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        page = 1
        params["page"] = page
        mPresenter.goodscommentsList(params)
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        page++
        params["page"] = page
        mPresenter.goodscommentsList(params)
    }


    override fun onGoodscommentsList(b: Boolean, data: MutableList<ProductCommentBean>?) {
        if (refreshcomment.state == RefreshState.Refreshing) {
            commentProductAdapter.data.clear()
            commentProductAdapter.notifyDataSetChanged()
            if (b && (data ?: mutableListOf()).size == 0) {
                commentProductAdapter.isUseEmpty(true)
            }

            refreshcomment.finishRefresh(b)
        }

        if (refreshcomment.state == RefreshState.Loading) {
            if (b && (data ?: mutableListOf()).size < Constants.PAGESIZE)
                refreshcomment.finishRefreshWithNoMoreData()
            else
                refreshcomment.finishLoadMore(b)
        }

        commentProductAdapter.addData(data ?: mutableListOf())
    }


}
