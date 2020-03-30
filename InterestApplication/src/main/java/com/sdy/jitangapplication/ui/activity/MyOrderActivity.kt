package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.tools.SdkVersionUtils
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.constant.RefreshState
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.CommentPicEvent
import com.sdy.jitangapplication.event.RefreshOrderStateEvent
import com.sdy.jitangapplication.model.MyOrderBean
import com.sdy.jitangapplication.presenter.MyOrderPresenter
import com.sdy.jitangapplication.presenter.view.MyOrderView
import com.sdy.jitangapplication.ui.adapter.MyOrderAdapter
import com.sdy.jitangapplication.ui.dialog.OrderCommentDialog
import kotlinx.android.synthetic.main.activity_my_order.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 我的订单
 */
class MyOrderActivity : BaseMvpActivity<MyOrderPresenter>(), MyOrderView, OnLoadMoreListener,
    OnRefreshListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_order)
        initView()
        mPresenter.myGoodsList(params)
    }

    private val myOrderAdapter by lazy { MyOrderAdapter() }
    private var page = 1
    private val params by lazy {
        hashMapOf<String, Any>(
            "page" to page,
            "pagesize" to Constants.PAGESIZE
        )
    }

    private fun initView() {
        EventBus.getDefault().register(this)

        mPresenter = MyOrderPresenter()
        mPresenter.mView = this
        mPresenter.context = this

        hotT1.text = "我的订单"
        btnBack.onClick {
            finish()
        }

        stateOrder.retryBtn.onClick {
            stateOrder.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.myGoodsList(params)
        }

        refreshOrder.setOnRefreshListener(this)
        refreshOrder.setOnLoadMoreListener(this)
        rvOrder.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rvOrder.adapter = myOrderAdapter

        myOrderAdapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.orderState -> {
                    //状态 1等待发货 2已经退货 3确认收货 4已收货、
                    if (myOrderAdapter.data[position].state == 3)
                        OrderCommentDialog(this, position, myOrderAdapter.data[position].id).show()
                }
            }
        }
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        page++
        params["page"] = page
        mPresenter.myGoodsList(params)
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        page = 1
        params["page"] = page
        mPresenter.myGoodsList(params)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PictureConfig.CHOOSE_REQUEST) {
                val imgaes = mutableListOf<String>()
                if (!PictureSelector.obtainMultipleResult(data).isNullOrEmpty()) {
                    for (tdata in PictureSelector.obtainMultipleResult(data)) {
                        if (SdkVersionUtils.checkedAndroid_Q())
                            if (tdata.androidQToPath.isNullOrEmpty())
                                imgaes.add(tdata.path)
                            else
                                imgaes.add(tdata.androidQToPath)
                        else
                            imgaes.add(tdata.path)
                    }
                    EventBus.getDefault().post(CommentPicEvent(imgaes))
                }
            }
        }
    }

    override fun onMyGoodsList(success: Boolean, orders: MutableList<MyOrderBean>?) {
        if (success) {
            stateOrder.viewState = MultiStateView.VIEW_STATE_CONTENT
            if (refreshOrder.state == RefreshState.Refreshing) {
                myOrderAdapter.data.clear()
                myOrderAdapter.notifyDataSetChanged()
                refreshOrder.resetNoMoreData()
                refreshOrder.finishRefresh()
            }
            if (refreshOrder.state == RefreshState.Loading) {
                if ((orders ?: mutableListOf<MyOrderBean>()).size < Constants.PAGESIZE) {
                    refreshOrder.finishLoadMoreWithNoMoreData()
                } else {
                    refreshOrder.finishLoadMore(true)
                }
            }
            myOrderAdapter.addData(orders ?: mutableListOf())
        } else {
            stateOrder.viewState = MultiStateView.VIEW_STATE_ERROR
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshOrderStateEvent(event: RefreshOrderStateEvent) {
        myOrderAdapter.data[event.position].state = event.state
        myOrderAdapter.notifyItemChanged(event.position)
    }
}
