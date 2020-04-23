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
import com.sdy.jitangapplication.event.RefreshGoodsMessageEvent
import com.sdy.jitangapplication.model.ProductMsgBean
import com.sdy.jitangapplication.presenter.MessagePresenter
import com.sdy.jitangapplication.presenter.view.MessageView
import com.sdy.jitangapplication.ui.activity.MatchDetailActivity
import com.sdy.jitangapplication.ui.adapter.MessageProductAdapter
import kotlinx.android.synthetic.main.empty_layout_comment.view.*
import kotlinx.android.synthetic.main.fragment_message.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 留言
 */
class MessageFragment(val goods_id: Int) : BaseMvpLazyLoadFragment<MessagePresenter>(), MessageView,
    OnRefreshListener, OnLoadMoreListener {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_message, container, false)
    }

    private val messageProductAdapter = MessageProductAdapter()
    private var page = 1
    val params by lazy {
        hashMapOf<String, Any>(
            "page" to page,
            "pagesize" to Constants.PAGESIZE,
            "goods_id" to goods_id
        )
    }

    override fun loadData() {
        EventBus.getDefault().register(this)

        mPresenter = MessagePresenter()
        mPresenter.mView = this
        mPresenter.context = activity!!

        refreshMessage.setOnRefreshListener(this)
        refreshMessage.setOnLoadMoreListener(this)
        rvMessage.layoutManager = LinearLayoutManager(activity!!, RecyclerView.VERTICAL, false)
        rvMessage.adapter = messageProductAdapter
        messageProductAdapter.setEmptyView(R.layout.empty_layout_comment, rvMessage)
        messageProductAdapter.emptyView.tv1.isVisible = false
        messageProductAdapter.emptyView.emptyImg.setImageResource(R.drawable.icon_empty_message)
        messageProductAdapter.emptyView.emptyTip.text = "暂时还没有人留言"
        messageProductAdapter.isUseEmpty(false)
        mPresenter.goodsMsgList(params)

        messageProductAdapter.setOnItemClickListener { _, view, position ->
            view.isEnabled = false
            MatchDetailActivity.start(activity!!, messageProductAdapter.data[position].accid)
            view.postDelayed({ view.isEnabled = false }, 2000L)
        }

    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        page = 1
        params["page"] = page
        mPresenter.goodsMsgList(params)

    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        page++
        params["page"] = page
        mPresenter.goodsMsgList(params)
    }

    override fun onGoodsMsgListList(b: Boolean, data: MutableList<ProductMsgBean>?) {
        if (refreshMessage.state != RefreshState.Loading) {
            messageProductAdapter.data.clear()
            messageProductAdapter.notifyDataSetChanged()
            if (b && (data ?: mutableListOf()).size == 0) {
                messageProductAdapter.isUseEmpty(true)
            }

            refreshMessage.finishRefresh(b)
        } else {
            if (b && (data ?: mutableListOf()).size < Constants.PAGESIZE)
                refreshMessage.finishRefreshWithNoMoreData()
            else
                refreshMessage.finishLoadMore(b)
        }

        messageProductAdapter.addData(data ?: mutableListOf())
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshGoodsMessageEvent(event: RefreshGoodsMessageEvent) {
        page = 1
        params["page"] = page
        mPresenter.goodsMsgList(params)
    }

}
