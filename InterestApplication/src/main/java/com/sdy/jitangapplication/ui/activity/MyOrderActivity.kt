package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.tools.SdkVersionUtils
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.event.CommentPicEvent
import com.sdy.jitangapplication.presenter.MyOrderPresenter
import com.sdy.jitangapplication.presenter.view.MyOrderView
import com.sdy.jitangapplication.ui.adapter.MyOrderAdapter
import com.sdy.jitangapplication.ui.dialog.OrderCommentDialog
import kotlinx.android.synthetic.main.activity_my_order.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.greenrobot.eventbus.EventBus

/**
 * 我的订单
 */
class MyOrderActivity : BaseMvpActivity<MyOrderPresenter>(), MyOrderView, OnLoadMoreListener,
    OnRefreshListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_order)
        initView()
    }

    private val myOrderAdapter by lazy { MyOrderAdapter() }

    private fun initView() {
        hotT1.text = "我的订单"
        btnBack.onClick {
            finish()
        }

        stateOrder.retryBtn.onClick {

        }

        refreshOrder.setOnRefreshListener(this)
        refreshOrder.setOnLoadMoreListener(this)
        rvOrder.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rvOrder.adapter = myOrderAdapter

        myOrderAdapter.setOnItemClickListener { _, view, position ->
            OrderCommentDialog(this).show()
        }

    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        for (i in 0 until 10) {
            myOrderAdapter.addData("")
        }
        refreshLayout.finishLoadMore()
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        myOrderAdapter.data.clear()
        myOrderAdapter.notifyDataSetChanged()
        for (i in 0 until 10) {
            myOrderAdapter.addData("")
        }
        refreshLayout.finishRefresh()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 100) {
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
}
