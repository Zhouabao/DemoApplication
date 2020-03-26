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
import com.sdy.jitangapplication.presenter.MessagePresenter
import com.sdy.jitangapplication.presenter.view.MessageView
import com.sdy.jitangapplication.ui.adapter.CommentProductAdapter
import kotlinx.android.synthetic.main.empty_layout_comment.view.*
import kotlinx.android.synthetic.main.fragment_comment.*

/**
 * 留言
 */
class CommentFragment : BaseMvpLazyLoadFragment<MessagePresenter>(), MessageView,
    OnRefreshListener, OnLoadMoreListener {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_comment, container, false)
    }
    private val commentProductAdapter = CommentProductAdapter()

    override fun loadData() {
        refreshcomment.setOnRefreshListener(this)
        refreshcomment.setOnLoadMoreListener(this)
        rvComment.layoutManager = LinearLayoutManager(activity!!, RecyclerView.VERTICAL, false)
        rvComment.adapter = commentProductAdapter

        commentProductAdapter.setEmptyView(R.layout.empty_layout_comment, rvComment)
        commentProductAdapter.emptyView.tv1.isVisible = false
        commentProductAdapter.emptyView.emptyImg.setImageResource(R.drawable.icon_message_comment)
        commentProductAdapter.emptyView.emptyTip.text = "暂时还没有人留言"
        for (i in 0 until 10) {
            commentProductAdapter.addData("")
        }
    }
    override fun onRefresh(refreshLayout: RefreshLayout) {
        commentProductAdapter.data.clear()
        commentProductAdapter.notifyDataSetChanged()
        for (i in 0 until 10) {
            commentProductAdapter.addData("")
        }
        refreshLayout.finishRefresh()
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        for (i in 0 until 10) {
            commentProductAdapter.addData("")
        }
        refreshLayout.finishLoadMore()
    }


}
