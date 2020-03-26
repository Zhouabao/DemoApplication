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
import com.sdy.jitangapplication.ui.adapter.MessageProductAdapter
import kotlinx.android.synthetic.main.empty_layout_comment.view.*
import kotlinx.android.synthetic.main.fragment_message.*

/**
 * 留言
 */
class MessageFragment : BaseMvpLazyLoadFragment<MessagePresenter>(), MessageView,
    OnRefreshListener, OnLoadMoreListener {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_message, container, false)
    }

    private val messageProductAdapter = MessageProductAdapter()

    override fun loadData() {
        refreshMessage.setOnRefreshListener(this)
        refreshMessage.setOnLoadMoreListener(this)
        rvMessage.layoutManager = LinearLayoutManager(activity!!, RecyclerView.VERTICAL, false)
        rvMessage.adapter = messageProductAdapter
        messageProductAdapter.setEmptyView(R.layout.empty_layout_comment, rvMessage)
        messageProductAdapter.emptyView.tv1.isVisible = false
        messageProductAdapter.emptyView.emptyImg.setImageResource(R.drawable.icon_empty_message)
        messageProductAdapter.emptyView.emptyTip.text = "暂时还没有人留言"

        for (i in 0 until 10) {
            messageProductAdapter.addData("")
        }
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        messageProductAdapter.data.clear()
        messageProductAdapter.notifyDataSetChanged()
        for (i in 0 until 10) {
            messageProductAdapter.addData("")
        }
        refreshLayout.finishRefresh()
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        for (i in 0 until 10) {
            messageProductAdapter.addData("")
        }
        refreshLayout.finishLoadMore()
    }


}
