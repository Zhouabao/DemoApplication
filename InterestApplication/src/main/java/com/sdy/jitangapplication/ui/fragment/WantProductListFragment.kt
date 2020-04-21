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
import com.sdy.jitangapplication.event.MatchByWishHelpEvent
import com.sdy.jitangapplication.model.GiftBean
import com.sdy.jitangapplication.model.WantFriendBean
import com.sdy.jitangapplication.presenter.WantProductListPresenter
import com.sdy.jitangapplication.presenter.view.WantProductListView
import com.sdy.jitangapplication.ui.activity.MatchDetailActivity
import com.sdy.jitangapplication.ui.adapter.WantProductAdapter
import com.sdy.jitangapplication.ui.dialog.HelpWishDialog
import kotlinx.android.synthetic.main.empty_layout_comment.view.*
import kotlinx.android.synthetic.main.fragment_want_product_list.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 想要
 */
class WantProductListFragment(
    val goods_id: Int,
    var myCandyAmount: Int = 0,
    var giftBean: GiftBean = GiftBean()
) :
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
        EventBus.getDefault().register(this)

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
        wantProductAdapter.isUseEmpty(false)

        wantProductAdapter.setOnItemChildClickListener { _, view, position ->
            view.isEnabled = false
            when (view.id) {
                R.id.donate -> {
                    HelpWishDialog(
                        myCandyAmount,
                        wantProductAdapter.data[position].accid,
                        wantProductAdapter.data[position].nickname,
                        giftBean,
                        activity!!,
                        wantProductAdapter.data[position].isfriend
                    ).show()
                }
                R.id.wantAvator -> {
                    MatchDetailActivity.start(activity!!,wantProductAdapter.data[position].accid)
                }

            }
            view.postDelayed({view.isEnabled =true},2000L)
        }


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
        if (refreshWant.state == RefreshState.Loading) {
            if ((data ?: mutableListOf()).size < Constants.PAGESIZE) {
                refreshWant.finishRefreshWithNoMoreData()
            } else {
                refreshWant.finishLoadMore(true)
            }
        } else {
            wantProductAdapter.data.clear()
            wantProductAdapter.notifyDataSetChanged()
            refreshWant.finishRefresh(data != null)
            refreshWant.resetNoMoreData()
            if (data.isNullOrEmpty()) {
                wantProductAdapter.isUseEmpty(true)
            }
        }
        if (data != null) {
            wantProductAdapter.addData(data)
        }

    }


    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMatchByWishHelpEvent(event: MatchByWishHelpEvent) {
        for (data in wantProductAdapter.data.withIndex()) {
            if (data.value.accid == event.target_accid) {
                data.value.isfriend = true
                data.value.ship_str="他是你的好友"
                wantProductAdapter.notifyItemChanged(data.index)
                break
            }
        }
    }
}
